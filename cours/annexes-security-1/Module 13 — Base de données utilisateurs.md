# Module 13 — Ajouter une base de données pour les utilisateurs

## Prérequis

Tu as terminé le Module 12 (projet avec JWT qui fonctionne).

---

## Ce qu'on va faire

Remplacer les utilisateurs en dur dans le code par une vraie base de données.

| Avant (Module 12) | Après (Module 13) |
|-------------------|-------------------|
| Users dans le code | Users dans la base H2 |
| 2 users fixes | Users dynamiques |
| Pas d'inscription | Endpoint /auth/register |

---

## Exercice pratique : Trouver et corriger les erreurs

### Objectif

Tu vas partir d'un projet **avec des erreurs volontaires** et tu dois les trouver et les corriger.

### Les deux dossiers

| Dossier | Contenu |
|---------|---------|
| `exercice-security-demo-module-13/` | Projet avec des problèmes à corriger |
| `solution-security-demo-module-13/` | Solution complète et fonctionnelle |

### Comment faire l'exercice

1. **Ouvrir le dossier** `exercice-security-demo-module-13/`

2. **Essayer de lancer** :
```bash
cd exercice-security-demo-module-13
mvn spring-boot:run
```

3. **Observer les erreurs** dans la console

4. **Trouver les problèmes** dans les fichiers :
   - `pom.xml`
   - `User.java`
   - `SecurityConfig.java`
   - `application.properties`

5. **Corriger les erreurs** une par une

6. **Tester** jusqu'à ce que l'application démarre

7. **Vérifier** que :
   - `http://localhost:8081/public` fonctionne
   - `http://localhost:8081/h2-console` fonctionne
   - `POST /auth/login` fonctionne

### Indices

<details>
<summary>Indice 1 : pom.xml</summary>

Vérifie la version de Spring Boot. Est-ce qu'elle existe vraiment ?

</details>

<details>
<summary>Indice 2 : pom.xml</summary>

Vérifie les noms des dépendances. Est-ce que `spring-boot-starter-webmvc` existe ?

</details>

<details>
<summary>Indice 3 : User.java</summary>

Regarde attentivement les imports. Il y a une erreur de syntaxe.

</details>

<details>
<summary>Indice 4 : SecurityConfig.java</summary>

CSRF est-il correctement configuré pour permettre les requêtes POST ?

</details>

<details>
<summary>Indice 5 : application.properties</summary>

Si tu as une erreur "Request header is too large", il manque une configuration.

</details>

### Solution

Si tu es bloqué, regarde le dossier `solution-security-demo-module-13/` pour voir la solution complète.

La section **"Problèmes courants et solutions"** plus bas dans ce module explique chaque erreur en détail.

---

## Étape 1 : Ajouter les dépendances JPA + H2

### 1.1 Ouvrir pom.xml

### 1.2 Ajouter ces dépendances

```xml
<!-- JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- H2 Database -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 1.3 Recharger Maven

Clic droit sur pom.xml > Maven > Reload Project

### 1.4 Tester

```bash
mvn spring-boot:run
```

L'application doit démarrer (avec des erreurs de config, c'est normal).

<details>
<summary>Erreur au démarrage ?</summary>

C'est normal si tu vois des erreurs liées à JPA. On va configurer la base dans l'étape suivante.

</details>

---

## Étape 2 : Configurer la base de données

### 2.1 Modifier application.properties

```properties
# JWT
jwt.secret=MaCleSecreteTresLongueAuMoins256BitsMinimumPourHMACSHA256

# H2 Database
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

### 2.2 Ce que fait chaque ligne

| Ligne | Effet |
|-------|-------|
| `jdbc:h2:mem:testdb` | Base en mémoire (reset au redémarrage) |
| `ddl-auto=create-drop` | Crée les tables au démarrage |
| `show-sql=true` | Affiche les requêtes SQL dans la console |
| `h2.console.enabled=true` | Active la console web H2 |

### 2.3 Tester

```bash
mvn spring-boot:run
```

Ouvrir : `http://localhost:8080/h2-console`

<details>
<summary>403 sur /h2-console ?</summary>

On va autoriser /h2-console dans SecurityConfig plus tard.

</details>

---

## Étape 3 : Créer l'entité User

### 3.1 Créer le fichier

`src/main/java/com/demo/securitydemo/User.java`

### 3.2 Copier ce code

```java
package com.demo.securitydemo;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    // Constructeurs
    public User() {}

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
```

### 3.3 Ce que fait chaque annotation

