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

### Mini cours : C'est quoi un Filtre?

En Java web, un **filtre** est un composant qui intercepte les requêtes HTTP **avant** qu'elles n'atteignent le Controller. C'est comme un gardien à l'entrée d'un bâtiment.

```mermaid
graph LR
    A["Requête HTTP"] --> B["Filtre 1"]
    B --> C["Filtre 2"]
    C --> D["Filtre 3"]
    D --> E["Controller"]
    
    style B fill:#FF9800,color:#fff
    style C fill:#FF9800,color:#fff
    style D fill:#FF9800,color:#fff
```

**Problème** : Parfois, une même requête peut passer plusieurs fois par le même filtre (à cause des redirections internes). Pour éviter ça, Spring fournit `OncePerRequestFilter`.

### Mini cours : OncePerRequestFilter

`OncePerRequestFilter` est une classe abstraite de Spring qui garantit que votre filtre s'exécute **exactement une fois** par requête, peu importe les redirections.

| Classe | Comportement |
|--------|--------------|
| `Filter` (interface Java) | Peut s'exécuter plusieurs fois |
| `OncePerRequestFilter` (Spring) | S'exécute **une seule fois** |

Quand vous héritez de `OncePerRequestFilter`, vous devez implémenter la méthode `doFilterInternal()` :

```java
@Override
protected void doFilterInternal(
    HttpServletRequest request,   // La requête entrante
    HttpServletResponse response, // La réponse à renvoyer
    FilterChain filterChain       // La chaîne de filtres
) throws ServletException, IOException {
    // Votre logique ici
}
```

```mermaid
graph TB
    A["OncePerRequestFilter"]
    A --> B["Garantit UNE SEULE exécution<br/>par requête"]
    A --> C["Même si la requête<br/>passe par plusieurs filtres"]
    A --> D["Méthode à implémenter:<br/>doFilterInternal()"]
    
    style A fill:#4CAF50,color:#fff
```

---

### Question à compléter

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

### Mini cours : L'injection de dépendances

En Spring, au lieu de créer vos objets avec `new`, vous demandez à Spring de vous les fournir. C'est l'**injection de dépendances**.

**Sans injection (mauvais)** :
```java
public class JwtAuthFilter {
    private JwtService jwtService = new JwtService(); // ❌ Vous créez l'objet
}
```

**Avec injection (bon)** :
```java
public class JwtAuthFilter {
    private final JwtService jwtService; // ✅ Spring fournit l'objet
    
    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }
}
```

### Mini cours : @RequiredArgsConstructor de Lombok

`@RequiredArgsConstructor` génère automatiquement un constructeur avec tous les champs `final`. Ça évite d'écrire le constructeur à la main.

```java
@RequiredArgsConstructor  // Lombok génère le constructeur
public class JwtAuthFilter {
    private final JwtService jwtService;        // Injecté automatiquement
    private final UserDetailsService userDetailsService; // Injecté automatiquement
}
```

Équivalent à :
```java
public class JwtAuthFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    
    // Constructeur généré par Lombok
    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }
}
```

### De quoi notre filtre a-t-il besoin?

```mermaid
graph TB
    A["JwtAuthFilter a besoin de:"]
    A --> B["JwtService<br/>Pour décoder le JWT<br/>et extraire l'email"]
    A --> C["UserDetailsService<br/>Pour charger l'utilisateur<br/>depuis la base de données"]
    
    style B fill:#2196F3,color:#fff
    style C fill:#FF9800,color:#fff
```

| Service | Rôle |
|---------|------|
| `JwtService` | Décoder le JWT, extraire l'email, valider le token |
| `UserDetailsService` | Charger l'utilisateur depuis PostgreSQL |

---

### Question à compléter

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

- Les noms suivent la convention **camelCase**
- `final` = la référence ne peut pas changer après l'injection
- Grâce à `@RequiredArgsConstructor`, Spring injecte automatiquement ces services

</details>

---

## Étape 3 : Récupérer le header Authorization

### Mini cours : Les headers HTTP

