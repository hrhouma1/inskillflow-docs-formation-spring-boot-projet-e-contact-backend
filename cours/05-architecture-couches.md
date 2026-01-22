# Chapitre 2.1 - Architecture n-tiers (Layered Architecture)

## Objectifs du chapitre

- Comprendre l'architecture en couches
- Connaitre les responsabilites de chaque couche
- Savoir pourquoi cette separation est importante

---

## 1. Qu'est-ce que l'architecture en couches?

### Definition

L'architecture en couches (ou n-tiers) organise le code en **niveaux distincts**, chacun ayant une responsabilite specifique.

### Principe fondamental

> Chaque couche ne communique qu'avec la couche adjacente.

```
     +-----------------+
     |   Presentation  |  (Controller)
     +-----------------+
             |
             v
     +-----------------+
     |     Metier      |  (Service)
     +-----------------+
             |
             v
     +-----------------+
     |     Donnees     |  (Repository)
     +-----------------+
             |
             v
     +-----------------+
     |  Base de donnees|
     +-----------------+
```

---

## 2. Les 4 couches principales

### 2.1 Couche Presentation (Controller)

**Role**: Recevoir les requetes HTTP et renvoyer les reponses.

```java
@RestController
@RequestMapping("/api/contact")
public class ContactController {
    
    private final LeadService leadService;
    
    @PostMapping
    public ResponseEntity<LeadDto> submit(@RequestBody ContactFormRequest request) {
        LeadDto lead = leadService.createLead(request);
        return ResponseEntity.ok(lead);
    }
}
```

**Responsabilites**:
- Valider les donnees d'entree
- Appeler le service approprie
- Formater la reponse HTTP
- Gerer les codes de statut

**Ne fait PAS**:
- Logique metier
- Acces direct a la base de donnees

---

### 2.2 Couche Metier (Service)

**Role**: Contenir la logique metier de l'application.

```java
@Service
@RequiredArgsConstructor
public class LeadService {
    
    private final LeadRepository leadRepository;
    private final EmailService emailService;
    
    public LeadDto createLead(ContactFormRequest request) {
        // 1. Creer l'entite
        Lead lead = new Lead();
        lead.setFullName(request.getFullName());
        lead.setEmail(request.getEmail());
        // ...
        
        // 2. Sauvegarder
        Lead saved = leadRepository.save(lead);
        
        // 3. Envoyer les notifications
        emailService.sendAdminNotification(saved);
        emailService.sendConfirmation(saved);
        
        // 4. Convertir en DTO
        return mapToDto(saved);
    }
}
```

**Responsabilites**:
- Implementer les regles metier
- Coordonner les operations
- Appeler les repositories
- Gerer les transactions

**Ne fait PAS**:
- Gerer les requetes HTTP
- Connaitre les details de la base de donnees

---

### 2.3 Couche Donnees (Repository)

**Role**: Abstraire l'acces a la base de donnees.

```java
@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    
    // Methodes heritees: save(), findById(), findAll(), delete()...
    
    // Methodes personnalisees
    List<Lead> findByStatus(LeadStatus status);
    long countByStatus(LeadStatus status);
}
```

**Responsabilites**:
- Operations CRUD
- Requetes personnalisees
- Abstraction du stockage

**Ne fait PAS**:
- Logique metier
- Gestion des requetes HTTP

---

### 2.4 Couche Model (Entites)

**Role**: Representer les donnees de l'application.

```java
@Entity
@Table(name = "leads")
@Data
public class Lead {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String fullName;
    
    @Column(nullable = false)
    private String email;
    
    @Enumerated(EnumType.STRING)
    private LeadStatus status = LeadStatus.NEW;
    
    private LocalDateTime createdAt = LocalDateTime.now();
}
```

**Responsabilites**:
- Mapper les tables de la base
- Definir les contraintes
- Representer le domaine

---

## 3. Flux d'une requete

### Exemple: Soumission d'un formulaire

```
Client                Controller           Service            Repository          DB
  |                       |                   |                   |                |
  |-- POST /api/contact ->|                   |                   |                |
  |                       |                   |                   |                |
  |                       |-- createLead() -->|                   |                |
  |                       |                   |                   |                |
  |                       |                   |-- save(lead) ---->|                |
  |                       |                   |                   |                |
  |                       |                   |                   |-- INSERT ----->|
  |                       |                   |                   |                |
  |                       |                   |                   |<-- OK ---------|
  |                       |                   |                   |                |
  |                       |                   |<-- Lead saved ----|                |
  |                       |                   |                   |                |
  |                       |<-- LeadDto -------|                   |                |
  |                       |                   |                   |                |
  |<-- 200 OK (JSON) -----|                   |                   |                |
```

---

