### Module 8 — OAuth2 + OpenID Connect (SSO) (version longue, vulgarisée, très théorique)

Objectif du module
Comprendre clairement ce qu’est OAuth2 et OpenID Connect, pourquoi on les utilise en entreprise (SSO), comment ça se distingue de “JWT maison”, quels sont les acteurs (client, provider, resource server), et comment les tokens circulent. Le but est que tu puisses l’expliquer simplement à quelqu’un d’autre et prendre de bonnes décisions d’architecture.

---

1. Le problème que OAuth2 / OIDC résout (version simple)

Imagine ton application. Tu veux des utilisateurs qui se connectent.

Option A : tu gères tout toi-même

* inscription
* mot de passe
* reset password
* MFA
* sécurité des comptes
* conformité
* etc.

Option B : tu délègues l’authentification à un fournisseur externe

* Google
* GitHub
* Microsoft
* un SSO d’entreprise (Azure AD / Entra ID, Okta, Keycloak, etc.)

L’utilisateur se connecte chez le fournisseur, et ton application reçoit une preuve.

Résultat :

* tu ne stockes pas de mots de passe
* tu bénéficies souvent de MFA, gestion de risques, politiques d’entreprise
* tu simplifies énormément les aspects sécurité “comptes”

OAuth2 / OIDC sont des standards qui organisent ça.

---

2. OAuth2 vs OpenID Connect (différence critique)

Beaucoup de gens disent “OAuth2 = login”.
C’est imprécis.

OAuth2

* c’est un protocole d’autorisation (authorization)
* il sert à obtenir un accès à une ressource
* exemple : permettre à une application d’accéder à tes données Google (avec ton accord)

OpenID Connect (OIDC)

* c’est une couche au-dessus de OAuth2 pour l’authentification (identity)
* il ajoute une notion standard d’identité utilisateur
* il fournit un ID token (souvent un JWT) qui dit “voici qui est l’utilisateur”

À retenir absolument

* OAuth2 = autoriser une application à accéder à une ressource
* OIDC = authentifier l’utilisateur + obtenir son identité

Dans le langage courant, quand on fait “Login with Google”, on utilise presque toujours OAuth2 + OIDC.

---

3. Les acteurs (vocabulaire de base)

Tu as toujours 3 rôles principaux :

1. L’utilisateur (resource owner)
   La personne qui veut se connecter.

2. Le client (client application)
   Ton application (frontend + backend) qui veut déléguer la connexion.

3. Le provider (authorization server / identity provider)
   Google, GitHub, Microsoft, Okta, Keycloak, etc.

Et parfois un 4e :

4. Resource server
   Un serveur d’API qui protège des ressources, et qui valide les tokens.

Exemple concret :

* Google = authorization server + identity provider
* Ton API = resource server
* Ton frontend = client

---

4. Pourquoi les entreprises aiment le SSO

SSO = Single Sign-On
Une entreprise veut souvent :

* un seul compte central
* des politiques globales (MFA obligatoire, rotation, règles de sécurité)
* désactivation immédiate d’un compte quand quelqu’un quitte l’entreprise
* audit des connexions
* contrôle d’accès centralisé

Si tu gères tes mots de passe toi-même, tu dois reconstruire tout ça.

Avec OIDC + provider entreprise :

* tu “branches” ton application sur le système d’identité existant
* tu récupères l’identité et tu appliques tes règles internes (rôles/permissions)

---

5. Les tokens dans OAuth2/OIDC (concepts)

Dans ce monde, tu verras souvent 3 types de tokens.

A) ID token (OIDC)

* prouve l’identité
* contient des infos d’identité (subject, email, nom, etc.)
* souvent un JWT

B) Access token (OAuth2)

* sert à accéder à une API (resource server)
* est présenté à l’API pour prouver l’autorisation
* peut être un JWT ou un token opaque selon provider

C) Refresh token

* permet d’obtenir un nouveau access token
* long, sensible, à gérer avec précaution

Ce qu’il faut comprendre :

* ID token = identité (qui tu es)
* access token = accès (ce que tu peux appeler)
* refresh token = continuité (rester connecté sans se relog)

---

6. Flow principal pour une application moderne (vulgarisé)

Le “flow” le plus courant pour du login web moderne s’appelle Authorization Code Flow (souvent avec PKCE quand client public).

Idée générale (sans détails techniques) :

1. l’utilisateur clique “Login with Google”
2. ton application redirige vers Google
3. Google authentifie l’utilisateur (mot de passe, MFA, etc.)
4. Google renvoie un code temporaire à ton application
5. ton backend échange ce code contre des tokens (id token + access token)
6. ton app connaît l’utilisateur et crée une session applicative ou fournit un token interne

La clé :
Le mot de passe ne passe jamais chez ton application.

---

7. PKCE (vulgarisé, utile à comprendre)

PKCE sert à renforcer la sécurité du flow, surtout quand le client n’est pas capable de garder un secret (ex : SPA, mobile).

Idée :

* au lieu de faire confiance à un “client secret” stocké dans un navigateur (impossible à cacher), on utilise un mécanisme de preuve lors de l’échange du code.

