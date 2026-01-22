# Chapitre 4.1 - JPA et Hibernate

## Objectifs du chapitre

- Comprendre JPA et Hibernate
- Connaitre le role de l'ORM
- Configurer JPA dans Spring Boot

---

## 1. Qu'est-ce que JPA?

### Definition

**JPA (Java Persistence API)** est une specification Java pour le mapping objet-relationnel (ORM). Elle definit comment les objets Java sont persistes dans une base de donnees relationnelle.

### JPA n'est qu'une specification

JPA definit les interfaces et annotations, mais pas l'implementation. Il faut un provider.

---

## 2. Qu'est-ce qu'Hibernate?

### Definition

**Hibernate** est l'implementation JPA la plus populaire. C'est le provider par defaut de Spring Boot.

### JPA vs Hibernate

```
JPA (Specification)     Hibernate (Implementation)
-------------------     -------------------------
@Entity                 HibernateEntityManager
EntityManager           SessionFactory
JPQL                    HQL
```

---

## 3. ORM (Object-Relational Mapping)

### Concept

L'ORM fait le pont entre le monde objet (Java) et le monde relationnel (SQL).

```
Classe Java            Table SQL
-----------            ---------
Lead                   leads
  - id                   - id
  - fullName             - full_name
  - email                - email
  - status               - status
```

### Avantages

1. **Productivite**: Pas de SQL manuel pour les operations basiques
2. **Portabilite**: Changer de base facilement
3. **Typage**: Erreurs detectees a la compilation
4. **Cache**: Optimisations automatiques

---

## 4. Configuration dans Spring Boot

### Dependance

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

### application.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/contact_db
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
    
  jpa:
    hibernate:
      ddl-auto: update           # Strategie de creation de schema
    show-sql: true               # Affiche les requetes SQL
    properties:
      hibernate:
        format_sql: true         # Formate le SQL affiche
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

---

## 5. Strategies ddl-auto

| Valeur | Description | Usage |
|--------|-------------|-------|
| none | Aucune action | Production |
| validate | Verifie que le schema correspond | Production |
| update | Met a jour le schema sans supprimer | Developpement |
| create | Cree le schema (supprime les donnees) | Test |
| create-drop | Cree et supprime a l'arret | Test |

### Recommandations

```yaml
# Developpement
spring.jpa.hibernate.ddl-auto: update

# Production
spring.jpa.hibernate.ddl-auto: validate
# Utiliser Flyway ou Liquibase pour les migrations
```

---

## 6. EntityManager

### Role

L'EntityManager est l'interface principale pour interagir avec le contexte de persistence.

### Operations de base

```java
@Repository
public class LeadRepositoryCustom {
    
    @PersistenceContext
    private EntityManager em;
    
    // Create
    public void save(Lead lead) {
        em.persist(lead);
    }
    
    // Read
    public Lead find(Long id) {
        return em.find(Lead.class, id);
    }
    
    // Update
    public Lead update(Lead lead) {
        return em.merge(lead);
    }
    
    // Delete
    public void delete(Lead lead) {
        em.remove(em.contains(lead) ? lead : em.merge(lead));
    }
    
    // Query
    public List<Lead> findByStatus(LeadStatus status) {
        return em.createQuery(
            "SELECT l FROM Lead l WHERE l.status = :status", Lead.class)
            .setParameter("status", status)
            .getResultList();
    }
}
```

### Avec Spring Data JPA

Spring Data JPA genere automatiquement ces operations. L'EntityManager est rarement utilise directement.

---

## 7. Cycle de vie d'une entite

### Etats

```
        +----------+
        |   NEW    |  (pas encore persiste)
        +----+-----+
             | persist()
             v
        +----------+
        | MANAGED  |  (geree par EntityManager)
        +----+-----+
             | detach() / transaction terminee
             v
        +----------+
        | DETACHED |  (n'est plus geree)
        +----------+
             | merge()
             v
        +----------+
        | MANAGED  |
        +----+-----+
             | remove()
             v
        +----------+
        | REMOVED  |
        +----------+
```

### Exemple

```java
Lead lead = new Lead();          // NEW
lead.setFullName("Jean");

em.persist(lead);                // MANAGED (id genere)

Lead found = em.find(Lead.class, lead.getId());  // MANAGED

em.detach(found);                // DETACHED

found.setFullName("Pierre");
Lead merged = em.merge(found);   // MANAGED

em.remove(merged);               // REMOVED
```

---

## 8. JPQL (Java Persistence Query Language)

