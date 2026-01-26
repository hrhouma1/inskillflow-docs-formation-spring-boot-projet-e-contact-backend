### Module 0 — Mise en contexte (théorie + vulgarisation)

Objectif du module
Comprendre clairement pourquoi Spring Security existe, ce qu’il protège, et les notions indispensables avant d’écrire une seule ligne de configuration.

---

1. Pourquoi sécuriser une application (API ou site web)

Quand tu crées une application Spring Boot, tu exposes des routes, par exemple :

* GET /courses (liste de cours)
* POST /courses (création d’un cours)
* GET /admin/stats (stats internes)

Sans sécurité, toute personne qui connaît l’URL peut appeler ces routes.
Le problème n’est pas “si quelqu’un trouve l’URL”, le problème c’est qu’une URL est faite pour être appelée, donc si tu ne mets aucune règle, tu ne contrôles rien.

En pratique, on veut toujours répondre à 2 questions :

* Qui es-tu ? (authentification)
* As-tu le droit ? (autorisation)

Et Spring Security sert exactement à appliquer ces deux idées partout dans ton application de façon cohérente.

---

2. Authentification vs Autorisation (différence simple)

Authentification = prouver ton identité
Exemples :

* login + mot de passe
* token (JWT)
* connexion Google / GitHub (OAuth2)
* clé API (API key)

Autorisation = décider ce que tu peux faire une fois identifié
Exemples :

* un USER peut lire ses données
* un ADMIN peut supprimer un utilisateur
* un visiteur (non connecté) peut accéder à la page d’accueil, mais pas au tableau de bord

Image mentale simple

* Authentification : montrer ta carte d’identité à l’entrée
* Autorisation : vérifier si ton badge te donne accès à telle salle

Important
Tu peux être authentifié (donc “connecté”) mais quand même refusé (pas les droits).
C’est exactement la différence entre “401” et “403” (on y revient plus bas).

---

3. C’est quoi Spring Security en une phrase

Spring Security est un ensemble de filtres et de règles qui s’exécutent avant ton contrôleur Spring pour :

* bloquer ou autoriser une requête
* déclencher une authentification si nécessaire
* attacher l’utilisateur connecté à la requête
* appliquer des règles basées sur des rôles/permissions

Ce n’est pas juste “un écran login”.
C’est un mécanisme global qui protège toutes tes routes et tes méthodes.

---

4. Comment Spring Security “voit” une requête (vulgarisation)

Quand un client envoie une requête HTTP à ton application :

Client → Spring Boot → Controller → Service → DB

Avec Spring Security, il y a une étape “vigile” avant d’entrer :

Client → chaîne de filtres Security → Controller → Service → DB

Ces filtres sont comme des agents de sécurité à l’entrée :

* l’un vérifie “est-ce que cette route est publique ?”
* l’un cherche un token dans les headers
* l’un vérifie le mot de passe si login
* l’un décide : laisser passer, ou bloquer

C’est pour ça qu’on parle souvent de filter chain (chaîne de filtres).

---

5. Les statuts 401 et 403 (très important à comprendre)

401 Unauthorized
Ça veut dire : tu n’es pas authentifié.
En clair : “je ne sais pas qui tu es” ou “tu n’as pas fourni de preuve”.

Exemples :

* tu appelles une route protégée sans token
* tu donnes un mauvais login/mot de passe

403 Forbidden
Ça veut dire : je sais qui tu es, mais tu n’as pas le droit.
En clair : “tu es connecté, mais pas autorisé”.

Exemple :

* tu es USER, tu appelles une route réservée ADMIN

Si tu comprends bien 401/403, tu débogues Spring Security beaucoup plus vite.

---

6. Session vs Token (conceptuellement)

Deux grandes façons de “retenir” que tu es connecté.

A) Session (souvent pour les sites web classiques)
Principe :

* tu te connectes avec login/mot de passe
* le serveur crée une session côté serveur
* ton navigateur reçoit un cookie de session
* à chaque requête suivante, le navigateur renvoie le cookie

Avantages :

* simple pour les applications web traditionnelles
* très intégré avec formulaires et pages

Limites :

* moins pratique pour API consommées par mobiles / front séparé
* gestion de scalabilité (plusieurs serveurs) plus délicate

B) Token (souvent pour API REST modernes)
Principe :

* tu te connectes
* le serveur te donne un token (ex : JWT)
* à chaque requête : tu envoies le token dans Authorization: Bearer ...
* le serveur vérifie le token, sans stocker de session

Avantages :

* très adapté aux API + front séparé
* stateless (plus simple à scaler)
* standard pour mobile/web SPA

Limites :

* il faut bien gérer expiration, refresh, stockage côté client

Résumé simple

* Session = “le serveur se souvient de toi”
* Token = “tu prouves qui tu es à chaque requête”

---

7. CSRF (vulgarisé, sans détails inutiles)

CSRF est un problème surtout lié aux cookies et aux sessions web.
Idée simple : si ton navigateur est déjà connecté (cookie automatique), un autre site peut tenter de te faire exécuter une action sans que tu t’en rendes compte.

C’est pour ça que Spring Security active souvent des protections CSRF par défaut dans des scénarios web.

Pour une API REST stateless avec JWT, le contexte est différent, et on configure souvent CSRF autrement (tu verras ça plus tard, module API/JWT).

---

8. Rôles et permissions (concept)

Rôle = grande catégorie d’accès
Exemples :

* ROLE_USER
* ROLE_ADMIN

Permission = action précise (plus fine)
Exemples :

* course:read
* course:create
* user:delete

Beaucoup d’applications commencent avec rôles, puis évoluent vers permissions quand ça devient plus complexe.

---

9. Les trois “cibles” de Spring Security

Spring Security peut sécuriser à plusieurs niveaux :

A) Par URL (routes)
Exemples :

* /public/** accessible à tous
* /admin/** réservé ADMIN

B) Par méthode (Service / Controller)
Exemples :

* une méthode Java ne s’exécute que si tu as un rôle particulier

C) Par objet / données (plus avancé)
Exemples :

* tu peux lire un “document” seulement si tu en es le propriétaire

Dans un cours, on apprend généralement dans cet ordre :
URL → méthode → cas avancés

---

10. Ce que tu dois retenir avant de passer au module 1

* Spring Security protège en interceptant les requêtes avant tes controllers
* Authentification = qui tu es
* Autorisation = ce que tu as le droit de faire
* 401 = pas connecté, 403 = connecté mais pas le droit
* Session et Token sont deux logiques différentes pour gérer la connexion
* CSRF concerne surtout les apps web basées sur cookies/sessions

---

Mini quiz rapide (pour vérifier que c’est clair)

1. Si tu appelles une route protégée sans token et tu reçois 401, ça veut dire quoi ?
2. Si tu es connecté mais tu reçois 403 sur /admin, ça veut dire quoi ?
3. Dans une API REST moderne, on préfère souvent session ou token ? Pourquoi ? (une phrase)

