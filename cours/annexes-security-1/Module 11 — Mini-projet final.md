# Module 11 — Démo Minimaliste Spring Security

## Objectif

Tester Spring Security en 30 minutes avec le projet le plus simple possible.
Tu vas créer 3 endpoints et voir comment Spring Security les protège.

---

## Étape 1 : Créer le projet

### 1.1 Aller sur start.spring.io

```
https://start.spring.io
```

### 1.2 Sélectionner ces options

| Option | Valeur |
|--------|--------|
| Project | Maven |
| Language | Java |
| Spring Boot | 3.2.x |
| Group | com.demo |
| Artifact | security-demo |
| Packaging | Jar |
| Java | 17 |

### 1.3 Ajouter ces dépendances

- Spring Web
- Spring Security

### 1.4 Cliquer "Generate" et extraire le zip

---

## Étape 2 : Créer un Controller simple

Créer le fichier `src/main/java/com/demo/securitydemo/HelloController.java`

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

## Étape 3 : Lancer et tester SANS configuration

### 3.1 Lancer l'application

```bash
mvn spring-boot:run
```

### 3.2 Tester dans le navigateur

Ouvrir : `http://localhost:8080/public`

**Résultat :** Tu vois une page de login !

**Pourquoi ?** Spring Security bloque TOUT par défaut.

### 3.3 Regarder la console

Tu vas voir un mot de passe généré :

```
Using generated security password: a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

### 3.4 Se connecter

- Username : `user`
- Password : celui dans la console

**Maintenant tu vois :** "Ceci est PUBLIC - tout le monde peut voir"

---

## Étape 4 : Configurer Spring Security

### Le problème actuel

| Endpoint | Ce qu'on veut | Ce qui se passe |
|----------|---------------|-----------------|
| /public | Accessible à tous | Bloqué (login requis) |
| /private | Login requis | Bloqué (login requis) |
| /admin | Admin requis | Bloqué (login requis) |

### La solution

Créer le fichier `src/main/java/com/demo/securitydemo/SecurityConfig.java`

```java
package com.demo.securitydemo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.permitAll())
            .logout(logout -> logout.permitAll());
        
        return http.build();
    }
}
```

### Ce que fait chaque ligne

| Ligne | Effet |
|-------|-------|
| `@Configuration` | Dit à Spring que c'est une classe de configuration |
| `@EnableWebSecurity` | Active Spring Security |
| `@Bean` | Crée un composant Spring |
| `.requestMatchers("/public").permitAll()` | /public est accessible sans login |
| `.anyRequest().authenticated()` | Tout le reste demande un login |
| `.formLogin(form -> form.permitAll())` | Active la page de login |

---

## Étape 5 : Tester après configuration

### Relancer l'application

```bash
mvn spring-boot:run
```

### Tester /public

```
http://localhost:8080/public
```

**Résultat :** Tu vois directement "Ceci est PUBLIC" (pas de login)

### Tester /private

```
http://localhost:8080/private
```

**Résultat :** Redirection vers /login

### Tester /admin

```
http://localhost:8080/admin
```

**Résultat :** Redirection vers /login

---

## Étape 6 : Ajouter des utilisateurs

### Problème actuel

On a un seul utilisateur (`user`) avec mot de passe random.
On veut :
- Un utilisateur normal
- Un administrateur

### Modifier SecurityConfig.java

```java
package com.demo.securitydemo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public").permitAll()
                .requestMatchers("/admin").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.permitAll())
            .logout(logout -> logout.permitAll());
        
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        var user = User.builder()
            .username("user")
            .password(passwordEncoder().encode("user123"))
            .roles("USER")
            .build();

        var admin = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("admin123"))
            .roles("ADMIN")
            .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### Nouvelle ligne ajoutée

```java
.requestMatchers("/admin").hasRole("ADMIN")
```

Cette ligne dit : seul un ADMIN peut accéder à /admin

---

## Étape 7 : Tester les rôles

### Relancer l'application

```bash
mvn spring-boot:run
```

### Tableau des tests

| Test | URL | Login | Résultat attendu |
|------|-----|-------|------------------|
| 1 | /public | aucun | OK (200) |
| 2 | /private | aucun | Redirection login |
| 3 | /private | user/user123 | OK (200) |
| 4 | /admin | aucun | Redirection login |
| 5 | /admin | user/user123 | ERREUR 403 Forbidden |
| 6 | /admin | admin/admin123 | OK (200) |

