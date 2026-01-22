# Chapitre 5.1 - Introduction a Spring Security

## Objectifs du chapitre

- Comprendre les concepts de base de la securite
- Configurer Spring Security
- Distinguer authentification et autorisation

---

## 1. Qu'est-ce que Spring Security?

### Definition

**Spring Security** est le framework standard pour securiser les applications Spring. Il fournit:

- Authentification (qui etes-vous?)
- Autorisation (que pouvez-vous faire?)
- Protection contre les attaques courantes

### Dependance

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

---

## 2. Comportement par defaut

### Sans configuration

Quand vous ajoutez Spring Security, TOUT est protege par defaut:

- Tous les endpoints necessitent une authentification
- Un utilisateur "user" est cree avec un mot de passe genere
- Un formulaire de login est affiche

```
Using generated security password: 8a7d-4b2c-9e1f-3a5b

http://localhost:8080/login
```

---

## 3. Authentification vs Autorisation

### Authentification

**Qui etes-vous?** Verification de l'identite.

```
Utilisateur: admin@example.com
Password: secret123

-> Authentification reussie (identite confirmee)
```

### Autorisation

**Que pouvez-vous faire?** Verification des permissions.

```
Role: ADMIN
-> Peut acceder a /api/admin/**

Role: USER
-> Ne peut PAS acceder a /api/admin/**
```

### Schema

```
       Requete
          |
          v
    +------------+
    | Authentif. |  "Qui etes-vous?"
    +------------+
          |
          v
    +------------+
    | Autorisation|  "Avez-vous le droit?"
    +------------+
          |
          v
       Ressource
```

---

## 4. Configuration de base

### SecurityConfig.java

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Active @PreAuthorize
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Desactiver CSRF pour les APIs REST
            .csrf(csrf -> csrf.disable())
            
            // Configurer CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Pas de session (stateless)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Regles d'autorisation
            .authorizeHttpRequests(auth -> auth
                // Endpoints publics
                .requestMatchers(HttpMethod.POST, "/api/contact").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                
                // Endpoints admin
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Tout le reste necessite authentification
                .anyRequest().authenticated()
            );
            
        return http.build();
    }
}
```

---

## 5. Regles d'autorisation

### Methodes disponibles

| Methode | Description |
|---------|-------------|
| permitAll() | Acces libre |
| authenticated() | Utilisateur authentifie |
| hasRole("ADMIN") | Role specifique |
| hasAnyRole("ADMIN", "USER") | Un des roles |
| hasAuthority("READ") | Autorite specifique |
| denyAll() | Acces refuse |

### Exemples

```java
.authorizeHttpRequests(auth -> auth
    // Tout le monde
    .requestMatchers("/api/public/**").permitAll()
    
    // Utilisateurs authentifies
    .requestMatchers("/api/user/**").authenticated()
    
    // Role ADMIN seulement
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    
    // Role ADMIN ou MANAGER
    .requestMatchers("/api/manage/**").hasAnyRole("ADMIN", "MANAGER")
    
    // Methode HTTP specifique
    .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
    
    // Tout le reste
    .anyRequest().authenticated()
)
```

---

## 6. @PreAuthorize

### Securite au niveau methode

```java
@RestController
@RequestMapping("/api/admin/leads")
public class LeadController {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Lead> getAllLeads() {
        return service.findAll();
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and #id != 1")  // Ne peut pas supprimer ID 1
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
    
