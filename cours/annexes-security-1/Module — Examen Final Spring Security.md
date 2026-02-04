# EXAMEN FINAL - SPRING SECURITY

## Spring Security : Fondations, JWT, et API REST

<br/>
<br/>

# Question 1

Dans Spring Security, quelle annotation permet de désactiver la protection CSRF pour une API REST ?

* [ ] @DisableCSRF
* [ ] .csrf(csrf -> csrf.enable())
* [ ] .csrf(csrf -> csrf.disable())
* [ ] @NoCSRF

<details>
<summary>Réponse</summary>

**Réponse :** `.csrf(csrf -> csrf.disable())`

Dans SecurityConfig, on désactive CSRF avec `.csrf(csrf -> csrf.disable())` car les API REST utilisent JWT et n'ont pas besoin de protection CSRF.

</details>

<br/>
<br/>

# Question 2

Quelle dépendance Maven est nécessaire pour utiliser JWT avec Spring Security ?

* [ ] spring-boot-starter-jwt
* [ ] jjwt-api et jjwt-impl
* [ ] spring-security-jwt
* [ ] jwt-core

<details>
<summary>Réponse</summary>

**Réponse :** `jjwt-api et jjwt-impl`

Les dépendances JJWT (Java JWT) sont :
- `io.jsonwebtoken:jjwt-api`
- `io.jsonwebtoken:jjwt-impl`
- `io.jsonwebtoken:jjwt-jackson`

</details>

<br/>
<br/>

# Question 3

Quel cycle de vie utilise-t-on pour un PasswordEncoder dans Spring Security ?

* [ ] Singleton
* [ ] Transient
* [ ] Scoped
* [ ] Request

<details>
<summary>Réponse</summary>

**Réponse :** `Singleton`

Le PasswordEncoder est un bean stateless (sans état), donc on utilise `@Bean` avec le scope par défaut qui est Singleton.

</details>

<br/>
<br/>

# Question 4

Où configure-t-on la chaîne de filtres de sécurité dans Spring Security ?

* [ ] Dans le constructeur du Controller
* [ ] Dans SecurityConfig avec SecurityFilterChain
* [ ] Dans application.properties
* [ ] Dans le Repository

<details>
<summary>Réponse</summary>

**Réponse :** `Dans SecurityConfig avec SecurityFilterChain`

La configuration se fait dans `SecurityConfig.java` avec une méthode annotée `@Bean` qui retourne un `SecurityFilterChain`.

</details>

<br/>
<br/>

# Question 5

Que fait la méthode `passwordEncoder.encode(password)` dans Spring Security ?

* [ ] Décrypte le mot de passe
* [ ] Hash le mot de passe avec BCrypt
* [ ] Compare deux mots de passe
* [ ] Génère un token JWT

<details>
<summary>Réponse</summary>

**Réponse :** `Hash le mot de passe avec BCrypt`

`encode()` crée un hash unidirectionnel du mot de passe. Pour vérifier, on utilise `passwordEncoder.matches(rawPassword, encodedPassword)`.

</details>

<br/>
<br/>

# Question 6

Dans une architecture Spring Security avec JWT, qui vérifie le token JWT à chaque requête ?

* [ ] Controller
* [ ] Service
* [ ] JwtFilter (OncePerRequestFilter)
* [ ] Repository

<details>
<summary>Réponse</summary>

**Réponse :** `JwtFilter (OncePerRequestFilter)`

Le `JwtFilter` étend `OncePerRequestFilter` et intercepte chaque requête HTTP pour extraire et valider le token JWT du header Authorization.

</details>

<br/>
<br/>

# Question 7

Que signifie l'annotation `@Entity` sur une classe Java ?

* [ ] La classe est un endpoint REST
* [ ] La classe représente une table dans la base de données
* [ ] La classe est un service Spring
* [ ] La classe est un composant de sécurité

<details>
<summary>Réponse</summary>

**Réponse :** `La classe représente une table dans la base de données`

`@Entity` indique à JPA/Hibernate que cette classe correspond à une table SQL. Elle est utilisée avec `@Table(name = "...")` pour spécifier le nom de la table.

</details>

<br/>
<br/>

# Question 8

Pourquoi utiliser `OncePerRequestFilter` pour créer un filtre JWT ?

* [ ] C'est obligatoire en Spring Security
* [ ] Pour garantir que le filtre s'exécute une seule fois par requête
* [ ] Pour améliorer la sécurité
* [ ] Pour utiliser moins de mémoire

<details>
<summary>Réponse</summary>

**Réponse :** `Pour garantir que le filtre s'exécute une seule fois par requête`

