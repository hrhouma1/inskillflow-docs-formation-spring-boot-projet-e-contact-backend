# Module 10 — Mini-projet final : API "Gestion de cours"

## Objectif du module

Mettre en pratique tous les concepts vus dans les modules précédents en construisant une API complète et sécurisée. Ce projet te permettra de consolider tes connaissances et de voir comment tout s'articule ensemble.

---

## 1. Description du projet

Tu vas créer une API REST pour une plateforme de gestion de cours en ligne.

### Les fonctionnalités :

| Fonctionnalité | Accès | Description |
|----------------|-------|-------------|
| Lister les cours | Public | Tout le monde peut voir les cours disponibles |
| Voir un cours | Public | Détails d'un cours spécifique |
| Créer un cours | ADMIN | Seuls les admins peuvent créer |
| Modifier un cours | ADMIN | Seuls les admins peuvent modifier |
| Supprimer un cours | ADMIN | Seuls les admins peuvent supprimer |
| S'inscrire à un cours | USER | Les utilisateurs connectés peuvent s'inscrire |
| Voir mes inscriptions | USER | Chaque user voit SES inscriptions |

---

## 2. Endpoints de l'API

### Authentification

| Méthode | Endpoint | Accès | Description |
|---------|----------|-------|-------------|
| POST | /api/auth/register | Public | Créer un compte |
| POST | /api/auth/login | Public | Se connecter (retourne JWT) |
| POST | /api/auth/refresh | Authentifié | Rafraîchir le token |

### Cours

| Méthode | Endpoint | Accès | Description |
|---------|----------|-------|-------------|
| GET | /api/courses | Public | Liste des cours |
| GET | /api/courses/{id} | Public | Détail d'un cours |
| POST | /api/courses | ADMIN | Créer un cours |
| PUT | /api/courses/{id} | ADMIN | Modifier un cours |
| DELETE | /api/courses/{id} | ADMIN | Supprimer un cours |

### Inscriptions

| Méthode | Endpoint | Accès | Description |
|---------|----------|-------|-------------|
| POST | /api/enrollments/{courseId} | USER | S'inscrire à un cours |
| GET | /api/enrollments/me | USER | Mes inscriptions |
| DELETE | /api/enrollments/{id} | USER | Se désinscrire (propriétaire) |

---

## 3. Modèles de données

### User

```java
@Entity
public class User implements UserDetails {
    @Id @GeneratedValue
    private Long id;
    
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    private Role role; // USER, ADMIN
    
    private boolean enabled = true;
}
```

### Course

```java
@Entity
public class Course {
    @Id @GeneratedValue
    private Long id;
    
    private String title;
    private String description;
    private String instructor;
    private Integer duration; // en heures
    private LocalDateTime createdAt;
}
```

### Enrollment

```java
@Entity
public class Enrollment {
    @Id @GeneratedValue
    private Long id;
    
    @ManyToOne
    private User user;
    
    @ManyToOne
    private Course course;
    
    private LocalDateTime enrolledAt;
}
```

---

## 4. Configuration de sécurité

### SecurityConfig

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, 
            JwtAuthFilter jwtFilter) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfig()))
            .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public
                .requestMatchers(GET, "/api/courses/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                // Admin
                .requestMatchers(POST, "/api/courses").hasRole("ADMIN")
                .requestMatchers(PUT, "/api/courses/**").hasRole("ADMIN")
                .requestMatchers(DELETE, "/api/courses/**").hasRole("ADMIN")
                // Authenticated
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

---

## 5. Tests à écrire

### Scénarios de test obligatoires

| # | Scénario | Résultat attendu |
|---|----------|------------------|
| 1 | GET /api/courses sans auth | 200 OK |
| 2 | POST /api/courses sans auth | 401 |
| 3 | POST /api/courses avec USER | 403 |
| 4 | POST /api/courses avec ADMIN | 201 Created |
| 5 | DELETE /api/courses/{id} avec USER | 403 |
| 6 | DELETE /api/courses/{id} avec ADMIN | 204 |
| 7 | POST /api/enrollments/{id} avec USER | 201 |
| 8 | GET /api/enrollments/me avec USER | 200 (ses inscriptions) |
| 9 | Token expiré | 401 |
| 10 | Token invalide | 401 |

---

## 6. Étapes de réalisation

### Phase 1 : Setup (30 min)

1. Créer le projet Spring Boot
2. Ajouter les dépendances (Security, JPA, JWT)
3. Configurer la base de données (H2 pour dev)

### Phase 2 : Modèles et Repositories (30 min)

1. Créer les entités User, Course, Enrollment
2. Créer les repositories
3. Implémenter UserDetails dans User

### Phase 3 : Sécurité (1h)

1. Créer JwtService (génération, validation)
2. Créer JwtAuthFilter
3. Configurer SecurityFilterChain
4. Créer UserDetailsService

### Phase 4 : Auth (45 min)

1. Créer AuthController (register, login)
2. Créer les DTOs (LoginRequest, AuthResponse)
3. Tester avec Postman

### Phase 5 : Cours (30 min)

1. Créer CourseController (CRUD)
2. Créer CourseService
3. Tester les permissions

### Phase 6 : Inscriptions (30 min)

1. Créer EnrollmentController
2. Créer EnrollmentService
3. Implémenter la logique "propriétaire"

### Phase 7 : Tests (1h)

1. Écrire les tests d'intégration
2. Valider tous les scénarios 401/403
3. Tester les cas limites

---

## 7. Bonus : OAuth2 Login

Si tu veux aller plus loin, ajoute :

- Login avec Google
- Login avec GitHub

Cela nécessite :

1. Configurer les credentials OAuth2
2. Ajouter `spring-boot-starter-oauth2-client`
3. Mapper l'utilisateur externe vers ton User interne

---

## 8. Checklist finale

Avant de considérer le projet terminé :

- [ ] Tous les endpoints fonctionnent
- [ ] Les routes publiques sont accessibles sans auth
- [ ] Les routes ADMIN refusent les USER (403)
- [ ] Les routes protégées refusent sans token (401)
- [ ] Le token expiré retourne 401
- [ ] Les inscriptions ne montrent que les données du propriétaire
- [ ] Les réponses d'erreur sont en JSON
- [ ] CORS est configuré pour le front
- [ ] Les mots de passe sont hashés (BCrypt)
- [ ] Les tests passent

---

## 9. Résumé

Ce mini-projet couvre :

- **Authentification** : JWT, login, register
- **Autorisation** : rôles (USER/ADMIN), propriétaire de données
- **Sécurité API** : stateless, CORS, erreurs JSON
- **Tests** : scénarios 401/403/200
- **Architecture** : séparation des responsabilités

En complétant ce projet, tu auras une compréhension solide de Spring Security dans un contexte réel.

---

## Mini quiz final

1. Pourquoi un USER ne peut pas créer de cours même s'il est authentifié ?
2. Comment s'assurer qu'un USER ne voit que SES inscriptions ?
3. Quelle est la différence entre désactiver CSRF et le configurer correctement ?

