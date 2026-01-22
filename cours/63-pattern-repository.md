# Chapitre 13.1 - Pattern Repository

## Objectifs du chapitre

- Comprendre le pattern Repository
- Voir son implementation avec Spring Data
- Connaitre les bonnes pratiques

---

## 1. Definition

### Qu'est-ce que le pattern Repository?

Le **Repository** est un pattern qui abstrait l'acces aux donnees. Il fait le lien entre le domaine metier et la couche de persistance.

### Objectif

Isoler la logique metier des details techniques de stockage.

```
Service                  Repository              Base de donnees
(logique metier)  <--->  (abstraction)    <--->  (PostgreSQL, MongoDB...)
```

---

## 2. Avantages

### 2.1 Separation des preoccupations

Le service ne connait pas les details de la base de donnees.

```java
// Le service ne sait pas si c'est SQL, MongoDB, ou un fichier
public class LeadService {
    private final LeadRepository repository;
    
    public List<Lead> getActiveLeads() {
        return repository.findByStatus(LeadStatus.NEW);
    }
}
```

### 2.2 Testabilite

Facile de mocker le repository pour les tests.

```java
@Test
void shouldReturnActiveLeads() {
    // Mock du repository
    when(repository.findByStatus(LeadStatus.NEW))
        .thenReturn(List.of(new Lead()));
    
    List<Lead> leads = service.getActiveLeads();
    
    assertEquals(1, leads.size());
}
```

### 2.3 Flexibilite

Changer de base de donnees sans modifier la logique metier.

```java
// Interface commune
public interface LeadRepository {
    Lead save(Lead lead);
    Optional<Lead> findById(Long id);
}

// Implementation PostgreSQL
public class PostgresLeadRepository implements LeadRepository { }

// Implementation MongoDB
public class MongoLeadRepository implements LeadRepository { }
```

---

## 3. Implementation manuelle

### Interface

```java
public interface LeadRepository {
    Lead save(Lead lead);
    Optional<Lead> findById(Long id);
    List<Lead> findAll();
    void delete(Lead lead);
    List<Lead> findByStatus(LeadStatus status);
}
```

### Implementation avec JDBC

```java
@Repository
public class JdbcLeadRepository implements LeadRepository {
    
    private final JdbcTemplate jdbc;
    
    @Override
    public Lead save(Lead lead) {
        if (lead.getId() == null) {
            String sql = "INSERT INTO leads (full_name, email, status) VALUES (?, ?, ?)";
            jdbc.update(sql, lead.getFullName(), lead.getEmail(), lead.getStatus().name());
        } else {
            String sql = "UPDATE leads SET full_name = ?, email = ?, status = ? WHERE id = ?";
            jdbc.update(sql, lead.getFullName(), lead.getEmail(), lead.getStatus().name(), lead.getId());
        }
        return lead;
    }
    
    @Override
    public Optional<Lead> findById(Long id) {
        String sql = "SELECT * FROM leads WHERE id = ?";
        try {
            Lead lead = jdbc.queryForObject(sql, this::mapRow, id);
            return Optional.ofNullable(lead);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    private Lead mapRow(ResultSet rs, int rowNum) throws SQLException {
        Lead lead = new Lead();
        lead.setId(rs.getLong("id"));
        lead.setFullName(rs.getString("full_name"));
        lead.setEmail(rs.getString("email"));
        lead.setStatus(LeadStatus.valueOf(rs.getString("status")));
        return lead;
    }
}
```

---

## 4. Implementation avec Spring Data JPA

### Interface (beaucoup plus simple!)

```java
@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    
    // Methodes generees automatiquement:
    // save(Lead)
    // findById(Long)
    // findAll()
    // delete(Lead)
    // count()
    // existsById(Long)
    
    // Methodes personnalisees (generees a partir du nom)
    List<Lead> findByStatus(LeadStatus status);
    
    Optional<Lead> findByEmail(String email);
    
    long countByStatus(LeadStatus status);
}
```

### Utilisation

```java
@Service
@RequiredArgsConstructor
public class LeadService {
    
    private final LeadRepository repository;
    
    public LeadDto createLead(ContactFormRequest request) {
        Lead lead = new Lead();
        lead.setFullName(request.getFullName());
        lead.setEmail(request.getEmail());
        
        Lead saved = repository.save(lead);  // JPA genere le SQL
        
        return mapToDto(saved);
    }
    
    public LeadDto findById(Long id) {
        return repository.findById(id)
            .map(this::mapToDto)
            .orElseThrow(() -> new ResourceNotFoundException("Lead not found"));
    }
}
```

---

## 5. Query Methods

### Principe

Spring Data genere les requetes SQL a partir du nom de la methode.

### Exemples

| Methode | SQL genere |
|---------|------------|
| findByEmail(String) | WHERE email = ? |
| findByStatusAndRequestType(Status, Type) | WHERE status = ? AND request_type = ? |
| findByFullNameContaining(String) | WHERE full_name LIKE %?% |
| findByCreatedAtAfter(LocalDateTime) | WHERE created_at > ? |
| countByStatus(Status) | SELECT COUNT(*) WHERE status = ? |
| existsByEmail(String) | SELECT EXISTS(... WHERE email = ?) |

