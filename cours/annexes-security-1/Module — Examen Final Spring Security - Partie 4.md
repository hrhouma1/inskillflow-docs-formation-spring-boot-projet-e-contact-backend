# EXAMEN PRATIQUE - Analyse de Code Spring Security

## Spring Security : Analyse de code et compréhension approfondie

<br/>
<br/>

# Question 1

**Extrait** : SecurityDemoApplication.java (Configuration principale)
```java
@SpringBootApplication
public class SecurityDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecurityDemoApplication.class, args);
    }
}
```

**Et** : SecurityConfig.java
```java
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

**Questions** :
1. Expliquez pourquoi `JwtFilter` est injecté dans le constructeur et non créé avec `new JwtFilter()`
2. Pourquoi utiliser `@Bean` pour `PasswordEncoder` et non `@Component` ?
3. Quel serait le problème si on inversait l'ordre (authenticated avant permitAll) ?

<details>
<summary>Réponse</summary>

**1. Injection de dépendances :**
Spring gère automatiquement l'injection de dépendances. En injectant `JwtFilter` dans le constructeur, Spring crée et injecte l'instance (qui doit être un `@Component`). Cela permet de tester facilement et de respecter le principe d'inversion de dépendances.

**2. @Bean vs @Component :**
`@Bean` est utilisé pour des méthodes qui créent des beans (comme `PasswordEncoder` qui est une classe externe). `@Component` est utilisé pour marquer des classes comme composants Spring. Ici, `PasswordEncoder` est une instance de `BCryptPasswordEncoder`, donc on utilise `@Bean` sur la méthode qui le crée.

**3. Ordre des règles d'autorisation :**
Spring Security évalue les règles dans l'ordre. Si `anyRequest().authenticated()` venait avant `permitAll()`, toutes les requêtes seraient bloquées, même celles qui devraient être publiques. L'ordre est crucial : les règles les plus spécifiques en premier, puis les plus générales.

</details>

<br/>
<br/>

# Question 2

**Extrait** : UserService.java
```java
@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public UserResponse register(UserRequest request) {
        // Vérifier que le username n'existe pas
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("Username déjà utilisé");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        
        User createdUser = userRepository.save(user);
        
        User savedUser = userRepository.findByUsername(createdUser.getUsername())
            .orElseThrow();
        
        return new UserResponse(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getRole()
        );
    }
}
```

**Questions** :
1. Pourquoi vérifier si le username existe AVANT de créer l'utilisateur ?
2. Expliquez pourquoi on fait `findByUsername` après `save`
3. Pourquoi mapper vers `UserResponse` au lieu de retourner `User` directement ?

<details>
<summary>Réponse</summary>

**1. Validation métier :**
C'est une règle métier : un username doit être unique. En vérifiant avant la création, on évite une exception de contrainte SQL et on retourne un message d'erreur plus clair à l'utilisateur.

**2. Récupération après sauvegarde :**
Après `save()`, l'entité peut avoir été modifiée par JPA (génération d'ID, timestamps, etc.). En refaisant une requête, on s'assure d'avoir l'état complet et à jour de l'entité, notamment si des triggers ou des listeners JPA ont modifié les données.

**3. Séparation Entity/DTO :**
`User` contient des données sensibles (passwordHash) et des détails d'implémentation. `UserResponse` expose uniquement ce qui est nécessaire à l'API, garantissant la sécurité et l'évolutivité du modèle.

</details>

<br/>
<br/>

# Question 3

**Extrait** : UserRepository.java
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findByRole(@Param("role") String role);
}
```

**Questions** :
1. Qu'est-ce que `JpaRepository<User, Long>` et pourquoi l'étendre ?
2. Comment Spring Data JPA génère-t-il l'implémentation de `findByUsername` ?
3. Pourquoi utiliser `@Query` pour `findByRole` au lieu de la convention de nommage ?

<details>
<summary>Réponse</summary>

**1. JpaRepository :**
`JpaRepository<User, Long>` est une interface Spring Data JPA qui fournit des méthodes CRUD standard (`save`, `findAll`, `findById`, `delete`, etc.). Le premier paramètre générique est l'entité, le second est le type de la clé primaire.

**2. Génération automatique :**
Spring Data JPA analyse le nom de la méthode `findByUsername` et génère automatiquement la requête SQL : `SELECT * FROM users WHERE username = ?`. Il suffit de respecter la convention de nommage (findBy + nom du champ).

**3. @Query pour plus de contrôle :**
Bien qu'on puisse utiliser `findByRole(String role)`, `@Query` permet d'écrire une requête JPQL ou SQL explicite, utile pour des requêtes complexes ou pour optimiser les performances.

