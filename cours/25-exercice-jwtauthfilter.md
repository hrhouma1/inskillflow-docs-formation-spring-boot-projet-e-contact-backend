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

## Diagramme des 13 étapes

```mermaid
graph TB
    subgraph "STRUCTURE DE LA CLASSE"
        E1["ÉTAPE 1<br/>Hériter de OncePerRequestFilter"]
        E2["ÉTAPE 2<br/>Injecter JwtService et UserDetailsService"]
    end
    
    subgraph "EXTRACTION DU JWT"
        E3["ÉTAPE 3<br/>Récupérer header Authorization"]
        E4["ÉTAPE 4<br/>Vérifier 'Bearer '"]
        E5["ÉTAPE 5<br/>Extraire le JWT (substring)"]
    end
    
    subgraph "VALIDATION"
        E6["ÉTAPE 6<br/>Extraire l'email du JWT"]
        E7["ÉTAPE 7<br/>Vérifier pas déjà authentifié"]
        E8["ÉTAPE 8<br/>Charger l'utilisateur (DB)"]
        E9["ÉTAPE 9<br/>Valider le token"]
    end
    
    subgraph "AUTHENTIFICATION"
        E10["ÉTAPE 10<br/>Créer Authentication"]
        E11["ÉTAPE 11<br/>Ajouter détails requête"]
        E12["ÉTAPE 12<br/>Stocker dans SecurityContext"]
    end
    
    subgraph "FINALISATION"
        E13["ÉTAPE 13<br/>Continuer la chaîne (doFilter)"]
    end
    
    E1 --> E2 --> E3 --> E4 --> E5 --> E6 --> E7 --> E8 --> E9 --> E10 --> E11 --> E12 --> E13
    
    style E1 fill:#9C27B0,color:#fff
    style E2 fill:#9C27B0,color:#fff
    style E3 fill:#2196F3,color:#fff
    style E4 fill:#2196F3,color:#fff
    style E5 fill:#2196F3,color:#fff
    style E6 fill:#FF9800,color:#fff
    style E7 fill:#FF9800,color:#fff
    style E8 fill:#FF9800,color:#fff
    style E9 fill:#FF9800,color:#fff
    style E10 fill:#4CAF50,color:#fff
    style E11 fill:#4CAF50,color:#fff
    style E12 fill:#4CAF50,color:#fff
    style E13 fill:#f44336,color:#fff
```

---

## Tableau récapitulatif des 13 étapes

| Étape | Action | Méthode/Concept | Ce qu'on complète |
|-------|--------|-----------------|-------------------|
| 1 | Hériter de la bonne classe | `extends` | `OncePerRequestFilter` |
| 2 | Injecter les dépendances | `private final` | `jwtService`, `userDetailsService` |
| 3 | Récupérer le header | `request.getHeader()` | `"Authorization"` |
| 4 | Vérifier le format | `startsWith()` | `"Bearer "` |
| 5 | Extraire le JWT | `substring()` | `7` |
| 6 | Extraire l'email | `jwtService.extractUsername()` | `extractUsername` |
| 7 | Vérifier pas déjà auth | `getAuthentication()` | `getAuthentication` |
| 8 | Charger l'utilisateur | `loadUserByUsername()` | `loadUserByUsername` |
| 9 | Valider le token | `isTokenValid()` | `isTokenValid` |
| 10 | Créer l'Authentication | `new ...Token()` | `userDetails`, `getAuthorities` |
| 11 | Ajouter les détails | `setDetails()` | `request` |
| 12 | Stocker dans le contexte | `setAuthentication()` | `setAuthentication` |
| 13 | Continuer la chaîne | `doFilter()` | `doFilter` |

---

## Explication des imports

Avant de coder, comprenons **pourquoi** on a besoin de chaque import :

```java
package com.example.contact.security;

// Imports pour le filtre HTTP
import jakarta.servlet.FilterChain;           // La chaîne de filtres
import jakarta.servlet.ServletException;      // Exception possible
import jakarta.servlet.http.HttpServletRequest;   // La requête HTTP
import jakarta.servlet.http.HttpServletResponse;  // La réponse HTTP

// Import Lombok
import lombok.RequiredArgsConstructor;        // Génère le constructeur

// Import Spring
import org.springframework.lang.NonNull;      // Annotation de nullabilité
import org.springframework.stereotype.Component;  // Marque comme bean Spring
import org.springframework.web.filter.OncePerRequestFilter;  // Classe parent

// Imports Spring Security
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;  // L'objet Authentication
import org.springframework.security.core.context.SecurityContextHolder;  // Stockage de l'auth
import org.springframework.security.core.userdetails.UserDetails;        // Interface utilisateur
import org.springframework.security.core.userdetails.UserDetailsService; // Service pour charger user
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;  // Détails requête

// Import Java standard
import java.io.IOException;  // Exception I/O
```

### Tableau des imports par catégorie

