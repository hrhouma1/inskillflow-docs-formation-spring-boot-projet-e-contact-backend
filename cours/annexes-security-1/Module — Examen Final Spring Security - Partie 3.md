# EXAMEN PRATIQUE - TROUBLESHOOTING CORS

## Spring Security : Diagnostic et résolution des problèmes CORS

<br/>
<br/>

## CAS 1 : Origin Not Allowed

### Scenario

**Frontend (React) :**
```javascript
// http://localhost:3000
fetch('http://localhost:8080/api/employees', {
  method: 'GET',
  headers: {
    'Content-Type': 'application/json'
  }
});
```

**Backend (Spring Boot) :**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
```

### Erreur dans la console

```
Access to fetch at 'http://localhost:8080/api/employees' from origin 
'http://localhost:3000' has been blocked by CORS policy: 
No 'Access-Control-Allow-Origin' header is present on the requested resource.
```

---

### Question 1.1 : Quelle est la cause principale ?

* [ ] Le frontend utilise HTTP au lieu de HTTPS
* [ ] Le backend n'a pas configuré CORS
* [ ] Le port 8080 est incorrect
* [ ] Le header Content-Type n'est pas autorisé

<details>
<summary>Réponse</summary>

**Réponse :** `Le backend n'a pas configuré CORS`

La configuration CORS est absente dans `SecurityConfig`. Il faut ajouter une configuration CORS pour autoriser les requêtes cross-origin.

</details>

---

### Question 1.2 : Quelle solution corrige le problème ?

* [ ] Changer le frontend en HTTPS
* [ ] Ajouter CorsConfigurationSource dans SecurityConfig
* [ ] Utiliser le même port pour frontend et backend
* [ ] Retirer le header Content-Type

<details>
<summary>Réponse</summary>

**Réponse :** `Ajouter CorsConfigurationSource dans SecurityConfig`

Il faut créer un bean `CorsConfigurationSource` et l'ajouter dans la configuration de sécurité avec `.cors(cors -> cors.configurationSource(corsConfigurationSource()))`.

</details>

<br/>
<br/>

## CAS 2 : Credentials Mode Error

### Scenario

**Frontend (Vue.js) :**
```javascript
// http://localhost:8080
const token = localStorage.getItem('token');
fetch('http://localhost:8080/api/employees', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  },
  credentials: 'include'  // ← Envoie cookies et Authorization
});
```

