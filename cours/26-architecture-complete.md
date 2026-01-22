# Chapitre 5.5 - Architecture complète : Quelle classe appelle quelle classe?

## Objectifs du chapitre

- Comprendre l'architecture globale du projet
- Savoir quelle classe appelle quelle classe
- Visualiser les flux de données
- Maîtriser les relations entre les couches

---

## Vue d'ensemble du projet

### Structure des dossiers

```
src/main/java/com/example/contact/
│
├── ContactApplication.java      ← Point d'entrée (main)
│
├── config/                      ← CONFIGURATION
│   ├── DataInitializer.java
│   ├── OpenApiConfig.java
│   ├── SecurityConfig.java
│   └── UserDetailsConfig.java
│
├── controller/                  ← ENDPOINTS REST
│   ├── AuthController.java
│   ├── ContactController.java
│   └── LeadController.java
│
├── dto/                         ← OBJETS DE TRANSFERT
│   ├── request/
│   │   ├── ContactFormRequest.java
│   │   ├── LoginRequest.java
│   │   └── UpdateStatusRequest.java
│   └── response/
│       ├── AuthResponse.java
│       ├── LeadDto.java
│       ├── LeadStatsDto.java
│       └── MessageResponse.java
│
├── exception/                   ← GESTION DES ERREURS
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
│
├── model/                       ← ENTITÉS JPA
│   ├── Lead.java
│   └── User.java
│
├── repository/                  ← ACCÈS BASE DE DONNÉES
│   ├── LeadRepository.java
│   └── UserRepository.java
│
├── security/                    ← SÉCURITÉ JWT
│   ├── JwtAuthFilter.java
│   └── JwtService.java
│
└── service/                     ← LOGIQUE MÉTIER
    ├── EmailService.java
    └── LeadService.java
```

---

## Diagramme de l'architecture en couches

```mermaid
graph TB
    subgraph "COUCHE CLIENT"
        CLIENT["Navigateur / Postman / Frontend"]
    end
    
    subgraph "COUCHE SÉCURITÉ"
        SC["SecurityConfig"]
        JAF["JwtAuthFilter"]
        JS["JwtService"]
    end
    
    subgraph "COUCHE CONTROLLER"
        AC["AuthController"]
        CC["ContactController"]
        LC["LeadController"]
    end
    
    subgraph "COUCHE SERVICE"
        LS["LeadService"]
        ES["EmailService"]
    end
    
    subgraph "COUCHE REPOSITORY"
        LR["LeadRepository"]
        UR["UserRepository"]
    end
    
    subgraph "COUCHE MODEL"
        LEAD["Lead"]
        USER["User"]
    end
    
    subgraph "BASE DE DONNÉES"
        DB[(PostgreSQL)]
    end
    
    CLIENT --> SC
    SC --> JAF
    JAF --> JS
    JAF --> AC
    JAF --> CC
    JAF --> LC
    
    AC --> UR
    CC --> LS
    LC --> LS
    
    LS --> LR
    LS --> ES
    
    LR --> DB
    UR --> DB
    
    style SC fill:#2196F3,color:#fff
    style JAF fill:#E91E63,color:#fff
    style JS fill:#E91E63,color:#fff
    style AC fill:#4CAF50,color:#fff
    style CC fill:#4CAF50,color:#fff
    style LC fill:#4CAF50,color:#fff
    style LS fill:#FF9800,color:#fff
    style ES fill:#FF9800,color:#fff
```

---

## Tableau complet : TOUTES les classes

### 1. CONFIGURATION (config/)

| Classe | Rôle | Appelle | Appelée par |
|--------|------|---------|-------------|
| `SecurityConfig` | Configure les règles de sécurité, CORS, CSRF | `JwtAuthFilter`, `PasswordEncoder` | Spring Boot (démarrage) |
| `UserDetailsConfig` | Fournit le bean UserDetailsService | `UserRepository` | Spring Boot (démarrage), `JwtAuthFilter` |
| `DataInitializer` | Crée l'admin au démarrage | `UserRepository`, `PasswordEncoder` | Spring Boot (démarrage) |
| `OpenApiConfig` | Configure Swagger/OpenAPI | Rien | Spring Boot (démarrage) |

