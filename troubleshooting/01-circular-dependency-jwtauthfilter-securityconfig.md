# 01 - Dépendance Circulaire : JwtAuthFilter ↔ SecurityConfig

## 🔴 Erreur rencontrée

```
APPLICATION FAILED TO START
***************************

Description:

The dependencies of some of the beans in the application context form a cycle:

┌─────┐
|  jwtAuthFilter defined in URL [jar:nested:/app/app.jar/!BOOT-INF/classes/!/com/example/contact/security/JwtAuthFilter.class]
↑     ↓
|  securityConfig defined in URL [jar:nested:/app/app.jar/!BOOT-INF/classes/!/com/example/contact/config/SecurityConfig.class]
└─────┘

Action:

Relying upon circular references is discouraged and they are prohibited by default.
```

## 🔍 Analyse du problème

### Le cycle de dépendances

```
SecurityConfig
      │
      │ injecte via @RequiredArgsConstructor
      ▼
JwtAuthFilter
      │
      │ injecte via @RequiredArgsConstructor
      ▼
UserDetailsService (bean)
      │
      │ défini comme @Bean dans
      ▼
SecurityConfig  ← RETOUR AU POINT DE DÉPART = CYCLE !
```

### Code problématique

**SecurityConfig.java** (avant correction) :
```java
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;      // ① Injecte JwtAuthFilter
    private final UserRepository userRepository;

    // ...

    @Bean
    public UserDetailsService userDetailsService() { // ③ Définit UserDetailsService ici
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
    }
}
```

**JwtAuthFilter.java** :
```java
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;  // ② A besoin de UserDetailsService
}
```

### Pourquoi Spring ne peut pas résoudre ce cycle ?

1. Pour créer `SecurityConfig`, Spring doit d'abord créer `JwtAuthFilter`
2. Pour créer `JwtAuthFilter`, Spring doit d'abord créer `UserDetailsService`
3. Pour créer `UserDetailsService`, Spring doit d'abord créer `SecurityConfig` (car c'est un `@Bean` dedans)
4. **BLOCAGE** : Spring ne peut pas créer `SecurityConfig` car il attend `JwtAuthFilter` !

## ✅ Solution appliquée

**Extraire le bean `UserDetailsService` dans une classe de configuration séparée.**

### Nouveau fichier : `UserDetailsConfig.java`

```java
package com.example.contact.config;

import com.example.contact.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
@RequiredArgsConstructor
public class UserDetailsConfig {

    private final UserRepository userRepository;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
    }
}
```

### SecurityConfig.java modifié

```java
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;  // Injecté depuis UserDetailsConfig

    // Le @Bean userDetailsService() a été SUPPRIMÉ d'ici

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);  // Utilise le champ injecté
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
```

### Nouveau flux de dépendances (sans cycle)

```
UserDetailsConfig
      │
      │ définit @Bean
      ▼
UserDetailsService
      │
      │ injecté dans
      ▼
JwtAuthFilter
      │
      │ injecté dans
      ▼
SecurityConfig  ✅ PAS DE CYCLE !
```

## 🎯 Alternatives possibles

### Alternative 1 : Utiliser `@Lazy`

```java
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    
    @Lazy  // Retarde l'injection jusqu'au premier usage
    private final UserDetailsService userDetailsService;
}
```

**Inconvénient** : Cache le problème architectural plutôt que de le résoudre.

### Alternative 2 : Injection par setter

```java
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
}
```

**Inconvénient** : Moins propre, le champ n'est plus `final`.

### Alternative 3 : Injecter `UserRepository` directement

```java
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;  // Direct au lieu de UserDetailsService
    
    // Dans doFilterInternal:
    UserDetails userDetails = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new UsernameNotFoundException("..."));
}
```

**Inconvénient** : Duplique la logique de chargement d'utilisateur.

## 📚 Leçons apprises

1. **Éviter de définir des beans dans les classes `@Configuration` qui injectent d'autres composants** dépendants de ces beans.

2. **Séparer les responsabilités** : Une classe de configuration pour la sécurité HTTP, une autre pour les services d'authentification.

3. **Spring Boot 2.6+ interdit les cycles par défaut** (avant, ils étaient autorisés avec un warning).

4. **Ne jamais utiliser `spring.main.allow-circular-references=true`** en production - c'est un pansement, pas une solution.

## 🔗 Références

- [Spring Framework - Circular Dependencies](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html#beans-dependency-resolution)
- [Spring Boot 2.6 Release Notes - Circular References](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.6-Release-Notes#circular-references-prohibited-by-default)

