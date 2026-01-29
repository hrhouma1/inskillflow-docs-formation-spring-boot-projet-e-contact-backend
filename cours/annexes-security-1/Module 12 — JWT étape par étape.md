# Module 12 — Ajouter JWT étape par étape

## Prérequis

Tu as terminé le Module 11 (projet security-demo qui fonctionne).

---

## Ce qu'on va faire

Transformer le projet du Module 11 pour utiliser JWT au lieu du formulaire de login.

| Avant (Module 11) | Après (Module 12) |
|-------------------|-------------------|
| Formulaire de login | Pas de formulaire |
| Session côté serveur | Stateless |
| Cookie de session | Token JWT |

---

## Étape 1 : Ajouter les dépendances JWT

### 1.1 Ouvrir pom.xml

### 1.2 Ajouter ces dépendances

```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
</dependency>
```

### 1.3 Recharger Maven

Clic droit sur pom.xml > Maven > Reload Project

### 1.4 Tester

L'application doit encore démarrer :

```bash
mvn spring-boot:run
```

<details>
<summary>Ça ne démarre pas ?</summary>

**Vérifier :**
1. Les 3 dépendances sont bien ajoutées
2. La version est 0.11.5 (pas 0.12.x qui a des changements)
3. Maven a bien rechargé (clic droit > Maven > Reload)

</details>

---

## Étape 2 : Créer la clé secrète

### 2.1 Ouvrir application.properties

Créer le fichier `src/main/resources/application.properties` s'il n'existe pas.

### 2.2 Ajouter cette ligne

```properties
jwt.secret=MaCleSecreteTresLongueAuMoins256BitsMinimumPourHMACSHA256
```

### 2.3 C'est quoi cette clé ?

| Question | Réponse |
|----------|---------|
| À quoi ça sert ? | Signer les tokens JWT |
| Pourquoi si longue ? | Sécurité (256 bits minimum) |
| Qui la connaît ? | Seulement le serveur |
| Si quelqu'un la vole ? | Il peut créer de faux tokens |

---

## Étape 3 : Créer JwtService

### 3.1 Créer le fichier

`src/main/java/com/demo/securitydemo/JwtService.java`

### 3.2 Copier ce code

```java
package com.demo.securitydemo;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    // Générer un token
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 heure
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extraire le username du token
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Vérifier si le token est valide
    public boolean isValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
```

### 3.3 Ce que fait chaque méthode

| Méthode | Ce qu'elle fait |
|---------|-----------------|
| `generateToken(username)` | Crée un nouveau token JWT |
| `extractUsername(token)` | Lit le username dans le token |
| `isValid(token)` | Vérifie si le token est bon |

### 3.4 Tester

L'application doit encore démarrer :

```bash
mvn spring-boot:run
```

<details>
<summary>Erreur : Could not resolve placeholder 'jwt.secret'</summary>

**Cause :** Le fichier application.properties n'est pas lu

**Solution :**
1. Vérifier que le fichier est dans `src/main/resources/`
2. Vérifier le nom exact : `application.properties`
3. Relancer l'application

</details>

---

## Étape 4 : Créer un endpoint de login

### 4.1 Créer le fichier

`src/main/java/com/demo/securitydemo/AuthController.java`

### 4.2 Copier ce code

```java
package com.demo.securitydemo;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        // Vérification simple (en vrai, on utilise UserDetailsService)
        if ("user".equals(username) && "user123".equals(password)) {
            String token = jwtService.generateToken(username);
            return Map.of("token", token);
        }
        if ("admin".equals(username) && "admin123".equals(password)) {
            String token = jwtService.generateToken(username);
            return Map.of("token", token);
        }

        throw new RuntimeException("Bad credentials");
    }
}
```

### 4.3 Ce que fait ce code

1. Reçoit username + password en JSON
2. Vérifie si c'est correct
3. Si oui : génère un token et le renvoie
4. Si non : erreur

### 4.4 Tester

L'application doit démarrer, mais /auth/login va demander un login (on va corriger ça).

---

## Étape 5 : Autoriser /auth/login sans authentification

### 5.1 Modifier SecurityConfig.java

Remplacer tout le contenu par :