### Test 5 : Le plus important

Quand tu te connectes avec `user/user123` et tu vas sur `/admin` :

**Tu vois :** Erreur 403 Forbidden

**Pourquoi ?** Tu es connecté (authentifié) mais tu n'as pas le rôle ADMIN (pas autorisé)

---

## Étape 8 : Comprendre 401 vs 403

| Code | Nom | Signification |
|------|-----|---------------|
| 401 | Unauthorized | Tu n'es pas connecté |
| 403 | Forbidden | Tu es connecté mais pas le droit |

### Analogie simple

- **401** = Tu n'as pas montré ta carte d'identité
- **403** = Tu as montré ta carte mais tu n'as pas accès à cette zone

---

## Étape 9 : Tester avec Postman/curl

### Pour voir les vrais codes HTTP

```bash
curl -v http://localhost:8080/public
```

Résultat : HTTP 200

```bash
curl -v http://localhost:8080/private
```

Résultat : HTTP 302 (redirection vers login)

```bash
curl -v -u user:user123 http://localhost:8080/private
```

Résultat : HTTP 200

```bash
curl -v -u user:user123 http://localhost:8080/admin
```

Résultat : HTTP 403

```bash
curl -v -u admin:admin123 http://localhost:8080/admin
```

Résultat : HTTP 200

---

## Résumé : Ce que tu as appris

| Concept | Ce que ça fait |
|---------|----------------|
| `@EnableWebSecurity` | Active Spring Security |
| `permitAll()` | Tout le monde peut accéder |
| `authenticated()` | Il faut être connecté |
| `hasRole("ADMIN")` | Il faut avoir le rôle ADMIN |
| `UserDetailsService` | Définit les utilisateurs |
| `PasswordEncoder` | Encode les mots de passe |
| 401 | Pas connecté |
| 403 | Connecté mais pas le droit |

---

## Exercice : À toi de jouer

### Niveau 1 : Ajouter un endpoint

Ajouter `/user` accessible seulement aux USER et ADMIN

<details>
<summary>Solution</summary>

Dans le controller :
```java
@GetMapping("/user")
public String userEndpoint() {
    return "Ceci est pour les USER";
}
```

Dans SecurityConfig :
```java
.requestMatchers("/user").hasAnyRole("USER", "ADMIN")
```

</details>

### Niveau 2 : Ajouter un rôle MODERATOR

Créer un utilisateur "modo" avec le rôle MODERATOR qui peut accéder à /user mais pas à /admin

<details>
<summary>Solution</summary>

```java
var modo = User.builder()
    .username("modo")
    .password(passwordEncoder().encode("modo123"))
    .roles("MODERATOR")
    .build();

return new InMemoryUserDetailsManager(user, admin, modo);
```

Et dans les règles :
```java
.requestMatchers("/user").hasAnyRole("USER", "ADMIN", "MODERATOR")
```

</details>

---

## Fichiers finaux

### HelloController.java

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

### SecurityConfig.java

```java
package com.demo.securitydemo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public").permitAll()
                .requestMatchers("/admin").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.permitAll())
            .logout(logout -> logout.permitAll());
        
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        var user = User.builder()
            .username("user")
            .password(passwordEncoder().encode("user123"))
            .roles("USER")
            .build();

        var admin = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("admin123"))
            .roles("ADMIN")
            .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

## Problèmes courants et solutions

### L'application ne démarre pas

<details>
<summary>Erreur : Port 8080 déjà utilisé</summary>

**Message :**
```
Web server failed to start. Port 8080 was already in use.
```

**Solution :**

Option 1 : Changer le port dans `application.properties`
```properties
server.port=8081
```

Option 2 : Tuer le processus qui utilise le port
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <numero> /F

# Mac/Linux
lsof -i :8080
kill -9 <numero>
```

</details>

<details>
<summary>Erreur : Cannot resolve symbol</summary>

**Message :**
```
Cannot resolve symbol 'HttpSecurity'
```

**Solution :**

1. Vérifier que la dépendance Spring Security est dans pom.xml
2. Faire Maven > Reload Project (ou clic droit > Maven > Reload)
3. Vérifier les imports en haut du fichier

</details>

---

### La page de login ne s'affiche pas

<details>
<summary>J'ai une erreur 404 sur /login</summary>

**Cause :** Tu as peut-être désactivé formLogin

**Solution :**

