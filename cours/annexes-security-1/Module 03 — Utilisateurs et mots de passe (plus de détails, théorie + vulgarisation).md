### Module 3 — Utilisateurs et mots de passe (plus de détails, théorie + vulgarisation)

Objectif du module
Comprendre comment Spring Security représente un utilisateur, comment il vérifie un mot de passe, pourquoi on ne stocke jamais un mot de passe en clair, et comment passer d’un utilisateur “en mémoire” à une base de données propre.

---

1. Ce que Spring Security veut savoir sur un utilisateur

Spring Security ne cherche pas à connaître “toute ta table user”.
Pour sécuriser une application, il lui faut seulement quelques informations essentielles :

* username (ou email) : l’identifiant de connexion
* password : le mot de passe stocké (mais encodé)
* enabled / disabled : est-ce que le compte est actif ?
* roles / authorities : ce que l’utilisateur a le droit de faire

Le reste (nom, prénom, téléphone, adresse) appartient à ton domaine métier, pas au système de sécurité.

Idée importante
La sécurité a besoin d’un profil minimal “authentifiable”, pas d’un profil “complet”.

---

2. Authentification : comment Spring valide l’identité (logique mentale)

Quand quelqu’un tente de se connecter (login + mot de passe), Spring Security fait toujours la même chose :

1. récupérer l’utilisateur à partir du username (ou email)
2. comparer le mot de passe donné avec le mot de passe stocké
3. si c’est bon : l’utilisateur est authentifié
4. sinon : refus (401 ou échec login)

Le point critique est l’étape 2 : comparer sans jamais stocker le mot de passe en clair.

---

3. Pourquoi on ne stocke jamais un mot de passe en clair (explication simple)

Si tu stockes un mot de passe en clair et que ta base fuite, c’est la catastrophe :

* l’attaquant voit directement les mots de passe
* et surtout, les gens réutilisent souvent le même mot de passe ailleurs

Donc la règle universelle :
On ne stocke jamais le mot de passe réel.
On stocke un résultat “transformé” qui ne permet pas de retrouver le mot de passe facilement.

Cette transformation s’appelle l’encodage / hashing.

---

4. Hashing vs Encryption (différence vulgarisée)

Beaucoup de débutants confondent.

Encryption (chiffrement) :

* réversible si tu as la clé
* donc quelqu’un qui obtient la clé peut retrouver le mot de passe

Hashing (hachage) :

* non réversible (en pratique)
* tu peux vérifier, mais pas retrouver

Pour un mot de passe, on veut hashing, pas encryption.

---

5. Salt : pourquoi deux mêmes mots de passe ne doivent pas donner le même résultat

Imaginons deux utilisateurs qui ont choisi “Password123”.

Sans protection supplémentaire, un hashing naïf donnerait la même valeur stockée.
Un attaquant peut alors détecter des patterns, et utiliser des tables pré-calculées (rainbow tables).

Solution : salt
Le salt est une valeur aléatoire ajoutée au mot de passe avant hachage.

Résultat :

* deux personnes avec le même mot de passe auront des valeurs stockées différentes
* ce qui rend les attaques pré-calculées beaucoup moins efficaces

---

6. Pourquoi BCrypt est souvent recommandé

BCrypt est un algorithme de hashing conçu pour les mots de passe.

Deux raisons principales :

A) Il est “lent volontairement”
Ça paraît bizarre, mais c’est le but.
Pour un humain, une connexion prend 200 ms, ce n’est pas grave.
Pour un attaquant qui veut tester 1 milliard de mots de passe, la lenteur devient un mur.

B) Il gère bien le salt
Il intègre les mécanismes pour éviter les collisions et renforcer la robustesse.

Idée simple
Un bon hashing mot de passe doit être coûteux pour l’attaquant, pas pour toi.

---

7. PasswordEncoder : le rôle exact dans Spring Security

Dans Spring Security, PasswordEncoder est le composant qui fait deux choses :

* encoder un mot de passe quand tu enregistres un utilisateur
* vérifier un mot de passe lors d’une connexion

