# Exercice Pratique — Projet Minimaliste Spring Security

## 🎯 Objectif

Créer une API REST minimaliste avec Spring Security pour comprendre les bases de l'authentification et de l'autorisation.

**Temps estimé :** 2-3 heures

---

## 📋 Cahier des charges

### Contexte

Tu dois créer une API pour une **bibliothèque** avec 3 types d'accès :
- **Public** : Consulter les livres
- **USER** : Emprunter un livre
- **ADMIN** : Ajouter/Supprimer des livres

---

## 🏗️ Structure du projet

```
src/main/java/com/exemple/biblio/
├── BiblioApplication.java
├── config/
│   └── SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   └── BookController.java
├── model/
│   ├── Book.java
│   └── User.java
├── repository/
│   ├── BookRepository.java
│   └── UserRepository.java
├── service/
│   └── BookService.java
├── dto/
│   ├── LoginRequest.java
│   └── BookDto.java
└── security/
    ├── JwtService.java
    └── JwtAuthFilter.java
```

---

## 📝 Étape 1 : Créer le projet Spring Boot

### 1.1 Dépendances (pom.xml)

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- JPA + H2 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
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
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

### 1.2 Configuration (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:bibliodb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
  h2:
    console:
      enabled: true

jwt:
  secret: VotreCleSecreteTresLongueAuMoins256BitsMinimum
  expiration: 3600000
```

---

## 📝 Étape 2 : Créer les entités

### 2.1 User.java

```java
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String email;
    
    private String password;
    
    private String nom;
    
    @Enumerated(EnumType.STRING)
    private Role role;
    
    public enum Role {
        USER, ADMIN
    }
    
    // TODO: Implémenter les méthodes UserDetails
    // getAuthorities(), getUsername(), isAccountNonExpired(), etc.
}
```

<details>
<summary>💡 Aide : Implémentation UserDetails</summary>

```java
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
}

@Override
public String getUsername() {
    return email;
}

@Override
public boolean isAccountNonExpired() {
    return true;
}

@Override
public boolean isAccountNonLocked() {
    return true;
}

@Override
public boolean isCredentialsNonExpired() {
    return true;
}

@Override
public boolean isEnabled() {
    return true;
}
```

</details>

---

### 2.2 Book.java

```java
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String titre;
    
    private String auteur;
    
    private boolean disponible = true;
    
    @ManyToOne
    private User emprunteur;
}
```

---

## 📝 Étape 3 : Créer les endpoints

### Tableau des endpoints à implémenter

| Méthode | Endpoint | Accès | Description |
|---------|----------|-------|-------------|
| POST | /api/auth/register | Public | Créer un compte |
| POST | /api/auth/login | Public | Se connecter |
| GET | /api/books | Public | Liste des livres |
| GET | /api/books/{id} | Public | Détail d'un livre |
| POST | /api/books | ADMIN | Ajouter un livre |
| DELETE | /api/books/{id} | ADMIN | Supprimer un livre |
| POST | /api/books/{id}/emprunter | USER | Emprunter un livre |
| POST | /api/books/{id}/rendre | USER | Rendre un livre |

---

## 📝 Étape 4 : Configurer Spring Security

### 4.1 SecurityConfig.java (À COMPLÉTER)

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthFilter jwtAuthFilter) throws Exception {
        
        http
            // TODO: Désactiver CSRF
            // TODO: Configurer la session STATELESS
            // TODO: Configurer les règles d'accès :
            //       - /api/auth/** : public
            //       - /api/books en GET : public
            //       - /api/books en POST/DELETE : ADMIN
            //       - /api/books/**/emprunter : USER ou ADMIN
            //       - le reste : authentifié
            // TODO: Ajouter le filtre JWT
            ;
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        // TODO: Retourner BCryptPasswordEncoder
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        // TODO: Retourner l'AuthenticationManager
    }
}
```