</details>

<br/>
<br/>

# Question 4

**Extrait** : JwtService.java
```java
@Service
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private long expiration;
    
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .claim("role", getUserRole(username))
            .signWith(SignatureAlgorithm.HS256, secret)
            .compact();
    }
    
    public boolean isValid(String token) {
        try {
            Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
    
    public String extractUsername(String token) {
        return Jwts.parser()
            .setSigningKey(secret)
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }
}
```

**Questions** :
1. Expliquez le rôle de chaque partie du token JWT (Subject, IssuedAt, Expiration, Claims)
2. Pourquoi utiliser HMAC-SHA256 pour la signature ?
3. Que se passe-t-il si `isValid` retourne `false` dans le filtre JWT ?

<details>
<summary>Réponse</summary>

**1. Structure du JWT :**
- **Subject** : Le username (identifiant principal)
- **IssuedAt** : Date de création du token
- **Expiration** : Date d'expiration (sécurité)
- **Claims** : Données supplémentaires (rôle, etc.)

**2. HMAC-SHA256 :**
HMAC-SHA256 est un algorithme de signature symétrique sécurisé et rapide. Il garantit l'intégrité du token : si le contenu est modifié, la signature ne correspondra plus.

**3. Token invalide :**
Si `isValid` retourne `false`, le filtre JWT ne met pas d'`Authentication` dans le contexte. La requête continue mais sera rejetée par `authenticated()` dans SecurityConfig, retournant 401 Unauthorized.

</details>

<br/>
<br/>

# Question 5

**Extrait** : AuthController.java
```java
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

**Questions** :
1. Expliquez chaque annotation sur la classe et les méthodes
2. Qu'est-ce que l'injection de dépendance dans le constructeur ?
3. Pourquoi utiliser `ResponseEntity` au lieu de retourner directement l'objet ?

<details>
<summary>Réponse</summary>

**1. Annotations :**
- `@RestController` : Combine `@Controller` et `@ResponseBody`, retourne du JSON
- `@RequestMapping("/auth")` : Préfixe toutes les routes avec `/auth`
- `@PostMapping("/login")` : Route POST pour `/auth/login`
- `@RequestBody` : Désérialise le JSON du body en objet Java

**2. Injection de dépendances :**
Spring détecte le constructeur et injecte automatiquement une instance de `AuthService` (qui doit être un `@Service`). Cela permet de tester facilement en mockant le service.

**3. ResponseEntity :**
`ResponseEntity` permet de contrôler le code HTTP (200, 201, 401, etc.) et les headers. Retourner directement l'objet donnerait toujours 200 OK, ce qui n'est pas approprié pour les erreurs.

</details>

<br/>
<br/>

# Question 6

**Extrait** : AuthController.java
```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    try {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    } catch (UnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("error", e.getMessage()));
    }
}
```

**Et** : LoginRequest.java
```java
public class LoginRequest {
    @NotBlank(message = "Username est requis")
    private String username;
    
    @NotBlank(message = "Password est requis")
    @Size(min = 6, message = "Password doit faire au moins 6 caractères")
    private String password;
    
    // Getters et setters
}
```

**Questions** :
1. Que fait `@NotBlank` et que se passe-t-il si la validation échoue ?
2. Pourquoi attraper `UnauthorizedException` et retourner `UNAUTHORIZED` ?
3. Expliquez `@RequestBody` et pourquoi il est nécessaire

<details>
<summary>Réponse</summary>

**1. @NotBlank :**
`@NotBlank` vérifie que la string n'est pas null, vide, ou seulement des espaces. Si la validation échoue avec `@Valid`, Spring retourne automatiquement 400 Bad Request avec les erreurs de validation.

**2. Gestion d'erreur :**
`UnauthorizedException` est une exception métier. En l'attrapant, on contrôle la réponse HTTP (401 au lieu de 500) et on retourne un message d'erreur structuré pour le client.

**3. @RequestBody :**
`@RequestBody` indique à Spring de désérialiser le JSON du body HTTP en objet Java. Sans cette annotation, Spring ne saurait pas où chercher les données (query params, path variables, etc.).

</details>

<br/>
<br/>

# Question 7

**Extrait** : SecurityConfig.java
```java
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
```

**Questions** :
1. Expliquez chaque configuration (csrf, sessionManagement, authorizeHttpRequests)
2. Pourquoi désactiver CSRF pour une API REST avec JWT ?
3. Que signifie `STATELESS` et pourquoi est-ce important ?

<details>
<summary>Réponse</summary>

**1. Configurations :**
- `csrf.disable()` : Désactive la protection CSRF (non nécessaire pour API REST avec JWT)
- `sessionCreationPolicy(STATELESS)` : Pas de session HTTP, chaque requête est indépendante
- `authorizeHttpRequests` : Définit les règles d'autorisation (qui peut accéder à quoi)
- `addFilterBefore` : Ajoute le filtre JWT avant le filtre d'authentification par défaut

**2. CSRF et JWT :**
CSRF protège contre les attaques cross-site en utilisant des tokens dans les formulaires. Avec JWT dans le header Authorization, chaque requête est authentifiée indépendamment, rendant CSRF inutile.

**3. STATELESS :**
STATELESS signifie qu'aucune session HTTP n'est créée. L'authentification se fait via le JWT à chaque requête. C'est essentiel pour les API REST scalables et pour éviter les problèmes de session partagée.

</details>

<br/>
<br/>

# Question 8

**Extrait** : SecurityConfig.java
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(Duration.ofHours(24));
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .anyRequest().authenticated()
        );
    return http.build();
}
```