Une requête HTTP contient plusieurs parties :

```
┌─────────────────────────────────────────────────┐
│ REQUÊTE HTTP                                     │
├─────────────────────────────────────────────────┤
│ Ligne de requête: GET /api/admin/leads HTTP/1.1 │
├─────────────────────────────────────────────────┤
│ HEADERS (en-têtes):                             │
│   Authorization: Bearer eyJhbGciOi...           │ ← Le JWT est ici!
│   Content-Type: application/json                │
│   Accept: */*                                   │
│   Host: localhost:8080                          │
├─────────────────────────────────────────────────┤
│ BODY (corps): { ... } (vide pour GET)           │
└─────────────────────────────────────────────────┘
```

### Mini cours : Le header Authorization

Le standard HTTP définit un header spécial appelé `Authorization` pour transmettre les informations d'authentification.

Pour JWT, le format est : `Bearer <token>`

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkB0ZXN0LmNvbSJ9.xxx
               ^^^^^^ ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
               Schéma                    Le token JWT
```

- **Bearer** = type d'authentification (porteur de token)
- **eyJ...** = le token JWT encodé en Base64

### Mini cours : HttpServletRequest

L'objet `request` (de type `HttpServletRequest`) représente la requête HTTP. Il a plusieurs méthodes utiles :

| Méthode | Usage |
|---------|-------|
| `getHeader("nom")` | Récupérer un header HTTP |
| `getParameter("nom")` | Récupérer un paramètre de l'URL (?nom=valeur) |
| `getMethod()` | GET, POST, PUT, DELETE |
| `getRequestURI()` | Le chemin (/api/admin/leads) |

```mermaid
graph TB
    A["HttpServletRequest"]
    A --> B["getHeader('Authorization')<br/>→ 'Bearer eyJ...'"]
    A --> C["getMethod()<br/>→ 'GET'"]
    A --> D["getRequestURI()<br/>→ '/api/admin/leads'"]
    
    style B fill:#4CAF50,color:#fff
```

---

### Question à compléter

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

- `getHeader()` récupère un header HTTP (pas `getParameter` qui est pour les query strings)
- Le header s'appelle `Authorization` (pas "Auth" ni "Bearer")
- Résultat : `"Bearer eyJhbGciOiJIUzI1NiJ9..."` ou `null` si absent

</details>

---

## Étape 4 : Vérifier le header

### Mini cours : Pourquoi vérifier?

Toutes les requêtes ne contiennent pas forcément un JWT :

| Type de requête | Header Authorization |
|-----------------|---------------------|
| `POST /api/contact` (public) | Absent (null) |
| `GET /api/admin/leads` (protégé) | `Bearer eyJ...` |
| Requête invalide | `Basic dXNlcjpwYXNz` (autre format) |

Notre filtre doit gérer tous ces cas sans planter.

### Mini cours : La méthode startsWith()

La méthode `startsWith()` de String vérifie si une chaîne commence par un préfixe :

```java
"Bearer eyJ...".startsWith("Bearer ")  // → true
"Basic xyz".startsWith("Bearer ")      // → false
"bearer eyJ...".startsWith("Bearer ") // → false (sensible à la casse!)
```

### Mini cours : Le pattern "early return"

Quand une condition n'est pas remplie, on sort de la méthode immédiatement avec `return`. C'est le pattern **early return** :

```java
// ❌ Sans early return (imbrication complexe)
if (authHeader != null) {
    if (authHeader.startsWith("Bearer ")) {
        // ... 50 lignes de code ...
    }
}

// ✅ Avec early return (plus lisible)
if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    filterChain.doFilter(request, response);
    return;  // On sort immédiatement
}
// ... code principal ici ...
```

```mermaid
graph TB
    A["Header Authorization"]
    A --> B{"authHeader == null?"}
    B -->|Oui| C["Pas de header<br/>→ Continuer sans auth"]
    B -->|Non| D{"startsWith('Bearer ')?"}
    D -->|Non| C
    D -->|Oui| E["JWT présent!<br/>→ Continuer le traitement"]
    
    style C fill:#FF9800,color:#fff
    style E fill:#4CAF50,color:#fff