Vérifier que tu as cette ligne dans SecurityConfig :
```java
.formLogin(form -> form.permitAll())
```

</details>

<details>
<summary>J'ai une page blanche</summary>

**Cause :** Peut-être une erreur dans le controller

**Solution :**

1. Vérifier la console pour des erreurs
2. Vérifier que le controller a `@RestController`
3. Vérifier que les méthodes ont `@GetMapping`

</details>

---

### Je ne peux pas me connecter

<details>
<summary>Bad credentials avec user/user123</summary>

**Cause :** Tu utilises encore l'utilisateur par défaut

**Solution :**

1. Vérifier que tu as ajouté `userDetailsService()` dans SecurityConfig
2. Vérifier que tu as ajouté `passwordEncoder()`
3. Relancer l'application après les modifications

</details>

<details>
<summary>Le mot de passe dans la console ne fonctionne plus</summary>

**Cause :** Tu as défini ton propre UserDetailsService

**Explication :**
Quand tu crées un `@Bean UserDetailsService`, Spring n'utilise plus l'utilisateur par défaut.

**Solution :**
Utiliser les credentials que tu as définis :
- user / user123
- admin / admin123

</details>

---

### Problème avec les rôles

<details>
<summary>403 Forbidden alors que je suis admin</summary>

**Causes possibles :**

1. Tu es connecté avec le mauvais utilisateur
2. Le rôle n'est pas bien configuré

**Solution :**

1. Se déconnecter : aller sur `http://localhost:8080/logout`
2. Se reconnecter avec admin/admin123
3. Vérifier que dans SecurityConfig tu as :
```java
.roles("ADMIN")  // pas .roles("ROLE_ADMIN")
```

</details>

<details>
<summary>Je veux que USER et ADMIN accèdent à /user</summary>

**Solution :**

Utiliser `hasAnyRole` :
```java
.requestMatchers("/user").hasAnyRole("USER", "ADMIN")
```

</details>

---

### Problème avec /public

<details>
<summary>/public demande toujours un login</summary>

**Cause :** L'ordre des règles est important

**Mauvais :**
```java
.anyRequest().authenticated()
.requestMatchers("/public").permitAll()  // trop tard !
```

**Bon :**
```java
.requestMatchers("/public").permitAll()  // d'abord les règles spécifiques
.anyRequest().authenticated()            // puis la règle générale
```

</details>

---

### Erreurs de compilation

<details>
<summary>The method authorizeHttpRequests is undefined</summary>

**Cause :** Mauvaise version de Spring Security ou imports manquants

**Solution :**

Vérifier les imports :
```java
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
```

</details>

<details>
<summary>No bean of type PasswordEncoder</summary>

**Cause :** Tu as oublié de créer le bean PasswordEncoder

**Solution :**

Ajouter dans SecurityConfig :
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

</details>

---

### Comment se déconnecter

<details>
<summary>Je veux changer d'utilisateur</summary>

**Solution :**

1. Aller sur `http://localhost:8080/logout`
2. Cliquer sur "Log Out"
3. Se reconnecter avec un autre utilisateur

Ou dans un nouvel onglet en navigation privée.

</details>

---

### Tester avec curl ne fonctionne pas

<details>
<summary>curl retourne du HTML au lieu de JSON</summary>

**Cause :** Spring renvoie la page de login

**Solution :**

Utiliser l'option `-u` pour l'authentification Basic :
```bash
curl -u user:user123 http://localhost:8080/private
```

</details>

<details>
<summary>curl: command not found</summary>

**Solution Windows :**

1. Utiliser PowerShell avec Invoke-WebRequest :
```powershell
Invoke-WebRequest -Uri http://localhost:8080/public
```

2. Ou installer curl : https://curl.se/windows/

**Solution :** Utiliser Postman à la place

</details>

---

## Prochaine étape

Une fois que tu maîtrises ça, tu peux passer à :
- JWT (Module 12)
- Base de données pour les utilisateurs (Module 3)
- OAuth2 (Module 8)

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
│       │               └── SecurityConfig.java
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

#### SecurityConfig.java

