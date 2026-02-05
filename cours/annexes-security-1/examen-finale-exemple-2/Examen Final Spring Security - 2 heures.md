# EXAMEN FINAL - SPRING SECURITY
## Durée : 2 heures

---

## Instructions générales

- **Durée totale** : 2 heures
- **Partie 1 (Quiz)** : 30 minutes - 10 questions
- **Partie 2 (Pratique)** : 90 minutes - 3 exercices de code


**Barème :**
- Partie 1 : 20 points (2 points par question)
- Partie 2 : 80 points (exercice 1: 25 points, exercice 2: 30 points, exercice 3: 25 points)
- **Total : 100 points**

---

# PARTIE 1 : QUIZ (30 minutes - 20 points)

## Contexte

Vous travaillez sur une application Spring Boot qui gère une API REST pour une bibliothèque en ligne. L'application utilise Spring Security avec JWT pour l'authentification et l'autorisation.

---

## Question 1 (2 points)

Dans Spring Security, quelle méthode permet de désactiver la protection CSRF pour une API REST avec JWT ?

* [ ] `@DisableCSRF`
* [ ] `.csrf(csrf -> csrf.enable())`
* [ ] `.csrf(csrf -> csrf.disable())`
* [ ] `@NoCSRF`

---

## Question 2 (2 points)

**Contexte** : Voici un extrait de `SecurityConfig.java` :

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**").permitAll()
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        );
    return http.build();
}
```

Que signifie `SessionCreationPolicy.STATELESS` ?

* [ ] Les sessions sont créées à chaque requête
* [ ] Aucune session n'est créée (pour API REST avec JWT)
* [ ] Les sessions sont créées seulement pour les admins
* [ ] Les sessions sont créées seulement si nécessaire

---

## Question 3 (2 points)

**Contexte** : Voici un extrait de `UserRepository.java` :

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
```

Quelle est la responsabilité principale d'un Repository ?

* [ ] Logique métier
* [ ] Validation des données
* [ ] Abstraction de l'accès aux données
* [ ] Génération de JWT

---

## Question 4 (2 points)

**Contexte** : Voici un extrait de `UserService.java` :

```java
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserResponse register(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("Username déjà utilisé");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        
        return mapToResponse(userRepository.save(user));
    }
}
```

Pourquoi vérifier si le username existe AVANT de créer l'utilisateur ?

* [ ] Pour optimiser les performances
* [ ] Pour respecter une règle métier (validation business)
* [ ] Pour éviter les erreurs SQL
* [ ] C'est optionnel

---

## Question 5 (2 points)

**Contexte** : Voici un extrait de `JwtFilter.java` :

```java
@Component
public class JwtFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) {
        String token = extractToken(request);
        if (token != null && jwtService.isValid(token)) {
            String username = jwtService.extractUsername(token);
            Authentication auth = new UsernamePasswordAuthenticationToken(
                username, null, getAuthorities(username)
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }
}
```

Pourquoi étendre `OncePerRequestFilter` au lieu de `Filter` ?

* [ ] C'est obligatoire en Spring Security
* [ ] Pour garantir que le filtre s'exécute une seule fois par requête
* [ ] Pour améliorer la sécurité
* [ ] Pour utiliser moins de mémoire

---

## Question 6 (2 points)

**Contexte** : Voici un extrait de `SecurityConfig.java` :

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    
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

Dans quel ordre doivent être les filtres CORS et JWT ?

* [ ] JWT Filter puis CORS
* [ ] CORS puis JWT Filter
* [ ] L'ordre n'a pas d'importance
* [ ] CORS n'est pas un filtre

---

## Question 7 (2 points)

**Contexte** : Voici un extrait de `User.java` :

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
}
```

Que fait l'annotation `@Column(unique = true, nullable = false)` ?

* [ ] La colonne doit être initialisée dans le constructeur
* [ ] La colonne SQL ne peut pas être NULL et doit être unique
* [ ] La colonne est obligatoire pour les tests
* [ ] La colonne doit être unique mais peut être NULL

---

## Question 8 (2 points)

**Contexte** : Voici un extrait de `AuthService.java` :

```java
@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
```

Pourquoi utiliser `passwordEncoder.matches()` au lieu de comparer directement les strings ?

* [ ] C'est plus rapide
* [ ] Les mots de passe sont hashés avec BCrypt (salt aléatoire)
* [ ] Pour améliorer la sécurité
* [ ] C'est obligatoire en Spring Security

---

## Question 9 (2 points)

**Contexte** : Voici un extrait de `application.properties` :

```properties
# JWT
jwt.secret=MaCleSecreteTresLongueAuMoins256BitsMinimumPourHMACSHA256
jwt.expiration=86400000

