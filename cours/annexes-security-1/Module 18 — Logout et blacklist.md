# Module 18 — Logout et blacklist de tokens

## Prérequis

Tu as terminé le Module 17 (rôles et permissions).

---

## Le problème du logout avec JWT

| Avec sessions | Avec JWT |
|---------------|----------|
| Logout = supprimer la session | Le token reste valide |
| Serveur contrôle | Token auto-suffisant |
| Facile | Complexe |

---

## Les solutions possibles

| Solution | Complexité | Efficacité |
|----------|------------|------------|
| Réduire la durée du token | Simple | Moyenne |
| Blacklist en mémoire | Moyenne | Bonne |
| Blacklist en base | Complexe | Excellente |
| Blacklist avec Redis | Complexe | Excellente |

On va utiliser **Blacklist en mémoire** (simple et efficace pour une démo).

---

## Étape 1 : Créer le service de blacklist

### 1.1 Créer le fichier

`src/main/java/com/demo/securitydemo/TokenBlacklistService.java`

### 1.2 Copier ce code

```java
package com.demo.securitydemo;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class TokenBlacklistService {

    // Liste des tokens invalidés (en mémoire)
    private final Set<String> blacklist = new HashSet<>();

    // Ajouter un token à la blacklist
    public void blacklist(String token) {
        blacklist.add(token);
    }

    // Vérifier si un token est blacklisté
    public boolean isBlacklisted(String token) {
        return blacklist.contains(token);
    }

    // Nombre de tokens blacklistés (pour debug)
    public int size() {
        return blacklist.size();
    }
}
```

### 1.3 Ce que fait ce service

| Méthode | Usage |
|---------|-------|
| `blacklist(token)` | Invalider un token |
| `isBlacklisted(token)` | Vérifier si invalidé |
| `size()` | Compter les tokens invalidés |

---

## Étape 2 : Modifier JwtFilter

### 2.1 Ajouter la vérification de blacklist

```java
package com.demo.securitydemo;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TokenBlacklistService blacklistService;  // AJOUTER

    public JwtFilter(JwtService jwtService, 
                     UserRepository userRepository,
                     TokenBlacklistService blacklistService) {  // AJOUTER
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.blacklistService = blacklistService;  // AJOUTER
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // AJOUTER : Vérifier si le token est blacklisté
        if (blacklistService.isBlacklisted(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (jwtService.isValidAccessToken(token)) {
            String username = jwtService.extractUsername(token);

            User user = userRepository.findByUsername(username).orElse(null);

            if (user != null) {
                String role = "ROLE_" + user.getRole();

                var auth = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        List.of(new SimpleGrantedAuthority(role))
                );

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

### 2.2 Ce qui a changé

| Avant | Après |
|-------|-------|
| Token valide = OK | Token valide + pas blacklisté = OK |

---

## Étape 3 : Ajouter l'endpoint /auth/logout

### 3.1 Modifier AuthController

Ajouter cette méthode :

```java
@PostMapping("/logout")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        throw new UnauthorizedException("Token manquant");
    }

    String token = authHeader.substring(7);

    // Blacklister l'access token
    blacklistService.blacklist(token);

    return ResponseEntity.ok(Map.of(
            "message", "Déconnexion réussie",
            "tokensBlacklisted", blacklistService.size()
    ));
}
```

### 3.2 Ajouter l'injection du service

Au début de AuthController, ajouter :

```java
private final TokenBlacklistService blacklistService;

public AuthController(JwtService jwtService,
                      PasswordEncoder passwordEncoder,
                      UserRepository userRepository,
                      TokenBlacklistService blacklistService) {  // AJOUTER
    this.jwtService = jwtService;
    this.passwordEncoder = passwordEncoder;
    this.userRepository = userRepository;
    this.blacklistService = blacklistService;  // AJOUTER
}
```

### 3.3 Ajouter l'import

```java
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestHeader;
```

---

## Étape 4 : AuthController complet

<details>
<summary>Voir le fichier complet</summary>

```java
package com.demo.securitydemo;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenBlacklistService blacklistService;

    public AuthController(JwtService jwtService,
                          PasswordEncoder passwordEncoder,
                          UserRepository userRepository,
                          TokenBlacklistService blacklistService) {
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.blacklistService = blacklistService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {

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

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new NotFoundException("User '" + request.getUsername() + "' non trouvé"));

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

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Token manquant");
        }

        String token = authHeader.substring(7);
        blacklistService.blacklist(token);

        return ResponseEntity.ok(Map.of(
                "message", "Déconnexion réussie",
                "tokensBlacklisted", blacklistService.size()
        ));
    }
}
```

</details>

---

## Étape 5 : Tester

### 5.1 Relancer l'application

```bash
mvn spring-boot:run
```

### 5.2 Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Garder le `accessToken`.

### 5.3 Accéder à /private (doit marcher)

```bash
curl http://localhost:8080/private \
  -H "Authorization: Bearer <TOKEN>"
