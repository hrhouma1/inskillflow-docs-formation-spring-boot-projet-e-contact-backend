# Annexe E - Guide complet des endpoints et scénarios

## Vue d'ensemble de l'API e-contact-backend

Cette annexe présente tous les endpoints de l'application, leur niveau de protection, et les scénarios d'utilisation concrets.

---

## 1. Carte des endpoints

```mermaid
graph TB
    subgraph "ENDPOINTS PUBLICS (permitAll)"
        P1["POST /api/contact<br/>Formulaire de contact"]
        P2["POST /api/auth/login<br/>Connexion admin"]
        P3["/swagger-ui/**<br/>Documentation API"]
        P4["/v3/api-docs/**<br/>OpenAPI JSON"]
        P5["/h2-console/**<br/>Console base de données"]
    end
    
    subgraph "ENDPOINTS PROTÉGÉS (ROLE_ADMIN requis)"
        A1["GET /api/admin/leads<br/>Liste des leads"]
        A2["GET /api/admin/leads/{id}<br/>Détail d'un lead"]
        A3["PUT /api/admin/leads/{id}/status<br/>Changer le statut"]
        A4["DELETE /api/admin/leads/{id}<br/>Supprimer un lead"]
        A5["GET /api/admin/leads/stats<br/>Statistiques"]
    end
    
    style P1 fill:#4CAF50,color:#fff
    style P2 fill:#4CAF50,color:#fff
    style P3 fill:#4CAF50,color:#fff
    style P4 fill:#4CAF50,color:#fff
    style P5 fill:#4CAF50,color:#fff
    
    style A1 fill:#f44336,color:#fff
    style A2 fill:#f44336,color:#fff
    style A3 fill:#f44336,color:#fff
    style A4 fill:#f44336,color:#fff
    style A5 fill:#f44336,color:#fff
```

---

## 2. Tableau récapitulatif des endpoints

| Endpoint | Méthode | Protection | Description | Controller |
|----------|---------|------------|-------------|------------|
| `/api/contact` | POST | **PUBLIC** | Soumettre formulaire de contact | ContactController |
| `/api/auth/login` | POST | **PUBLIC** | Connexion administrateur | AuthController |
| `/api/admin/leads` | GET | **ADMIN** | Liste paginée des leads | LeadController |
| `/api/admin/leads/{id}` | GET | **ADMIN** | Détails d'un lead | LeadController |
| `/api/admin/leads/{id}/status` | PUT | **ADMIN** | Modifier le statut | LeadController |
| `/api/admin/leads/{id}` | DELETE | **ADMIN** | Supprimer un lead | LeadController |
| `/api/admin/leads/stats` | GET | **ADMIN** | Statistiques | LeadController |
| `/swagger-ui/**` | GET | **PUBLIC** | Interface Swagger | Spring Doc |
| `/v3/api-docs/**` | GET | **PUBLIC** | Spec OpenAPI | Spring Doc |
| `/h2-console/**` | ANY | **PUBLIC** | Console H2 (dev) | H2 |

---

## 3. Configuration de sécurité correspondante

```java
// SecurityConfig.java - Les règles qui définissent la protection
.authorizeHttpRequests(auth -> auth
    // ✅ PUBLICS
    .requestMatchers(HttpMethod.POST, "/api/contact").permitAll()
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/h2-console/**").permitAll()
    .requestMatchers("/swagger-ui/**").permitAll()
    .requestMatchers("/swagger-ui.html").permitAll()
    .requestMatchers("/v3/api-docs/**").permitAll()
    
    // 🔒 PROTÉGÉS - ADMIN seulement
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    
    // 🔐 TOUT LE RESTE - Authentification requise
    .anyRequest().authenticated()
)
```

---

## 4. Scénarios détaillés

### SCÉNARIO A : Visiteur soumet le formulaire de contact

**Acteur** : Visiteur anonyme (pas de compte)
**Endpoint** : `POST /api/contact`
**Protection** : Aucune (public)

