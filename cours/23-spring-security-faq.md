# Chapitre 5.2 - Spring Security : Questions fréquentes et vulgarisation

## Objectifs du chapitre

- Répondre aux questions courantes des étudiants
- Vulgariser les concepts complexes avec des analogies
- Clarifier les confusions fréquentes (CORS vs CSRF, etc.)

---

## 1. Vulgarisation : Les deux gardiens de sécurité

### L'analogie des deux bâtiments

Imaginons que vous avez **deux bâtiments** :
- **Bâtiment A** : Votre frontend (React, Next.js, Vue...)
- **Bâtiment B** : Votre backend (Spring Boot API)

```mermaid
graph TB
    subgraph "Bâtiment A - Frontend"
        VISITORS["Visiteurs<br/>(utilisateurs)"]
        GUARD_A["Gardien NextAuth<br/>Gère l'interface<br/>et stocke le badge"]
    end
    
    subgraph "Bâtiment B - Backend"
        GUARD_B["Gardien Spring Security<br/>CRÉE et VÉRIFIE<br/>les badges (JWT)"]
        COFFRE["Coffre-fort<br/>(Base de données)"]
    end
    
    VISITORS -->|"1. Demande de badge<br/>(login)"| GUARD_A
    GUARD_A -->|"2. Transmet la demande"| GUARD_B
    GUARD_B -->|"3. Crée le badge JWT"| GUARD_A
    GUARD_A -->|"4. Stocke le badge"| VISITORS
    VISITORS -->|"5. Présente le badge<br/>à chaque visite"| GUARD_B
    GUARD_B --> COFFRE
    
    style GUARD_A fill:#2196F3,color:#fff
    style GUARD_B fill:#4CAF50,color:#fff
```

### Flux correct détaillé

```mermaid
sequenceDiagram
    participant U as Utilisateur
    participant F as Frontend (NextAuth)
    participant B as Backend (Spring Security)
    participant DB as Base de données
    
    Note over U,B: 1. CONNEXION (Login)
    U->>F: Email + Password
    F->>B: POST /api/auth/login
    B->>DB: Vérifier credentials
    DB-->>B: Utilisateur trouvé
    B->>B: GÉNÈRE le JWT
    B-->>F: { token: "eyJ..." }
    F->>F: Stocke le JWT (localStorage/cookie)
    F-->>U: Connecté!
    
    Note over U,B: 2. REQUÊTES SUIVANTES
    U->>F: Clic sur "Mes leads"
    F->>B: GET /api/leads<br/>Authorization: Bearer eyJ...
    B->>B: VÉRIFIE le JWT
    B->>DB: Récupère les données
    DB-->>B: Leads
    B-->>F: [ leads... ]
    F-->>U: Affiche les leads
```

> **Point clé** : C'est le **BACKEND (Spring Security)** qui :
> - Crée le JWT lors du login
> - Vérifie le JWT à chaque requête
> 
> Le **FRONTEND (NextAuth)** ne fait que :
> - Transmettre les credentials au backend
> - Stocker le JWT reçu
> - Envoyer le JWT avec chaque requête

### NextAuth + Spring Security : Faut-il les deux?

**OUI, il faut les deux!** Mais ils ont des rôles différents :

| Gardien | Rôle | Ce qu'il fait |
|---------|------|---------------|
| **Spring Security** (Backend) | CRÉE et VÉRIFIE les JWT | Authentifie, autorise, protège l'API |
| **NextAuth** (Frontend) | STOCKE et ENVOIE les JWT | Gère l'état de session côté client |

**Pourquoi les deux?** 
- **Spring Security est OBLIGATOIRE** : C'est lui qui génère les tokens et protège vos données
- **NextAuth est PRATIQUE** : Il facilite la gestion de session côté frontend

Imaginez que quelqu'un contourne le gardien A (frontend) et va directement au bâtiment B (API). Sans le gardien B (Spring Security), il accède directement au coffre-fort!

```mermaid
graph LR
    subgraph "Scénario 1 : Normal"
        U1[Utilisateur] --> F1[Frontend + NextAuth] --> A1[API + Spring Security]
    end
    
    subgraph "Scénario 2 : Attaque directe"
        H[Hacker] -->|"curl, Postman"| A2[API + Spring Security]
        A2 -->|"❌ Bloqué!"| X[Refusé]
    end
    
    style X fill:#f44336,color:#fff
```

