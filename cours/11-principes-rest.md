# Chapitre 3.1 - Principes REST

## Objectifs du chapitre

- Comprendre l'architecture REST
- Connaitre les contraintes REST
- Appliquer les bonnes pratiques

---

## 1. Qu'est-ce que REST?

### Definition

**REST (REpresentational State Transfer)** est un style d'architecture pour concevoir des APIs web. Il a ete defini par Roy Fielding en 2000.

### API RESTful

Une API est dite RESTful si elle respecte les contraintes REST.

---

## 2. Les 6 contraintes REST

### 2.1 Client-Serveur

Separation des responsabilites.

```
Client                          Serveur
(UI, experience utilisateur)    (Logique metier, donnees)
        |                           |
        |------- Requete HTTP ----->|
        |<------ Reponse JSON ------|
```

### 2.2 Sans etat (Stateless)

Chaque requete contient toutes les informations necessaires. Le serveur ne garde pas de session.

```
// MAUVAIS (avec etat)
1. Login -> Serveur cree une session
2. GET /leads -> Serveur lit la session

// BON (sans etat)
1. Login -> Retourne un token
2. GET /leads + Authorization: Bearer <token>
   Le token contient toutes les infos necessaires
```

### 2.3 Cacheable

Les reponses peuvent etre mises en cache.

```
GET /api/leads/123
Cache-Control: max-age=3600
```

### 2.4 Interface uniforme

Utilisation coherente des methodes HTTP et des URLs.

| Methode | Action | Exemple |
|---------|--------|---------|
| GET | Lire | GET /api/leads |
| POST | Creer | POST /api/leads |
| PUT | Remplacer | PUT /api/leads/123 |
| PATCH | Modifier | PATCH /api/leads/123 |
| DELETE | Supprimer | DELETE /api/leads/123 |

### 2.5 Systeme en couches

Le client ne sait pas s'il communique directement avec le serveur ou via des intermediaires.

```
Client --> Load Balancer --> API Gateway --> Serveur
```

### 2.6 Code a la demande (optionnel)

Le serveur peut envoyer du code executable au client.

---

## 3. Ressources et URIs

### Concepts cles

- **Ressource**: Une entite (lead, user, product)
- **URI**: Identifiant de la ressource (/api/leads/123)
- **Representation**: Format des donnees (JSON, XML)

### Bonnes pratiques pour les URIs

```
// BON: Noms au pluriel
GET /api/leads
GET /api/users

// MAUVAIS: Verbes dans l'URL
GET /api/getLeads
GET /api/fetchUsers

// BON: Hierarchie claire
GET /api/users/123/leads         # Leads de l'utilisateur 123

// BON: Parametres de query pour filtrer
GET /api/leads?status=NEW&page=0&size=10

// MAUVAIS: Action dans l'URL
POST /api/leads/123/activate     # Preferer PATCH /api/leads/123
```

---

## 4. Methodes HTTP

### GET - Lire

```
GET /api/leads              # Liste tous les leads
GET /api/leads/123          # Recupere le lead 123

Reponse: 200 OK
{
  "id": 123,
  "fullName": "Jean Dupont"
}
```

### POST - Creer

```
POST /api/leads
Content-Type: application/json

{
  "fullName": "Jean Dupont",
  "email": "jean@example.com"
}

Reponse: 201 Created
Location: /api/leads/124
{
  "id": 124,
  "fullName": "Jean Dupont"
}
```

### PUT - Remplacer completement

```
PUT /api/leads/123
Content-Type: application/json

{
  "fullName": "Jean Dupont",
  "email": "nouveau@example.com",
  "company": "ACME",
  "phone": "0123456789",
  "requestType": "INFO",
  "message": "Nouveau message"
}

Reponse: 200 OK
```

### PATCH - Modifier partiellement

```
PATCH /api/leads/123
Content-Type: application/json

{
  "status": "CONTACTED"
}

Reponse: 200 OK
```

### DELETE - Supprimer

```
DELETE /api/leads/123

Reponse: 204 No Content
```

---

## 5. Codes de statut HTTP

### Succes (2xx)

| Code | Nom | Usage |
|------|-----|-------|
| 200 | OK | Requete reussie |
| 201 | Created | Ressource creee |
| 204 | No Content | Succes sans corps |

### Redirection (3xx)

| Code | Nom | Usage |
|------|-----|-------|
| 301 | Moved Permanently | Redirection permanente |
| 304 | Not Modified | Ressource non modifiee (cache) |

### Erreurs client (4xx)