```java
package com.demo.securitydemo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/public").permitAll()
                .anyRequest().authenticated()
            );
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 5.2 Ce qui a changé

| Ligne | Effet |
|-------|-------|
| `.csrf(csrf -> csrf.disable())` | Désactive CSRF (pas besoin avec JWT) |
| `.sessionManagement(...)` | Pas de session (stateless) |
| `.requestMatchers("/auth/**").permitAll()` | /auth/login accessible sans token |
| Plus de `.formLogin()` | Pas de formulaire de login |

### 5.3 Tester le login

Relancer l'application :

```bash
mvn spring-boot:run
```

Tester avec curl :

```bash
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d "{\"username\":\"user\",\"password\":\"user123\"}"
```

**Résultat attendu :**

```json
{"token":"eyJhbGciOiJIUzI1NiJ9..."}
```

<details>
<summary>Erreur 403 ?</summary>

**Cause :** CSRF n'est pas désactivé

**Solution :**
Vérifier que tu as bien `.csrf(csrf -> csrf.disable())`

</details>

<details>
<summary>Erreur 415 Unsupported Media Type ?</summary>

**Cause :** Le Content-Type n'est pas envoyé

**Solution :**
Ajouter `-H "Content-Type: application/json"` dans curl

</details>

---

## Étape 6 : Créer le filtre JWT

### 6.1 Créer le fichier

`src/main/java/com/demo/securitydemo/JwtFilter.java`

### 6.2 Copier ce code

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

    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Lire le header Authorization
        String authHeader = request.getHeader("Authorization");

        // 2. Vérifier qu'il commence par "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraire le token
        String token = authHeader.substring(7);

        // 4. Vérifier si le token est valide
        if (jwtService.isValid(token)) {
            // 5. Extraire le username
            String username = jwtService.extractUsername(token);

            // 6. Définir le rôle (simplifié)
            String role = "admin".equals(username) ? "ROLE_ADMIN" : "ROLE_USER";

            // 7. Créer l'authentification
            var auth = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    List.of(new SimpleGrantedAuthority(role))
            );

            // 8. Mettre dans le contexte
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // 9. Continuer
        filterChain.doFilter(request, response);
    }
}
```

### 6.3 Ce que fait chaque étape

| Étape | Ce qu'elle fait |
|-------|-----------------|
| 1 | Lit le header `Authorization` |
| 2 | Vérifie que ça commence par `Bearer ` |
| 3 | Enlève "Bearer " pour avoir juste le token |
| 4 | Vérifie que le token est valide |
| 5 | Lit le username dans le token |
| 6 | Détermine le rôle (admin ou user) |
| 7 | Crée un objet Authentication |
| 8 | Le met dans le SecurityContext |
| 9 | Passe au filtre suivant |

---

## Étape 7 : Ajouter le filtre dans SecurityConfig

### 7.1 Modifier SecurityConfig.java

