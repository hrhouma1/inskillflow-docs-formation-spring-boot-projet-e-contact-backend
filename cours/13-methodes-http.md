# Chapitre 3.3 - Methodes HTTP (GET, POST, PUT, DELETE)

## Objectifs du chapitre

- Maitriser les methodes HTTP
- Savoir quand utiliser chaque methode
- Implementer les operations CRUD

---

## 1. Vue d'ensemble

### Correspondance CRUD - HTTP

| CRUD | HTTP | Description |
|------|------|-------------|
| Create | POST | Creer une ressource |
| Read | GET | Lire une ressource |
| Update | PUT/PATCH | Modifier une ressource |
| Delete | DELETE | Supprimer une ressource |

---

## 2. GET - Lire

### Caracteristiques

- **Idempotent**: Plusieurs appels donnent le meme resultat
- **Safe**: Ne modifie pas les donnees
- **Cacheable**: Peut etre mis en cache
- **Pas de corps**: Les donnees sont dans l'URL

### Exemples

```java
// Liste tous les leads
// GET /api/leads
@GetMapping
public List<LeadDto> getAllLeads() {
    return service.findAll();
}

// Liste avec pagination
// GET /api/leads?page=0&size=10
@GetMapping
public Page<LeadDto> getAllLeads(Pageable pageable) {
    return service.findAll(pageable);
}

// Recupere un lead par ID
// GET /api/leads/123
@GetMapping("/{id}")
public ResponseEntity<LeadDto> getLeadById(@PathVariable Long id) {
    return ResponseEntity.ok(service.findById(id));
}

// Filtre par statut
// GET /api/leads?status=NEW
@GetMapping
public List<LeadDto> getLeadsByStatus(@RequestParam LeadStatus status) {
    return service.findByStatus(status);
}

// Recherche
// GET /api/leads/search?q=dupont
@GetMapping("/search")
public List<LeadDto> search(@RequestParam("q") String query) {
    return service.search(query);
}
```

### Reponses typiques

```
200 OK              - Succes
404 Not Found       - Ressource inexistante
```

---

## 3. POST - Creer

### Caracteristiques

- **Non idempotent**: Chaque appel cree une nouvelle ressource
- **Pas safe**: Modifie les donnees
- **Corps**: Contient les donnees a creer

### Exemples

```java
// Cree un nouveau lead
// POST /api/leads
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public LeadDto createLead(@RequestBody @Valid ContactFormRequest request) {
    return service.create(request);
}

// Avec ResponseEntity pour plus de controle
@PostMapping
public ResponseEntity<LeadDto> createLead(@RequestBody @Valid ContactFormRequest request) {
    LeadDto created = service.create(request);
    URI location = URI.create("/api/leads/" + created.getId());
    return ResponseEntity.created(location).body(created);
}
```

### Requete HTTP

```
POST /api/leads HTTP/1.1
Content-Type: application/json

{
  "fullName": "Jean Dupont",
  "email": "jean@example.com",
  "requestType": "INFO",
  "message": "Je voudrais des informations"
}
```

### Reponses typiques

```
201 Created         - Ressource creee
400 Bad Request     - Donnees invalides
409 Conflict        - Doublon (email existe deja)
```

---

## 4. PUT - Remplacer

### Caracteristiques

- **Idempotent**: Plusieurs appels donnent le meme resultat
- **Remplace completement**: Tous les champs doivent etre fournis
- **Corps**: Contient la ressource complete

### Exemples

```java
// Remplace completement un lead
// PUT /api/leads/123
@PutMapping("/{id}")
public ResponseEntity<LeadDto> updateLead(
        @PathVariable Long id,
        @RequestBody @Valid LeadUpdateRequest request) {
    LeadDto updated = service.replace(id, request);
    return ResponseEntity.ok(updated);
}
```

### Requete HTTP

```
PUT /api/leads/123 HTTP/1.1
Content-Type: application/json

{
  "fullName": "Jean Dupont",
  "email": "nouveau@example.com",
  "company": "ACME Corp",
  "phone": "0123456789",
  "requestType": "DEMO",
  "message": "Mise a jour complete"
}
```

### PUT vs PATCH

| Aspect | PUT | PATCH |
|--------|-----|-------|
| Donnees | Ressource complete | Champs a modifier |
| Champs non fournis | Remis a null | Non modifies |
| Idempotent | Oui | Oui |

---

## 5. PATCH - Modifier partiellement

### Caracteristiques

- **Idempotent**: Plusieurs appels donnent le meme resultat
- **Modifie partiellement**: Seuls les champs fournis sont modifies
- **Corps**: Contient seulement les champs a modifier

### Exemples

```java
// Modifie le statut d'un lead
// PATCH /api/leads/123
@PatchMapping("/{id}")
public ResponseEntity<LeadDto> updateStatus(
        @PathVariable Long id,
        @RequestBody UpdateStatusRequest request) {
    LeadDto updated = service.updateStatus(id, request);
    return ResponseEntity.ok(updated);
}

// Modification partielle generique
@PatchMapping("/{id}")
public ResponseEntity<LeadDto> partialUpdate(
        @PathVariable Long id,
        @RequestBody Map<String, Object> updates) {
    LeadDto updated = service.partialUpdate(id, updates);
    return ResponseEntity.ok(updated);
}
```

### Requete HTTP

```
PATCH /api/leads/123 HTTP/1.1
Content-Type: application/json

{
  "status": "CONTACTED"
}
```

