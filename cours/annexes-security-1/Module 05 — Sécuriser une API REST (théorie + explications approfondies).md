### Module 5 — Sécuriser une API REST (théorie + explications approfondies)

Objectif du module
Comprendre comment sécuriser une API REST “comme en vrai” : différence avec une application web, notion stateless, pourquoi Basic Auth existe, comment gérer proprement les erreurs 401/403 en JSON, et les sujets incontournables autour de CORS et CSRF dans un contexte API.

---

1. API REST vs application web : même sécurité, attentes différentes

Une application web classique (MVC) fonctionne souvent avec :

* pages HTML
* formulaires
* redirections (vers /login)
* sessions + cookies

Une API REST moderne fonctionne souvent avec :

* JSON partout
* clients variés (front React/Angular, mobile, autres services)
* pas de redirection HTML
* stateless (pas de session côté serveur)
* authentification par token (souvent JWT) ou autre mécanisme adapté

Donc la sécurité doit être pensée pour :

* des réponses API claires
* des statuts HTTP cohérents
* une expérience développeur propre (Postman, front, etc.)

---

2. Le concept central : stateless (vulgarisé)

Stateless veut dire :
Le serveur ne “se souvient” pas du client entre deux requêtes.

Conséquence :

* chaque requête doit contenir tout ce qu’il faut pour prouver l’identité
* typiquement via un header Authorization

Pourquoi c’est populaire ?

* scalabilité plus simple (plusieurs serveurs derrière un load balancer)
* pas besoin de partager des sessions
* API plus prédictible

Image mentale

* Stateful : “je te reconnais parce que j’ai ton dossier chez moi”
* Stateless : “montre ton badge à chaque fois”

---

3. Pourquoi Basic Auth existe (et pourquoi c’est limité)

Basic Auth, c’est le mécanisme “username + password” envoyé à chaque requête (dans un header).
C’est simple à tester, et très utile pour apprendre.

Ce que Basic Auth apporte :

* tu peux protéger une API rapidement
* tu comprends 401/403 facilement
* tu n’as pas encore besoin de JWT

Mais Basic Auth a des limites :

* tu envoies des identifiants à chaque requête (même si chiffré via HTTPS, ça reste fragile)
* pas pratique pour un vrai front moderne
* pas de mécanisme natif élégant pour “session courte + refresh”
* difficile à gérer proprement à grande échelle (rotation, révocation, etc.)

Conclusion réaliste
Basic Auth est une étape pédagogique ou un choix pour des APIs internes simples.
Pour une API publique moderne, on va souvent vers token/JWT ou OAuth2.

---

4. Les erreurs 401 et 403 en API : comment tu dois les penser

Pour une API, une erreur doit être claire et exploitable par un client.

401 Unauthorized

* l’utilisateur n’est pas authentifié
* ou n’a pas fourni la preuve attendue
* réponse attendue : un JSON indiquant “auth required” (pas une page HTML)

403 Forbidden

* l’utilisateur est authentifié
* mais n’a pas les droits

Ce qui est important dans un projet :

* renvoyer un format stable (ex : { "error": "...", "message": "...", "path": "...", "timestamp": ... })
* éviter les réponses HTML
* éviter les messages trop bavards (ne pas donner d’infos à un attaquant)

But :
Le front ou le client mobile doit pouvoir faire :

* si 401 → rediriger vers login / rafraîchir token
* si 403 → afficher “accès refusé” et ne pas boucler

---

5. Pourquoi une API sécurisée doit être prévisible

Si ton API mélange :

* redirections
* pages login HTML
* erreurs brutes
* réponses parfois JSON, parfois HTML

Ton front devient un cauchemar.

Donc en REST, la règle d’or :
Toujours répondre comme une API :

* JSON
* statuts HTTP cohérents
* pas de redirection vers une page login

---

6. CORS : le sujet obligatoire dès que tu as un front séparé

