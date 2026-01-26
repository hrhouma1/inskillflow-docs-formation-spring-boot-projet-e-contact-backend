### Module 6 — JWT (Token) de A à Z (théorie + vulgarisation très complète)

Objectif du module
Comprendre JWT de manière solide : ce que c’est, à quoi ça sert, comment ça marche (sans tomber dans le “magique”), pourquoi c’est populaire pour les APIs REST stateless, et surtout les bonnes pratiques et pièges à éviter.

---

1. Pourquoi on utilise des tokens (et pourquoi JWT est devenu populaire)

Dans une API REST moderne, on veut souvent :

* pas de session côté serveur (stateless)
* un moyen simple d’identifier l’utilisateur à chaque requête
* une solution qui fonctionne bien avec front web SPA + mobile + autres services

Un token répond à ça :

* après un login, le serveur donne au client une “preuve”
* le client envoie cette preuve à chaque requête
* le serveur valide la preuve et autorise l’accès

JWT est un format très répandu pour ce token, car :

* il est standardisé et facile à transporter (texte)
* il peut contenir des “informations” (claims) sur l’utilisateur
* il est signé, donc falsifiable difficilement si la clé est bien gérée

---

2. JWT en une phrase (vulgarisé)

JWT est une carte d’accès signée que le client garde et présente à chaque requête.

* carte d’accès : “voici qui je suis”
* signée : “tu peux vérifier que ça vient bien de toi (le serveur)”
* présentée à chaque requête : modèle stateless

---

3. Structure d’un JWT (sans détails inutiles, mais clair)

Un JWT est composé de 3 parties séparées par des points :

* header
* payload
* signature

On le voit comme :
xxxxx.yyyyy.zzzzz

Header
Indique le type de token et l’algorithme de signature.

Payload
Contient les claims (infos) :

* qui est l’utilisateur (subject)
* quand le token expire (exp)
* rôles/permissions (parfois)
* infos techniques (issuer, audience)

Signature
Permet de vérifier que :

* le token n’a pas été modifié
* le token a été créé par quelqu’un qui possède la clé

---

4. Signé ne veut pas dire chiffré (point crucial)

Erreur classique :
“Le JWT est sécurisé, donc je peux y mettre des infos sensibles.”

Non.

JWT standard (JWS) est signé, pas chiffré.
Ça veut dire :

* n’importe qui peut lire le payload si il récupère le token
* mais il ne peut pas le modifier sans casser la signature

Conclusion de sécurité :
Ne mets jamais d’infos sensibles dans un JWT.
Pas de mot de passe, pas de numéro de carte, pas de données médicales, etc.

Tu peux mettre :

* un identifiant utilisateur
* des rôles/permissions (si tu acceptes que ce soit lisible)
* des informations techniques nécessaires

---

5. Le flux complet JWT (le scénario de base)

Étape A : login

* le client envoie username/password au serveur
* le serveur vérifie (UserDetailsService + PasswordEncoder)
* si OK, le serveur génère un JWT et le renvoie

Étape B : appel API

* le client appelle une route protégée
* il met le token dans un header :
  Authorization: Bearer <token>

Étape C : validation

* l’API reçoit la requête
* un filtre Spring Security lit le header
* il valide la signature + expiration
* s’il est valide, il “reconstruit” l’identité de l’utilisateur dans le contexte Security
* puis la requête continue vers le controller
* et les règles de rôles/permissions s’appliquent

Le point important :
Avec JWT, il n’y a pas de “session serveur”.
Tout repose sur la validité du token.

---

6. Expiration (exp) : pourquoi c’est obligatoire

Un token ne doit pas être valide “pour toujours”.

Pourquoi ?

* si un token est volé, il donne accès tant qu’il est valide
* si tu ne mets pas d’expiration, c’est un accès potentiellement illimité

Donc on met un temps de vie court pour l’access token :

* par exemple 10 min, 15 min, 30 min

Mais alors, problème :
“Je ne veux pas obliger l’utilisateur à se reconnecter toutes les 15 minutes.”

Solution : refresh token (concept suivant).

---

7. Refresh token (concept simple)