```

Résultat : `200 OK`

### 5.4 Logout

```bash
curl -X POST http://localhost:8080/auth/logout \
  -H "Authorization: Bearer <TOKEN>"
```

Résultat :
```json
{
  "message": "Déconnexion réussie",
  "tokensBlacklisted": 1
}
```

### 5.5 Accéder à /private (doit échouer)

```bash
curl http://localhost:8080/private \
  -H "Authorization: Bearer <TOKEN>"
```

Résultat : `403 Forbidden`

Le token est maintenant invalide.

---

## Étape 6 : Blacklister aussi le refresh token

### 6.1 Modifier /logout pour accepter le refresh token

```java
@PostMapping("/logout")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<?> logout(
        @RequestHeader("Authorization") String authHeader,
        @RequestBody(required = false) Map<String, String> body) {

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        throw new UnauthorizedException("Token manquant");
    }

    // Blacklister l'access token
    String accessToken = authHeader.substring(7);
    blacklistService.blacklist(accessToken);

    // Blacklister le refresh token si fourni
    if (body != null && body.containsKey("refreshToken")) {
        String refreshToken = body.get("refreshToken");
        blacklistService.blacklist(refreshToken);
    }

    return ResponseEntity.ok(Map.of(
            "message", "Déconnexion réussie",
            "tokensBlacklisted", blacklistService.size()
    ));
}
```

### 6.2 Modifier /refresh pour vérifier la blacklist

Dans la méthode refresh() de AuthController, ajouter :

```java
@PostMapping("/refresh")
public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
    String refreshToken = request.get("refreshToken");

    if (refreshToken == null || refreshToken.isBlank()) {
        throw new UnauthorizedException("Refresh token manquant");
    }

    // AJOUTER : Vérifier si blacklisté
    if (blacklistService.isBlacklisted(refreshToken)) {
        throw new UnauthorizedException("Refresh token révoqué");
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
```

---

## Tableau des tests

| # | Action | Résultat |
|---|--------|----------|
| 1 | Login | accessToken + refreshToken |
| 2 | /private avec token | 200 OK |
| 3 | /auth/logout | "Déconnexion réussie" |
| 4 | /private avec même token | 403 Forbidden |
| 5 | /auth/refresh avec refreshToken | 401 (si blacklisté) |

---

## Limitation de la blacklist en mémoire

| Avantage | Inconvénient |
|----------|--------------|
| Simple | Perdu au redémarrage |
| Rapide | Pas partagé entre serveurs |
| Pas de dépendance | Mémoire croissante |

Pour la production, utiliser Redis ou la base de données.

---

## Problèmes courants

<details>
<summary>Le logout ne fonctionne pas</summary>

**Cause :** Le token n'est pas ajouté à la blacklist

**Solution :**
Vérifier que `blacklistService.blacklist(token)` est appelé

</details>

<details>
<summary>Le token est toujours accepté après logout</summary>

**Cause :** JwtFilter ne vérifie pas la blacklist

**Solution :**
Ajouter dans JwtFilter :
```java
if (blacklistService.isBlacklisted(token)) {
    filterChain.doFilter(request, response);
    return;
}
```

</details>

<details>
<summary>La blacklist est vide après redémarrage</summary>

**Cause :** Stockage en mémoire

**Solution :**
Normal pour cette démo. Pour la production, utiliser une base de données ou Redis.

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
│   ├── AuthController.java            ← Modifié (/logout)
│   ├── SecurityConfig.java
│   ├── JwtService.java
│   ├── JwtFilter.java                 ← Modifié (blacklist check)
│   ├── User.java
│   ├── UserRepository.java
│   ├── DataInit.java
│   ├── RegisterRequest.java
│   ├── LoginRequest.java
│   ├── GlobalExceptionHandler.java
│   ├── ErrorResponse.java
│   ├── NotFoundException.java
│   ├── UnauthorizedException.java
│   ├── ConflictException.java
│   ├── ForbiddenException.java
│   └── TokenBlacklistService.java     ← Nouveau
└── src/main/resources/
    └── application.properties
```

</details>

---

## Annexe 2 : Fichier test-logout.http

<details>
<summary>Voir le fichier de test</summary>

```http
### =============================================
### Module 18 - Tests Logout et Blacklist
### =============================================

### -----------------------------------------
### Étape 1 : Login
### Copier accessToken et refreshToken
### -----------------------------------------
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "admin123"
}

### -----------------------------------------
### Test 1 : /private avec token (avant logout)
### Résultat attendu : 200 OK
### REMPLACER <ACCESS_TOKEN>
### -----------------------------------------
GET http://localhost:8080/private
Authorization: Bearer <ACCESS_TOKEN>

### -----------------------------------------
### Test 2 : Logout (juste access token)
### Résultat attendu : 200 OK + message
### REMPLACER <ACCESS_TOKEN>
### -----------------------------------------
POST http://localhost:8080/auth/logout
Authorization: Bearer <ACCESS_TOKEN>

### -----------------------------------------
### Test 3 : /private avec token (après logout)
### Résultat attendu : 403 Forbidden
### REMPLACER <ACCESS_TOKEN>
### -----------------------------------------
GET http://localhost:8080/private
Authorization: Bearer <ACCESS_TOKEN>

### -----------------------------------------
### Test 4 : Login à nouveau
### Copier les nouveaux tokens
### -----------------------------------------
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "admin123"
}

### -----------------------------------------
### Test 5 : Logout avec refresh token
### REMPLACER <ACCESS_TOKEN> et <REFRESH_TOKEN>
### -----------------------------------------
POST http://localhost:8080/auth/logout
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json

{
    "refreshToken": "<REFRESH_TOKEN>"
}

### -----------------------------------------
### Test 6 : Refresh avec token blacklisté
### Résultat attendu : 401 Unauthorized
### REMPLACER <REFRESH_TOKEN>
### -----------------------------------------
POST http://localhost:8080/auth/refresh
Content-Type: application/json

{
    "refreshToken": "<REFRESH_TOKEN>"
}
```

</details>

---

## Résumé

| Étape | Ce qu'on a fait |
|-------|-----------------|
| 1 | Créé TokenBlacklistService |
| 2 | Modifié JwtFilter (vérifie blacklist) |
| 3 | Ajouté /auth/logout |
| 4 | Vu AuthController complet |
| 5 | Testé le flux complet |
| 6 | Ajouté blacklist du refresh token |

---

## Flux complet du logout

```
┌─────────────────────────────────────────────────────────┐
│                    FLUX LOGOUT                           │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  1. LOGIN                                                │
│     POST /auth/login                                     │
│     ↓                                                    │
│     accessToken + refreshToken                           │
│                                                          │
│  2. UTILISATION                                          │
│     GET /private                                         │
│     Header: Authorization: Bearer <accessToken>          │
│     ↓                                                    │
│     JwtFilter vérifie :                                  │
│       - Token valide ? OUI                               │
│       - Blacklisté ? NON                                 │
│     ↓                                                    │
│     200 OK                                               │
│                                                          │
│  3. LOGOUT                                               │
│     POST /auth/logout                                    │
│     Header: Authorization: Bearer <accessToken>          │
│     Body: { "refreshToken": "<refreshToken>" }           │
│     ↓                                                    │
│     Les 2 tokens ajoutés à la blacklist                  │
│                                                          │
│  4. APRÈS LOGOUT                                         │
│     GET /private                                         │
│     Header: Authorization: Bearer <accessToken>          │
│     ↓                                                    │
│     JwtFilter vérifie :                                  │
│       - Token valide ? OUI                               │
│       - Blacklisté ? OUI                                 │
│     ↓                                                    │
│     403 Forbidden                                        │
│                                                          │
│  5. REFRESH APRÈS LOGOUT                                 │
│     POST /auth/refresh                                   │
│     Body: { "refreshToken": "<refreshToken>" }           │
│     ↓                                                    │
│     Vérifie blacklist → BLACKLISTÉ                       │
│     ↓                                                    │
│     401 Unauthorized                                     │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

---

## Félicitations

Tu as terminé les 8 modules de la formation Spring Security.

| Module | Sujet |
|--------|-------|
| 11 | Demo minimaliste |
| 12 | JWT étape par étape |
| 13 | Base de données |
| 14 | Refresh Token |
| 15 | Validation |
| 16 | Gestion des erreurs |
| 17 | Rôles et permissions |
| 18 | Logout et blacklist |

Tu as maintenant une API sécurisée complète.

