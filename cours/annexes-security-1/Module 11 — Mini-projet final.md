# Module 11 — Demo Minimaliste Spring Security

## Objectif

Tester Spring Security en 30 minutes avec le projet le plus simple possible.
Tu vas créer 3 endpoints et voir comment Spring Security les protège.

---

## Etape 1 : Créer le projet

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

## Etape 2 : Créer un Controller simple

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
        return "Ceci est PRIVE - il faut etre connecte";
    }

    @GetMapping("/admin")
    public String adminEndpoint() {
        return "Ceci est ADMIN - il faut etre admin";
    }
}
```

---

## Etape 3 : Lancer et tester SANS configuration

### 3.1 Lancer l'application

```bash
mvn spring-boot:run
```

### 3.2 Tester dans le navigateur

Ouvrir : `http://localhost:8080/public`

**Resultat :** Tu vois une page de login !

**Pourquoi ?** Spring Security bloque TOUT par defaut.

### 3.3 Regarder la console

Tu vas voir un mot de passe genere :

```
Using generated security password: a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

### 3.4 Se connecter

- Username : `user`
- Password : celui dans la console

**Maintenant tu vois :** "Ceci est PUBLIC - tout le monde peut voir"

---

## Etape 4 : Configurer Spring Security

### Le probleme actuel

| Endpoint | Ce qu'on veut | Ce qui se passe |
|----------|---------------|-----------------|
| /public | Accessible a tous | Bloque (login requis) |
| /private | Login requis | Bloque (login requis) |
| /admin | Admin requis | Bloque (login requis) |

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
| `@Configuration` | Dit a Spring que c'est une classe de configuration |
| `@EnableWebSecurity` | Active Spring Security |
| `@Bean` | Cree un composant Spring |
| `.requestMatchers("/public").permitAll()` | /public est accessible sans login |
| `.anyRequest().authenticated()` | Tout le reste demande un login |
| `.formLogin(form -> form.permitAll())` | Active la page de login |

---

## Etape 5 : Tester apres configuration

### Relancer l'application

```bash
mvn spring-boot:run
```

### Tester /public

```
http://localhost:8080/public
```

**Resultat :** Tu vois directement "Ceci est PUBLIC" (pas de login)

### Tester /private

```
http://localhost:8080/private
```

**Resultat :** Redirection vers /login

### Tester /admin

```
http://localhost:8080/admin
```

**Resultat :** Redirection vers /login

---

## Etape 6 : Ajouter des utilisateurs

### Probleme actuel

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

### Nouvelle ligne ajoutee

```java
.requestMatchers("/admin").hasRole("ADMIN")
```

Cette ligne dit : seul un ADMIN peut acceder a /admin

---

## Etape 7 : Tester les roles

### Relancer l'application

```bash
mvn spring-boot:run
```

### Tableau des tests

| Test | URL | Login | Resultat attendu |
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

**Pourquoi ?** Tu es connecte (authentifie) mais tu n'as pas le role ADMIN (pas autorise)

---

## Etape 8 : Comprendre 401 vs 403

| Code | Nom | Signification |
|------|-----|---------------|
| 401 | Unauthorized | Tu n'es pas connecte |
| 403 | Forbidden | Tu es connecte mais pas le droit |

### Analogie simple

- **401** = Tu n'as pas montre ta carte d'identite
- **403** = Tu as montre ta carte mais tu n'as pas acces a cette zone

---

## Etape 9 : Tester avec Postman/curl

### Pour voir les vrais codes HTTP

```bash
curl -v http://localhost:8080/public
```

Resultat : HTTP 200

```bash
curl -v http://localhost:8080/private
```

Resultat : HTTP 302 (redirection vers login)

```bash
curl -v -u user:user123 http://localhost:8080/private
```

Resultat : HTTP 200

```bash
curl -v -u user:user123 http://localhost:8080/admin
```

Resultat : HTTP 403

```bash
curl -v -u admin:admin123 http://localhost:8080/admin
```

Resultat : HTTP 200

---

## Resume : Ce que tu as appris

| Concept | Ce que ca fait |
|---------|----------------|
| `@EnableWebSecurity` | Active Spring Security |
| `permitAll()` | Tout le monde peut acceder |
| `authenticated()` | Il faut etre connecte |
| `hasRole("ADMIN")` | Il faut avoir le role ADMIN |
| `UserDetailsService` | Definit les utilisateurs |
| `PasswordEncoder` | Encode les mots de passe |
| 401 | Pas connecte |
| 403 | Connecte mais pas le droit |

---

## Exercice : A toi de jouer

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

### Niveau 2 : Ajouter un role MODERATOR

Creer un utilisateur "modo" avec le role MODERATOR qui peut acceder a /user mais pas a /admin

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

Et dans les regles :
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
        return "Ceci est PRIVE - il faut etre connecte";
    }

    @GetMapping("/admin")
    public String adminEndpoint() {
        return "Ceci est ADMIN - il faut etre admin";
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

## Problemes courants et solutions

### L'application ne demarre pas

<details>
<summary>Erreur : Port 8080 deja utilise</summary>

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

1. Verifier que la dependance Spring Security est dans pom.xml
2. Faire Maven > Reload Project (ou clic droit > Maven > Reload)
3. Verifier les imports en haut du fichier

</details>

---

### La page de login ne s'affiche pas

<details>
<summary>J'ai une erreur 404 sur /login</summary>

**Cause :** Tu as peut-etre desactive formLogin

**Solution :**

Verifier que tu as cette ligne dans SecurityConfig :
```java
.formLogin(form -> form.permitAll())
```

</details>

<details>
<summary>J'ai une page blanche</summary>

**Cause :** Peut-etre une erreur dans le controller

**Solution :**

1. Verifier la console pour des erreurs
2. Verifier que le controller a `@RestController`
3. Verifier que les methodes ont `@GetMapping`

</details>

---

### Je ne peux pas me connecter

<details>
<summary>Bad credentials avec user/user123</summary>

**Cause :** Tu utilises encore l'utilisateur par defaut

**Solution :**

1. Verifier que tu as ajoute `userDetailsService()` dans SecurityConfig
2. Verifier que tu as ajoute `passwordEncoder()`
3. Relancer l'application apres les modifications

</details>

<details>
<summary>Le mot de passe dans la console ne fonctionne plus</summary>

**Cause :** Tu as defini ton propre UserDetailsService

**Explication :**
Quand tu crees un `@Bean UserDetailsService`, Spring n'utilise plus l'utilisateur par defaut.

**Solution :**
Utiliser les credentials que tu as definis :
- user / user123
- admin / admin123

</details>

---

### Probleme avec les roles

<details>
<summary>403 Forbidden alors que je suis admin</summary>

**Causes possibles :**

1. Tu es connecte avec le mauvais utilisateur
2. Le role n'est pas bien configure

**Solution :**

1. Se deconnecter : aller sur `http://localhost:8080/logout`
2. Se reconnecter avec admin/admin123
3. Verifier que dans SecurityConfig tu as :
```java
.roles("ADMIN")  // pas .roles("ROLE_ADMIN")
```

