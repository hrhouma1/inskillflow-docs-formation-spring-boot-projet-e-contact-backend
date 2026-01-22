# Chapitre 5.4 - Exercice pratique : Créer JwtAuthFilter étape par étape

## Objectifs du chapitre

- Comprendre le rôle d'un filtre de sécurité
- Écrire JwtAuthFilter de zéro
- Compléter le code avec les parties manquantes
- Valider votre compréhension avec un quiz

---

## Contexte de l'exercice

Vous travaillez sur une API REST sécurisée avec JWT. Vous devez créer le filtre `JwtAuthFilter` qui :
1. Intercepte chaque requête HTTP
2. Vérifie si un token JWT est présent
3. Valide le token
4. Authentifie l'utilisateur

---

## Vue d'ensemble : Que fait JwtAuthFilter?

```mermaid
graph TB
    A["Requête HTTP arrive"]
    A --> B{"Header Authorization<br/>présent?"}
    B -->|Non| C["Continuer sans auth"]
    B -->|Oui| D{"Commence par<br/>'Bearer '?"}
    D -->|Non| C
    D -->|Oui| E["Extraire le JWT"]
    E --> F["Extraire l'email du JWT"]
    F --> G["Charger l'utilisateur"]
    G --> H{"Token valide?"}
    H -->|Non| C
    H -->|Oui| I["Créer Authentication"]
    I --> J["Stocker dans SecurityContext"]
    J --> C
    C --> K["filterChain.doFilter()"]
    
    style A fill:#2196F3,color:#fff
    style I fill:#4CAF50,color:#fff
    style J fill:#4CAF50,color:#fff
    style C fill:#FF9800,color:#fff
```

---

## Étape 0 : Le squelette de la classe (À COMPLÉTER)

Voici le code avec des parties manquantes. Votre mission : compléter les `______`.

```java
package com.example.contact.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends ______ {  // ÉTAPE 1

    private final JwtService ______;           // ÉTAPE 2
    private final UserDetailsService ______;   // ÉTAPE 2

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // ÉTAPE 3 : Récupérer le header Authorization
        final String authHeader = request.______("______");
        
        // ÉTAPE 4 : Vérifier si le header existe et commence par "Bearer "
        if (______ == null || !authHeader.______("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // ÉTAPE 5 : Extraire le JWT (enlever "Bearer ")
        final String jwt = authHeader.______(7);
        
        // ÉTAPE 6 : Extraire l'email du JWT
        final String userEmail = jwtService.______(jwt);

        // ÉTAPE 7 : Vérifier si l'email existe et si l'utilisateur n'est pas déjà authentifié
        if (userEmail != null && SecurityContextHolder.getContext().______() == null) {
            
            // ÉTAPE 8 : Charger l'utilisateur depuis la base de données
            UserDetails userDetails = this.userDetailsService.______(userEmail);

            // ÉTAPE 9 : Vérifier si le token est valide
            if (jwtService.______(jwt, userDetails)) {
                
                // ÉTAPE 10 : Créer l'objet Authentication
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        ______,      // Le principal (l'utilisateur)
                        null,        // Les credentials (null car déjà authentifié via JWT)
                        userDetails.______()  // Les autorités/rôles
                );
                
                // ÉTAPE 11 : Ajouter les détails de la requête
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(______));
                
                // ÉTAPE 12 : Stocker l'authentication dans le SecurityContext
                SecurityContextHolder.getContext().______(authToken);
            }
        }

        // ÉTAPE 13 : Continuer la chaîne de filtres
        filterChain.______(request, response);
    }
}
```

---

## Étape 1 : Hériter de OncePerRequestFilter

### Pourquoi OncePerRequestFilter?

```mermaid
graph TB
    A["OncePerRequestFilter"]
    A --> B["Garantit UNE SEULE exécution<br/>par requête"]
    A --> C["Même si la requête<br/>passe par plusieurs filtres"]
    A --> D["Méthode à implémenter:<br/>doFilterInternal()"]
    
    style A fill:#4CAF50,color:#fff
```