| Annotation | Effet |
|------------|-------|
| `@Entity` | Dit que c'est une table |
| `@Table(name = "users")` | Nom de la table |
| `@Id` | Clé primaire |
| `@GeneratedValue` | Auto-incrément |
| `@Column(unique = true)` | Pas de doublons |

---

## Étape 4 : Créer le Repository

### 4.1 Créer le fichier

`src/main/java/com/demo/securitydemo/UserRepository.java`

### 4.2 Copier ce code

```java
package com.demo.securitydemo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
```

### 4.3 Ce que fait ce code

| Méthode | Effet |
|---------|-------|
| `findByUsername` | Cherche un user par son username |
| `existsByUsername` | Vérifie si le username existe déjà |
| `save()` | Héritée de JpaRepository |
| `findAll()` | Héritée de JpaRepository |

---

## Étape 5 : Modifier AuthController

### 5.1 Remplacer le contenu de AuthController.java

```java
package com.demo.securitydemo;

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
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        // Vérifier si le username existe déjà
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username déjà utilisé"));
        }

        // Créer le user
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");

        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "User créé avec succès"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        // Chercher le user
        User user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "User non trouvé"));
        }

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Mot de passe incorrect"));
        }

        // Générer le token
        String token = jwtService.generateToken(username);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", username,
                "role", user.getRole()
        ));
    }
}
```

### 5.2 Ce qui a changé

| Avant | Après |
|-------|-------|
| Users en dur dans le code | Users dans la base |
| Pas de /register | Endpoint /register |
| `throw new RuntimeException` | `ResponseEntity` avec messages |

---

## Étape 6 : Modifier JwtFilter pour lire le rôle

### 6.1 Remplacer le contenu de JwtFilter.java

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

    public JwtFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
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

            // Chercher le user dans la base
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

### 6.2 Ce qui a changé

| Avant | Après |
|-------|-------|
| Rôle basé sur le username | Rôle lu depuis la base |
| `"admin".equals(username)` | `user.getRole()` |

---

## Étape 7 : Modifier SecurityConfig pour /h2-console

### 7.1 Remplacer le contenu de SecurityConfig.java

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
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/public").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
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
| `.headers(headers -> headers.frameOptions(...))` | Permet les iframes (pour H2 console) |
| `.requestMatchers("/h2-console/**").permitAll()` | Autorise la console H2 |

---

## Étape 8 : Créer un admin au démarrage

### 8.1 Créer le fichier

`src/main/java/com/demo/securitydemo/DataInit.java`

### 8.2 Copier ce code

```java
package com.demo.securitydemo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInit implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInit(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Créer un admin si il n'existe pas
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            userRepository.save(admin);
            System.out.println("Admin créé : admin / admin123");
        }

        // Créer un user si il n'existe pas
        if (!userRepository.existsByUsername("user")) {
            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole("USER");
            userRepository.save(user);
            System.out.println("User créé : user / user123");
        }
    }
}
```

### 8.3 Ce que fait ce code

Au démarrage de l'application :
1. Vérifie si "admin" existe
2. Si non, crée l'admin avec mot de passe "admin123"
3. Vérifie si "user" existe
4. Si non, crée le user avec mot de passe "user123"

---

## Étape 9 : Tester

### 9.1 Relancer l'application

```bash
mvn spring-boot:run
```

Tu dois voir dans la console :
```
Admin créé : admin / admin123
User créé : user / user123
```

### 9.2 Tester la console H2

Ouvrir dans le navigateur : `http://localhost:8080/h2-console`

**IMPORTANT** : Pas besoin de token JWT ici. La console H2 est en `permitAll()`.

La page de connexion H2 va s'afficher. Ce n'est **PAS** une connexion Spring Security, c'est une connexion à la **base de données H2** :

| Champ | Valeur |
|-------|--------|
| JDBC URL | `jdbc:h2:mem:testdb` |
| User Name | `sa` |
| Password | (laisser vide) |

Cliquer "Connect"

Exécuter cette requête SQL :
```sql
SELECT * FROM USERS;
```

Tu dois voir 2 lignes (admin et user).

<details>
<summary>Ça ne fonctionne pas ? 403 Forbidden ?</summary>

Vérifie dans SecurityConfig.java que tu as bien :

```java
.csrf(csrf -> csrf.disable())
.headers(headers -> headers.frameOptions(frame -> frame.disable()))
.requestMatchers("/h2-console/**").permitAll()
```

Les 3 lignes sont nécessaires pour que H2 Console fonctionne.

</details>

<details>
<summary>Différence entre connexion H2 et connexion JWT</summary>

