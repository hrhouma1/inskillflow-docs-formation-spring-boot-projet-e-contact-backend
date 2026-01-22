# Chapitre 10.1 - OpenAPI et Swagger

## Objectifs du chapitre

- Comprendre OpenAPI et Swagger
- Configurer springdoc-openapi
- Documenter l'API automatiquement

---

## 1. OpenAPI vs Swagger

### OpenAPI

**OpenAPI** (anciennement Swagger Specification) est une specification standard pour decrire les APIs REST.

### Swagger

**Swagger** est un ensemble d'outils pour implementer OpenAPI:
- **Swagger UI**: Interface web interactive
- **Swagger Editor**: Editeur de specification
- **Swagger Codegen**: Generation de code

---

## 2. springdoc-openapi

### Dependance

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

### URLs par defaut

| URL | Description |
|-----|-------------|
| /swagger-ui.html | Interface Swagger UI |
| /v3/api-docs | Specification JSON |
| /v3/api-docs.yaml | Specification YAML |

---

## 3. Configuration

### OpenApiConfig.java

```java
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Contact Form API")
                .version("1.0.0")
                .description("API REST pour gerer les formulaires de contact et les leads")
                .contact(new Contact()
                    .name("Support")
                    .email("support@example.com")
                    .url("https://example.com"))
                .license(new License()
                    .name("MIT")
                    .url("https://opensource.org/licenses/MIT")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Entrez votre token JWT")));
    }
}
```

### application.yml

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: method
    tagsSorter: alpha
    tryItOutEnabled: true
```

---

## 4. Annotations de base

### Sur le controller

```java
@RestController
@RequestMapping("/api/leads")
@Tag(name = "Leads", description = "Gestion des leads")
public class LeadController {
    // ...
}
```

### Sur les methodes

```java
@GetMapping
@Operation(
    summary = "Liste des leads",
    description = "Retourne tous les leads avec pagination"
)
public Page<LeadDto> getAllLeads(Pageable pageable) {
    return service.findAll(pageable);
}

@GetMapping("/{id}")
@Operation(summary = "Recuperer un lead par ID")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Lead trouve"),
    @ApiResponse(responseCode = "404", description = "Lead non trouve")
})
public LeadDto getById(@PathVariable Long id) {
    return service.findById(id);
}
```

### Sur les parametres

```java
@GetMapping
public Page<LeadDto> search(
    @Parameter(description = "Terme de recherche", example = "dupont")
    @RequestParam(required = false) String query,
    
    @Parameter(description = "Statut du lead")
    @RequestParam(required = false) LeadStatus status
) {
    return service.search(query, status);
}
```

---

## 5. Documentation des DTOs

### Schema sur le DTO

```java
@Data
@Schema(description = "Requete de formulaire de contact")
public class ContactFormRequest {
    
    @Schema(description = "Nom complet", example = "Jean Dupont", required = true)
    @NotBlank
    private String fullName;
    
    @Schema(description = "Adresse email", example = "jean@example.com", required = true)
    @Email
    private String email;
    
    @Schema(description = "Nom de l'entreprise", example = "ACME Corp")
    private String company;
    
    @Schema(description = "Type de demande", example = "INFO", required = true)
    @NotNull
    private RequestType requestType;
    
    @Schema(description = "Message", example = "Je souhaite des informations...", minLength = 10)
    @NotBlank
    private String message;
}
```

### Exemple de reponse

```java
@Data
@Schema(description = "Lead retourne par l'API")
public class LeadDto {
    
    @Schema(description = "ID unique", example = "1")
    private Long id;
    
    @Schema(description = "Nom complet", example = "Jean Dupont")
    private String fullName;
    
    @Schema(description = "Statut du lead", example = "NEW")
    private LeadStatus status;
    