### Question

```java
public class JwtAuthFilter extends ______ {
```

**Que mettre à la place de `______`?**

- a) `Filter`
- b) `OncePerRequestFilter`
- c) `HttpFilter`
- d) `SecurityFilter`

<details>
<summary>Voir la réponse</summary>

**Réponse : b) `OncePerRequestFilter`**

```java
public class JwtAuthFilter extends OncePerRequestFilter {
```

`OncePerRequestFilter` garantit que notre filtre s'exécute **une seule fois par requête**, même si la requête est forwardée ou redirigée.

</details>

---

## Étape 2 : Injecter les dépendances

### De quoi avons-nous besoin?

```mermaid
graph TB
    A["JwtAuthFilter a besoin de:"]
    A --> B["JwtService<br/>Pour décoder/valider le JWT"]
    A --> C["UserDetailsService<br/>Pour charger l'utilisateur"]
    
    style B fill:#2196F3,color:#fff
    style C fill:#FF9800,color:#fff
```

### Question

```java
private final JwtService ______;
private final UserDetailsService ______;
```

**Quels noms de variables utiliser?**

<details>
<summary>Voir la réponse</summary>

```java
private final JwtService jwtService;
private final UserDetailsService userDetailsService;
```

Grâce à `@RequiredArgsConstructor` de Lombok, Spring injectera automatiquement ces dépendances via le constructeur.

</details>

---

## Étape 3 : Récupérer le header Authorization

### Comment ça fonctionne?

```mermaid
graph LR
    A["Requête HTTP"] --> B["Headers"]
    B --> C["Authorization: Bearer eyJ..."]
    B --> D["Content-Type: application/json"]
    B --> E["Accept: */*"]
    
    style C fill:#4CAF50,color:#fff
```

### Question

```java
final String authHeader = request.______("______");
```

**Quelle méthode et quel nom de header?**

- a) `getParameter("Authorization")`
- b) `getHeader("Authorization")`
- c) `getAttribute("Auth")`
- d) `getHeader("Bearer")`

<details>
<summary>Voir la réponse</summary>

**Réponse : b) `getHeader("Authorization")`**

```java
final String authHeader = request.getHeader("Authorization");
```

- `getHeader()` récupère un header HTTP
- Le header s'appelle `Authorization` (standard HTTP)
- Sa valeur ressemble à : `Bearer eyJhbGciOiJIUzI1NiJ9...`

</details>

---

## Étape 4 : Vérifier le header

### Pourquoi cette vérification?

```mermaid
graph TB
    A["Header Authorization"]
    A --> B{"Existe?"}
    B -->|Non| C["Pas de JWT<br/>→ Continuer sans auth"]
    B -->|Oui| D{"Commence par<br/>'Bearer '?"}
    D -->|Non| C
    D -->|Oui| E["JWT présent!<br/>→ Continuer le traitement"]
    
    style C fill:#FF9800,color:#fff
    style E fill:#4CAF50,color:#fff
```

### Question

```java
if (______ == null || !authHeader.______("Bearer ")) {
    filterChain.doFilter(request, response);
    return;
}
```

**Complétez les blancs :**

<details>
<summary>Voir la réponse</summary>

```java
if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    filterChain.doFilter(request, response);
    return;
}
```