## 4. Avantages de cette architecture

### 4.1 Separation des preoccupations

Chaque couche a une responsabilite unique et bien definie.

### 4.2 Testabilite

```java
// On peut tester le service en mockant le repository
@Test
void createLead_shouldSaveAndReturnDto() {
    // Arrange
    when(leadRepository.save(any())).thenReturn(mockLead);
    
    // Act
    LeadDto result = leadService.createLead(request);
    
    // Assert
    assertNotNull(result);
}
```

### 4.3 Maintenabilite

Modifier une couche n'impacte pas les autres (si l'interface est preservee).

### 4.4 Reutilisabilite

Un service peut etre utilise par plusieurs controllers.

### 4.5 Scalabilite

Les couches peuvent etre deployees separement si necessaire.

---

## 5. Regles a respecter

### Ce qui est permis

| Couche | Peut appeler |
|--------|--------------|
| Controller | Service |
| Service | Repository, autres Services |
| Repository | (Rien, utilise JPA) |

### Ce qui est interdit

| Couche | Ne doit PAS appeler |
|--------|---------------------|
| Controller | Repository (directement) |
| Service | Controller |
| Repository | Service |

### Mauvaise pratique

```java
// MAUVAIS: Controller appelle directement le Repository
@RestController
public class BadController {
    
    @Autowired
    private LeadRepository repository; // NON!
    
    @GetMapping("/leads")
    public List<Lead> getLeads() {
        return repository.findAll(); // Pas de logique metier!
    }
}
```

### Bonne pratique

```java
// BON: Controller passe par le Service
@RestController
public class GoodController {
    
    private final LeadService service;
    
    @GetMapping("/leads")
    public List<LeadDto> getLeads() {
        return service.getAllLeads(); // Le service gere la logique
    }
}
```

---

## 6. DTOs et separation des couches

### Pourquoi utiliser des DTOs?

```
Client <---> Controller <---> Service <---> Repository <---> DB
              (DTO)            (DTO/Entity)   (Entity)
```

1. **Securite**: Ne pas exposer les champs sensibles (password)
2. **Flexibilite**: L'API peut evoluer independamment de la base
3. **Performance**: Ne transferer que les champs necessaires

### Exemple

```java
// Entite (interne)
@Entity
public class User {
    private Long id;
    private String email;
    private String password;    // Sensible!
    private String role;
}

// DTO (externe)
public class UserDto {
    private Long id;
    private String email;
    private String role;
    // Pas de password!
}
```

---

## 7. Points cles a retenir

1. **4 couches**: Controller, Service, Repository, Model
2. **Communication verticale**: Chaque couche n'appelle que la suivante
3. **Separation des responsabilites**: Une couche = un role
4. **DTOs**: Separent l'API des entites internes
5. **Testabilite**: Les couches peuvent etre testees independamment

---

## QUIZ 2.1 - Architecture en couches

**1. Combien de couches principales dans une architecture Spring Boot typique?**
   - a) 2
   - b) 3
   - c) 4
   - d) 5

**2. Quelle couche contient la logique metier?**
   - a) Controller
   - b) Repository
   - c) Service
   - d) Model

**3. Quelle couche recoit les requetes HTTP?**
   - a) Service
   - b) Controller
   - c) Repository
   - d) Model

**4. VRAI ou FAUX: Un Controller peut appeler directement un Repository.**

**5. Quelle couche communique avec la base de donnees?**
   - a) Controller
   - b) Service
   - c) Repository
   - d) Model

**6. Pourquoi utiliser des DTOs?**
   - a) Pour cacher les champs sensibles
   - b) Pour permettre l'evolution independante de l'API
   - c) Pour ameliorer les performances
   - d) Toutes les reponses ci-dessus

**7. Dans quel ordre les couches sont-elles traversees lors d'une requete?**
   - a) Controller -> Service -> Repository
   - b) Repository -> Service -> Controller
   - c) Service -> Controller -> Repository
   - d) Model -> Repository -> Service

**8. Completez: Chaque couche n'appelle que la couche _______.**

**9. Quel est l'avantage principal de cette architecture pour les tests?**
   - a) Plus rapide
   - b) On peut mocker les dependances
   - c) Moins de code
   - d) Pas besoin de tests

**10. Quelle annotation marque un service?**
   - a) @Controller
   - b) @Repository
   - c) @Service
   - d) @Component

---

### REPONSES QUIZ 2.1

1. c) 4
2. c) Service
3. b) Controller
4. VRAI (mais c'est une mauvaise pratique)
5. c) Repository
6. d) Toutes les reponses ci-dessus
7. a) Controller -> Service -> Repository
8. adjacente (ou suivante)
9. b) On peut mocker les dependances
10. c) @Service