```mermaid
graph TB
    subgraph "config/"
        SC["SecurityConfig<br/>@Configuration"]
        UDC["UserDetailsConfig<br/>@Configuration"]
        DI["DataInitializer<br/>@Component"]
        OAC["OpenApiConfig<br/>@Configuration"]
    end
    
    SC -->|"configure"| JAF["JwtAuthFilter"]
    SC -->|"crée"| PE["PasswordEncoder Bean"]
    SC -->|"crée"| AP["AuthenticationProvider Bean"]
    
    UDC -->|"utilise"| UR["UserRepository"]
    UDC -->|"crée"| UDS["UserDetailsService Bean"]
    
    DI -->|"utilise"| UR
    DI -->|"utilise"| PE
    
    style SC fill:#2196F3,color:#fff
    style UDC fill:#2196F3,color:#fff
    style DI fill:#2196F3,color:#fff
    style OAC fill:#2196F3,color:#fff
```

---

### 2. SÉCURITÉ (security/)

| Classe | Rôle | Appelle | Appelée par |
|--------|------|---------|-------------|
| `JwtAuthFilter` | Valide le JWT à chaque requête | `JwtService`, `UserDetailsService` | Spring Security (automatique) |
| `JwtService` | Génère et valide les tokens JWT | Rien (utilitaire) | `JwtAuthFilter`, `AuthController` |

```mermaid
graph TB
    subgraph "security/"
        JAF["JwtAuthFilter<br/>@Component"]
        JS["JwtService<br/>@Service"]
    end
    
    SS["Spring Security"] -->|"appelle automatiquement"| JAF
    JAF -->|"extractUsername()"| JS
    JAF -->|"isTokenValid()"| JS
    JAF -->|"loadUserByUsername()"| UDS["UserDetailsService"]
    
    AC["AuthController"] -->|"generateToken()"| JS
    
    style JAF fill:#E91E63,color:#fff
    style JS fill:#E91E63,color:#fff
```

---

### 3. CONTROLLERS (controller/)

| Classe | Endpoints | Appelle | Appelée par |
|--------|-----------|---------|-------------|
| `AuthController` | `POST /api/auth/login` | `AuthenticationManager`, `JwtService`, `UserRepository` | Client HTTP |
| `ContactController` | `POST /api/contact` | `LeadService` | Client HTTP |
| `LeadController` | `GET/PUT/DELETE /api/admin/leads/**` | `LeadService` | Client HTTP (après auth) |

```mermaid
graph TB
    subgraph "controller/"
        AC["AuthController<br/>@RestController<br/>/api/auth"]
        CC["ContactController<br/>@RestController<br/>/api/contact"]
        LC["LeadController<br/>@RestController<br/>/api/admin/leads"]
    end
    
    CLIENT["Client HTTP"] --> AC
    CLIENT --> CC
    CLIENT --> LC
    
    AC -->|"authenticate()"| AM["AuthenticationManager"]
    AC -->|"generateToken()"| JS["JwtService"]
    AC -->|"findByEmail()"| UR["UserRepository"]
    
    CC -->|"createLead()"| LS["LeadService"]
    
    LC -->|"getAllLeads()"| LS
    LC -->|"getLeadById()"| LS
    LC -->|"updateStatus()"| LS
    LC -->|"deleteLead()"| LS
    LC -->|"getStats()"| LS
    
    style AC fill:#4CAF50,color:#fff
    style CC fill:#4CAF50,color:#fff
    style LC fill:#4CAF50,color:#fff
```

---

### 4. SERVICES (service/)