**Backend (Spring Boot) :**
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("*"));  // ← Wildcard *
    configuration.setAllowedMethods(Arrays.asList("*"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);  // ← Avec credentials
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### Erreur dans la console

```
Access to fetch at 'http://localhost:8080/api/employees' from origin 
'http://localhost:8080' has been blocked by CORS policy: 
The value of the 'Access-Control-Allow-Origin' header in the response 
must not be the wildcard '*' when the request's credentials mode is 'include'.
```

---

### Question 2.1 : Pourquoi cette erreur se produit-elle ?

* [ ] Le token JWT est invalide
* [ ] On ne peut pas utiliser "*" (wildcard) avec setAllowCredentials(true)
* [ ] Le frontend doit utiliser fetch sans credentials
* [ ] Le backend doit retirer setAllowedHeaders

<details>
<summary>Réponse</summary>

**Réponse :** `On ne peut pas utiliser "*" (wildcard) avec setAllowCredentials(true)`

Quand `allowCredentials` est `true`, on ne peut pas utiliser `"*"` pour les origines. Il faut spécifier explicitement les origines autorisées.

</details>

---

### Question 2.2 : Quelle est la solution correcte ?

* [ ] Retirer credentials: 'include' du frontend
* [ ] Remplacer "*" par Arrays.asList("http://localhost:8080")
* [ ] Retirer setAllowCredentials(true) du backend
* [ ] Utiliser cookies au lieu de Authorization header

<details>
<summary>Réponse</summary>

**Réponse :** `Remplacer "*" par Arrays.asList("http://localhost:8080")`

Il faut remplacer `setAllowedOrigins(Arrays.asList("*"))` par `setAllowedOrigins(Arrays.asList("http://localhost:8080"))` pour spécifier explicitement l'origine autorisée.

</details>

<br/>
<br/>

## CAS 3 : Preflight Failure

### Scenario

**Frontend (Angular) :**
```typescript
// http://localhost:4200
this.http.delete(`http://localhost:8080/api/employees/${id}`, {
  headers: new HttpHeaders({
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  })
}).subscribe();
```

**Backend (Spring Boot) :**
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)  // ← JWT en premier
        .cors(cors -> cors.configurationSource(corsConfigurationSource()));  // ← CORS après JWT
    
    return http.build();
}

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
    configuration.setAllowedMethods(Arrays.asList("*"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### Erreur dans la console

```
Access to XMLHttpRequest at 'http://localhost:8080/api/employees/1' from origin 
'http://localhost:4200' has been blocked by CORS policy: 
Response to preflight request doesn't pass access control check: 
It does not have HTTP ok status.
```

---

### Question 3.1 : Quelle est la cause de cette erreur ?

* [ ] La méthode DELETE n'est pas autorisée
* [ ] CORS est configuré après le filtre JWT, bloquant les requêtes OPTIONS
* [ ] L'origine Angular n'est pas correcte
* [ ] Le header Authorization manque dans setAllowedHeaders

<details>
<summary>Réponse</summary>

**Réponse :** `CORS est configuré après le filtre JWT, bloquant les requêtes OPTIONS`

Les requêtes preflight (OPTIONS) ne contiennent pas de JWT. Si le filtre JWT s'exécute avant CORS, il bloque la requête OPTIONS car elle n'a pas de token valide.

</details>

---

### Question 3.2 : Quel est l'ordre correct des filtres ?

* [ ] JWT Filter → CORS
* [ ] CORS → JWT Filter
* [ ] Authorization → CORS → JWT Filter
* [ ] L'ordre n'a pas d'importance

<details>
<summary>Réponse</summary>

**Réponse :** `CORS → JWT Filter`

CORS doit être traité en premier pour permettre les requêtes preflight (OPTIONS) qui n'ont pas de JWT. Le filtre JWT s'exécute ensuite pour authentifier les vraies requêtes.

</details>

---

### Question 3.3 : Pourquoi cet ordre est-il crucial ?

* [ ] Pour des raisons de performance
* [ ] Les requêtes OPTIONS (preflight) ne contiennent pas de JWT
* [ ] Pour éviter les attaques XSS
* [ ] C'est une convention Spring Security

<details>
<summary>Réponse</summary>

**Réponse :** `Les requêtes OPTIONS (preflight) ne contiennent pas de JWT`

Les requêtes preflight sont envoyées automatiquement par le navigateur avant la vraie requête. Elles n'ont pas de JWT, donc si le filtre JWT s'exécute en premier, il bloque la requête OPTIONS.

</details>

<br/>
<br/>

## CAS 4 : Method Not Allowed

### Scenario

**Frontend (Next.js) :**
```typescript
// http://localhost:3000
const response = await fetch('http://localhost:8080/api/employees', {
  method: 'PUT',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({ name: 'John Updated' })
});
```

**Backend (Spring Boot) :**
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "DELETE"));  // ← PUT manquant
    configuration.setAllowedHeaders(Arrays.asList("*"));
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### Erreur dans la console

```
Access to fetch at 'http://localhost:8080/api/employees' from origin 
'http://localhost:3000' has been blocked by CORS policy: 
Method PUT is not allowed by Access-Control-Allow-Methods in preflight response.
```

---

### Question 4.1 : Quelle est la cause ?

* [ ] Le controller n'a pas de méthode PUT
* [ ] setAllowedMethods ne contient pas PUT
* [ ] PUT n'est pas une méthode HTTP valide
* [ ] Le frontend doit utiliser POST au lieu de PUT

<details>
<summary>Réponse</summary>

**Réponse :** `setAllowedMethods ne contient pas PUT`

La configuration CORS n'autorise que GET, POST, DELETE. Il faut ajouter PUT dans la liste des méthodes autorisées.

</details>

---

### Question 4.2 : Quelle solution corrige le problème ?

* [ ] Ajouter "PUT" dans setAllowedMethods
* [ ] Utiliser setAllowedOrigins("*") au lieu de l'origine spécifique
* [ ] Changer PUT en POST dans le frontend
* [ ] Retirer setAllowedHeaders

<details>
<summary>Réponse</summary>

**Réponse :** `Ajouter "PUT" dans setAllowedMethods`

Il faut ajouter "PUT" dans la liste : `configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"))` ou utiliser `setAllowedMethods(Arrays.asList("*"))` pour autoriser toutes les méthodes.

