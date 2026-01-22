# Chapitre 5.3 - SecurityConfig.java : Explication ligne par ligne

## Objectifs du chapitre

- Comprendre chaque ligne du fichier `SecurityConfig.java`
- Maîtriser les annotations de configuration Spring Security
- Comprendre le rôle de chaque Bean
- Savoir personnaliser la configuration de sécurité

---

## Vue d'ensemble du fichier

Le fichier `SecurityConfig.java` est le **cerveau de la sécurité** de votre application. C'est ici que vous définissez :
- Quels endpoints sont publics ou protégés
- Comment authentifier les utilisateurs
- Comment gérer les sessions
- Comment configurer CORS

```mermaid
mindmap
  root((SecurityConfig))
    Annotations
      @Configuration
      @EnableWebSecurity
      @EnableMethodSecurity
    Beans
      SecurityFilterChain
      CorsConfigurationSource
      AuthenticationProvider
      AuthenticationManager
      PasswordEncoder
    Configuration
      CSRF désactivé
      CORS configuré
      Sessions stateless
      Règles d'accès
```

---

## 1. Les imports : Comprendre les dépendances

```java
package com.example.contact.config;

import com.example.contact.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
```

### Explication des imports par catégorie

```mermaid
graph TB
    subgraph "Notre code"
        JWT["JwtAuthFilter<br/>Notre filtre personnalisé"]
    end
    
    subgraph "Configuration Spring"
        BEAN["@Bean - Déclarer des objets gérés"]
        CONFIG["@Configuration - Classe de config"]
    end
    
    subgraph "Spring Security Core"
        AM["AuthenticationManager<br/>Gère l'authentification"]
        AP["AuthenticationProvider<br/>Vérifie les credentials"]
        DAO["DaoAuthenticationProvider<br/>Utilise une base de données"]
        UDS["UserDetailsService<br/>Charge les utilisateurs"]
    end
    
    subgraph "Spring Security Web"
        HTTP["HttpSecurity<br/>Configure la sécurité HTTP"]
        SFC["SecurityFilterChain<br/>Chaîne de filtres"]
        UPAF["UsernamePasswordAuthenticationFilter<br/>Filtre standard à remplacer"]
    end
    
    subgraph "Configuration additionnelle"
        CSRF["AbstractHttpConfigurer<br/>Désactiver CSRF"]
        SESSION["SessionCreationPolicy<br/>Politique de session"]
        CORS["CorsConfiguration<br/>Configuration CORS"]
    end
```

| Import | Rôle |
|--------|------|
| `JwtAuthFilter` | Notre filtre personnalisé pour valider les JWT |
| `@Bean` | Déclare un objet géré par Spring |
| `@Configuration` | Marque la classe comme source de configuration |
| `HttpMethod` | Enum pour GET, POST, PUT, DELETE |
| `AuthenticationManager` | Interface centrale pour l'authentification |
| `AuthenticationProvider` | Fournit la logique d'authentification |
| `DaoAuthenticationProvider` | Implémentation qui utilise UserDetailsService |
| `HttpSecurity` | Builder pour configurer la sécurité HTTP |
| `SessionCreationPolicy` | Définit comment gérer les sessions |
| `SecurityFilterChain` | La chaîne de filtres de sécurité |
| `PasswordEncoder` | Interface pour encoder les mots de passe |
| `BCryptPasswordEncoder` | Implémentation BCrypt (recommandée) |
| `CorsConfiguration` | Configuration CORS |

---

## 2. Les annotations de classe

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
```

### @Configuration

Cette annotation indique à Spring que cette classe contient des définitions de **Beans**. Spring va scanner cette classe au démarrage et créer tous les objets marqués `@Bean`.

```mermaid
sequenceDiagram
    participant S as Spring Boot
    participant SC as SecurityConfig
    participant C as Conteneur IoC
    
    S->>SC: 1. Trouve @Configuration
    S->>SC: 2. Scanne les méthodes @Bean
    SC->>C: 3. Crée SecurityFilterChain
    SC->>C: 4. Crée PasswordEncoder
    SC->>C: 5. Crée AuthenticationProvider
    SC->>C: 6. Crée AuthenticationManager
    SC->>C: 7. Crée CorsConfigurationSource