| Classe | Rôle | Appelle | Appelée par |
|--------|------|---------|-------------|
| `LeadService` | Logique métier des leads | `LeadRepository`, `EmailService` | `ContactController`, `LeadController` |
| `EmailService` | Envoi d'emails | `JavaMailSender` (Spring) | `LeadService` |

```mermaid
graph TB
    subgraph "service/"
        LS["LeadService<br/>@Service"]
        ES["EmailService<br/>@Service"]
    end
    
    CC["ContactController"] -->|"createLead()"| LS
    LC["LeadController"] -->|"getAllLeads()<br/>updateStatus()<br/>deleteLead()"| LS
    
    LS -->|"save()<br/>findAll()<br/>findById()"| LR["LeadRepository"]
    LS -->|"sendNotification()<br/>sendConfirmation()"| ES
    
    ES -->|"send()"| JMS["JavaMailSender"]
    
    style LS fill:#FF9800,color:#fff
    style ES fill:#FF9800,color:#fff
```

---

### 5. REPOSITORIES (repository/)

| Classe | Rôle | Appelle | Appelée par |
|--------|------|---------|-------------|
| `LeadRepository` | CRUD pour les leads | PostgreSQL (via JPA) | `LeadService` |
| `UserRepository` | CRUD pour les utilisateurs | PostgreSQL (via JPA) | `UserDetailsConfig`, `DataInitializer`, `AuthController` |

```mermaid
graph TB
    subgraph "repository/"
        LR["LeadRepository<br/>extends JpaRepository"]
        UR["UserRepository<br/>extends JpaRepository"]
    end
    
    LS["LeadService"] -->|"save()<br/>findAll()<br/>findById()<br/>delete()"| LR
    
    UDC["UserDetailsConfig"] -->|"findByEmail()"| UR
    DI["DataInitializer"] -->|"existsByEmail()<br/>save()"| UR
    AC["AuthController"] -->|"findByEmail()"| UR
    
    LR -->|"SQL"| DB[(PostgreSQL)]
    UR -->|"SQL"| DB
    
    style LR fill:#9C27B0,color:#fff
    style UR fill:#9C27B0,color:#fff
```

---

### 6. MODELS (model/)

| Classe | Rôle | Relations | Utilisée par |
|--------|------|-----------|--------------|
| `Lead` | Entité représentant un prospect | Aucune | `LeadRepository`, `LeadService`, `LeadController` |
| `User` | Entité représentant un utilisateur | Implémente `UserDetails` | `UserRepository`, `JwtAuthFilter`, `AuthController` |

```mermaid
graph TB
    subgraph "model/"
        LEAD["Lead<br/>@Entity"]
        USER["User<br/>@Entity<br/>implements UserDetails"]
    end
    
    LEAD -->|"mapped to"| T1["Table: leads"]
    USER -->|"mapped to"| T2["Table: users"]
    
    LR["LeadRepository"] -->|"CRUD"| LEAD
    UR["UserRepository"] -->|"CRUD"| USER
    
    USER -->|"getAuthorities()"| SS["Spring Security"]
    
    style LEAD fill:#795548,color:#fff
    style USER fill:#795548,color:#fff
```

---

### 7. DTOs (dto/)

| Classe | Type | Utilisée pour | Utilisée par |
|--------|------|---------------|--------------|
| `ContactFormRequest` | Request | Données du formulaire de contact | `ContactController` |
| `LoginRequest` | Request | Email + mot de passe pour login | `AuthController` |
| `UpdateStatusRequest` | Request | Nouveau statut d'un lead | `LeadController` |
| `AuthResponse` | Response | Token JWT + infos utilisateur | `AuthController` |
| `LeadDto` | Response | Données d'un lead | `LeadController`, `LeadService` |
| `LeadStatsDto` | Response | Statistiques des leads | `LeadController`, `LeadService` |
| `MessageResponse` | Response | Message simple | `ContactController`, `LeadController` |