- On vérifie si `authHeader` est `null` (pas de header)
- On vérifie si ça commence par `"Bearer "` (avec l'espace!)
- Si non, on continue sans authentification

</details>

---

## Étape 5 : Extraire le JWT

### Structure du header

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkB0ZXN0LmNvbSJ9.xxx
               ↑      ↑
               |      |
            Index 0   Index 7 (après "Bearer ")
```

### Question

```java
final String jwt = authHeader.______(7);
```

**Quelle méthode pour extraire le JWT?**

<details>
<summary>Voir la réponse</summary>

```java
final String jwt = authHeader.substring(7);
```

- `"Bearer "` fait 7 caractères (B-e-a-r-e-r-espace)
- `substring(7)` retourne tout à partir de l'index 7
- Résultat : le JWT pur sans "Bearer "

</details>

---

## Étape 6 : Extraire l'email du JWT

### Structure d'un JWT

```mermaid
graph LR
    A["JWT"] --> B["Header<br/>(algorithme)"]
    A --> C["Payload<br/>(données)"]
    A --> D["Signature"]
    
    C --> E["sub: admin@test.com<br/>iat: 1705312000<br/>exp: 1705398400"]
    
    style C fill:#4CAF50,color:#fff
    style E fill:#4CAF50,color:#fff
```

### Question

```java
final String userEmail = jwtService.______(jwt);
```

**Quelle méthode de JwtService utiliser?**

<details>
<summary>Voir la réponse</summary>

```java
final String userEmail = jwtService.extractUsername(jwt);
```

Cette méthode :
1. Décode le JWT
2. Extrait le champ `subject` (sub)
3. Retourne l'email de l'utilisateur

</details>

---

## Étape 7 : Vérifier si l'utilisateur n'est pas déjà authentifié

### Pourquoi cette vérification?

```mermaid
graph TB
    A["Vérification double"]
    A --> B["userEmail != null<br/>Email extrait avec succès"]
    A --> C["getAuthentication() == null<br/>Pas encore authentifié"]
    
    B --> D["Si les deux sont vrais<br/>→ Procéder à l'authentification"]
    
    style D fill:#4CAF50,color:#fff
```

### Question

```java
if (userEmail != null && SecurityContextHolder.getContext().______() == null) {
```

**Quelle méthode du SecurityContext?**

<details>
<summary>Voir la réponse</summary>

```java
if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
```

- `getAuthentication()` retourne l'objet Authentication actuel
- Si `null`, personne n'est encore authentifié pour cette requête
- On évite de ré-authentifier si déjà fait

</details>

---

## Étape 8 : Charger l'utilisateur depuis la base de données

### Flux de chargement

```mermaid
graph TB
    A["userDetailsService.loadUserByUsername(email)"]
    A --> B["UserDetailsConfig.userDetailsService()"]
    B --> C["userRepository.findByEmail(email)"]
    C --> D["PostgreSQL"]
    D --> E["User (implements UserDetails)"]
    
    style A fill:#2196F3,color:#fff
    style E fill:#4CAF50,color:#fff
```

### Question

```java
UserDetails userDetails = this.userDetailsService.______(userEmail);
```

**Quelle méthode de UserDetailsService?**

<details>
<summary>Voir la réponse</summary>

```java
UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
```

Cette méthode :
1. Cherche l'utilisateur par son email dans la base de données
2. Retourne un objet `UserDetails` (notre classe `User`)
3. Lance une exception si l'utilisateur n'existe pas

</details>

---

## Étape 9 : Vérifier si le token est valide

### Qu'est-ce qu'un token valide?

```mermaid
graph TB
    A["isTokenValid(jwt, userDetails)"]
    A --> B["1. L'email dans le JWT<br/>= email de l'utilisateur?"]
    A --> C["2. Le token n'est pas expiré?"]
    
    B --> D{"Les deux OK?"}
    C --> D
    D -->|Oui| E["Token VALIDE ✅"]
    D -->|Non| F["Token INVALIDE ❌"]
    
    style E fill:#4CAF50,color:#fff
    style F fill:#f44336,color:#fff
```

### Question

```java
if (jwtService.______(jwt, userDetails)) {
```

**Quelle méthode de JwtService?**

<details>
<summary>Voir la réponse</summary>

```java
if (jwtService.isTokenValid(jwt, userDetails)) {
```

Cette méthode vérifie :
1. Que l'email dans le JWT correspond à l'utilisateur chargé
2. Que le token n'est pas expiré
3. Que la signature est valide

</details>

---

## Étape 10 : Créer l'objet Authentication

### Structure de UsernamePasswordAuthenticationToken

```mermaid
graph TB
    A["UsernamePasswordAuthenticationToken"]
    A --> B["Principal<br/>(l'utilisateur)"]
    A --> C["Credentials<br/>(null - déjà auth via JWT)"]
    A --> D["Authorities<br/>(les rôles)"]
    
    style B fill:#4CAF50,color:#fff
    style D fill:#2196F3,color:#fff
```

### Question

```java
UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
        ______,      // Le principal
        null,        
        userDetails.______()  // Les autorités
);
```

**Complétez les blancs :**

<details>
<summary>Voir la réponse</summary>

```java
UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
        userDetails,              // Le principal (l'utilisateur complet)
        null,                     // Credentials (null car JWT déjà validé)
        userDetails.getAuthorities()  // Les rôles [ROLE_ADMIN]
);
```

- `userDetails` : l'objet User complet
- `null` : pas besoin de mot de passe, le JWT a déjà été validé
- `getAuthorities()` : retourne la liste des rôles (ex: [ROLE_ADMIN])

</details>

---

## Étape 11 : Ajouter les détails de la requête

### Pourquoi?

```mermaid
graph TB
    A["WebAuthenticationDetailsSource"]
    A --> B["Ajoute des infos sur la requête:"]
    B --> C["Adresse IP du client"]
    B --> D["Session ID"]
    B --> E["Autres métadonnées"]
    
    style A fill:#FF9800,color:#fff
```

### Question

```java
authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(______));
```

**Quel objet passer à buildDetails?**

<details>
<summary>Voir la réponse</summary>

```java
authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
```

On passe l'objet `request` (HttpServletRequest) pour que Spring puisse extraire les détails de la requête HTTP.

</details>

---

## Étape 12 : Stocker dans le SecurityContext

### Le SecurityContext

```mermaid
graph TB
    A["SecurityContextHolder"]
    A --> B["getContext()"]
    B --> C["SecurityContext"]
    C --> D["setAuthentication(authToken)"]
    D --> E["L'utilisateur est maintenant<br/>authentifié pour cette requête!"]
    
    style E fill:#4CAF50,color:#fff
```

### Question

```java
SecurityContextHolder.getContext().______(authToken);
```

**Quelle méthode pour stocker l'authentication?**

<details>
<summary>Voir la réponse</summary>

```java
SecurityContextHolder.getContext().setAuthentication(authToken);
```

- `SecurityContextHolder` : stockage thread-local de la sécurité
- `getContext()` : récupère le contexte de sécurité
- `setAuthentication()` : stocke l'objet Authentication

Après cette ligne, Spring Security sait que l'utilisateur est authentifié!

</details>

---

## Étape 13 : Continuer la chaîne de filtres

### La chaîne de filtres

```mermaid
graph TB
    A["JwtAuthFilter"]
    A --> B["filterChain.doFilter()"]
    B --> C["Prochain filtre"]
    C --> D["..."]
    D --> E["Controller"]
    
    style A fill:#E91E63,color:#fff
    style B fill:#4CAF50,color:#fff
```

### Question

```java
filterChain.______(request, response);
```

**Quelle méthode pour passer au filtre suivant?**

<details>
<summary>Voir la réponse</summary>

```java
filterChain.doFilter(request, response);
```

- `doFilter()` passe la requête au prochain filtre dans la chaîne
- Finalement, la requête atteindra le Controller
- **IMPORTANT** : Cette ligne doit TOUJOURS être appelée, sinon la requête reste bloquée!

</details>

---

## Solution complète

<details>
<summary>Cliquez pour voir la solution complète</summary>

```java
package com.example.contact.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Récupérer le header Authorization
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Vérifier si le header existe et commence par "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraire le JWT (enlever "Bearer ")
        jwt = authHeader.substring(7);

        try {
            // Extraire l'email du JWT
            userEmail = jwtService.extractUsername(jwt);

            // Vérifier si l'email existe et si l'utilisateur n'est pas déjà authentifié
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Charger l'utilisateur depuis la base de données
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Vérifier si le token est valide
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    
                    // Créer l'objet Authentication
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    
                    // Ajouter les détails de la requête
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Stocker l'authentication dans le SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token invalide - continuer sans authentification
        }

        // Continuer la chaîne de filtres
        filterChain.doFilter(request, response);
    }
}
```

</details>

---

## Récapitulatif visuel

```mermaid
graph TB
    A["1. Requête arrive"]
    A --> B["2. getHeader('Authorization')"]
    B --> C["3. startsWith('Bearer ')?"]
    C --> D["4. substring(7) → JWT"]
    D --> E["5. extractUsername(jwt)"]
    E --> F["6. loadUserByUsername(email)"]
    F --> G["7. isTokenValid(jwt, user)"]
    G --> H["8. new AuthenticationToken()"]
    H --> I["9. setAuthentication()"]
    I --> J["10. doFilter() → Controller"]
    
    style A fill:#2196F3,color:#fff
    style E fill:#FF9800,color:#fff
    style G fill:#E91E63,color:#fff
    style I fill:#4CAF50,color:#fff
    style J fill:#4CAF50,color:#fff