CORS n’est pas “une sécurité d’API” au sens authentification.
C’est une règle du navigateur.

Problème typique :

* ton API tourne sur api.monsite.com
* ton front tourne sur app.monsite.com (ou localhost:3000)
  Le navigateur bloque certaines requêtes cross-origin si l’API n’autorise pas explicitement.

Points clés à retenir :

* CORS ne protège pas ton API contre Postman/curl (eux s’en fichent)
* CORS protège surtout les utilisateurs contre certains abus côté navigateur
* si tu ne configures pas CORS, ton front va échouer même si ton API fonctionne

Dans une API moderne, tu dois être capable d’expliquer :

* quels origins sont autorisés
* quelles méthodes (GET/POST/PUT/DELETE)
* quels headers (Authorization, Content-Type)
* si tu autorises les credentials (cookies) ou non

Règle simple :

* API token (JWT) : souvent pas de cookies → CORS plus simple
* API session cookie : credential true → CORS plus délicat

---

7. CSRF en contexte API : vulgarisation correcte

CSRF est un problème surtout quand :

* le navigateur envoie automatiquement un cookie de session
* et qu’une action sensible peut être déclenchée “à ton insu”

Dans une API stateless avec token envoyé explicitement dans Authorization, le contexte change :

* le navigateur n’envoie pas ce token “automatiquement” comme un cookie
* donc le risque CSRF est généralement différent, souvent réduit

Idée à retenir :

* CSRF est très important pour les apps web à base de session/cookie
* pour une API stateless avec JWT, on le traite autrement (souvent pas pareil, parfois désactivé selon architecture)
* mais il ne faut jamais “désactiver sans comprendre”, il faut savoir quel modèle on utilise

---

8. Le trio indispensable d’une API REST sécurisée

Dans une API moderne, tu dois choisir clairement :

A) Comment on s’authentifie

* Basic Auth (simple)
* JWT (courant)
* OAuth2/OIDC (social login, SSO entreprise)

B) Comment on gère l’état

* stateless (recommandé pour beaucoup d’APIs)
* stateful (possible, mais implique cookies/sessions)

C) Comment on gère les erreurs

* format JSON stable
* 401/403 cohérents

Sans ce trio clair, le projet devient instable.

---

9. Modèle mental “flux complet” d’une requête API sécurisée

Exemple : POST /courses (création)

1. le client appelle l’endpoint
2. Security regarde si la route est protégée
3. Security cherche l’identité (Authorization header)
4. si absent/invalid → 401
5. si valide → utilisateur authentifié
6. Security vérifie l’autorisation (rôle/permission)
7. si pas le droit → 403
8. si ok → controller exécute l’action

Tu dois être capable d’expliquer ce flux en entretien, en projet, et en debug.

---

10. Erreurs fréquentes en API REST

Erreur 1 : mélanger session web et API token sans décision claire
Ça crée des comportements incohérents.

Erreur 2 : autoriser CORS “*” en production sans réfléchir
Ça peut exposer inutilement.

Erreur 3 : retourner des erreurs différentes selon le client
API doit être stable.

Erreur 4 : protéger seulement par URL et oublier certaines actions
Les actions destructives doivent être protégées sérieusement (souvent aussi par méthode).

---

11. Résumé Module 5

* Une API REST veut du JSON, pas des redirections vers login HTML
* Stateless = chaque requête prouve l’identité (souvent via Authorization)
* Basic Auth est simple mais limité
* 401 = pas authentifié, 403 = pas autorisé
* CORS est un sujet navigateur incontournable
* CSRF concerne surtout cookies/sessions ; en stateless token, le contexte est différent
* une API sécurisée doit être prévisible : mêmes formats, mêmes statuts

---

Mini quiz

1. Pourquoi une API REST préfère stateless dans beaucoup de cas ?
2. Quelle est la différence entre CORS et authentification ?
3. Dans quel cas CSRF est particulièrement important ?