```mermaid
graph LR
    subgraph "dto/request/"
        CFR["ContactFormRequest"]
        LR["LoginRequest"]
        USR["UpdateStatusRequest"]
    end
    
    subgraph "dto/response/"
        AR["AuthResponse"]
        LD["LeadDto"]
        LSD["LeadStatsDto"]
        MR["MessageResponse"]
    end
    
    CLIENT["Client"] -->|"envoie"| CFR
    CLIENT -->|"envoie"| LR
    CLIENT -->|"envoie"| USR
    
    AR -->|"reçoit"| CLIENT
    LD -->|"reçoit"| CLIENT
    LSD -->|"reçoit"| CLIENT
    MR -->|"reçoit"| CLIENT
    
    style CFR fill:#00BCD4,color:#fff
    style LR fill:#00BCD4,color:#fff
    style USR fill:#00BCD4,color:#fff
    style AR fill:#8BC34A,color:#fff
    style LD fill:#8BC34A,color:#fff
    style LSD fill:#8BC34A,color:#fff
    style MR fill:#8BC34A,color:#fff
```

---

### 8. EXCEPTIONS (exception/)

| Classe | Rôle | Utilisée par |
|--------|------|--------------|
| `GlobalExceptionHandler` | Gère toutes les exceptions de l'application | Spring (automatique) |
| `ResourceNotFoundException` | Exception pour ressource non trouvée | `LeadService` |

---

## Tableau récapitulatif : QUI APPELLE QUI?

| # | Appelant | Méthode | Appelé | Quand? |
|---|----------|---------|--------|--------|
| 1 | **Spring Boot** | démarrage | `SecurityConfig` | Lancement de l'app |
| 2 | **Spring Boot** | démarrage | `UserDetailsConfig` | Lancement de l'app |
| 3 | **Spring Boot** | démarrage | `DataInitializer` | Lancement de l'app |
| 4 | **Spring Security** | filter chain | `JwtAuthFilter.doFilterInternal()` | Chaque requête HTTP |
| 5 | **JwtAuthFilter** | `extractUsername()` | `JwtService` | Validation JWT |
| 6 | **JwtAuthFilter** | `loadUserByUsername()` | `UserDetailsService` | Chargement user |
| 7 | **JwtAuthFilter** | `isTokenValid()` | `JwtService` | Validation JWT |
| 8 | **Client** | `POST /api/auth/login` | `AuthController.login()` | Login |
| 9 | **AuthController** | `authenticate()` | `AuthenticationManager` | Vérification credentials |
| 10 | **AuthController** | `generateToken()` | `JwtService` | Création JWT |
| 11 | **Client** | `POST /api/contact` | `ContactController.submitContactForm()` | Formulaire |
| 12 | **ContactController** | `createLead()` | `LeadService` | Création lead |
| 13 | **Client** | `GET /api/admin/leads` | `LeadController.getAllLeads()` | Liste leads |
| 14 | **LeadController** | `getAllLeads()` | `LeadService` | Récupération |
| 15 | **LeadService** | `save()` / `findAll()` | `LeadRepository` | Accès DB |
| 16 | **LeadService** | `sendNotification()` | `EmailService` | Envoi email |
| 17 | **LeadRepository** | JPA | `PostgreSQL` | Requête SQL |
| 18 | **UserRepository** | JPA | `PostgreSQL` | Requête SQL |

---

## Flux complet : Soumission d'un formulaire

