# Chapitre 6.1 - Qu'est-ce que JWT?

## Objectifs du chapitre

- Comprendre JWT et son fonctionnement
- Maîtriser la structure d'un token
- Connaître les avantages et inconvénients
- Savoir quand utiliser JWT

---

## 1. Définition

### JWT = JSON Web Token

Un **JWT** (prononcé "jot") est un standard ouvert (RFC 7519) pour transmettre des informations de manière sécurisée entre deux parties sous forme de JSON.

### Diagramme : Vue d'ensemble

```mermaid
graph LR
    CLIENT[Client] -->|"1. Login"| SERVER[Serveur]
    SERVER -->|"2. JWT"| CLIENT
    CLIENT -->|"3. JWT dans header"| SERVER
    SERVER -->|"4. Données"| CLIENT
    
    style SERVER fill:#4CAF50,color:#fff
```

### Caractéristiques principales

```mermaid
mindmap
  root((JWT))
    Compact
      URL-safe
      Header HTTP
      Cookie
    Auto-contenu
      Données utilisateur
      Pas de lookup DB
      Standalone
    Signé
      Intégrité garantie
      Non falsifiable
      Vérifiable
```

- **Compact** : Peut être envoyé dans une URL, un header HTTP ou un cookie
- **Auto-contenu** : Contient toutes les informations nécessaires sur l'utilisateur
- **Signé** : Garantit l'intégrité des données (non modifiable)

---

## 2. Pourquoi JWT?

### Authentification traditionnelle (sessions)

```mermaid
sequenceDiagram
    participant C as Client
    participant S as Serveur
    participant DB as Base/Mémoire
    
    C->>S: POST /login (email, password)
    S->>DB: Créer session
    S-->>C: Cookie de session (JSESSIONID)
    
    C->>S: GET /api/data + Cookie
    S->>DB: Vérifier session
    DB-->>S: Session valide
    S-->>C: Données
    
    Note over S,DB: Le serveur doit stocker<br/>toutes les sessions!
```

**Problèmes des sessions :**
- Le serveur doit **stocker** toutes les sessions
- Difficile à **scaler** (plusieurs serveurs = session partagée?)
- **Pas adapté** aux microservices
- Problèmes de **synchronisation**

### Authentification JWT (stateless)

```mermaid
sequenceDiagram
    participant C as Client
    participant S as Serveur
    
    C->>S: POST /login (email, password)
    Note over S: Génère JWT<br/>(rien stocké!)
    S-->>C: { token: "eyJ..." }
    
    C->>S: GET /api/data<br/>Authorization: Bearer eyJ...
    Note over S: Vérifie signature<br/>(pas de DB!)
    S-->>C: Données
    
    Note over S: Serveur stateless!
```

**Avantages de JWT :**
- Serveur **stateless** (rien à stocker)
- **Scalable** (load balancer transparent)
- **Adapté** aux microservices
- Fonctionne **cross-domain**

### Comparaison

```mermaid
graph TB
    subgraph "Sessions (Stateful)"
        S1["Serveur stocke les sessions"]
        S2["Cookie de session"]
        S3["Lookup DB à chaque requête"]
    end
    
    subgraph "JWT (Stateless)"
        J1["Serveur ne stocke rien"]
        J2["Token auto-contenu"]
        J3["Vérification locale"]
    end
    
    style S1 fill:#f44336,color:#fff
    style J1 fill:#4CAF50,color:#fff
```

---

## 3. Structure d'un JWT

### Trois parties séparées par des points

```mermaid
graph LR
    JWT[JWT] --> H["HEADER<br/>(xxxxx)"]
    JWT --> P["PAYLOAD<br/>(yyyyy)"]
    JWT --> S["SIGNATURE<br/>(zzzzz)"]
    
    H -->|"."| P
    P -->|"."| S
```

```
xxxxx.yyyyy.zzzzz
HEADER.PAYLOAD.SIGNATURE
```

### Exemple réel

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTcwNTMxMjAwMCwiZXhwIjoxNzA1Mzk4NDAwfQ.abc123signature
```

### Visualisation

```mermaid
graph TB
    subgraph "Header (Base64)"
        H1["eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"]
        H2["↓ décodé ↓"]
        H3["{<br/>  alg: 'HS256',<br/>  typ: 'JWT'<br/>}"]
    end
    
    subgraph "Payload (Base64)"
        P1["eyJzdWIiOiJhZG1pbi..."]
        P2["↓ décodé ↓"]
        P3["{<br/>  sub: 'admin@example.com',<br/>  iat: 1705312000,<br/>  exp: 1705398400<br/>}"]
    end
    
    subgraph "Signature"
        S1["HMACSHA256(<br/>header + '.' + payload,<br/>SECRET<br/>)"]
    end