</details>

<br/>
<br/>

## CAS 5 : Header Not Allowed

### Scenario

**Frontend (React) :**
```javascript
// http://localhost:3000
fetch('http://localhost:8080/api/employees', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'X-Custom-Header': 'my-value',
    'Content-Type': 'application/json'
  }
});
```

**Backend (Spring Boot) :**
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
    configuration.setAllowedHeaders(Arrays.asList("content-type", "authorization"));  // ← Headers spécifiques
    configuration.setAllowedMethods(Arrays.asList("*"));
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### Erreur dans la console

```
Access to fetch at 'http://localhost:8080/api/employees' from origin 
'http://localhost:3000' has been blocked by CORS policy: 
Request header field x-custom-header is not allowed by Access-Control-Allow-Headers 
in preflight response.
```

---

### Question 5.1 : Pourquoi l'erreur se produit-elle ?

* [ ] X-Custom-Header n'existe pas en HTTP
* [ ] setAllowedHeaders ne contient pas x-custom-header
* [ ] Les headers personnalisés sont interdits en CORS
* [ ] Le frontend doit utiliser un nom de header différent

<details>
<summary>Réponse</summary>

**Réponse :** `setAllowedHeaders ne contient pas x-custom-header`

La configuration n'autorise que "content-type" et "authorization". Il faut ajouter "x-custom-header" dans la liste des headers autorisés.

</details>

---

### Question 5.2 : Quelle est la meilleure solution ?

* [ ] Retirer X-Custom-Header du frontend
* [ ] Ajouter "x-custom-header" dans setAllowedHeaders
* [ ] Remplacer setAllowedHeaders par Arrays.asList("*")
* [ ] Utiliser Authorization pour transmettre la valeur

<details>
<summary>Réponse</summary>

**Réponse :** `Ajouter "x-custom-header" dans setAllowedHeaders` ou `Remplacer setAllowedHeaders par Arrays.asList("*")`

On peut soit ajouter explicitement "x-custom-header", soit utiliser `setAllowedHeaders(Arrays.asList("*"))` pour autoriser tous les headers (moins sécurisé mais plus flexible).

</details>

<br/>
<br/>

## CAS 6 : Wrong Origin Configuration

### Scenario

**Frontend déployé en production :**
```
URL: https://app.example.com
```

**Backend (Spring Boot) :**
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://app.example.com"));  // ← HTTP au lieu de HTTPS
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### Erreur dans la console

```
Access to fetch at 'https://api.example.com/employees' from origin 
'https://app.example.com' has been blocked by CORS policy: 
The 'Access-Control-Allow-Origin' header has a value 'http://app.example.com' 
that is not equal to the supplied origin.
```

---

### Question 6.1 : Quelle est l'erreur de configuration ?

* [ ] Le port est manquant dans setAllowedOrigins
* [ ] HTTP ne correspond pas à HTTPS
* [ ] Le sous-domaine app. est incorrect
* [ ] setAllowCredentials ne devrait pas être utilisé

<details>
<summary>Réponse</summary>

**Réponse :** `HTTP ne correspond pas à HTTPS`

L'origine configurée est `http://app.example.com` mais le frontend utilise `https://app.example.com`. Le protocole doit correspondre exactement.

</details>

---

### Question 6.2 : Quelle est la correction ?

* [ ] Changer le frontend en HTTP
* [ ] Changer setAllowedOrigins en "https://app.example.com"
* [ ] Ajouter les deux HTTP et HTTPS dans setAllowedOrigins
* [ ] Utiliser setAllowedOrigins(Arrays.asList("*"))

<details>
<summary>Réponse</summary>

**Réponse :** `Changer setAllowedOrigins en "https://app.example.com"`

Il faut corriger l'origine pour utiliser HTTPS : `configuration.setAllowedOrigins(Arrays.asList("https://app.example.com"))`.

</details>

<br/>
<br/>

## CAS 7 : Port Mismatch

### Scenario

**Frontend (Vue.js en dev) :**
```javascript
// Vite dev server : http://localhost:5173
fetch('http://localhost:8080/api/employees');
```

**Backend (Spring Boot) :**
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));  // ← Port 3000 au lieu de 5173
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("*"));
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### Erreur dans la console