```mermaid
sequenceDiagram
    participant C as Client
    participant SC as SecurityConfig
    participant JAF as JwtAuthFilter
    participant CC as ContactController
    participant LS as LeadService
    participant LR as LeadRepository
    participant ES as EmailService
    participant DB as PostgreSQL
    
    C->>SC: POST /api/contact
    SC->>SC: requestMatchers(POST, "/api/contact").permitAll()
    SC->>JAF: Passe au filtre
    JAF->>JAF: Pas de JWT requis (public)
    JAF->>CC: doFilter() → Controller
    
    CC->>CC: Validation @Valid
    CC->>LS: createLead(request)
    LS->>LS: Convertit DTO → Entity
    LS->>LR: save(lead)
    LR->>DB: INSERT INTO leads...
    DB-->>LR: Lead(id=15)
    LR-->>LS: Lead sauvegardé
    
    LS->>ES: sendNotificationToAdmin(lead)
    ES->>ES: Envoie email async
    
    LS-->>CC: Void (succès)
    CC-->>C: 200 OK { message: "Merci!" }
```

---

## Flux complet : Login administrateur

```mermaid
sequenceDiagram
    participant C as Client
    participant SC as SecurityConfig
    participant JAF as JwtAuthFilter
    participant AC as AuthController
    participant AM as AuthenticationManager
    participant UDS as UserDetailsService
    participant UR as UserRepository
    participant PE as PasswordEncoder
    participant JS as JwtService
    participant DB as PostgreSQL
    
    C->>SC: POST /api/auth/login
    SC->>SC: /api/auth/** → permitAll()
    SC->>JAF: Passe au filtre
    JAF->>JAF: Pas de JWT (login)
    JAF->>AC: doFilter() → Controller
    
    AC->>AM: authenticate(email, password)
    AM->>UDS: loadUserByUsername(email)
    UDS->>UR: findByEmail(email)
    UR->>DB: SELECT * FROM users WHERE email=?
    DB-->>UR: User
    UR-->>UDS: User
    UDS-->>AM: UserDetails
    
    AM->>PE: matches(password, hash)
    PE-->>AM: true
    AM-->>AC: Authentication(user)
    
    AC->>JS: generateToken(user)
    JS-->>AC: "eyJhbGciOi..."
    
    AC-->>C: 200 OK { token: "eyJ...", role: "ADMIN" }
```

---

## Flux complet : Accès endpoint protégé

```mermaid
sequenceDiagram
    participant C as Client
    participant SC as SecurityConfig
    participant JAF as JwtAuthFilter
    participant JS as JwtService
    participant UDS as UserDetailsService
    participant UR as UserRepository
    participant LC as LeadController
    participant LS as LeadService
    participant LR as LeadRepository
    participant DB as PostgreSQL
    
    C->>SC: GET /api/admin/leads<br/>Authorization: Bearer eyJ...
    SC->>JAF: Passe au filtre JWT
    
    JAF->>JAF: Extrait le JWT du header
    JAF->>JS: extractUsername(jwt)
    JS-->>JAF: "admin@test.com"
    
    JAF->>UDS: loadUserByUsername("admin@test.com")
    UDS->>UR: findByEmail("admin@test.com")
    UR->>DB: SELECT * FROM users...
    DB-->>UR: User(ADMIN)
    UR-->>UDS: User
    UDS-->>JAF: UserDetails
    
    JAF->>JS: isTokenValid(jwt, user)
    JS-->>JAF: true
    
    JAF->>JAF: SecurityContext.setAuthentication()
    JAF->>SC: filterChain.doFilter()
    
    SC->>SC: /api/admin/** → hasRole("ADMIN")?
    SC->>SC: User.getAuthorities() = [ROLE_ADMIN] ✓
    SC->>LC: Accès autorisé
    
    LC->>LS: getAllLeads(pageable)
    LS->>LR: findAll(pageable)
    LR->>DB: SELECT * FROM leads...
    DB-->>LR: [leads]
    LR-->>LS: Page<Lead>
    LS-->>LC: Page<LeadDto>
    LC-->>C: 200 OK { content: [...] }
```

---

## Schéma final : Toutes les relations