```

---

### Question à compléter

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

- `authHeader == null` : vérifie si le header est absent
- `!authHeader.startsWith("Bearer ")` : vérifie que ça commence par "Bearer " (avec l'espace!)
- `||` : opérateur OU logique - si l'une des conditions est vraie, on entre dans le if
- `return` : on sort de la méthode immédiatement

</details>

---

## Étape 5 : Extraire le JWT

### Mini cours : La méthode substring()

La méthode `substring()` extrait une partie d'une chaîne de caractères.

```java
String texte = "Bonjour le monde";

texte.substring(0)    // → "Bonjour le monde" (tout)
texte.substring(8)    // → "le monde" (à partir de l'index 8)
texte.substring(0, 7) // → "Bonjour" (de l'index 0 à 6)
```

**Important** : Les index commencent à 0 en Java!

### Mini cours : Compter les caractères

```
Chaîne:  "Bearer eyJhbGci..."
Index:    0123456789...

B = index 0
e = index 1
a = index 2
r = index 3
e = index 4
r = index 5
  = index 6 (espace)
e = index 7 (début du JWT!)
```

Donc `"Bearer "` fait **7 caractères** (n'oubliez pas l'espace!).

### Visualisation

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkB0ZXN0LmNvbSJ9.xxx
               ├─────┤├──────────────────────────────────────────────────────────┤
               7 chars              Le JWT qu'on veut extraire
               
authHeader.substring(7) → "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkB0ZXN0LmNvbSJ9.xxx"
```

```mermaid
graph LR
    A["'Bearer eyJhbG...'"] --> B["substring(7)"]
    B --> C["'eyJhbG...'"]
    
    style A fill:#FF9800,color:#fff
    style C fill:#4CAF50,color:#fff
```

---

### Question à compléter

```java
final String jwt = authHeader.______(7);
```

**Quelle méthode pour extraire le JWT?**

<details>
<summary>Voir la réponse</summary>

```java
final String jwt = authHeader.substring(7);
```

- `substring(7)` prend tout à partir de l'index 7
- `"Bearer "` = 7 caractères (B-e-a-r-e-r-espace)
- Résultat : le JWT pur sans le préfixe "Bearer "

**Exemple** :
```java
"Bearer eyJabc123".substring(7) // → "eyJabc123"
```

</details>

---

## Étape 6 : Extraire l'email du JWT

### Mini cours : Structure d'un JWT

Un JWT (JSON Web Token) est composé de **3 parties** séparées par des points :

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkB0ZXN0LmNvbSIsImlhdCI6MTcwNTMxMjAwMH0.xxxSignature
├─────────────────────┤├──────────────────────────────────────────────────────────────┤├───────────┤
       HEADER                              PAYLOAD                                    SIGNATURE
```

**1. Header** (en-tête) - Informations sur l'algorithme :
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**2. Payload** (données) - Les informations de l'utilisateur :
```json
{
  "sub": "admin@test.com",   ← L'email est ici! (subject)
  "iat": 1705312000,         ← Date de création
  "exp": 1705398400          ← Date d'expiration
}
```

**3. Signature** - Pour vérifier que le token n'a pas été modifié.

### Mini cours : Le champ "sub" (subject)

Par convention, le champ `sub` (subject) contient l'identifiant unique de l'utilisateur. Dans notre cas, c'est l'email.

```mermaid
graph TB
    A["JWT décodé"]
    A --> B["Header: alg, typ"]
    A --> C["Payload"]
    A --> D["Signature"]
    
    C --> E["sub = 'admin@test.com'"]
    C --> F["iat = timestamp création"]
    C --> G["exp = timestamp expiration"]
    
    style E fill:#4CAF50,color:#fff