```
Access to fetch at 'http://localhost:8080/api/employees' from origin 
'http://localhost:5173' has been blocked by CORS policy: 
The 'Access-Control-Allow-Origin' header has a value 'http://localhost:3000' 
that is not equal to the supplied origin.
```

---

### Question 7.1 : Quelle est la cause ?

* [ ] Le frontend utilise le mauvais port pour l'API
* [ ] Le port dans setAllowedOrigins ne correspond pas au port du frontend
* [ ] Vite utilise un port non standard
* [ ] Le backend doit écouter sur le port 5173

<details>
<summary>Réponse</summary>

**Réponse :** `Le port dans setAllowedOrigins ne correspond pas au port du frontend`

L'origine configurée est `http://localhost:3000` mais le frontend tourne sur `http://localhost:5173`. Le port fait partie de l'origine, donc ils doivent correspondre.

</details>

---

### Question 7.2 : Quelle est la solution ?

* [ ] Changer le frontend pour utiliser le port 3000
* [ ] Changer setAllowedOrigins en "http://localhost:5173"
* [ ] Ajouter ":5173" à la fin de l'URL de l'API
* [ ] Utiliser setAllowedOrigins(Arrays.asList("*"))

<details>
<summary>Réponse</summary>

**Réponse :** `Changer setAllowedOrigins en "http://localhost:5173"`

Il faut mettre à jour la configuration pour correspondre au port réel du frontend : `configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"))`.

</details>

<br/>
<br/>

## CAS 8 : Missing CORS Configuration

### Scenario

**Frontend (React) :**
```javascript
fetch('http://localhost:8080/api/employees');
```

**Backend (Spring Boot) :**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            );
        // Pas de configuration CORS !
        return http.build();
    }
    
    // CorsConfigurationSource existe mais n'est pas utilisé
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### Erreur dans la console

```
Access to fetch at 'http://localhost:8080/api/employees' from origin 
'http://localhost:3000' has been blocked by CORS policy: 
No 'Access-Control-Allow-Origin' header is present on the requested resource.
```

---

### Question 8.1 : Quelle est la cause ?

* [ ] La configuration CorsConfigurationSource est incorrecte
* [ ] CorsConfigurationSource existe mais n'est pas ajouté dans filterChain
* [ ] L'origine est incorrecte
* [ ] Le frontend doit envoyer un header spécial

<details>
<summary>Réponse</summary>

**Réponse :** `CorsConfigurationSource existe mais n'est pas ajouté dans filterChain`

Le bean `CorsConfigurationSource` existe mais n'est pas utilisé dans la configuration de sécurité. Il faut ajouter `.cors(cors -> cors.configurationSource(corsConfigurationSource()))` dans `filterChain`.

</details>

---

### Question 8.2 : Que faut-il ajouter ?

* [ ] @EnableCors sur la classe
* [ ] .cors(cors -> cors.configurationSource(corsConfigurationSource())) dans filterChain
* [ ] @CrossOrigin sur les controllers
* [ ] Un filtre CORS personnalisé

<details>
<summary>Réponse</summary>

**Réponse :** `.cors(cors -> cors.configurationSource(corsConfigurationSource())) dans filterChain`

Il faut ajouter la configuration CORS dans la chaîne de filtres :
```java
http
    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
    .csrf(csrf -> csrf.disable())
    // ...
```

</details>

<br/>
<br/>

## CAS 9 : Postman Works, Browser Doesn't

### Scenario

**Test Postman :**
```
GET http://localhost:8080/api/employees
Authorization: Bearer eyJhbGc...
```
**Résultat** : ✅ 200 OK

**Test Browser (fetch) :**
```javascript
// http://localhost:3000
fetch('http://localhost:8080/api/employees', {
  headers: { 'Authorization': `Bearer ${token}` }
});
```
**Résultat** : ❌ CORS Error

**Backend :**
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .anyRequest().authenticated()
        );
    // Aucune configuration CORS
    return http.build();
}
```

---

### Question 9.1 : Pourquoi Postman fonctionne mais pas le navigateur ?

* [ ] Postman utilise un protocole différent
* [ ] Postman n'applique pas la Same-Origin Policy
* [ ] Le navigateur bloque les requêtes HTTP
* [ ] Le token JWT n'est valide que pour Postman

<details>
<summary>Réponse</summary>

**Réponse :** `Postman n'applique pas la Same-Origin Policy`