Très important : lors d’une connexion, Spring ne “décode” pas le mot de passe.
Il ré-encode le mot de passe fourni, et compare intelligemment avec la valeur stockée.

Donc :

* on n’a pas besoin de retrouver le mot de passe original
* on a juste besoin de vérifier que la personne connaît le bon mot de passe

---

8. Utilisateur “en mémoire” : à quoi ça sert (et ses limites)

Quand tu débutes, Spring Security peut fonctionner avec des utilisateurs “en mémoire”.

C’est utile pour apprendre :

* comprendre les rôles
* tester des routes protégées
* valider 401/403
* apprendre la configuration sans base de données

Mais c’est limité :

* tout disparaît au redémarrage
* pas de gestion réelle d’inscription
* pas de persistance
* pas de gestion fine (comptes, verrouillage, etc.)

Donc c’est une étape pédagogique, pas une solution.

---

9. UserDetails et UserDetailsService (concepts centraux)

UserDetails
C’est la “fiche sécurité” d’un utilisateur.
Elle contient exactement ce que Spring Security veut : username, password, rôles, statut du compte.

UserDetailsService
C’est le “service de recherche d’utilisateur” utilisé par Spring Security.

Il répond à une question :
“Donne-moi l’utilisateur correspondant à ce username.”

Spring Security ne sait pas où sont tes utilisateurs :

* mémoire
* base SQL
* LDAP
* service externe
  Il veut juste une fonction qui lui donne l’utilisateur.

Vulgarisation
UserDetailsService, c’est comme un annuaire :
“Tu me donnes un identifiant, je te retourne le profil sécurité.”

---

10. Passer à une base de données : la logique propre

Quand tu passes en base, tu dois penser en deux objets :

A) Ton objet métier UserEntity

* id, email, passwordHash, createdAt, etc.

B) L’objet attendu par Spring Security UserDetails

* username, password, roles, enabled, etc.

Tu fais une adaptation entre les deux.
C’est propre, parce que :

* ton domaine métier reste ton domaine
* Spring Security reçoit uniquement ce dont il a besoin

---

11. Rôles vs Authorities (point qui mélange souvent)

Rôle (role)

* vision “macro”
* souvent : USER, ADMIN

Authority (authority / permission)

* vision “fine”
* souvent : course:read, course:write, user:delete

Dans Spring, un rôle est souvent une authority avec un préfixe ROLE_.

Tu n’as pas besoin d’entrer dans le détail maintenant, mais retiens :

* rôle = simple
* authority = plus précis

---

12. Ce qu’on fera plus tard grâce à ces notions (projection)

Une fois que tu comprends ce module, tu pourras construire :

* inscription : encoder le mot de passe avant sauvegarde
* login : vérifier le mot de passe via PasswordEncoder
* routes protégées par rôles
* JWT : ton user est chargé depuis la base pour créer le token
* OAuth2 : ton user vient d’un provider externe, puis tu l’associes à ton système

---

13. Erreurs fréquentes (pour éviter les pièges)

Erreur 1 : stocker le mot de passe en clair
C’est interdit en pratique.

Erreur 2 : encoder deux fois
Exemple : tu encodes à l’inscription, puis tu encodes encore au login manuellement.
Tu dois laisser Spring faire la vérification correctement.

Erreur 3 : confondre username et email sans cohérence
Choisis un identifiant (email souvent) et sois constant partout.

Erreur 4 : oublier enabled/disabled
Un compte désactivé doit être bloqué même si mot de passe correct.

---

14. Résumé du module 3 (à savoir par cœur)

* Spring Security a besoin d’un profil minimal utilisateur : identifiant + password encodé + rôles + statut
* on stocke des hashes, pas des mots de passe
* BCrypt est recommandé car il est lent et salé
* PasswordEncoder sert à encoder et à vérifier, sans décoder
* UserDetails = fiche sécurité
* UserDetailsService = annuaire qui charge l’utilisateur

---

Mini quiz (pour valider)

1. Pourquoi on préfère hashing à encryption pour un mot de passe ?
2. Pourquoi BCrypt est volontairement lent ?
3. UserDetailsService sert à faire quoi, en une phrase ?

