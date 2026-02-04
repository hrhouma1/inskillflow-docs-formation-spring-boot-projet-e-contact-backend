# EXAMEN NATIONAL - PARTIE 2

## Spring Security : Services, DTOs, et CORS

<br/>
<br/>

## Partie A : Services et Repositories (Questions 1-10)

# Question 1

**Context** : Dans un projet Spring Security, voici la structure en couches :
```
UserController → UserService → UserRepository → JPA/Hibernate
```

**Question** : Quelle est la responsabilité principale d'un Service ?

* [ ] Accès à la base de données
* [ ] Logique métier et orchestration
* [ ] Gérer les requêtes HTTP
* [ ] Mapper les DTOs

<details>
<summary>Réponse</summary>

**Réponse :** `Logique métier et orchestration`

Le Service contient la logique métier (règles business) et orchestre les appels aux repositories. Il ne doit pas accéder directement à la base de données.

</details>

<br/>
<br/>

# Question 2

**Context** : Voici un Repository typique dans Spring Security :
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
```

**Question** : Quelle est la responsabilité principale d'un Repository ?

* [ ] Logique métier
* [ ] Validation des données
* [ ] Abstraction de l'accès aux données
* [ ] Génération de JWT

<details>
<summary>Réponse</summary>

**Réponse :** `Abstraction de l'accès aux données`

Le Repository abstrait l'accès aux données. Il fournit des méthodes pour interagir avec la base de données sans exposer les détails de JPA/Hibernate au Service.

</details>

<br/>
<br/>

# Question 3

**Context** : Voici le constructeur d'AuthService :
```java
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
}
```

**Question** : Combien de dépendances un Service peut-il injecter ?

* [ ] Un seul obligatoirement
* [ ] Aucun
* [ ] Plusieurs (exemple : AuthService injecte 3 dépendances)
* [ ] Tous les beans existants

<details>
<summary>Réponse</summary>

**Réponse :** `Plusieurs (exemple : AuthService injecte 3 dépendances)`

Un Service peut injecter autant de dépendances que nécessaire via le constructeur. Spring gère automatiquement l'injection de dépendances.

</details>

<br/>
<br/>

# Question 4

Que retourne `CompletableFuture<T>` ou `Mono<T>` en Java ?

* [ ] Un thread
* [ ] Une promesse de valeur future (opération asynchrone)
* [ ] Un tableau
* [ ] Une exception

<details>
<summary>Réponse</summary>

**Réponse :** `Une promesse de valeur future (opération asynchrone)`

En Java, les opérations asynchrones retournent `CompletableFuture<T>` ou `Mono<T>` (Reactive). En Spring, on utilise principalement `async/await` avec des méthodes qui retournent des types comme `Optional<T>` ou des collections.

</details>

<br/>
<br/>

# Question 5

Pourquoi utiliser async/await dans les méthodes Repository ?

* [ ] C'est obligatoire en Spring Boot
* [ ] Pour ne pas bloquer le thread pendant les opérations I/O
* [ ] Pour améliorer la sécurité
* [ ] Pour utiliser moins de mémoire

<details>
<summary>Réponse</summary>

**Réponse :** `Pour ne pas bloquer le thread pendant les opérations I/O`

Les opérations de base de données sont I/O-bound. `async/await` permet de libérer le thread pendant l'attente de la réponse de la base, améliorant ainsi le throughput de l'application.

</details>

<br/>
<br/>

# Question 6

**Context** : Extrait de UserService.register() :
```java
public UserResponse register(UserRequest request) {
    // Vérification que le username n'existe pas
    if (userRepository.existsByUsername(request.getUsername())) {
        throw new UsernameAlreadyExistsException("Username déjà utilisé");
    }
    
    // Créer l'utilisateur...
}
```

**Question** : Pourquoi vérifier que le username n'existe pas ?

* [ ] Pour optimiser les performances
* [ ] Pour respecter une règle métier (validation business)
* [ ] Pour éviter les erreurs SQL
* [ ] C'est optionnel

<details>
<summary>Réponse</summary>

