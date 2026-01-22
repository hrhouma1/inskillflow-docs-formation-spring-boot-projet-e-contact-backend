# Chapitre 1.4 - Structure d'un projet Spring Boot

## Objectifs du chapitre

- Connaitre l'organisation standard d'un projet Spring Boot
- Comprendre le role de chaque dossier et fichier
- Naviguer efficacement dans le code source

---

## 1. Vue d'ensemble

```
projet-e-contact-backend/
|
|-- src/
|   |-- main/
|   |   |-- java/                    # Code source Java
|   |   |-- resources/               # Configuration et ressources
|   |-- test/
|       |-- java/                    # Tests
|
|-- target/                          # Build (genere, ignore par Git)
|
|-- pom.xml                          # Dependances Maven
|-- Dockerfile                       # Image Docker
|-- docker-compose.yml               # Orchestration Docker
|-- README.md                        # Documentation
|-- .gitignore                       # Fichiers ignores par Git
```

---

## 2. Le code source (src/main/java)

### Structure par packages

```
com.example.contact/
|
|-- ContactApplication.java          # Point d'entree
|
|-- config/                          # Configuration
|   |-- DataInitializer.java
|   |-- OpenApiConfig.java
|   |-- SecurityConfig.java
|   |-- UserDetailsConfig.java
|
|-- controller/                      # Endpoints REST
|   |-- AuthController.java
|   |-- ContactController.java
|   |-- LeadController.java
|
|-- dto/                             # Data Transfer Objects
|   |-- request/
|   |   |-- ContactFormRequest.java
|   |   |-- LoginRequest.java
|   |   |-- UpdateStatusRequest.java
|   |-- response/
|       |-- AuthResponse.java
|       |-- LeadDto.java
|       |-- LeadStatsDto.java
|       |-- MessageResponse.java
|
|-- exception/                       # Gestion des erreurs
|   |-- GlobalExceptionHandler.java
|   |-- ResourceNotFoundException.java
|
|-- model/                           # Entites JPA
|   |-- Lead.java
|   |-- User.java
|
|-- repository/                      # Acces donnees
|   |-- LeadRepository.java
|   |-- UserRepository.java
|
|-- security/                        # Securite JWT
|   |-- JwtAuthFilter.java
|   |-- JwtService.java
|
|-- service/                         # Logique metier
    |-- EmailService.java
    |-- LeadService.java
```

### Role de chaque package

| Package | Responsabilite | Annotations typiques |
|---------|---------------|---------------------|
| config | Configuration Spring | @Configuration |
| controller | Endpoints HTTP | @RestController |
| dto | Objets de transfert | @Data (Lombok) |
| exception | Gestion des erreurs | @ControllerAdvice |
| model | Entites base de donnees | @Entity |
| repository | Acces aux donnees | @Repository |
| security | Authentification JWT | @Component |
| service | Logique metier | @Service |

---

## 3. Les ressources (src/main/resources)

```
resources/
|
|-- application.yml                  # Configuration principale
|-- application-dev.yml              # Config profil dev (optionnel)
|-- application-prod.yml             # Config profil prod (optionnel)
|
|-- static/                          # Fichiers statiques (CSS, JS, images)
|-- templates/                       # Templates (Thymeleaf, si utilise)
```

### application.yml

Ce fichier contient toute la configuration de l'application:

```yaml
spring:
  application:
    name: contact-api
  
  datasource:
    url: jdbc:h2:mem:testdb
    
  jpa:
    hibernate:
      ddl-auto: create-drop
      
  mail:
    host: localhost
    port: 1025

server:
  port: 8080

app:
  jwt:
    secret: ma-cle-secrete
    expiration: 86400000
```

---

## 4. Classe principale

### ContactApplication.java

```java
package com.example.contact;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync           // Active les methodes asynchrones
@EnableScheduling      // Active les taches planifiees
public class ContactApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContactApplication.class, args);
    }
}
```

### Points importants

1. **@SpringBootApplication**: Combine @Configuration, @EnableAutoConfiguration, @ComponentScan
2. **Package racine**: Tous les composants doivent etre dans ce package ou un sous-package
3. **@EnableAsync**: Permet l'envoi d'emails en arriere-plan

---

## 5. Configuration (package config)

### SecurityConfig.java

Configure Spring Security:
- Endpoints publics vs proteges
- CORS
- Authentification JWT

### OpenApiConfig.java