### Syntaxe

JPQL utilise les noms de classes et proprietes Java, pas les tables SQL.

```java
// JPQL
String jpql = "SELECT l FROM Lead l WHERE l.status = :status";

// SQL equivalent
String sql = "SELECT * FROM leads l WHERE l.status = ?";
```

### Exemples

```java
// Tous les leads
List<Lead> leads = em.createQuery("SELECT l FROM Lead l", Lead.class)
    .getResultList();

// Avec filtre
List<Lead> newLeads = em.createQuery(
    "SELECT l FROM Lead l WHERE l.status = :status", Lead.class)
    .setParameter("status", LeadStatus.NEW)
    .getResultList();

// Compter
Long count = em.createQuery(
    "SELECT COUNT(l) FROM Lead l WHERE l.status = :status", Long.class)
    .setParameter("status", LeadStatus.NEW)
    .getSingleResult();

// Tri
List<Lead> sorted = em.createQuery(
    "SELECT l FROM Lead l ORDER BY l.createdAt DESC", Lead.class)
    .getResultList();

// Pagination
List<Lead> page = em.createQuery("SELECT l FROM Lead l", Lead.class)
    .setFirstResult(0)   // offset
    .setMaxResults(10)   // limit
    .getResultList();
```

---

## 9. Transactions

### @Transactional

```java
@Service
@Transactional  // Toutes les methodes sont transactionnelles
public class LeadService {
    
    @Transactional  // Peut aussi etre sur une methode
    public void createLead(Lead lead) {
        // Si exception -> rollback automatique
    }
    
    @Transactional(readOnly = true)  // Optimisation pour les lectures
    public List<Lead> getAllLeads() {
        return repository.findAll();
    }
}
```

### Comportement

```java
@Transactional
public void transferLeads() {
    Lead lead1 = repository.findById(1L).get();
    Lead lead2 = repository.findById(2L).get();
    
    lead1.setStatus(LeadStatus.CONVERTED);
    lead2.setStatus(LeadStatus.NEW);
    
    repository.save(lead1);
    // Si exception ici, lead1 n'est pas sauvegarde non plus (rollback)
    repository.save(lead2);
}
```

---

## 10. Points cles a retenir

1. **JPA** = specification, **Hibernate** = implementation
2. **ORM** fait le pont entre Java et SQL
3. **ddl-auto** controle la creation du schema
4. **EntityManager** gere les entites
5. **@Transactional** garantit la coherence

---

## QUIZ 4.1 - JPA et Hibernate

**1. Qu'est-ce que JPA?**
   - a) Une base de donnees
   - b) Une specification ORM
   - c) Un framework web
   - d) Un langage de requete

**2. Qu'est-ce qu'Hibernate?**
   - a) La specification JPA
   - b) Une implementation de JPA
   - c) Un serveur d'applications
   - d) Un outil de migration

**3. Que fait ddl-auto: update?**
   - a) Supprime et recree le schema
   - b) Ne fait rien
   - c) Met a jour le schema sans supprimer les donnees
   - d) Valide le schema seulement

**4. Quelle valeur de ddl-auto pour la production?**
   - a) create
   - b) update
   - c) validate ou none
   - d) create-drop

**5. VRAI ou FAUX: JPQL utilise les noms de tables SQL.**

**6. Quel est le role de l'EntityManager?**
   - a) Gerer les connexions
   - b) Gerer le cycle de vie des entites
   - c) Gerer les transactions
   - d) Gerer le cache

**7. Quelle annotation rend une methode transactionnelle?**
   - a) @Transaction
   - b) @Transactional
   - c) @TX
   - d) @Commit

**8. Completez: ORM signifie Object-_______ Mapping.**

**9. Quel etat a une entite apres persist()?**
   - a) NEW
   - b) MANAGED
   - c) DETACHED
   - d) REMOVED

**10. Quelle est la difference entre JPA et Hibernate?**
   - a) Aucune
   - b) JPA = specification, Hibernate = implementation
   - c) Hibernate = specification, JPA = implementation
   - d) Deux frameworks differents

---

### REPONSES QUIZ 4.1

1. b) Une specification ORM
2. b) Une implementation de JPA
3. c) Met a jour le schema sans supprimer les donnees
4. c) validate ou none
5. FAUX (noms de classes et proprietes Java)
6. b) Gerer le cycle de vie des entites
7. b) @Transactional
8. Relational
9. b) MANAGED
10. b) JPA = specification, Hibernate = implementation

