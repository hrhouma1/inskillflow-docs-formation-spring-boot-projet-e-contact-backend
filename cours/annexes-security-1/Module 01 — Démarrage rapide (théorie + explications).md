### Module 1 — Démarrage rapide (théorie + explications)

Objectif du module
Comprendre ce qui se passe dès que tu ajoutes Spring Security dans un projet Spring Boot : pourquoi tout devient “bloqué”, d’où viennent la page de login, les erreurs 401/403, et comment lire ce que Spring Security fait réellement.

---

1. Ce qui arrive quand tu ajoutes Spring Security (comportement par défaut)

Quand tu ajoutes la dépendance Spring Security (starter), Spring Boot active une configuration “par défaut” (auto-configuration).
Cette configuration a un but très simple : sécuriser ton application immédiatement.

Conséquence immédiate :

* presque toutes les routes deviennent protégées
* si tu vas sur une route depuis un navigateur, tu vois souvent une page de login
* si tu appelles une API depuis Postman/curl sans credentials, tu reçois un 401

Pourquoi ce choix ?
Parce qu’en sécurité, laisser tout ouvert par défaut est dangereux.
Spring préfère “tout bloquer tant que tu n’as pas décidé explicitement quoi ouvrir”.

---

2. Le réflexe mental à prendre : “une requête traverse une chaîne de filtres”

Dans Spring MVC, tu penses souvent :
Client → Controller → Service → Repository

Avec Security, il faut toujours penser :
Client → Security Filter Chain → Controller → Service → Repository

Et la règle numéro 1 :
Si la requête est bloquée dans la filter chain, ton controller ne sera même pas exécuté.

C’est pour ça que parfois tu mets un breakpoint dans ton controller… et il n’est jamais atteint.
Ce n’est pas un bug : c’est Security qui a stoppé la requête avant.

---

3. Pourquoi tu vois une page login dans le navigateur

Si tu construis une application web (ou que tu testes une route dans le navigateur), Spring Security utilise par défaut un mécanisme “form login”.

Explication simple :

* le navigateur demande une route protégée
* Security dit : “pas connecté”
* au lieu de renvoyer seulement un 401 brut, il redirige vers une page de login (par défaut)
* une fois connecté, tu reviens à la page demandée

C’est pratique pour démarrer vite côté web.

Dans une API REST pure, ce comportement “page login” est souvent inutile.
Une API REST veut plutôt :

* répondre en JSON
* renvoyer des statuts HTTP clairs (401/403)
* pas de redirection vers une page HTML

C’est pour ça que plus tard (module API/JWT), on adapte la config.

---

4. Pourquoi une API reçoit 401 au lieu de login HTML

Quand tu appelles une route via curl/Postman, Spring Security n’a pas l’idée “page web”.
Il traite ça comme un client HTTP standard.

Donc :

* pas d’authentification fournie
* route protégée
  → 401 Unauthorized

Et souvent, tu verras aussi un header du type :
WWW-Authenticate: Basic

Parce que par défaut, Spring Security peut aussi activer HTTP Basic pour permettre un test simple.

---

5. Le mot-clé central : “par défaut, tout est protégé”

Ce que tu dois retenir :

* tant que tu n’as pas défini tes règles, Spring Security protège presque tout
* donc ton application devient “fermée”
* et c’est à toi d’ouvrir proprement ce qui doit être public

Exemples de routes typiquement publiques dans un projet réel :