```

**Sans @Configuration** : Spring ne saurait pas que cette classe contient des Beans à créer.

### @EnableWebSecurity

Cette annotation **active Spring Security** pour votre application web. Elle fait plusieurs choses :

1. Active le filtre de sécurité Spring
2. Configure l'infrastructure de base
3. Permet d'utiliser `HttpSecurity`

```mermaid
graph LR
    A["@EnableWebSecurity"] --> B["Active SpringSecurityFilterChain"]
    B --> C["Intercepte TOUTES les requêtes"]
    C --> D["Applique vos règles de sécurité"]
```

**Sans @EnableWebSecurity** : Aucune requête ne serait filtrée, votre API serait ouverte à tous!

### @EnableMethodSecurity

Cette annotation permet d'utiliser les annotations de sécurité **au niveau des méthodes** :
- `@PreAuthorize` - Vérifie AVANT l'exécution
- `@PostAuthorize` - Vérifie APRÈS l'exécution
- `@Secured` - Version simplifiée

```java
// Exemple d'utilisation grâce à @EnableMethodSecurity
@GetMapping("/{id}")
@PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
public User getUser(@PathVariable Long id) {
    return userService.findById(id);
}
```

**Sans @EnableMethodSecurity** : Les annotations `@PreAuthorize` seraient ignorées!

---

## 3. SecurityFilterChain : Le coeur de la configuration

```java
@Bean
public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        JwtAuthFilter jwtAuthFilter,
        AuthenticationProvider authenticationProvider) throws Exception {
```

### Qu'est-ce qu'un SecurityFilterChain?

C'est une **chaîne de filtres** qui traite chaque requête HTTP. Imaginez une série de gardiens, chacun vérifiant quelque chose de différent.

```mermaid
graph LR
    REQ[Requête HTTP] --> F1["Filtre CORS"]
    F1 --> F2["Filtre CSRF"]
    F2 --> F3["Filtre JWT<br/>(notre JwtAuthFilter)"]
    F3 --> F4["Filtre Autorisation"]
    F4 --> CTRL[Controller]
    
    style F3 fill:#4CAF50,color:#fff
```

### Les paramètres injectés

```mermaid
graph TB
    subgraph "Injection de dépendances"
        HTTP["HttpSecurity<br/>Fourni par Spring Security"]
        JWT["JwtAuthFilter<br/>Notre @Component"]
        AP["AuthenticationProvider<br/>Notre @Bean ci-dessous"]
    end
    
    HTTP --> SFC["securityFilterChain()"]
    JWT --> SFC
    AP --> SFC
```

| Paramètre | Source | Rôle |
|-----------|--------|------|
| `HttpSecurity http` | Spring Security | Builder pour configurer la sécurité |
| `JwtAuthFilter jwtAuthFilter` | Notre code (@Component) | Filtre pour valider les JWT |
| `AuthenticationProvider authenticationProvider` | Notre @Bean | Vérifie les credentials |

---

## 4. Configuration CSRF

```java
http
    .csrf(AbstractHttpConfigurer::disable)
```

### Qu'est-ce que ça fait?

Cette ligne **désactive la protection CSRF**. 

### Pourquoi désactiver CSRF?

```mermaid
graph TB
    subgraph "API REST avec JWT"
        A["Pas de cookies de session"]
        B["Token envoyé manuellement"]
        C["CSRF impossible"]
        D["On désactive CSRF"]
    end
    
    A --> B --> C --> D
    
    subgraph "Application MVC classique"
        E["Cookies de session"]
        F["Envoi automatique"]
        G["CSRF possible!"]
        H["CSRF OBLIGATOIRE"]
    end
    
    E --> F --> G --> H
    
    style D fill:#4CAF50,color:#fff
    style H fill:#f44336,color:#fff
```

### Syntaxe moderne vs ancienne

```java
// Spring Security 6+ (moderne) - Method Reference
.csrf(AbstractHttpConfigurer::disable)

// Équivalent avec lambda
.csrf(csrf -> csrf.disable())

// Ancienne syntaxe (dépréciée)
.csrf().disable()
```

---

## 5. Configuration CORS

```java
.cors(cors -> cors.configurationSource(corsConfigurationSource()))
```

### Qu'est-ce que ça fait?

Cette ligne active CORS et utilise notre configuration personnalisée définie dans la méthode `corsConfigurationSource()`.

```mermaid
sequenceDiagram
    participant B as Navigateur
    participant API as Spring Boot
    
    B->>API: OPTIONS /api/leads (preflight)
    API->>API: Vérifie corsConfigurationSource()
    API-->>B: Access-Control-Allow-Origin: *
    B->>API: GET /api/leads
    API-->>B: 200 OK + données
```

---

## 6. Gestion des sessions

```java
.sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

### SessionCreationPolicy.STATELESS

Cette configuration indique à Spring de **ne jamais créer de session HTTP**. Chaque requête est indépendante.

```mermaid
graph TB
    subgraph "STATELESS (notre choix)"
        A1["Requête 1 + JWT"] --> S1["Vérifier JWT"]
        A2["Requête 2 + JWT"] --> S2["Vérifier JWT"]
        A3["Requête 3 + JWT"] --> S3["Vérifier JWT"]
    end
    
    subgraph "STATEFUL (sessions)"
        B1["Login"] --> SESSION["Session serveur<br/>JSESSIONID=abc123"]
        SESSION --> B2["Requête 2"]
        SESSION --> B3["Requête 3"]
    end
```

| Policy | Description | Usage |
|--------|-------------|-------|
| `STATELESS` | Pas de session, chaque requête est indépendante | API REST avec JWT |
| `ALWAYS` | Toujours créer une session | Applications web classiques |
| `IF_REQUIRED` | Créer si nécessaire | Par défaut |
| `NEVER` | Ne pas créer, mais utiliser si existe | Cas spéciaux |

### Pourquoi STATELESS pour une API REST?

1. **Scalabilité** : Pas besoin de partager les sessions entre serveurs
2. **Performance** : Pas de stockage serveur
3. **Simplicité** : Le JWT contient toutes les infos nécessaires
4. **Mobile-friendly** : Les apps mobiles n'utilisent pas de cookies

---

## 7. Règles d'autorisation

```java
.authorizeHttpRequests(auth -> auth
        // Endpoints publics
        .requestMatchers(HttpMethod.POST, "/api/contact").permitAll()
        .requestMatchers("/api/auth/**").permitAll()
        .requestMatchers("/h2-console/**").permitAll()
        // Swagger UI
        .requestMatchers("/swagger-ui/**").permitAll()
        .requestMatchers("/swagger-ui.html").permitAll()
        .requestMatchers("/v3/api-docs/**").permitAll()
        // Endpoints admin
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        // Tout le reste requiert authentification
        .anyRequest().authenticated()
)
```

### Diagramme des règles

```mermaid
graph TB
    REQ[Requête entrante] --> CHECK{Quelle URL?}
    
    CHECK -->|"POST /api/contact"| PUBLIC1["✅ permitAll()"]
    CHECK -->|"/api/auth/**"| PUBLIC2["✅ permitAll()"]
    CHECK -->|"/h2-console/**"| PUBLIC3["✅ permitAll()"]
    CHECK -->|"/swagger-ui/**"| PUBLIC4["✅ permitAll()"]
    CHECK -->|"/v3/api-docs/**"| PUBLIC5["✅ permitAll()"]
    CHECK -->|"/api/admin/**"| ADMIN{"hasRole('ADMIN')?"}
    CHECK -->|"Autre URL"| AUTH{"authenticated()?"}
    
    ADMIN -->|"Oui"| OK1["✅ Accès accordé"]
    ADMIN -->|"Non"| DENIED1["❌ 403 Forbidden"]
    
    AUTH -->|"Oui"| OK2["✅ Accès accordé"]
    AUTH -->|"Non"| DENIED2["❌ 401 Unauthorized"]
    
    style PUBLIC1 fill:#4CAF50,color:#fff
    style PUBLIC2 fill:#4CAF50,color:#fff
    style PUBLIC3 fill:#4CAF50,color:#fff
    style PUBLIC4 fill:#4CAF50,color:#fff
    style PUBLIC5 fill:#4CAF50,color:#fff
    style OK1 fill:#4CAF50,color:#fff
    style OK2 fill:#4CAF50,color:#fff
    style DENIED1 fill:#f44336,color:#fff
    style DENIED2 fill:#f44336,color:#fff
```

### Explication de chaque règle

| Règle | Signification |
|-------|---------------|
| `requestMatchers(HttpMethod.POST, "/api/contact").permitAll()` | SEULEMENT POST sur /api/contact est public |
| `requestMatchers("/api/auth/**").permitAll()` | Tout sous /api/auth/ est public (login, register) |
| `requestMatchers("/h2-console/**").permitAll()` | Console H2 accessible (développement) |
| `requestMatchers("/swagger-ui/**").permitAll()` | Documentation Swagger accessible |
| `requestMatchers("/api/admin/**").hasRole("ADMIN")` | Tout sous /api/admin/ requiert le rôle ADMIN |
| `anyRequest().authenticated()` | Tout le reste requiert une authentification |

### L'ordre est IMPORTANT!

```mermaid
graph TB
    A["Règle 1: /api/contact POST → public"] --> B["Règle 2: /api/auth/** → public"]
    B --> C["Règle 3: /api/admin/** → ADMIN"]
    C --> D["Règle 4: anyRequest → authenticated"]
    
    NOTE["⚠️ Spring évalue les règles<br/>dans l'ordre de déclaration!<br/>La première qui matche gagne."]
```

**Mauvais ordre** :
```java
// ❌ ERREUR : anyRequest() en premier bloque tout!
.anyRequest().authenticated()
.requestMatchers("/api/auth/**").permitAll()  // Jamais atteint!
```

**Bon ordre** :
```java
// ✅ CORRECT : du plus spécifique au plus général
.requestMatchers("/api/auth/**").permitAll()
.anyRequest().authenticated()
```

---

## 8. Configuration des headers

```java
.headers(headers -> headers.frameOptions(frame -> frame.disable()))
```

### Qu'est-ce que ça fait?

Désactive la protection `X-Frame-Options` qui empêche l'affichage dans une iframe.

### Pourquoi?

La console H2 utilise des iframes pour son interface. Sans cette configuration, elle ne s'afficherait pas.

```mermaid
graph LR
    subgraph "Avec X-Frame-Options"
        A["Console H2"] -->|"iframe"| B["❌ Bloqué"]
    end
    
    subgraph "Sans X-Frame-Options"
        C["Console H2"] -->|"iframe"| D["✅ Fonctionne"]
    end
```

> ⚠️ **En production**, vous devriez réactiver cette protection et ne pas exposer la console H2.

---

## 9. Ajout du filtre JWT

```java
.authenticationProvider(authenticationProvider)
.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
```

### authenticationProvider()

Définit comment Spring Security vérifie les credentials (email/mot de passe).

### addFilterBefore()

Ajoute notre `JwtAuthFilter` AVANT le filtre standard d'authentification.

```mermaid
graph LR
    subgraph "Chaîne de filtres"
        A["...autres filtres..."] --> B["JwtAuthFilter<br/>(notre filtre)"]
        B --> C["UsernamePasswordAuthenticationFilter<br/>(filtre standard)"]
        C --> D["...autres filtres..."]
    end
    
    style B fill:#4CAF50,color:#fff
```

### Pourquoi "before"?

Notre filtre JWT doit s'exécuter AVANT le filtre standard car :
1. Si le JWT est valide, l'utilisateur est déjà authentifié
2. Le filtre standard n'a plus besoin de s'exécuter
3. On évite une double vérification

---

## 10. CorsConfigurationSource

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("*"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setExposedHeaders(List.of("Authorization"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### Explication ligne par ligne

```mermaid
graph TB
    subgraph "CorsConfiguration"
        O["setAllowedOrigins(['*'])<br/>Qui peut appeler?"]
        M["setAllowedMethods([...])<br/>Quelles méthodes HTTP?"]
        H["setAllowedHeaders(['*'])<br/>Quels headers entrants?"]
        E["setExposedHeaders(['Authorization'])<br/>Quels headers sortants?"]
    end
    
    subgraph "UrlBasedCorsConfigurationSource"
        R["registerCorsConfiguration('/**', config)<br/>Appliquer à toutes les URLs"]
    end
    
    O --> M --> H --> E --> R
```

| Méthode | Valeur | Signification |
|---------|--------|---------------|
| `setAllowedOrigins(List.of("*"))` | `*` | Tous les domaines peuvent appeler l'API |
| `setAllowedMethods(...)` | GET, POST, PUT, DELETE, OPTIONS | Méthodes HTTP autorisées |
| `setAllowedHeaders(List.of("*"))` | `*` | Tous les headers sont acceptés |
| `setExposedHeaders(List.of("Authorization"))` | Authorization | Le frontend peut lire ce header |
| `registerCorsConfiguration("/**", ...)` | `/**` | S'applique à toutes les URLs |

### En production

```java
// ⚠️ En production, spécifiez les origines exactes!
configuration.setAllowedOrigins(List.of(
    "https://monsite.com",
    "https://www.monsite.com"
));
```

---

## 11. AuthenticationProvider

```java
@Bean
public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
}
```

### Qu'est-ce que c'est?

L'`AuthenticationProvider` est responsable de **vérifier les credentials** (email + mot de passe).

```mermaid
sequenceDiagram
    participant C as Controller (/login)
    participant AM as AuthenticationManager
    participant AP as DaoAuthenticationProvider
    participant UDS as UserDetailsService
    participant PE as PasswordEncoder
    participant DB as Base de données
    
    C->>AM: authenticate(email, password)
    AM->>AP: authenticate()
    AP->>UDS: loadUserByUsername(email)
    UDS->>DB: SELECT * FROM users WHERE email=?
    DB-->>UDS: User
    UDS-->>AP: UserDetails
    AP->>PE: matches(password, hashedPassword)
    PE-->>AP: true/false
    AP-->>AM: Authentication (success/failure)
    AM-->>C: Résultat
```

### DaoAuthenticationProvider

C'est une implémentation qui :
1. Utilise `UserDetailsService` pour charger l'utilisateur
2. Utilise `PasswordEncoder` pour vérifier le mot de passe

```java
DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
authProvider.setUserDetailsService(userDetailsService);  // Comment charger l'utilisateur
authProvider.setPasswordEncoder(passwordEncoder());       // Comment vérifier le mot de passe
```

---

## 12. AuthenticationManager

```java
@Bean
public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
}
```

### Qu'est-ce que c'est?

L'`AuthenticationManager` est le **point d'entrée** pour l'authentification. Il délègue le travail aux `AuthenticationProvider`.

```mermaid
graph TB
    LOGIN["AuthController.login()"] --> AM["AuthenticationManager"]
    AM --> AP1["DaoAuthenticationProvider<br/>(notre provider)"]
    AM --> AP2["Autres providers possibles"]
    
    AP1 --> UDS["UserDetailsService"]
    AP1 --> PE["PasswordEncoder"]
```

### Pourquoi cette syntaxe?

```java
// Spring Security configure automatiquement un AuthenticationManager
// On récupère juste celui qui est configuré
return config.getAuthenticationManager();
```

### Utilisation dans le code

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    
    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        // Utilise l'AuthenticationManager pour authentifier
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );
        // Si pas d'exception, l'authentification a réussi
        // Générer et retourner le JWT...
    }
}
```

---

## 13. PasswordEncoder

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### Qu'est-ce que BCrypt?

BCrypt est un algorithme de **hachage** spécialement conçu pour les mots de passe. Il est :
- **Lent** (intentionnellement, pour résister aux attaques brute-force)
- **Salé** (chaque hash inclut un sel aléatoire)
- **Adaptatif** (le coût peut être augmenté avec le temps)

```mermaid
graph LR
    A["motdepasse123"] --> B["BCrypt"]
    B --> C["$2a$10$N9qo8uLOickgx2ZMRZoMye<br/>IjZAgcfl7p92ldGxad68LJZdL17lhWy"]
    
    D["motdepasse123"] --> E["BCrypt<br/>(même mot de passe)"]
    E --> F["$2a$10$DIFFÉRENT...<br/>(hash différent à cause du sel)"]
```

### Comparaison des algorithmes

| Algorithme | Sécurité | Vitesse | Usage |
|------------|----------|---------|-------|
| MD5 | ❌ Cassé | Très rapide | JAMAIS pour les mots de passe |
| SHA-256 | ⚠️ Trop rapide | Rapide | Checksums, pas mots de passe |
| BCrypt | ✅ Recommandé | Lent | Mots de passe |
| Argon2 | ✅ Très sécurisé | Configurable | Alternative moderne |

### Fonctionnement

```java
// Lors de l'inscription
String hash = passwordEncoder.encode("motdepasse123");
// hash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"

// Lors du login
boolean match = passwordEncoder.matches("motdepasse123", hash);
// match = true
```

---

## 14. Schéma complet de la configuration

```mermaid
graph TB
    subgraph "SecurityConfig.java"
        direction TB
        
        subgraph "Annotations"
            A1["@Configuration<br/>Classe de config"]
            A2["@EnableWebSecurity<br/>Active Spring Security"]
            A3["@EnableMethodSecurity<br/>Active @PreAuthorize"]
        end
        
        subgraph "SecurityFilterChain Bean"
            B1["csrf().disable()<br/>Pas besoin pour JWT"]
            B2["cors()<br/>Autorise les requêtes cross-origin"]
            B3["sessionManagement(STATELESS)<br/>Pas de session serveur"]
            B4["authorizeHttpRequests()<br/>Règles d'accès"]
            B5["addFilterBefore(jwtAuthFilter)<br/>Ajoute notre filtre JWT"]
        end
        
        subgraph "Autres Beans"
            C1["CorsConfigurationSource<br/>Config CORS détaillée"]
            C2["AuthenticationProvider<br/>Vérifie les credentials"]
            C3["AuthenticationManager<br/>Point d'entrée auth"]
            C4["PasswordEncoder<br/>BCrypt"]
        end
    end
    
    A1 --> B1
    A2 --> B1
    A3 --> B4
    
    B1 --> B2 --> B3 --> B4 --> B5
    
    C1 --> B2
    C2 --> B5
    C4 --> C2
```

---

## QUIZ 5.3 - SecurityConfig en détail

**1. Que fait l'annotation @EnableWebSecurity?**
- a) Active la sécurité des méthodes
- b) Active Spring Security et le filtre de sécurité
- c) Désactive CSRF
- d) Configure CORS

<details>
<summary>Voir la réponse</summary>

**Réponse : b) Active Spring Security et le filtre de sécurité**

@EnableWebSecurity active le filtre Spring Security qui intercepte toutes les requêtes HTTP et applique les règles de sécurité configurées.
</details>

---

**2. Pourquoi utilise-t-on SessionCreationPolicy.STATELESS?**
- a) Pour améliorer les performances
- b) Parce qu'on utilise JWT qui contient toutes les infos nécessaires
- c) Pour économiser la mémoire
- d) Toutes ces réponses

<details>
<summary>Voir la réponse</summary>

**Réponse : d) Toutes ces réponses**

STATELESS signifie pas de session serveur. Avec JWT, chaque requête contient toutes les informations (dans le token), donc pas besoin de sessions. Cela améliore les performances, économise la mémoire, et facilite la scalabilité.
</details>

---

**3. Pourquoi l'ordre des requestMatchers est-il important?**
- a) Pour la lisibilité
- b) Spring évalue les règles dans l'ordre, la première qui matche gagne
- c) Pour les performances
- d) Ce n'est pas important

<details>
<summary>Voir la réponse</summary>

**Réponse : b) Spring évalue les règles dans l'ordre, la première qui matche gagne**

Si vous mettez `anyRequest().authenticated()` en premier, toutes les requêtes nécessiteront une authentification, même celles censées être publiques. Toujours aller du plus spécifique au plus général.
</details>

---

**4. Que fait addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)?**
- a) Remplace le filtre standard
- b) Ajoute notre filtre APRÈS le filtre standard
- c) Ajoute notre filtre AVANT le filtre standard
- d) Désactive le filtre standard

<details>
<summary>Voir la réponse</summary>

**Réponse : c) Ajoute notre filtre AVANT le filtre standard**

Notre JwtAuthFilter s'exécute avant UsernamePasswordAuthenticationFilter. Si le JWT est valide, l'utilisateur est authentifié et le filtre standard n'a plus besoin de faire son travail.
</details>

---

**5. Pourquoi BCrypt est-il recommandé pour les mots de passe?**
- a) Il est rapide
- b) Il est simple à implémenter
- c) Il est lent, salé et résistant aux attaques brute-force
- d) Il est réversible

<details>
<summary>Voir la réponse</summary>

**Réponse : c) Il est lent, salé et résistant aux attaques brute-force**

BCrypt est intentionnellement lent pour rendre les attaques brute-force impraticables. Il inclut un sel aléatoire dans chaque hash, donc deux mots de passe identiques produisent des hashs différents.
</details>

---

**6. Que signifie `configuration.setAllowedOrigins(List.of("*"))`?**
- a) Aucune origine autorisée
- b) Seul localhost est autorisé
- c) Toutes les origines sont autorisées
- d) Seules les requêtes HTTPS sont autorisées

<details>
<summary>Voir la réponse</summary>

**Réponse : c) Toutes les origines sont autorisées**

Le `*` (wildcard) autorise tous les domaines à appeler l'API. En production, vous devriez spécifier les origines exactes pour plus de sécurité.
</details>

---

**7. Quel est le rôle de DaoAuthenticationProvider?**
- a) Générer des JWT
- b) Charger les utilisateurs et vérifier les mots de passe
- c) Configurer CORS
- d) Gérer les sessions

<details>
<summary>Voir la réponse</summary>

**Réponse : b) Charger les utilisateurs et vérifier les mots de passe**

DaoAuthenticationProvider utilise UserDetailsService pour charger l'utilisateur depuis la base de données et PasswordEncoder pour vérifier que le mot de passe fourni correspond au hash stocké.
</details>

---

## Navigation

| Précédent | Suivant |
|-----------|---------|
| [23 - Spring Security FAQ](23-spring-security-faq.md) | [25 - JwtAuthFilter en détail](25-jwt-auth-filter.md) |