```mermaid
graph TB
    subgraph "CLIENT"
        CL["Navigateur/Postman"]
    end
    
    subgraph "SÉCURITÉ"
        SC["SecurityConfig"]
        JAF["JwtAuthFilter"]
        JS["JwtService"]
        UDC["UserDetailsConfig"]
    end
    
    subgraph "CONTROLLER"
        AC["AuthController"]
        CC["ContactController"]
        LC["LeadController"]
    end
    
    subgraph "SERVICE"
        LS["LeadService"]
        ES["EmailService"]
    end
    
    subgraph "REPOSITORY"
        LR["LeadRepository"]
        UR["UserRepository"]
    end
    
    subgraph "DATABASE"
        DB[(PostgreSQL)]
    end
    
    CL -->|"1"| SC
    SC -->|"2"| JAF
    JAF -->|"3"| JS
    JAF -->|"4"| UDC
    UDC -->|"5"| UR
    
    JAF -->|"6"| AC
    JAF -->|"7"| CC
    JAF -->|"8"| LC
    
    AC -->|"9"| JS
    AC -->|"10"| UR
    
    CC -->|"11"| LS
    LC -->|"12"| LS
    
    LS -->|"13"| LR
    LS -->|"14"| ES
    
    LR -->|"15"| DB
    UR -->|"16"| DB
    
    style SC fill:#2196F3,color:#fff
    style JAF fill:#E91E63,color:#fff
    style JS fill:#E91E63,color:#fff
    style UDC fill:#2196F3,color:#fff
    style AC fill:#4CAF50,color:#fff
    style CC fill:#4CAF50,color:#fff
    style LC fill:#4CAF50,color:#fff
    style LS fill:#FF9800,color:#fff
    style ES fill:#FF9800,color:#fff
    style LR fill:#9C27B0,color:#fff
    style UR fill:#9C27B0,color:#fff
```

---

## QUIZ - Architecture

**1. Quelle classe appelle JwtAuthFilter.doFilterInternal()?**
- a) SecurityConfig
- b) AuthController
- c) Spring Security (automatiquement)
- d) JwtService

<details>
<summary>Voir la réponse</summary>

**Réponse : c) Spring Security (automatiquement)**

Spring Security appelle automatiquement tous les filtres de la chaîne à chaque requête HTTP. `JwtAuthFilter` est ajouté à cette chaîne par `SecurityConfig.addFilterBefore()`.
</details>

---

**2. LeadController appelle directement quelle classe?**
- a) LeadRepository
- b) LeadService
- c) PostgreSQL
- d) EmailService

<details>
<summary>Voir la réponse</summary>

**Réponse : b) LeadService**

Les Controllers appellent les Services, jamais les Repositories directement. C'est le principe de l'architecture en couches.
</details>

---

**3. Qui appelle UserRepository.findByEmail()?**
- a) Seulement AuthController
- b) Seulement UserDetailsConfig
- c) AuthController, UserDetailsConfig, et DataInitializer
- d) LeadService

<details>
<summary>Voir la réponse</summary>

**Réponse : c) AuthController, UserDetailsConfig, et DataInitializer**

`UserRepository` est utilisé par plusieurs classes pour différentes raisons : login, validation JWT, et création de l'admin initial.
</details>

---

**4. Dans quel ordre sont appelées les classes lors d'un GET /api/admin/leads?**
- a) Controller → Service → Repository → DB
- b) SecurityConfig → JwtAuthFilter → Controller → Service → Repository → DB
- c) JwtService → Controller → Service
- d) Repository → Service → Controller

<details>
<summary>Voir la réponse</summary>

**Réponse : b) SecurityConfig → JwtAuthFilter → Controller → Service → Repository → DB**

La requête passe d'abord par la couche sécurité (validation JWT), puis Controller → Service → Repository → DB.
</details>

---

## Navigation

| Précédent | Suivant |
|-----------|---------|
| [25 - Exercice JwtAuthFilter](25-exercice-jwtauthfilter.md) | [27 - Tests et validation](27-tests-validation.md) |

