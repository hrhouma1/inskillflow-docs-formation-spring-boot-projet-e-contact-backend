# Module 15 — Validation des données

## Prérequis

Tu as terminé le Module 14 (projet avec refresh token).

---

## Pourquoi valider ?

| Sans validation | Avec validation |
|-----------------|-----------------|
| Username vide accepté | Erreur claire |
| Password "a" accepté | Minimum 6 caractères |
| Email invalide accepté | Format vérifié |
| Crash en base | Erreur avant la base |

---

## Étape 1 : Ajouter la dépendance

### 1.1 Ouvrir pom.xml

### 1.2 Ajouter cette dépendance

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### 1.3 Recharger Maven

Clic droit sur pom.xml > Maven > Reload Project

---

## Étape 2 : Créer un DTO pour l'inscription

### 2.1 Créer le fichier

`src/main/java/com/demo/securitydemo/RegisterRequest.java`

### 2.2 Copier ce code

```java
package com.demo.securitydemo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Le username est obligatoire")
    @Size(min = 3, max = 20, message = "Le username doit faire entre 3 et 20 caractères")
    private String username;

    @NotBlank(message = "Le password est obligatoire")
    @Size(min = 6, message = "Le password doit faire au moins 6 caractères")
    private String password;

    // Getters et Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
```

### 2.3 Ce que fait chaque annotation

| Annotation | Effet |
|------------|-------|
| `@NotBlank` | Ne peut pas être vide ou null |
| `@Size(min=3)` | Minimum 3 caractères |
| `@Size(max=20)` | Maximum 20 caractères |
| `message = "..."` | Message d'erreur personnalisé |

---

## Étape 3 : Créer un DTO pour le login

### 3.1 Créer le fichier

`src/main/java/com/demo/securitydemo/LoginRequest.java`

### 3.2 Copier ce code

```java
package com.demo.securitydemo;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Le username est obligatoire")
    private String username;

    @NotBlank(message = "Le password est obligatoire")
    private String password;

    // Getters et Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
```

---

## Étape 4 : Modifier AuthController

### 4.1 Remplacer le contenu de AuthController.java

```java
package com.demo.securitydemo;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthController(JwtService jwtService,
                          PasswordEncoder passwordEncoder,
                          UserRepository userRepository) {
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username déjà utilisé"));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");

        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "User créé avec succès"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername()).orElse(null);

        if (user == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "User non trouvé"));
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Mot de passe incorrect"));
        }

        String accessToken = jwtService.generateAccessToken(request.getUsername());
        String refreshToken = jwtService.generateRefreshToken(request.getUsername());

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "username", request.getUsername(),
                "role", user.getRole()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (!jwtService.isValidRefreshToken(refreshToken)) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Refresh token invalide ou expiré"));
        }

        String username = jwtService.extractUsername(refreshToken);

        if (!userRepository.existsByUsername(username)) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "User n'existe plus"));
        }

        String newAccessToken = jwtService.generateAccessToken(username);

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }
}
```

### 4.2 Ce qui a changé

| Avant | Après |
|-------|-------|
| `Map<String, String> request` | `RegisterRequest request` |
| `request.get("username")` | `request.getUsername()` |
| Pas de validation | `@Valid` active la validation |

---

## Étape 5 : Tester sans gestion d'erreur

### 5.1 Relancer l'application

```bash
mvn spring-boot:run
```

### 5.2 Tester avec username vide

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"","password":"test123"}'
```

Résultat : `400 Bad Request` avec une erreur moche

Le problème : l'erreur n'est pas lisible. On va corriger ça.

---

## Étape 6 : Créer un GlobalExceptionHandler

### 6.1 Créer le fichier

`src/main/java/com/demo/securitydemo/GlobalExceptionHandler.java`

### 6.2 Copier ce code

```java
package com.demo.securitydemo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity.badRequest().body(Map.of(
                "error", "Validation échouée",
                "details", errors
        ));
    }
}
```

### 6.3 Ce que fait ce code

| Élément | Effet |
|---------|-------|
| `@RestControllerAdvice` | Intercepte les erreurs de tous les controllers |
| `@ExceptionHandler` | Gère un type d'erreur spécifique |
| `MethodArgumentNotValidException` | Erreur quand @Valid échoue |
| `getFieldErrors()` | Liste des champs en erreur |

---

## Étape 7 : Tester avec gestion d'erreur

### 7.1 Relancer l'application

```bash
mvn spring-boot:run
```

### 7.2 Tester avec username vide

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"","password":"test123"}'
```

Résultat :
```json
{
  "error": "Validation échouée",
  "details": {
    "username": "Le username est obligatoire"
  }
}
```

### 7.3 Tester avec password trop court

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123"}'
```

Résultat :
```json
{
  "error": "Validation échouée",
  "details": {
    "password": "Le password doit faire au moins 6 caractères"
  }
}
```

### 7.4 Tester avec plusieurs erreurs

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"ab","password":"123"}'
```

