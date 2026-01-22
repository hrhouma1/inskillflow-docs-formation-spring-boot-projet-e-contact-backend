# Chapitre 2.3 - Couche Repository (Spring Data JPA)

## Objectifs du chapitre

- Creer des repositories avec Spring Data JPA
- Utiliser les methodes CRUD automatiques
- Ecrire des requetes personnalisees

---

## 1. Qu'est-ce qu'un Repository?

### Definition

Un **Repository** est une interface qui abstrait l'acces aux donnees. Avec Spring Data JPA, il suffit de definir l'interface; Spring genere l'implementation automatiquement.

### Avantages

1. **Pas de code SQL** pour les operations basiques
2. **Methodes generees** a partir du nom
3. **Typage fort** avec les generics

---

## 2. JpaRepository

### Heritage

```
Repository (marqueur)
    |
    v
CrudRepository (CRUD basique)
    |
    v
PagingAndSortingRepository (pagination, tri)
    |
    v
JpaRepository (flush, batch, etc.)
```

### Declaration

```java
@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    // Lead = type de l'entite
    // Long = type de la cle primaire
}
```

---

## 3. Methodes heritees

### JpaRepository fournit automatiquement:

| Methode | Description |
|---------|-------------|
| save(entity) | Cree ou met a jour |
| saveAll(entities) | Sauvegarde multiple |
| findById(id) | Trouve par ID (Optional) |
| existsById(id) | Verifie l'existence |
| findAll() | Liste tout |
| findAll(Pageable) | Liste avec pagination |
| findAll(Sort) | Liste avec tri |
| count() | Compte les entites |
| deleteById(id) | Supprime par ID |
| delete(entity) | Supprime une entite |
| deleteAll() | Supprime tout |

### Exemples d'utilisation

```java
@Service
public class LeadService {
    
    private final LeadRepository repository;
    
    // Sauvegarder un lead
    public Lead create(Lead lead) {
        return repository.save(lead);
    }
    
    // Trouver par ID
    public Lead findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Lead not found"));
    }
    
    // Lister tous les leads
    public List<Lead> findAll() {
        return repository.findAll();
    }
    
    // Compter les leads
    public long count() {
        return repository.count();
    }
    
    // Supprimer un lead
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
```

---

## 4. Query Methods (methodes de requete)

### Principe

Spring Data genere les requetes SQL a partir du nom de la methode.

### Syntaxe

```
findBy + NomDuChamp + Condition
```

### Exemples

```java
public interface LeadRepository extends JpaRepository<Lead, Long> {
    
    // SELECT * FROM leads WHERE status = ?
    List<Lead> findByStatus(LeadStatus status);
    
    // SELECT * FROM leads WHERE email = ?
    Optional<Lead> findByEmail(String email);
    
    // SELECT * FROM leads WHERE full_name LIKE ?
    List<Lead> findByFullNameContaining(String name);
    
    // SELECT * FROM leads WHERE status = ? AND request_type = ?
    List<Lead> findByStatusAndRequestType(LeadStatus status, RequestType type);
    
    // SELECT * FROM leads WHERE status = ? OR status = ?
    List<Lead> findByStatusOrStatus(LeadStatus status1, LeadStatus status2);
    
    // SELECT * FROM leads WHERE created_at > ?
    List<Lead> findByCreatedAtAfter(LocalDateTime date);
    
    // SELECT * FROM leads ORDER BY created_at DESC
    List<Lead> findAllByOrderByCreatedAtDesc();
    
    // SELECT COUNT(*) FROM leads WHERE status = ?
    long countByStatus(LeadStatus status);
    
    // SELECT EXISTS(SELECT 1 FROM leads WHERE email = ?)
    boolean existsByEmail(String email);
}
```

### Mots-cles disponibles

| Mot-cle | Exemple | SQL |
|---------|---------|-----|
| And | findByNameAndEmail | WHERE name = ? AND email = ? |
| Or | findByNameOrEmail | WHERE name = ? OR email = ? |
| Between | findByAgeBetween | WHERE age BETWEEN ? AND ? |
| LessThan | findByAgeLessThan | WHERE age < ? |
| GreaterThan | findByAgeGreaterThan | WHERE age > ? |
| Like | findByNameLike | WHERE name LIKE ? |
| Containing | findByNameContaining | WHERE name LIKE %?% |
| StartingWith | findByNameStartingWith | WHERE name LIKE ?% |
| EndingWith | findByNameEndingWith | WHERE name LIKE %? |
| IsNull | findByEmailIsNull | WHERE email IS NULL |
| IsNotNull | findByEmailIsNotNull | WHERE email IS NOT NULL |
| OrderBy | findByStatusOrderByCreatedAtDesc | ORDER BY created_at DESC |
| Not | findByStatusNot | WHERE status <> ? |
| In | findByStatusIn | WHERE status IN (?, ?, ?) |

---

## 5. Pagination et tri

### Pagination

```java
// Dans le Repository (deja disponible via JpaRepository)
Page<Lead> findAll(Pageable pageable);

// Dans le Service
public Page<Lead> getLeads(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return repository.findAll(pageable);
}

// Dans le Controller
@GetMapping
public Page<LeadDto> getLeads(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
) {
    return service.getLeads(page, size);
}
```

