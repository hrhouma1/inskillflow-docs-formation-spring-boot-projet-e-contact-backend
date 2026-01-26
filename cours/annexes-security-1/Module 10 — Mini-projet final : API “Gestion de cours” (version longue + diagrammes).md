### Module 10 — Mini-projet final : API “Gestion de cours” (version longue + diagrammes Mermaid)

Objectif du module
Construire une API REST complète et réaliste, sécurisée avec Spring Security, avec une séparation propre (auth / user / security), des rôles/permissions, JWT (access + refresh), des règles d’accès cohérentes, et une validation par scénarios (401/403). Le projet est conçu pour être documenté sur GitHub.

---

1. Cahier des charges (fonctionnel)

1) Types d’utilisateurs

* VISITOR : non connecté (pas un rôle, juste “public”)
* USER : utilisateur standard
* ADMIN : administrateur

2. Fonctionnalités principales

* Consultation publique des cours
* Inscription d’un utilisateur à un cours
* Gestion des cours (CRUD) réservée à ADMIN (et éventuellement MANAGER si extension)
* Authentification JWT avec refresh token

3. Contraintes importantes

* API uniquement (JSON), pas de pages HTML
* Stateless pour les routes API (pas de session serveur)
* Erreurs 401/403 standardisées en JSON
* CORS configuré pour un front séparé

---

2. Modèle d’accès (règles)

Règles simples (version de base) :

* Public :

  * GET /api/courses
  * GET /api/courses/{id}
  * POST /api/auth/login
  * POST /api/auth/refresh
  * POST /api/auth/register (optionnel)

* Authentifié (USER ou ADMIN) :

  * POST /api/enrollments/{courseId}
  * GET /api/me
  * GET /api/me/enrollments

* ADMIN seulement :

  * POST /api/courses
  * PUT /api/courses/{id}
  * DELETE /api/courses/{id}
  * GET /api/admin/** (optionnel)

---

3. Diagramme d’architecture (Mermaid)

```mermaid
flowchart LR
  Client[Client Web/Mobile] -->|HTTP JSON| API[Spring Boot API]
  API --> SEC[Spring Security Filter Chain]
  SEC -->|if allowed| CTRL[Controllers]
  CTRL --> SRV[Services]
  SRV --> DB[(Database)]
  SEC -->|401/403 JSON| ERR[Security Error Handlers]

  API --> JWT[JWT Validation Filter]
  JWT --> SEC
```

Idée à retenir
Toutes les requêtes passent d’abord par la sécurité, puis seulement ensuite par tes controllers.

---

4. Flux Auth : login → access token + refresh token

```mermaid
sequenceDiagram
  autonumber
  participant U as User/Client
  participant A as API (/auth)
  participant S as Spring Security
  participant D as User DB

  U->>A: POST /auth/login (username, password)
  A->>D: load user by username/email
  D-->>A: user + passwordHash + roles
  A->>A: verify password (PasswordEncoder)
  A-->>U: 200 (accessToken, refreshToken)

  U->>A: GET /api/me (Authorization: Bearer accessToken)
  A->>S: JWT filter validates token
  S-->>A: user authenticated in SecurityContext
  A-->>U: 200 (profile JSON)
```

---

5. Flux Refresh : access token expiré → refresh → nouveau access token

```mermaid
sequenceDiagram
  autonumber
  participant U as User/Client
  participant A as API (/auth)
  participant D as RefreshToken Store (DB)

  U->>A: POST /auth/refresh (refreshToken)
  A->>D: validate refreshToken (exists, not revoked, not expired)
  D-->>A: OK
  A-->>U: 200 (new accessToken, new refreshToken optional)
```

Note pédagogique
Le refresh token est l’endroit où tu peux gérer la révocation de manière “entreprise”.

---

6. Flux d’autorisation : 401 vs 403 (à tester)

```mermaid
sequenceDiagram
  autonumber
  participant C as Client
  participant API as Spring Boot API
  participant SEC as Security Filters

  C->>API: POST /api/courses (no token)
  API->>SEC: check security
  SEC-->>C: 401 (not authenticated)

  C->>API: POST /api/courses (token USER)
  API->>SEC: check roles/permissions
  SEC-->>C: 403 (forbidden)

  C->>API: POST /api/courses (token ADMIN)
  API->>SEC: authorized
  SEC-->>API: proceed
  API-->>C: 201 (created)
```

---

7. Modèle de données (conceptuel)

Entités minimales :

* users

  * id
  * email (ou username)
  * password_hash
  * role (USER/ADMIN) ou authorities
  * enabled

* courses

  * id
  * title
  * description
  * createdAt

* enrollments

  * id
  * user_id
  * course_id
  * enrolledAt

Option refresh tokens (si tu veux refresh “propre”)

* refresh_tokens

  * id
  * user_id
  * token_hash (on évite de stocker le token en clair)
  * expiresAt
  * revoked

---

8. Diagramme de classes (Mermaid)

```mermaid
classDiagram
  class User {
    +Long id
    +String email
    +String passwordHash
    +boolean enabled
    +String role
  }

  class Course {
    +Long id
    +String title
    +String description
    +Instant createdAt
  }

  class Enrollment {
    +Long id
    +Instant enrolledAt
  }

  User "1" --> "0..*" Enrollment
  Course "1" --> "0..*" Enrollment
```

---

9. Organisation des packages (structure GitHub)

Structure recommandée (lisible) :

* security

  * config
  * jwt
  * exception
  * permissions

* auth

  * controller
  * service
  * dto

* user

  * entity
  * repository
  * service
  * controller (me endpoints)

* course

  * entity
  * repository
  * service
  * controller

* enrollment

  * entity
  * repository
  * service
  * controller

But :

* éviter un monolithe de classes
* permettre une lecture rapide par domaine

---

10. Scénarios de test obligatoires (liste GitHub)

Auth

* login OK (200)
* login mauvais mot de passe (401)
* refresh OK (200)
* refresh token invalide (401)

Courses

* GET /courses public (200)
* POST /courses sans token (401)
* POST /courses avec USER (403)
* POST /courses avec ADMIN (201)

Enrollments

* POST /enrollments/{courseId} sans token (401)
* POST /enrollments/{courseId} USER (201)
* POST /enrollments/{courseId} ADMIN (201 ou règle à définir)

Me

* GET /me sans token (401)
* GET /me USER (200)

---

11. Livrables (ce que tu mets sur GitHub)

* README.md

  * description du projet
  * endpoints + règles d’accès
  * diagrammes Mermaid (ceux ci-dessus)
  * comment lancer
  * comment tester (Postman/curl)
* collection Postman (optionnel)
* tests d’intégration security
* scripts de seed (users/courses)
* exemples de réponses JSON (success + errors)

---

12. Extension “bonus” (si tu veux pousser niveau entreprise)

* Ajouter permissions fines (course:create, course:update, course:delete)
* Mettre rôles comme bundle de permissions
* Rotation refresh token
* Stocker refresh token hashé en base
* Ajouter audit log minimal (login success/fail, forbidden attempts)
* Protéger swagger en dev seulement ou role ADMIN
* Ajouter OAuth2 login (Google/Microsoft) + mapping user interne