* / (page d’accueil)
* /login, /logout (si app web)
* /register (inscription)
* /public/** (contenu public)
* /swagger-ui/**, /v3/api-docs/** (souvent en dev)
* /actuator/health (monitoring minimal, selon contexte)

Mais attention : “public” ne veut pas dire “sans contrôle”.
Ça veut dire “accessible sans être connecté”.

---

6. Comprendre les 3 grandes familles de configuration de sécurité (vision d’ensemble)

Au tout début, tu dois comprendre que tu vas configurer Spring Security autour de trois axes :

A) Les règles d’accès (autorisation)
Qui a le droit d’accéder à quoi ?

* public vs authentifié vs rôle ADMIN

B) Le mécanisme de connexion (authentification)
Comment l’utilisateur prouve son identité ?

* form login
* basic auth
* JWT
* OAuth2

C) Le mode de fonctionnement (stateful vs stateless)
Est-ce que le serveur garde une session ?

* stateful : session + cookie
* stateless : token à chaque requête

Module 1, on reste surtout sur la logique “comprendre”, pas encore “industrialiser”.

---

7. Lire les logs : ton meilleur outil de compréhension

Spring Security est parfois frustrant car “ça bloque” sans être évident.
Les logs te disent :

* quelle règle a matché
* quel filtre a stoppé
* pourquoi tu as eu 401/403

Même sans entrer dans la technique profonde, tu dois apprendre à lire les symptômes :

Cas typiques :

* tu reçois 401 : tu n’as pas fourni de preuve (pas authentifié)
* tu reçois 403 : tu es identifié mais pas autorisé
* tu es redirigé vers /login : comportement web (form login)
* ton controller n’est jamais appelé : filtre Security a bloqué avant

L’objectif de ce module : être capable de diagnostiquer ces situations sans panique.

---

8. Comprendre “utilisateur par défaut” (ce qui se passe au démarrage)

Au démarrage, Spring Security crée souvent un utilisateur temporaire “par défaut” en mémoire, avec un mot de passe généré dans les logs.

But :

* te permettre de tester immédiatement un login, sans base de données
* te forcer à voir le flux “authentification”

Ce n’est pas un compte réel à garder.
C’est une roue d’entraînement.

Ce qu’il faut retenir :

* tu n’es pas censé utiliser ça en production
* plus tard, tu vas fournir tes propres users (module 3)

---

9. Démarrage rapide, mais avec une mentalité propre

À ce stade, un débutant fait souvent deux erreurs :

Erreur 1 : “je désactive Security”
Oui, tu peux tout désactiver, mais tu n’apprends rien, et tu t’éloignes du réel.

Erreur 2 : “j’ouvre tout en permitAll()”
Tu avances vite, mais tu finis avec une application ouverte, donc non sécurisée.

Meilleure approche :

* ouvrir seulement ce qui doit être public
* garder le reste protégé
* comprendre 401/403 avant de continuer

---

10. Ce que tu dois être capable de faire à la fin du module 1 (sans encore coder beaucoup)

* expliquer pourquoi Spring Security “bloque tout” par défaut
* expliquer pourquoi navigateur = page login, API = 401
* faire la différence entre 401 et 403 dans tes tests
* savoir que ton controller peut ne pas être appelé à cause de Security
* comprendre que tu vas définir : règles d’accès + méthode d’auth + stateful/stateless

---

Mini quiz (pour fixer les idées)

1. Pourquoi Spring Security protège presque tout par défaut ?
2. Pourquoi tu vois une page login dans un navigateur, mais un 401 dans Postman ?
3. Si ton controller n’est jamais appelé, quelle est l’explication la plus probable ?



# Réponse question 1

<details>

  Parce que Spring Security applique une règle simple de sécurité : “tout est fermé tant que tu n’as pas décidé quoi ouvrir”.

Les raisons principales :

1. Éviter les erreurs dangereuses
   Si une appli démarre avec tout ouvert, un oubli de configuration peut exposer des routes sensibles (admin, données privées, actions de suppression). En sécurité, “par défaut ouvert” est trop risqué.

2. Te forcer à définir explicitement tes règles
   Spring veut que tu écrives clairement : quelles routes sont publiques, lesquelles demandent une connexion, lesquelles exigent un rôle. Sans ça, il préfère bloquer.

3. Principe de moindre privilège
   C’est une règle générale en cybersécurité : donner le minimum d’accès par défaut, puis élargir seulement ce qui est nécessaire.

4. Démarrage cohérent et prévisible
   Dès que Security est présent, il installe sa chaîne de filtres et protège l’accès. Tu as un comportement standard (401 pour API, redirection login en web) plutôt qu’un mélange de routes ouvertes/fermées sans logique.

</details>

# Réponse question 2

<details></details>


# Réponse question 3

<details></details>