```

### Mini cours : JwtService.extractUsername()

Notre `JwtService` a une méthode `extractUsername()` qui :
1. Décode le JWT (Base64)
2. Lit le JSON du payload
3. Extrait la valeur du champ `sub`
4. Retourne l'email

```java
// Dans JwtService.java
public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);  // Récupère "sub"
}
```

---

### Question à compléter

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
1. Décode le JWT (Base64)
2. Parse le JSON du payload
3. Extrait le champ `sub` (subject)
4. Retourne l'email : `"admin@test.com"`

</details>

---

## Étape 7 : Vérifier si l'utilisateur n'est pas déjà authentifié

### Mini cours : SecurityContextHolder

`SecurityContextHolder` est comme une **boîte de stockage** qui garde les informations de sécurité de la requête en cours.

```mermaid
graph TB
    A["SecurityContextHolder"]
    A --> B["getContext()"]
    B --> C["SecurityContext"]
    C --> D["getAuthentication()"]
    D --> E["Authentication<br/>(ou null si pas connecté)"]
    
    style A fill:#9C27B0,color:#fff
    style E fill:#4CAF50,color:#fff
```

**Stockage thread-local** : Chaque requête HTTP a son propre thread, donc son propre SecurityContext. Les requêtes ne se mélangent pas.

### Mini cours : Pourquoi vérifier?

On fait **deux vérifications** :

| Vérification | Pourquoi? |
|--------------|-----------|
| `userEmail != null` | S'assurer que l'extraction du JWT a réussi |
| `getAuthentication() == null` | Éviter de ré-authentifier si déjà fait |

```mermaid
graph TB
    A["Vérification double"]
    A --> B{"userEmail != null?"}
    B -->|Non| C["JWT invalide/corrompu<br/>→ Ne pas authentifier"]
    B -->|Oui| D{"getAuthentication() == null?"}
    D -->|Non| E["Déjà authentifié<br/>→ Ne rien faire"]
    D -->|Oui| F["Procéder à l'authentification ✅"]
    
    style C fill:#f44336,color:#fff
    style E fill:#FF9800,color:#fff
    style F fill:#4CAF50,color:#fff