```java
package com.demo.securitydemo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/public").permitAll()
                .requestMatchers("/admin").hasRole("ADMIN")
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

### 7.2 Ce qui a changé

| Ligne | Effet |
|-------|-------|
| `private final JwtFilter jwtFilter` | Injecte notre filtre |
| `.addFilterBefore(...)` | Ajoute le filtre JWT avant le filtre standard |
| `.requestMatchers("/admin").hasRole("ADMIN")` | /admin réservé aux admins |

---

## Étape 8 : Tester tout le flux

### 8.1 Relancer l'application

```bash
mvn spring-boot:run
```

### 8.2 Test 1 : /public sans token

```bash
curl http://localhost:8080/public
```

**Résultat :** `Ceci est PUBLIC - tout le monde peut voir`

### 8.3 Test 2 : /private sans token

```bash
curl http://localhost:8080/private
```

**Résultat :** Erreur 403

### 8.4 Test 3 : Se connecter et obtenir un token

```bash
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d "{\"username\":\"user\",\"password\":\"user123\"}"
```

**Résultat :** `{"token":"eyJhbG..."}`

Copier le token !

### 8.5 Test 4 : /private avec token

```bash
curl http://localhost:8080/private -H "Authorization: Bearer COLLER_LE_TOKEN_ICI"
```

**Résultat :** `Ceci est PRIVE - il faut être connecté`

### 8.6 Test 5 : /admin avec token user

```bash
curl http://localhost:8080/admin -H "Authorization: Bearer COLLER_LE_TOKEN_USER_ICI"
```

**Résultat :** Erreur 403 (user n'est pas admin)

### 8.7 Test 6 : /admin avec token admin

D'abord, obtenir un token admin :

```bash
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```

Puis tester /admin :

```bash
curl http://localhost:8080/admin -H "Authorization: Bearer COLLER_LE_TOKEN_ADMIN_ICI"
```

**Résultat :** `Ceci est ADMIN - il faut être admin`

---

## Tableau récapitulatif des tests

| # | Endpoint | Token | Résultat |
|---|----------|-------|----------|
| 1 | /public | Aucun | 200 OK |
| 2 | /private | Aucun | 403 |
| 3 | /private | Token user | 200 OK |
| 4 | /private | Token admin | 200 OK |
| 5 | /admin | Aucun | 403 |
| 6 | /admin | Token user | 403 |
| 7 | /admin | Token admin | 200 OK |

---

## Problèmes courants

<details>
<summary>403 sur /auth/login</summary>

**Cause :** CSRF activé ou /auth/** pas autorisé

**Solution :**

Vérifier dans SecurityConfig :
```java
.csrf(csrf -> csrf.disable())
.requestMatchers("/auth/**").permitAll()
```

</details>

<details>
<summary>Le token ne fonctionne pas</summary>

**Vérifier :**

1. Le header est `Authorization` (pas `Authorisation`)
2. Le format est `Bearer TOKEN` (avec l'espace)
3. Le token n'est pas expiré (1h par défaut)

**Tester le format :**
```bash
curl http://localhost:8080/private -H "Authorization: Bearer eyJhbG..."
```

</details>

<details>
<summary>403 même avec un bon token</summary>

**Cause :** Le filtre JWT n'est pas appelé

**Solution :**

1. Vérifier que JwtFilter a `@Component`
2. Vérifier que SecurityConfig a `.addFilterBefore(jwtFilter, ...)`
3. Ajouter un log dans JwtFilter pour voir s'il est appelé

</details>

<details>
<summary>Token admin donne 403 sur /admin</summary>

**Cause :** Le rôle n'est pas bien défini

**Vérifier dans JwtFilter :**
```java
String role = "admin".equals(username) ? "ROLE_ADMIN" : "ROLE_USER";
```

Le préfixe `ROLE_` est obligatoire !

</details>

---

## Fichiers finaux

### application.properties

```properties
jwt.secret=MaCleSecreteTresLongueAuMoins256BitsMinimumPourHMACSHA256
```

### JwtService.java

```java
package com.demo.securitydemo;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean isValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
```

### JwtFilter.java

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

    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
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

        if (jwtService.isValid(token)) {
            String username = jwtService.extractUsername(token);
            String role = "admin".equals(username) ? "ROLE_ADMIN" : "ROLE_USER";

            var auth = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    List.of(new SimpleGrantedAuthority(role))
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
```

### AuthController.java

```java
package com.demo.securitydemo;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if ("user".equals(username) && "user123".equals(password)) {
            String token = jwtService.generateToken(username);
            return Map.of("token", token);
        }
        if ("admin".equals(username) && "admin123".equals(password)) {
            String token = jwtService.generateToken(username);
            return Map.of("token", token);
        }

        throw new RuntimeException("Bad credentials");
    }
}
```

### SecurityConfig.java