```

---

## 4. Header

### Contenu

Le header contient les métadonnées sur l'algorithme utilisé.

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

| Champ | Description | Valeurs courantes |
|-------|-------------|-------------------|
| alg | Algorithme de signature | HS256, RS256, ES256 |
| typ | Type de token | JWT |

### Algorithmes courants

```mermaid
graph TB
    ALG[Algorithmes] --> SYM["Symétriques<br/>(même clé)"]
    ALG --> ASYM["Asymétriques<br/>(clé publique/privée)"]
    
    SYM --> HS256["HS256<br/>HMAC + SHA-256"]
    SYM --> HS384["HS384"]
    SYM --> HS512["HS512"]
    
    ASYM --> RS256["RS256<br/>RSA + SHA-256"]
    ASYM --> ES256["ES256<br/>ECDSA"]
    
    style HS256 fill:#4CAF50,color:#fff
```

> **Note** : HS256 est le plus courant pour les applications simples. RS256 est utilisé quand plusieurs services doivent vérifier les tokens.

### Encodage

Le header est encodé en **Base64URL** (pas chiffré!).

---

## 5. Payload (Claims)

### Contenu

Le payload contient les "claims" (revendications) - les données sur l'utilisateur.

```json
{
  "sub": "admin@example.com",
  "iat": 1705312000,
  "exp": 1705398400,
  "role": "ADMIN"
}
```

### Claims standards (registered)

```mermaid
graph TB
    CLAIMS[Claims standards] --> SUB["sub<br/>Subject (utilisateur)"]
    CLAIMS --> IAT["iat<br/>Issued At (émission)"]
    CLAIMS --> EXP["exp<br/>Expiration"]
    CLAIMS --> ISS["iss<br/>Issuer (émetteur)"]
    CLAIMS --> AUD["aud<br/>Audience (destinataire)"]
    CLAIMS --> NBF["nbf<br/>Not Before"]
```

| Claim | Description | Exemple |
|-------|-------------|---------|
| sub | Subject (identifiant utilisateur) | admin@example.com |
| iat | Issued At (timestamp création) | 1705312000 |
| exp | Expiration (timestamp expiration) | 1705398400 |
| iss | Issuer (émetteur du token) | my-app |
| aud | Audience (destinataire) | api.example.com |
| nbf | Not Before (pas valide avant) | 1705312000 |

### Claims personnalisés

Vous pouvez ajouter n'importe quelle donnée :

```json
{
  "sub": "admin@example.com",
  "role": "ADMIN",
  "department": "IT",
  "permissions": ["READ", "WRITE", "DELETE"]
}
```

### Attention - Sécurité!

```mermaid
graph TB
    subgraph "❌ JAMAIS dans le payload"
        BAD1["Mot de passe"]
        BAD2["Numéro de carte"]
        BAD3["Données sensibles"]
    end
    
    subgraph "✅ OK dans le payload"
        GOOD1["Email/Username"]
        GOOD2["Rôle"]
        GOOD3["ID utilisateur"]
    end
    
    style BAD1 fill:#f44336,color:#fff
    style BAD2 fill:#f44336,color:#fff
    style BAD3 fill:#f44336,color:#fff
    style GOOD1 fill:#4CAF50,color:#fff
    style GOOD2 fill:#4CAF50,color:#fff
    style GOOD3 fill:#4CAF50,color:#fff
```

> **Important** : Le payload est **encodé**, pas **chiffré**. N'importe qui peut le décoder. Ne mettez JAMAIS de données sensibles!

---

## 6. Signature

### Rôle

La signature **garantit que le token n'a pas été modifié**.

### Calcul

```mermaid
graph LR
    H["Header (Base64)"] --> CONCAT
    P["Payload (Base64)"] --> CONCAT
    CONCAT["header.payload"] --> HMAC
    SECRET["Clé secrète"] --> HMAC
    HMAC["HMACSHA256()"] --> SIG["Signature"]
```

```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret
)
```

### Vérification

```mermaid
flowchart TB
    A[Token reçu] --> B[Extraire header.payload]
    B --> C[Recalculer signature avec SECRET]
    C --> D{Signature identique?}
    D -->|Oui| E["✅ Token valide"]
    D -->|Non| F["❌ Token modifié/invalide"]
    
    style E fill:#4CAF50,color:#fff
    style F fill:#f44336,color:#fff