**Questions** :
1. Pourquoi cet ordre spécifique (cors avant csrf) ?
2. Que se passerait-il si `cors` venait après `authorizeHttpRequests` ?
3. Expliquez le rôle de `setMaxAge`

<details>
<summary>Réponse</summary>

**1. Ordre des filtres :**
CORS doit être traité en premier pour permettre les requêtes preflight (OPTIONS) qui n'ont pas de JWT. CSRF vient ensuite, puis l'authentification.

**2. CORS après authorizeHttpRequests :**
Si CORS venait après, les requêtes preflight (OPTIONS) seraient bloquées car elles n'ont pas de token JWT. Le navigateur ne pourrait pas vérifier si la vraie requête est autorisée.

**3. setMaxAge :**
`setMaxAge` indique au navigateur combien de temps (24h) mettre en cache la réponse preflight. Cela réduit le nombre de requêtes OPTIONS, améliorant les performances.

</details>

<br/>
<br/>

# Question 9

**Extrait** : User.java (Entity)
```java
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String passwordHash;
    
    @Column(nullable = false)
    private String role;
    
    // Getters et setters
}
```

**Et** : UserRepository.java
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
```

**Questions** :
1. Expliquez chaque annotation JPA (@Entity, @Table, @Id, @GeneratedValue, @Column)
2. Que fait `GenerationType.IDENTITY` ?
3. Pourquoi utiliser `Optional<User>` au lieu de `User` directement ?

<details>
<summary>Réponse</summary>

**1. Annotations JPA :**
- `@Entity` : Marque la classe comme entité JPA (table SQL)
- `@Table(name = "users")` : Nom de la table en base
- `@Id` : Clé primaire
- `@GeneratedValue` : Auto-incrément de la clé primaire
- `@Column` : Configuration de la colonne (unique, nullable, etc.)

**2. GenerationType.IDENTITY :**
Utilise l'auto-incrément de la base de données (AUTO_INCREMENT en MySQL, IDENTITY en SQL Server). La base génère l'ID automatiquement.

**3. Optional :**
`Optional<User>` évite les `NullPointerException`. Si l'utilisateur n'existe pas, on obtient `Optional.empty()` au lieu de `null`, permettant un traitement explicite avec `.orElse()`, `.orElseThrow()`, etc.

</details>

<br/>
<br/>

# Question 10

**Extrait complet** : Flow d'une requête GET /api/users (avec JWT)

```java
// 1. Controller
@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}

// 2. Service
@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
            .map(user -> new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole()
            ))
            .collect(Collectors.toList());
    }
}

// 3. Repository
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Méthodes héritées : findAll(), findById(), etc.
}

