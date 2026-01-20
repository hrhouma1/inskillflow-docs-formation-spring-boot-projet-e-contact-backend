# 22 - Guide : Accéder aux Pages Web (Swagger, MailHog, API, Frontend)

## Table des matières

1. [Vue d'ensemble des URLs](#vue-densemble-des-urls)
2. [Accéder à Swagger UI](#accéder-à-swagger-ui)
3. [Accéder à MailHog](#accéder-à-mailhog)
4. [Accéder à l'API directement](#accéder-à-lapi-directement)
5. [Accéder au Frontend](#accéder-au-frontend)
6. [Accéder à la console H2 (mode dev)](#accéder-à-la-console-h2-mode-dev)
7. [URLs sur Codespaces](#urls-sur-codespaces)

---

# VUE D'ENSEMBLE DES URLS

## Tableau récapitulatif (Docker Desktop local)

| Service | URL | Description |
|---------|-----|-------------|
| **Swagger UI** | http://localhost:8080/swagger-ui.html | Documentation interactive de l'API |
| **OpenAPI JSON** | http://localhost:8080/v3/api-docs | Spécification OpenAPI en JSON |
| **MailHog** | http://localhost:8025 | Interface emails capturés |
| **API Contact** | http://localhost:8080/api/contact | Endpoint public |
| **API Auth** | http://localhost:8080/api/auth/login | Authentification |
| **API Admin** | http://localhost:8080/api/admin/leads | Gestion des leads (protégé) |
| **Frontend** | http://localhost:80 ou http://localhost:3000 | Formulaire de contact |
| **Console H2** | http://localhost:8080/h2-console | Base de données dev (si activée) |

---

# ACCÉDER À SWAGGER UI

## Étape 1 : Ouvrir Swagger UI

1. Ouvrez votre navigateur (Chrome, Firefox, Edge)
2. Tapez dans la barre d'adresse :

```
http://localhost:8080/swagger-ui.html
```

3. Appuyez sur Entrée

## Étape 2 : Interface Swagger UI

```
+------------------------------------------------------------------+
|  Contact Form API                                                 |
|  API REST pour formulaire de contact et gestion de leads          |
+------------------------------------------------------------------+
|                                                                   |
|  [Authorize] 🔓                                                   |
|                                                                   |
+------------------------------------------------------------------+
|                                                                   |
|  auth-controller                                            [v]   |
|  +------------------------------------------------------------+  |
|  | POST /api/auth/register  Register a new admin              |  |
|  | POST /api/auth/login     Authenticate and get JWT token    |  |
|  +------------------------------------------------------------+  |
|                                                                   |
|  contact-controller                                         [v]   |
|  +------------------------------------------------------------+  |
|  | POST /api/contact        Submit a contact form             |  |
|  +------------------------------------------------------------+  |
|                                                                   |
|  admin-controller                                           [v]   |
|  +------------------------------------------------------------+  |
|  | GET  /api/admin/leads           Get all leads (paginated)  |  |
|  | GET  /api/admin/leads/{id}      Get lead by ID             |  |
|  | PUT  /api/admin/leads/{id}/status  Update lead status      |  |
|  | DELETE /api/admin/leads/{id}    Delete a lead              |  |
|  | GET  /api/admin/stats           Get statistics             |  |
|  +------------------------------------------------------------+  |
|                                                                   |
+------------------------------------------------------------------+
```

## Étape 3 : Tester un endpoint public (sans authentification)

### 3.1 Créer un contact

1. Cliquez sur **contact-controller** pour le déplier
2. Cliquez sur **POST /api/contact**
3. Cliquez sur **Try it out**
4. Dans le champ **Request body**, collez :

```json
{
  "fullName": "Jean Dupont",
  "company": "Mon Entreprise",
  "email": "jean.dupont@gmail.com",
  "phone": "514-555-1234",
  "requestType": "INFO",
  "message": "Ceci est un test depuis Swagger UI"
}
```

5. Cliquez sur **Execute**

### 3.2 Résultat attendu

```
Code: 200

Response body:
{
  "id": 1,
  "fullName": "Jean Dupont",
  "company": "Mon Entreprise",
  "email": "jean.dupont@gmail.com",
  "phone": "514-555-1234",
  "requestType": "INFO",
  "message": "Ceci est un test depuis Swagger UI",
  "status": "NEW",
  "createdAt": "2026-01-20T15:30:00",
  "updatedAt": "2026-01-20T15:30:00"
}
```

## Étape 4 : S'authentifier pour les endpoints protégés

### 4.1 Obtenir un token JWT

1. Cliquez sur **auth-controller**
2. Cliquez sur **POST /api/auth/login**
3. Cliquez sur **Try it out**
4. Dans le champ **Request body**, collez :

```json
{
  "email": "admin@example.com",
  "password": "admin123"
}
```

5. Cliquez sur **Execute**

### 4.2 Copier le token

Résultat :
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTcwNTc2MDYwMCwiZXhwIjoxNzA1ODQ3MDAwfQ.abc123def456..."
}
```

**Copiez le token** (la longue chaîne de caractères)

### 4.3 Autoriser Swagger UI

1. Cliquez sur le bouton **Authorize** (en haut à droite)
2. Dans le champ **Value**, tapez :

```
Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTcwNTc2MDYwMCwiZXhwIjoxNzA1ODQ3MDAwfQ.abc123def456...
```

> **Important** : N'oubliez pas le mot `Bearer ` (avec un espace) avant le token !

3. Cliquez sur **Authorize**
4. Cliquez sur **Close**

### 4.4 Tester un endpoint protégé

1. Cliquez sur **admin-controller**
2. Cliquez sur **GET /api/admin/leads**
3. Cliquez sur **Try it out**
4. Modifiez les paramètres si nécessaire :
   - `page`: 0
   - `size`: 10
   - `sort`: `createdAt,desc`

5. Cliquez sur **Execute**

### 4.5 Résultat attendu

```json
{
  "content": [
    {
      "id": 1,
      "fullName": "Jean Dupont",
      "email": "jean.dupont@gmail.com",
      "status": "NEW",
      ...
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

## Étape 5 : Autres endpoints à tester

### Voir les statistiques

1. **GET /api/admin/stats**
2. Execute

Résultat :
```json
{
  "totalLeads": 5,
  "newLeads": 3,
  "contactedLeads": 1,
  "convertedLeads": 1,
  "conversionRate": 20.0
}
```

### Modifier le statut d'un lead

1. **PUT /api/admin/leads/{id}/status**
2. `id`: 1
3. `status`: CONTACTED
4. Execute

### Supprimer un lead

1. **DELETE /api/admin/leads/{id}**
2. `id`: 1
3. Execute

---

# ACCÉDER À MAILHOG

## Étape 1 : Ouvrir MailHog

1. Ouvrez votre navigateur
2. Tapez dans la barre d'adresse :

```
http://localhost:8025
```

3. Appuyez sur Entrée

## Étape 2 : Interface MailHog

```
+------------------------------------------------------------------+
|  MailHog                                                          |
+------------------------------------------------------------------+
|                                                                   |
|  Inbox (2)                                                        |
|                                                                   |
|  +------------------------------------------------------------+  |
|  | From              | To                | Subject              | |
|  +------------------------------------------------------------+  |
|  | noreply@example   | admin@example     | Nouveau contact:     | |
|  |                   |                   | Jean Dupont          | |
|  +------------------------------------------------------------+  |
|  | noreply@example   | jean.dupont@      | Confirmation - Nous  | |
|  |                   | gmail.com         | avons bien reçu...   | |
|  +------------------------------------------------------------+  |
|                                                                   |
+------------------------------------------------------------------+
```

## Étape 3 : Voir un email

1. Cliquez sur un email dans la liste
2. L'email s'affiche :

```
+------------------------------------------------------------------+
|  Email Details                                                    |
+------------------------------------------------------------------+
|                                                                   |
|  From: noreply@example.com                                        |
|  To: admin@example.com                                            |
|  Subject: Nouveau contact: Jean Dupont                            |
|  Date: 2026-01-20 15:30:00                                        |
|                                                                   |
|  ---------------------------------------------------------------- |
|                                                                   |
|  Nouveau contact reçu!                                            |
|                                                                   |
|  Nom: Jean Dupont                                                 |
|  Entreprise: Mon Entreprise                                       |
|  Email: jean.dupont@gmail.com                                     |
|  Téléphone: 514-555-1234                                          |
|  Type: INFO                                                       |
|                                                                   |
|  Message:                                                         |
|  Ceci est un test depuis Swagger UI                               |
|                                                                   |
+------------------------------------------------------------------+
```

## Étape 4 : Actions dans MailHog

| Action | Comment |
|--------|---------|
| Voir un email | Cliquez sur l'email |
| Supprimer un email | Cliquez sur l'icône poubelle |
| Supprimer tous | Menu > Delete all messages |
| Rafraîchir | La page se rafraîchit automatiquement |

## Étape 5 : API MailHog

MailHog expose aussi une API :

| URL | Description |
|-----|-------------|
| http://localhost:8025/api/v2/messages | Liste tous les emails (JSON) |
| http://localhost:8025/api/v1/messages | Supprimer tous les emails (DELETE) |

### Exemple avec curl

```powershell
# Voir tous les emails
curl http://localhost:8025/api/v2/messages

# Supprimer tous les emails
curl -X DELETE http://localhost:8025/api/v1/messages
```

---

# ACCÉDER À L'API DIRECTEMENT

## Avec le navigateur (GET uniquement)

```
http://localhost:8080/api/contact
```

> Erreur 405 : Méthode GET non autorisée (normal, c'est un POST)

## Avec curl (PowerShell)

### Créer un contact

```powershell
curl -X POST http://localhost:8080/api/contact `
  -H "Content-Type: application/json" `
  -d '{\"fullName\":\"Test\",\"email\":\"test@test.com\",\"requestType\":\"INFO\",\"message\":\"Test\"}'
```

### Se connecter

```powershell
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"email\":\"admin@example.com\",\"password\":\"admin123\"}'
```

### Voir les leads (avec token)

```powershell
curl http://localhost:8080/api/admin/leads `
  -H "Authorization: Bearer VOTRE_TOKEN_ICI"
```

## Avec Postman

1. Téléchargez Postman : https://www.postman.com/downloads/
2. Créez une nouvelle requête
3. Méthode : POST
4. URL : http://localhost:8080/api/contact
5. Body > raw > JSON :

```json
{
  "fullName": "Test Postman",
  "email": "test@postman.com",
  "requestType": "INFO",
  "message": "Test depuis Postman"
}
```

6. Cliquez sur **Send**

---

# ACCÉDER AU FRONTEND

## Option 1 : Docker Compose Full (port 80)

Si vous utilisez `docker-compose.full.yml` :

```
http://localhost
```

ou

```
http://localhost:80
```

## Option 2 : Python HTTP Server (port 3000)

Si vous servez le frontend manuellement :

```powershell
cd frontend
python -m http.server 3000
```

Puis ouvrez :

```
http://localhost:3000
```

## Option 3 : Ouvrir directement le fichier

1. Allez dans le dossier `frontend/`
2. Double-cliquez sur `index.html`
3. Le fichier s'ouvre dans votre navigateur

> Note : Avec cette méthode, l'URL sera `file:///C:/...` au lieu de `http://localhost`

## Option 4 : VS Code Live Server

1. Installez l'extension **Live Server** dans VS Code
2. Clic droit sur `frontend/index.html`
3. Cliquez sur **Open with Live Server**
4. Le navigateur s'ouvre automatiquement sur :

```
http://127.0.0.1:5500/frontend/index.html
```

---

# ACCÉDER À LA CONSOLE H2 (MODE DEV)

> Note : H2 est disponible uniquement en mode développement (profil `dev`)

## Étape 1 : Vérifier le profil actif

La console H2 n'est disponible que si `SPRING_PROFILES_ACTIVE=dev`

## Étape 2 : Ouvrir la console H2

```
http://localhost:8080/h2-console
```

## Étape 3 : Configuration de connexion

| Champ | Valeur |
|-------|--------|
| Driver Class | org.h2.Driver |
| JDBC URL | jdbc:h2:mem:contactdb |
| User Name | sa |
| Password | (laisser vide) |

## Étape 4 : Cliquez sur Connect

Vous pouvez maintenant exécuter des requêtes SQL directement dans le navigateur.

---

# URLS SUR CODESPACES

## Différence avec Docker Desktop local

Sur Codespaces, les URLs sont différentes car les ports sont exposés via un proxy GitHub.

## Comment trouver les URLs

### Méthode 1 : Onglet Ports

1. Dans VS Code (Codespaces), cliquez sur l'onglet **PORTS** (en bas)
2. Vous voyez la liste des ports exposés :

```
+------------------------------------------------------------------+
|  PORTS                                                            |
+------------------------------------------------------------------+
|  Port    | Local Address        | Visibility | Forwarded Address  |
+------------------------------------------------------------------+
|  8080    | localhost:8080       | Public     | https://xxx-8080...|
|  8025    | localhost:8025       | Public     | https://xxx-8025...|
|  5432    | localhost:5432       | Private    | -                  |
+------------------------------------------------------------------+
```

3. Cliquez sur l'icône globe (🌐) pour ouvrir dans le navigateur

### Méthode 2 : Construire l'URL

Format : `https://CODESPACE_NAME-PORT.app.github.dev`

Exemple :
- Swagger : `https://mon-codespace-8080.app.github.dev/swagger-ui.html`
- MailHog : `https://mon-codespace-8025.app.github.dev`

## Tableau des URLs Codespaces

| Service | URL Codespaces |
|---------|----------------|
| Swagger UI | `https://CODESPACE-8080.app.github.dev/swagger-ui.html` |
| MailHog | `https://CODESPACE-8025.app.github.dev` |
| API | `https://CODESPACE-8080.app.github.dev/api/contact` |
| Frontend | `https://CODESPACE-3000.app.github.dev` (si Python) |
| Frontend | `https://CODESPACE-80.app.github.dev` (si Docker full) |

## Rendre un port public

Par défaut, les ports peuvent être privés. Pour les rendre publics :

1. Clic droit sur le port dans l'onglet **PORTS**
2. Cliquez sur **Port Visibility**
3. Sélectionnez **Public**

---

# RÉSUMÉ DES URLS

## Docker Desktop (Local)

| Service | URL |
|---------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| MailHog | http://localhost:8025 |
| API | http://localhost:8080/api/contact |
| Frontend (Docker full) | http://localhost |
| Frontend (Python) | http://localhost:3000 |

## GitHub Codespaces

| Service | URL |
|---------|-----|
| Swagger UI | https://CODESPACE-8080.app.github.dev/swagger-ui.html |
| MailHog | https://CODESPACE-8025.app.github.dev |
| API | https://CODESPACE-8080.app.github.dev/api/contact |
| Frontend | https://CODESPACE-80.app.github.dev |

---

# AIDE-MÉMOIRE VISUEL

```
+------------------------------------------------------------------+
|                    ACCÈS AUX SERVICES WEB                         |
+------------------------------------------------------------------+
|                                                                   |
|  SWAGGER UI (Documentation API interactive)                       |
|  http://localhost:8080/swagger-ui.html                            |
|  -> Tester les endpoints                                          |
|  -> S'authentifier avec JWT                                       |
|                                                                   |
|  ---------------------------------------------------------------- |
|                                                                   |
|  MAILHOG (Emails capturés)                                        |
|  http://localhost:8025                                            |
|  -> Voir les emails envoyés                                       |
|  -> Vérifier le contenu                                           |
|                                                                   |
|  ---------------------------------------------------------------- |
|                                                                   |
|  API DIRECTE                                                      |
|  http://localhost:8080/api/contact (POST)                         |
|  http://localhost:8080/api/auth/login (POST)                      |
|  http://localhost:8080/api/admin/leads (GET, avec JWT)            |
|                                                                   |
|  ---------------------------------------------------------------- |
|                                                                   |
|  FRONTEND                                                         |
|  http://localhost (Docker full)                                   |
|  http://localhost:3000 (Python server)                            |
|  file:///C:/.../frontend/index.html (direct)                      |
|                                                                   |
+------------------------------------------------------------------+
```

---

# ANNEXE : EXEMPLE CONCRET D'URLS CODESPACES

## Format d'URL Codespaces

Le format est : `https://NOM-CODESPACE-PORT.app.github.dev`

Exemple réel de nom de Codespace :
```
refactored-space-funicular-7vr6459xgwprhr75j
```

## URLs pour ce Codespace

| Service | URL complète |
|---------|--------------|
| **Swagger UI** | https://refactored-space-funicular-7vr6459xgwprhr75j-8080.app.github.dev/swagger-ui.html |
| **MailHog** | https://refactored-space-funicular-7vr6459xgwprhr75j-8025.app.github.dev |
| **API Contact** | https://refactored-space-funicular-7vr6459xgwprhr75j-8080.app.github.dev/api/contact |
| **API Auth** | https://refactored-space-funicular-7vr6459xgwprhr75j-8080.app.github.dev/api/auth/login |
| **API Admin** | https://refactored-space-funicular-7vr6459xgwprhr75j-8080.app.github.dev/api/admin/leads |
| **Frontend (port 80)** | https://refactored-space-funicular-7vr6459xgwprhr75j-80.app.github.dev |
| **Frontend (port 3000)** | https://refactored-space-funicular-7vr6459xgwprhr75j-3000.app.github.dev |
| **OpenAPI JSON** | https://refactored-space-funicular-7vr6459xgwprhr75j-8080.app.github.dev/v3/api-docs |

## Comment trouver votre nom de Codespace

### Méthode 1 : Dans la barre de titre VS Code

Le nom apparaît dans la barre de titre :
```
Codespaces: refactored-space-funicular-7vr6459xgwprhr75j
```

### Méthode 2 : Dans le terminal

```bash
echo $CODESPACE_NAME
```

Résultat :
```
refactored-space-funicular-7vr6459xgwprhr75j
```

### Méthode 3 : Dans l'onglet PORTS

1. Cliquez sur l'onglet **PORTS** en bas de VS Code
2. La colonne **Forwarded Address** montre l'URL complète

```
+--------------------------------------------------------------------------+
|  PORTS                                                                    |
+--------------------------------------------------------------------------+
|  Port | Local Address    | Forwarded Address                             |
+--------------------------------------------------------------------------+
|  8080 | localhost:8080   | refactored-space-funicular-7vr6459xgwprhr75j  |
|       |                  | -8080.app.github.dev                          |
+--------------------------------------------------------------------------+
|  8025 | localhost:8025   | refactored-space-funicular-7vr6459xgwprhr75j  |
|       |                  | -8025.app.github.dev                          |
+--------------------------------------------------------------------------+
```

## Construire l'URL manuellement

### Formule

```
https://{CODESPACE_NAME}-{PORT}.app.github.dev{PATH}
```

### Exemples

| Composant | Valeur |
|-----------|--------|
| CODESPACE_NAME | `refactored-space-funicular-7vr6459xgwprhr75j` |
| PORT | `8080` |
| PATH | `/swagger-ui.html` |

**URL finale :**
```
https://refactored-space-funicular-7vr6459xgwprhr75j-8080.app.github.dev/swagger-ui.html
```

## Script pour afficher toutes les URLs

Créez un script `show-urls.sh` :

```bash
#!/bin/bash

echo "=========================================="
echo "URLs de votre Codespace"
echo "=========================================="
echo ""
echo "Swagger UI:"
echo "https://${CODESPACE_NAME}-8080.app.github.dev/swagger-ui.html"
echo ""
echo "MailHog:"
echo "https://${CODESPACE_NAME}-8025.app.github.dev"
echo ""
echo "API Contact:"
echo "https://${CODESPACE_NAME}-8080.app.github.dev/api/contact"
echo ""
echo "Frontend (port 80):"
echo "https://${CODESPACE_NAME}-80.app.github.dev"
echo ""
echo "Frontend (port 3000):"
echo "https://${CODESPACE_NAME}-3000.app.github.dev"
echo ""
echo "=========================================="
```

Exécutez :
```bash
chmod +x show-urls.sh
./show-urls.sh
```

## Comparaison Local vs Codespaces

| Service | URL Locale | URL Codespaces |
|---------|------------|----------------|
| Swagger | http://localhost:8080/swagger-ui.html | https://CODESPACE-8080.app.github.dev/swagger-ui.html |
| MailHog | http://localhost:8025 | https://CODESPACE-8025.app.github.dev |
| API | http://localhost:8080/api/contact | https://CODESPACE-8080.app.github.dev/api/contact |

> **Note** : Sur Codespaces, c'est `https://` (sécurisé), pas `http://`

---

# CHECKLIST

- [ ] Docker Desktop lancé (icône verte)
- [ ] Containers en cours (`docker ps`)
- [ ] Swagger UI accessible : http://localhost:8080/swagger-ui.html
- [ ] MailHog accessible : http://localhost:8025
- [ ] Test création contact dans Swagger
- [ ] Email reçu dans MailHog
- [ ] Authentification JWT fonctionnelle
- [ ] Endpoints admin accessibles avec JWT

## Checklist Codespaces

- [ ] Ports 8080 et 8025 visibles dans l'onglet PORTS
- [ ] Ports rendus publics (clic droit > Port Visibility > Public)
- [ ] URL Swagger fonctionne : https://CODESPACE-8080.app.github.dev/swagger-ui.html
- [ ] URL MailHog fonctionne : https://CODESPACE-8025.app.github.dev

