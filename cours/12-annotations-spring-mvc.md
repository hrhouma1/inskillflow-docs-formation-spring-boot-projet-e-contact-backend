# Chapitre 3.2 - Annotations Spring MVC

## Objectifs du chapitre

- Maitriser les annotations de mapping
- Comprendre les annotations de parametres
- Utiliser les annotations de reponse

---

## 1. Annotations de classe

### @Controller

Pour les controllers MVC classiques (renvoie des vues).

```java
@Controller
public class WebController {
    @GetMapping("/home")
    public String home(Model model) {
        return "home";  // Nom de la vue
    }
}
```

### @RestController

Pour les APIs REST (renvoie du JSON).

```java
@RestController  // = @Controller + @ResponseBody
public class ApiController {
    @GetMapping("/api/data")
    public DataDto getData() {
        return new DataDto();  // Converti en JSON
    }
}
```

### @RequestMapping (niveau classe)

Definit le prefixe d'URL pour tous les endpoints.

```java
@RestController
@RequestMapping("/api/leads")
public class LeadController {
    
    @GetMapping           // GET /api/leads
    @GetMapping("/{id}")  // GET /api/leads/123
    @PostMapping          // POST /api/leads
}
```

---

## 2. Annotations de methode

### @GetMapping

```java
@GetMapping                    // GET /api/leads
@GetMapping("/{id}")           // GET /api/leads/123
@GetMapping("/stats")          // GET /api/leads/stats
@GetMapping(produces = "application/json")  // Specifie le type de reponse
```

### @PostMapping

```java
@PostMapping                   // POST /api/leads
@PostMapping(consumes = "application/json")  // Specifie le type de requete
```

### @PutMapping

```java
@PutMapping("/{id}")           // PUT /api/leads/123
```

### @PatchMapping

```java
@PatchMapping("/{id}")         // PATCH /api/leads/123
```

### @DeleteMapping

```java
@DeleteMapping("/{id}")        // DELETE /api/leads/123
```

### @RequestMapping (niveau methode)

Version generique (peut specifier la methode HTTP).

```java
@RequestMapping(value = "/{id}", method = RequestMethod.GET)
// Equivalent a @GetMapping("/{id}")
```

---

## 3. Annotations de parametres

### @PathVariable

Extrait une variable de l'URL.

```java
// GET /api/leads/123
@GetMapping("/{id}")
public Lead getLead(@PathVariable Long id) {
    return service.findById(id);
}

// Nom explicite
@GetMapping("/{leadId}")
public Lead getLead(@PathVariable("leadId") Long id) {
    return service.findById(id);
}

// Plusieurs variables
@GetMapping("/{userId}/leads/{leadId}")
public Lead getLead(
        @PathVariable Long userId,
        @PathVariable Long leadId) {
    return service.findByUserAndLead(userId, leadId);
}
```

### @RequestParam

Extrait un parametre de query string.

```java
// GET /api/leads?status=NEW
@GetMapping
public List<Lead> getLeads(@RequestParam LeadStatus status) {
    return service.findByStatus(status);
}

// Parametre optionnel
@GetMapping
public List<Lead> getLeads(
        @RequestParam(required = false) LeadStatus status) {
    return status != null ? service.findByStatus(status) : service.findAll();
}

// Valeur par defaut
@GetMapping
public Page<Lead> getLeads(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    return service.findAll(PageRequest.of(page, size));
}

// Nom explicite
@GetMapping
public List<Lead> search(@RequestParam("q") String query) {
    return service.search(query);
}
```

### @RequestBody

Extrait le corps JSON de la requete.

```java
@PostMapping
public Lead create(@RequestBody ContactFormRequest request) {
    return service.create(request);
}

// Avec validation
@PostMapping
public Lead create(@RequestBody @Valid ContactFormRequest request) {
    return service.create(request);
}
```

### @RequestHeader

Extrait un en-tete HTTP.

```java
@GetMapping
public String getData(
        @RequestHeader("Authorization") String token,
        @RequestHeader(value = "X-Custom", required = false) String custom) {
    // ...
}
```

### @CookieValue

Extrait un cookie.

```java
@GetMapping
public String getData(@CookieValue("sessionId") String sessionId) {
    // ...
}
```

---

## 4. Annotations de validation

### @Valid

Active la validation du DTO.

```java
@PostMapping
public Lead create(@RequestBody @Valid ContactFormRequest request) {
    // Si validation echoue, exception MethodArgumentNotValidException
    return service.create(request);
}
```

### @Validated

Version Spring avec support des groupes.

```java
@PostMapping
public Lead create(@RequestBody @Validated(OnCreate.class) Request request) {
    return service.create(request);
}
```

---

## 5. Annotations de reponse

### @ResponseStatus

Definit le code HTTP de la reponse.

```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)  // 201 au lieu de 200
public Lead create(@RequestBody @Valid Request request) {
    return service.create(request);
}

@DeleteMapping("/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)  // 204
public void delete(@PathVariable Long id) {
    service.delete(id);
}
```