---

## 6. Requetes personnalisees

### @Query (JPQL)

```java
@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    
    @Query("SELECT l FROM Lead l WHERE l.status = :status ORDER BY l.createdAt DESC")
    List<Lead> findRecentByStatus(@Param("status") LeadStatus status);
    
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.createdAt > :date")
    long countRecentLeads(@Param("date") LocalDateTime date);
}
```

### @Query (SQL natif)

```java
@Query(value = "SELECT * FROM leads WHERE created_at > NOW() - INTERVAL '7 days'", 
       nativeQuery = true)
List<Lead> findLeadsFromLastWeek();
```

---

## 7. Specification (requetes dynamiques)

### Pour des filtres complexes

```java
public class LeadSpecifications {
    
    public static Specification<Lead> hasStatus(LeadStatus status) {
        return (root, query, cb) -> 
            status == null ? null : cb.equal(root.get("status"), status);
    }
    
    public static Specification<Lead> nameContains(String name) {
        return (root, query, cb) -> 
            name == null ? null : cb.like(root.get("fullName"), "%" + name + "%");
    }
}

// Utilisation
@Service
public class LeadService {
    
    public List<Lead> search(LeadStatus status, String name) {
        Specification<Lead> spec = Specification
            .where(LeadSpecifications.hasStatus(status))
            .and(LeadSpecifications.nameContains(name));
        
        return repository.findAll(spec);
    }
}
```

---

## 8. Bonnes pratiques

### 8.1 Interface specifique au domaine

```java
// BON: Interface metier
public interface LeadRepository extends JpaRepository<Lead, Long> {
    List<Lead> findActiveLeads();  // Nom metier
}

// MOINS BON: Exposition des details techniques
public interface LeadRepository extends JpaRepository<Lead, Long> {
    List<Lead> findByStatusNotAndDeletedFalseOrderByCreatedAtDesc();
}
```

### 8.2 Pas de logique metier dans le repository

```java
// MAUVAIS
@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    default void processAndSave(Lead lead) {
        lead.setStatus(calculateStatus(lead));  // Logique metier!
        save(lead);
    }
}

// BON: La logique est dans le service
@Service
public class LeadService {
    public void processAndSave(Lead lead) {
        lead.setStatus(calculateStatus(lead));
        repository.save(lead);
    }
}
```

### 8.3 Utiliser Optional

```java
// BON
Optional<Lead> findByEmail(String email);

// MOINS BON (peut retourner null)
Lead findByEmail(String email);
```

---

## 9. Repositories du projet

### LeadRepository

```java
@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    
    long countByStatus(LeadStatus status);
    
    List<Lead> findByStatusOrderByCreatedAtDesc(LeadStatus status);
    
    Optional<Lead> findByEmail(String email);
    
    boolean existsByEmail(String email);
}
```

### UserRepository

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
}
```

---

## 10. Points cles a retenir

1. **Repository** abstrait l'acces aux donnees
2. **Spring Data JPA** genere l'implementation
3. **Query Methods** creent les requetes a partir du nom
4. **@Query** pour les requetes personnalisees
5. **Pas de logique metier** dans le repository

---

## QUIZ 13.1 - Pattern Repository

**1. Quel est le role du pattern Repository?**
   - a) Gerer les transactions
   - b) Abstraire l'acces aux donnees
   - c) Valider les donnees
   - d) Gerer la securite

**2. Quelle interface etendre pour Spring Data JPA?**
   - a) Repository
   - b) CrudRepository
   - c) JpaRepository
   - d) DataRepository

**3. Comment Spring Data genere-t-il les requetes?**
   - a) A partir des annotations
   - b) A partir du nom de la methode
   - c) A partir du fichier XML
   - d) Manuellement

**4. Que retourne findById()?**
   - a) L'entite ou null
   - b) Optional<Entity>
   - c) L'entite ou exception
   - d) Liste d'entites

**5. VRAI ou FAUX: Le repository peut contenir de la logique metier.**

**6. Quelle annotation pour une requete JPQL personnalisee?**
   - a) @Sql
   - b) @Query
   - c) @Jpql
   - d) @CustomQuery

**7. Que genere findByStatusAndRequestType()?**
   - a) WHERE status = ? OR request_type = ?
   - b) WHERE status = ? AND request_type = ?
   - c) WHERE status LIKE ? AND request_type LIKE ?
   - d) Erreur

**8. Completez: Spring Data JPA genere l'_______ du repository.**

**9. Quel avantage pour les tests unitaires?**
   - a) Plus rapide
   - b) Facile a mocker
   - c) Pas besoin de tests
   - d) Tests automatiques

**10. Quelle methode verifie l'existence sans charger l'entite?**
   - a) exists()
   - b) existsById()
   - c) hasId()
   - d) contains()

---

### REPONSES QUIZ 13.1

1. b) Abstraire l'acces aux donnees
2. c) JpaRepository
3. b) A partir du nom de la methode
4. b) Optional<Entity>
5. FAUX (logique metier = service)
6. b) @Query
7. b) WHERE status = ? AND request_type = ?
8. implementation
9. b) Facile a mocker
10. b) existsById()