| Code | Nom | Usage |
|------|-----|-------|
| 400 | Bad Request | Requete invalide |
| 401 | Unauthorized | Non authentifie |
| 403 | Forbidden | Non autorise |
| 404 | Not Found | Ressource inexistante |
| 409 | Conflict | Conflit (doublon) |
| 422 | Unprocessable Entity | Validation echouee |

### Erreurs serveur (5xx)

| Code | Nom | Usage |
|------|-----|-------|
| 500 | Internal Server Error | Erreur interne |
| 502 | Bad Gateway | Proxy/gateway invalide |
| 503 | Service Unavailable | Service indisponible |

---

## 6. Format des donnees

### JSON (standard)

```json
{
  "id": 123,
  "fullName": "Jean Dupont",
  "email": "jean@example.com",
  "status": "NEW",
  "createdAt": "2024-01-15T10:30:00"
}
```

### Headers

```
Content-Type: application/json    # Format du corps
Accept: application/json          # Format attendu en reponse
```

---

## 7. Pagination

### Parametres de query

```
GET /api/leads?page=0&size=10&sort=createdAt,desc
```

### Reponse paginee

```json
{
  "content": [
    { "id": 1, "fullName": "Lead 1" },
    { "id": 2, "fullName": "Lead 2" }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 100,
  "totalPages": 10,
  "first": true,
  "last": false
}
```

---

## 8. Filtrage et recherche

### Filtres simples

```
GET /api/leads?status=NEW
GET /api/leads?status=NEW&requestType=INFO
```

### Recherche

```
GET /api/leads?search=dupont
GET /api/leads?q=dupont
```

### Tri

```
GET /api/leads?sort=createdAt,desc
GET /api/leads?sort=fullName,asc&sort=createdAt,desc
```

---

## 9. HATEOAS (optionnel)

### Hypermedia As The Engine Of Application State

Les reponses contiennent des liens vers les actions possibles.

```json
{
  "id": 123,
  "fullName": "Jean Dupont",
  "status": "NEW",
  "_links": {
    "self": { "href": "/api/leads/123" },
    "update": { "href": "/api/leads/123", "method": "PUT" },
    "delete": { "href": "/api/leads/123", "method": "DELETE" },
    "updateStatus": { "href": "/api/leads/123/status", "method": "PATCH" }
  }
}
```

---

## 10. Points cles a retenir

1. **REST** = style d'architecture pour APIs web
2. **Stateless** = pas de session serveur
3. **Ressources** identifiees par des URIs
4. **Methodes HTTP** correspondent aux actions CRUD
5. **Codes de statut** informent du resultat

---

## QUIZ 3.1 - Principes REST

**1. Que signifie REST?**
   - a) Remote Execution Service Transfer
   - b) REpresentational State Transfer
   - c) Resource Exchange Standard Technology
   - d) Request-Response State Transfer

**2. Quelle est la caracteristique "stateless"?**
   - a) Le serveur garde une session
   - b) Chaque requete contient toutes les infos necessaires
   - c) Le client garde l'etat
   - d) Les donnees sont en cache

**3. Quelle methode HTTP pour creer une ressource?**
   - a) GET
   - b) PUT
   - c) POST
   - d) CREATE

**4. Quel code HTTP indique une creation reussie?**
   - a) 200 OK
   - b) 201 Created
   - c) 204 No Content
   - d) 202 Accepted

**5. VRAI ou FAUX: Les URLs REST doivent contenir des verbes.**

**6. Quelle est la difference entre PUT et PATCH?**
   - a) Aucune
   - b) PUT remplace, PATCH modifie partiellement
   - c) PATCH remplace, PUT modifie partiellement
   - d) PUT est pour creation, PATCH pour mise a jour

**7. Quel code HTTP pour "non authentifie"?**
   - a) 400
   - b) 401
   - c) 403
   - d) 404

**8. Completez: Une API REST utilise des _______ pour identifier les ressources.**

**9. Quel format de donnees est standard pour les APIs REST?**
   - a) XML
   - b) CSV
   - c) JSON
   - d) HTML

**10. Quel code HTTP pour "ressource non trouvee"?**
   - a) 400
   - b) 401
   - c) 403
   - d) 404

---

### REPONSES QUIZ 3.1

1. b) REpresentational State Transfer
2. b) Chaque requete contient toutes les infos necessaires
3. c) POST
4. b) 201 Created
5. FAUX (noms de ressources au pluriel)
6. b) PUT remplace, PATCH modifie partiellement
7. b) 401
8. URIs
9. c) JSON
10. d) 404