`OncePerRequestFilter` garantit que `doFilterInternal()` ne s'exécute qu'une seule fois par requête HTTP, même si le filtre est appelé plusieurs fois dans la chaîne.

</details>

<br/>
<br/>

# Question 9

Quelle commande Maven lance une application Spring Boot ?

* [ ] mvn start
* [ ] mvn run
* [ ] mvn spring-boot:run
* [ ] mvn boot:start

<details>
<summary>Réponse</summary>

**Réponse :** `mvn spring-boot:run`

La commande Maven pour lancer une application Spring Boot est `mvn spring-boot:run` ou `./mvnw spring-boot:run` avec le wrapper Maven.

</details>

<br/>
<br/>

# Question 10

Quelle méthode Spring Security configure l'authentification JWT ?

* [ ] builder.Services.AddJwt()
* [ ] http.addFilterBefore(jwtFilter, ...)
* [ ] SecurityConfig.addJwtFilter()
* [ ] @EnableJwtSecurity

<details>
<summary>Réponse</summary>

**Réponse :** `http.addFilterBefore(jwtFilter, ...)`

Dans `SecurityConfig`, on ajoute le `JwtFilter` avant `UsernamePasswordAuthenticationFilter` avec :
```java
.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
```

</details>

<br/>
<br/>

# Question 11

Dans quel ordre doivent être les middlewares dans SecurityFilterChain ?

* [ ] UseAuthorization puis UseAuthentication
* [ ] UseAuthentication puis UseAuthorization
* [ ] L'ordre n'a pas d'importance
* [ ] UseAuthentication seulement

<details>
<summary>Réponse</summary>

**Réponse :** `UseAuthentication puis UseAuthorization`

L'authentification doit venir AVANT l'autorisation. Le filtre JWT (authentification) doit être ajouté avant que Spring Security vérifie les autorisations.

</details>

<br/>
<br/>

# Question 12

Que fait l'annotation `@PreAuthorize("hasRole('ADMIN')")` sur une méthode ?

* [ ] Autorise tous les utilisateurs
* [ ] Nécessite un JWT valide avec le rôle ADMIN
* [ ] Génère un JWT
* [ ] Vérifie le mot de passe

<details>
<summary>Réponse</summary>

**Réponse :** `Nécessite un JWT valide avec le rôle ADMIN`

`@PreAuthorize` vérifie que l'utilisateur authentifié a le rôle spécifié avant d'exécuter la méthode. Il faut activer `@EnableMethodSecurity` dans la configuration.

</details>

<br/>
<br/>

# Question 13

Comment spécifier qu'un endpoint est accessible seulement aux Admins dans SecurityConfig ?

* [ ] .requestMatchers("/admin").permitAll()
* [ ] .requestMatchers("/admin").hasRole("ADMIN")
* [ ] .requestMatchers("/admin").@AdminOnly
* [ ] .requestMatchers("/admin").requireRole("Admin")

<details>
<summary>Réponse</summary>

**Réponse :** `.requestMatchers("/admin").hasRole("ADMIN")`

Dans `SecurityConfig`, on utilise `.hasRole("ADMIN")` pour restreindre l'accès. Spring Security ajoute automatiquement le préfixe "ROLE_", donc le rôle dans la base doit être "ADMIN" et Spring cherche "ROLE_ADMIN".

</details>

<br/>
<br/>

# Question 14

Dans une API Spring Security avec JWT, où le frontend envoie-t-il le JWT ?

* [ ] Dans l'URL
* [ ] Dans le body de la requête
* [ ] Dans le header Authorization: Bearer <token>
* [ ] Dans un cookie

<details>
<summary>Réponse</summary>

**Réponse :** `Dans le header Authorization: Bearer <token>`

Le token JWT est envoyé dans le header HTTP :
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

</details>

<br/>
<br/>

# Question 15

Quelle méthode Spring retourne HTTP 404 Not Found ?

* [ ] ResponseEntity.missing()
* [ ] ResponseEntity.notFound().build()
* [ ] ResponseEntity.error404()
* [ ] ResponseEntity.resourceNotFound()

<details>
<summary>Réponse</summary>

**Réponse :** `ResponseEntity.notFound().build()`

Pour retourner un 404, on utilise :
```java
return ResponseEntity.notFound().build();
```

</details>

<br/>
<br/>

# Question 16

Avec `@Valid` sur un paramètre, que se passe-t-il si la validation échoue ?

* [ ] Exception levée automatiquement
* [ ] 400 Bad Request automatique si @Valid est présent
* [ ] L'exécution continue
* [ ] 500 Internal Server Error

<details>
<summary>Réponse</summary>

**Réponse :** `400 Bad Request automatique si @Valid est présent`

