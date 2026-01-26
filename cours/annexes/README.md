## Introduction

Spring Security, c’est **le “vigile” de ton application Spring Boot**.

* Il **protège tes pages et tes API** (personne n’accède sans autorisation).
* Il gère **qui est connecté** (authentification : login/mot de passe, token, etc.).
* Il décide **ce que chaque utilisateur a le droit de faire** (autorisation : rôles ADMIN/USER, permissions).
* Il bloque des attaques courantes (ex: **CSRF**, accès non autorisé, etc.).


## Structure du cours

### Module 0 — Mise en contexte

1. Pourquoi sécuriser une API / un site web
2. Authentification vs Autorisation (avec exemples très simples)
3. Session vs Token (idée générale)

### Module 1 — Démarrage rapide

1. Créer un projet Spring Boot (Web + Security)
2. Comprendre le comportement par défaut (login auto, blocage des routes)
3. Lire les logs et comprendre “401” et “403”

### Module 2 — Les bases de la configuration

1. `SecurityFilterChain` (à quoi ça sert, en pratique)
2. Autoriser certaines routes (public) et protéger d’autres (privé)
3. Pages de login / logout (si app web) ou routes API (si REST)

### Module 3 — Utilisateurs et mots de passe

1. Utilisateur “en mémoire” (pour apprendre)
2. `UserDetailsService` (principe)
3. `PasswordEncoder` (BCrypt : pourquoi on ne stocke jamais un mot de passe en clair)

### Module 4 — Rôles et permissions

1. Rôles (`ROLE_USER`, `ROLE_ADMIN`)
2. Protéger des routes par rôle
3. Sécurité au niveau des méthodes (`@PreAuthorize`)

### Module 5 — Sécuriser une API REST (cas le plus fréquent)

1. Comprendre Basic Auth (simple mais limité)
2. API stateless (sans session)
3. Gestion des erreurs JSON (réponses propres 401/403)

### Module 6 — JWT (Token) de A à Z

1. C’est quoi un JWT (simple)
2. Login → génération d’un token
3. Ajouter un filtre pour lire le token
4. Expiration, refresh token (concept)
5. Bonnes pratiques (ne pas mettre de données sensibles)

### Module 7 — Sécurité “propre” en entreprise

1. Organisation des packages (config / security / auth / user)
2. Gestion centralisée des exceptions
3. CORS (front React/Angular + API)
4. Sécuriser Swagger/OpenAPI

### Module 8 — OAuth2 (login Google / GitHub) + OpenID Connect

1. À quoi ça sert et quand l’utiliser
2. Mise en place simple (login via provider)
3. Récupérer l’utilisateur connecté
4. Différence OAuth2 vs JWT “maison”

### Module 9 — Tests & validation

1. Tests unitaires de règles de sécurité
2. Tests d’intégration avec `MockMvc`
3. Vérifier qu’un USER ne peut pas faire une action ADMIN

### Module 10 — Mini-projet final (super utile)

**Projet : API “Gestion de cours”**

* Public : consulter la liste des cours
* USER : s’inscrire à un cours
* ADMIN : créer/modifier/supprimer un cours
* Auth : JWT
* Bonus : OAuth2 login