| Connexion H2 Console | Connexion API (JWT) |
|---------------------|---------------------|
| Pour accéder à la base de données | Pour accéder aux endpoints protégés |
| User: `sa`, Password: vide | POST /auth/login avec username/password |
| Interface web H2 | Header Authorization: Bearer token |
| Pas besoin de token | Token obligatoire |

</details>

### 9.3 Tester l'inscription

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"nouveau","password":"nouveau123"}'
```

Résultat :
```json
{"message":"User créé avec succès"}
```

### 9.4 Tester le login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"nouveau","password":"nouveau123"}'
```

Résultat :
```json
{"token":"eyJhbG...","username":"nouveau","role":"USER"}
```

### 9.5 Vérifier dans H2

```sql
SELECT * FROM USERS;
```

Tu dois voir 3 lignes maintenant.

---

## Tableau des tests

| # | Action | Commande | Résultat |
|---|--------|----------|----------|
| 1 | Register | POST /auth/register | 200 + message |
| 2 | Register doublon | POST /auth/register (même user) | 400 + error |
| 3 | Login | POST /auth/login | 200 + token |
| 4 | Login mauvais mdp | POST /auth/login | 401 + error |
| 5 | /private avec token | GET /private | 200 OK |
| 6 | /admin avec token user | GET /admin | 403 |
| 7 | /admin avec token admin | GET /admin | 200 OK |

---

## Problèmes courants et solutions

### Problèmes de configuration pom.xml

<details>
<summary>spring-boot-starter-parent version 4.x n'existe pas</summary>

**Cause :** La version 4.x de Spring Boot n'existe pas encore. La dernière version stable est 3.x.

**Erreur :**
```
Could not find artifact org.springframework.boot:spring-boot-starter-parent:pom:4.0.2
```

**Solution :**
Dans pom.xml, changer :
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>
```

</details>

<details>
<summary>spring-boot-starter-webmvc n'existe pas</summary>

**Cause :** L'artifact correct est `spring-boot-starter-web`, pas `spring-boot-starter-webmvc`.

**Solution :**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

</details>

<details>
<summary>spring-boot-starter-webmvc-test n'existe pas</summary>

**Cause :** Cet artifact n'existe pas.

**Solution :**
Utiliser `spring-boot-starter-test` :
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

</details>

<details>
<summary>spring-boot-starter-security-test n'existe pas</summary>

**Cause :** L'artifact correct est `spring-security-test`.

**Solution :**
```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

</details>

### Problèmes de syntaxe Java

<details>
<summary>Double point-virgule dans les imports</summary>

**Cause :** Erreur de frappe dans le fichier Java.

**Erreur :**
```java
import jakarta.persistence.*;;
```

**Solution :**
```java
import jakarta.persistence.*;
```

</details>

### Problèmes de sécurité (403 Forbidden)

<details>
<summary>403 sur /auth/login</summary>

**Cause :** CSRF est activé et bloque les requêtes POST.

**Solution :**
Dans SecurityConfig.java, désactiver CSRF :
```java
http
    .csrf(csrf -> csrf.disable())
```

</details>

<details>
<summary>403 sur /h2-console</summary>

**Cause :** SecurityConfig ne l'autorise pas ou les frames sont bloquées.

**Solution :**
Les 3 lignes suivantes sont nécessaires :
```java
http
    .csrf(csrf -> csrf.disable())
    .headers(headers -> headers.frameOptions(frame -> frame.disable()))
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/h2-console/**").permitAll()
        // ...
    )
```

</details>

### Problèmes de connexion H2 Console

<details>
<summary>400 Bad Request sur /h2-console</summary>

**Cause :** "Request header is too large" - le navigateur envoie trop de cookies.

**Solution 1 :** Ouvrir en navigation privée (Ctrl+Shift+N)

**Solution 2 :** Effacer les cookies pour localhost

**Solution 3 :** Ajouter dans application.properties :
```properties
server.max-http-request-header-size=48KB
```

</details>

<details>
<summary>Confusion entre connexion H2 et connexion JWT</summary>

**Explication :**

| Connexion H2 Console | Connexion API (JWT) |
|---------------------|---------------------|
| Pour accéder à la base de données | Pour accéder aux endpoints protégés |
| User: `sa`, Password: vide | POST /auth/login avec username/password |
| Interface web H2 | Header Authorization: Bearer token |
| Pas besoin de token JWT | Token obligatoire |

La page de connexion H2 demande les identifiants de la **base de données**, pas de Spring Security.

</details>

### Problèmes de port

<details>
<summary>Port 8081 already in use</summary>