| Catégorie | Import | Rôle |
|-----------|--------|------|
| **Servlet** | `FilterChain` | Représente la chaîne de filtres à parcourir |
| **Servlet** | `ServletException` | Exception levée par les servlets |
| **Servlet** | `HttpServletRequest` | Objet représentant la requête HTTP entrante |
| **Servlet** | `HttpServletResponse` | Objet représentant la réponse HTTP |
| **Lombok** | `@RequiredArgsConstructor` | Génère automatiquement le constructeur avec les champs `final` |
| **Spring Core** | `@NonNull` | Indique qu'un paramètre ne peut pas être null |
| **Spring Core** | `@Component` | Marque la classe comme un bean géré par Spring |
| **Spring Web** | `OncePerRequestFilter` | Classe abstraite garantissant 1 seule exécution par requête |
| **Security** | `UsernamePasswordAuthenticationToken` | Objet représentant un utilisateur authentifié |
| **Security** | `SecurityContextHolder` | Conteneur thread-local pour stocker l'Authentication |
| **Security** | `UserDetails` | Interface représentant un utilisateur pour Spring Security |
| **Security** | `UserDetailsService` | Service pour charger un utilisateur depuis la DB |
| **Security** | `WebAuthenticationDetailsSource` | Factory pour créer les détails de la requête |
| **Java** | `IOException` | Exception d'entrée/sortie |

### Diagramme des dépendances

```mermaid
graph TB
    subgraph "Java Servlet API"
        A1["FilterChain"]
        A2["HttpServletRequest"]
        A3["HttpServletResponse"]
        A4["ServletException"]
    end
    
    subgraph "Spring Framework"
        B1["@Component"]
        B2["OncePerRequestFilter"]
        B3["@NonNull"]
    end
    
    subgraph "Spring Security"
        C1["UsernamePasswordAuthenticationToken"]
        C2["SecurityContextHolder"]
        C3["UserDetails"]
        C4["UserDetailsService"]
        C5["WebAuthenticationDetailsSource"]
    end
    
    subgraph "Lombok"
        D1["@RequiredArgsConstructor"]
    end
    
    subgraph "Notre classe"
        E["JwtAuthFilter"]
    end
    
    A1 --> E
    A2 --> E
    A3 --> E
    B1 --> E
    B2 --> E
    C1 --> E
    C2 --> E
    C3 --> E
    C4 --> E
    C5 --> E
    D1 --> E
    
    style E fill:#E91E63,color:#fff
```

---

## Tableau des méthodes utilisées

| Méthode | Classe/Interface | Ce qu'elle fait | Retourne |
|---------|------------------|-----------------|----------|
| `getHeader(name)` | HttpServletRequest | Récupère un header HTTP | String ou null |
| `startsWith(prefix)` | String | Vérifie si la chaîne commence par... | boolean |
| `substring(index)` | String | Extrait une sous-chaîne à partir de... | String |
| `extractUsername(token)` | JwtService | Extrait l'email du JWT | String |
| `getContext()` | SecurityContextHolder | Récupère le SecurityContext | SecurityContext |
| `getAuthentication()` | SecurityContext | Récupère l'Authentication actuelle | Authentication ou null |
| `loadUserByUsername(email)` | UserDetailsService | Charge l'utilisateur depuis la DB | UserDetails |
| `isTokenValid(token, user)` | JwtService | Vérifie si le token est valide | boolean |
| `getAuthorities()` | UserDetails | Retourne les rôles de l'utilisateur | Collection |
| `setDetails(details)` | Authentication | Attache les détails de la requête | void |
| `buildDetails(request)` | WebAuthenticationDetailsSource | Crée les détails depuis la requête | WebAuthenticationDetails |
| `setAuthentication(auth)` | SecurityContext | Stocke l'Authentication | void |
| `doFilter(req, resp)` | FilterChain | Passe au prochain filtre | void |

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

### Réponse : b) `OncePerRequestFilter`

```java
public class JwtAuthFilter extends OncePerRequestFilter {
```

### Explication détaillée

**Pourquoi pas les autres options?**

| Option | Pourquoi pas? |
|--------|---------------|
| a) `Filter` | Interface Java de base - peut s'exécuter plusieurs fois |
| c) `HttpFilter` | Existe mais moins utilisé dans Spring |
| d) `SecurityFilter` | N'existe pas! |

**Que se passe-t-il quand on hérite de OncePerRequestFilter?**