---

## 6. DELETE - Supprimer

### Caracteristiques

- **Idempotent**: Supprimer deux fois = supprimer une fois
- **Pas de corps**: L'ID est dans l'URL
- **Pas de reponse**: Souvent 204 No Content

### Exemples

```java
// Supprime un lead
// DELETE /api/leads/123
@DeleteMapping("/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void deleteLead(@PathVariable Long id) {
    service.delete(id);
}

// Avec verification et reponse
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteLead(@PathVariable Long id) {
    if (!service.exists(id)) {
        return ResponseEntity.notFound().build();
    }
    service.delete(id);
    return ResponseEntity.noContent().build();
}

// Suppression logique (soft delete)
@DeleteMapping("/{id}")
public ResponseEntity<LeadDto> softDelete(@PathVariable Long id) {
    LeadDto deleted = service.softDelete(id);
    return ResponseEntity.ok(deleted);
}
```

### Reponses typiques

```
204 No Content      - Suppression reussie
404 Not Found       - Ressource inexistante
```

---

## 7. Exemple complet: LeadController

```java
@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService service;

    // CREATE
    @PostMapping
    public ResponseEntity<LeadDto> create(@RequestBody @Valid ContactFormRequest request) {
        LeadDto created = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/leads/" + created.getId()))
                .body(created);
    }

    // READ (list)
    @GetMapping
    public Page<LeadDto> getAll(Pageable pageable) {
        return service.findAll(pageable);
    }

    // READ (single)
    @GetMapping("/{id}")
    public ResponseEntity<LeadDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    // UPDATE (replace)
    @PutMapping("/{id}")
    public ResponseEntity<LeadDto> replace(
            @PathVariable Long id,
            @RequestBody @Valid LeadUpdateRequest request) {
        return ResponseEntity.ok(service.replace(id, request));
    }

    // UPDATE (partial)
    @PatchMapping("/{id}/status")
    public ResponseEntity<LeadDto> updateStatus(
            @PathVariable Long id,
            @RequestBody @Valid UpdateStatusRequest request) {
        return ResponseEntity.ok(service.updateStatus(id, request));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 8. Idempotence

### Definition

Une operation est **idempotente** si l'executer plusieurs fois produit le meme resultat qu'une seule execution.

### Tableau recapitulatif

| Methode | Idempotent | Safe | Cacheable |
|---------|------------|------|-----------|
| GET | Oui | Oui | Oui |
| POST | Non | Non | Non |
| PUT | Oui | Non | Non |
| PATCH | Oui | Non | Non |
| DELETE | Oui | Non | Non |

### Exemples

```
// GET est idempotent et safe
GET /api/leads/123    # Renvoie le lead
GET /api/leads/123    # Renvoie le meme lead

// POST n'est pas idempotent
POST /api/leads       # Cree lead ID 1
POST /api/leads       # Cree lead ID 2 (nouveau!)

// PUT est idempotent
PUT /api/leads/123    # Remplace le lead
PUT /api/leads/123    # Meme resultat

// DELETE est idempotent
DELETE /api/leads/123 # Supprime le lead
DELETE /api/leads/123 # Deja supprime, meme resultat
```

---

## 9. Points cles a retenir

1. **GET** = Lire (idempotent, safe)
2. **POST** = Creer (non idempotent)
3. **PUT** = Remplacer completement (idempotent)
4. **PATCH** = Modifier partiellement (idempotent)
5. **DELETE** = Supprimer (idempotent)

---

## QUIZ 3.3 - Methodes HTTP

**1. Quelle methode HTTP pour creer une ressource?**
   - a) GET
   - b) POST
   - c) PUT
   - d) CREATE

**2. Quelle est la difference entre PUT et PATCH?**
   - a) Aucune
   - b) PUT remplace, PATCH modifie partiellement
   - c) PATCH remplace, PUT modifie
   - d) PUT est pour creation

**3. Quelle methode est idempotente?**
   - a) POST seulement
   - b) GET, PUT, PATCH, DELETE
   - c) GET seulement
   - d) Aucune

**4. Quel code HTTP retourner apres DELETE?**
   - a) 200 OK
   - b) 201 Created
   - c) 204 No Content
   - d) 202 Accepted

**5. VRAI ou FAUX: GET peut avoir un corps de requete.**

**6. Quelle methode est "safe"?**
   - a) POST
   - b) PUT
   - c) GET
   - d) DELETE

**7. POST est-il idempotent?**
   - a) Oui
   - b) Non
   - c) Parfois
   - d) Ca depend

**8. Completez: PUT est _______ car plusieurs appels donnent le meme resultat.**

**9. Quel code HTTP pour une creation reussie avec POST?**
   - a) 200 OK
   - b) 201 Created
   - c) 204 No Content
   - d) 202 Accepted

**10. Si un champ n'est pas fourni dans un PUT, que se passe-t-il?**
   - a) Erreur
   - b) Le champ est ignore
   - c) Le champ est mis a null
   - d) Le champ garde sa valeur

---

### REPONSES QUIZ 3.3

1. b) POST
2. b) PUT remplace, PATCH modifie partiellement
3. b) GET, PUT, PATCH, DELETE
4. c) 204 No Content
5. FAUX (techniquement possible mais deconseille)
6. c) GET
7. b) Non
8. idempotent
9. b) 201 Created
10. c) Le champ est mis a null