Postman est un outil de test qui n'est pas soumis à la Same-Origin Policy du navigateur. Le navigateur applique CORS, mais Postman non.

</details>

---

### Question 9.2 : Quelle est la solution ?

* [ ] Utiliser Postman pour tous les tests
* [ ] Configurer CORS dans le backend
* [ ] Désactiver la sécurité du navigateur
* [ ] Utiliser un proxy pour contourner CORS

<details>
<summary>Réponse</summary>

**Réponse :** `Configurer CORS dans le backend`

Il faut ajouter la configuration CORS dans `SecurityConfig` pour que le navigateur autorise les requêtes cross-origin.

</details>

<br/>
<br/>

## CAS 10 : Multiple Origins

### Scenario

**Vous avez 3 frontends :**
- React Dev : `http://localhost:3000`
- React Prod : `https://app.example.com`
- Admin Vue : `http://localhost:8080`

**Backend actuel :**
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));  // ← Une seule origine
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### Problème

React Dev fonctionne, mais Admin Vue et React Prod sont bloquées.

---

### Question 10.1 : Quelle est la cause ?

* [ ] On ne peut autoriser qu'une seule origine en CORS
* [ ] setAllowedOrigins ne contient qu'une origine
* [ ] setAllowCredentials bloque les autres origines
* [ ] Il faut une configuration séparée pour chaque frontend

<details>
<summary>Réponse</summary>

**Réponse :** `setAllowedOrigins ne contient qu'une origine`

La configuration n'autorise qu'une seule origine. Il faut ajouter toutes les origines nécessaires dans la liste.

</details>

---

### Question 10.2 : Quelle est la solution correcte ?

* [ ] Créer 3 configurations CORS différentes
* [ ] Utiliser setAllowedOrigins(Arrays.asList("*"))
* [ ] Ajouter les 3 origines dans setAllowedOrigins
* [ ] Déployer tous les frontends sur le même domaine

<details>
<summary>Réponse</summary>

**Réponse :** `Ajouter les 3 origines dans setAllowedOrigins`

Il faut ajouter toutes les origines : `configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://app.example.com", "http://localhost:8080"))`.

</details>

<br/>
<br/>

## CAS 11 : Subdomain Issue

### Scenario

**Frontend :**
```
URL: https://app.example.com
```

**Backend API :**
```
URL: https://api.example.com
```

**Configuration CORS :**
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("https://example.com"));  // ← Domaine parent
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("*"));
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### Erreur dans la console

```
Access to fetch at 'https://api.example.com/employees' from origin 
'https://app.example.com' has been blocked by CORS policy.
```

---

### Question 11.1 : Pourquoi l'erreur se produit-elle ?

* [ ] HTTPS ne fonctionne pas avec les sous-domaines
* [ ] example.com et app.example.com sont des origines différentes
* [ ] Le backend doit être sur le même sous-domaine
* [ ] setAllowedOrigins ne supporte pas les sous-domaines

<details>
<summary>Réponse</summary>

**Réponse :** `example.com et app.example.com sont des origines différentes`

En CORS, `https://example.com` et `https://app.example.com` sont considérés comme des origines différentes. Il faut spécifier exactement l'origine du frontend.

</details>

---

### Question 11.2 : Quelle est la solution ?

* [ ] Utiliser un wildcard "https://*.example.com"
* [ ] Changer setAllowedOrigins en "https://app.example.com"
* [ ] Déployer frontend et backend sur le même sous-domaine
* [ ] Utiliser setAllowedOriginPatterns avec un pattern

<details>
<summary>Réponse</summary>

**Réponse :** `Changer setAllowedOrigins en "https://app.example.com"` ou `Utiliser setAllowedOriginPatterns avec un pattern`

Il faut soit spécifier exactement l'origine : `"https://app.example.com"`, soit utiliser `setAllowedOriginPatterns(Arrays.asList("https://*.example.com"))` pour autoriser tous les sous-domaines.

</details>

<br/>
<br/>

## CAS 12 : Environment-Specific Configuration

### Scenario

**Développement :**
- Frontend : `http://localhost:3000`
- Backend : `http://localhost:8080`

**Production :**
- Frontend : `https://app.example.com`
- Backend : `https://api.example.com`

**Configuration actuelle (fonctionne en dev mais pas en prod) :**
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));  // ← Seulement dev
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

---

