# 13 - Guide Complet : Environnement Local avec VS Code + Docker Desktop

## 🎯 Objectif

Configurer et tester le projet en **local** avec :
- VS Code (pas Codespaces)
- Docker Desktop (Windows/Mac)
- Fichier `.env` pour les variables d'environnement

---

## 📋 Prérequis

### Logiciels à installer

| Logiciel | Téléchargement | Version minimale |
|----------|----------------|------------------|
| **Git** | https://git-scm.com | 2.40+ |
| **Docker Desktop** | https://docker.com/products/docker-desktop | 4.0+ |
| **VS Code** | https://code.visualstudio.com | 1.80+ |
| **Java 17** (optionnel) | https://adoptium.net | 17+ |

### Extensions VS Code recommandées

1. **Docker** - ms-azuretools.vscode-docker
2. **REST Client** - humao.rest-client
3. **Java Extension Pack** - vscjava.vscode-java-pack (optionnel)

---

# ÉTAPE 1 : CLONER LE PROJET

## 📍 Étape 1.1 : Ouvrir un terminal

- **Windows** : PowerShell ou Git Bash
- **Mac** : Terminal

---

## 📍 Étape 1.2 : Cloner le repository

```bash
# Aller dans votre dossier de projets
cd ~/projets
# ou sur Windows
cd C:\projets

# Cloner le repo
git clone https://github.com/hrhouma1/inskillflow-docs-formation-spring-boot-projet-e-contact-backend.git

# Entrer dans le dossier
cd inskillflow-docs-formation-spring-boot-projet-e-contact-backend
```

---

## 📍 Étape 1.3 : Ouvrir dans VS Code

```bash
code .
```

Ou :
1. Ouvrez VS Code
2. **File** → **Open Folder**
3. Sélectionnez le dossier du projet

---

# ÉTAPE 2 : CONFIGURER LE FICHIER .env

## 📍 Étape 2.1 : Créer le fichier .env