Configure Swagger/OpenAPI:
- Titre et description de l'API
- Informations de contact
- Configuration de l'authentification

### DataInitializer.java

Initialise les donnees au demarrage:
- Cree un utilisateur admin par defaut
- S'execute une seule fois

### UserDetailsConfig.java

Configure le service de chargement des utilisateurs pour Spring Security.

---

## 6. Controllers (package controller)

### ContactController.java

```java
@RestController
@RequestMapping("/api/contact")
public class ContactController {
    
    @PostMapping
    public ResponseEntity<?> submitContact(@RequestBody @Valid ContactFormRequest request) {
        // ...
    }
}
```

Endpoint public pour soumettre le formulaire.

### AuthController.java

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
        // ...
    }
}
```

Endpoint pour l'authentification.

### LeadController.java

```java
@RestController
@RequestMapping("/api/admin/leads")
@PreAuthorize("hasRole('ADMIN')")
public class LeadController {
    
    @GetMapping
    public Page<LeadDto> getAllLeads(Pageable pageable) {
        // ...
    }
}
```

Endpoints proteges pour la gestion des leads.

---

## 7. DTOs (package dto)

### Request DTOs

Objets recus du client:

```java
// ContactFormRequest.java
@Data
public class ContactFormRequest {
    @NotBlank
    private String fullName;
    
    @Email
    private String email;
    
    private String company;
    private String phone;
    
    @NotNull
    private RequestType requestType;
    
    @NotBlank
    private String message;
}
```

### Response DTOs

Objets envoyes au client:

```java
// LeadDto.java
@Data
public class LeadDto {
    private Long id;
    private String fullName;
    private String email;
    private String company;
    private String phone;
    private RequestType requestType;
    private String message;
    private LeadStatus status;
    private LocalDateTime createdAt;
}
```

---

## 8. Model (package model)

### Lead.java

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
    
    // ...
}
```

### User.java

```java
@Entity
@Table(name = "users")
@Data
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    // ...
}
```

---

## 9. Repository (package repository)

```java
// LeadRepository.java
@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    long countByStatus(LeadStatus status);
}

// UserRepository.java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

---

## 10. Service (package service)

```java
// LeadService.java
@Service
@RequiredArgsConstructor
public class LeadService {
    private final LeadRepository leadRepository;
    private final EmailService emailService;
    
    public LeadDto createLead(ContactFormRequest request) {
        // Logique metier
    }
}
```

---

## 11. Points cles a retenir

1. **Un package = une responsabilite**
2. **La classe principale** doit etre a la racine des packages
3. **application.yml** centralise la configuration
4. **Les DTOs** separent l'API des entites internes
5. **Les conventions** permettent l'auto-configuration

---

## QUIZ 1.4 - Structure du projet

**1. Ou se trouve le code source Java?**
   - a) src/java
   - b) src/main/java
   - c) java/src
   - d) main/java

**2. Quel package contient les entites JPA?**
   - a) entity
   - b) domain
   - c) model
   - d) persistence

**3. Ou est le fichier application.yml?**
   - a) src/main/java
   - b) src/main/resources
   - c) config/
   - d) A la racine du projet

**4. Quel package contient la logique metier?**
   - a) controller
   - b) repository
   - c) service
   - d) business

**5. VRAI ou FAUX: Les DTOs doivent avoir les memes champs que les entites.**

**6. Quelle annotation marque une classe de configuration?**
   - a) @Config
   - b) @Settings
   - c) @Configuration
   - d) @Setup

**7. Quel dossier est genere par Maven et ignore par Git?**
   - a) src/
   - b) build/
   - c) target/
   - d) out/

**8. Completez: Un _______ definit l'interface pour acceder aux donnees.**

**9. Pourquoi separer request et response dans les DTOs?**
   - a) Pour la securite
   - b) Pour eviter d'exposer des champs sensibles
   - c) Pour permettre des validations differentes
   - d) Toutes les reponses ci-dessus

**10. Quelle classe doit etre a la racine du package principal?**
   - a) MainController
   - b) Application (classe avec @SpringBootApplication)
   - c) Config
   - d) Bootstrap

---

### REPONSES QUIZ 1.4

1. b) src/main/java
2. c) model
3. b) src/main/resources
4. c) service
5. FAUX (les DTOs peuvent avoir des champs differents)
6. c) @Configuration
7. c) target/
8. Repository
9. d) Toutes les reponses ci-dessus
10. b) Application (classe avec @SpringBootApplication)