### Tri

```java
// Tri simple
List<Lead> findAllByOrderByCreatedAtDesc();

// Tri avec Sort
public List<Lead> getAllSorted() {
    Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
    return repository.findAll(sort);
}

// Pagination + Tri
public Page<Lead> getLeadsPaged(int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    return repository.findAll(pageable);
}
```

### Objet Page

```java
Page<Lead> page = repository.findAll(pageable);

page.getContent();        // Liste des elements
page.getTotalElements();  // Nombre total d'elements
page.getTotalPages();     // Nombre total de pages
page.getNumber();         // Numero de la page actuelle
page.getSize();           // Taille de la page
page.hasNext();           // Y a-t-il une page suivante?
page.hasPrevious();       // Y a-t-il une page precedente?
```

---

## 6. Requetes personnalisees (@Query)

### JPQL (Java Persistence Query Language)

```java
public interface LeadRepository extends JpaRepository<Lead, Long> {
    
    @Query("SELECT l FROM Lead l WHERE l.status = :status")
    List<Lead> findLeadsByStatus(@Param("status") LeadStatus status);
    
    @Query("SELECT l FROM Lead l WHERE l.email LIKE %:domain")
    List<Lead> findByEmailDomain(@Param("domain") String domain);
    
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.status = :status")
    long countLeadsByStatus(@Param("status") LeadStatus status);
}
```

### SQL natif

```java
@Query(value = "SELECT * FROM leads WHERE status = ?1", nativeQuery = true)
List<Lead> findByStatusNative(String status);

@Query(value = "SELECT COUNT(*) FROM leads WHERE created_at > NOW() - INTERVAL '7 days'", 
       nativeQuery = true)
long countRecentLeads();
```

---

## 7. Repositories du projet

### LeadRepository.java

```java
@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    
    long countByStatus(LeadStatus status);
    
    // Optionnel: methodes supplementaires
    List<Lead> findByStatusOrderByCreatedAtDesc(LeadStatus status);
    
    Optional<Lead> findByEmail(String email);
}
```

### UserRepository.java

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
}
```

---

## 8. Bonnes pratiques

### 8.1 Utiliser Optional pour les recherches uniques

```java
// BON
Optional<Lead> findByEmail(String email);

// MAUVAIS (peut lever une exception si null)
Lead findByEmail(String email);
```

### 8.2 Preferer les Query Methods aux @Query simples

```java
// BON: lisible et simple
List<Lead> findByStatus(LeadStatus status);

// MOINS BON: verbose pour une requete simple
@Query("SELECT l FROM Lead l WHERE l.status = :status")
List<Lead> findLeadsByStatus(@Param("status") LeadStatus status);
```

### 8.3 Utiliser @Query pour les requetes complexes

```java
// Requete complexe avec jointures
@Query("SELECT l FROM Lead l JOIN l.assignedTo u WHERE u.department = :dept")
List<Lead> findByAssignedDepartment(@Param("dept") String department);
```

### 8.4 Eviter les requetes N+1

```java
// Avec fetch join pour eviter les requetes multiples
@Query("SELECT l FROM Lead l LEFT JOIN FETCH l.comments")
List<Lead> findAllWithComments();
```

---

## 9. Points cles a retenir

1. **JpaRepository** fournit les methodes CRUD automatiquement
2. **Query Methods** generent les requetes a partir du nom
3. **Pageable** pour la pagination
4. **Sort** pour le tri
5. **@Query** pour les requetes personnalisees

---

## QUIZ 2.3 - Couche Repository

**1. Quelle interface etendre pour un repository JPA?**
   - a) CrudRepository
   - b) JpaRepository
   - c) Repository
   - d) DataRepository

**2. Quelle methode trouve une entite par ID?**
   - a) getById()
   - b) find()
   - c) findById()
   - d) get()

**3. Que retourne findById()?**
   - a) L'entite ou null
   - b) L'entite ou exception
   - c) Optional
   - d) Liste

**4. Quelle methode genere "WHERE status = ?"?**
   - a) getByStatus()
   - b) findByStatus()
   - c) selectByStatus()
   - d) whereStatus()

**5. VRAI ou FAUX: Spring genere automatiquement l'implementation du repository.**

**6. Quel mot-cle genere "LIKE %?%"?**
   - a) Like
   - b) Containing
   - c) Matching
   - d) Has

**7. Quelle classe utiliser pour la pagination?**
   - a) Page
   - b) Pagination
   - c) Pageable
   - d) PageRequest

**8. Completez: @Query avec nativeQuery = true execute du _______ brut.**

**9. Comment trier par createdAt descendant?**
   - a) findAllSortByCreatedAt()
   - b) findAllOrderByCreatedAtDesc()
   - c) findAllByOrderByCreatedAtDesc()
   - d) findAllSorted()

**10. Quelle annotation marque un repository?**
   - a) @Repository
   - b) @Repo
   - c) @DataAccess
   - d) @Dao

---

### REPONSES QUIZ 2.3

1. b) JpaRepository
2. c) findById()
3. c) Optional
4. b) findByStatus()
5. VRAI
6. b) Containing
7. c) Pageable (PageRequest pour creer une instance)
8. SQL
9. c) findAllByOrderByCreatedAtDesc()
10. a) @Repository