    @Schema(description = "Date de creation", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
}
```

---

## 6. Securite dans Swagger

### Afficher le bouton Authorize

```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
        .components(new Components()
            .addSecuritySchemes("Bearer Authentication",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
}
```

### Marquer les endpoints proteges

```java
@GetMapping
@Operation(summary = "Liste des leads", security = @SecurityRequirement(name = "Bearer Authentication"))
public Page<LeadDto> getAllLeads() {
    return service.findAll();
}
```

### Exclure les endpoints publics

```java
@PostMapping
@Operation(summary = "Soumettre formulaire", security = {})  // Pas de securite
public MessageResponse submit(@RequestBody ContactFormRequest request) {
    return service.create(request);
}
```

---

## 7. Personnalisation

### Grouper les endpoints

```java
@Bean
public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
        .group("public")
        .pathsToMatch("/api/contact/**", "/api/auth/**")
        .build();
}

@Bean
public GroupedOpenApi adminApi() {
    return GroupedOpenApi.builder()
        .group("admin")
        .pathsToMatch("/api/admin/**")
        .build();
}
```

### Cacher des endpoints

```java
@GetMapping("/internal")
@Hidden  // N'apparait pas dans Swagger
public String internal() {
    return "hidden";
}
```

---

## 8. Utiliser Swagger UI

### Tester un endpoint

1. Ouvrir http://localhost:8080/swagger-ui.html
2. Cliquer sur un endpoint
3. Cliquer "Try it out"
4. Remplir les parametres
5. Cliquer "Execute"

### Authentification JWT

1. Obtenir un token via POST /api/auth/login
2. Cliquer "Authorize" (cadenas)
3. Entrer le token (sans "Bearer ")
4. Cliquer "Authorize"
5. Tous les endpoints utiliseront le token

---

## 9. Bonnes pratiques

### 9.1 Descriptions claires

```java
@Operation(
    summary = "Titre court",           // Affiche dans la liste
    description = "Description detaillee avec exemples et cas d'utilisation"
)
```

### 9.2 Exemples realistes

```java
@Schema(example = "jean.dupont@entreprise.com")  // Pas "test@test.com"
private String email;
```

### 9.3 Documenter les erreurs

```java
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Succes"),
    @ApiResponse(responseCode = "400", description = "Donnees invalides"),
    @ApiResponse(responseCode = "401", description = "Non authentifie"),
    @ApiResponse(responseCode = "404", description = "Non trouve")
})
```

---

## 10. Points cles a retenir

1. **springdoc-openapi** pour Spring Boot 3
2. **@Tag** pour grouper les controllers
3. **@Operation** pour documenter les methodes
4. **@Schema** pour documenter les DTOs
5. **SecurityScheme** pour l'authentification JWT

---

## QUIZ 10.1 - OpenAPI et Swagger

**1. Quelle est la difference entre OpenAPI et Swagger?**
   - a) Aucune
   - b) OpenAPI = specification, Swagger = outils
   - c) Swagger = specification, OpenAPI = outils
   - d) Deux projets concurrents

**2. Quelle URL pour Swagger UI par defaut?**
   - a) /api-docs
   - b) /swagger
   - c) /swagger-ui.html
   - d) /docs

**3. Quelle annotation pour documenter un controller?**
   - a) @Api
   - b) @Tag
   - c) @Controller
   - d) @Documented

**4. Quelle annotation pour documenter une methode?**
   - a) @Api
   - b) @ApiOperation
   - c) @Operation
   - d) @Method

**5. VRAI ou FAUX: Swagger peut executer des requetes directement.**

**6. Quelle annotation pour documenter un DTO?**
   - a) @ApiModel
   - b) @Schema
   - c) @Model
   - d) @Dto

**7. Comment cacher un endpoint de Swagger?**
   - a) @Ignore
   - b) @Hidden
   - c) @Private
   - d) @NoDoc

**8. Completez: springdoc-openapi genere la specification au format _______.**

**9. Quelle URL pour la specification JSON?**
   - a) /api-docs
   - b) /v3/api-docs
   - c) /openapi.json
   - d) /swagger.json

**10. Comment authentifier dans Swagger UI?**
   - a) Login/password
   - b) Bouton Authorize avec le token
   - c) Cookie
   - d) Header manuel

---

### REPONSES QUIZ 10.1

1. b) OpenAPI = specification, Swagger = outils
2. c) /swagger-ui.html
3. b) @Tag
4. c) @Operation
5. VRAI (Try it out)
6. b) @Schema
7. b) @Hidden
8. JSON (ou YAML)
9. b) /v3/api-docs
10. b) Bouton Authorize avec le token