    @GetMapping("/my-leads")
    @PreAuthorize("#username == authentication.principal.username")
    public List<Lead> getMyLeads(@RequestParam String username) {
        return service.findByAssignedTo(username);
    }
}
```

### Activer @PreAuthorize

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Necessaire!
public class SecurityConfig {
    // ...
}
```

---

## 7. PasswordEncoder

### Importance

Les mots de passe ne doivent JAMAIS etre stockes en clair!

### BCryptPasswordEncoder

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### Utilisation

```java
@Service
public class UserService {
    
    private final PasswordEncoder passwordEncoder;
    
    public void createUser(String email, String rawPassword) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));  // Hash!
        repository.save(user);
    }
    
    public boolean verifyPassword(String raw, String encoded) {
        return passwordEncoder.matches(raw, encoded);
    }
}
```

### Exemple de hash

```
Password: "secret123"
Hash BCrypt: "$2a$10$N9qo8uLOickgx2ZMRZoMye..."
```

---

## 8. UserDetails et UserDetailsService

### UserDetails

Interface representant un utilisateur authentifie.

```java
@Entity
public class User implements UserDetails {
    
    @Id
    private Long id;
    private String email;
    private String password;
    private Role role;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public boolean isAccountNonExpired() { return true; }
    
    @Override
    public boolean isAccountNonLocked() { return true; }
    
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    
    @Override
    public boolean isEnabled() { return true; }
}
```

### UserDetailsService

Charge l'utilisateur depuis la base.

```java
@Configuration
public class UserDetailsConfig {
    
    @Bean
    public UserDetailsService userDetailsService(UserRepository repository) {
        return username -> repository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
```

---

## 9. CORS (Cross-Origin Resource Sharing)

### Pourquoi?

Les navigateurs bloquent les requetes vers un domaine different (securite).

### Configuration

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:3000", "https://monsite.com"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

---

## 10. Points cles a retenir

1. **Spring Security** protege tout par defaut
2. **Authentification** = qui etes-vous
3. **Autorisation** = que pouvez-vous faire
4. **BCrypt** pour hasher les mots de passe
5. **CORS** pour les requetes cross-origin

---

## QUIZ 5.1 - Introduction a Spring Security

**1. Quelle est la difference entre authentification et autorisation?**
   - a) Aucune
   - b) Authentification = identite, Autorisation = permissions
   - c) Authentification = permissions, Autorisation = identite
   - d) Deux termes pour la meme chose

**2. Que fait permitAll()?**
   - a) Refuse l'acces
   - b) Autorise tout le monde
   - c) Autorise les admins
   - d) Autorise les utilisateurs authentifies

**3. Quel est le comportement par defaut de Spring Security?**
   - a) Tout est public
   - b) Tout est protege
   - c) Seuls les admins ont acces
   - d) Aucune securite

**4. Quelle annotation active la securite au niveau methode?**
   - a) @EnableSecurity
   - b) @EnableMethodSecurity
   - c) @SecureMethod
   - d) @MethodProtection

**5. VRAI ou FAUX: Les mots de passe doivent etre stockes en clair.**

**6. Quel algorithme est recommande pour hasher les mots de passe?**
   - a) MD5
   - b) SHA-1
   - c) BCrypt
   - d) Base64

**7. Que fait hasRole("ADMIN")?**
   - a) Verifie que l'utilisateur est authentifie
   - b) Verifie que l'utilisateur a le role ADMIN
   - c) Cree le role ADMIN
   - d) Supprime le role ADMIN

**8. Completez: CORS signifie Cross-_______ Resource Sharing.**

**9. Quelle interface represente un utilisateur authentifie?**
   - a) User
   - b) UserDetails
   - c) AuthenticatedUser
   - d) Principal

**10. Pourquoi desactiver CSRF pour une API REST?**
   - a) Pour la performance
   - b) Parce qu'une API stateless n'utilise pas de cookies de session
   - c) Pour la securite
   - d) Ce n'est pas recommande

---

### REPONSES QUIZ 5.1

1. b) Authentification = identite, Autorisation = permissions
2. b) Autorise tout le monde
3. b) Tout est protege
4. b) @EnableMethodSecurity
5. FAUX (toujours hasher!)
6. c) BCrypt
7. b) Verifie que l'utilisateur a le role ADMIN
8. Origin
9. b) UserDetails
10. b) Parce qu'une API stateless n'utilise pas de cookies de session