# Database
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
```

Quelle est la durée de vie du token JWT (en heures) ?

* [ ] 1 heure
* [ ] 12 heures
* [ ] 24 heures
* [ ] 48 heures

---

## Question 10 (2 points)

**Contexte** : Voici un extrait de `UserController.java` :

```java
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        // ...
    }
}
```

Que fait l'annotation `@PreAuthorize("hasRole('ADMIN')")` ?

* [ ] Autorise tous les utilisateurs
* [ ] Nécessite un JWT valide avec le rôle ADMIN
* [ ] Génère un JWT
* [ ] Vérifie le mot de passe

---

# PARTIE 2 : EXERCICES PRATIQUES (90 minutes - 80 points)

## Contexte général

Vous devez compléter et corriger le code d'une application Spring Boot qui gère une API REST pour une bibliothèque. L'application doit permettre :

- **Public** : Consulter la liste des livres
- **USER** : Emprunter un livre
- **ADMIN** : Ajouter/Supprimer des livres

L'application utilise :
- Spring Security avec JWT
- JPA/Hibernate avec H2 (base en mémoire)
- Architecture en couches : Controller → Service → Repository

---

## Exercice 1 : Compléter SecurityConfig (25 points)

**Contexte** : Le fichier `SecurityConfig.java` est incomplet. Vous devez le compléter pour que l'application fonctionne correctement.

**Fichier à compléter** : `src/main/java/com/biblio/config/SecurityConfig.java`

```java
package com.biblio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.biblio.security.JwtFilter;

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
            // TODO 1.1 : Désactiver CSRF (2 points)
            
            // TODO 1.2 : Configurer la session en mode STATELESS (2 points)
            
            // TODO 1.3 : Configurer les autorisations :
            //   - /auth/** : permitAll()
            //   - /api/books (GET) : permitAll()
            //   - /api/books (POST, DELETE) : hasRole("ADMIN")
            //   - /api/books/borrow : hasRole("USER")
            //   - anyRequest() : authenticated()
            // (8 points)
            
            // TODO 1.4 : Ajouter le filtre JWT avant UsernamePasswordAuthenticationFilter (3 points)
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        // TODO 1.5 : Retourner une instance de BCryptPasswordEncoder (2 points)
        return null;
    }
}
```

**Questions à répondre** (8 points) :

1. Pourquoi désactiver CSRF pour une API REST avec JWT ? (2 points)
2. Que signifie `SessionCreationPolicy.STATELESS` ? (2 points)
3. Pourquoi ajouter le filtre JWT avant `UsernamePasswordAuthenticationFilter` ? (2 points)
4. Expliquez le rôle de `PasswordEncoder` dans Spring Security. (2 points)

---

## Exercice 2 : Compléter JwtFilter (30 points)

**Contexte** : Le filtre JWT est incomplet. Vous devez le compléter pour valider les tokens JWT et mettre l'authentification dans le contexte Spring Security.

**Fichier à compléter** : `src/main/java/com/biblio/security/JwtFilter.java`

```java
package com.biblio.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.biblio.repository.UserRepository;
import com.biblio.model.User;

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
        
        // TODO 2.1 : Extraire le header "Authorization" (3 points)
        String authHeader = null; // À compléter
        
        // TODO 2.2 : Vérifier si le header existe et commence par "Bearer " (3 points)
        // Si non, appeler filterChain.doFilter() et return
        
        // TODO 2.3 : Extraire le token (enlever "Bearer " du début) (2 points)
        String token = null; // À compléter
        
        // TODO 2.4 : Valider le token avec jwtService.isValid(token) (3 points)
        // Si valide, continuer, sinon appeler filterChain.doFilter() et return
        
        // TODO 2.5 : Extraire le username du token avec jwtService.extractUsername(token) (3 points)
        String username = null; // À compléter
        
        // TODO 2.6 : Chercher l'utilisateur dans la base avec userRepository.findByUsername(username) (4 points)
        // Utiliser .orElse(null) pour gérer le cas où l'utilisateur n'existe pas
        User user = null; // À compléter
        
        // TODO 2.7 : Si l'utilisateur existe, créer l'Authentication et l'ajouter au contexte (12 points)
        if (user != null) {
            // Créer le rôle avec le préfixe "ROLE_"
            String role = "ROLE_" + user.getRole();
            
            // Créer l'Authentication avec :
            // - principal : username
            // - credentials : null
            // - authorities : List contenant un SimpleGrantedAuthority avec le rôle
            Authentication auth = null; // À compléter
            
            // Ajouter l'Authentication au SecurityContextHolder
            // À compléter
        }
        
        // TODO 2.8 : Appeler filterChain.doFilter() pour continuer la chaîne de filtres (2 points)
        // À compléter
    }
}
```

**Questions à répondre** (8 points) :

1. Pourquoi vérifier le user dans la base après avoir validé le JWT ? (2 points)
2. Pourquoi ajouter le préfixe "ROLE_" au rôle ? (2 points)
3. Que se passe-t-il si `SecurityContextHolder.getContext().setAuthentication(auth)` n'est pas appelé ? (2 points)
4. Expliquez le rôle de `filterChain.doFilter()` dans le filtre. (2 points)

---

## Exercice 3 : Compléter AuthService (25 points)

**Contexte** : Le service d'authentification est incomplet. Vous devez compléter les méthodes `login` et `register`.

**Fichier à compléter** : `src/main/java/com/biblio/service/AuthService.java`

```java
package com.biblio.service;