### @ResponseBody

Indique que la valeur de retour est le corps de la reponse (inclus dans @RestController).

```java
@Controller
public class MixedController {
    
    @GetMapping("/page")
    public String page() {
        return "page";  // Retourne une vue
    }
    
    @GetMapping("/api/data")
    @ResponseBody  // Retourne du JSON
    public DataDto data() {
        return new DataDto();
    }
}
```

---

## 6. Annotations de securite

### @PreAuthorize

Verifie les permissions avant l'execution.

```java
@GetMapping
@PreAuthorize("hasRole('ADMIN')")
public List<Lead> getLeads() {
    return service.findAll();
}

@DeleteMapping("/{id}")
@PreAuthorize("hasRole('ADMIN') and #id != principal.id")
public void delete(@PathVariable Long id) {
    service.delete(id);
}
```

### @Secured

Version simplifiee.

```java
@GetMapping
@Secured("ROLE_ADMIN")
public List<Lead> getLeads() {
    return service.findAll();
}
```

---

## 7. Autres annotations utiles

### @CrossOrigin

Configure CORS pour un controller ou une methode.

```java
@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class ApiController {
    // ...
}

// Ou par methode
@GetMapping
@CrossOrigin(origins = "*", maxAge = 3600)
public List<Lead> getLeads() {
    return service.findAll();
}
```

### @ExceptionHandler

Gere les exceptions dans un controller.

```java
@RestController
public class LeadController {
    
    @GetMapping("/{id}")
    public Lead getLead(@PathVariable Long id) {
        return service.findById(id);  // Peut lever ResourceNotFoundException
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ResourceNotFoundException ex) {
        return new ErrorResponse(ex.getMessage());
    }
}
```

---

## 8. Resume des annotations

| Annotation | Niveau | Usage |
|------------|--------|-------|
| @RestController | Classe | Controller REST |
| @RequestMapping | Classe/Methode | Prefixe URL |
| @GetMapping | Methode | GET |
| @PostMapping | Methode | POST |
| @PutMapping | Methode | PUT |
| @PatchMapping | Methode | PATCH |
| @DeleteMapping | Methode | DELETE |
| @PathVariable | Parametre | Variable URL |
| @RequestParam | Parametre | Query string |
| @RequestBody | Parametre | Corps JSON |
| @RequestHeader | Parametre | En-tete HTTP |
| @Valid | Parametre | Validation |
| @ResponseStatus | Methode | Code HTTP |
| @PreAuthorize | Methode | Securite |
| @CrossOrigin | Classe/Methode | CORS |

---

## 9. Points cles a retenir

1. **@RestController** = @Controller + @ResponseBody
2. **@RequestMapping** definit le prefixe d'URL
3. **@PathVariable** pour les variables dans l'URL
4. **@RequestParam** pour les parametres de query
5. **@RequestBody** pour le corps JSON
6. **@Valid** active la validation

---

## QUIZ 3.2 - Annotations Spring MVC

**1. Quelle annotation combine @Controller et @ResponseBody?**
   - a) @ApiController
   - b) @RestController
   - c) @WebController
   - d) @JsonController

**2. Quelle annotation extrait une variable de l'URL /api/leads/123?**
   - a) @RequestParam
   - b) @PathVariable
   - c) @RequestBody
   - d) @UrlVariable

**3. Quelle annotation extrait le corps JSON?**
   - a) @RequestParam
   - b) @PathVariable
   - c) @RequestBody
   - d) @JsonBody

**4. Comment rendre un parametre optionnel avec @RequestParam?**
   - a) @RequestParam(optional = true)
   - b) @RequestParam(required = false)
   - c) @RequestParam(nullable = true)
   - d) @Optional @RequestParam

**5. VRAI ou FAUX: @GetMapping est equivalent a @RequestMapping(method = GET).**

**6. Quelle annotation active la validation du DTO?**
   - a) @Validate
   - b) @Valid
   - c) @Check
   - d) @Verified

**7. Comment specifier un code 201 pour une creation?**
   - a) @ResponseCode(201)
   - b) @ResponseStatus(HttpStatus.CREATED)
   - c) @HttpStatus(201)
   - d) @Status(CREATED)

**8. Completez: @RequestParam extrait les parametres de la _______ string.**

**9. Quelle annotation pour securiser une methode?**
   - a) @Secure
   - b) @PreAuthorize
   - c) @Protected
   - d) @Auth

**10. Comment donner une valeur par defaut a un @RequestParam?**
   - a) @RequestParam = "default"
   - b) @RequestParam(default = "value")
   - c) @RequestParam(defaultValue = "value")
   - d) @DefaultValue("value") @RequestParam

---

### REPONSES QUIZ 3.2

1. b) @RestController
2. b) @PathVariable
3. c) @RequestBody
4. b) @RequestParam(required = false)
5. VRAI
6. b) @Valid
7. b) @ResponseStatus(HttpStatus.CREATED)
8. query
9. b) @PreAuthorize
10. c) @RequestParam(defaultValue = "value")