Résultat :
```json
{
  "error": "Validation échouée",
  "details": {
    "username": "Le username doit faire entre 3 et 20 caractères",
    "password": "Le password doit faire au moins 6 caractères"
  }
}
```

---

## Tableau des annotations de validation

| Annotation | Usage | Exemple |
|------------|-------|---------|
| `@NotBlank` | Champ texte obligatoire | Username |
| `@NotNull` | Champ obligatoire (tout type) | Age |
| `@Size(min, max)` | Longueur min/max | Password |
| `@Min(value)` | Valeur minimum | Age >= 18 |
| `@Max(value)` | Valeur maximum | Quantité <= 100 |
| `@Email` | Format email | Email valide |
| `@Pattern(regexp)` | Regex personnalisée | Téléphone |

---

## Tableau des tests

| # | Test | Body | Résultat |
|---|------|------|----------|
| 1 | Username vide | `{"username":"","password":"test123"}` | 400 + erreur username |
| 2 | Password court | `{"username":"test","password":"123"}` | 400 + erreur password |
| 3 | Les deux invalides | `{"username":"","password":""}` | 400 + 2 erreurs |
| 4 | Données valides | `{"username":"test","password":"test123"}` | 200 OK |

---

## Problèmes courants

<details>
<summary>La validation ne fonctionne pas</summary>

**Cause :** Oublié `@Valid` devant `@RequestBody`

**Solution :**
```java
// Mauvais
public ResponseEntity<?> register(@RequestBody RegisterRequest request)

// Bon
public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request)
```

</details>

<details>
<summary>Erreur 500 au lieu de 400</summary>

**Cause :** Pas de GlobalExceptionHandler

**Solution :**
Créer GlobalExceptionHandler.java avec `@RestControllerAdvice`

</details>

<details>
<summary>Message d'erreur en anglais</summary>

**Cause :** Pas de `message = "..."` dans l'annotation

**Solution :**
```java
@NotBlank(message = "Le username est obligatoire")
```

</details>

---

## Annexe 1 : Structure du projet

<details>
<summary>Voir la structure</summary>

```
security-demo/
├── pom.xml                        ← Modifié (validation)
├── src/main/java/com/demo/securitydemo/
│   ├── SecurityDemoApplication.java
│   ├── HelloController.java
│   ├── AuthController.java        ← Modifié (@Valid)
│   ├── SecurityConfig.java
│   ├── JwtService.java
│   ├── JwtFilter.java
│   ├── User.java
│   ├── UserRepository.java
│   ├── DataInit.java
│   ├── RegisterRequest.java       ← Nouveau
│   ├── LoginRequest.java          ← Nouveau
│   └── GlobalExceptionHandler.java ← Nouveau
└── src/main/resources/
    └── application.properties
```

</details>

---

## Annexe 2 : Fichier test-validation.http

<details>
<summary>Voir le fichier de test</summary>

```http
### =============================================
### Module 15 - Tests Validation
### =============================================

### -----------------------------------------
### Test 1 : Username vide
### Résultat attendu : 400 + erreur username
### -----------------------------------------
POST http://localhost:8080/auth/register
Content-Type: application/json

{
    "username": "",
    "password": "test123"
}

### -----------------------------------------
### Test 2 : Password trop court
### Résultat attendu : 400 + erreur password
### -----------------------------------------
POST http://localhost:8080/auth/register
Content-Type: application/json

{
    "username": "testuser",
    "password": "123"
}

### -----------------------------------------
### Test 3 : Username trop court
### Résultat attendu : 400 + erreur username
### -----------------------------------------
POST http://localhost:8080/auth/register
Content-Type: application/json

{
    "username": "ab",
    "password": "test123"
}

### -----------------------------------------
### Test 4 : Les deux invalides
### Résultat attendu : 400 + 2 erreurs
### -----------------------------------------
POST http://localhost:8080/auth/register
Content-Type: application/json

{
    "username": "",
    "password": ""
}

### -----------------------------------------
### Test 5 : Données valides
### Résultat attendu : 200 OK
### -----------------------------------------
POST http://localhost:8080/auth/register
Content-Type: application/json

{
    "username": "validuser",
    "password": "validpass123"
}

### -----------------------------------------
### Test 6 : Login avec username vide
### Résultat attendu : 400 + erreur username
### -----------------------------------------
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "username": "",
    "password": "test123"
}

### -----------------------------------------
### Test 7 : Login valide
### Résultat attendu : 200 OK + tokens
### -----------------------------------------
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "admin123"
}
```

</details>

---

## Résumé

| Étape | Ce qu'on a fait |
|-------|-----------------|
| 1 | Ajouté dépendance validation |
| 2 | Créé RegisterRequest avec annotations |
| 3 | Créé LoginRequest avec annotations |
| 4 | Modifié AuthController (@Valid) |
| 5 | Testé (erreur moche) |
| 6 | Créé GlobalExceptionHandler |
| 7 | Testé (erreur propre) |

---

## Prochaine étape

Module 16 : Gestion avancée des erreurs