Avec `@Valid` et `@RequestBody`, Spring valide automatiquement et retourne 400 Bad Request avec les erreurs de validation si `@Valid` est présent et que la validation échoue.

</details>

<br/>
<br/>

# Question 17

Quelle annotation de validation vérifie qu'un champ n'est pas null et pas vide ?

* [ ] @NotNull
* [ ] @NotEmpty
* [ ] @NotBlank
* [ ] @Required

<details>
<summary>Réponse</summary>

**Réponse :** `@NotBlank`

- `@NotNull` : vérifie que la valeur n'est pas null
- `@NotEmpty` : vérifie que la valeur n'est pas null et pas vide (pour collections/strings)
- `@NotBlank` : vérifie que la string n'est pas null, pas vide, et pas seulement des espaces

</details>

<br/>
<br/>

# Question 18

Quelle est la différence entre HTTP 401 et HTTP 403 ?

* [ ] Pas de différence
* [ ] 401 = pas authentifié, 403 = pas autorisé
* [ ] 401 = erreur serveur, 403 = erreur client
* [ ] 401 = token invalide, 403 = token expiré

<details>
<summary>Réponse</summary>

**Réponse :** `401 = pas authentifié, 403 = pas autorisé`

- **401 Unauthorized** : L'utilisateur n'est pas authentifié (pas de token ou token invalide)
- **403 Forbidden** : L'utilisateur est authentifié mais n'a pas les permissions nécessaires (mauvais rôle)

</details>

<br/>
<br/>

# Question 19

Dans Spring Security, quelle exception est levée si une ressource n'existe pas ?

* [ ] ResourceNotFoundException
* [ ] EntityNotFoundException
* [ ] Aucune exception automatique
* [ ] MissingException

<details>
<summary>Réponse</summary>

**Réponse :** `Aucune exception automatique`

Spring Security ne lève pas automatiquement d'exception. C'est au développeur de gérer les cas où une ressource n'existe pas, généralement avec `Optional` et `orElseThrow()` ou en retournant `ResponseEntity.notFound()`.

</details>

<br/>
<br/>

# Question 20

Dans une API REST Spring Boot, les URLs doivent contenir :

* [ ] Des verbes (getUsers, createUser)
* [ ] Des noms de ressources (users, products)
* [ ] Des actions (retrieve, insert)
* [ ] Peu importe

<details>
<summary>Réponse</summary>

**Réponse :** `Des noms de ressources (users, products)`

Les bonnes pratiques REST utilisent des noms de ressources au pluriel :
- `GET /users` - liste des utilisateurs
- `POST /users` - créer un utilisateur
- `GET /users/{id}` - obtenir un utilisateur
- `PUT /users/{id}` - mettre à jour
- `DELETE /users/{id}` - supprimer

</details>

<br/>
<br/>

# Question 21

Quelle annotation Spring Data JPA permet de créer une méthode de recherche personnalisée ?

* [ ] @Query
* [ ] @FindBy
* [ ] @Search
* [ ] @CustomQuery

<details>
<summary>Réponse</summary>

**Réponse :** `@Query`

L'annotation `@Query` permet d'écrire des requêtes SQL ou JPQL personnalisées :
```java
@Query("SELECT u FROM User u WHERE u.username = :username")
Optional<User> findByUsername(@Param("username") String username);
```

</details>

<br/>
<br/>

# Question 22

Pourquoi désactiver les frameOptions dans SecurityConfig pour H2 Console ?

* [ ] Pour améliorer les performances
* [ ] Pour permettre à H2 Console de s'afficher dans un iframe
* [ ] Pour désactiver la sécurité
* [ ] Pour activer CORS

<details>
<summary>Réponse</summary>

**Réponse :** `Pour permettre à H2 Console de s'afficher dans un iframe`

H2 Console s'affiche dans un iframe. Par défaut, Spring Security bloque les iframes avec `X-Frame-Options: DENY`. Il faut désactiver cette protection :
```java
.headers(headers -> headers.frameOptions(frame -> frame.disable()))
```

</details>

<br/>
<br/>

# Question 23

Quelle méthode du JwtService extrait le username depuis un token JWT ?

* [ ] getUsername(token)
* [ ] extractUsername(token)
* [ ] decodeUsername(token)
* [ ] parseUsername(token)

<details>
<summary>Réponse</summary>

**Réponse :** `extractUsername(token)`

La méthode standard pour extraire le username (ou toute claim) d'un token JWT est `extractUsername(token)` ou `extractClaim(token, Claims::getSubject)`.

</details>

<br/>
<br/>

# Question 24

Quelle configuration application.properties active la console H2 ?

* [ ] h2.console.enabled=true
* [ ] spring.h2.console.enabled=true
* [ ] database.h2.console=true
* [ ] jpa.h2.console.enabled=true