```mermaid
sequenceDiagram
    participant V as Visiteur
    participant F as Frontend
    participant API as API Spring Boot
    participant DB as PostgreSQL
    participant MAIL as Service Email
    
    V->>F: Remplit le formulaire<br/>(nom, email, message)
    F->>API: POST /api/contact<br/>Content-Type: application/json
    
    Note over API: JwtAuthFilter vérifie:<br/>Pas de header Authorization<br/>→ Continue sans authentification
    
    Note over API: SecurityConfig vérifie:<br/>POST /api/contact → permitAll() ✅
    
    API->>API: ContactController.submitContactForm()
    API->>API: Validation des données (@Valid)
    API->>DB: INSERT INTO leads (...)
    API->>MAIL: Envoie notification à l'admin
    API-->>F: 200 OK { message: "Merci!..." }
    F-->>V: Affiche confirmation
```

**Requête HTTP** :
```http
POST /api/contact HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
    "fullName": "Marie Dupont",
    "email": "marie@example.com",
    "phone": "0612345678",
    "company": "TechCorp",
    "subject": "Demande de devis",
    "message": "Bonjour, je souhaite..."
}
```

**Réponse** :
```json
{
    "message": "Merci! Votre message a été envoyé. Nous vous répondrons bientôt."
}
```

---

### SCÉNARIO B : Administrateur se connecte

**Acteur** : Administrateur avec compte existant
**Endpoint** : `POST /api/auth/login`
**Protection** : Aucune (public) - mais vérifie les credentials

```mermaid
sequenceDiagram
    participant A as Admin
    participant F as Frontend Admin
    participant API as API Spring Boot
    participant SS as Spring Security
    participant DB as PostgreSQL
    participant JWT as JwtService
    
    A->>F: Entre email + mot de passe
    F->>API: POST /api/auth/login
    
    Note over API: SecurityConfig:<br/>/api/auth/** → permitAll() ✅
    
    API->>SS: AuthController appelle<br/>authenticationManager.authenticate()
    SS->>DB: SELECT * FROM users<br/>WHERE email='admin@test.com'
    DB-->>SS: User (avec password hashé)
    SS->>SS: BCrypt.matches(password, hash)
    
    alt Mot de passe correct
        SS-->>API: Authentication success
        API->>JWT: generateToken(user)
        JWT-->>API: "eyJhbGciOiJIUzI1NiJ9..."
        API-->>F: 200 OK { token: "eyJ...", role: "ADMIN" }
        F->>F: Stocke le token (localStorage)
        F-->>A: Redirige vers dashboard
    else Mot de passe incorrect
        SS-->>API: BadCredentialsException
        API-->>F: 401 Unauthorized
        F-->>A: Affiche erreur
    end
```

**Requête HTTP** :
```http
POST /api/auth/login HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
    "email": "admin@test.com",
    "password": "admin123"
}
```

**Réponse succès** :
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkB0ZXN0LmNvbSIsImlhdCI6MTcwNTMxMjAwMCwiZXhwIjoxNzA1Mzk4NDAwfQ.xxx",
    "type": "Bearer",
    "expiresIn": 86400000,
    "email": "admin@test.com",
    "role": "ADMIN"
}
```

**Réponse erreur** :
```json
{
    "timestamp": "2024-01-15T10:30:00",
    "status": 401,
    "error": "Unauthorized",
    "message": "Bad credentials"
}
```

---

### SCÉNARIO C : Admin consulte la liste des leads

**Acteur** : Administrateur connecté (avec JWT)
**Endpoint** : `GET /api/admin/leads`
**Protection** : ROLE_ADMIN requis

#### Vue simplifiée (3 étapes)

```mermaid
graph LR
    A["1. Client envoie<br/>GET + JWT"] --> B["2. JwtAuthFilter<br/>valide le token"]
    B --> C["3. SecurityConfig<br/>vérifie ROLE_ADMIN"]
    C --> D["4. LeadController<br/>retourne les leads"]
    
    style A fill:#2196F3,color:#fff
    style B fill:#FF9800,color:#fff
    style C fill:#4CAF50,color:#fff
    style D fill:#9C27B0,color:#fff
```

#### Étape 1 : Le client envoie la requête

```mermaid
sequenceDiagram
    participant A as Admin
    participant F as Frontend
    participant API as API
    
    A->>F: Clique "Voir les leads"
    F->>F: Token = localStorage.getItem('token')
    F->>API: GET /api/admin/leads<br/>Authorization: Bearer eyJ...