```java
package com.demo.securitydemo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public").permitAll()
                .requestMatchers("/admin").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.permitAll())
            .logout(logout -> logout.permitAll());
        
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        var user = User.builder()
            .username("user")
            .password(passwordEncoder().encode("user123"))
            .roles("USER")
            .build();

        var admin = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("admin123"))
            .roles("ADMIN")
            .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

#### application.properties

```properties
# Fichier vide pour le Module 11
# (pas de configuration nécessaire)
```

</details>

---

## Annexe 2 : Tester avec REST Client (.http)

<details>
<summary>Voir le fichier de test</summary>

### C'est quoi REST Client ?

REST Client est une extension pour VS Code et IntelliJ qui permet de tester des requêtes HTTP directement dans l'éditeur.

### Installation

**VS Code :**
1. Ouvrir Extensions (Ctrl+Shift+X)
2. Chercher "REST Client"
3. Installer l'extension de Huachao Mao

**IntelliJ :**
- Déjà inclus (fichiers .http supportés nativement)

### Créer le fichier de test

Créer un fichier `test.http` ou `test.rest` à la racine du projet :

```
security-demo/
├── test.http          ← Créer ce fichier
├── pom.xml
└── src/
```

### Contenu du fichier test.http

```http
### =============================================
### Module 11 - Tests Spring Security (Form Login)
### =============================================

### -----------------------------------------
### Test 1 : Accéder à /public (sans auth)
### Résultat attendu : 200 OK
### -----------------------------------------
GET http://localhost:8080/public

### -----------------------------------------
### Test 2 : Accéder à /private (sans auth)
### Résultat attendu : 302 Redirect vers /login
### -----------------------------------------
GET http://localhost:8080/private

### -----------------------------------------
### Test 3 : Accéder à /admin (sans auth)
### Résultat attendu : 302 Redirect vers /login
### -----------------------------------------
GET http://localhost:8080/admin

### -----------------------------------------
### Test 4 : Se connecter avec USER
### Résultat attendu : 302 Redirect (session créée)
### -----------------------------------------
POST http://localhost:8080/login
Content-Type: application/x-www-form-urlencoded

username=user&password=user123

### -----------------------------------------
### Test 5 : Accéder à /private avec Basic Auth (user)
### Résultat attendu : 200 OK
### -----------------------------------------
GET http://localhost:8080/private
Authorization: Basic dXNlcjp1c2VyMTIz

### -----------------------------------------
### Test 6 : Accéder à /admin avec Basic Auth (user)
### Résultat attendu : 403 Forbidden
### -----------------------------------------
GET http://localhost:8080/admin
Authorization: Basic dXNlcjp1c2VyMTIz

### -----------------------------------------
### Test 7 : Se connecter avec ADMIN
### Résultat attendu : 302 Redirect (session créée)
### -----------------------------------------
POST http://localhost:8080/login
Content-Type: application/x-www-form-urlencoded

username=admin&password=admin123

### -----------------------------------------
### Test 8 : Accéder à /admin avec Basic Auth (admin)
### Résultat attendu : 200 OK
### -----------------------------------------
GET http://localhost:8080/admin
Authorization: Basic YWRtaW46YWRtaW4xMjM=

### -----------------------------------------
### Test 9 : Mauvais mot de passe
### Résultat attendu : 401 Unauthorized
### -----------------------------------------
GET http://localhost:8080/private
Authorization: Basic dXNlcjptYXV2YWlz
```

### Comment utiliser

1. Ouvrir le fichier `test.http`
2. Cliquer sur "Send Request" au-dessus de chaque requête
3. Voir la réponse dans le panneau de droite

### Les codes Basic Auth

| Utilisateur | Basic Auth (Base64) |
|-------------|---------------------|
| user:user123 | `dXNlcjp1c2VyMTIz` |
| admin:admin123 | `YWRtaW46YWRtaW4xMjM=` |
| user:mauvais | `dXNlcjptYXV2YWlz` |

### Comment générer un Basic Auth

```bash
# Linux/Mac
echo -n "user:user123" | base64

# Windows PowerShell
[Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("user:user123"))
```

### Tableau des résultats attendus

| Test | Endpoint | Auth | Résultat |
|------|----------|------|----------|
| 1 | /public | Aucune | 200 OK |
| 2 | /private | Aucune | 302 Redirect |
| 3 | /admin | Aucune | 302 Redirect |
| 4 | /login | Form (user) | 302 Redirect |
| 5 | /private | Basic (user) | 200 OK |
| 6 | /admin | Basic (user) | 403 Forbidden |
| 7 | /login | Form (admin) | 302 Redirect |
| 8 | /admin | Basic (admin) | 200 OK |
| 9 | /private | Basic (mauvais) | 401 Unauthorized |

</details>