import com.biblio.model.User;
import com.biblio.repository.UserRepository;
import com.biblio.dto.LoginRequest;
import com.biblio.dto.RegisterRequest;
import com.biblio.dto.AuthResponse;
import com.biblio.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    public AuthService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder,
                      JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    
    public AuthResponse login(LoginRequest request) {
        // TODO 3.1 : Chercher l'utilisateur par username (3 points)
        // Utiliser userRepository.findByUsername() avec .orElseThrow()
        // Lancer une exception si l'utilisateur n'existe pas
        User user = null; // À compléter
        
        // TODO 3.2 : Vérifier le mot de passe avec passwordEncoder.matches() (4 points)
        // Si le mot de passe ne correspond pas, lancer une exception
        // À compléter
        
        // TODO 3.3 : Générer le token JWT avec jwtService.generateToken(user.getUsername()) (3 points)
        String token = null; // À compléter
        
        // TODO 3.4 : Retourner un AuthResponse avec token, username et role (2 points)
        return null; // À compléter
    }
    
    public AuthResponse register(RegisterRequest request) {
        // TODO 3.5 : Vérifier si le username existe déjà avec userRepository.existsByUsername() (3 points)
        // Si oui, lancer une exception UsernameAlreadyExistsException
        // À compléter
        
        // TODO 3.6 : Créer un nouvel utilisateur (5 points)
        User user = new User();
        // Définir le username
        // Hasher le mot de passe avec passwordEncoder.encode()
        // Définir le rôle par défaut à "USER"
        // À compléter
        
        // TODO 3.7 : Sauvegarder l'utilisateur avec userRepository.save() (2 points)
        User savedUser = null; // À compléter
        
        // TODO 3.8 : Générer le token JWT et retourner AuthResponse (3 points)
        // À compléter
        return null;
    }
}
```

**Structure des DTOs** (fournie) :

```java
// LoginRequest.java
public class LoginRequest {
    private String username;
    private String password;
    // Getters et setters
}

// RegisterRequest.java
public class RegisterRequest {
    private String username;
    private String password;
    // Getters et setters
}

// AuthResponse.java
public class AuthResponse {
    private String token;
    private String username;
    private String role;
    // Constructeur et getters
}
```

**Questions à répondre** (8 points) :

1. Pourquoi vérifier si le username existe avant de créer l'utilisateur ? (2 points)
2. Pourquoi hasher le mot de passe avec `passwordEncoder.encode()` au lieu de le stocker en clair ? (2 points)
3. Pourquoi générer le token JWT seulement après la validation du mot de passe ? (2 points)
4. Expliquez la différence entre `login` et `register` en termes de sécurité. (2 points)

---

## Instructions pour la remise

1. Complétez tous les `TODO` dans les fichiers fournis
2. Répondez aux questions posées dans chaque exercice
3. Assurez-vous que votre code compile sans erreurs
4. Commentez votre code si nécessaire pour expliquer vos choix

**Critères d'évaluation :**
- **Correction du code** : 60%
- **Réponses aux questions** : 25%
- **Qualité du code** (lisibilité, commentaires) : 15%

---

**Bon courage ! 🚀**