1. Spring appelle automatiquement `doFilterInternal()` à chaque requête
2. Spring garantit que c'est appelé **une seule fois** (même avec redirections)
3. Vous devez implémenter `doFilterInternal()` (c'est une méthode abstraite)

```java
public abstract class OncePerRequestFilter {
    
    // Cette méthode est appelée par Spring
    public final void doFilter(request, response, chain) {
        // Vérifie si déjà exécuté pour cette requête
        if (!alreadyFiltered) {
            doFilterInternal(request, response, chain);  // ← Votre code!
        }
    }
    
    // VOUS devez implémenter cette méthode
    protected abstract void doFilterInternal(request, response, chain);
}
```

### Visualisation

```mermaid
graph TB
    A["Requête arrive"]
    A --> B["Spring appelle doFilter()"]
    B --> C{"Déjà filtré?"}
    C -->|Non| D["Appelle doFilterInternal()<br/>(votre code)"]
    C -->|Oui| E["Skip - déjà fait"]
    D --> F["Marque comme filtré"]
    
    style D fill:#4CAF50,color:#fff
```

### À retenir

- `OncePerRequestFilter` = classe de Spring (pas Java standard)
- Package : `org.springframework.web.filter`
- Méthode à implémenter : `doFilterInternal()`
- Garantie : **1 seule exécution par requête**

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

### Réponse

```java
private final JwtService jwtService;
private final UserDetailsService userDetailsService;
```

### Explication détaillée

**Convention de nommage en Java :**

| Type | Convention | Exemple |
|------|------------|---------|
| Classe | PascalCase | `JwtService` |
| Variable | camelCase | `jwtService` |
| Constante | UPPER_SNAKE | `MAX_SIZE` |

**Pourquoi `final`?**

```java
// Avec final : la référence ne peut PAS changer
private final JwtService jwtService;  // ✅ Recommandé

jwtService = new JwtService();  // ❌ ERREUR de compilation!

// Sans final : la référence PEUT changer
private JwtService jwtService;  // ⚠️ Moins sûr

jwtService = new JwtService();  // ✅ Possible mais dangereux
```

`final` garantit que la dépendance ne sera jamais remplacée accidentellement.

### Comment l'injection fonctionne?

```mermaid
graph TB
    A["@RequiredArgsConstructor"]
    A --> B["Lombok génère le constructeur"]
    B --> C["public JwtAuthFilter(<br/>JwtService jwtService,<br/>UserDetailsService userDetailsService)"]
    C --> D["Spring voit le constructeur"]
    D --> E["Spring injecte automatiquement<br/>les beans correspondants"]
    
    style E fill:#4CAF50,color:#fff
```

**Code généré par Lombok :**

```java
// Ce que vous écrivez :
@RequiredArgsConstructor
public class JwtAuthFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
}

// Ce que Lombok génère :
public class JwtAuthFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    
    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }
}
```

### À retenir

- Noms de variables en **camelCase** (première lettre minuscule)
- `final` = immutable (bonne pratique pour les injections)
- `@RequiredArgsConstructor` génère le constructeur automatiquement

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

### Réponse : b) `getHeader("Authorization")`

```java
final String authHeader = request.getHeader("Authorization");
```

### Pourquoi pas les autres options?

| Option | Pourquoi pas? |
|--------|---------------|
| a) `getParameter("Authorization")` | `getParameter` est pour les query strings (?key=value), pas les headers |
| c) `getAttribute("Auth")` | `getAttribute` est pour les attributs de requête, pas les headers |
| d) `getHeader("Bearer")` | "Bearer" n'est pas le nom du header, c'est le schéma d'auth |

### Différence entre getHeader et getParameter

```
URL: http://localhost:8080/api/leads?status=NEW
                                     ↑
                                getParameter("status") → "NEW"

Headers:
  Authorization: Bearer eyJ...      ← getHeader("Authorization")
  Content-Type: application/json    ← getHeader("Content-Type")
```

```mermaid
graph TB
    subgraph "getParameter()"
        A["URL ?key=value"]
        A --> B["?status=NEW"]
        B --> C["getParameter('status') → 'NEW'"]
    end
    
    subgraph "getHeader()"
        D["Headers HTTP"]
        D --> E["Authorization: Bearer eyJ..."]
        E --> F["getHeader('Authorization') → 'Bearer eyJ...'"]
    end
    
    style F fill:#4CAF50,color:#fff
```

### Valeur retournée

```java
// Si le header existe :
request.getHeader("Authorization")  // → "Bearer eyJhbGciOiJIUzI1NiJ9..."

// Si le header n'existe pas :
request.getHeader("Authorization")  // → null
```

### Attention à la casse!

```java
request.getHeader("Authorization")   // ✅ Correct
request.getHeader("authorization")   // ✅ Fonctionne aussi (HTTP est case-insensitive)
request.getHeader("AUTHORIZATION")   // ✅ Fonctionne aussi
request.getHeader("Auth")            // ❌ Mauvais nom de header!
```

### À retenir

- `getHeader()` = récupérer un header HTTP
- `getParameter()` = récupérer un paramètre d'URL (?key=value)
- Le header s'appelle `Authorization` (standard HTTP)
- Retourne `null` si le header est absent

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

### Réponse

```java
if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    filterChain.doFilter(request, response);
    return;
}
```

### Explication détaillée

**Décomposition de la condition :**