</details>

<details>
<summary>Je veux que USER et ADMIN accedent a /user</summary>

**Solution :**

Utiliser `hasAnyRole` :
```java
.requestMatchers("/user").hasAnyRole("USER", "ADMIN")
```

</details>

---

### Probleme avec /public

<details>
<summary>/public demande toujours un login</summary>

**Cause :** L'ordre des regles est important

**Mauvais :**
```java
.anyRequest().authenticated()
.requestMatchers("/public").permitAll()  // trop tard !
```

**Bon :**
```java
.requestMatchers("/public").permitAll()  // d'abord les regles specifiques
.anyRequest().authenticated()            // puis la regle generale
```

</details>

---

### Erreurs de compilation

<details>
<summary>The method authorizeHttpRequests is undefined</summary>

**Cause :** Mauvaise version de Spring Security ou imports manquants

**Solution :**

Verifier les imports :
```java
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
```

</details>

<details>
<summary>No bean of type PasswordEncoder</summary>

**Cause :** Tu as oublie de creer le bean PasswordEncoder

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

### Comment se deconnecter

<details>
<summary>Je veux changer d'utilisateur</summary>

**Solution :**

1. Aller sur `http://localhost:8080/logout`
2. Cliquer sur "Log Out"
3. Se reconnecter avec un autre utilisateur

Ou dans un nouvel onglet en navigation privee.

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

**Solution :** Utiliser Postman a la place

</details>

---

## Prochaine etape

Une fois que tu maitrises ca, tu peux passer a :
- JWT (Module 6)
- Base de donnees pour les utilisateurs (Module 3)
- OAuth2 (Module 8)