### Question 12.1 : Quel est le problème ?

* [ ] Production n'a pas de configuration CORS
* [ ] HTTPS ne fonctionne pas avec cette configuration
* [ ] Il faut désactiver CORS en production
* [ ] setAllowCredentials est incompatible avec production

<details>
<summary>Réponse</summary>

**Réponse :** `Production n'a pas de configuration CORS`

La configuration n'autorise que l'origine de développement. En production, l'origine `https://app.example.com` n'est pas autorisée.

</details>

---

### Question 12.2 : Quelle est la meilleure approche ?

* [ ] Utiliser setAllowedOrigins(Arrays.asList("*")) en production
* [ ] Configurer les origines selon l'environnement (application.properties)
* [ ] Ajouter les deux origines dans setAllowedOrigins
* [ ] Utiliser deux configurations séparées

<details>
<summary>Réponse</summary>

**Réponse :** `Configurer les origines selon l'environnement (application.properties)`

La meilleure pratique est d'utiliser des profils Spring (dev/prod) et de lire les origines depuis `application.properties` ou `application-prod.properties` pour chaque environnement.

</details>

<br/>
<br/>

## CAS 13 : MaxAge and Performance

### Scenario

**Votre API reçoit beaucoup de requêtes DELETE.**

**Network tab montre :**
```
OPTIONS /api/employees/1 → 204 No Content
DELETE /api/employees/1 → 200 OK

OPTIONS /api/employees/2 → 204 No Content
DELETE /api/employees/2 → 200 OK

OPTIONS /api/employees/3 → 204 No Content
DELETE /api/employees/3 → 200 OK
```

**Configuration actuelle :**
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("*"));
    // Pas de setMaxAge
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

---

### Question 13.1 : Quel est le problème de performance ?

* [ ] DELETE est plus lent que GET
* [ ] Chaque DELETE déclenche une preflight OPTIONS
* [ ] Le backend répond trop lentement
* [ ] Il y a trop de headers dans les requêtes

<details>
<summary>Réponse</summary>

**Réponse :** `Chaque DELETE déclenche une preflight OPTIONS`

Sans `setMaxAge`, le navigateur envoie une requête preflight (OPTIONS) avant chaque DELETE. Cela double le nombre de requêtes.

</details>

---

### Question 13.2 : Quelle optimisation appliquer ?

* [ ] Retirer DELETE de setAllowedMethods pour éviter les preflights
* [ ] Ajouter setMaxAge pour mettre en cache les réponses preflight
* [ ] Utiliser GET au lieu de DELETE
* [ ] Désactiver CORS pour les DELETE

<details>
<summary>Réponse</summary>

**Réponse :** `Ajouter setMaxAge pour mettre en cache les réponses preflight`

`setMaxAge` indique au navigateur combien de temps mettre en cache la réponse preflight, réduisant ainsi le nombre de requêtes OPTIONS.

</details>

---

### Question 13.3 : Quelle est la configuration optimale ?

* [ ] setMaxAge(Duration.ofSeconds(30))
* [ ] setMaxAge(Duration.ofHours(24))
* [ ] setMaxAge(Duration.ofDays(365))
* [ ] Ne pas utiliser setMaxAge

<details>
<summary>Réponse</summary>

**Réponse :** `setMaxAge(Duration.ofHours(24))`

Une valeur raisonnable est 24 heures. Trop court (30 secondes) ne réduit pas assez les requêtes, trop long (365 jours) peut poser des problèmes si la configuration change.

</details>

<br/>
<br/>

## CAS 14 : Controller-Level CORS

### Scenario

**Vous avez une API publique et une API privée.**