<details>
<summary>💡 Solution SecurityConfig</summary>

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthFilter jwtAuthFilter,
            AuthenticationProvider authProvider) throws Exception {
        
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/books/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/books").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("ADMIN")
                .requestMatchers("/api/books/**/emprunter", "/api/books/**/rendre")
                    .hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions(f -> f.disable()))
            .authenticationProvider(authProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

</details>

---

## 📝 Étape 5 : Implémenter JwtService

### 5.1 JwtService.java (À COMPLÉTER)

```java
@Service
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration}")
    private long expiration;
    
    public String generateToken(UserDetails userDetails) {
        // TODO: Générer un JWT avec :
        // - subject = username (email)
        // - issuedAt = maintenant
        // - expiration = maintenant + expiration
        // - signé avec la clé secrète
    }
    
    public String extractUsername(String token) {
        // TODO: Extraire le subject du token
    }
    
    public boolean isTokenValid(String token, UserDetails userDetails) {
        // TODO: Vérifier que :
        // - le username correspond
        // - le token n'est pas expiré
    }
    
    private boolean isTokenExpired(String token) {
        // TODO: Vérifier si le token est expiré
    }
    
    private Date extractExpiration(String token) {
        // TODO: Extraire la date d'expiration
    }
    
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        // TODO: Extraire un claim spécifique
    }
    
    private Claims extractAllClaims(String token) {
        // TODO: Parser le token et retourner tous les claims
    }
    
    private Key getSigningKey() {
        // TODO: Retourner la clé de signature
    }
}
```

<details>
<summary>💡 Solution JwtService</summary>

```java
@Service
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration}")
    private long expiration;
    
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
    
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
    
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

</details>

---

## 📝 Étape 6 : Implémenter JwtAuthFilter

### 6.1 JwtAuthFilter.java (À COMPLÉTER)

```java
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        // TODO:
        // 1. Extraire le header Authorization
        // 2. Vérifier qu'il commence par "Bearer "
        // 3. Extraire le token
        // 4. Extraire l'email du token
        // 5. Charger l'utilisateur
        // 6. Valider le token
        // 7. Créer l'Authentication et la mettre dans le SecurityContext
        // 8. Appeler filterChain.doFilter()
    }
}
```

<details>
<summary>💡 Solution JwtAuthFilter</summary>

```java
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        final String jwt = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(jwt);
        
        if (userEmail != null && 
            SecurityContextHolder.getContext().getAuthentication() == null) {
            
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

</details>

---

## 📝 Étape 7 : Tests avec Postman/cURL

### 7.1 Créer un compte

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@test.com",
    "password": "password123",
    "nom": "Test User"
  }'
```

### 7.2 Se connecter

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@test.com",
    "password": "password123"
  }'
```

**Réponse attendue :**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "user@test.com",
  "role": "USER"
}
```

### 7.3 Lister les livres (public)

```bash
curl http://localhost:8080/api/books
```

### 7.4 Ajouter un livre (ADMIN requis)

```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN_ADMIN>" \
  -d '{
    "titre": "Clean Code",
    "auteur": "Robert C. Martin"
  }'
```

### 7.5 Emprunter un livre (USER requis)

```bash
curl -X POST http://localhost:8080/api/books/1/emprunter \
  -H "Authorization: Bearer <TOKEN_USER>"
```

---

## ✅ Critères de validation

| # | Critère | Points |
|---|---------|--------|
| 1 | GET /api/books fonctionne sans authentification | 10 |
| 2 | POST /api/auth/register crée un utilisateur | 10 |
| 3 | POST /api/auth/login retourne un JWT valide | 15 |
| 4 | POST /api/books sans token → 401 | 10 |
| 5 | POST /api/books avec USER → 403 | 10 |
| 6 | POST /api/books avec ADMIN → 201 | 15 |
| 7 | POST /api/books/{id}/emprunter avec USER → 200 | 15 |
| 8 | Le mot de passe est hashé en base | 10 |
| 9 | Le code est propre et organisé | 5 |
| **Total** | | **100** |

---

## 🎁 Bonus (optionnel)

1. **+10 points** : Ajouter un endpoint GET /api/users/me qui retourne l'utilisateur connecté
2. **+10 points** : Empêcher un USER d'emprunter plus de 3 livres
3. **+10 points** : Ajouter la validation des DTOs avec @Valid
4. **+15 points** : Écrire des tests d'intégration pour les scénarios 401/403

---

## 📚 Ressources

- [Documentation Spring Security](https://docs.spring.io/spring-security/reference/)
- [JWT.io](https://jwt.io/) - Pour décoder et vérifier vos tokens
- [Chapitre 22 - Spring Security Intro](../22-spring-security-intro.md)
- [Chapitre 25 - Exercice JwtAuthFilter](../25-exercice-jwtauthfilter.md)

---

## 💬 Questions fréquentes

<details>
<summary>Pourquoi mon token ne fonctionne pas ?</summary>

Vérifiez :
1. Le header est bien `Authorization: Bearer <token>` (avec l'espace après Bearer)
2. Le token n'est pas expiré
3. La clé secrète est la même pour génération et validation
4. Le token n'a pas été modifié

</details>

<details>
<summary>Pourquoi j'ai 403 au lieu de 401 ?</summary>

- **401** = pas de token ou token invalide
- **403** = token valide mais pas le bon rôle

Si vous avez 403, votre token est valide mais vous n'avez pas les permissions requises.

</details>

<details>
<summary>Comment créer un compte ADMIN ?</summary>

Option 1 : Modifier le rôle directement dans H2 Console
Option 2 : Créer un DataInitializer qui crée un admin au démarrage
Option 3 : Ajouter un endpoint temporaire (à supprimer après)

</details>

---

**Bon courage ! 🚀**

