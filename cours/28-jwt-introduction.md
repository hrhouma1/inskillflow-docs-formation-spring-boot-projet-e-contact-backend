# Chapitre 6.1 - Qu'est-ce que JWT?

## Objectifs du chapitre

- Comprendre JWT et son fonctionnement
- Connaitre les avantages et inconvenients
- Savoir quand utiliser JWT

---

## 1. Definition

### JWT = JSON Web Token

Un **JWT** est un standard ouvert (RFC 7519) pour transmettre des informations de maniere securisee entre deux parties sous forme de JSON.

### Caracteristiques

- **Compact**: Peut etre envoye dans une URL, un header HTTP ou un cookie
- **Auto-contenu**: Contient toutes les informations necessaires
- **Signe**: Garantit l'integrite des donnees

---

## 2. Pourquoi JWT?

### Authentification traditionnelle (sessions)

```
1. Client envoie login/password
2. Serveur cree une session (stockee en memoire/base)
3. Serveur envoie un cookie de session
4. Client envoie le cookie a chaque requete
5. Serveur verifie la session dans sa memoire
```

**Problemes**:
- Le serveur doit stocker les sessions
- Difficile a scaler (plusieurs serveurs)
- Pas adapte aux microservices

### Authentification JWT (stateless)

```
1. Client envoie login/password
2. Serveur genere un JWT (rien stocke)
3. Serveur envoie le JWT au client
4. Client envoie le JWT a chaque requete
5. Serveur verifie le JWT (pas de lookup en base)
```

**Avantages**:
- Serveur stateless (rien a stocker)
- Scalable (load balancer)
- Adapte aux microservices

---

## 3. Structure d'un JWT

### Trois parties

```
xxxxx.yyyyy.zzzzz
HEADER.PAYLOAD.SIGNATURE
```

### Exemple reel

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTcwNTMxMjAwMCwiZXhwIjoxNzA1Mzk4NDAwfQ.abc123signature
```

---

## 4. Header

### Contenu

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

| Champ | Description |
|-------|-------------|
| alg | Algorithme de signature (HS256, RS256...) |
| typ | Type de token (toujours "JWT") |

### Encodage

Le header est encode en Base64URL.

---

## 5. Payload (Claims)

### Contenu

```json
{
  "sub": "admin@example.com",
  "iat": 1705312000,
  "exp": 1705398400,
  "role": "ADMIN"
}
```

### Claims standards

| Claim | Description |
|-------|-------------|
| sub | Subject (identifiant utilisateur) |
| iat | Issued At (date de creation) |
| exp | Expiration (date d'expiration) |
| iss | Issuer (emetteur) |
| aud | Audience (destinataire) |

### Claims personnalises

Vous pouvez ajouter n'importe quelle donnee:

```json
{
  "sub": "admin@example.com",
  "role": "ADMIN",
  "department": "IT",
  "permissions": ["READ", "WRITE", "DELETE"]
}
```

### Attention!

Le payload est encode, pas chiffre. N'y mettez JAMAIS de donnees sensibles (mot de passe, numero de carte...).

---

## 6. Signature

### Role

La signature garantit que le token n'a pas ete modifie.

### Calcul

```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret
)
```

### Verification

1. Le serveur recalcule la signature avec sa cle secrete
2. Compare avec la signature du token
3. Si different -> token invalide/modifie

---

## 7. Flux d'authentification

### Diagramme

```
Client                                  Serveur
   |                                       |
   |------ POST /api/auth/login ---------->|
   |       { email, password }             |
   |                                       |
   |                              Verifie credentials
   |                              Genere JWT
   |                                       |
   |<----- { token: "eyJ..." } ------------|
   |                                       |
   |  Stocke le token (localStorage)       |
   |                                       |
   |------ GET /api/admin/leads ---------->|
   |       Authorization: Bearer eyJ...    |
   |                                       |
   |                              Verifie JWT
   |                              Extrait user
   |                                       |
   |<----- [ leads... ] ------------------|