```java
package com.demo.securitydemo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/public").permitAll()
                .requestMatchers("/admin").hasRole("ADMIN")
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

### HelloController.java

Pas de changement (garde celui du Module 11).

---

## Résumé

| Étape | Ce qu'on a fait |
|-------|-----------------|
| 1 | Ajouté les dépendances JWT |
| 2 | Créé la clé secrète |
| 3 | Créé JwtService (génère et valide les tokens) |
| 4 | Créé AuthController (endpoint /auth/login) |
| 5 | Autorisé /auth/login sans token |
| 6 | Créé JwtFilter (lit le token à chaque requête) |
| 7 | Ajouté le filtre dans SecurityConfig |
| 8 | Testé tout le flux |

---

## Prochaine étape

- Module 13 : Ajouter une base de données pour les utilisateurs
- Module 14 : Refresh token

---

## Annexe 1 : Structure complète du projet

<details>
<summary>Voir la structure du projet</summary>

### Arborescence des fichiers

```
security-demo/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── demo/
│       │           └── securitydemo/
│       │               ├── SecurityDemoApplication.java
│       │               ├── HelloController.java
│       │               ├── AuthController.java
│       │               ├── SecurityConfig.java
│       │               ├── JwtService.java
│       │               └── JwtFilter.java
│       └── resources/
│           └── application.properties
```

### Tous les fichiers

---

#### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <groupId>com.demo</groupId>
    <artifactId>security-demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>security-demo</name>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        
        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.11.5</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.11.5</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.11.5</version>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

#### application.properties

```properties
jwt.secret=MaCleSecreteTresLongueAuMoins256BitsMinimumPourHMACSHA256
```

---

#### SecurityDemoApplication.java

```java
package com.demo.securitydemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SecurityDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityDemoApplication.class, args);
    }
}
```

---

#### HelloController.java

```java
package com.demo.securitydemo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "Ceci est PUBLIC - tout le monde peut voir";
    }

    @GetMapping("/private")
    public String privateEndpoint() {
        return "Ceci est PRIVE - il faut être connecté";
    }

    @GetMapping("/admin")
    public String adminEndpoint() {
        return "Ceci est ADMIN - il faut être admin";
    }
}
```

---

#### AuthController.java

```java
package com.demo.securitydemo;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if ("user".equals(username) && "user123".equals(password)) {
            String token = jwtService.generateToken(username);
            return Map.of("token", token);
        }
        if ("admin".equals(username) && "admin123".equals(password)) {
            String token = jwtService.generateToken(username);
            return Map.of("token", token);
        }

        throw new RuntimeException("Bad credentials");
    }
}
```

---

#### JwtService.java

```java
package com.demo.securitydemo;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean isValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
```

---

#### JwtFilter.java

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

    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
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

        if (jwtService.isValid(token)) {
            String username = jwtService.extractUsername(token);
            String role = "admin".equals(username) ? "ROLE_ADMIN" : "ROLE_USER";

            var auth = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    List.of(new SimpleGrantedAuthority(role))
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
```

---

#### SecurityConfig.java

```java
package com.demo.securitydemo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/public").permitAll()
                .requestMatchers("/admin").hasRole("ADMIN")
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

</details>

---

## Annexe 2 : Comparaison Module 11 vs Module 12

<details>
<summary>Voir les différences</summary>

### Fichiers

| Fichier | Module 11 | Module 12 |
|---------|-----------|-----------|
| pom.xml | Web + Security | Web + Security + JWT |
| application.properties | Vide | jwt.secret=... |
| SecurityDemoApplication.java | Identique | Identique |
| HelloController.java | Identique | Identique |
| SecurityConfig.java | formLogin | stateless + JwtFilter |
| AuthController.java | N'existe pas | Créé |
| JwtService.java | N'existe pas | Créé |
| JwtFilter.java | N'existe pas | Créé |

### SecurityConfig.java : Différences

| Module 11 | Module 12 |
|-----------|-----------|
| `.formLogin(form -> form.permitAll())` | Supprimé |
| `.logout(logout -> logout.permitAll())` | Supprimé |
| - | `.csrf(csrf -> csrf.disable())` |
| - | `.sessionManagement(session -> STATELESS)` |
| - | `.addFilterBefore(jwtFilter, ...)` |
| `UserDetailsService` bean | Supprimé |

### Comment on se connecte

| Module 11 | Module 12 |
|-----------|-----------|
| Formulaire HTML | POST /auth/login avec JSON |
| Cookie de session | Header Authorization: Bearer token |
| Serveur garde la session | Serveur ne garde rien (stateless) |

### Commandes de test

**Module 11 :**
```bash
# Dans le navigateur
http://localhost:8080/private
# → Page de login
# → Entrer user/user123
# → OK
```

**Module 12 :**
```bash
# Étape 1 : Obtenir le token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"user123"}'

# Étape 2 : Utiliser le token
curl http://localhost:8080/private \
  -H "Authorization: Bearer eyJhbG..."
```

</details>

---

## Annexe 3 : Tester avec REST Client (.http)

<details>
<summary>Voir le fichier de test</summary>

### Créer le fichier de test

Créer un fichier `test-jwt.http` à la racine du projet :

```
security-demo/
├── test-jwt.http      ← Créer ce fichier
├── pom.xml
└── src/
```

### Contenu du fichier test-jwt.http

```http
### =============================================
### Module 12 - Tests Spring Security avec JWT
### =============================================

### -----------------------------------------
### Test 1 : Accéder à /public (sans token)
### Résultat attendu : 200 OK
### -----------------------------------------
GET http://localhost:8080/public

### -----------------------------------------
### Test 2 : Accéder à /private (sans token)
### Résultat attendu : 403 Forbidden
### -----------------------------------------
GET http://localhost:8080/private