```

1. Le serveur **recalcule** la signature avec sa clé secrète
2. **Compare** avec la signature du token reçu
3. Si différent → token **invalide ou modifié**

---

## 7. Flux d'authentification complet

### Diagramme détaillé

```mermaid
sequenceDiagram
    participant C as Client
    participant S as Serveur
    participant DB as Base de données
    
    Note over C,S: 1. AUTHENTIFICATION
    C->>S: POST /api/auth/login<br/>{ email, password }
    S->>DB: Vérifier credentials
    DB-->>S: Utilisateur trouvé
    Note over S: Générer JWT
    S-->>C: { token: "eyJ...", expiresIn: 3600 }
    
    Note over C: Stocker le token<br/>(localStorage/memory)
    
    Note over C,S: 2. REQUÊTES AUTHENTIFIÉES
    C->>S: GET /api/admin/leads<br/>Authorization: Bearer eyJ...
    Note over S: Vérifier JWT<br/>Extraire utilisateur
    S-->>C: [ leads... ]
    
    Note over C,S: 3. TOKEN EXPIRÉ
    C->>S: GET /api/admin/leads<br/>Authorization: Bearer eyJ... (expiré)
    S-->>C: 401 Unauthorized
    Note over C: Rediriger vers login
```

### Header Authorization

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
              └─────┘ └─────────────────────────────────────┘
               Type              Token JWT
```

---

## 8. Avantages et inconvénients

### Avantages

```mermaid
graph TB
    A[Avantages JWT] --> A1["Stateless<br/>Serveur ne stocke rien"]
    A --> A2["Scalable<br/>Load balancer facile"]
    A --> A3["Cross-domain<br/>Fonctionne partout"]
    A --> A4["Mobile-friendly<br/>Pas de cookies"]
    A --> A5["Microservices<br/>Chaque service vérifie"]
    
    style A fill:#4CAF50,color:#fff
```

| Avantage | Description |
|----------|-------------|
| Stateless | Serveur ne stocke rien |
| Scalable | Fonctionne avec load balancer |
| Cross-domain | Peut être utilisé entre domaines |
| Mobile-friendly | Pas de cookies nécessaires |
| Microservices | Chaque service peut vérifier indépendamment |

### Inconvénients

```mermaid
graph TB
    I[Inconvénients JWT] --> I1["Taille<br/>Plus gros qu'un cookie"]
    I --> I2["Révocation<br/>Difficile d'invalider"]
    I --> I3["Compromission<br/>Si clé volée = danger"]
    I --> I4["Stockage<br/>localStorage vulnérable XSS"]
    
    style I fill:#FF9800,color:#fff
```

| Inconvénient | Description | Solution |
|--------------|-------------|----------|
| Taille | Plus gros qu'un cookie de session | Minimiser les claims |
| Révocation | Difficile d'invalider un token | Courte durée de vie |
| Compromission | Clé compromise = tous tokens compromis | Rotation de clés |
| Stockage client | localStorage vulnérable au XSS | httpOnly cookies |

---

## 9. Bonnes pratiques

### 9.1 Durée de vie courte

```mermaid
graph LR
    subgraph "❌ Mauvais"
        B1["expiration: 1 semaine"]
    end
    
    subgraph "✅ Bon"
        G1["expiration: 1 heure"]
        G2["+ refresh token"]
    end
    
    style B1 fill:#f44336,color:#fff
    style G1 fill:#4CAF50,color:#fff
```

```java
// 1 heure au lieu de 1 semaine
@Value("${app.jwt.expiration}")
private long expiration = 3600000;  // 1h en ms
```

### 9.2 Clé secrète forte

```java
// ❌ MAUVAIS
private String secret = "secret";
private String secret = "123456";

// ✅ BON (au moins 256 bits pour HS256)
private String secret = "dGhpcy1pcy1hLXZlcnktbG9uZy1zZWNyZXQta2V5LWZvci1qd3Q=";
```

> **Règle** : Pour HS256, la clé doit faire au moins **256 bits** (32 caractères).

### 9.3 HTTPS obligatoire

```mermaid
graph TB
    subgraph "HTTP (❌)"
        H1[Token] -->|"En clair"| H2[Intercepté!]
    end
    
    subgraph "HTTPS (✅)"
        S1[Token] -->|"Chiffré TLS"| S2[Sécurisé]
    end
    
    style H2 fill:#f44336,color:#fff
    style S2 fill:#4CAF50,color:#fff
```

### 9.4 Ne pas stocker de données sensibles

```json
// ❌ MAUVAIS
{ "password": "secret123", "creditCard": "1234..." }

// ✅ BON
{ "sub": "admin@example.com", "role": "ADMIN" }
```

---