```

### Header Authorization

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 8. Avantages et inconvenients

### Avantages

| Avantage | Description |
|----------|-------------|
| Stateless | Serveur ne stocke rien |
| Scalable | Fonctionne avec load balancer |
| Cross-domain | Peut etre utilise entre domaines |
| Mobile-friendly | Pas de cookies |
| Microservices | Chaque service peut verifier |

### Inconvenients

| Inconvenient | Description |
|--------------|-------------|
| Taille | Plus gros qu'un cookie de session |
| Revocation | Difficile d'invalider un token |
| Securite | Si la cle est compromise, tous les tokens sont compromis |
| Stockage client | localStorage vulnerable au XSS |

---

## 9. Bonnes pratiques

### Duree de vie courte

```java
// 1 heure au lieu de 1 semaine
@Value("${app.jwt.expiration}")
private long expiration = 3600000;  // 1h en ms
```

### Cle secrete forte

```java
// MAUVAIS
private String secret = "secret";

// BON (au moins 256 bits pour HS256)
private String secret = "dGhpcy1pcy1hLXZlcnktbG9uZy1zZWNyZXQta2V5LWZvci1qd3Q=";
```

### HTTPS obligatoire

Les tokens sont transmis dans les headers. Sans HTTPS, ils peuvent etre interceptes.

### Ne pas stocker de donnees sensibles

```json
// MAUVAIS
{ "password": "secret123", "creditCard": "1234..." }

// BON
{ "sub": "admin@example.com", "role": "ADMIN" }
```

---

## 10. Points cles a retenir

1. **JWT** = token auto-contenu et signe
2. **3 parties**: Header, Payload, Signature
3. **Stateless**: Le serveur ne stocke rien
4. **Header Authorization**: Bearer <token>
5. **Duree de vie courte** et **cle secrete forte**

---

## QUIZ 6.1 - Qu'est-ce que JWT?

**1. Que signifie JWT?**
   - a) Java Web Token
   - b) JSON Web Token
   - c) JavaScript Web Token
   - d) Java Web Transfer

**2. Combien de parties compose un JWT?**
   - a) 2
   - b) 3
   - c) 4
   - d) 5

**3. Le payload JWT est-il chiffre?**
   - a) Oui, toujours
   - b) Non, seulement encode
   - c) Ca depend de l'algorithme
   - d) Oui, avec la cle secrete

**4. A quoi sert la signature?**
   - a) Chiffrer les donnees
   - b) Garantir l'integrite
   - c) Identifier l'utilisateur
   - d) Stocker les permissions

**5. VRAI ou FAUX: JWT est stateful comme les sessions.**

**6. Quel header HTTP contient le JWT?**
   - a) X-Token
   - b) Authentication
   - c) Authorization
   - d) JWT-Token

**7. Quel est le format du header Authorization?**
   - a) JWT <token>
   - b) Bearer <token>
   - c) Token <token>
   - d) Auth <token>

**8. Completez: Le claim "exp" indique la date d'_______.**

**9. Peut-on stocker un mot de passe dans un JWT?**
   - a) Oui
   - b) Non, c'est dangereux
   - c) Seulement s'il est hashe
   - d) Seulement en HTTPS

**10. Pourquoi JWT est adapte aux microservices?**
   - a) Il est petit
   - b) Chaque service peut verifier le token sans base partagee
   - c) Il est chiffre
   - d) Il ne contient pas de donnees

---

### REPONSES QUIZ 6.1

1. b) JSON Web Token
2. b) 3 (Header, Payload, Signature)
3. b) Non, seulement encode
4. b) Garantir l'integrite
5. FAUX (stateless)
6. c) Authorization
7. b) Bearer <token>
8. expiration
9. b) Non, c'est dangereux
10. b) Chaque service peut verifier le token sans base partagee