### -----------------------------------------
### Test 3 : Accéder à /admin (sans token)
### Résultat attendu : 403 Forbidden
### -----------------------------------------
GET http://localhost:8080/admin

### -----------------------------------------
### Test 4 : Login avec USER
### Résultat attendu : 200 OK + token
### COPIER LE TOKEN POUR LES TESTS SUIVANTS
### -----------------------------------------
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "username": "user",
    "password": "user123"
}

### -----------------------------------------
### Test 5 : Login avec ADMIN
### Résultat attendu : 200 OK + token
### COPIER LE TOKEN POUR LES TESTS SUIVANTS
### -----------------------------------------
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "admin123"
}

### -----------------------------------------
### Test 6 : Login avec mauvais mot de passe
### Résultat attendu : 500 Internal Server Error
### -----------------------------------------
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "username": "user",
    "password": "mauvais"
}

### -----------------------------------------
### Test 7 : Accéder à /private avec token USER
### Résultat attendu : 200 OK
### REMPLACER {{token_user}} par le vrai token
### -----------------------------------------
GET http://localhost:8080/private
Authorization: Bearer {{token_user}}

### -----------------------------------------
### Test 8 : Accéder à /admin avec token USER
### Résultat attendu : 403 Forbidden
### -----------------------------------------
GET http://localhost:8080/admin
Authorization: Bearer {{token_user}}

### -----------------------------------------
### Test 9 : Accéder à /admin avec token ADMIN
### Résultat attendu : 200 OK
### REMPLACER {{token_admin}} par le vrai token
### -----------------------------------------
GET http://localhost:8080/admin
Authorization: Bearer {{token_admin}}

### -----------------------------------------
### Test 10 : Token invalide
### Résultat attendu : 403 Forbidden
### -----------------------------------------
GET http://localhost:8080/private
Authorization: Bearer token_bidon_invalide

### -----------------------------------------
### Test 11 : Token mal formé (sans Bearer)
### Résultat attendu : 403 Forbidden
### -----------------------------------------
GET http://localhost:8080/private
Authorization: eyJhbGciOiJIUzI1NiJ9.xxx
```

### Comment utiliser

**Étape 1 :** Exécuter le Test 4 (login user)

**Étape 2 :** Copier le token de la réponse :
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0..."
}
```

**Étape 3 :** Remplacer `{{token_user}}` par le token copié dans les tests 7 et 8

**Étape 4 :** Exécuter le Test 5 (login admin) et copier le token

**Étape 5 :** Remplacer `{{token_admin}}` par le token admin dans le test 9

### Tableau des résultats attendus

| Test | Endpoint | Token | Résultat |
|------|----------|-------|----------|
| 1 | /public | Aucun | 200 OK |
| 2 | /private | Aucun | 403 Forbidden |
| 3 | /admin | Aucun | 403 Forbidden |
| 4 | /auth/login | user/user123 | 200 + token |
| 5 | /auth/login | admin/admin123 | 200 + token |
| 6 | /auth/login | user/mauvais | 500 Error |
| 7 | /private | Token user | 200 OK |
| 8 | /admin | Token user | 403 Forbidden |
| 9 | /admin | Token admin | 200 OK |
| 10 | /private | Token invalide | 403 Forbidden |
| 11 | /private | Sans Bearer | 403 Forbidden |

### Version avec variables (VS Code REST Client)

Créer un fichier `test-jwt-vars.http` :

```http
### Variables
@baseUrl = http://localhost:8080
@token_user = 
@token_admin = 

### Login USER - Copier le token dans @token_user
POST {{baseUrl}}/auth/login
Content-Type: application/json

{
    "username": "user",
    "password": "user123"
}

### Login ADMIN - Copier le token dans @token_admin
POST {{baseUrl}}/auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "admin123"
}

### Test /public
GET {{baseUrl}}/public

### Test /private avec token user
GET {{baseUrl}}/private
Authorization: Bearer {{token_user}}

### Test /admin avec token user (403)
GET {{baseUrl}}/admin
Authorization: Bearer {{token_user}}

### Test /admin avec token admin (200)
GET {{baseUrl}}/admin
Authorization: Bearer {{token_admin}}
```

**Comment utiliser les variables :**
1. Exécuter le login
2. Copier le token de la réponse
3. Coller dans `@token_user = ` en haut du fichier
4. Exécuter les autres tests

</details>