**Réponse :** `Pour respecter une règle métier (validation business)`

C'est une règle métier : un username doit être unique. Cette validation se fait dans le Service, pas dans le Repository.

</details>

<br/>
<br/>

# Question 7

Où doit se faire la validation des règles métier ?

* [ ] Dans le Controller
* [ ] Dans le Service
* [ ] Dans le Repository
* [ ] Dans l'Entity

<details>
<summary>Réponse</summary>

**Réponse :** `Dans le Service`

Le Service est responsable de la logique métier et des règles de validation business. Le Controller gère HTTP, le Repository gère les données.

</details>

<br/>
<br/>

# Question 8

Que fait la méthode `findAll()` dans un Repository Spring Data JPA ?

* [ ] Crée une liste vide
* [ ] Exécute la requête SQL et retourne tous les résultats
* [ ] Convertit un tableau en liste
* [ ] Sauvegarde les données

<details>
<summary>Réponse</summary>

**Réponse :** `Exécute la requête SQL et retourne tous les résultats`

`findAll()` est une méthode héritée de `JpaRepository` qui exécute `SELECT * FROM table` et retourne tous les enregistrements.

</details>

<br/>
<br/>

# Question 9

Quelle est la différence entre un Service et un Repository ?

* [ ] Pas de différence
* [ ] Service = logique métier, Repository = accès données
* [ ] Service = HTTP, Repository = SQL
* [ ] Repository = async, Service = sync

<details>
<summary>Réponse</summary>

**Réponse :** `Service = logique métier, Repository = accès données`

- **Service** : Contient la logique métier, orchestre les opérations
- **Repository** : Abstrait l'accès aux données, méthodes CRUD

</details>

<br/>
<br/>

# Question 10

Dans un projet Spring Security, où se trouve le mapping Entity vers DTO ?

* [ ] Dans le Controller
* [ ] Dans le Service
* [ ] Dans le Repository
* [ ] Dans l'Entity

<details>
<summary>Réponse</summary>

**Réponse :** `Dans le Service`

Le mapping Entity → DTO se fait généralement dans le Service, souvent avec des méthodes comme `mapToResponse(User user)` ou avec AutoMapper.

</details>

<br/>
<br/>

## Partie B : DTOs et Mapping (Questions 11-20)

# Question 11

**Context** : Dans un projet Spring Security, on a :
- Entities/ : User, Role (modèles de base de données)
- DTOs/Request/ : UserRequest, LoginRequest (données entrantes API)
- DTOs/Response/ : UserResponse, AuthResponse (données sortantes API)

**Question** : Que signifie DTO ?

* [ ] Data Type Object
* [ ] Data Transfer Object
* [ ] Database Transaction Object
* [ ] Dynamic Type Object

<details>
<summary>Réponse</summary>

**Réponse :** `Data Transfer Object`

DTO = Data Transfer Object. C'est un objet qui transporte des données entre les couches de l'application, sans logique métier.

</details>

<br/>
<br/>

# Question 12

**Context** : Comparaison Entity vs DTO Response :
```java
// Entity (base de données)
@Entity
public class User {
    private Long id;
    private String username;
    private String passwordHash;  // Hash BCrypt
    private Role role;  // Navigation property
}

// DTO Response (API)
public class UserResponse {
    private Long id;
    private String username;
    private String roleName;  // Juste le nom du rôle
    // Pas de passwordHash !
}
```

**Question** : Pourquoi séparer les Entities des DTOs ?

* [ ] Pour rendre le code plus complexe
* [ ] Pour séparer le modèle de données du modèle de communication API
* [ ] Pour respecter les conventions de nommage
* [ ] Pour améliorer les performances

<details>
<summary>Réponse</summary>

**Réponse :** `Pour séparer le modèle de données du modèle de communication API`

Séparer Entity et DTO permet de :
- Ne pas exposer les champs sensibles (passwordHash)
- Contrôler exactement ce qui est exposé via l'API
- Éviter les références circulaires
- Évoluer indépendamment le modèle DB et l'API

</details>

<br/>
<br/>

# Question 13

