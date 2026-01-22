# Chapitre 3.5 - Validation des donnees

## Objectifs du chapitre

- Utiliser Bean Validation
- Creer des validations personnalisees
- Gerer les erreurs de validation

---

## 1. Introduction a Bean Validation

### Qu'est-ce que Bean Validation?

Bean Validation (JSR 380) est une specification Java pour valider les objets avec des annotations.

### Dependance

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

---

## 2. Annotations de validation

### Annotations pour les chaines

| Annotation | Description |
|------------|-------------|
| @NotNull | Ne doit pas etre null |
| @NotEmpty | Non null et non vide |
| @NotBlank | Non null, non vide, non espaces seuls |
| @Size(min, max) | Taille entre min et max |
| @Pattern(regexp) | Doit correspondre a l'expression reguliere |

```java
public class ContactFormRequest {
    
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit faire entre 2 et 100 caracteres")
    private String fullName;
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;
    
    @Pattern(regexp = "^[+]?[0-9\\s-]{0,20}$", message = "Format de telephone invalide")
    private String phone;
}
```

### Annotations pour les nombres

| Annotation | Description |
|------------|-------------|
| @Min(value) | Valeur minimale |
| @Max(value) | Valeur maximale |
| @Positive | Doit etre positif (> 0) |
| @PositiveOrZero | Doit etre >= 0 |
| @Negative | Doit etre negatif (< 0) |
| @NegativeOrZero | Doit etre <= 0 |
| @Digits(integer, fraction) | Nombre de chiffres |

```java
public class ProductRequest {
    
    @Positive(message = "Le prix doit etre positif")
    @Digits(integer = 10, fraction = 2, message = "Format de prix invalide")
    private BigDecimal price;
    
    @Min(value = 0, message = "La quantite ne peut pas etre negative")
    @Max(value = 1000, message = "La quantite ne peut pas depasser 1000")
    private Integer quantity;
}
```

### Annotations pour les dates

| Annotation | Description |
|------------|-------------|
| @Past | Doit etre dans le passe |
| @PastOrPresent | Passe ou present |
| @Future | Doit etre dans le futur |
| @FutureOrPresent | Futur ou present |

```java
public class EventRequest {
    
    @FutureOrPresent(message = "La date ne peut pas etre dans le passe")
    private LocalDateTime eventDate;
    
    @Past(message = "La date de naissance doit etre dans le passe")
    private LocalDate birthDate;
}
```

### Autres annotations

| Annotation | Description |
|------------|-------------|
| @Email | Format email valide |
| @AssertTrue | Doit etre true |
| @AssertFalse | Doit etre false |

---

## 3. Activer la validation

### Dans le Controller

```java
@PostMapping
public ResponseEntity<LeadDto> create(
        @RequestBody @Valid ContactFormRequest request) {  // @Valid active la validation
    return ResponseEntity.ok(service.create(request));
}
```

### Sans @Valid

La validation n'est PAS executee!

```java
@PostMapping
public ResponseEntity<LeadDto> create(
        @RequestBody ContactFormRequest request) {  // Pas de @Valid = pas de validation!
    return ResponseEntity.ok(service.create(request));
}
```

---

## 4. DTO avec validation complete

### ContactFormRequest.java

```java
package com.example.contact.dto.request;

import com.example.contact.model.RequestType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ContactFormRequest {
    
    @NotBlank(message = "Le nom complet est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit faire entre {min} et {max} caracteres")
    private String fullName;
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Size(max = 255, message = "L'email ne peut pas depasser {max} caracteres")
    private String email;
    
    @Size(max = 100, message = "Le nom de l'entreprise ne peut pas depasser {max} caracteres")
    private String company;
    
    @Pattern(
        regexp = "^$|^[+]?[0-9\\s()-]{6,20}$",
        message = "Format de telephone invalide"
    )
    private String phone;
    
    @NotNull(message = "Le type de demande est obligatoire")
    private RequestType requestType;
    
    @NotBlank(message = "Le message est obligatoire")
    @Size(min = 10, max = 5000, message = "Le message doit faire entre {min} et {max} caracteres")
    private String message;
}
```

---

## 5. Gestion des erreurs de validation

### Exception levee

Quand la validation echoue, Spring leve `MethodArgumentNotValidException`.

### GlobalExceptionHandler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        
        ValidationErrorResponse response = new ValidationErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Erreur de validation",
            errors
        );
        
        return ResponseEntity.badRequest().body(response);
    }
}
```

### ValidationErrorResponse.java

```java
@Data
@AllArgsConstructor
public class ValidationErrorResponse {
    private int status;
    private String message;
    private Map<String, String> errors;
}
```

### Reponse JSON

```json
{
  "status": 400,
  "message": "Erreur de validation",
  "errors": {
    "fullName": "Le nom complet est obligatoire",
    "email": "Format d'email invalide",
    "message": "Le message doit faire entre 10 et 5000 caracteres"
  }
}
```

---

## 6. Validation personnalisee

### Creer une annotation

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneValidator.class)
public @interface ValidPhone {
    String message() default "Format de telephone invalide";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

### Creer le validateur

```java
public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {
    
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^[+]?[0-9\\s()-]{6,20}$");
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;  // Utiliser @NotBlank si obligatoire
        }
        return PHONE_PATTERN.matcher(value).matches();
    }
}
```

### Utilisation

```java
public class ContactFormRequest {
    
