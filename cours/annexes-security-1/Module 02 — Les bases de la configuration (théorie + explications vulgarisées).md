### Module 2 — Les bases de la configuration (théorie + explications vulgarisées)

Objectif du module
Comprendre comment Spring Security prend une décision “j’autorise / je bloque”, où on écrit ces règles, et comment penser une configuration propre sans se perdre dans les détails.

---

1. La question centrale de Spring Security

À chaque requête HTTP, Spring Security répond à ceci :

* Est-ce que cette route est publique ?
* Si non, est-ce que la personne est authentifiée ?
* Si oui, est-ce qu’elle a les droits (rôle/permission) ?

Tu peux imaginer un portier qui applique toujours le même raisonnement, dans le même ordre.

---

2. Où se mettent les règles (idée générale)

Dans Spring Boot moderne, on décrit la sécurité dans une configuration, souvent un fichier du style :

* SecurityConfig

Là, tu définis deux choses :

A) Les règles d’accès (autorisation)
Exemple conceptuel (sans code) :

* /public/** : accessible à tous
* /api/** : authentifié obligatoire
* /admin/** : rôle ADMIN obligatoire

B) Le mécanisme d’authentification

* form login (application web)
* basic auth (test rapide)
* jwt (api moderne)
* oauth2 (login google/github)

Module 2, on se concentre sur A : les règles d’accès.

---

3. Le concept de “matcher” (comment une règle s’applique)

Spring Security ne lit pas tes règles comme un humain.
Il compare la requête avec des patterns.

Exemples de patterns :

* /public/** : tout ce qui commence par /public/
* /admin/** : tout ce qui commence par /admin/
* /api/courses/** : un sous-ensemble précis

Quand une requête arrive sur /admin/stats, elle “matche” /admin/**.

C’est exactement comme des règles de pare-feu :

* si une règle correspond, elle s’applique.

---

4. L’ordre des règles est crucial (vulgarisation)

Important : la première règle qui correspond peut décider de l’accès.

Si tu écris (en logique) :

* règle 1 : /** est public
* règle 2 : /admin/** réservé ADMIN

Tu viens de casser ta sécurité, parce que /** correspond à tout, donc /admin/** sera déjà “capturé”.

Règle mentale simple :

* les règles les plus spécifiques d’abord
* les plus générales à la fin

Tu construis du précis → vers du global.

---

5. Les 3 niveaux de décision les plus fréquents

A) Public (permit all)
Ça veut dire : pas besoin d’être connecté.

Exemples typiques :

* page d’accueil
* inscription
* documentation (en dev)
* ressources statiques

B) Authentifié (authenticated)
Ça veut dire : tu dois être connecté, peu importe ton rôle.

Exemples typiques :

* profil utilisateur
* dashboard
* endpoints “mon compte”

C) Par rôle (has role)
Ça veut dire : connecté + rôle particulier.

Exemples typiques :

* /admin/**
* actions de suppression
* gestion d’utilisateurs

Dans 80% des projets débutants, ces trois niveaux suffisent.

---

6. Pourquoi on parle de “SecurityFilterChain” (explication simple)

Spring Security fonctionne avec une chaîne de filtres, et ta configuration “décrit” comment cette chaîne doit se comporter.

SecurityFilterChain, c’est l’objet qui représente :

* les règles d’accès
* les mécanismes d’auth
* certains paramètres (CSRF, CORS, sessions, etc.)

Tu n’as pas besoin de connaître toute la mécanique interne maintenant.
Tu dois juste retenir :

Tu configures un “plan de contrôle” qui décide comment les requêtes sont filtrées avant d’arriver à ton code métier.

---

7. Application web vs API : mêmes concepts, comportements différents

Les règles d’accès (public/auth/role) sont les mêmes.

Mais les comportements “autour” changent :

Application web (souvent) :

* redirection vers login
* sessions + cookies
* CSRF activé par défaut

API REST (souvent) :

* pas de redirection
* stateless
* tokens
* réponses JSON

Module 2 : tu apprends la logique des règles.
Plus tard : tu adaptes le comportement au type d’application.

---

8. Exemple mental complet (sans code) : mini système de cours

Imaginons une API de cours.

Routes :

* GET /courses : voir la liste (public)
* GET /courses/{id} : voir un cours (public)
* POST /courses : créer un cours (ADMIN)
* PUT /courses/{id} : modifier (ADMIN)
* DELETE /courses/{id} : supprimer (ADMIN)
* POST /enroll/{courseId} : s’inscrire (USER connecté)

Règles :

* /courses/** en GET : public
* /enroll/** : authentifié (ou rôle USER)
* POST/PUT/DELETE sur /courses/** : rôle ADMIN

Tu vois ici un point important : parfois la règle dépend aussi de la méthode HTTP (GET/POST/PUT/DELETE).
C’est très commun en REST.

---

9. Différence entre sécuriser par URL et sécuriser par méthode

A) Par URL
Tu protèges l’accès aux endpoints.

Avantage :

* simple à visualiser
* centralisé

Limite :

* parfois tu veux une règle plus fine à l’intérieur du code

B) Par méthode
Tu mets une règle directement sur une méthode Java.

Exemple logique :

* “seul ADMIN peut exécuter deleteCourse()”

Avantage :

* proche du métier
* protège même si quelqu’un expose la méthode via un autre endpoint plus tard

Limite :

* tu dois être rigoureux pour garder une vue d’ensemble

Bonne pratique fréquente :

* règles simples par URL
* règles sensibles par méthode (double sécurité)

---

10. Les erreurs les plus fréquentes au début

Erreur 1 : ouvrir trop large

* mettre “tout public” juste pour tester
  Ça cache les vrais problèmes et retarde l’apprentissage.

Erreur 2 : mettre des règles dans le mauvais ordre

* une règle globale trop tôt
  Ça fait que les règles spécifiques ne servent plus.

Erreur 3 : confondre “authentifié” et “rôle”

* authentifié = connecté
* rôle = niveau d’accès

---

11. Ce que tu dois retenir à la fin du module 2

* Spring Security applique des règles basées sur des patterns d’URL (et parfois méthodes HTTP)
* l’ordre des règles est essentiel (spécifique → général)
* on construit souvent 3 niveaux : public / authentifié / rôle
* la configuration décrit le comportement de la chaîne de filtres
* web et API partagent la logique, mais pas forcément les mêmes comportements

---

Mini quiz (pour valider)

1. Pourquoi l’ordre des règles est-il important ?
2. Différence entre “public” et “authenticated” ?
3. Si tu mets /** en public au début, qu’est-ce qui arrive à /admin/** ?