// 4. JwtFilter
@Component
public class JwtFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) {
        String token = extractToken(request);
        if (token != null && jwtService.isValid(token)) {
            String username = jwtService.extractUsername(token);
            // Créer Authentication et l'ajouter au contexte
            Authentication auth = new UsernamePasswordAuthenticationToken(
                username, null, getAuthorities(username)
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }
}
```

**Question** : Tracez le flow complet de cette requête depuis le client HTTP jusqu'à la base de données et retour. Expliquez le rôle de chaque couche et filtre.

<details>
<summary>Réponse</summary>

**Flow complet :**

1. **Client HTTP** : Envoie `GET /api/users` avec header `Authorization: Bearer <token>`

2. **JwtFilter** (OncePerRequestFilter) :
   - Extrait le token du header Authorization
   - Valide le token avec `jwtService.isValid()`
   - Extrait le username et crée une `Authentication`
   - Met l'`Authentication` dans `SecurityContextHolder`

3. **SecurityConfig** :
   - Vérifie les règles d'autorisation
   - `@PreAuthorize("hasRole('ADMIN')")` vérifie que l'utilisateur a le rôle ADMIN
   - Si autorisé, la requête continue

4. **UserController** :
   - Reçoit la requête HTTP
   - Appelle `userService.getAllUsers()`

5. **UserService** :
   - Appelle `userRepository.findAll()`
   - Mappe les entités `User` vers les DTOs `UserResponse`
   - Retourne la liste

6. **UserRepository** :
   - Exécute `SELECT * FROM users` via JPA
   - Retourne les entités `User`

7. **Retour** :
   - Service → Controller → JSON → Client HTTP
   - `ResponseEntity.ok(users)` sérialise en JSON

**Rôle de chaque couche :**
- **Filter** : Authentification (vérifie le JWT)
- **SecurityConfig** : Autorisation (vérifie les rôles)
- **Controller** : Point d'entrée HTTP, gestion des requêtes/réponses
- **Service** : Logique métier, orchestration, mapping Entity → DTO
- **Repository** : Abstraction de l'accès aux données

</details>

<br/>
<br/>

# Question 11

**Extrait** : JwtFilter.java
```java
@Component
public class JwtFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    private final UserRepository userRepository;
    
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
            
            User user = userRepository.findByUsername(username)
                .orElse(null);
            
