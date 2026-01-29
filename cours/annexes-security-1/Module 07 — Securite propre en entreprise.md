# Module 7 — Sécurité "propre" en entreprise (détails + vulgarisation + organisation)

## Objectif du module

Passer d'une sécurité "qui marche" à une sécurité "propre, maintenable et scalable". En entreprise, le vrai défi n'est pas juste d'ajouter Spring Security, c'est de garder un code lisible, testable, évolutif, et cohérent quand l'application grandit.

---

## 1. Pourquoi "propre en entreprise" est différent

Dans un petit projet, tu peux tout mettre dans un seul fichier :

- SecurityConfig
- Users
- Auth
- exceptions

Ça fonctionne, mais ça devient rapidement ingérable.

En entreprise, tu as :

- plusieurs types d'utilisateurs
- plusieurs domaines (courses, payments, admin, etc.)
- des règles plus fines (permissions, propriétaire de données)
- des équipes différentes qui touchent au code
- des besoins de conformité (logs, audit, sécurité)

Donc il faut organiser la sécurité comme un vrai module, pas comme un patch.

---

## 2. Organisation des packages (structure recommandée)

L'idée : séparer clairement ce qui relève de :

- l'authentification (login, tokens, filtres)
- la récupération utilisateur (UserDetails, DB)
- l'autorisation (rôles/permissions, règles d'accès)
- la gestion des erreurs (401/403 en JSON)

### Structure typique (conceptuelle) :

```
security/
├── config/          # configuration principale
├── auth/            # login, refresh, génération token
├── jwt/             # validation token, filtre, utilitaires
├── user/            # UserDetailsService, mapping user entity -> userdetails
├── exception/       # handlers 401/403, réponses JSON
└── permissions/     # roles/authorities, mapping
```

**Pourquoi c'est bien :**

- tu sais où chercher
- tu évites le fichier "monstre"
- tu peux tester chaque bloc séparément

---

## 3. Séparer "auth" et "user" (concept important)

Dans beaucoup de projets, on mélange :

- gestion des utilisateurs (CRUD users, profil)
- authentification (login, tokens)

En réalité, ce sont deux sujets différents.

### Auth = prouver l'identité

- login
- refresh
- logout logique (selon modèle)
- génération et validation de tokens

### User = données métier de l'utilisateur

- profil
- préférences
- informations personnelles
- gestion admin des utilisateurs

**Si tu sépares, ton code devient clair :**

- auth module n'a besoin que du strict minimum user
- user module n'est pas pollué par la logique token

---

## 4. Gestion centralisée des erreurs (401/403 propres en API)

En entreprise, une API doit répondre avec des erreurs standardisées.

**Pourquoi ?**

- le front doit interpréter facilement
- les logs doivent être exploitables
- le support doit diagnostiquer vite

**Objectif :**

- 401 toujours cohérent (non authentifié)
- 403 toujours cohérent (pas autorisé)
- format JSON stable, avec champs utiles :
  - timestamp
  - status
  - error
  - message (sans trop en dire)
  - path
  - requestId (si tu en as un)

> **Règle importante :**
> Ne pas divulguer des détails sensibles.
> Exemple : ne pas dire "username existe mais mot de passe incorrect" → tu aides un attaquant à deviner des comptes.

---

## 5. CORS "propre" (pas juste "mettre *")

En entreprise, tu ne veux pas :

- autoriser n'importe quel origin en production
- ouvrir trop large des headers ou méthodes

**Tu veux une politique claire :**

- quels domaines front sont autorisés
- quelles méthodes
- quels headers (Authorization, Content-Type)
- est-ce que tu utilises cookies (credentials) ou token Bearer

**Règle simple :**

- si tu utilises Bearer token, tu n'as généralement pas besoin de cookies
- donc credentials = false dans beaucoup de cas (plus simple)

---

## 6. CSRF : décision claire selon modèle

Tu ne traites pas CSRF de la même façon selon :

### A) App web session/cookie

- CSRF très important
- stratégie classique : token CSRF

### B) API stateless token Bearer

- contexte différent
- souvent traité autrement (et pas avec la même approche)

**Ce qui compte en entreprise :**

- être capable de justifier ton choix
- ne pas appliquer des "recettes" sans comprendre

---

## 7. Sécuriser Swagger / OpenAPI (cas réel incontournable)

Swagger est très utile en dev, mais peut devenir une porte d'entrée si exposé publiquement.

**Stratégies réalistes :**

- désactiver Swagger en production
- ou restreindre Swagger à ADMIN
- ou restreindre par IP / réseau interne (selon infra)

**But :**
Éviter de donner une cartographie complète de ton API à tout le monde.

---

## 8. Éviter un SecurityConfig énorme : patterns de configuration

Dans les projets qui grandissent, SecurityConfig devient vite illisible car tu empiles :

- routes
- filtres
- règles
- exceptions
- CORS/CSRF
- sessions
- JWT
- OAuth2

### Approche propre :

- config principale minimaliste
- classes dédiées pour :
  - règles d'accès (routes)
  - composants JWT (filtre, provider)
  - handlers 401/403
  - CORS config

**But :**
Chaque fichier a une responsabilité claire.

---

## 9. Logging et audit (vulgarisé, mais important)

En entreprise, la sécurité sans logs, c'est aveugle.

Sans entrer dans des détails techniques :

- loguer les tentatives d'accès refusées (403)
- loguer certains événements auth (login success/fail)
- corréler avec un requestId
- éviter de loguer des secrets (tokens, passwords)

**Objectif :**

- diagnostiquer incidents
- répondre à des audits
- améliorer la sécurité

---

## 10. "Défense en profondeur" (mentalité entreprise)

Une règle entreprise très utile :
**Ne pas dépendre d'un seul niveau de protection.**

**Exemples :**

- protéger par URL + par méthode pour actions critiques
- valider côté métier même si endpoint est protégé
- limiter certains endpoints par réseau (en plus de roles)
- configurer des timeouts et expirations

**Idée :**
Si une couche est mal configurée, l'autre peut limiter les dégâts.

---

## 11. Résumé Module 7

- organiser la sécurité en modules clairs (auth, jwt, user, exception)
- séparer authentification et gestion utilisateur
- standardiser 401/403 en JSON
- configurer CORS proprement (pas "*" en prod)
- prendre une décision claire sur CSRF selon le modèle
- protéger Swagger
- éviter un fichier config énorme : responsabilités séparées
- logs et audit sans exposer de secrets
- défense en profondeur

---

## Mini quiz

1. Pourquoi séparer auth et user est une bonne pratique ?
2. Pourquoi Swagger doit être protégé en production ?
3. Qu'est-ce que "défense en profondeur" en une phrase ?