Tu n’as pas besoin de connaître les détails cryptographiques pour le moment, mais retiens :
PKCE est une protection standard dans beaucoup d’applications modernes.

---

8. OAuth2/OIDC vs JWT maison : quand choisir quoi

JWT maison (ton serveur génère ses propres tokens)
Avantages :

* très simple à adapter à ton domaine
* tu contrôles entièrement les claims
* idéal pour un produit que tu maîtrises de bout en bout

Limites :

* tu dois gérer la sécurité des comptes (ou au moins les login/password)
* tu dois gérer MFA si nécessaire
* tu dois gérer resets, politiques, etc.

OAuth2/OIDC (SSO)
Avantages :

* tu délègues la sécurité du login à un provider (souvent très solide)
* tu peux brancher ton app sur un SSO entreprise
* tu évites la gestion des mots de passe
* tu bénéficies de MFA et politiques globales

Limites :

* plus complexe au départ
* dépendance au provider
* gestion des rôles internes peut nécessiter un mapping

Choix typique :

* application interne entreprise : OAuth2/OIDC presque obligatoire
* produit grand public : souvent mix (login maison + providers sociaux)
* architecture microservices : OAuth2/OIDC fréquent, mais parfois JWT interne aussi

---

9. Très important : identité externe vs autorisation interne

Quand tu utilises Google/Microsoft/Okta, tu reçois l’identité.

Mais ton application doit encore décider :

* est-ce que cet utilisateur a accès à ton application ?
* quel rôle lui donner ?
* quelles permissions ?

Donc il y a souvent une étape de “mapping” :

* tu reçois email/subject du provider
* tu cherches l’utilisateur dans ta base (ou tu le crées à la première connexion)
* tu attribues un rôle (USER par défaut, ou selon domaine)
* tu appliques tes permissions

Vulgarisation :
Le provider dit “qui c’est”.
Ton application dit “ce qu’il peut faire chez toi”.

---

10. Deux architectures courantes en entreprise

A) Application web + API dans le même backend

* le backend gère OAuth2 login
* il crée une session ou un token interne
* il protège ensuite ses propres endpoints

B) Front séparé + API séparée (plus moderne)

* le provider fournit un access token
* le front appelle l’API avec Authorization Bearer
* l’API valide le token (resource server)
* les rôles/permissions sont gérés via claims ou via DB interne

Dans une vraie entreprise, c’est très fréquent :

* token OIDC validé côté API
* mapping des rôles dans l’API

---

11. Tokens “opaques” vs JWT (précision utile)

Certains providers donnent des access tokens qui ne sont pas des JWT lisibles (opaques).
Ça veut dire :

* ton API ne peut pas lire directement les claims dedans
* elle doit parfois appeler le provider (introspection endpoint) pour valider

JWT access tokens :

* l’API peut valider localement (signature + exp)
* plus rapide, plus autonome

En pratique :

* dépend du provider, des politiques, et de l’architecture

---

12. Scopes (vulgarisé, mais essentiel)

Un scope, c’est une permission demandée au provider.

Exemples :

* read:user
* email
* profile
* calendar.read

Le scope répond à :
“Qu’est-ce que ton application veut avoir le droit de faire chez le provider ?”

Pour un simple login, tu demandes souvent :

* profile
* email

Tu ne demandes pas “calendar.write” si tu n’en as pas besoin.

Bon réflexe sécurité :
Demander le minimum.

---

13. Bonnes pratiques de sécurité (niveau architecture)

* demander le minimum de scopes
* ne pas stocker de tokens en clair dans des endroits exposés
* préférer des durées raisonnables, rotation de refresh token si possible
* valider issuer, audience, expiration
* loguer les événements d’auth de manière sûre (pas de token dans les logs)
* prévoir la désactivation (si user perd accès au SSO, ton app doit suivre)

---

14. Erreurs fréquentes (pièges classiques)

Erreur 1 : croire que OAuth2 = authentification
OAuth2 seul n’est pas “login”. OIDC est la couche identité.

Erreur 2 : confondre ID token et access token

* ID token prouve l’identité
* access token sert à appeler une API

Erreur 3 : donner des rôles automatiquement sans contrôle
“Si email se termine par @entreprise.com alors ADMIN” est dangereux si mal géré.

Erreur 4 : demander trop de scopes
Tu augmentes la surface de risque.

Erreur 5 : oublier de valider audience/issuer
Tu risques d’accepter un token destiné à une autre application.

---

15. Résumé Module 8

* OAuth2 = autorisation ; OIDC = authentification + identité
* SSO entreprise = gros gain de sécurité et de gestion (MFA, politiques, audit)
* tokens : ID token (identité), access token (accès), refresh token (renouvellement)
* ton app reçoit l’identité mais garde la décision des droits internes
* flows modernes : authorization code (souvent avec PKCE)
* scopes = permissions demandées au provider, minimum nécessaire
* valider correctement les tokens (issuer, audience, exp)
* ne pas confondre login provider avec autorisation interne

---

Mini quiz

1. OAuth2 et OIDC : différence en une phrase chacun ?
2. ID token vs access token : à quoi sert chacun ?
3. Pourquoi ton application doit quand même gérer rôles/permissions même avec SSO ?

