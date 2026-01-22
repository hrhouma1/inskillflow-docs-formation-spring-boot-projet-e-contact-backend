# Chapitre 3.4 - Codes de reponse HTTP

## Objectifs du chapitre

- Connaitre les codes de statut HTTP
- Savoir quel code utiliser dans chaque situation
- Implementer les reponses appropriees

---

## 1. Categories de codes

| Plage | Categorie | Description |
|-------|-----------|-------------|
| 1xx | Information | Requete recue, traitement en cours |
| 2xx | Succes | Requete reussie |
| 3xx | Redirection | Action supplementaire requise |
| 4xx | Erreur client | Erreur dans la requete |
| 5xx | Erreur serveur | Erreur cote serveur |

---

## 2. Codes de succes (2xx)

### 200 OK

La requete a reussi. Utilise pour GET, PUT, PATCH.

```java
@GetMapping("/{id}")
public ResponseEntity<LeadDto> getLead(@PathVariable Long id) {
    LeadDto lead = service.findById(id);
    return ResponseEntity.ok(lead);  // 200
}
```

### 201 Created

Une ressource a ete creee. Utilise pour POST.

```java
@PostMapping
public ResponseEntity<LeadDto> create(@RequestBody @Valid Request request) {
    LeadDto created = service.create(request);
    URI location = URI.create("/api/leads/" + created.getId());
    return ResponseEntity.created(location).body(created);  // 201
}
```

### 204 No Content

Succes sans contenu a retourner. Utilise pour DELETE.

```java
@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();  // 204
}
```

### 202 Accepted

Requete acceptee, traitement en cours (asynchrone).

```java
@PostMapping("/export")
public ResponseEntity<Void> exportLeads() {
    service.startExportAsync();  // Traitement en arriere-plan
    return ResponseEntity.accepted().build();  // 202
}
```

---

## 3. Codes d'erreur client (4xx)

### 400 Bad Request

Requete mal formee ou donnees invalides.

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    return ResponseEntity.badRequest().body(
        new ErrorResponse("Donnees invalides")
    );  // 400
}
```

### 401 Unauthorized

Non authentifie (identite non prouvee).

```java
// Gere automatiquement par Spring Security
// Retourne 401 si pas de token ou token invalide
```

### 403 Forbidden

Authentifie mais non autorise (permissions insuffisantes).

```java
@ExceptionHandler(AccessDeniedException.class)
public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
        new ErrorResponse("Acces refuse")
    );  // 403
}
```

### 404 Not Found

Ressource inexistante.

```java
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
    return ResponseEntity.notFound().build();  // 404
}

// Ou avec corps
return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
    new ErrorResponse("Lead non trouve")
);
```

### 405 Method Not Allowed

Methode HTTP non supportee pour cette ressource.

```java
// Si GET /api/leads/123 existe mais POST /api/leads/123 non
// Spring retourne automatiquement 405
```

### 409 Conflict

Conflit avec l'etat actuel (doublon, version obsolete).

```java
@ExceptionHandler(DuplicateEmailException.class)
public ResponseEntity<ErrorResponse> handleConflict(DuplicateEmailException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(
        new ErrorResponse("Un lead avec cet email existe deja")
    );  // 409
}
```

### 422 Unprocessable Entity

Donnees syntaxiquement correctes mais semantiquement invalides.

```java
@ExceptionHandler(BusinessException.class)
public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
    return ResponseEntity.unprocessableEntity().body(
        new ErrorResponse(ex.getMessage())
    );  // 422
}
```

---

## 4. Codes d'erreur serveur (5xx)

### 500 Internal Server Error

Erreur interne du serveur.

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
    log.error("Erreur interne", ex);
    return ResponseEntity.internalServerError().body(
        new ErrorResponse("Erreur interne du serveur")
    );  // 500
}
```

### 502 Bad Gateway

Le serveur agit comme proxy et a recu une reponse invalide.

### 503 Service Unavailable

Service temporairement indisponible.

```java
@ExceptionHandler(ServiceUnavailableException.class)
public ResponseEntity<ErrorResponse> handleUnavailable(ServiceUnavailableException ex) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
        new ErrorResponse("Service temporairement indisponible")
    );  // 503
}
```

---

## 5. Tableau recapitulatif

