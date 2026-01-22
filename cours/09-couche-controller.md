# Chapitre 2.5 - Couche Controller (API REST)

## Objectifs du chapitre

- Creer des controllers REST
- Gerer les requetes et reponses HTTP
- Utiliser les annotations Spring MVC

---

## 1. Role du Controller

### Responsabilites

1. **Recevoir** les requetes HTTP
2. **Valider** les donnees d'entree
3. **Deleguer** au service
4. **Retourner** la reponse HTTP appropriee

### Ce que le Controller NE fait PAS

- Logique metier (role du Service)
- Acces direct a la base de donnees

---

## 2. @RestController vs @Controller

### @Controller

Renvoie des vues (HTML avec Thymeleaf, JSP...).

```java
@Controller
public class WebController {
    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("message", "Bonjour");
        return "home";  // Retourne le nom de la vue
    }
}
```

### @RestController

Renvoie directement des donnees (JSON, XML...).

```java
@RestController  // = @Controller + @ResponseBody
public class ApiController {
    @GetMapping("/api/data")
    public DataDto getData() {
        return new DataDto();  // Converti automatiquement en JSON
    }
}
```

---

## 3. Structure d'un Controller

### Anatomie

```java
@RestController                          // 1. Type de controller
@RequestMapping("/api/contact")          // 2. Prefixe d'URL
@RequiredArgsConstructor                 // 3. Injection
public class ContactController {

    private final LeadService leadService;  // 4. Dependance

    @PostMapping                         // 5. Methode HTTP + chemin
    public ResponseEntity<LeadDto> submit(
            @RequestBody @Valid ContactFormRequest request) {  // 6. Corps de requete
        
        LeadDto lead = leadService.createLead(request);
        return ResponseEntity.ok(lead);  // 7. Reponse
    }
}
```

---

## 4. ContactController

```java
package com.example.contact.controller;

import com.example.contact.dto.request.ContactFormRequest;
import com.example.contact.dto.response.LeadDto;
import com.example.contact.dto.response.MessageResponse;
import com.example.contact.service.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final LeadService leadService;

    @PostMapping
    public ResponseEntity<MessageResponse> submitContact(
            @RequestBody @Valid ContactFormRequest request) {
        
        LeadDto lead = leadService.createLead(request);
        
        return ResponseEntity.ok(
            new MessageResponse("Votre demande a ete envoyee avec succes!")
        );
    }
}
```

---

## 5. LeadController (Admin)

```java
package com.example.contact.controller;

import com.example.contact.dto.request.UpdateStatusRequest;
import com.example.contact.dto.response.LeadDto;
import com.example.contact.dto.response.LeadStatsDto;
import com.example.contact.dto.response.MessageResponse;
import com.example.contact.service.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/leads")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")  // Securite
public class LeadController {

    private final LeadService leadService;

    // GET /api/admin/leads?page=0&size=10
    @GetMapping
    public Page<LeadDto> getAllLeads(Pageable pageable) {
        return leadService.getAllLeads(pageable);
    }

    // GET /api/admin/leads/123
    @GetMapping("/{id}")
    public ResponseEntity<LeadDto> getLeadById(@PathVariable Long id) {
        LeadDto lead = leadService.getLeadById(id);
        return ResponseEntity.ok(lead);
    }

    // PUT /api/admin/leads/123/status
    @PutMapping("/{id}/status")
    public ResponseEntity<LeadDto> updateStatus(
            @PathVariable Long id,
            @RequestBody @Valid UpdateStatusRequest request) {
        
        LeadDto updated = leadService.updateStatus(id, request);
        return ResponseEntity.ok(updated);
    }

    // DELETE /api/admin/leads/123
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteLead(@PathVariable Long id) {
        leadService.deleteLead(id);
        return ResponseEntity.ok(new MessageResponse("Lead supprime"));
    }

    // GET /api/admin/leads/stats
    @GetMapping("/stats")
    public ResponseEntity<LeadStatsDto> getStats() {
        return ResponseEntity.ok(leadService.getStats());
    }
}
```

---

## 6. AuthController

```java
package com.example.contact.controller;

import com.example.contact.dto.request.LoginRequest;
import com.example.contact.dto.response.AuthResponse;
import com.example.contact.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody @Valid LoginRequest request) {
        
        // 1. Authentifier l'utilisateur
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );
        
        // 2. Generer le token JWT
        UserDetails user = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(user);
        
        // 3. Retourner la reponse
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
```

---

## 7. Annotations de mapping

### Methodes HTTP

| Annotation | Methode HTTP | Usage |
|------------|--------------|-------|
| @GetMapping | GET | Lire des donnees |
| @PostMapping | POST | Creer une ressource |
| @PutMapping | PUT | Mettre a jour (complet) |
| @PatchMapping | PATCH | Mettre a jour (partiel) |
| @DeleteMapping | DELETE | Supprimer |

### Chemins

```java
@RequestMapping("/api/leads")  // Prefixe pour toute la classe
public class LeadController {

    @GetMapping                  // GET /api/leads
    @GetMapping("/{id}")         // GET /api/leads/123
    @GetMapping("/stats")        // GET /api/leads/stats
    @PostMapping                 // POST /api/leads
    @PutMapping("/{id}")         // PUT /api/leads/123
    @DeleteMapping("/{id}")      // DELETE /api/leads/123
}
```

---

## 8. Parametres de requete

### @PathVariable - Variable dans l'URL

```java
// GET /api/leads/123
@GetMapping("/{id}")
public Lead getLead(@PathVariable Long id) {
    return service.findById(id);
}

// Avec nom explicite
@GetMapping("/{leadId}")
public Lead getLead(@PathVariable("leadId") Long id) {
    return service.findById(id);
}
```