Dans le terminal VS Code (Ctrl + `) :

```bash
# Windows PowerShell
Copy-Item .env.example .env

# Mac/Linux
cp .env.example .env
```

---

## 📍 Étape 2.2 : Vérifier le contenu du fichier .env

Ouvrez le fichier `.env` dans VS Code :

```env
# ============================================
# Contact Form API - Variables d'environnement
# ============================================

# --- Profil Spring ---
SPRING_PROFILES_ACTIVE=prod

# --- Base de données PostgreSQL ---
DB_HOST=postgres
DB_PORT=5432
DB_NAME=contactdb
DB_USER=postgres
DB_PASSWORD=postgres

# --- Email (MailHog pour dev/test) ---
MAIL_HOST=mailhog
MAIL_PORT=1025
MAIL_USER=
MAIL_PASSWORD=
MAIL_AUTH=false
MAIL_STARTTLS=false

# --- Email admin (notifications) ---
ADMIN_EMAIL=admin@example.com

# --- JWT (Sécurité) ---
JWT_SECRET=dGhpcy1pcy1hLXZlcnktbG9uZy1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tZ2VuZXJhdGlvbi0yNTY=
JWT_EXPIRATION=86400000
```

✅ **Aucune modification nécessaire** pour le développement local !

---

## 📍 Étape 2.3 : Vérifier que .env est ignoré par Git

```bash
# Le fichier .env ne doit PAS apparaître
git status
```

Si `.env` apparaît, vérifiez que `.gitignore` contient :
```
.env
.env.local
```

---

# ÉTAPE 3 : DÉMARRER DOCKER DESKTOP

## 📍 Étape 3.1 : Lancer Docker Desktop

### Windows
1. Cherchez **Docker Desktop** dans le menu Démarrer
2. Cliquez pour lancer
3. Attendez que l'icône Docker (baleine) apparaisse dans la barre des tâches
4. Vérifiez que le statut est **Running**

### Mac
1. Ouvrez **Docker** depuis Applications
2. Attendez que l'icône Docker apparaisse dans la barre de menu
3. Vérifiez que le statut est **Running**

---

## 📍 Étape 3.2 : Vérifier que Docker fonctionne

Dans le terminal VS Code :

```bash
docker --version
```

**Résultat attendu :**
```
Docker version 24.0.7, build afdd53b
```

```bash
docker ps
```

**Résultat attendu :**
```
CONTAINER ID   IMAGE   COMMAND   CREATED   STATUS   PORTS   NAMES
```

(Liste vide = OK, Docker fonctionne)

---

## 📍 Étape 3.3 : Vérifier Docker Compose

```bash
docker compose version
```

**Résultat attendu :**
```
Docker Compose version v2.23.0
```

---

# ÉTAPE 4 : LANCER L'APPLICATION

## 📍 Étape 4.1 : Construire et démarrer les conteneurs

Dans le terminal VS Code :

```bash
docker compose up --build -d
```

**Explication des options :**
- `--build` : Reconstruit l'image avec le code actuel
- `-d` : Mode détaché (en arrière-plan)

---

## 📍 Étape 4.2 : Attendre la fin du build

Le build prend environ 1-2 minutes. Vous verrez :

```
[+] Building 45.2s (12/12) FINISHED
 => [api] FROM docker.io/library/eclipse-temurin:17-jre-alpine
 => [api build] RUN mvn clean package -DskipTests
 => exporting to image
 => => naming to docker.io/library/...-api

[+] Running 4/4
 ✔ Network ..._default      Created
 ✔ Container contact-db      Healthy
 ✔ Container contact-mailhog Started
 ✔ Container contact-api     Started
```

---

## 📍 Étape 4.3 : Vérifier que les conteneurs tournent

```bash
docker ps
```

**Résultat attendu :**
```
CONTAINER ID   IMAGE              STATUS                   PORTS                              NAMES
abc123         ...-api            Up 30 seconds            0.0.0.0:8080->8080/tcp             contact-api
def456         postgres:15        Up 45 seconds (healthy)  0.0.0.0:5432->5432/tcp             contact-db
ghi789         mailhog/mailhog    Up 45 seconds            0.0.0.0:1025->1025/tcp,            contact-mailhog
                                                           0.0.0.0:8025->8025/tcp
```

✅ Les 3 conteneurs doivent être **Up**

---

## 📍 Étape 4.4 : Vérifier les logs de l'API

```bash
docker logs contact-api -f
```

**Attendez de voir :**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
...
Started ContactApplication in 8.234 seconds
```

Appuyez sur **Ctrl+C** pour quitter les logs.

---

# ÉTAPE 5 : ACCÉDER AUX SERVICES

## 📍 Étape 5.1 : URLs locales

| Service | URL | Description |
|---------|-----|-------------|
| 🚀 **API** | http://localhost:8080 | API REST |
| 📘 **Swagger UI** | http://localhost:8080/swagger-ui.html | Documentation interactive |
| 📧 **MailHog** | http://localhost:8025 | Interface emails |
| 🗄️ **PostgreSQL** | localhost:5432 | Base de données |

---

## 📍 Étape 5.2 : Ouvrir Swagger UI

1. Ouvrez votre navigateur
2. Allez sur : **http://localhost:8080/swagger-ui.html**

Vous devriez voir l'interface Swagger avec :
- contact-controller
- lead-controller
- auth-controller

---

## 📍 Étape 5.3 : Ouvrir MailHog

1. Dans un nouvel onglet : **http://localhost:8025**
2. Vous devriez voir : **Inbox (0)**

---

# ÉTAPE 6 : CRÉER UN LEAD ET TESTER LES EMAILS

## 📍 Étape 6.1 : Dans Swagger UI

1. Cliquez sur **contact-controller**
2. Cliquez sur `POST /api/contact`
3. Cliquez sur **Try it out**

---

## 📍 Étape 6.2 : Entrer les données du lead

Collez ce JSON dans le body :

```json
{
  "fullName": "Jean Local",
  "company": "Local Company",
  "email": "jean@local.com",
  "phone": "514-555-9999",
  "requestType": "INFO",
  "message": "Test depuis mon environnement local avec Docker Desktop!"
}
```

---

## 📍 Étape 6.3 : Exécuter la requête

1. Cliquez sur **Execute**
2. Vérifiez la réponse : **200 OK**

```json
{
  "message": "Merci! Votre message a été envoyé. Nous vous répondrons bientôt."
}
```

---

## 📍 Étape 6.4 : Vérifier les emails dans MailHog

1. Allez sur **http://localhost:8025**
2. Rafraîchissez la page (F5)
3. Vous devriez voir **Inbox (2)**

### Email 1 : Notification admin
```
To: admin@example.com
Subject: Nouveau contact: Jean Local
```

### Email 2 : Confirmation visiteur
```
To: jean@local.com
Subject: Confirmation - Nous avons bien reçu votre message
```

---

# ÉTAPE 7 : S'AUTHENTIFIER ET TESTER L'ADMIN

## 📍 Étape 7.1 : Obtenir un token JWT

Dans Swagger UI :

1. Cliquez sur **auth-controller**
2. Cliquez sur `POST /api/auth/login`
3. **Try it out**
4. Body :

```json
{
  "email": "admin@example.com",
  "password": "admin123"
}
```

5. **Execute**
6. **Copiez le token** de la réponse

---

## 📍 Étape 7.2 : Configurer l'autorisation

1. Cliquez sur **Authorize** 🔓 (en haut à droite)
2. Tapez : `Bearer VOTRE_TOKEN_ICI`
3. **Authorize** → **Close**

---

## 📍 Étape 7.3 : Lister les leads

1. Cliquez sur **lead-controller**
2. `GET /api/admin/leads`
3. **Try it out**
4. Dans **pageable** :
```json
{
  "page": 0,
  "size": 10
}
```
5. **Execute**

Vous devriez voir votre lead "Jean Local" !

---

# ÉTAPE 8 : ACCÉDER À LA BASE DE DONNÉES

## 📍 Étape 8.1 : Se connecter à PostgreSQL

```bash
docker exec -it contact-db psql -U postgres -d contactdb
```

---

## 📍 Étape 8.2 : Voir les leads

```sql
SELECT id, full_name, email, status FROM leads;
```

**Résultat :**
```
 id |  full_name  |     email      | status 
----+-------------+----------------+--------
  1 | Jean Local  | jean@local.com | NEW
```

---

## 📍 Étape 8.3 : Voir les utilisateurs

```sql
SELECT id, email, role FROM users;
```

**Résultat :**
```
 id |        email        | role  
----+---------------------+-------
  1 | admin@example.com   | ADMIN
```

---

## 📍 Étape 8.4 : Quitter PostgreSQL

```sql
\q
```

---

# ÉTAPE 9 : ARRÊTER ET REDÉMARRER

## 📍 Étape 9.1 : Arrêter les conteneurs

```bash
docker compose down
```

---

## 📍 Étape 9.2 : Redémarrer (sans rebuild)

```bash
docker compose up -d
```

---

## 📍 Étape 9.3 : Redémarrer avec rebuild (après modif code)

```bash
docker compose up --build -d
```

---

## 📍 Étape 9.4 : Tout supprimer (reset complet)

```bash
# Arrêter et supprimer les volumes (données)
docker compose down -v

# Relancer proprement
docker compose up --build -d
```

⚠️ **Attention** : `-v` supprime les données de la base !

---

# ÉTAPE 10 : DÉPANNAGE

## 🔴 Problème : "port is already allocated"

**Cause** : Un autre service utilise le port 8080, 5432 ou 8025.

**Solution Windows :**
```powershell
# Trouver le processus
netstat -ano | findstr :8080

# Tuer le processus (remplacez PID)
taskkill /PID <PID> /F
```

**Solution Mac :**
```bash
# Trouver le processus
lsof -i :8080

# Tuer le processus
kill -9 <PID>
```

---

## 🔴 Problème : "Cannot connect to Docker daemon"

**Solution :**
1. Vérifiez que Docker Desktop est lancé
2. Redémarrez Docker Desktop
3. Attendez que l'icône Docker soit verte

---

## 🔴 Problème : Build échoue

**Solution :**
```bash
# Nettoyer et reconstruire
docker compose down -v
docker system prune -f
docker compose up --build -d
```

---

## 🔴 Problème : Emails n'apparaissent pas

**Vérifications :**
```bash
# Vérifier que MailHog tourne
docker ps | grep mailhog

# Vérifier les logs
docker logs contact-api | grep -i email

# Vérifier la config
docker exec contact-api env | grep MAIL
```

---

# RÉCAPITULATIF

## 📊 Architecture locale

```
┌─────────────────────────────────────────────────────────────┐
│                    VOTRE MACHINE LOCALE                      │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────┐                                           │
│  │   VS Code    │                                           │
│  │   (IDE)      │                                           │
│  └──────────────┘                                           │
│         │                                                    │
│         │ docker compose up                                  │
│         ▼                                                    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │              DOCKER DESKTOP                          │    │
│  ├─────────────────────────────────────────────────────┤    │
│  │                                                      │    │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐    │    │
│  │  │ contact-api│  │ contact-db │  │  mailhog   │    │    │
│  │  │   :8080    │  │   :5432    │  │:1025 :8025 │    │    │
│  │  │ Spring Boot│  │ PostgreSQL │  │   SMTP     │    │    │
│  │  └────────────┘  └────────────┘  └────────────┘    │    │
│  │                                                      │    │
│  └─────────────────────────────────────────────────────┘    │
│         │                                                    │
│         │ Accessible via localhost                          │
│         ▼                                                    │
│  ┌──────────────┐                                           │
│  │  Navigateur  │                                           │
│  │  - Swagger   │ http://localhost:8080/swagger-ui.html    │
│  │  - MailHog   │ http://localhost:8025                     │
│  └──────────────┘                                           │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 📋 Commandes essentielles

| Action | Commande |
|--------|----------|
| Démarrer | `docker compose up -d` |
| Démarrer + rebuild | `docker compose up --build -d` |
| Arrêter | `docker compose down` |
| Voir les logs | `docker logs contact-api -f` |
| Voir les conteneurs | `docker ps` |
| Accès PostgreSQL | `docker exec -it contact-db psql -U postgres -d contactdb` |
| Reset complet | `docker compose down -v && docker compose up --build -d` |

---

## 🌐 URLs locales

| Service | URL |
|---------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| API | http://localhost:8080/api/contact |
| MailHog | http://localhost:8025 |
| PostgreSQL | localhost:5432 |

---

## 🔐 Identifiants

| Service | User | Password |
|---------|------|----------|
| Admin API | admin@example.com | admin123 |
| PostgreSQL | postgres | postgres |

---

## ✅ Checklist finale

- [ ] Docker Desktop installé et running
- [ ] Projet cloné
- [ ] Fichier `.env` créé
- [ ] `docker compose up --build -d` exécuté
- [ ] 3 conteneurs running
- [ ] Swagger UI accessible sur localhost:8080
- [ ] MailHog accessible sur localhost:8025
- [ ] Lead créé avec succès
- [ ] Emails visibles dans MailHog
- [ ] Authentification JWT fonctionnelle
- [ ] Accès à PostgreSQL fonctionnel

---

## 🎉 Félicitations !

Votre environnement de développement local est prêt !

