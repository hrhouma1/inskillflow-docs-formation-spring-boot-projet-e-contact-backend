# Module 16 — Gestion des erreurs

## Prérequis

Tu as terminé le Module 15 (validation des données).

---

## Pourquoi gérer les erreurs ?

| Sans gestion | Avec gestion |
|--------------|--------------|
| Stack trace exposée | Message propre |
| 500 Internal Server Error | Code HTTP approprié |
| Pas de détails utiles | Informations claires |
| Faille de sécurité | Sécurisé |

---

## Les types d'erreurs à gérer

| Type | Exemple | Code HTTP |
|------|---------|-----------|
| Validation | Username vide | 400 |
| Non trouvé | User inexistant | 404 |
| Non autorisé | Mauvais password | 401 |
| Interdit | Pas le bon rôle | 403 |
| Conflit | Username déjà pris | 409 |
| Serveur | Bug dans le code | 500 |

---

## Étape 1 : Créer les exceptions personnalisées

### 1.1 Créer NotFoundException

`src/main/java/com/demo/securitydemo/NotFoundException.java`

```java
package com.demo.securitydemo;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
```

### 1.2 Créer UnauthorizedException

`src/main/java/com/demo/securitydemo/UnauthorizedException.java`

```java
package com.demo.securitydemo;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
```

### 1.3 Créer ConflictException

`src/main/java/com/demo/securitydemo/ConflictException.java`

```java
package com.demo.securitydemo;

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
```

---

## Étape 2 : Créer une réponse d'erreur standard

### 2.1 Créer le fichier

`src/main/java/com/demo/securitydemo/ErrorResponse.java`

### 2.2 Copier ce code

```java
package com.demo.securitydemo;

import java.time.LocalDateTime;

public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;

    public ErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
```

### 2.3 Exemple de réponse

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "User non trouvé",
  "path": "/auth/login",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## Étape 3 : Modifier GlobalExceptionHandler

### 3.1 Remplacer le contenu

```java
package com.demo.securitydemo;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Erreur de validation (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", "Validation Error",
                "message", "Données invalides",
                "details", errors,
                "path", request.getRequestURI()
        ));
    }

    // Non trouvé (404)
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            NotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                404,
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // Non autorisé (401)
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                401,
                "Unauthorized",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // Conflit (409)
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                409,
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // Erreur générique (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(
            Exception ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                500,
                "Internal Server Error",
                "Une erreur inattendue s'est produite",
                request.getRequestURI()
        );

        // Log l'erreur pour le debug (ne pas exposer au client)
        System.err.println("Erreur: " + ex.getMessage());
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

---

## Étape 4 : Modifier AuthController pour utiliser les exceptions

### 4.1 Remplacer le contenu

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

        // Vérifie si username existe déjà
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username '" + request.getUsername() + "' déjà utilisé");
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

        // Cherche le user
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new NotFoundException("User '" + request.getUsername() + "' non trouvé"));

        // Vérifie le password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Mot de passe incorrect");
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

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh token manquant");
        }

        if (!jwtService.isValidRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Refresh token invalide ou expiré");
        }

        String username = jwtService.extractUsername(refreshToken);

        if (!userRepository.existsByUsername(username)) {
            throw new NotFoundException("User n'existe plus");
        }

        String newAccessToken = jwtService.generateAccessToken(username);

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }
}
```

### 4.2 Ce qui a changé

| Avant | Après |
|-------|-------|
| `return ResponseEntity.badRequest()` | `throw new ConflictException()` |
| `return ResponseEntity.status(401)` | `throw new UnauthorizedException()` |
| `.orElse(null)` puis `if (user == null)` | `.orElseThrow()` |

---

## Étape 5 : Tester

### 5.1 Relancer l'application

```bash
mvn spring-boot:run
```

### 5.2 Tester 404 - User non trouvé

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"nexistepas","password":"test123"}'
```

Résultat :
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "User 'nexistepas' non trouvé",
  "path": "/auth/login",
  "timestamp": "2024-01-15T10:30:00"
}
```

### 5.3 Tester 401 - Mauvais password

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"mauvais"}'
```

Résultat :
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Mot de passe incorrect",
  "path": "/auth/login",
  "timestamp": "2024-01-15T10:30:00"
}
```