Tu utilises souvent 2 tokens :

Access token (JWT)

* court (10–30 min)
* envoyé à chaque requête

Refresh token

* plus long (jours/semaines)
* sert uniquement à demander un nouveau access token
* ne doit pas être utilisé comme preuve d’accès aux routes normales

Flux :

* access token expire → le client appelle /auth/refresh avec refresh token
* le serveur vérifie refresh token
* il renvoie un nouveau access token

Pourquoi c’est plus sûr ?

* tu limites la fenêtre de vol de l’access token
* tu peux gérer la révocation du refresh token côté serveur (liste, DB, rotation)

---

8. Révocation : le point faible du JWT pur

JWT est stateless, donc par nature :
Le serveur ne garde pas la liste de tous les tokens émis.

Donc si tu veux “annuler” un token avant son expiration, c’est plus compliqué.

Approches courantes :

* access token très court (réduit le risque)
* refresh token stocké en base et révocable
* rotation de refresh token (chaque refresh donne un nouveau refresh token)
* blacklist (liste de tokens révoqués) : possible mais réintroduit du state

Conclusion pragmatique :
En entreprise, on combine souvent :

* access token court
* refresh token contrôlé côté serveur

---

9. Où stocker le token côté client (vulgarisation + prudence)

Cas front web (SPA) :

* localStorage : simple mais exposé aux attaques XSS (si ton front a une faille)
* cookies httpOnly : plus sécurisé contre XSS, mais attention CSRF et configuration

Cas mobile :

* secure storage (keystore/keychain)

Il n’y a pas un choix “magique” universel, mais règle simple :

* évite de rendre le token accessible au JavaScript si tu veux réduire le risque XSS
* sécurise ton front (CSP, pas d’injection, etc.)

---

10. JWT et rôles/permissions : deux stratégies

Stratégie 1 : mettre les rôles dans le token
Avantages :

* l’API peut décider rapidement sans requête DB
  Limites :
* si tu changes les rôles en base, le token existant ne reflète pas le changement jusqu’à expiration

Stratégie 2 : token minimal + rechargement côté serveur
Exemple :

* le token contient juste userId
* à chaque requête, tu charges les rôles depuis la base (ou cache)

Avantages :

* droits à jour immédiatement
  Limites :
* coût plus élevé (DB/cache), mais souvent acceptable

Choix réaliste :

* projets simples : rôles dans token
* entreprise : souvent minimal + DB/cache (ou mix)

---

11. JWT vs OAuth2/OIDC (clarification)

JWT est un format de token.
OAuth2/OIDC est un protocole/standard pour déléguer l’authentification (SSO).

Tu peux avoir :

* OAuth2 qui te donne un token (souvent JWT)
* ou JWT “maison” généré par ton serveur

Donc ils ne s’opposent pas forcément.
Ils se complètent selon le besoin.

---

12. Les erreurs classiques avec JWT

* Erreur 1 : mettre des infos sensibles dans le payload

Signé ≠ chiffré.

* Erreur 2 : tokens trop longs (1 an)

Plus long = plus risqué.

* Erreur 3 : pas de rotation refresh token

Si refresh token est volé, l’attaquant peut persister longtemps.

* Erreur 4 : mauvaise gestion de la clé de signature

Si la clé est exposée, tout est compromis.

* Erreur 5 : oublier “audience/issuer” quand tu intègres plusieurs services

Dans un système multi-services, tu veux vérifier que le token est bien destiné à ton API.

---

13. Résumé du module 6 (essentiel)

* JWT = preuve signée, lisible, non chiffrée
* utilisé pour API stateless : Authorization: Bearer …
* 3 parties : header + payload + signature
* expiration obligatoire
* souvent : access token court + refresh token long
* révocation est gérée via refresh token ou stratégies de rotation/blacklist
* stockage côté client doit être réfléchi
* ne jamais mettre d’infos sensibles dans le token

---

Mini quiz

1. Pourquoi “signé” ne veut pas dire “chiffré” ?
2. Pourquoi on utilise access token court + refresh token long ?
3. Donne un risque principal du stockage du token en localStorage.