> **Règle d'or** : Le backend doit TOUJOURS se protéger lui-même, car on ne peut pas faire confiance au frontend.

---

## 2. CSRF : C'est quoi exactement?

### Explication en 5 lignes

**CSRF** (Cross-Site Request Forgery) est une attaque où un site malveillant fait exécuter des actions à votre insu sur un site où vous êtes connecté. Imaginez que vous êtes connecté à votre banque (onglet 1), puis vous visitez un site pirate (onglet 2). Ce site pirate peut envoyer une requête à votre banque en utilisant vos cookies de session, car le navigateur les envoie automatiquement. Résultat : un virement est effectué sans votre consentement. CSRF exploite la confiance du serveur envers les cookies du navigateur.

### Exemple concret d'attaque CSRF

```mermaid
sequenceDiagram
    participant U as Vous
    participant B as Banque (bank.com)
    participant M as Site Malveillant
    
    U->>B: 1. Login sur bank.com
    B-->>U: Cookie de session (JSESSIONID)
    Note over U: Vous êtes connecté à la banque
    
    U->>M: 2. Visite d'un site pirate
    M-->>U: Page avec formulaire caché
    
    Note over M: Le site contient:<br/><form action="bank.com/transfer"><br/><input name="to" value="hacker"><br/><input name="amount" value="10000">
    
    U->>B: 3. Formulaire soumis automatiquement<br/>+ Cookie envoyé automatiquement!
    B-->>U: ❌ Virement effectué!
```

### Code d'une page malveillante

```html
<!-- Site malveillant : hacker.com -->
<html>
<body>
  <h1>Félicitations! Vous avez gagné!</h1>
  
  <!-- Formulaire caché qui s'envoie automatiquement -->
  <form id="csrf-form" action="https://bank.com/api/transfer" method="POST" style="display:none">
    <input name="to" value="compte-du-hacker">
    <input name="amount" value="10000">
  </form>
  
  <script>
    // Soumission automatique dès que la page charge
    document.getElementById('csrf-form').submit();
  </script>
</body>
</html>
```

### Pourquoi CSRF ne concerne PAS les API REST?

```mermaid
graph TB
    subgraph "Application MVC traditionnelle"
        A1["Navigateur envoie cookie<br/>AUTOMATIQUEMENT"]
        A1 --> B1["Serveur fait confiance<br/>au cookie"]
        B1 --> C1["❌ CSRF possible!"]
    end
    
    subgraph "API REST avec JWT"
        A2["Token JWT dans header<br/>MANUELLEMENT ajouté"]
        A2 --> B2["Serveur vérifie<br/>le header Authorization"]
        B2 --> C2["✅ CSRF impossible!"]
    end
    
    style C1 fill:#f44336,color:#fff
    style C2 fill:#4CAF50,color:#fff
```

**Explication** : CSRF exploite les cookies envoyés automatiquement. Avec JWT :
1. Le token n'est PAS un cookie
2. Vous devez l'ajouter MANUELLEMENT dans le header `Authorization: Bearer xxx`
3. Un site malveillant ne peut PAS ajouter ce header à votre place
4. Donc CSRF est **impossible** → on le désactive

---

## 3. CORS vs CSRF : La confusion classique

### Ce sont deux choses COMPLÈTEMENT différentes!

```mermaid
graph TB
    subgraph "CORS - Contrôle d'accès"
        CORS1["QUI peut appeler mon API?"]
        CORS2["Bloque les requêtes<br/>d'origines non autorisées"]
        CORS3["Protection côté NAVIGATEUR"]
    end
    
    subgraph "CSRF - Type d'attaque"
        CSRF1["Attaque par formulaire caché"]
        CSRF2["Exploite les cookies<br/>de session"]
        CSRF3["Protection côté SERVEUR"]
    end
    
    style CORS1 fill:#2196F3,color:#fff
    style CSRF1 fill:#FF9800,color:#fff
```

| Aspect | CORS | CSRF |
|--------|------|------|
| **C'est quoi?** | Mécanisme de contrôle d'accès | Type d'attaque |
| **Qui l'implémente?** | Navigateur | Attaquant |
| **Protection?** | Headers HTTP du serveur | Token anti-CSRF |
| **Concerne les API REST?** | OUI (pour les frontends web) | NON (pas de cookies) |

### Pourquoi CORS ne protège PAS contre CSRF?

CORS bloque les requêtes **JavaScript** cross-origin. Mais les **formulaires HTML** ne sont PAS soumis à CORS!

