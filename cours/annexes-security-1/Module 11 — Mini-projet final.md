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

## Prochaine etape

Une fois que tu maitrises ca, tu peux passer a :
- JWT (Module 6)
- Base de donnees pour les utilisateurs (Module 3)
- OAuth2 (Module 8)