    @ValidPhone
    private String phone;
}
```

---

## 7. Validation de groupes

### Definir des groupes

```java
public interface OnCreate {}
public interface OnUpdate {}
```

### Appliquer les groupes

```java
public class LeadRequest {
    
    @Null(groups = OnCreate.class, message = "L'ID ne doit pas etre fourni")
    @NotNull(groups = OnUpdate.class, message = "L'ID est obligatoire")
    private Long id;
    
    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    private String fullName;
}
```

### Utiliser @Validated

```java
@PostMapping
public ResponseEntity<?> create(
        @RequestBody @Validated(OnCreate.class) LeadRequest request) {
    // ...
}

@PutMapping("/{id}")
public ResponseEntity<?> update(
        @PathVariable Long id,
        @RequestBody @Validated(OnUpdate.class) LeadRequest request) {
    // ...
}
```

---

## 8. Validation dans le Service

### Validation programmatique

```java
@Service
public class LeadService {
    
    private final Validator validator;
    
    public LeadDto create(ContactFormRequest request) {
        // Validation manuelle
        Set<ConstraintViolation<ContactFormRequest>> violations = 
            validator.validate(request);
        
        if (!violations.isEmpty()) {
            throw new ValidationException(violations);
        }
        
        // Suite du traitement
    }
}
```

### Validation metier

```java
public LeadDto create(ContactFormRequest request) {
    // Validation metier (pas dans le DTO)
    if (leadRepository.existsByEmail(request.getEmail())) {
        throw new DuplicateEmailException("Un lead avec cet email existe deja");
    }
    
    // ...
}
```

---

## 9. Bonnes pratiques

### 9.1 Validation cote serveur obligatoire

Meme si le frontend valide, toujours valider cote serveur!

### 9.2 Messages clairs

```java
// MAUVAIS
@NotBlank
private String fullName;  // Message par defaut peu explicite

// BON
@NotBlank(message = "Le nom complet est obligatoire")
private String fullName;
```

### 9.3 Utiliser le bon type de validation

```java
// Pour une chaine obligatoire
@NotBlank  // Verifie null, vide, et espaces

// Pour un objet obligatoire
@NotNull

// Pour une liste non vide
@NotEmpty
```

### 9.4 Separer validation technique et metier

- **Technique** (DTO): @NotBlank, @Email, @Size
- **Metier** (Service): Verifier les doublons, les regles specifiques

---

## 10. Points cles a retenir

1. **@Valid** active la validation dans le controller
2. **@NotBlank** pour les chaines obligatoires
3. **@Email, @Pattern** pour les formats
4. **MethodArgumentNotValidException** pour les erreurs
5. **Validations personnalisees** avec @Constraint

---

## QUIZ 3.5 - Validation des donnees

**1. Quelle annotation active la validation dans le controller?**
   - a) @Validate
   - b) @Valid
   - c) @Check
   - d) @Validated

**2. Quelle annotation pour une chaine non nulle et non vide?**
   - a) @NotNull
   - b) @NotEmpty
   - c) @NotBlank
   - d) @Required

**3. Quelle exception est levee si la validation echoue?**
   - a) ValidationException
   - b) MethodArgumentNotValidException
   - c) IllegalArgumentException
   - d) BadRequestException

**4. Quelle annotation valide un format email?**
   - a) @Mail
   - b) @Email
   - c) @ValidEmail
   - d) @EmailFormat

**5. VRAI ou FAUX: Si @Valid est absent, la validation est quand meme executee.**

**6. Comment specifier une taille minimale et maximale?**
   - a) @Length(min, max)
   - b) @Size(min, max)
   - c) @Range(min, max)
   - d) @Between(min, max)

**7. Quelle annotation pour un nombre positif?**
   - a) @Min(1)
   - b) @Positive
   - c) @GreaterThanZero
   - d) @Above(0)

**8. Completez: @Pattern valide avec une expression _______.**

**9. Ou placer la validation "email unique"?**
   - a) Dans le DTO
   - b) Dans le Controller
   - c) Dans le Service
   - d) Dans le Repository

**10. Quelle annotation pour une date dans le futur?**
   - a) @After
   - b) @FutureDate
   - c) @Future
   - d) @Later

---

### REPONSES QUIZ 3.5

1. b) @Valid
2. c) @NotBlank
3. b) MethodArgumentNotValidException
4. b) @Email
5. FAUX
6. b) @Size(min, max)
7. b) @Positive
8. reguliere (ou regex)
9. c) Dans le Service
10. c) @Future