```

### Mini cours : L'opérateur &&

L'opérateur `&&` (ET logique) retourne `true` seulement si **les deux conditions** sont vraies.

```java
true && true   // → true
true && false  // → false
false && true  // → false (pas évalué grâce au "short-circuit")
```

**Short-circuit evaluation** : Si la première condition est `false`, Java ne vérifie même pas la deuxième.

---

### Question à compléter

```java
if (userEmail != null && SecurityContextHolder.getContext().______() == null) {
```

**Quelle méthode du SecurityContext?**

<details>
<summary>Voir la réponse</summary>

```java
if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
```

- `SecurityContextHolder.getContext()` : récupère le contexte de sécurité
- `getAuthentication()` : retourne l'objet Authentication actuel (ou null)
- Si `null`, personne n'est authentifié → on peut procéder

</details>

---

## Étape 8 : Charger l'utilisateur depuis la base de données

### Mini cours : Pourquoi charger l'utilisateur?

Le JWT contient l'email, mais pas toutes les informations de l'utilisateur (rôle, nom, etc.). On doit donc :

1. Extraire l'email du JWT
2. Chercher l'utilisateur complet dans la base de données
3. Récupérer ses rôles/permissions

```mermaid
graph TB
    A["JWT contient"]
    A --> B["email: admin@test.com"]
    A --> C["iat: timestamp"]
    A --> D["exp: timestamp"]
    
    E["Base de données contient"]
    E --> F["email: admin@test.com"]
    E --> G["password: $2a$10$hash..."]
    E --> H["role: ADMIN"]
    E --> I["firstName: Jean"]
    
    style H fill:#4CAF50,color:#fff
```

### Mini cours : UserDetailsService

`UserDetailsService` est une interface Spring Security avec **une seule méthode** :

```java
public interface UserDetailsService {
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
```

Dans notre projet, c'est configuré dans `UserDetailsConfig.java` :

```java
@Bean
public UserDetailsService userDetailsService() {
    return username -> userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
}
```

### Flux de chargement

```mermaid
graph TB
    A["loadUserByUsername('admin@test.com')"]
    A --> B["UserDetailsConfig"]
    B --> C["userRepository.findByEmail()"]
    C --> D["SELECT * FROM users<br/>WHERE email = ?"]
    D --> E["PostgreSQL"]
    E --> F["User(email, password, role=ADMIN)"]
    
    style A fill:#2196F3,color:#fff
    style F fill:#4CAF50,color:#fff
```

### Mini cours : UserDetails

`UserDetails` est une interface qui représente un utilisateur pour Spring Security :

```java
public interface UserDetails {
    String getUsername();
    String getPassword();
    Collection<? extends GrantedAuthority> getAuthorities();  // Les rôles!
    boolean isEnabled();
    // ...
}
```

Notre classe `User` implémente cette interface.

---

### Question à compléter

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
1. Prend l'email comme paramètre
2. Fait une requête SQL : `SELECT * FROM users WHERE email = ?`
3. Retourne l'objet `User` complet (avec le rôle!)
4. Lance `UsernameNotFoundException` si l'utilisateur n'existe pas

</details>

---

## Étape 9 : Vérifier si le token est valide

### Mini cours : Qu'est-ce qu'un token valide?

Un JWT peut être **invalide** pour plusieurs raisons :

| Problème | Description |
|----------|-------------|
| **Expiré** | La date `exp` est dépassée |
| **Signature invalide** | Quelqu'un a modifié le token |
| **Mauvais utilisateur** | L'email ne correspond pas |
| **Mal formé** | Le token n'est pas un JWT valide |

### Mini cours : isTokenValid()

Notre `JwtService` a une méthode qui vérifie tout ça :

```java
// Dans JwtService.java
public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
}
```

**Deux vérifications** :
1. L'email dans le JWT = l'email de l'utilisateur chargé?
2. Le token n'est pas expiré?

```mermaid
graph TB
    A["isTokenValid(jwt, userDetails)"]
    A --> B["extractUsername(jwt)"]
    B --> C{"email JWT == email DB?"}
    C -->|Non| D["INVALIDE ❌"]
    C -->|Oui| E["isTokenExpired(jwt)"]
    E --> F{"Expiré?"}
    F -->|Oui| D
    F -->|Non| G["VALIDE ✅"]
    
    style D fill:#f44336,color:#fff
    style G fill:#4CAF50,color:#fff
```

### Mini cours : Pourquoi comparer les emails?

Scénario d'attaque possible :
1. Hacker récupère un vieux JWT de l'utilisateur A
2. L'utilisateur A est supprimé de la base
3. Hacker essaie d'utiliser le JWT
4. `loadUserByUsername` échoue → sécurisé!

Ou pire :
1. Hacker modifie le JWT pour mettre un autre email
2. Mais la signature sera invalide → sécurisé!

---

### Question à compléter

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
1. Que l'email dans le JWT = email de l'utilisateur en base
2. Que le token n'est pas expiré (`exp` > maintenant)
3. La signature est vérifiée automatiquement lors du décodage

Retourne `true` si tout est OK, `false` sinon.

</details>

---

## Étape 10 : Créer l'objet Authentication

### Mini cours : C'est quoi Authentication?

`Authentication` est un objet qui représente **un utilisateur authentifié**. Il contient :

```mermaid
graph TB
    A["Authentication"]
    A --> B["Principal<br/>= L'utilisateur (qui?)"]
    A --> C["Credentials<br/>= Le mot de passe (comment?)"]
    A --> D["Authorities<br/>= Les permissions (quoi?)"]
    A --> E["isAuthenticated()<br/>= true/false"]
    
    style B fill:#4CAF50,color:#fff
    style D fill:#2196F3,color:#fff
```

### Mini cours : UsernamePasswordAuthenticationToken

C'est l'implémentation la plus courante de `Authentication`. Son constructeur a **3 arguments** :

```java
new UsernamePasswordAuthenticationToken(
    principal,      // Qui est l'utilisateur?
    credentials,    // Comment s'est-il authentifié?
    authorities     // Quels sont ses droits?
)
```

| Argument | Type | Notre valeur | Explication |
|----------|------|--------------|-------------|
| `principal` | Object | `userDetails` | L'objet User complet |
| `credentials` | Object | `null` | Pas besoin, JWT déjà validé |
| `authorities` | Collection | `userDetails.getAuthorities()` | Liste des rôles |

### Mini cours : getAuthorities()

Notre classe `User` a une méthode `getAuthorities()` qui retourne les rôles :

```java
// Dans User.java
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    // Retourne: [ROLE_ADMIN] ou [ROLE_SUPER_ADMIN]
}
```

Ces rôles seront utilisés par `hasRole("ADMIN")` dans SecurityConfig.

### Mini cours : Pourquoi null pour credentials?

```mermaid
graph LR
    A["Authentification classique"]
    A --> B["username + password"]
    B --> C["Vérifier le password"]
    
    D["Notre cas (JWT)"]
    D --> E["JWT déjà validé"]
    E --> F["Pas besoin de password"]
    F --> G["credentials = null"]
    
    style G fill:#4CAF50,color:#fff