## 10. Points clés à retenir

```mermaid
mindmap
  root((JWT))
    Structure
      Header (algorithme)
      Payload (claims)
      Signature (intégrité)
    Caractéristiques
      Auto-contenu
      Stateless
      Signé mais pas chiffré
    Utilisation
      Header Authorization
      Bearer <token>
    Sécurité
      Durée courte
      Clé forte
      HTTPS
      Pas de données sensibles
```

1. **JWT** = token auto-contenu et signé
2. **3 parties** : Header, Payload, Signature
3. **Stateless** : Le serveur ne stocke rien
4. **Header Authorization** : `Bearer <token>`
5. **Durée de vie courte** et **clé secrète forte**

---

## QUIZ 6.1 - Qu'est-ce que JWT?

**1. Que signifie JWT?**
- a) Java Web Token
- b) JSON Web Token
- c) JavaScript Web Token
- d) Java Web Transfer

<details>
<summary>Voir la réponse</summary>

**Réponse : b) JSON Web Token**

JWT signifie JSON Web Token. C'est un standard (RFC 7519) pour transmettre des informations sous forme de JSON de manière sécurisée.
</details>

---

**2. Combien de parties compose un JWT?**
- a) 2
- b) 3
- c) 4
- d) 5

<details>
<summary>Voir la réponse</summary>

**Réponse : b) 3**

Un JWT est composé de 3 parties séparées par des points : Header.Payload.Signature
</details>

---

**3. Le payload JWT est-il chiffré?**
- a) Oui, toujours
- b) Non, seulement encodé
- c) Ça dépend de l'algorithme
- d) Oui, avec la clé secrète

<details>
<summary>Voir la réponse</summary>

**Réponse : b) Non, seulement encodé**

Le payload est encodé en Base64URL, pas chiffré. N'importe qui peut le décoder et lire son contenu. Ne jamais y mettre de données sensibles!
</details>

---

**4. À quoi sert la signature?**
- a) Chiffrer les données
- b) Garantir l'intégrité
- c) Identifier l'utilisateur
- d) Stocker les permissions

<details>
<summary>Voir la réponse</summary>

**Réponse : b) Garantir l'intégrité**

La signature garantit que le token n'a pas été modifié. Elle ne chiffre pas les données.
</details>

---

**5. VRAI ou FAUX : JWT est stateful comme les sessions.**

<details>
<summary>Voir la réponse</summary>

**Réponse : FAUX**

JWT est **stateless**. Le serveur ne stocke rien. Toutes les informations sont dans le token lui-même.
</details>

---

**6. Quel header HTTP contient le JWT?**
- a) X-Token
- b) Authentication
- c) Authorization
- d) JWT-Token

<details>
<summary>Voir la réponse</summary>

**Réponse : c) Authorization**

Le JWT est envoyé dans le header `Authorization` avec le préfixe `Bearer`.
</details>

---

**7. Quel est le format du header Authorization?**
- a) JWT <token>
- b) Bearer <token>
- c) Token <token>
- d) Auth <token>

<details>
<summary>Voir la réponse</summary>

**Réponse : b) Bearer <token>**

Le format standard est `Authorization: Bearer eyJhbGciOiJIUzI1NiIs...`
</details>

---

**8. Complétez : Le claim "exp" indique la date d'_______.**

<details>
<summary>Voir la réponse</summary>

**Réponse : expiration**

Le claim `exp` (expiration) contient un timestamp Unix indiquant quand le token expire.
</details>

---

**9. Peut-on stocker un mot de passe dans un JWT?**
- a) Oui
- b) Non, c'est dangereux
- c) Seulement s'il est hashé
- d) Seulement en HTTPS

<details>
<summary>Voir la réponse</summary>

**Réponse : b) Non, c'est dangereux**

Le payload JWT est seulement encodé, pas chiffré. N'importe qui peut décoder et lire le contenu. Ne jamais y stocker de données sensibles!
</details>

---

**10. Pourquoi JWT est adapté aux microservices?**
- a) Il est petit
- b) Chaque service peut vérifier le token sans base partagée
- c) Il est chiffré
- d) Il ne contient pas de données

<details>
<summary>Voir la réponse</summary>

**Réponse : b) Chaque service peut vérifier le token sans base partagée**

Chaque microservice peut vérifier la signature du JWT indépendamment avec la clé secrète, sans avoir besoin d'accéder à une base de sessions centralisée.
</details>

---

## Navigation

| Précédent | Suivant |
|-----------|---------|
| [22 - Spring Security Introduction](22-spring-security-intro.md) | [29 - Implémentation JWT](29-jwt-implementation.md) |