<details>
<summary>Réponse</summary>

**Réponse :** `spring.h2.console.enabled=true`

Pour activer la console H2, on ajoute dans `application.properties` :
```properties
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

</details>

<br/>
<br/>

# Question 25

Dans SecurityConfig, que signifie `SessionCreationPolicy.STATELESS` ?

* [ ] Les sessions sont créées à chaque requête
* [ ] Aucune session n'est créée (pour API REST avec JWT)
* [ ] Les sessions sont créées seulement pour les admins
* [ ] Les sessions sont créées seulement si nécessaire

<details>
<summary>Réponse</summary>

**Réponse :** `Aucune session n'est créée (pour API REST avec JWT)`

`STATELESS` signifie que Spring Security ne crée pas de session HTTP. C'est nécessaire pour les API REST qui utilisent JWT, car chaque requête doit être authentifiée indépendamment via le token.

</details>

<br/>
<br/>

# Question 26

Quelle interface Spring Data JPA étend-on pour créer un Repository ?

* [ ] Repository
* [ ] JpaRepository<User, Long>
* [ ] CrudRepository
* [ ] DataRepository

<details>
<summary>Réponse</summary>

**Réponse :** `JpaRepository<User, Long>`

L'interface standard est `JpaRepository<Entity, ID>` :
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
```

</details>

<br/>
<br/>

# Question 27

Quelle méthode vérifie si un mot de passe en clair correspond à un hash BCrypt ?

* [ ] passwordEncoder.compare()
* [ ] passwordEncoder.matches(rawPassword, encodedPassword)
* [ ] passwordEncoder.verify()
* [ ] passwordEncoder.check()

<details>
<summary>Réponse</summary>

**Réponse :** `passwordEncoder.matches(rawPassword, encodedPassword)`

Pour vérifier un mot de passe :
```java
if (passwordEncoder.matches(rawPassword, user.getPassword())) {
    // Mot de passe correct
}
```

</details>

<br/>
<br/>

# Question 28

Quelle annotation Spring Boot exécute du code au démarrage de l'application ?

* [ ] @Startup
* [ ] @PostConstruct
* [ ] @Component avec CommandLineRunner
* [ ] @Init

<details>
<summary>Réponse</summary>

**Réponse :** `@Component avec CommandLineRunner`

Pour exécuter du code au démarrage :
```java
@Component
public class DataInit implements CommandLineRunner {
    @Override
    public void run(String... args) {
        // Code exécuté au démarrage
    }
}
```

</details>

<br/>
<br/>

# Question 29

Dans JwtFilter, que fait `SecurityContextHolder.getContext().setAuthentication(auth)` ?

* [ ] Génère un nouveau token
* [ ] Stocke l'authentification dans le contexte Spring Security
* [ ] Vérifie le mot de passe
* [ ] Désactive la sécurité

<details>
<summary>Réponse</summary>

**Réponse :** `Stocke l'authentification dans le contexte Spring Security`

Cette ligne indique à Spring Security que l'utilisateur est authentifié. Une fois l'`Authentication` stockée dans le contexte, les vérifications d'autorisation (`hasRole`, `@PreAuthorize`, etc.) peuvent fonctionner.

</details>

<br/>
<br/>

# Question 30

Quelle configuration JPA crée automatiquement les tables au démarrage ?

* [ ] spring.jpa.hibernate.ddl-auto=create
* [ ] spring.jpa.hibernate.ddl-auto=update
* [ ] spring.jpa.hibernate.ddl-auto=create-drop
* [ ] spring.jpa.hibernate.ddl-auto=validate

<details>
<summary>Réponse</summary>

**Réponse :** `spring.jpa.hibernate.ddl-auto=create-drop`

- `create` : crée les tables au démarrage, ne les supprime pas à l'arrêt
- `create-drop` : crée au démarrage et supprime à l'arrêt (utile pour les tests)
- `update` : met à jour le schéma si nécessaire
- `validate` : valide le schéma sans le modifier

</details>

<br/>
<br/>

---

## Correction

**Barème :**
- 30 questions
- 1 point par question
- Note sur 30

**Seuil de réussite :**
- 18/30 (60%) : Réussi
- 24/30 (80%) : Bien
- 27/30 (90%) : Très bien

---

## Conseils pour la révision

1. **Revoyez les modules 1 à 13** sur Spring Security
2. **Pratiquez** avec les projets d'exercice
3. **Testez** chaque concept dans un projet réel
4. **Comprenez** la différence entre authentification et autorisation
5. **Maîtrisez** le flux JWT : login → token → filtre → contexte

---

**Bonne chance ! 🚀**