```java
authHeader == null              // Vrai si pas de header Authorization
    ||                          // OU logique
!authHeader.startsWith("Bearer ")  // Vrai si ne commence PAS par "Bearer "
```

**Table de vérité :**

| authHeader | startsWith("Bearer ") | Résultat | Action |
|------------|----------------------|----------|--------|
| `null` | - | ENTRE dans le if | Continue sans auth |
| `"Basic xyz"` | false | ENTRE dans le if | Continue sans auth |
| `"bearer token"` | false (casse!) | ENTRE dans le if | Continue sans auth |
| `"Bearer eyJ..."` | true | N'ENTRE PAS | Continue le traitement |

### Attention à l'espace!

```java
"Bearer eyJ...".startsWith("Bearer ")  // ✅ true (avec espace)
"Bearer eyJ...".startsWith("Bearer")   // ✅ true aussi mais moins précis
"BearereyJ...".startsWith("Bearer ")   // ❌ false (pas d'espace)
```

On met `"Bearer "` (avec espace) pour être sûr que le format est correct.

### L'opérateur || (OU logique)

```mermaid
graph TB
    A["condition1 || condition2"]
    A --> B{"condition1<br/>vraie?"}
    B -->|Oui| C["Résultat: TRUE<br/>(condition2 pas évaluée)"]
    B -->|Non| D{"condition2<br/>vraie?"}
    D -->|Oui| C
    D -->|Non| E["Résultat: FALSE"]
    
    style C fill:#4CAF50,color:#fff
```

**Short-circuit evaluation** : Si `authHeader == null` est vrai, Java n'évalue PAS `authHeader.startsWith()`. C'est important car sinon on aurait une `NullPointerException`!

### Le pattern early return

```java
// ✅ Avec early return (lisible)
if (mauvaise_condition) {
    return;  // Sort immédiatement
}
// Code principal ici (pas d'indentation supplémentaire)

// ❌ Sans early return (imbriqué)
if (!mauvaise_condition) {
    // Code principal ici (indentation supplémentaire)
}
```

### À retenir

- `||` = OU logique (une des deux conditions suffit)
- `!` = négation (inverse le booléen)
- `"Bearer "` avec l'espace est important
- `return` sort de la méthode immédiatement
- Toujours appeler `doFilter()` même si on sort tôt!

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

### Réponse

```java
final String jwt = authHeader.substring(7);
```

### Explication détaillée

**Comment fonctionne substring()?**

```java
String texte = "Hello World";
//              01234567890
//              ↑    ↑
//              0    5

texte.substring(0)     // → "Hello World" (tout)
texte.substring(6)     // → "World" (à partir de l'index 6)
texte.substring(0, 5)  // → "Hello" (de 0 à 4 inclus)
```

**Application à notre cas :**

```
"Bearer eyJhbGciOiJIUzI1NiJ9..."
 0123456789...
       ↑
       Index 7 = début du JWT
```

```java
"Bearer eyJhbGci...".substring(7)  // → "eyJhbGci..."
```

### Visualisation caractère par caractère

```
Index:  0   1   2   3   4   5   6   7   8   9   10  11  ...
Char:   B   e   a   r   e   r   ' ' e   y   J   a   l   ...
        └───────────────────────┘   └──────────────────────┘
              7 caractères              Le JWT (ce qu'on veut)
              (à ignorer)
```

### Pourquoi 7 et pas 6?

```java
"Bearer ".length()  // → 7

// B = 1
// e = 2
// a = 3
// r = 4
// e = 5
// r = 6
// (espace) = 7
```

L'espace après "Bearer" compte!

### Test dans votre IDE

```java
public static void main(String[] args) {
    String authHeader = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWI";
    String jwt = authHeader.substring(7);
    System.out.println(jwt);  
    // Affiche: eyJhbGciOiJIUzI1NiJ9.eyJzdWI
}
```

### À retenir

- `substring(n)` = tout à partir de l'index n
- Les index commencent à 0 en Java
- `"Bearer "` = 7 caractères (n'oubliez pas l'espace!)
- Résultat : le JWT pur, sans préfixe

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

### Réponse

```java
final String userEmail = jwtService.extractUsername(jwt);
```

### Explication détaillée

**Que fait extractUsername() en interne?**

```java
// Dans JwtService.java
public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
}

// Claims::getSubject récupère le champ "sub" du JWT
```

### Processus de décodage

```mermaid
graph TB
    A["JWT encodé<br/>eyJhbGciOiJIUzI1..."]
    A --> B["1. Séparer en 3 parties<br/>(header.payload.signature)"]
    B --> C["2. Décoder le payload (Base64)"]
    C --> D["3. Parser le JSON"]
    D --> E["4. Extraire 'sub'"]
    E --> F["'admin@test.com'"]
    
    style F fill:#4CAF50,color:#fff
```

### Exemple concret