Quelle est la différence entre Request et Response DTOs ?

* [ ] Pas de différence
* [ ] Request = données entrantes, Response = données sortantes
* [ ] Request = GET, Response = POST
* [ ] Request = client, Response = serveur

<details>
<summary>Réponse</summary>

**Réponse :** `Request = données entrantes, Response = données sortantes`

- **Request DTO** : Données envoyées par le client (POST/PUT body)
- **Response DTO** : Données retournées par le serveur au client

</details>

<br/>
<br/>

# Question 14

Pourquoi UserResponse n'a pas de navigation property Role complète ?

* [ ] Pour économiser de la mémoire
* [ ] Pour éviter les références circulaires et contrôler ce qui est exposé
* [ ] C'est une erreur
* [ ] Pour accélérer la sérialisation

<details>
<summary>Réponse</summary>

**Réponse :** `Pour éviter les références circulaires et contrôler ce qui est exposé`

Exposer l'objet Role complet peut créer des références circulaires (Role → User → Role...) et expose plus de données que nécessaire. On expose seulement ce qui est utile (roleName).

</details>

<br/>
<br/>

# Question 15

**Context** : Extrait d'UserService.register() :
```java
public UserResponse register(UserRequest request) {
    // Mapping Request → Entity
    User user = new User();
    user.setUsername(request.getUsername());
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    user.setRole("USER");
    
    User created = userRepository.save(user);
    return mapToResponse(created);
}
```

**Question** : Où se fait le mapping Request vers Entity ?

* [ ] Dans le Controller
* [ ] Dans le Service lors de la création
* [ ] Dans le Repository
* [ ] Automatiquement par JPA

<details>
<summary>Réponse</summary>

**Réponse :** `Dans le Service lors de la création`

Le mapping Request → Entity se fait dans le Service, généralement dans la méthode qui crée l'entité (register, create, etc.).

</details>

<br/>
<br/>

# Question 16

Qu'est-ce que MapStruct ou ModelMapper ?

* [ ] Un outil de navigation
* [ ] Une bibliothèque pour automatiser le mapping entre objets
* [ ] Un générateur de DTOs
* [ ] Un validateur

<details>
<summary>Réponse</summary>

**Réponse :** `Une bibliothèque pour automatiser le mapping entre objets`

MapStruct et ModelMapper sont des bibliothèques Java qui automatisent le mapping entre objets (Entity ↔ DTO), évitant d'écrire manuellement le code de mapping.

</details>

<br/>
<br/>

# Question 17

Pourquoi UserResponse n'expose pas passwordHash ?

* [ ] Pour économiser de la bande passante
* [ ] Pour la sécurité - ne jamais exposer les hash de mots de passe
* [ ] C'est trop long
* [ ] Pour respecter RGPD

<details>
<summary>Réponse</summary>

**Réponse :** `Pour la sécurité - ne jamais exposer les hash de mots de passe`

Même si c'est un hash, l'exposer peut aider un attaquant. C'est une règle de sécurité fondamentale : ne jamais exposer les mots de passe (même hashés) dans les réponses API.

</details>

<br/>
<br/>

# Question 18

Dans UserResponse, la propriété `age` est :

* [ ] Stockée en base de données
* [ ] Calculée automatiquement à partir de birthDate
* [ ] Envoyée par le client
* [ ] Générée par JPA

<details>
<summary>Réponse</summary>

**Réponse :** `Calculée automatiquement à partir de birthDate`

L'âge est une propriété calculée (dérivée), pas stockée. On peut l'ajouter dans le DTO avec un getter qui calcule `LocalDate.now().getYear() - birthDate.getYear()`.

</details>

<br/>
<br/>

# Question 19

Quelle annotation valide qu'un champ Request est requis ?

* [ ] @Mandatory
* [ ] @NotNull ou @NotBlank
* [ ] @Required
* [ ] @Obligatoire

<details>
<summary>Réponse</summary>

**Réponse :** `@NotNull ou @NotBlank`

