### Module 4 — Rôles et permissions (encore plus détaillé, théorie + vulgarisation)

Objectif du module
Comprendre comment organiser les droits dans une application Spring Security : rôles vs permissions, comment concevoir une stratégie simple au début puis extensible, et comment appliquer ces droits au bon niveau (URL ou méthode) sans faire n’importe quoi.

---

1. Le vrai problème : “qui peut faire quoi”

Dès que ton application a plus qu’un seul type d’utilisateur, tu dois décider :

* qui peut lire des données
* qui peut créer / modifier / supprimer
* qui peut accéder à l’administration
* qui peut voir ses propres données seulement

Spring Security ne décide pas à ta place.
Il te donne un cadre pour appliquer tes décisions.

Donc le module 4 n’est pas seulement technique : c’est du design d’accès.

---

2. Rôle vs Permission (différence claire)

Rôle
Un rôle, c’est une catégorie d’utilisateur, une vue “macro”.

Exemples :

* USER : utilisateur normal
* ADMIN : administrateur
* MANAGER : superviseur

Permission (authority)
Une permission, c’est une action précise, une vue “micro”.

Exemples :

* course:read
* course:create
* course:update
* course:delete
* user:ban
* report:export

Vulgarisation

* Rôle = ton métier (qui tu es dans le système)
* Permission = ton droit exact (ce que tu peux faire)

---

3. Pourquoi commencer par des rôles (et quand ça devient insuffisant)

Pour un petit projet, 2 rôles suffisent souvent :

* USER
* ADMIN

C’est simple, lisible, efficace.

Mais au bout d’un moment, tu vas avoir des demandes du type :

* “un ADMIN peut tout faire sauf supprimer les factures”
* “un MANAGER peut modifier les cours mais pas supprimer”
* “un SUPPORT peut lire les comptes mais pas modifier”

Là, les rôles explosent :

* ADMIN_FULL
* ADMIN_LIMITED
* MANAGER_CONTENT
* SUPPORT_READONLY
  et tu te retrouves avec une liste incompréhensible.

C’est le signe qu’il faut passer à des permissions fines.

---

4. Stratégie propre et réaliste (celle qu’on voit en entreprise)

Approche progressive :

Étape 1 : rôles simples

* ROLE_USER
* ROLE_ADMIN

Étape 2 : permissions métier

* course:read, course:write, course:delete
* user:read, user:write
* etc.

Étape 3 : mapping rôle → permissions
Un rôle devient un “bundle” de permissions.
Exemple :

* ADMIN = toutes les permissions
* MANAGER = course:read + course:write
* SUPPORT = user:read

Ainsi tu gardes :

* simplicité (rôles)
* précision (permissions)
* évolutivité (tu changes des bundles sans casser tout le code)

---

5. Comment Spring Security “comprend” les rôles

Dans Spring, un rôle est généralement une authority avec un préfixe.

Exemple mental :

* rôle ADMIN → "ROLE_ADMIN"
* rôle USER → "ROLE_USER"

Donc au niveau sécurité, tout finit souvent en “authorities”.

Tu n’as pas besoin de te battre avec les détails maintenant, juste retenir :

* rôle = une forme spéciale d’authority
* permissions = authorities “normales”

---

6. Deux endroits où tu appliques les droits

A) Par URL (routes)
Tu dis : “cette route exige tel rôle”.

Exemples conceptuels :

* /admin/** : ADMIN seulement
* /api/** : authentifié
* /public/** : public

Avantages :

* très rapide à comprendre
* tu vois la sécurité au niveau HTTP

Limites :

* parfois tu as besoin de règles plus fines (propriétaire d’un objet, action spécifique)

B) Par méthode (niveau code Java)
Tu dis : “cette méthode ne s’exécute que si tu as telle permission”.

Exemples conceptuels :

* deleteCourse() nécessite course:delete
* exportReport() nécessite report:export

Avantages :

* proche du métier
* protège même si un endpoint change plus tard
* sécurité plus robuste

Limites :

* si tu mets tout au niveau méthode, tu perds une vision globale

Bonne pratique fréquente :

* URL pour le “gros filtrage”
* méthode pour les opérations critiques

---

7. Exemple complet (vulgarisé) : API “Gestion de cours”

Routes :

* GET /courses : public (tout le monde peut lire)
* POST /courses : réservé ADMIN (création)
* PUT /courses/{id} : ADMIN ou MANAGER (édition)
* DELETE /courses/{id} : ADMIN seulement
* POST /enroll/{courseId} : USER connecté

Si tu fais seulement avec rôles :

* ADMIN peut tout faire
* USER peut s’inscrire
* MANAGER ajoute de la complexité

Avec permissions :

* course:read
* course:create
* course:update
* course:delete
* enroll:create

Tu peux décider :

* ADMIN : tout
* MANAGER : course:read + course:update
* USER : course:read + enroll:create

Et tu n’as pas besoin d’inventer 20 rôles.

---

8. Le cas très important : “propriétaire de la donnée”

Un cas réel :

* un utilisateur peut voir ses propres inscriptions
* mais pas celles des autres

Ce n’est pas juste un rôle.
C’est une règle dynamique liée à la donnée.

Exemples :

* USER A peut lire /users/A/enrollments
* USER A ne peut pas lire /users/B/enrollments

On appelle ça souvent de l’autorisation “par contexte”.

Tu ne peux pas résoudre ça uniquement par URL.
Tu as besoin d’une vérification côté code, souvent au niveau méthode/service.

C’est une raison forte d’utiliser la sécurité par méthode en complément.

---

9. Les pièges classiques avec les rôles

Piège 1 : “ADMIN partout”
Si tout ce qui est sensible est “ADMIN”, tu as une application binaire :

* soit tu es admin, soit tu ne peux rien faire
  C’est rarement réaliste.

Piège 2 : trop de rôles
Si tu crées un rôle pour chaque nuance, tu perds la maîtrise.

Piège 3 : logique dispersée
Si tu mets des if partout dans le code (if user is admin …), tu casses l’idée même de Spring Security, qui est centraliser la décision.

---

10. Bonnes pratiques simples (même pour débutant)

* Commencer avec 2 rôles : USER et ADMIN
* Ajouter des permissions quand les règles deviennent fines
* Garder une logique lisible : routes publiques clairement listées
* Protéger les actions destructives par méthode (suppression, export, admin)
* Éviter les if dans le code métier (favoriser les règles de sécurité)

---

11. Résumé à retenir

* Rôle = catégorie, Permission = action précise
* Commencer simple, évoluer vers permissions
* URL = filtrage global, méthode = contrôle fin
* Certaines règles sont contextuelles (propriétaire), donc niveau méthode souvent nécessaire
* Éviter “ADMIN partout” et éviter “100 rôles”

---

Mini quiz

1. Pourquoi trop de rôles est un problème ?
2. Donne un exemple où une permission est meilleure qu’un rôle.
3. Pourquoi “propriétaire de la donnée” est difficile à gérer seulement avec des routes ?

