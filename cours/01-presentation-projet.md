# Chapitre 1.1 - Presentation du projet

## Objectifs du chapitre

- Comprendre le contexte et les besoins du projet
- Identifier les fonctionnalites principales
- Visualiser l'architecture globale

---

## 1. Contexte

### Problematique

Une entreprise souhaite collecter les demandes de contact de ses visiteurs via un formulaire web. Elle a besoin de:

1. Un formulaire accessible publiquement
2. Une notification par email a chaque nouvelle demande
3. Un espace d'administration securise pour gerer les leads
4. Des statistiques sur les demandes recues

### Solution

Developper une **API REST** avec **Spring Boot** qui:

- Expose un endpoint public pour soumettre le formulaire
- Envoie des emails automatiquement
- Securise l'acces admin avec JWT
- Stocke les donnees dans une base PostgreSQL

---

## 2. Fonctionnalites

### Partie publique

| Fonctionnalite | Endpoint | Description |
|----------------|----------|-------------|
| Soumettre formulaire | POST /api/contact | Cree un nouveau lead |

### Partie administration

| Fonctionnalite | Endpoint | Description |
|----------------|----------|-------------|
| Connexion | POST /api/auth/login | Obtenir un token JWT |
| Liste des leads | GET /api/admin/leads | Pagination et filtres |
| Modifier statut | PUT /api/admin/leads/{id}/status | NEW, CONTACTED, CONVERTED, LOST |
| Supprimer | DELETE /api/admin/leads/{id} | Suppression d'un lead |
| Statistiques | GET /api/admin/leads/stats | Compteurs par statut |

---

## 3. Architecture globale

```
                    +------------------+
                    |    Frontend      |
                    | (HTML/React/etc) |
                    +--------+---------+
                             |
                             | HTTP/HTTPS
                             v
                    +------------------+
                    |    API REST      |
                    |  (Spring Boot)   |
                    +--------+---------+
                             |
              +--------------+--------------+
              |              |              |
              v              v              v
        +---------+   +-----------+   +----------+
        |   JWT   |   | PostgreSQL|   |   SMTP   |
        | Service |   |    DB     |   |  Server  |
        +---------+   +-----------+   +----------+
```

---

## 4. Technologies utilisees

| Categorie | Technologie | Version |
|-----------|-------------|---------|
| Framework | Spring Boot | 3.2.0 |
| Langage | Java | 17 |
| Build | Maven | 3.x |
| Base de donnees (prod) | PostgreSQL | 15 |
| Base de donnees (dev) | H2 | Embedded |
| Securite | Spring Security + JWT | - |
| Email | Spring Mail | - |
| Documentation | OpenAPI / Swagger | 3.0 |
| Conteneurisation | Docker | - |

---

## 5. Structure du projet

```
projet-e-contact-backend/
|
|-- src/main/java/com/example/contact/
|   |-- ContactApplication.java      # Point d'entree
|   |-- config/                      # Configuration
|   |-- controller/                  # Endpoints REST
|   |-- dto/                         # Objets de transfert
|   |-- exception/                   # Gestion des erreurs
|   |-- model/                       # Entites JPA
|   |-- repository/                  # Acces donnees
|   |-- security/                    # JWT
|   |-- service/                     # Logique metier
|
|-- src/main/resources/
|   |-- application.yml              # Configuration
|
|-- docker-compose.yml               # Orchestration Docker
|-- Dockerfile                       # Image Docker
|-- pom.xml                          # Dependances Maven
```

---

## 6. Flux de donnees

### Soumission d'un formulaire

```
1. Visiteur remplit le formulaire
         |
         v
2. POST /api/contact (JSON)
         |
         v
3. ContactController recoit la requete
         |
         v
4. LeadService traite la logique
         |
         +---> Sauvegarde en base (LeadRepository)
         |
         +---> Envoi email admin (EmailService)
         |
         +---> Envoi email confirmation visiteur
         |
         v
5. Reponse JSON avec ID du lead
```

### Authentification admin

```
1. Admin envoie email/password
         |
         v
2. POST /api/auth/login
         |
         v
3. Verification des credentials
         |
         v
4. Generation token JWT
         |
         v
5. Reponse avec token
         |
         v
6. Admin utilise le token pour les requetes suivantes
   Authorization: Bearer <token>
```

---

## 7. Points cles a retenir

1. **Separation des responsabilites**: Chaque couche a un role precis
2. **API Stateless**: Pas de session serveur, authentification par token
3. **Securite**: Endpoints admin proteges, mots de passe hashes
4. **Flexibilite**: Configuration par profils (dev/prod)
5. **Documentation**: API auto-documentee avec Swagger

---

## QUIZ 1.1 - Presentation du projet

**1. Quel est le role principal de cette API?**
   - a) Gerer un site e-commerce
   - b) Collecter et gerer des demandes de contact
   - c) Envoyer des newsletters
   - d) Gerer un blog

**2. Quel endpoint permet de soumettre un formulaire de contact?**
   - a) GET /api/contact
   - b) POST /api/leads
   - c) POST /api/contact
   - d) PUT /api/contact

**3. Quelle technologie est utilisee pour l'authentification?**
   - a) Sessions
   - b) Cookies
   - c) JWT
   - d) OAuth2

**4. VRAI ou FAUX: Les endpoints admin sont accessibles sans authentification.**

**5. Quelle base de donnees est utilisee en production?**
   - a) H2
   - b) MySQL
   - c) PostgreSQL
   - d) MongoDB

**6. Combien de couches principales composent l'architecture?**
   - a) 2
   - b) 3
   - c) 4
   - d) 5

**7. Que se passe-t-il apres la soumission d'un formulaire? (plusieurs reponses)**
   - a) Le lead est sauvegarde en base
   - b) Un email est envoye a l'admin
   - c) Un email de confirmation est envoye au visiteur
   - d) Toutes les reponses ci-dessus

**8. Completez: L'API est _______ car elle n'utilise pas de sessions serveur.**

**9. Quel outil permet de documenter automatiquement l'API?**
   - a) Javadoc
   - b) Swagger/OpenAPI
   - c) Postman
   - d) JUnit

**10. Dans quel fichier sont definies les dependances Maven?**
   - a) build.gradle
   - b) package.json
   - c) pom.xml
   - d) dependencies.yml

---

### REPONSES QUIZ 1.1

1. b) Collecter et gerer des demandes de contact
2. c) POST /api/contact
3. c) JWT
4. FAUX
5. c) PostgreSQL
6. c) 4 (Controller, Service, Repository, Model)
7. d) Toutes les reponses ci-dessus
8. stateless
9. b) Swagger/OpenAPI
10. c) pom.xml