```html
<!-- Cette requête N'EST PAS bloquée par CORS! -->
<form action="https://autre-site.com/api" method="POST">
  <input name="data" value="malicious">
</form>
```

> **CORS** = contrôle les requêtes AJAX/fetch
> **CSRF** = exploite les formulaires HTML classiques

---

## 4. requestMatchers : Pourquoi POST pour certains?

### La question

```java
.requestMatchers(HttpMethod.POST, "/api/contact").permitAll()  // Avec POST
.requestMatchers("/api/auth/**").permitAll()                   // Sans méthode
.requestMatchers("/api/admin/**").hasRole("ADMIN")             // Sans méthode
```

**Pourquoi spécifier POST seulement pour `/api/contact`?**

### Explication

```mermaid
graph TB
    subgraph "/api/contact"
        C1["POST /api/contact → Envoyer formulaire ✅ Public"]
        C2["GET /api/contact → Lister contacts ❌ Protégé"]
        C3["DELETE /api/contact → Supprimer ❌ Protégé"]
    end
    
    subgraph "/api/auth/**"
        A1["POST /api/auth/login → Se connecter ✅ Public"]
        A2["POST /api/auth/register → S'inscrire ✅ Public"]
    end
    
    subgraph "/api/admin/**"
        AD1["Tout → Réservé aux ADMIN"]
    end
```