```

---

## QUIZ Final

**1. Pourquoi hériter de OncePerRequestFilter?**
- a) Pour filtrer plusieurs fois
- b) Pour garantir une seule exécution par requête
- c) Pour améliorer les performances
- d) C'est obligatoire

<details>
<summary>Voir la réponse</summary>

**Réponse : b) Pour garantir une seule exécution par requête**
</details>

---

**2. Que retourne `authHeader.substring(7)` si authHeader = "Bearer eyJxxx"?**
- a) "Bearer "
- b) "Bearer eyJxxx"
- c) "eyJxxx"
- d) Une erreur

<details>
<summary>Voir la réponse</summary>

**Réponse : c) "eyJxxx"**

`substring(7)` enlève les 7 premiers caractères ("Bearer ").
</details>

---

**3. Pourquoi passe-t-on `null` comme credentials dans UsernamePasswordAuthenticationToken?**
- a) Bug dans le code
- b) Le mot de passe est secret
- c) Le JWT a déjà validé l'identité
- d) Spring l'ignore

<details>
<summary>Voir la réponse</summary>

**Réponse : c) Le JWT a déjà validé l'identité**

Le token JWT prouve déjà que l'utilisateur est authentifié. Pas besoin de re-vérifier le mot de passe.
</details>

---

**4. Que se passe-t-il si on oublie `filterChain.doFilter()`?**
- a) Rien de grave
- b) La requête est bloquée et ne répond jamais
- c) Le controller s'exécute quand même
- d) Une exception est levée

<details>
<summary>Voir la réponse</summary>

**Réponse : b) La requête est bloquée et ne répond jamais**

`doFilter()` passe la requête au prochain filtre. Sans ça, la chaîne s'arrête et le client attend indéfiniment.
</details>

---

**5. Où est stockée l'Authentication après validation?**
- a) Dans la session HTTP
- b) Dans un cookie
- c) Dans SecurityContextHolder
- d) Dans la base de données

<details>
<summary>Voir la réponse</summary>

**Réponse : c) SecurityContextHolder**

`SecurityContextHolder` est un stockage thread-local qui garde l'Authentication pour la durée de la requête.
</details>

---

## Navigation

| Précédent | Suivant |
|-----------|---------|
| [24 - SecurityConfig en détail](24-security-config-detail.md) | [26 - Exercice JwtService](26-exercice-jwtservice.md) |

