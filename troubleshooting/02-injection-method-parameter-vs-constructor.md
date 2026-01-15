# 02 - Injection par Paramètre de Méthode vs Constructeur

## 🔴 Contexte

Même après avoir extrait `UserDetailsService` dans une classe séparée (`UserDetailsConfig`), le cycle de dépendances persistait :

```
┌─────┐
|  jwtAuthFilter
↑     ↓
|  securityConfig
└─────┘
```

## 🔍 Analyse du problème

### Pourquoi le cycle persistait ?

Le problème venait de l'**injection par constructeur** de `JwtAuthFilter` dans `SecurityConfig` :

```java
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;  // ← Injection constructeur
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // ...
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        // ...
    }
}
```

### Ordre de création des beans par Spring

1. Spring scanne `SecurityConfig` (classe `@Configuration`)
2. Il voit que `SecurityConfig` a besoin de `JwtAuthFilter` (constructeur)
3. Il essaie de créer `JwtAuthFilter`
4. `JwtAuthFilter` a besoin de `UserDetailsService`
5. `UserDetailsService` est créé par `UserDetailsConfig` ✅
6. **MAIS** `JwtAuthFilter` est un `@Component` qui peut dépendre d'autres beans de sécurité...
7. Spring détecte une dépendance circulaire potentielle et refuse de continuer

### Le vrai problème : ordre d'initialisation

Avec `@RequiredArgsConstructor`, Spring doit créer **tous les beans injectés AVANT** de pouvoir instancier `SecurityConfig`. Cela crée une contrainte d'ordre stricte qui entre en conflit avec le cycle de vie des beans de sécurité.

## ✅ Solution : Injection par paramètre de méthode `@Bean`

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    // JwtAuthFilter N'EST PLUS injecté via constructeur

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        //                                                           ↑
        //                                          Injection via paramètre de méthode
        http
                .csrf(AbstractHttpConfigurer::disable)
                // ...
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

### Pourquoi ça fonctionne ?

1. Spring crée `SecurityConfig` **sans** avoir besoin de `JwtAuthFilter` (pas dans le constructeur)
2. Spring crée `JwtAuthFilter` séparément (c'est un `@Component`)
3. Quand Spring appelle la méthode `securityFilterChain()`, il **injecte** `JwtAuthFilter` comme paramètre
4. **Pas de cycle** car l'ordre de création est flexible

### Différence clé

| Injection Constructeur | Injection Paramètre Méthode |
|------------------------|----------------------------|
| Bean requis **avant** création de la classe | Bean requis **au moment** de l'appel |
| Ordre strict | Ordre flexible |
| Peut créer des cycles | Évite les cycles |

## 📊 Comparaison des approches

### ❌ Injection constructeur (problématique)

```java
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;  // Requis à l'instanciation
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        .addFilterBefore(jwtAuthFilter, ...);  // Utilise le champ
    }
}
```

**Timeline Spring :**
```
1. Besoin de SecurityConfig
2. → Besoin de JwtAuthFilter (constructeur)
3. → → Besoin de UserDetailsService
4. → → → Besoin de SecurityConfig (pour AuthenticationProvider?)
5. ❌ CYCLE DÉTECTÉ
```

### ✅ Injection paramètre méthode (solution)

```java
@RequiredArgsConstructor
public class SecurityConfig {
    // Pas de JwtAuthFilter ici
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) {
        .addFilterBefore(jwtAuthFilter, ...);  // Paramètre de méthode
    }
}
```

**Timeline Spring :**
```
1. Créer SecurityConfig ✅ (pas besoin de JwtAuthFilter)
2. Créer JwtAuthFilter ✅ (UserDetailsService disponible)
3. Appeler securityFilterChain(http, jwtAuthFilter) ✅
4. ✅ PAS DE CYCLE
```

## 🎯 Règle générale

> **Pour les classes `@Configuration` avec des dépendances complexes, préférer l'injection par paramètre de méthode `@Bean` plutôt que par constructeur.**

### Quand utiliser chaque approche ?

| Situation | Recommandation |
|-----------|---------------|
| Services simples (`@Service`, `@Component`) | Injection constructeur ✅ |
| Classes `@Configuration` avec filtres de sécurité | Injection paramètre méthode ✅ |
| Dépendances circulaires potentielles | Injection paramètre méthode ✅ |
| Beans avec ordre de création critique | Injection paramètre méthode ✅ |

## 📁 Fichiers modifiés

### `SecurityConfig.java`

```java
// AVANT
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // ...
    }
}

// APRÈS
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserDetailsService userDetailsService;
    // JwtAuthFilter retiré du constructeur

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        //                                                           ↑ Ajouté comme paramètre
        // ...
    }
}
```

## 📚 Leçons apprises

1. **L'injection par constructeur n'est pas toujours la meilleure option** pour les classes `@Configuration`.

2. **Spring injecte automatiquement les paramètres des méthodes `@Bean`** - c'est une fonctionnalité puissante et sous-utilisée.

3. **Les filtres de sécurité ont des cycles de vie complexes** qui peuvent créer des dépendances circulaires inattendues.

4. **Tester avec Docker** est important car le comportement peut différer entre l'IDE et le conteneur (ordre de chargement des classes).

## 🔗 Références

- [Spring Framework - Method Injection](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-method-injection.html)
- [Spring Security - Architecture](https://docs.spring.io/spring-security/reference/servlet/architecture.html)
- [Baeldung - Circular Dependencies in Spring](https://www.baeldung.com/circular-dependencies-in-spring)