```

Le JWT a **déjà prouvé** l'identité de l'utilisateur. Pas besoin de re-vérifier le mot de passe.

---

### Question à compléter

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

- `userDetails` : l'objet User chargé depuis la base de données
- `null` : on n'a pas besoin du mot de passe, le JWT a déjà prouvé l'identité
- `getAuthorities()` : retourne `[ROLE_ADMIN]` pour que `hasRole("ADMIN")` fonctionne

</details>

---

## Étape 11 : Ajouter les détails de la requête

### Mini cours : Pourquoi des détails supplémentaires?

L'objet Authentication peut contenir des **métadonnées** sur la requête. C'est utile pour :

- **Logging** : savoir d'où vient la requête
- **Audit** : tracer les actions des utilisateurs
- **Sécurité** : détecter les comportements suspects

### Mini cours : WebAuthenticationDetailsSource

Cette classe extrait automatiquement des informations de la requête HTTP :

```mermaid
graph TB
    A["WebAuthenticationDetailsSource"]
    A --> B["buildDetails(request)"]
    B --> C["WebAuthenticationDetails"]
    C --> D["Adresse IP du client"]
    C --> E["Session ID"]
    C --> F["Autres métadonnées"]
    
    style C fill:#FF9800,color:#fff
```

### Mini cours : setDetails()

`authToken.setDetails()` permet d'attacher ces informations à l'Authentication :

```java
// Crée un objet avec les détails de la requête
WebAuthenticationDetails details = new WebAuthenticationDetailsSource().buildDetails(request);

// Attache ces détails à l'Authentication
authToken.setDetails(details);
```

Plus tard, vous pourrez récupérer ces infos :
```java
WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails();
String ip = details.getRemoteAddress();  // Adresse IP du client
```

---

### Question à compléter

```java
authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(______));
```

**Quel objet passer à buildDetails?**

<details>
<summary>Voir la réponse</summary>

```java
authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
```

- `request` est l'objet `HttpServletRequest` reçu en paramètre de `doFilterInternal()`
- `buildDetails(request)` extrait l'IP, la session, etc.
- Ces infos sont attachées à l'Authentication pour le logging/audit

</details>

---

## Étape 12 : Stocker dans le SecurityContext

### Mini cours : Où stocker l'Authentication?

Une fois l'Authentication créée, il faut la **stocker** quelque part pour que :
- Les autres filtres y aient accès
- Le Controller puisse savoir qui est connecté
- `hasRole("ADMIN")` puisse vérifier les permissions

### Mini cours : SecurityContextHolder

`SecurityContextHolder` est un **conteneur global** (thread-local) qui stocke le contexte de sécurité :

```mermaid
graph TB
    A["Thread de la requête"]
    A --> B["SecurityContextHolder"]
    B --> C["getContext()"]
    C --> D["SecurityContext"]
    D --> E["Authentication"]
    E --> F["Principal (User)"]
    E --> G["Authorities (Rôles)"]
    
    style B fill:#9C27B0,color:#fff
    style E fill:#4CAF50,color:#fff