            if (user != null) {
                String role = "ROLE_" + user.getRole();
                
                Authentication auth = new UsernamePasswordAuthenticationToken(
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

**Questions** :
1. Pourquoi étendre `OncePerRequestFilter` au lieu de `Filter` ?
2. Pourquoi vérifier le user dans la base après avoir validé le JWT ?
3. Pourquoi ajouter le préfixe "ROLE_" au rôle ?

<details>
<summary>Réponse</summary>

**1. OncePerRequestFilter :**
`OncePerRequestFilter` garantit que `doFilterInternal()` ne s'exécute qu'une seule fois par requête, même si le filtre est appelé plusieurs fois dans la chaîne. Cela évite les traitements en double.

**2. Vérification en base :**
Même si le JWT est valide, l'utilisateur peut avoir été supprimé ou son rôle modifié depuis la création du token. En vérifiant en base, on s'assure d'avoir les informations à jour (rôle actuel).

**3. Préfixe "ROLE_" :**
Spring Security attend les rôles avec le préfixe "ROLE_" par convention. `hasRole("ADMIN")` cherche "ROLE_ADMIN" dans les autorités. Si on stocke "ADMIN" en base, il faut ajouter le préfixe.

</details>

<br/>
<br/>

# Question 12

**Extrait** : application.properties
```properties
# Database
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

# JWT
jwt.secret=MaCleSecreteTresLongueAuMoins256BitsMinimumPourHMACSHA256
jwt.expiration=86400000
```

**Questions** :
1. Expliquez chaque configuration (datasource, JPA, H2, JWT)
2. Que signifie `ddl-auto=create-drop` ?
3. Pourquoi la clé JWT doit-elle être longue ?

<details>
<summary>Réponse</summary>

**1. Configurations :**
- **datasource** : Configuration de la connexion à la base H2 (en mémoire)
- **JPA** : `ddl-auto=create-drop` crée les tables au démarrage et les supprime à l'arrêt
- **H2 Console** : Active l'interface web pour accéder à la base
- **JWT** : Clé secrète et durée d'expiration (86400000 ms = 24h)

**2. create-drop :**
Crée les tables au démarrage de l'application et les supprime à l'arrêt. Utile pour les tests et le développement, mais dangereux en production (perte de données).

**3. Longueur de la clé :**
Pour HMAC-SHA256, la clé doit faire au minimum 256 bits (32 caractères). Une clé plus longue (512 bits = 64 caractères) renforce la sécurité contre les attaques par force brute.

</details>

<br/>
<br/>

# Question 13

**Extrait** : DataInit.java
```java
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
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            userRepository.save(admin);
            System.out.println("Admin créé : admin / admin123");
        }
        
        if (!userRepository.existsByUsername("user")) {
            User user = new User();
            user.setUsername("user");
            user.setPasswordHash(passwordEncoder.encode("user123"));
            user.setRole("USER");
            userRepository.save(user);
            System.out.println("User créé : user / user123");
        }
    }
}
```

**Questions** :
1. Qu'est-ce que `CommandLineRunner` et quand s'exécute-t-il ?
2. Pourquoi vérifier `existsByUsername` avant de créer ?
3. Pourquoi utiliser `PasswordEncoder.encode()` au lieu de stocker le mot de passe en clair ?

<details>
<summary>Réponse</summary>

**1. CommandLineRunner :**
`CommandLineRunner` est une interface Spring Boot. La méthode `run()` s'exécute automatiquement après le démarrage complet de l'application, une fois que tous les beans sont initialisés. C'est idéal pour initialiser des données.

**2. Vérification avant création :**
En vérifiant si l'utilisateur existe déjà, on évite de créer des doublons si l'application redémarre. Cela permet aussi d'éviter les exceptions de contrainte unique en base.

**3. Hashage du mot de passe :**
Jamais stocker les mots de passe en clair ! `PasswordEncoder.encode()` crée un hash unidirectionnel (BCrypt) avec salt automatique. Même si la base est compromise, les mots de passe ne peuvent pas être récupérés.

</details>

<br/>
<br/>

# Question 14

**Extrait** : AuthService.java
```java
@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new UnauthorizedException("Utilisateur non trouvé"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Mot de passe incorrect");
        }
        
        String token = jwtService.generateToken(user.getUsername());
        
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }
}
```

**Questions** :
1. Pourquoi utiliser `orElseThrow()` au lieu de vérifier `isPresent()` ?
2. Expliquez `passwordEncoder.matches()` et pourquoi on ne peut pas comparer directement ?
3. Pourquoi générer le token seulement après la validation du mot de passe ?

<details>
<summary>Réponse</summary>

**1. orElseThrow() :**
`orElseThrow()` est plus concis et expressif. Il lance directement une exception si l'Optional est vide, évitant un if/else explicite. C'est une pratique idiomatique en Java moderne.

**2. passwordEncoder.matches() :**
On ne peut pas comparer directement car le mot de passe est hashé avec BCrypt (qui inclut un salt aléatoire). `matches()` compare le mot de passe en clair avec le hash stocké en utilisant l'algorithme BCrypt. Chaque hash est unique même pour le même mot de passe.

**3. Génération du token après validation :**
Le token ne doit être généré que si l'authentification réussit. Sinon, un attaquant pourrait obtenir un token valide même avec un mauvais mot de passe. C'est une règle de sécurité fondamentale.

</details>

<br/>
<br/>

# Question 15

**Extrait** : Comparaison de deux approches

**Approche 1 : Sans Service**
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserRepository userRepository;
    
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
}
```

**Approche 2 : Avec Service**
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}

@Service
public class UserService {
    private final UserRepository userRepository;
    
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
}
```

**Questions** :
1. Quels sont les avantages de l'approche 2 (avec Service) ?
2. Quel est le problème de sécurité de l'approche 1 ?
3. Pourquoi séparer Controller, Service et Repository ?

<details>
<summary>Réponse</summary>

**1. Avantages de l'approche 2 :**
- **Séparation des responsabilités** : Chaque couche a un rôle clair
- **Sécurité** : Le Service peut filtrer les données sensibles (passwordHash)
- **Réutilisabilité** : La logique métier peut être réutilisée ailleurs
- **Testabilité** : Plus facile de tester chaque couche indépendamment
- **Évolutivité** : Facile d'ajouter de la logique métier sans toucher au Controller

**2. Problème de sécurité de l'approche 1 :**
En retournant directement les entités `User`, on expose potentiellement des données sensibles (passwordHash, champs internes). De plus, on expose la structure de la base de données.

**3. Séparation des couches :**
- **Controller** : Gère HTTP (requêtes/réponses, codes d'erreur)
- **Service** : Logique métier, règles business, orchestration
- **Repository** : Accès aux données, abstraction de la base

Cette séparation suit le principe de responsabilité unique (SOLID) et facilite la maintenance et les tests.

</details>

<br/>
<br/>

---

## Correction

**Barème :**
- 15 questions d'analyse de code
- 2-3 sous-questions par question
- Total : 35 points
- Note sur 35

**Seuil de réussite :**
- 21/35 (60%) : Réussi
- 28/35 (80%) : Bien
- 32/35 (90%) : Très bien

---

## Conseils pour la révision

1. **Comprenez l'architecture** : Controller → Service → Repository
2. **Injection de dépendances** : Constructeur, @Component, @Service, @Repository
3. **JWT Flow** : Génération → Validation → Extraction → Authentication
4. **Sécurité** : Hashage des mots de passe, séparation Entity/DTO
5. **Spring Security** : Filtres, autorisations, contexte de sécurité
6. **JPA** : Entités, repositories, requêtes, relations

---

**Bonne chance ! 🚀**