### 5.4 Tester 409 - Username déjà pris

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"test123"}'
```

Résultat :
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Username 'admin' déjà utilisé",
  "path": "/auth/register",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## Tableau des tests

| # | Test | Code | Message |
|---|------|------|---------|
| 1 | User non trouvé | 404 | User 'x' non trouvé |
| 2 | Mauvais password | 401 | Mot de passe incorrect |
| 3 | Username déjà pris | 409 | Username 'x' déjà utilisé |
| 4 | Validation échouée | 400 | Données invalides + details |
| 5 | Refresh token invalide | 401 | Refresh token invalide |
| 6 | Login OK | 200 | tokens |

---

## Problèmes courants

<details>
<summary>L'exception n'est pas interceptée</summary>

**Cause :** L'exception n'est pas dans le handler

**Solution :**
Ajouter un `@ExceptionHandler` pour cette exception dans GlobalExceptionHandler

</details>

<details>
<summary>Stack trace exposée au client</summary>

**Cause :** Pas de handler pour Exception.class

**Solution :**
Ajouter le handler générique :
```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleGenericError(...)
```

</details>

<details>
<summary>Le timestamp est null</summary>

**Cause :** Pas initialisé dans le constructeur

**Solution :**
```java
this.timestamp = LocalDateTime.now();
```

</details>

---

## Annexe 1 : Structure du projet

<details>
<summary>Voir la structure</summary>

```
security-demo/
├── pom.xml
├── src/main/java/com/demo/securitydemo/
│   ├── SecurityDemoApplication.java
│   ├── HelloController.java
│   ├── AuthController.java           ← Modifié (throw exceptions)
│   ├── SecurityConfig.java
│   ├── JwtService.java
│   ├── JwtFilter.java
│   ├── User.java
│   ├── UserRepository.java
│   ├── DataInit.java
│   ├── RegisterRequest.java
│   ├── LoginRequest.java
│   ├── GlobalExceptionHandler.java   ← Modifié (nouveaux handlers)
│   ├── ErrorResponse.java            ← Nouveau
│   ├── NotFoundException.java        ← Nouveau
│   ├── UnauthorizedException.java    ← Nouveau
│   └── ConflictException.java        ← Nouveau
└── src/main/resources/
    └── application.properties
```

</details>

---

## Annexe 2 : Fichier test-errors.http

<details>
<summary>Voir le fichier de test</summary>

```http
### =============================================
### Module 16 - Tests Gestion des erreurs
### =============================================

### -----------------------------------------
### Test 1 : 404 - User non trouvé
### -----------------------------------------
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "username": "nexistepas",
    "password": "test123"
}

### -----------------------------------------
### Test 2 : 401 - Mauvais password
### -----------------------------------------
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "mauvais"
}

### -----------------------------------------
### Test 3 : 409 - Username déjà pris
### -----------------------------------------
POST http://localhost:8080/auth/register
Content-Type: application/json

{
    "username": "admin",
    "password": "test123456"
}

### -----------------------------------------
### Test 4 : 400 - Validation échouée
### -----------------------------------------
POST http://localhost:8080/auth/register
Content-Type: application/json

{
    "username": "",
    "password": "123"
}

### -----------------------------------------
### Test 5 : 401 - Refresh token invalide
### -----------------------------------------
POST http://localhost:8080/auth/refresh
Content-Type: application/json

{
    "refreshToken": "token.invalide.ici"
}

### -----------------------------------------
### Test 6 : 401 - Refresh token manquant
### -----------------------------------------
POST http://localhost:8080/auth/refresh
Content-Type: application/json

{
    "refreshToken": ""
}

### -----------------------------------------
### Test 7 : 200 - Login OK (pour comparaison)
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
| 1 | Créé 3 exceptions personnalisées |
| 2 | Créé ErrorResponse (format standard) |
| 3 | Modifié GlobalExceptionHandler (tous les handlers) |
| 4 | Modifié AuthController (throw au lieu de return) |
| 5 | Testé tous les cas d'erreur |

---

## Avantages de cette approche

| Aspect | Bénéfice |
|--------|----------|
| Code plus propre | throw au lieu de return partout |
| Format uniforme | Même structure pour toutes les erreurs |
| Sécurisé | Pas de stack trace exposée |
| Maintenable | Toutes les erreurs au même endroit |
| Testable | Facile à tester |

---

## Prochaine étape

Module 17 : Rôles et permissions (@PreAuthorize)