```

### Mini cours : Thread-local

**Thread-local** signifie que chaque requête HTTP a son propre espace de stockage :

```
Requête 1 (admin@test.com) → Thread 1 → SecurityContext 1 → Auth(ADMIN)
Requête 2 (user@test.com)  → Thread 2 → SecurityContext 2 → Auth(USER)
Requête 3 (pas de JWT)     → Thread 3 → SecurityContext 3 → null
```

Les requêtes ne peuvent pas voir les données des autres requêtes.

### Mini cours : setAuthentication()

```java
SecurityContextHolder.getContext().setAuthentication(authToken);
```

Cette ligne fait 3 choses :
1. `SecurityContextHolder` : accède au stockage global
2. `.getContext()` : récupère le contexte de cette requête
3. `.setAuthentication(authToken)` : stocke notre objet Authentication

**Après cette ligne**, tout le code peut savoir :
- Que l'utilisateur est connecté
- Qui il est (`getPrincipal()`)
- Quels sont ses rôles (`getAuthorities()`)

---

### Question à compléter

```java
SecurityContextHolder.getContext().______(authToken);
```

**Quelle méthode pour stocker l'authentication?**

<details>
<summary>Voir la réponse</summary>

```java
SecurityContextHolder.getContext().setAuthentication(authToken);
```

- `SecurityContextHolder` : le conteneur thread-local de Spring Security
- `getContext()` : récupère le SecurityContext de cette requête
- `setAuthentication(authToken)` : stocke l'Authentication

Après cette ligne, Spring Security considère l'utilisateur comme **authentifié** pour le reste de la requête.

</details>

---

## Étape 13 : Continuer la chaîne de filtres

### Mini cours : La chaîne de filtres

En Java web, les filtres sont organisés en **chaîne**. Chaque filtre doit passer la requête au suivant :

```mermaid
graph TB
    A["Requête HTTP"]
    A --> B["Filtre CORS"]
    B --> C["Filtre CSRF"]
    C --> D["JwtAuthFilter<br/>(notre filtre)"]
    D --> E["Filtre Autorisation"]
    E --> F["Controller"]
    F --> G["Réponse"]
    
    style D fill:#E91E63,color:#fff
```

### Mini cours : filterChain.doFilter()

La méthode `doFilter()` dit : **"J'ai fini mon travail, passe au suivant"**.

```java
filterChain.doFilter(request, response);
```

- `filterChain` : représente la chaîne de filtres
- `doFilter(request, response)` : passe la requête au prochain filtre
- Si c'est le dernier filtre, la requête va au Controller

### Mini cours : ATTENTION - Ne jamais oublier doFilter()!

```java
// ❌ ERREUR : On oublie doFilter()
protected void doFilterInternal(...) {
    // ... traitement ...
    // Oups! Pas de doFilter()
}
// Résultat : La requête est BLOQUÉE. Le client attend indéfiniment.

// ✅ CORRECT : Toujours appeler doFilter()
protected void doFilterInternal(...) {
    // ... traitement ...
    filterChain.doFilter(request, response);  // OBLIGATOIRE!
}
```

### Mini cours : Quand appeler doFilter()?

On appelle `doFilter()` dans **tous les cas** :

```mermaid
graph TB
    A["Début du filtre"]
    A --> B{"JWT présent?"}
    B -->|Non| C["doFilter() → sans auth"]
    B -->|Oui| D{"JWT valide?"}
    D -->|Non| E["doFilter() → sans auth"]
    D -->|Oui| F["Créer Authentication"]
    F --> G["Stocker dans SecurityContext"]
    G --> H["doFilter() → avec auth"]
    
    style C fill:#FF9800,color:#fff
    style E fill:#FF9800,color:#fff
    style H fill:#4CAF50,color:#fff
```

**Dans tous les chemins**, on finit par `doFilter()`.

---

### Question à compléter

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
- **CRITIQUE** : Cette ligne doit TOUJOURS être appelée!
- Si vous l'oubliez, le client attend indéfiniment (timeout)

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