| Pattern | Signification |
|---------|---------------|
| `requestMatchers(HttpMethod.POST, "/api/contact")` | SEULEMENT POST sur /api/contact |
| `requestMatchers("/api/auth/**")` | TOUTES les méthodes sur /api/auth/* |
| `requestMatchers("/api/admin/**")` | TOUTES les méthodes sur /api/admin/* |

**Cas d'usage** :
- `/api/contact` : On veut que n'importe qui puisse ENVOYER un formulaire (POST), mais pas LISTER tous les contacts (GET)
- `/api/auth/**` : Login et register doivent être publics pour toutes les méthodes
- `/api/admin/**` : Tout est réservé aux admins

---

## 5. @PreAuthorize et le symbole #

### C'est quoi @PreAuthorize?

`@PreAuthorize` permet de sécuriser au niveau de la **méthode** (pas seulement l'URL). C'est comme un gardien personnel pour chaque fonction.

```java
@GetMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")  // Vérifie AVANT d'exécuter la méthode
public Lead getLead(@PathVariable Long id) {
    return service.findById(id);
}
```

### C'est quoi le # (dièse)?

Le `#` permet d'accéder aux **paramètres de la méthode** dans l'expression de sécurité.

```java
@DeleteMapping("/{id}")
@PreAuthorize("hasRole('ADMIN') and #id != 1")
public void delete(@PathVariable Long id) {
    //                              ↑
    //                    #id référence ce paramètre!
}
```

### Exemples avec #

```java
// #id = valeur du paramètre id (vient de l'URL : /leads/5 → id=5)
@PreAuthorize("#id != 1")
public void delete(@PathVariable Long id) { }

// #username = valeur du paramètre username (vient de ?username=jean)
@PreAuthorize("#username == authentication.principal.username")
public List<Lead> getMyLeads(@RequestParam String username) { }

// #request = l'objet request complet
@PreAuthorize("#request.email == authentication.principal.username")
public void update(@RequestBody UpdateRequest request) { }
```

### D'où vient la valeur?

```mermaid
graph LR
    subgraph "Sources des valeurs"
        URL["URL: /leads/5"] -->|"@PathVariable"| ID["#id = 5"]
        QUERY["URL: ?name=jean"] -->|"@RequestParam"| NAME["#name = 'jean'"]
        BODY["Body JSON"] -->|"@RequestBody"| OBJ["#request.email"]
    end
```

| Annotation | Source | Exemple |
|------------|--------|---------|
| `@PathVariable` | URL path | `/leads/{id}` → `#id` |
| `@RequestParam` | Query string | `?username=x` → `#username` |
| `@RequestBody` | Corps JSON | `{email: "x"}` → `#request.email` |

---

## 6. Protection à chaque couche?

### Faut-il sécuriser Controller ET Service?

```mermaid
graph TB
    REQ[Requête HTTP] --> FILTER["Spring Security Filter<br/>1ère vérification"]
    FILTER --> CTRL["Controller<br/>@PreAuthorize possible"]
    CTRL --> SVC["Service<br/>@PreAuthorize possible"]
    SVC --> REPO["Repository<br/>Pas de sécurité ici"]
    REPO --> DB[(Base de données)]
    
    style FILTER fill:#f44336,color:#fff
    style CTRL fill:#FF9800,color:#fff
    style SVC fill:#4CAF50,color:#fff
```

### Réponse courte : NON, pas à chaque couche!

| Couche | Sécurité? | Pourquoi? |
|--------|-----------|-----------|
| **Filter** | ✅ OUI | Point d'entrée - bloque les requêtes non autorisées |
| **Controller** | ⚠️ Parfois | Pour des règles spécifiques par endpoint |
| **Service** | ⚠️ Rarement | Si le service est appelé par plusieurs sources |
| **Repository** | ❌ NON | Pas de contexte utilisateur à ce niveau |

### Recommandation pratique

```java
// NIVEAU 1 : SecurityConfig (obligatoire)
.requestMatchers("/api/admin/**").hasRole("ADMIN")

// NIVEAU 2 : Controller (si besoin de règles fines)
@PreAuthorize("#id != 1")  // Empêcher suppression de l'admin principal
public void delete(@PathVariable Long id) { }

// NIVEAU 3 : Service (rarement nécessaire)
// Utile si le service est appelé par plusieurs controllers ou tâches planifiées
```

---

## 7. SimpleGrantedAuthority et getAuthorities()

### C'est quoi SimpleGrantedAuthority?

C'est une classe qui représente un **droit/permission** d'un utilisateur.

```java
// Un utilisateur peut avoir plusieurs autorités
List<GrantedAuthority> authorities = List.of(
    new SimpleGrantedAuthority("ROLE_ADMIN"),
    new SimpleGrantedAuthority("ROLE_USER"),
    new SimpleGrantedAuthority("READ_PRIVILEGE"),
    new SimpleGrantedAuthority("WRITE_PRIVILEGE")
);
```

### getAuthorities() : Qui l'appelle?

```mermaid
sequenceDiagram
    participant SS as Spring Security
    participant U as Votre classe User
    
    Note over SS: Lors de l'authentification
    SS->>U: user.getAuthorities()
    U-->>SS: List[ROLE_ADMIN]
    
    Note over SS: Lors de la vérification hasRole("ADMIN")
    SS->>SS: Vérifie si "ROLE_ADMIN"<br/>est dans la liste
```

**Réponse** : C'est **Spring Security** qui appelle `getAuthorities()`, PAS vous!

```java
@Entity
public class User implements UserDetails {
    
    private Role role;  // ADMIN ou USER
    
    @Override  // Spring Security appelle cette méthode
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Vous définissez juste QUOI retourner
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
```

### Pourquoi implémenter une interface sur notre propre classe?

```mermaid
graph TB
    subgraph "Interface Spring Security"
        UD["UserDetails<br/>(interface de Spring)"]
    end
    
    subgraph "Votre code"
        USER["User<br/>(votre entité JPA)"]
    end
    
    USER -->|"implements"| UD
    
    NOTE["Spring Security sait comment<br/>manipuler n'importe quel objet<br/>qui implémente UserDetails"]
```

**Analogie** : C'est comme une prise électrique standardisée. Spring Security est l'appareil, UserDetails est la norme de la prise. Votre classe User doit respecter cette norme pour être "branchée" à Spring Security.

---

## 8. @Bean : C'est quoi?

### Explication en 5 phrases

1. **@Bean** dit à Spring : "Crée cet objet et garde-le en mémoire"
2. Spring gère ensuite cet objet (création, injection, destruction)
3. Quand une autre classe a besoin de cet objet, Spring le fournit automatiquement
4. Sans @Bean, vous devriez créer l'objet manuellement avec `new`
5. C'est le cœur de l'**Inversion de Contrôle** (IoC) : Spring contrôle les objets, pas vous

### Avec vs Sans @Bean

```java
// ❌ SANS @Bean : Vous gérez tout manuellement
public class MonService {
    private UserDetailsService userDetailsService;
    
    public MonService() {
        // Vous devez créer vous-même
        this.userDetailsService = new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) {
                // Et vous devez aussi créer le repository manuellement!
                UserRepository repo = new UserRepository();  // Comment???
                return repo.findByEmail(username).orElseThrow();
            }
        };
    }
}

// ✅ AVEC @Bean : Spring gère tout
@Configuration
public class SecurityConfig {
    
    @Bean  // Spring crée et gère cet objet
    public UserDetailsService userDetailsService(UserRepository repository) {
        //                                        ↑ Injecté automatiquement!
        return username -> repository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("Not found"));
    }
}

@Service
public class MonService {
    private final UserDetailsService userDetailsService;  // Injecté automatiquement!
    
    public MonService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
}
```

---

## 9. CORS : Explication en 5 phrases

1. **CORS** (Cross-Origin Resource Sharing) est un mécanisme de sécurité des navigateurs web
2. Par défaut, une page web ne peut faire des requêtes qu'à son propre domaine (même origine)
3. Si votre frontend est sur `localhost:3000` et l'API sur `localhost:8080`, le navigateur bloque les requêtes
4. CORS permet au serveur de dire "J'autorise les requêtes depuis localhost:3000"
5. C'est une **liste blanche** : seules les origines explicitement autorisées peuvent appeler l'API

```java
// Le serveur dit : "Ces origines sont autorisées"
config.setAllowedOrigins(List.of(
    "http://localhost:3000",     // Frontend dev
    "https://monsite.com"        // Frontend prod
));
```

---

## 10. Récapitulatif visuel

```mermaid
mindmap
  root((Spring Security))
    Authentification
      Qui êtes-vous?
      UserDetails
      UserDetailsService
      @Bean pour configurer
    Autorisation
      Que pouvez-vous faire?
      hasRole
      @PreAuthorize
      # pour paramètres
    Protection
      CSRF inutile pour REST
      CORS pour cross-origin
      Sécuriser au bon niveau
    Avec NextAuth
      Les deux sont nécessaires
      Frontend = NextAuth
      Backend = Spring Security
```

---

## QUIZ 5.2 - Clarifications Spring Security

**1. Si j'utilise NextAuth, dois-je aussi utiliser Spring Security?**
- a) Non, NextAuth suffit
- b) Oui, les deux sont nécessaires
- c) Seulement en production
- d) C'est optionnel

<details>
<summary>Voir la réponse</summary>

**Réponse : b) Oui, les deux sont nécessaires**

NextAuth protège le frontend, Spring Security protège le backend. Un attaquant peut contourner le frontend et appeler directement l'API.
</details>

---

**2. Pourquoi désactiver CSRF pour une API REST?**
- a) Pour la performance
- b) Parce que JWT n'utilise pas de cookies automatiques
- c) CSRF n'existe pas
- d) C'est obligatoire

<details>
<summary>Voir la réponse</summary>

**Réponse : b) Parce que JWT n'utilise pas de cookies automatiques**

CSRF exploite les cookies envoyés automatiquement. JWT est envoyé manuellement dans le header Authorization, donc CSRF est impossible.
</details>

---

**3. Que signifie `#id` dans `@PreAuthorize("#id != 1")`?**
- a) Un commentaire
- b) La valeur du paramètre `id` de la méthode
- c) L'ID de l'utilisateur connecté
- d) Un nombre fixe

<details>
<summary>Voir la réponse</summary>

**Réponse : b) La valeur du paramètre `id` de la méthode**

Le # permet d'accéder aux paramètres de la méthode annotée dans l'expression SpEL.
</details>

---

**4. Qui appelle la méthode getAuthorities()?**
- a) Vous, dans votre code
- b) Spring Security automatiquement
- c) Le frontend
- d) La base de données

<details>
<summary>Voir la réponse</summary>

**Réponse : b) Spring Security automatiquement**

Spring Security appelle getAuthorities() pour récupérer les rôles/permissions de l'utilisateur lors de l'authentification et des vérifications d'autorisation.
</details>

---

**5. CORS et CSRF sont-ils la même chose?**
- a) Oui
- b) Non, CORS contrôle l'accès, CSRF est une attaque
- c) Non, CSRF contrôle l'accès, CORS est une attaque
- d) Ce sont deux protections identiques

<details>
<summary>Voir la réponse</summary>

**Réponse : b) Non, CORS contrôle l'accès, CSRF est une attaque**

CORS est un mécanisme de contrôle d'accès implémenté par les navigateurs. CSRF est un type d'attaque qui exploite les cookies de session.
</details>

---

## Navigation

| Précédent | Suivant |
|-----------|---------|
| [22 - Spring Security Introduction](22-spring-security-intro.md) | [24 - Configuration avancée](24-security-config-avancee.md) |