**Cause :** Une autre instance de l'application tourne déjà.

**Solution Windows :**
```powershell
# Trouver le processus
netstat -ano | findstr :8081

# Tuer le processus (remplacer XXXX par le PID)
taskkill /PID XXXX /F
```

**Solution Linux/Mac :**
```bash
# Trouver le processus
lsof -i :8081

# Tuer le processus (remplacer XXXX par le PID)
kill -9 XXXX
```

</details>

### Problèmes de base de données

<details>
<summary>Table USERS not found</summary>

**Cause :** JPA n'a pas créé la table.

**Solution :**
1. Vérifier `spring.jpa.hibernate.ddl-auto=create-drop` dans application.properties
2. Vérifier que User.java a l'annotation `@Entity`
3. Relancer l'application

</details>

<details>
<summary>Le nouveau user n'a pas de rôle</summary>

**Cause :** Le rôle n'est pas défini dans /register.

**Solution :**
Vérifier dans AuthController.register() :
```java
user.setRole("USER");
```

</details>

### Tableau récapitulatif des corrections

| Fichier | Problème | Correction |
|---------|----------|------------|
| pom.xml | Version 4.0.2 n'existe pas | Changer à 3.2.0 |
| pom.xml | spring-boot-starter-webmvc | Changer à spring-boot-starter-web |
| pom.xml | spring-boot-starter-webmvc-test | Supprimer ou utiliser spring-boot-starter-test |
| pom.xml | spring-boot-starter-security-test | Changer à spring-security-test |
| User.java | Double point-virgule ;; | Corriger à ; |
| SecurityConfig.java | CSRF bloque /auth/** | Désactiver CSRF |
| SecurityConfig.java | Frames bloquées pour H2 | Désactiver frameOptions |
| application.properties | Header trop large | Ajouter max-http-request-header-size=48KB |

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
│   ├── AuthController.java      ← Modifié
│   ├── SecurityConfig.java      ← Modifié
│   ├── JwtService.java
│   ├── JwtFilter.java           ← Modifié
│   ├── User.java                ← Nouveau
│   ├── UserRepository.java      ← Nouveau
│   └── DataInit.java            ← Nouveau
└── src/main/resources/
    └── application.properties   ← Modifié
```

</details>

---

## Annexe 2 : Fichier test-db.http

<details>
<summary>Voir le fichier de test</summary>

```http
### =============================================
### Module 13 - Tests avec base de données
### =============================================

### -----------------------------------------
### Test 1 : Créer un nouveau user
### Résultat attendu : 200 OK
### -----------------------------------------
POST http://localhost:8080/auth/register
Content-Type: application/json

{
    "username": "testuser",
    "password": "test123"
}

### -----------------------------------------
### Test 2 : Créer le même user (doublon)
### Résultat attendu : 400 Bad Request
### -----------------------------------------
POST http://localhost:8080/auth/register
Content-Type: application/json

{
    "username": "testuser",
    "password": "test123"
}

### -----------------------------------------
### Test 3 : Login avec le nouveau user
### Résultat attendu : 200 OK + token
### -----------------------------------------
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "username": "testuser",
    "password": "test123"
}

### -----------------------------------------
### Test 4 : Login avec admin (créé au démarrage)
### Résultat attendu : 200 OK + token + role=ADMIN
### -----------------------------------------
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "admin123"
}

### -----------------------------------------
### Test 5 : Login avec user (créé au démarrage)
### Résultat attendu : 200 OK + token + role=USER
### -----------------------------------------
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "username": "user",
    "password": "user123"
}

### -----------------------------------------
### Test 6 : Login avec mauvais mot de passe
### Résultat attendu : 401 Unauthorized
### -----------------------------------------
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "mauvais"
}

### -----------------------------------------
### Test 7 : Login avec user inexistant
### Résultat attendu : 401 Unauthorized
### -----------------------------------------
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "username": "nexistepas",
    "password": "test123"
}
```

</details>

---

## Résumé

| Étape | Ce qu'on a fait |
|-------|-----------------|
| 1 | Ajouté dépendances JPA + H2 |
| 2 | Configuré la base de données |
| 3 | Créé l'entité User |
| 4 | Créé le UserRepository |
| 5 | Modifié AuthController (register + login) |
| 6 | Modifié JwtFilter (rôle depuis la base) |
| 7 | Modifié SecurityConfig (/h2-console) |
| 8 | Créé DataInit (admin au démarrage) |
| 9 | Testé tout |

---

## Prochaine étape

- Module 14 : Refresh token
- Module 15 : Validation des données (@Valid)