```java
// JWT encodé
String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkB0ZXN0LmNvbSIsImlhdCI6MTcwNTMxMjAwMH0.xxx";

// Payload décodé (Base64 → JSON)
{
    "sub": "admin@test.com",  // ← extractUsername() retourne ceci
    "iat": 1705312000,
    "exp": 1705398400
}

// Résultat
jwtService.extractUsername(jwt)  // → "admin@test.com"
```

### Pourquoi "sub" et pas "email"?

`sub` (subject) est le nom **standard** dans les JWT pour identifier l'utilisateur. C'est une convention définie dans la spécification JWT (RFC 7519).

| Claim | Signification | Exemple |
|-------|---------------|---------|
| `sub` | Subject (identifiant) | "admin@test.com" |
| `iat` | Issued At (création) | 1705312000 |
| `exp` | Expiration | 1705398400 |
| `iss` | Issuer (émetteur) | "myapp.com" |

### Code complet de JwtService

```java
public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
}

public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
}

private Claims extractAllClaims(String token) {
    return Jwts.parser()
            .verifyWith(getSigningKey())  // Vérifie la signature
            .build()
            .parseSignedClaims(token)
            .getPayload();
}
```

### À retenir

- `extractUsername()` décode le JWT et extrait le champ `sub`
- `sub` (subject) est le standard pour l'identifiant utilisateur
- Le JWT est encodé en Base64, pas chiffré (n'y mettez jamais de secrets!)
- La signature est vérifiée pendant le décodage

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

### Réponse

```java
if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
```

### Explication détaillée

**Décomposition :**

```java
userEmail != null                                    // Condition 1
    &&                                               // ET logique
SecurityContextHolder.getContext().getAuthentication() == null  // Condition 2
```

**Les deux conditions doivent être vraies :**

| Condition 1 | Condition 2 | Résultat | Signification |
|-------------|-------------|----------|---------------|
| userEmail = null | - | FALSE | JWT corrompu, on ne fait rien |
| userEmail = "admin@..." | auth != null | FALSE | Déjà authentifié, on ne refait pas |
| userEmail = "admin@..." | auth == null | TRUE | On peut authentifier! |

### Pourquoi vérifier getAuthentication() == null?

```mermaid
graph TB
    A["Requête arrive"]
    A --> B["JwtAuthFilter s'exécute"]
    B --> C{"Déjà authentifié?"}
    C -->|Oui| D["Ne rien faire<br/>(éviter le travail inutile)"]
    C -->|Non| E["Procéder à l'authentification"]
    
    style D fill:#FF9800,color:#fff
    style E fill:#4CAF50,color:#fff
```

Scénarios où l'utilisateur pourrait déjà être authentifié :
- Un autre filtre l'a déjà fait
- La requête a été forwardée après une première authentification

### L'opérateur && (ET logique)

```java
true && true   // → true   (les deux vraies)
true && false  // → false  (une des deux fausse)
false && true  // → false  (short-circuit: true pas évalué!)
false && false // → false
```

**Short-circuit** : Si la première condition est `false`, Java n'évalue PAS la deuxième. C'est important car si `userEmail` est `null`, on ne veut pas appeler `getAuthentication()` pour rien.

### Visualisation du SecurityContextHolder

```
┌─────────────────────────────────────────┐
│         SecurityContextHolder           │
│  (conteneur global thread-local)        │
│                                         │
│    ┌─────────────────────────────┐      │
│    │     SecurityContext         │      │
│    │                             │      │
│    │  ┌─────────────────────┐   │      │
│    │  │   Authentication    │   │      │
│    │  │   - principal       │   │      │
│    │  │   - authorities     │   │      │
│    │  │   - authenticated   │   │      │
│    │  └─────────────────────┘   │      │
│    │       (ou null)            │      │
│    └─────────────────────────────┘      │
└─────────────────────────────────────────┘
```

### À retenir

- `&&` = ET logique (les deux conditions doivent être vraies)
- `getAuthentication()` retourne `null` si personne n'est connecté
- On vérifie `userEmail != null` AVANT d'accéder aux autres données
- Short-circuit évite les erreurs si la première condition est fausse

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

### Réponse

```java
UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
```

### Explication détaillée

**Flux complet de chargement :**

```mermaid
graph TB
    A["loadUserByUsername('admin@test.com')"]
    A --> B["UserDetailsConfig.java"]
    B --> C["userRepository.findByEmail(email)"]
    C --> D["JPA génère la requête SQL"]
    D --> E["SELECT * FROM users<br/>WHERE email = 'admin@test.com'"]
    E --> F["PostgreSQL"]
    F --> G["User(id=1, email, password, role=ADMIN)"]
    
    style A fill:#2196F3,color:#fff
    style G fill:#4CAF50,color:#fff
```

### Pourquoi this.?

```java
this.userDetailsService.loadUserByUsername(userEmail);
//  ↑
//  Fait référence à l'attribut de la classe
```

`this.` est optionnel ici, mais rend le code plus clair. Il indique qu'on utilise l'attribut injecté, pas une variable locale.

### Que se passe-t-il si l'utilisateur n'existe pas?

```java
// Dans UserDetailsConfig.java
@Bean
public UserDetailsService userDetailsService() {
    return username -> userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
            //           ↑
            //           Lance une exception si pas trouvé!
}
```

L'exception sera attrapée par notre `try-catch` dans JwtAuthFilter :

```java
try {
    userEmail = jwtService.extractUsername(jwt);
    // ...
    UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
    // ...
} catch (Exception e) {
    // UsernameNotFoundException sera attrapée ici
    // On continue sans authentification
}
```

### Pourquoi charger l'utilisateur?

Le JWT contient seulement l'email. On a besoin de plus d'informations :

| Source | Contient |
|--------|----------|
| JWT | email, dates |
| Base de données | email, password, **rôle**, nom, etc. |

On charge l'utilisateur pour récupérer son **rôle** (ADMIN, USER, etc.) qui sera utilisé par `hasRole()`.

### À retenir

- `loadUserByUsername()` est la méthode standard de `UserDetailsService`
- Elle charge l'utilisateur complet depuis la base de données
- Lance `UsernameNotFoundException` si l'utilisateur n'existe pas
- On a besoin de l'utilisateur complet pour obtenir ses rôles

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

### Réponse

```java
if (jwtService.isTokenValid(jwt, userDetails)) {
```

### Explication détaillée

**Que vérifie isTokenValid()?**

```java
// Dans JwtService.java
public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    //      ↑                                                ↑
    //      Vérification 1: mêmes emails                    Vérification 2: pas expiré
}
```

### Les 3 vérifications de sécurité

```mermaid
graph TB
    A["isTokenValid(jwt, userDetails)"]
    A --> B["1. Signature valide?<br/>(fait pendant extractUsername)"]
    A --> C["2. Email JWT == Email DB?"]
    A --> D["3. Token pas expiré?"]
    
    B --> E{"Tous OK?"}
    C --> E
    D --> E
    
    E -->|Oui| F["return true ✅"]
    E -->|Non| G["return false ❌"]
    
    style F fill:#4CAF50,color:#fff
    style G fill:#f44336,color:#fff
```

### Vérification 1 : Signature

La signature est vérifiée **automatiquement** quand on décode le JWT :

```java
// Si la signature est invalide, une exception est lancée
Jwts.parser()
    .verifyWith(getSigningKey())  // ← Vérifie la signature
    .build()
    .parseSignedClaims(token);    // ← Lance une exception si invalide
```

### Vérification 2 : Mêmes emails

```java
username.equals(userDetails.getUsername())
```

Pourquoi cette vérification? Scénario d'attaque :
1. Hacker modifie l'email dans le JWT
2. Mais il ne peut pas refaire la signature (il n'a pas la clé secrète)
3. → Signature invalide, attaque bloquée!

### Vérification 3 : Pas expiré

```java
private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
    //     ↑                              ↑
    //     Date d'expiration du JWT       Maintenant
}
```

```mermaid
graph LR
    A["exp = 2024-01-20 10:00"]
    B["now = 2024-01-19 15:00"]
    A --> C{"exp.before(now)?"}
    B --> C
    C -->|Non| D["Token VALIDE"]
    
    E["exp = 2024-01-18 10:00"]
    F["now = 2024-01-19 15:00"]
    E --> G{"exp.before(now)?"}
    F --> G
    G -->|Oui| H["Token EXPIRÉ"]
    
    style D fill:#4CAF50,color:#fff
    style H fill:#f44336,color:#fff
```

### À retenir

- `isTokenValid()` fait 3 vérifications
- La signature est vérifiée automatiquement au décodage
- On compare l'email du JWT avec l'email en base de données
- On vérifie que le token n'est pas expiré
- Retourne `true` seulement si TOUT est OK

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

### Réponse

```java
UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
        userDetails,              // Le principal (l'utilisateur complet)
        null,                     // Credentials (null car JWT déjà validé)
        userDetails.getAuthorities()  // Les rôles [ROLE_ADMIN]
);
```

### Explication détaillée

**Les 3 paramètres du constructeur :**

| # | Paramètre | Notre valeur | Description |
|---|-----------|--------------|-------------|
| 1 | `principal` | `userDetails` | L'objet User complet |
| 2 | `credentials` | `null` | Le mot de passe (inutile ici) |
| 3 | `authorities` | `userDetails.getAuthorities()` | Les rôles/permissions |

### Paramètre 1 : Le Principal

```java
userDetails  // C'est notre objet User chargé depuis la DB
```

Le **principal** représente "qui est l'utilisateur". Plus tard, vous pouvez le récupérer :

```java
// Dans un Controller
@GetMapping("/me")
public User getCurrentUser(Authentication auth) {
    User user = (User) auth.getPrincipal();  // Récupère le principal
    return user;
}
```

### Paramètre 2 : Les Credentials (null)

```java
null  // On met null car le JWT a déjà prouvé l'identité
```

Pourquoi null?

```mermaid
graph LR
    subgraph "Login classique"
        A["username + password"]
        A --> B["Vérifier password"]
        B --> C["credentials = password"]
    end
    
    subgraph "Notre cas (JWT)"
        D["JWT valide"]
        D --> E["Identité déjà prouvée"]
        E --> F["credentials = null"]
    end
    
    style F fill:#4CAF50,color:#fff
```

### Paramètre 3 : Les Authorities

```java
userDetails.getAuthorities()  // Retourne les rôles de l'utilisateur
```

Dans notre classe `User.java` :

```java
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    // Si role = ADMIN → retourne [ROLE_ADMIN]
    // Si role = USER  → retourne [ROLE_USER]
}
```

Ces rôles sont utilisés par SecurityConfig :

```java
.requestMatchers("/api/admin/**").hasRole("ADMIN")
//                                        ↑
//                               Vérifie si ROLE_ADMIN est dans authorities
```

### Visualisation de l'objet créé

```
UsernamePasswordAuthenticationToken
├── principal: User {
│       id: 1,
│       email: "admin@test.com",
│       role: ADMIN
│   }
├── credentials: null
├── authorities: [ROLE_ADMIN]
└── authenticated: true
```

### À retenir

- `userDetails` = l'objet User complet (le "qui")
- `null` = pas de credentials car JWT déjà validé
- `getAuthorities()` = les rôles pour `hasRole()` et `@PreAuthorize`
- Ce constructeur avec 3 arguments marque automatiquement `authenticated = true`

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

### Réponse

```java
authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
```

### Explication détaillée

**Décomposition :**

```java
new WebAuthenticationDetailsSource()  // 1. Crée une factory de détails
    .buildDetails(request)            // 2. Extrait les détails de la requête
// Résultat : WebAuthenticationDetails(remoteAddress=192.168.1.1, sessionId=abc123)

authToken.setDetails(...)             // 3. Attache ces détails à l'Authentication
```

### Que contient WebAuthenticationDetails?

```mermaid
graph TB
    A["WebAuthenticationDetails"]
    A --> B["remoteAddress<br/>= IP du client<br/>(ex: 192.168.1.1)"]
    A --> C["sessionId<br/>= ID de session<br/>(ex: abc123...)"]
    
    style B fill:#2196F3,color:#fff
    style C fill:#FF9800,color:#fff
```

### À quoi ça sert?

| Usage | Exemple |
|-------|---------|
| **Logging** | "User admin@test.com logged in from 192.168.1.1" |
| **Audit** | Tracer qui a fait quoi et d'où |
| **Sécurité** | Détecter des connexions depuis des IPs suspectes |

### Comment récupérer ces infos plus tard?

```java
// Dans un Controller ou Service
@GetMapping("/audit")
public void logAction(Authentication auth) {
    WebAuthenticationDetails details = 
        (WebAuthenticationDetails) auth.getDetails();
    
    String ip = details.getRemoteAddress();    // → "192.168.1.1"
    String session = details.getSessionId();   // → "abc123..."
    
    logger.info("Action from IP: " + ip);
}
```

### Le paramètre request

```java
protected void doFilterInternal(
    HttpServletRequest request,   // ← C'est cet objet qu'on passe!
    HttpServletResponse response,
    FilterChain filterChain
)
```

`request` contient toutes les informations sur la requête HTTP entrante.

### À retenir

- `WebAuthenticationDetailsSource` extrait les métadonnées de la requête
- `request` est l'objet HttpServletRequest reçu en paramètre
- Ces détails sont utiles pour le logging et l'audit
- C'est optionnel mais recommandé pour la traçabilité

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

### Réponse

```java
SecurityContextHolder.getContext().setAuthentication(authToken);
```

### Explication détaillée

**Décomposition :**

```java
SecurityContextHolder     // 1. Accède au conteneur global
    .getContext()         // 2. Récupère le contexte de cette requête
    .setAuthentication(authToken)  // 3. Stocke notre Authentication
```

### Visualisation avant/après

```
AVANT setAuthentication():
┌─────────────────────────────────┐
│     SecurityContext             │
│     authentication = null       │  ← Pas connecté
└─────────────────────────────────┘

APRÈS setAuthentication():
┌─────────────────────────────────┐
│     SecurityContext             │
│     authentication = {          │
│       principal: User,          │  ← Connecté!
│       authorities: [ROLE_ADMIN] │
│     }                           │
└─────────────────────────────────┘
```

### Que se passe-t-il après cette ligne?

```mermaid
graph TB
    A["setAuthentication(authToken)"]
    A --> B["SecurityContext contient<br/>l'utilisateur authentifié"]
    B --> C["filterChain.doFilter()"]
    C --> D["Prochain filtre"]
    D --> E["SecurityConfig vérifie<br/>hasRole('ADMIN')"]
    E --> F{"authorities contient<br/>ROLE_ADMIN?"}
    F -->|Oui| G["Accès autorisé ✅"]
    F -->|Non| H["403 Forbidden ❌"]
    
    style B fill:#4CAF50,color:#fff
    style G fill:#4CAF50,color:#fff
```

### Thread-local expliqué

```
Requête 1 (Thread 1):
┌─────────────────────────────────┐
│ SecurityContext (Thread 1)      │
│ auth = User(admin@test.com)     │
└─────────────────────────────────┘

Requête 2 (Thread 2):
┌─────────────────────────────────┐
│ SecurityContext (Thread 2)      │
│ auth = User(user@test.com)      │  ← Différent!
└─────────────────────────────────┘

Requête 3 (Thread 3):
┌─────────────────────────────────┐
│ SecurityContext (Thread 3)      │
│ auth = null (pas de JWT)        │  ← Pas connecté
└─────────────────────────────────┘
```

Chaque thread (= chaque requête) a son propre SecurityContext. Ils ne se mélangent pas.

### Comment récupérer l'utilisateur connecté plus tard?

```java
// Méthode 1 : Via SecurityContextHolder
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
User user = (User) auth.getPrincipal();

// Méthode 2 : Injection dans le Controller (plus propre)
@GetMapping("/me")
public User getCurrentUser(Authentication auth) {
    return (User) auth.getPrincipal();
}

// Méthode 3 : Annotation @AuthenticationPrincipal
@GetMapping("/me")
public User getCurrentUser(@AuthenticationPrincipal User user) {
    return user;
}
```

### À retenir

- `setAuthentication()` stocke l'Authentication dans le contexte
- Après cette ligne, l'utilisateur est considéré comme **connecté**
- Le contexte est **thread-local** (isolé par requête)
- Les filtres et controllers suivants peuvent accéder à l'Authentication

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

### Réponse

```java
filterChain.doFilter(request, response);
```

### Explication détaillée

**Que fait doFilter()?**

```java
filterChain.doFilter(request, response);
//          ↑
//          "Passe au prochain filtre dans la chaîne"
```

C'est comme dire : "J'ai fini mon travail, au suivant!"

### Visualisation de la chaîne

```mermaid
graph TB
    A["Requête HTTP"]
    A --> B["Filtre 1"]
    B -->|doFilter| C["Filtre 2"]
    C -->|doFilter| D["JwtAuthFilter<br/>(nous)"]
    D -->|doFilter| E["Filtre 4"]
    E -->|doFilter| F["Controller"]
    F --> G["Réponse"]
    
    style D fill:#E91E63,color:#fff
```

### ⚠️ ERREUR CRITIQUE : Oublier doFilter()

```java
// ❌ ERREUR FATALE
protected void doFilterInternal(...) {
    // ... traitement ...
    // Oups! Pas de doFilter()
}
```

**Conséquences :**
- La requête est **bloquée**
- Le client attend indéfiniment
- Timeout après 30-60 secondes
- Aucune réponse envoyée

### Les deux paramètres

```java
filterChain.doFilter(
    request,   // La requête (peut être modifiée)
    response   // La réponse (peut être modifiée)
);
```

| Paramètre | Type | Description |
|-----------|------|-------------|
| `request` | HttpServletRequest | La requête entrante |
| `response` | HttpServletResponse | La réponse à construire |

### Quand appeler doFilter()?

**TOUJOURS!** Dans tous les chemins possibles :

```java
protected void doFilterInternal(...) {
    if (pasDeJwt) {
        filterChain.doFilter(request, response);  // ✅
        return;
    }
    
    try {
        // ... traitement ...
    } catch (Exception e) {
        // Erreur gérée
    }
    
    filterChain.doFilter(request, response);  // ✅ Toujours à la fin!
}
```

### Schéma de décision

```mermaid
graph TB
    A["Début du filtre"]
    A --> B{"JWT présent?"}
    B -->|Non| C["doFilter() ✅"]
    B -->|Oui| D["Traitement JWT"]
    D --> E{"Erreur?"}
    E -->|Oui| F["catch + doFilter() ✅"]
    E -->|Non| G["Auth créée + doFilter() ✅"]
    
    style C fill:#4CAF50,color:#fff
    style F fill:#4CAF50,color:#fff
    style G fill:#4CAF50,color:#fff
```

### Test pour vérifier

Si vous oubliez `doFilter()`, vous verrez :
1. Le navigateur qui tourne indéfiniment
2. Pas de réponse (même pas d'erreur)
3. Timeout après ~30 secondes

### À retenir

- `doFilter()` = "passe au suivant"
- **OBLIGATOIRE** dans TOUS les chemins
- Sans ça, la requête est **bloquée**
- C'est la dernière ligne de votre filtre (généralement)
- Toujours appeler même si on sort tôt (early return)

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