### @RequestParam - Parametre de query string

```java
// GET /api/leads?status=NEW
@GetMapping
public List<Lead> getLeads(@RequestParam LeadStatus status) {
    return service.findByStatus(status);
}

// Parametre optionnel avec valeur par defaut
// GET /api/leads?page=0&size=10
@GetMapping
public Page<Lead> getLeads(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    return service.findAll(PageRequest.of(page, size));
}
```

### @RequestBody - Corps de la requete

```java
// POST /api/leads avec JSON dans le body
@PostMapping
public Lead create(@RequestBody @Valid ContactFormRequest request) {
    return service.create(request);
}
```

### @RequestHeader - En-tete HTTP

```java
@GetMapping
public String getData(@RequestHeader("Authorization") String token) {
    // ...
}
```

---

## 9. ResponseEntity

### Controler la reponse HTTP

```java
// 200 OK avec corps
return ResponseEntity.ok(data);

// 201 Created avec location
return ResponseEntity
    .created(URI.create("/api/leads/" + lead.getId()))
    .body(lead);

// 204 No Content
return ResponseEntity.noContent().build();

// 400 Bad Request
return ResponseEntity.badRequest().body(error);

// 404 Not Found
return ResponseEntity.notFound().build();

// Status personnalise
return ResponseEntity.status(HttpStatus.CREATED).body(data);
```

### Structure

```java
ResponseEntity<T>
    .status(HttpStatus)    // Code de statut
    .header("X-Custom", "value")  // En-tetes
    .body(data)            // Corps de la reponse
```

---

## 10. Validation

### Activer la validation

```java
@PostMapping
public ResponseEntity<?> create(
        @RequestBody @Valid ContactFormRequest request) {  // @Valid active la validation
    // ...
}
```

### Dans le DTO

```java
public class ContactFormRequest {
    
    @NotBlank(message = "Le nom est obligatoire")
    private String fullName;
    
    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;
    
    @NotNull(message = "Le type de demande est obligatoire")
    private RequestType requestType;
    
    @NotBlank(message = "Le message est obligatoire")
    @Size(min = 10, message = "Le message doit faire au moins 10 caracteres")
    private String message;
}
```

---

## 11. Bonnes pratiques

### 11.1 Controllers minces

```java
// BON: Delegue au service
@PostMapping
public ResponseEntity<?> create(@RequestBody @Valid Request request) {
    return ResponseEntity.ok(service.create(request));
}

// MAUVAIS: Logique dans le controller
@PostMapping
public ResponseEntity<?> create(@RequestBody Request request) {
    // Validation, calculs, acces base... NON!
}
```

### 11.2 Nommage RESTful

```java
// BON
GET    /api/leads          // Liste
GET    /api/leads/123      // Detail
POST   /api/leads          // Creer
PUT    /api/leads/123      // Modifier
DELETE /api/leads/123      // Supprimer

// MAUVAIS
GET    /api/getLeads
POST   /api/createLead
POST   /api/deleteLead/123
```

### 11.3 Codes de statut appropries

| Action | Code | Signification |
|--------|------|---------------|
| GET reussi | 200 | OK |
| POST reussi | 201 | Created |
| PUT/PATCH reussi | 200 | OK |
| DELETE reussi | 204 | No Content |
| Validation echouee | 400 | Bad Request |
| Non authentifie | 401 | Unauthorized |
| Non autorise | 403 | Forbidden |
| Non trouve | 404 | Not Found |

---

## 12. Points cles a retenir

1. **@RestController** pour les APIs REST (JSON)
2. **@RequestMapping** definit le prefixe d'URL
3. **@GetMapping, @PostMapping...** pour les methodes HTTP
4. **@PathVariable, @RequestParam, @RequestBody** pour les parametres
5. **ResponseEntity** pour controler la reponse HTTP

---

## QUIZ 2.5 - Couche Controller

**1. Quelle annotation pour un controller REST?**
   - a) @Controller
   - b) @RestController
   - c) @ApiController
   - d) @WebController

**2. Que fait @RequestBody?**
   - a) Lit les parametres d'URL
   - b) Lit le corps JSON de la requete
   - c) Lit les en-tetes
   - d) Lit les cookies

**3. Comment recuperer l'ID dans GET /api/leads/123?**
   - a) @RequestParam Long id
   - b) @PathVariable Long id
   - c) @RequestBody Long id
   - d) @QueryParam Long id

**4. Quel code HTTP pour une creation reussie?**
   - a) 200 OK
   - b) 201 Created
   - c) 204 No Content
   - d) 202 Accepted

**5. VRAI ou FAUX: Un controller peut appeler directement un repository.**

**6. Quelle annotation active la validation des DTOs?**
   - a) @Validate
   - b) @Valid
   - c) @Validated
   - d) @Check

**7. Comment retourner un 404 avec ResponseEntity?**
   - a) ResponseEntity.notFound().build()
   - b) ResponseEntity.error(404)
   - c) ResponseEntity.status(404)
   - d) throw new NotFoundException()

**8. Completez: @RequestParam lit les parametres de la _______ string.**

**9. Quelle methode HTTP pour modifier une ressource?**
   - a) POST
   - b) GET
   - c) PUT
   - d) PATCH
   - e) c et d

**10. Quel code HTTP si la validation echoue?**
   - a) 401
   - b) 403
   - c) 400
   - d) 500

---

### REPONSES QUIZ 2.5

1. b) @RestController
2. b) Lit le corps JSON de la requete
3. b) @PathVariable Long id
4. b) 201 Created
5. VRAI (mais c'est une mauvaise pratique)
6. b) @Valid
7. a) ResponseEntity.notFound().build()
8. query
9. e) c et d (PUT ou PATCH)
10. c) 400