| Code | Nom | Quand l'utiliser |
|------|-----|------------------|
| 200 | OK | GET, PUT, PATCH reussi |
| 201 | Created | POST reussi |
| 204 | No Content | DELETE reussi |
| 400 | Bad Request | Validation echouee |
| 401 | Unauthorized | Non authentifie |
| 403 | Forbidden | Pas les permissions |
| 404 | Not Found | Ressource inexistante |
| 409 | Conflict | Doublon |
| 422 | Unprocessable Entity | Erreur metier |
| 500 | Internal Server Error | Bug serveur |

---

## 6. ResponseEntity en detail

### Methodes statiques

```java
// 200 OK avec corps
ResponseEntity.ok(body);
ResponseEntity.ok().body(body);

// 201 Created avec location
ResponseEntity.created(URI.create("/api/leads/1")).body(body);

// 204 No Content
ResponseEntity.noContent().build();

// 400 Bad Request
ResponseEntity.badRequest().body(error);

// 404 Not Found
ResponseEntity.notFound().build();

// 422 Unprocessable Entity
ResponseEntity.unprocessableEntity().body(error);

// 500 Internal Server Error
ResponseEntity.internalServerError().body(error);

// Code personnalise
ResponseEntity.status(HttpStatus.CONFLICT).body(error);
```

### Builder complet

```java
ResponseEntity
    .status(HttpStatus.CREATED)
    .header("X-Custom-Header", "value")
    .contentType(MediaType.APPLICATION_JSON)
    .body(lead);
```

---

## 7. Reponse d'erreur standardisee

### ErrorResponse.java

```java
@Data
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    
    public ErrorResponse(int status, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = HttpStatus.valueOf(status).getReasonPhrase();
        this.message = message;
        this.path = path;
    }
}
```

### Reponse JSON

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Lead non trouve avec l'ID 123",
  "path": "/api/leads/123"
}
```

---

## 8. GlobalExceptionHandler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
            404, ex.getMessage(), request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        ErrorResponse error = new ErrorResponse(
            400, message, request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
            500, "Erreur interne", request.getRequestURI()
        );
        return ResponseEntity.internalServerError().body(error);
    }
}
```

---

## 9. Points cles a retenir

1. **2xx** = Succes (200, 201, 204)
2. **4xx** = Erreur client (400, 401, 403, 404)
3. **5xx** = Erreur serveur (500, 503)
4. **ResponseEntity** pour controler le code et le corps
5. **Standardiser** les reponses d'erreur

---

## QUIZ 3.4 - Codes de reponse HTTP

**1. Quel code pour une creation reussie?**
   - a) 200 OK
   - b) 201 Created
   - c) 204 No Content
   - d) 202 Accepted

**2. Quelle est la difference entre 401 et 403?**
   - a) Aucune
   - b) 401 = non authentifie, 403 = non autorise
   - c) 401 = non autorise, 403 = non authentifie
   - d) 401 = client, 403 = serveur

**3. Quel code pour une ressource inexistante?**
   - a) 400
   - b) 401
   - c) 403
   - d) 404

**4. Quel code apres un DELETE reussi?**
   - a) 200 OK
   - b) 201 Created
   - c) 204 No Content
   - d) 202 Accepted

**5. VRAI ou FAUX: 500 indique une erreur dans la requete client.**

**6. Quel code pour des donnees invalides?**
   - a) 400 Bad Request
   - b) 401 Unauthorized
   - c) 404 Not Found
   - d) 500 Internal Server Error

**7. La plage 4xx indique quoi?**
   - a) Succes
   - b) Redirection
   - c) Erreur client
   - d) Erreur serveur

**8. Completez: ResponseEntity.ok() retourne un code _______.**

**9. Quel code pour un doublon (email existe deja)?**
   - a) 400
   - b) 404
   - c) 409
   - d) 422

**10. Quel code si le service est temporairement indisponible?**
   - a) 500
   - b) 502
   - c) 503
   - d) 504

---

### REPONSES QUIZ 3.4

1. b) 201 Created
2. b) 401 = non authentifie, 403 = non autorise
3. d) 404
4. c) 204 No Content
5. FAUX (500 = erreur serveur)
6. a) 400 Bad Request
7. c) Erreur client
8. 200
9. c) 409
10. c) 503