En Java avec Bean Validation :
- `@NotNull` : la valeur ne doit pas être null
- `@NotBlank` : la string ne doit pas être null, vide, ou seulement des espaces
- `@NotEmpty` : pour les collections/strings non vides

</details>

<br/>
<br/>

# Question 20

Pourquoi mapper User.role vers UserResponse.roleName ?

* [ ] Pour gagner de la place
* [ ] Pour éviter de retourner l'objet Role complet avec toutes ses propriétés
* [ ] C'est obligatoire
* [ ] Pour la performance

<details>
<summary>Réponse</summary>

**Réponse :** `Pour éviter de retourner l'objet Role complet avec toutes ses propriétés`

On expose seulement le nom du rôle (roleName) au lieu de l'objet Role complet, ce qui évite d'exposer des données inutiles et potentiellement des références circulaires.

</details>

<br/>
<br/>

## Partie C : CORS et Sécurité (Questions 21-30)

# Question 21

**Context** : Dans un projet Spring Security, le frontend tourne sur http://localhost:3000 et le backend sur http://localhost:8080. Ce sont des origines différentes (ports différents).

**Question** : Que signifie CORS ?

* [ ] Cross-Origin Resource Security
* [ ] Cross-Origin Resource Sharing
* [ ] Cross-Origin Request Security
* [ ] Common Origin Resource Sharing

<details>
<summary>Réponse</summary>

**Réponse :** `Cross-Origin Resource Sharing`

CORS = Cross-Origin Resource Sharing. C'est un mécanisme qui permet à un serveur d'autoriser des requêtes provenant d'une origine différente (protocole, domaine, ou port).

</details>

<br/>
<br/>

# Question 22

Pourquoi le navigateur bloque-t-il les requêtes cross-origin par défaut ?

* [ ] Pour économiser la bande passante
* [ ] Pour la sécurité (Same-Origin Policy)
* [ ] C'est un bug
* [ ] Pour accélérer le chargement

<details>
<summary>Réponse</summary>

**Réponse :** `Pour la sécurité (Same-Origin Policy)`

La Same-Origin Policy est une mesure de sécurité du navigateur qui empêche les scripts d'une origine d'accéder aux ressources d'une autre origine, sauf si explicitement autorisé (CORS).

</details>

<br/>
<br/>

# Question 23

**Context** : Configuration CORS dans SecurityConfig.java :
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://localhost:3000"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

**Question** : Quelle origine est autorisée par la configuration CORS ?

* [ ] Toutes les origines
* [ ] http://localhost:3000 et https://localhost:3000
* [ ] Seulement https://localhost:3000
* [ ] Aucune origine

<details>
<summary>Réponse</summary>

**Réponse :** `http://localhost:3000 et https://localhost:3000`

La configuration autorise explicitement ces deux origines via `setAllowedOrigins()`.

</details>

<br/>
<br/>

# Question 24

Que signifie `setAllowCredentials(true)` dans la configuration CORS ?

* [ ] Autoriser les mots de passe
* [ ] Autoriser les cookies et headers Authorization (JWT)
* [ ] Autoriser tous les utilisateurs
* [ ] Autoriser HTTPS

<details>
<summary>Réponse</summary>

**Réponse :** `Autoriser les cookies et headers Authorization (JWT)`

`allowCredentials(true)` permet d'envoyer des cookies et des headers d'authentification (comme `Authorization: Bearer <token>`) dans les requêtes cross-origin.

</details>

<br/>
<br/>

# Question 25

**Context** : Pipeline de filtres dans SecurityConfig.java :
```java
http
    .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // CORS
    .csrf(csrf -> csrf.disable())
    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/auth/**").permitAll()
        .anyRequest().authenticated())
    .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);  // JWT Filter
```

**Question** : Dans quel ordre doivent être les filtres CORS et JWT ?

* [ ] JWT Filter puis CORS
* [ ] CORS puis JWT Filter
* [ ] L'ordre n'a pas d'importance
* [ ] CORS n'est pas un filtre

<details>
<summary>Réponse</summary>

**Réponse :** `CORS puis JWT Filter`