```

#### Étape 2 : JwtAuthFilter valide le token

```mermaid
sequenceDiagram
    participant API as API
    participant JAF as JwtAuthFilter
    participant JS as JwtService
    participant DB as Database
    
    API->>JAF: Requête arrive
    JAF->>JAF: Extrait "Bearer eyJ..." → jwt
    JAF->>JS: extractUsername(jwt)
    JS-->>JAF: "admin@test.com"
    JAF->>DB: findByEmail("admin@test.com")
    DB-->>JAF: User(ADMIN)
    JAF->>JS: isTokenValid(jwt, user)
    JS-->>JAF: true ✅
    JAF->>JAF: SecurityContext.setAuthentication(user)
```

#### Étape 3 : Vérification des règles et réponse

```mermaid
sequenceDiagram
    participant JAF as JwtAuthFilter
    participant SC as SecurityConfig
    participant LC as LeadController
    participant DB as Database
    participant F as Frontend
    
    JAF->>SC: filterChain.doFilter()
    SC->>SC: /api/admin/** → hasRole('ADMIN')?<br/>User a ROLE_ADMIN ✅
    SC->>LC: Accès autorisé
    LC->>DB: SELECT * FROM leads
    DB-->>LC: [leads...]
    LC-->>F: 200 OK { content: [...] }
```

**Requête HTTP** :
```http
GET /api/admin/leads?page=0&size=20&sort=createdAt,desc HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Réponse** :
```json
{
    "content": [
        {
            "id": 15,
            "fullName": "Marie Dupont",
            "email": "marie@example.com",
            "phone": "0612345678",
            "company": "TechCorp",
            "subject": "Demande de devis",
            "message": "Bonjour, je souhaite...",
            "status": "NEW",
            "createdAt": "2024-01-15T10:30:00"
        },
        {
            "id": 14,
            "fullName": "Jean Martin",
            "email": "jean@example.com",
            ...
        }
    ],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 20
    },
    "totalElements": 87,
    "totalPages": 5
}
```

---

### SCÉNARIO D : Admin change le statut d'un lead

**Acteur** : Administrateur connecté
**Endpoint** : `PUT /api/admin/leads/{id}/status`
**Protection** : ROLE_ADMIN requis

```mermaid
sequenceDiagram
    participant A as Admin
    participant F as Frontend
    participant API as API
    participant DB as PostgreSQL
    participant MAIL as Email Service
    
    A->>F: Sélectionne "Contacté" dans le dropdown
    F->>API: PUT /api/admin/leads/15/status<br/>Authorization: Bearer eyJ...
    
    Note over API: JwtAuthFilter valide le JWT ✅<br/>SecurityConfig vérifie ROLE_ADMIN ✅
    
    API->>DB: UPDATE leads<br/>SET status='CONTACTED'<br/>WHERE id=15
    DB-->>API: Lead mis à jour
    
    alt Statut = CONTACTED
        API->>MAIL: Envoie email au visiteur<br/>"Nous avons bien reçu..."
    end
    
    API-->>F: 200 OK { lead mis à jour }
    F-->>A: Met à jour l'affichage
```

**Requête HTTP** :
```http
PUT /api/admin/leads/15/status HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
    "status": "CONTACTED"
}
```

**Réponse** :
```json
{
    "id": 15,
    "fullName": "Marie Dupont",
    "email": "marie@example.com",
    "status": "CONTACTED",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T14:45:00"
}
```

---

### SCÉNARIO E : Tentative d'accès sans JWT (REFUSÉ)

**Acteur** : Quelqu'un sans authentification
**Endpoint** : `GET /api/admin/leads`
**Protection** : ROLE_ADMIN requis

```mermaid
sequenceDiagram
    participant H as Hacker/Visiteur
    participant JAF as JwtAuthFilter
    participant SC as SecurityConfig
    
    H->>JAF: GET /api/admin/leads<br/>(pas de header Authorization)
    
    Note over JAF: authHeader == null<br/>→ Pas d'authentification créée<br/>→ SecurityContext VIDE
    
    JAF->>SC: Continue (filterChain.doFilter)
    
    Note over SC: /api/admin/** → hasRole('ADMIN')<br/>Pas d'Authentication → ❌
    
    SC-->>H: 401 Unauthorized
```

**Requête HTTP** :
```http
GET /api/admin/leads HTTP/1.1
Host: localhost:8080
```

**Réponse** :
```json
{
    "timestamp": "2024-01-15T10:30:00",
    "status": 401,
    "error": "Unauthorized",
    "path": "/api/admin/leads"
}
```

---

### SCÉNARIO F : Tentative avec JWT invalide (REFUSÉ)

**Acteur** : Quelqu'un avec un faux token
**Endpoint** : `GET /api/admin/leads`

```mermaid
sequenceDiagram
    participant H as Hacker
    participant JAF as JwtAuthFilter
    participant JS as JwtService
    participant SC as SecurityConfig
    
    H->>JAF: GET /api/admin/leads<br/>Authorization: Bearer fake.token.here
    
    JAF->>JS: extractUsername("fake.token.here")
    JS->>JS: Décode le JWT...<br/>Signature invalide!
    JS-->>JAF: Exception!
    
    Note over JAF: catch (Exception e)<br/>→ Pas d'authentification créée
    
    JAF->>SC: Continue (filterChain.doFilter)
    
    Note over SC: Pas d'Authentication → ❌
    
    SC-->>H: 401 Unauthorized
```

---

### SCÉNARIO G : JWT expiré (REFUSÉ)

**Acteur** : Admin avec un vieux token
**Endpoint** : `GET /api/admin/leads`

```mermaid
sequenceDiagram
    participant A as Admin
    participant JAF as JwtAuthFilter
    participant JS as JwtService
    participant SC as SecurityConfig
    
    A->>JAF: GET /api/admin/leads<br/>Authorization: Bearer eyJ... (expiré)
    
    JAF->>JS: extractUsername(jwt)
    JS-->>JAF: "admin@test.com"
    
    JAF->>JS: isTokenValid(jwt, user)
    JS->>JS: extractExpiration(jwt)<br/>→ 2024-01-14 (hier!)
    JS->>JS: isTokenExpired? → true
    JS-->>JAF: false (token invalide)
    
    Note over JAF: Token invalide<br/>→ Pas d'authentification créée
    
    JAF->>SC: Continue
    SC-->>A: 401 Unauthorized
```

**Solution** : L'admin doit se reconnecter pour obtenir un nouveau token.

---

## 5. Flux complet d'une session utilisateur

```mermaid
sequenceDiagram
    participant V as Visiteur
    participant A as Admin
    participant API as API
    participant DB as DB
    
    Note over V,DB: === PARTIE 1: Visiteur soumet formulaire ===
    V->>API: POST /api/contact (public)
    API->>DB: INSERT lead
    API-->>V: 200 OK "Merci!"
    
    Note over A,DB: === PARTIE 2: Admin se connecte ===
    A->>API: POST /api/auth/login (public)
    API->>DB: Vérifie credentials
    API-->>A: 200 OK { token: "eyJ..." }
    
    Note over A,DB: === PARTIE 3: Admin gère les leads ===
    A->>API: GET /api/admin/leads<br/>+ Bearer token
    API->>DB: SELECT leads
    API-->>A: 200 OK [leads]
    
    A->>API: PUT /api/admin/leads/15/status<br/>+ Bearer token
    API->>DB: UPDATE lead
    API-->>A: 200 OK {lead}
    
    Note over A,DB: === PARTIE 4: Token expire (24h plus tard) ===
    A->>API: GET /api/admin/leads<br/>+ Bearer token (expiré)
    API-->>A: 401 Unauthorized
    
    Note over A: Doit se reconnecter
    A->>API: POST /api/auth/login
    API-->>A: 200 OK { nouveau token }
```

---

## 6. Tests avec cURL

### Test 1 : Soumettre un formulaire (public)

```bash
curl -X POST http://localhost:8080/api/contact \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "email": "test@example.com",
    "phone": "0600000000",
    "subject": "Test",
    "message": "Ceci est un test"
  }'
```

### Test 2 : Se connecter

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@test.com",
    "password": "admin123"
  }'
```

### Test 3 : Accéder aux leads (avec token)

```bash
# Remplacez TOKEN par le token reçu au login
curl -X GET http://localhost:8080/api/admin/leads \
  -H "Authorization: Bearer TOKEN"
```

### Test 4 : Changer le statut d'un lead

```bash
curl -X PUT http://localhost:8080/api/admin/leads/1/status \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status": "CONTACTED"}'
```

### Test 5 : Tentative sans token (doit échouer)

```bash
curl -X GET http://localhost:8080/api/admin/leads
# Résultat attendu: 401 Unauthorized
```

---

## 7. Codes de réponse HTTP par endpoint

| Endpoint | 200 OK | 201 Created | 400 Bad Request | 401 Unauthorized | 403 Forbidden | 404 Not Found |
|----------|--------|-------------|-----------------|------------------|---------------|---------------|
| POST /api/contact | ✅ Succès | - | Validation échouée | - | - | - |
| POST /api/auth/login | ✅ Token | - | - | Mauvais credentials | - | - |
| GET /api/admin/leads | ✅ Liste | - | - | Pas de JWT | USER (pas ADMIN) | - |
| GET /api/admin/leads/{id} | ✅ Lead | - | - | Pas de JWT | USER | Lead inexistant |
| PUT /api/admin/leads/{id}/status | ✅ Lead | - | Statut invalide | Pas de JWT | USER | Lead inexistant |
| DELETE /api/admin/leads/{id} | ✅ Message | - | - | Pas de JWT | USER | Lead inexistant |
| GET /api/admin/leads/stats | ✅ Stats | - | - | Pas de JWT | USER | - |

---

## 8. Statuts possibles d'un Lead

```mermaid
stateDiagram-v2
    [*] --> NEW: Formulaire soumis
    NEW --> CONTACTED: Admin contacte
    NEW --> IN_PROGRESS: Admin travaille dessus
    CONTACTED --> IN_PROGRESS: Suite à discussion
    IN_PROGRESS --> CONVERTED: Client signé!
    IN_PROGRESS --> LOST: Pas intéressé
    CONTACTED --> CONVERTED: Accord rapide
    CONTACTED --> LOST: Refus
    
    NEW --> SPAM: Marqué comme spam
    
    CONVERTED --> [*]
    LOST --> [*]
    SPAM --> [*]
```

| Statut | Description | Couleur suggérée |
|--------|-------------|------------------|
| `NEW` | Nouveau lead, non traité | 🔵 Bleu |
| `CONTACTED` | Admin a contacté le visiteur | 🟡 Jaune |
| `IN_PROGRESS` | Discussion en cours | 🟠 Orange |
| `CONVERTED` | Devenu client | 🟢 Vert |
| `LOST` | Opportunité perdue | ⚫ Gris |
| `SPAM` | Spam/Test | 🔴 Rouge |

---

## 9. Résumé visuel de la sécurité

```mermaid
graph TB
    subgraph "Internet"
        V["Visiteur anonyme"]
        A["Admin avec JWT"]
        H["Hacker sans JWT"]
    end
    
    subgraph "API e-contact-backend"
        subgraph "Zone publique"
            EP1["POST /api/contact"]
            EP2["POST /api/auth/login"]
            EP3["Swagger UI"]
        end
        
        subgraph "Zone protégée (ADMIN)"
            EP4["GET /api/admin/leads"]
            EP5["PUT /api/admin/leads/{id}/status"]
            EP6["DELETE /api/admin/leads/{id}"]
        end
    end
    
    V -->|"✅"| EP1
    V -->|"✅"| EP2
    V -->|"✅"| EP3
    V -->|"❌ 401"| EP4
    
    A -->|"✅"| EP1
    A -->|"✅"| EP2
    A -->|"✅ JWT valide"| EP4
    A -->|"✅ JWT valide"| EP5
    A -->|"✅ JWT valide"| EP6
    
    H -->|"❌ 401"| EP4
    H -->|"❌ 401"| EP5
    H -->|"❌ 401"| EP6
    
    style EP1 fill:#4CAF50,color:#fff
    style EP2 fill:#4CAF50,color:#fff
    style EP3 fill:#4CAF50,color:#fff
    style EP4 fill:#f44336,color:#fff
    style EP5 fill:#f44336,color:#fff
    style EP6 fill:#f44336,color:#fff
```

---

## Navigation

| Précédent | Suivant |
|-----------|---------|
| [Annexe D - Ressources](annexe-d-ressources.md) | [Annexe F - Erreurs courantes](annexe-f-erreurs-courantes.md) |