**Configuration globale :**
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/public/**").permitAll()
            .anyRequest().authenticated()
        );
    // Pas de CORS global
    return http.build();
}

@Bean
public CorsConfigurationSource publicCorsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("GET"));
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/public/**", configuration);
    return source;
}

@Bean
public CorsConfigurationSource privateCorsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("https://app.example.com"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/employees/**", configuration);
    return source;
}
```

**Controllers :**
```java
@RestController
@RequestMapping("/api/public")
public class PublicController {
    @GetMapping
    public ResponseEntity<String> getPublicData() {
        return ResponseEntity.ok("public");
    }
}

@RestController
@RequestMapping("/api/employees")
@PreAuthorize("hasRole('USER')")
public class EmployeeController {
    @GetMapping
    public ResponseEntity<List<Employee>> getEmployees() {
        return ResponseEntity.ok(service.getAll());
    }
}
```

### Problème

Les deux endpoints sont bloqués par CORS.

---

### Question 14.1 : Quelle est la cause ?

* [ ] Les configurations publicCorsConfigurationSource et privateCorsConfigurationSource sont mal configurées
* [ ] Les configurations CORS ne sont pas utilisées dans filterChain
* [ ] Il faut absolument utiliser une configuration CORS globale
* [ ] @PreAuthorize bloque CORS

<details>
<summary>Réponse</summary>

**Réponse :** `Les configurations CORS ne sont pas utilisées dans filterChain`

Les beans `CorsConfigurationSource` existent mais ne sont pas utilisés dans la configuration de sécurité. Il faut les ajouter avec `.cors()`.

</details>

---

### Question 14.2 : Quelle est la solution ?

* [ ] Ajouter @CrossOrigin sur chaque controller
* [ ] Utiliser une seule configuration CORS globale dans filterChain
* [ ] Retirer @PreAuthorize de EmployeeController
* [ ] Fusionner les deux configurations en une seule

<details>
<summary>Réponse</summary>

**Réponse :** `Utiliser une seule configuration CORS globale dans filterChain`

La meilleure approche est de créer une seule configuration CORS qui gère les deux cas, ou d'utiliser `@CrossOrigin` sur les controllers pour une configuration plus fine.

</details>

<br/>
<br/>

## CAS 15 : Production Security Issue

### Scenario

**Configuration actuelle en production :**
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("*"));  // ← Dangereux !
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("*"));
    // Pas de setAllowCredentials
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### Problème

Un site malveillant peut appeler votre API.

---

### Question 15.1 : Quel est le risque de sécurité ?

* [ ] Les données sont exposées publiquement
* [ ] N'importe quel site peut appeler l'API
* [ ] Le backend est vulnérable aux attaques DDoS
* [ ] Les tokens JWT peuvent être volés

<details>
<summary>Réponse</summary>

**Réponse :** `N'importe quel site peut appeler l'API`

Avec `setAllowedOrigins(Arrays.asList("*"))`, n'importe quel site web peut faire des requêtes vers votre API, ce qui peut permettre des attaques CSRF ou l'utilisation abusive de votre API.

</details>

---

### Question 15.2 : Quelle est la meilleure pratique en production ?

* [ ] Utiliser setAllowedOrigins(Arrays.asList("*")) pour la flexibilité
* [ ] Spécifier les origines exactes avec setAllowedOrigins
* [ ] Désactiver CORS en production
* [ ] Utiliser un firewall pour bloquer les mauvaises origines

<details>
<summary>Réponse</summary>

**Réponse :** `Spécifier les origines exactes avec setAllowedOrigins`

En production, il faut toujours spécifier explicitement les origines autorisées pour limiter l'accès à votre API.

</details>

---

### Question 15.3 : Configuration sécurisée pour production ?

* [ ] setAllowedOrigins("*") + setAllowedMethods("*")
* [ ] setAllowedOrigins("https://app.example.com") + setAllowCredentials(true)
* [ ] setAllowedOrigins("*") + setAllowedMethods("GET")
* [ ] Pas de CORS en production

<details>
<summary>Réponse</summary>

**Réponse :** `setAllowedOrigins("https://app.example.com") + setAllowCredentials(true)`

La configuration sécurisée spécifie explicitement l'origine autorisée et active les credentials si nécessaire. Jamais de wildcard "*" en production.

</details>

<br/>
<br/>

---

## Correction

**Barème :**
- 15 cas pratiques
- 2-3 questions par cas
- Total : 35 questions
- 1 point par question
- Note sur 35

**Seuil de réussite :**
- 21/35 (60%) : Réussi
- 28/35 (80%) : Bien
- 32/35 (90%) : Très bien

---

## Conseils pour la révision

1. **Comprenez CORS** : Same-Origin Policy, preflight, credentials
2. **Ordre des filtres** : CORS avant JWT Filter
3. **Configuration** : CorsConfigurationSource + .cors() dans filterChain
4. **Sécurité** : Jamais de wildcard "*" avec credentials, spécifier les origines en production
5. **Environnements** : Utiliser des profils Spring pour dev/prod

---

**Bonne chance ! 🚀**

