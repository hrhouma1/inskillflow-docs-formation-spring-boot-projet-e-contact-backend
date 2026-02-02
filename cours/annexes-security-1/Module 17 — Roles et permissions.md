# Module 17 — Rôles et permissions

## Prérequis

Tu as terminé le Module 16 (gestion des erreurs).

---

## Ce qu'on va faire

| Avant | Après |
|-------|-------|
| 2 rôles (USER, ADMIN) | Rôles dynamiques |
| Vérification dans SecurityConfig | @PreAuthorize sur les méthodes |
| Permissions basiques | Permissions fines |

---

## Les 2 approches

| Approche | Où | Exemple |
|----------|-----|---------|
| SecurityConfig | Centralisé | `.requestMatchers("/admin").hasRole("ADMIN")` |
| @PreAuthorize | Sur chaque méthode | `@PreAuthorize("hasRole('ADMIN')")` |

On va utiliser **@PreAuthorize** car c'est plus flexible.

---

## Étape 1 : Activer @PreAuthorize

### 1.1 Modifier SecurityConfig.java

Ajouter `@EnableMethodSecurity` en haut de la classe :

```java
package com.demo.securitydemo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // <-- AJOUTER CETTE LIGNE
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/public").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                // Retirer .requestMatchers("/admin").hasRole("ADMIN")
                // On va utiliser @PreAuthorize à la place
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 1.2 Ce que fait @EnableMethodSecurity

| Sans | Avec |
|------|------|
| @PreAuthorize ignoré | @PreAuthorize actif |
| Sécurité dans SecurityConfig seulement | Sécurité sur chaque méthode |

---

## Étape 2 : Modifier HelloController

### 2.1 Remplacer le contenu

```java
package com.demo.securitydemo;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "Ceci est PUBLIC - tout le monde peut voir";
    }

    @GetMapping("/private")
    @PreAuthorize("isAuthenticated()")
    public String privateEndpoint(Authentication auth) {
        return "Bonjour " + auth.getName() + " ! Tu es connecté.";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminEndpoint(Authentication auth) {
        return "Bonjour Admin " + auth.getName() + " !";
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public String userEndpoint(Authentication auth) {
        return "Bonjour User " + auth.getName() + " !";
    }

    @GetMapping("/admin-or-user")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String adminOrUserEndpoint(Authentication auth) {
        return "Bonjour " + auth.getName() + " (admin ou user)";
    }
}
```

### 2.2 Les expressions @PreAuthorize

| Expression | Signification |
|------------|---------------|
| `isAuthenticated()` | Connecté (peu importe le rôle) |
| `hasRole('ADMIN')` | Rôle ADMIN |
| `hasRole('USER')` | Rôle USER |
| `hasAnyRole('ADMIN', 'USER')` | ADMIN ou USER |
| `permitAll()` | Tout le monde (pas besoin de @PreAuthorize) |

### 2.3 Le paramètre Authentication

```java
public String privateEndpoint(Authentication auth) {
    auth.getName();           // Username
    auth.getAuthorities();    // Liste des rôles
    auth.isAuthenticated();   // true/false
}
```

---

## Étape 3 : Gérer l'erreur 403

### 3.1 Créer ForbiddenException

`src/main/java/com/demo/securitydemo/ForbiddenException.java`

```java
package com.demo.securitydemo;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
```

### 3.2 Ajouter le handler dans GlobalExceptionHandler

Ajouter cette méthode :

```java
import org.springframework.security.access.AccessDeniedException;

// Accès refusé (403) - Spring Security
@ExceptionHandler(AccessDeniedException.class)
public ResponseEntity<ErrorResponse> handleAccessDenied(
        AccessDeniedException ex,
        HttpServletRequest request) {

    ErrorResponse error = new ErrorResponse(
            403,
            "Forbidden",
            "Accès refusé - vous n'avez pas les droits",
            request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
}
```

---

## Étape 4 : Tester

### 4.1 Relancer l'application

```bash
mvn spring-boot:run
```

### 4.2 Login admin

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Garder le `accessToken`.

### 4.3 Login user

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"user123"}'
```

Garder le `accessToken`.

### 4.4 Tester /admin avec token admin

```bash
curl http://localhost:8080/admin \
  -H "Authorization: Bearer <TOKEN_ADMIN>"
```

Résultat : `Bonjour Admin admin !`

### 4.5 Tester /admin avec token user

```bash
curl http://localhost:8080/admin \
  -H "Authorization: Bearer <TOKEN_USER>"
```

Résultat :
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Accès refusé - vous n'avez pas les droits",
  "path": "/admin"
}
```

### 4.6 Tester /user avec token user

```bash
curl http://localhost:8080/user \
  -H "Authorization: Bearer <TOKEN_USER>"
```

Résultat : `Bonjour User user !`

### 4.7 Tester /admin-or-user avec token user

```bash
curl http://localhost:8080/admin-or-user \
  -H "Authorization: Bearer <TOKEN_USER>"
```

Résultat : `Bonjour user (admin ou user)`

---

## Étape 5 : Ajouter des permissions plus fines

### 5.1 Créer un endpoint avec vérification du username

Ajouter dans HelloController :

```java
@GetMapping("/profile/{username}")
@PreAuthorize("isAuthenticated() and (#username == authentication.name or hasRole('ADMIN'))")
public String profileEndpoint(@PathVariable String username, Authentication auth) {
    return "Profil de " + username + " (demandé par " + auth.getName() + ")";
}
```

### 5.2 Ce que fait cette expression

```
#username == authentication.name or hasRole('ADMIN')
```

| Qui | Peut voir |
|-----|-----------|
| L'utilisateur lui-même | Son propre profil |
| Un admin | Tous les profils |
| Un autre user | Refusé (403) |

### 5.3 Tester

```bash
# User "user" voit son profil
curl http://localhost:8080/profile/user \
  -H "Authorization: Bearer <TOKEN_USER>"
# OK

# User "user" voit profil admin
curl http://localhost:8080/profile/admin \
  -H "Authorization: Bearer <TOKEN_USER>"
# 403 Forbidden

# Admin voit profil user
curl http://localhost:8080/profile/user \
  -H "Authorization: Bearer <TOKEN_ADMIN>"
# OK
```

---

## Tableau des tests

| # | Endpoint | Token | Résultat |
|---|----------|-------|----------|
| 1 | /public | Aucun | 200 OK |
| 2 | /private | Aucun | 403 |
| 3 | /private | User | 200 OK |
| 4 | /admin | User | 403 |
| 5 | /admin | Admin | 200 OK |
| 6 | /user | User | 200 OK |
| 7 | /user | Admin | 403 |
| 8 | /admin-or-user | User | 200 OK |
| 9 | /admin-or-user | Admin | 200 OK |
| 10 | /profile/user | User | 200 OK |
| 11 | /profile/admin | User | 403 |
| 12 | /profile/user | Admin | 200 OK |

---

## Tableau des expressions @PreAuthorize

| Expression | Description |
|------------|-------------|
| `permitAll()` | Tout le monde |
| `denyAll()` | Personne |
| `isAuthenticated()` | Connecté |
| `isAnonymous()` | Non connecté |
| `hasRole('X')` | A le rôle X |
| `hasAnyRole('X','Y')` | A le rôle X ou Y |
| `hasAuthority('X')` | A l'autorité X (sans ROLE_) |
| `#param == authentication.name` | Paramètre = username |
| `@bean.method()` | Appelle une méthode d'un bean |

---

## Problèmes courants

<details>
<summary>@PreAuthorize n'a aucun effet</summary>

**Cause :** @EnableMethodSecurity manquant

**Solution :**
Ajouter sur SecurityConfig :
```java
@EnableMethodSecurity
```

</details>

<details>
<summary>403 même avec le bon rôle</summary>

**Cause :** Le rôle n'a pas le préfixe ROLE_

**Solution :**
Dans JwtFilter :
```java
String role = "ROLE_" + user.getRole();
```

</details>

<details>
<summary>Erreur #username cannot be resolved</summary>

**Cause :** Le paramètre n'a pas @PathVariable ou @RequestParam

**Solution :**
```java
public String method(@PathVariable String username)
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
│   ├── HelloController.java          ← Modifié (@PreAuthorize)
│   ├── AuthController.java
│   ├── SecurityConfig.java           ← Modifié (@EnableMethodSecurity)
│   ├── JwtService.java
│   ├── JwtFilter.java
│   ├── User.java
│   ├── UserRepository.java
│   ├── DataInit.java
│   ├── RegisterRequest.java
│   ├── LoginRequest.java
│   ├── GlobalExceptionHandler.java   ← Modifié (403 handler)
│   ├── ErrorResponse.java
│   ├── NotFoundException.java
│   ├── UnauthorizedException.java
│   ├── ConflictException.java
│   └── ForbiddenException.java       ← Nouveau
└── src/main/resources/
    └── application.properties
```

</details>

---

## Annexe 2 : Fichier test-roles.http

<details>
<summary>Voir le fichier de test</summary>

```http
### =============================================
### Module 17 - Tests Rôles et Permissions
### =============================================

### -----------------------------------------
### Étape 1 : Login admin
### Copier le accessToken
### -----------------------------------------
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "admin123"
}

### -----------------------------------------
### Étape 2 : Login user
### Copier le accessToken
### -----------------------------------------
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "username": "user",
    "password": "user123"
}

### -----------------------------------------
### Test 1 : /public sans token
### Résultat attendu : 200 OK
### -----------------------------------------
GET http://localhost:8080/public

### -----------------------------------------
### Test 2 : /private sans token
### Résultat attendu : 403 Forbidden
### -----------------------------------------
GET http://localhost:8080/private

### -----------------------------------------
### Test 3 : /private avec token user
### Résultat attendu : 200 OK
### REMPLACER <TOKEN_USER>
### -----------------------------------------
GET http://localhost:8080/private
Authorization: Bearer <TOKEN_USER>

### -----------------------------------------
### Test 4 : /admin avec token user
### Résultat attendu : 403 Forbidden
### REMPLACER <TOKEN_USER>
### -----------------------------------------
GET http://localhost:8080/admin
Authorization: Bearer <TOKEN_USER>

### -----------------------------------------
### Test 5 : /admin avec token admin
### Résultat attendu : 200 OK
### REMPLACER <TOKEN_ADMIN>
### -----------------------------------------
GET http://localhost:8080/admin
Authorization: Bearer <TOKEN_ADMIN>

### -----------------------------------------
### Test 6 : /user avec token user
### Résultat attendu : 200 OK
### REMPLACER <TOKEN_USER>
### -----------------------------------------
GET http://localhost:8080/user
Authorization: Bearer <TOKEN_USER>

### -----------------------------------------
### Test 7 : /admin-or-user avec token user
### Résultat attendu : 200 OK
### REMPLACER <TOKEN_USER>
### -----------------------------------------
GET http://localhost:8080/admin-or-user
Authorization: Bearer <TOKEN_USER>

### -----------------------------------------
### Test 8 : /profile/user avec token user
### Résultat attendu : 200 OK (son propre profil)
### REMPLACER <TOKEN_USER>
### -----------------------------------------
GET http://localhost:8080/profile/user
Authorization: Bearer <TOKEN_USER>

### -----------------------------------------
### Test 9 : /profile/admin avec token user
### Résultat attendu : 403 Forbidden (pas son profil)
### REMPLACER <TOKEN_USER>
### -----------------------------------------
GET http://localhost:8080/profile/admin
Authorization: Bearer <TOKEN_USER>

### -----------------------------------------
### Test 10 : /profile/user avec token admin
### Résultat attendu : 200 OK (admin peut tout voir)
### REMPLACER <TOKEN_ADMIN>
### -----------------------------------------
GET http://localhost:8080/profile/user
Authorization: Bearer <TOKEN_ADMIN>
```

</details>

---

## Résumé

| Étape | Ce qu'on a fait |
|-------|-----------------|
| 1 | Activé @EnableMethodSecurity |
| 2 | Ajouté @PreAuthorize sur les endpoints |
| 3 | Géré l'erreur 403 |
| 4 | Testé tous les cas |
| 5 | Ajouté permissions fines (#username) |

---

## Prochaine étape

Module 18 : Logout et blacklist de tokens