CORS doit être traité en premier pour permettre les requêtes preflight (OPTIONS). Le filtre JWT s'exécute ensuite pour authentifier la requête.

</details>

<br/>
<br/>

# Question 26

Qu'est-ce qu'une requête preflight ?

* [ ] Une requête de test avant déploiement
* [ ] Une requête OPTIONS envoyée par le navigateur avant la vraie requête
* [ ] La première requête de l'application
* [ ] Une requête de vérification SSL

<details>
<summary>Réponse</summary>

**Réponse :** `Une requête OPTIONS envoyée par le navigateur avant la vraie requête`

Pour certaines requêtes cross-origin (POST avec JSON, headers personnalisés), le navigateur envoie d'abord une requête OPTIONS (preflight) pour vérifier si le serveur autorise la vraie requête.

</details>

<br/>
<br/>

# Question 27

Pourquoi HTTPS est-il obligatoire en production pour une API avec JWT ?

* [ ] C'est plus rapide
* [ ] Pour chiffrer le token JWT pendant le transport
* [ ] Pour économiser de la mémoire
* [ ] C'est une loi

<details>
<summary>Réponse</summary>

**Réponse :** `Pour chiffrer le token JWT pendant le transport`

HTTPS chiffre toutes les données en transit, y compris le token JWT. Sans HTTPS, un attaquant pourrait intercepter le token (Man-in-the-Middle) et l'utiliser pour s'authentifier.

</details>

<br/>
<br/>

# Question 28

**Context** : Dans AuthService.java lors de l'inscription :
```java
User user = new User();
user.setUsername(request.getUsername());
user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
userRepository.save(user);
```

Lors de la connexion :
```java
if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
    throw new UnauthorizedException("Mot de passe incorrect");
}
```

**Question** : Comment Spring Security hash-t-il les mots de passe ?

* [ ] MD5
* [ ] SHA-256
* [ ] BCrypt (par défaut avec BCryptPasswordEncoder)
* [ ] Pas de hash

<details>
<summary>Réponse</summary>

**Réponse :** `BCrypt (par défaut avec BCryptPasswordEncoder)`

Spring Security utilise `BCryptPasswordEncoder` par défaut, qui implémente l'algorithme BCrypt. C'est un hash unidirectionnel sécurisé avec salt automatique.

</details>

<br/>
<br/>

# Question 29

Que fait `@EnableWebSecurity` dans SecurityConfig ?

* [ ] Active HTTPS
* [ ] Active la configuration de sécurité web Spring Security
* [ ] Vérifie les certificats SSL
* [ ] Génère des certificats

<details>
<summary>Réponse</summary>

**Réponse :** `Active la configuration de sécurité web Spring Security`

`@EnableWebSecurity` active la configuration de sécurité web de Spring Security. C'est nécessaire pour que `SecurityConfig` soit pris en compte.

</details>

<br/>
<br/>

# Question 30

**Context** : Dans application.properties :
```properties
jwt.secret=MaCleSecreteTresLongueAuMoins256BitsMinimumPourHMACSHA256
```

Cette clé fait environ 64 caractères.

**Question** : Quelle est la longueur minimale recommandée pour une clé secrète JWT avec HMAC-SHA256 ?

* [ ] 8 caractères
* [ ] 16 caractères
* [ ] 32 caractères (256 bits minimum)
* [ ] 64 caractères (512 bits recommandé)

<details>
<summary>Réponse</summary>

**Réponse :** `32 caractères (256 bits minimum)`

Pour HMAC-SHA256, la clé secrète doit faire au minimum 256 bits (32 caractères). Une clé plus longue (512 bits = 64 caractères) est recommandée pour une sécurité renforcée.

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

1. **Revoyez l'architecture en couches** : Controller → Service → Repository
2. **Comprenez les DTOs** : Pourquoi séparer Entity et DTO
3. **Maîtrisez CORS** : Configuration, preflight, credentials
4. **Sécurité** : BCrypt, HTTPS, longueur des clés JWT
5. **Pratiquez** le mapping Entity ↔ DTO

---

**Bonne chance ! 🚀**

