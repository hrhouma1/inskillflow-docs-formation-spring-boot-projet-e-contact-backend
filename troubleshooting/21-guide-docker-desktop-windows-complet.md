# 21 - Guide Ultra Détaillé : Docker Desktop sur Windows

## Table des matières

1. [Installation de Docker Desktop](#partie-1--installation-de-docker-desktop)
2. [Interface Docker Desktop](#partie-2--interface-docker-desktop)
3. [Terminal et Commandes Linux](#partie-3--terminal-et-commandes-linux)
4. [Lancer le Projet](#partie-4--lancer-le-projet)
5. [Gérer les Containers](#partie-5--gérer-les-containers)
6. [Gérer les Images](#partie-6--gérer-les-images)
7. [Accéder à la Base de Données](#partie-7--accéder-à-la-base-de-données)
8. [Gérer les Volumes](#partie-8--gérer-les-volumes)
9. [Voir les Logs](#partie-9--voir-les-logs)
10. [Résoudre les Problèmes](#partie-10--résoudre-les-problèmes)

---

# PARTIE 1 : INSTALLATION DE DOCKER DESKTOP

## Étape 1.1 : Télécharger Docker Desktop

1. Ouvrez votre navigateur
2. Allez sur : https://www.docker.com/products/docker-desktop/
3. Cliquez sur **Download for Windows**
4. Le fichier `Docker Desktop Installer.exe` se télécharge

## Étape 1.2 : Installer Docker Desktop

1. Double-cliquez sur `Docker Desktop Installer.exe`
2. Cochez **Use WSL 2 instead of Hyper-V** (recommandé)
3. Cochez **Add shortcut to desktop**
4. Cliquez sur **Ok**
5. Attendez l'installation (5-10 minutes)
6. Cliquez sur **Close and restart**

## Étape 1.3 : Premier lancement

1. Après le redémarrage, Docker Desktop se lance automatiquement
2. Acceptez les conditions d'utilisation
3. Vous pouvez **Skip** la création de compte (optionnel)
4. Attendez que Docker démarre (icône verte dans la barre des tâches)

## Étape 1.4 : Vérifier l'installation

Ouvrez PowerShell (clic droit sur Démarrer > Terminal) et tapez :

```powershell
docker --version
```

Résultat attendu :
```
Docker version 24.0.7, build afdd53b
```

```powershell
docker compose version
```

Résultat attendu :
```
Docker Compose version v2.23.3-desktop.2
```

---

# PARTIE 2 : INTERFACE DOCKER DESKTOP

## Étape 2.1 : Ouvrir Docker Desktop

### Option A : Depuis le bureau
- Double-cliquez sur l'icône **Docker Desktop** sur le bureau

### Option B : Depuis la barre des tâches
- Cliquez sur la flèche en bas à droite (zone de notification)
- Cliquez sur l'icône de la baleine Docker

### Option C : Depuis le menu Démarrer
- Tapez "Docker" dans la recherche Windows
- Cliquez sur **Docker Desktop**

## Étape 2.2 : Comprendre l'interface

```
+------------------------------------------------------------------+
|  Docker Desktop                                          _ [] X  |
+------------------------------------------------------------------+
|                                                                   |
|  [Containers]  [Images]  [Volumes]  [Dev Environments]           |
|                                                                   |
+------------------------------------------------------------------+
|                                                                   |
|  CONTAINERS                                                       |
|  +---------------------------------------------------------+     |
|  | NAME           | IMAGE          | STATUS    | PORT      |     |
|  +---------------------------------------------------------+     |
|  | contact-api    | projet_api     | Running   | 8080:8080 |     |
|  | contact-db     | postgres:15    | Running   | 5432:5432 |     |
|  | contact-mailhog| mailhog        | Running   | 8025:8025 |     |
|  +---------------------------------------------------------+     |
|                                                                   |
+------------------------------------------------------------------+
```

### Les onglets principaux

| Onglet | Description |
|--------|-------------|
| **Containers** | Liste des containers en cours/arrêtés |
| **Images** | Liste des images Docker téléchargées |
| **Volumes** | Données persistantes (bases de données) |
| **Dev Environments** | Environnements de développement |

---

# PARTIE 3 : TERMINAL ET COMMANDES LINUX

## Étape 3.1 : Ouvrir un terminal

### Option A : PowerShell (recommandé pour Windows)

1. Clic droit sur le bouton **Démarrer**
2. Cliquez sur **Terminal** ou **Windows PowerShell**

### Option B : Invite de commandes (CMD)

1. Appuyez sur `Windows + R`
2. Tapez `cmd`
3. Appuyez sur Entrée

### Option C : Terminal intégré VS Code

1. Ouvrez VS Code
2. Menu **Terminal** > **New Terminal**
3. Ou raccourci : `Ctrl + ù` (clavier français) ou `Ctrl + `` (clavier anglais)

## Étape 3.2 : Naviguer dans les dossiers

### Commandes de base

| Commande Windows | Commande Linux | Description |
|------------------|----------------|-------------|
| `cd dossier` | `cd dossier` | Entrer dans un dossier |
| `cd ..` | `cd ..` | Remonter d'un niveau |
| `cd \` | `cd /` | Aller à la racine |
| `dir` | `ls` | Lister les fichiers |
| `cls` | `clear` | Effacer l'écran |
| `type fichier` | `cat fichier` | Afficher un fichier |

### Exemple pratique

```powershell
# Aller dans le dossier du projet
cd C:\00-projetsGA\projet-e-contact-backend

# Vérifier qu'on est au bon endroit
dir

# Résultat attendu :
#    Directory: C:\00-projetsGA\projet-e-contact-backend
#
#    Mode         LastWriteTime     Length Name
#    ----         -------------     ------ ----
#    d-----       20/01/2026        14:30  frontend
#    d-----       20/01/2026        14:30  src
#    d-----       20/01/2026        14:30  troubleshooting
#    -a----       20/01/2026        14:30  docker-compose.yml
#    -a----       20/01/2026        14:30  Dockerfile
#    ...
```

## Étape 3.3 : Commandes Docker essentielles

### Commandes de base

```powershell
# Voir les containers en cours
docker ps

# Voir TOUS les containers (y compris arrêtés)
docker ps -a

# Voir les images
docker images

# Voir les volumes
docker volume ls
```

### Commandes Docker Compose

```powershell
# Lancer les services
docker compose up -d

# Lancer avec rebuild
docker compose up --build -d

# Arrêter les services
docker compose down

# Voir les logs
docker compose logs

# Voir les logs en temps réel
docker compose logs -f
```

---

# PARTIE 4 : LANCER LE PROJET

## Étape 4.1 : Cloner ou télécharger le projet

### Option A : Avec Git

```powershell
# Aller dans le dossier de vos projets
cd C:\00-projetsGA

# Cloner le repo
git clone https://github.com/VOTRE_USERNAME/projet-e-contact-backend.git

# Entrer dans le dossier
cd projet-e-contact-backend
```

### Option B : Télécharger le ZIP

1. Allez sur GitHub
2. Cliquez sur **Code** > **Download ZIP**
3. Extrayez le ZIP dans `C:\00-projetsGA\`

## Étape 4.2 : Créer le fichier .env (optionnel)

```powershell
# Copier le fichier exemple
copy .env.example .env

# Ou créer manuellement avec Notepad
notepad .env
```

Contenu du fichier `.env` :

```env
# Base de données
DB_HOST=postgres
DB_PORT=5432
DB_NAME=contactdb
DB_USER=postgres
DB_PASSWORD=postgres

# Email
MAIL_HOST=mailhog
MAIL_PORT=1025
MAIL_USER=noreply@example.com

# JWT
JWT_SECRET=votre-secret-jwt-ici
```

## Étape 4.3 : Lancer le projet

```powershell
# S'assurer d'être dans le bon dossier
cd C:\00-projetsGA\projet-e-contact-backend

# Lancer Docker Compose
docker compose up --build -d
```

### Ce qui se passe :

```
[+] Building 45.2s (12/12) FINISHED
 => [internal] load build definition from Dockerfile
 => [internal] load .dockerignore
 => [internal] load metadata for docker.io/library/maven:3.9-eclipse-temurin-17
 => [internal] load metadata for docker.io/library/eclipse-temurin:17-jre-alpine
 => [build 1/4] FROM docker.io/library/maven:3.9-eclipse-temurin-17
 => [stage-1 1/3] FROM docker.io/library/eclipse-temurin:17-jre-alpine
 => [build 2/4] WORKDIR /app
 => [build 3/4] COPY pom.xml .
 => [build 4/4] COPY src ./src
 => [build 5/4] RUN mvn clean package -DskipTests
 => [stage-1 2/3] COPY --from=build /app/target/*.jar app.jar
 => exporting to image

[+] Running 4/4
 ✔ Network projet-e-contact-backend_default  Created
 ✔ Container contact-db                      Started
 ✔ Container contact-mailhog                 Started
 ✔ Container contact-api                     Started
```

## Étape 4.4 : Vérifier que tout fonctionne

```powershell
# Voir les containers
docker ps
```

Résultat attendu :

```
CONTAINER ID   IMAGE                  COMMAND                  STATUS          PORTS                    NAMES
abc123def456   projet_api             "java -jar app.jar"      Up 2 minutes    0.0.0.0:8080->8080/tcp   contact-api
def456ghi789   postgres:15-alpine     "docker-entrypoint.s…"   Up 2 minutes    0.0.0.0:5432->5432/tcp   contact-db
ghi789jkl012   mailhog/mailhog        "MailHog"                Up 2 minutes    0.0.0.0:8025->8025/tcp   contact-mailhog
```

## Étape 4.5 : Accéder aux services

Ouvrez votre navigateur :

| Service | URL |
|---------|-----|
| API (Swagger) | http://localhost:8080/swagger-ui.html |
| MailHog | http://localhost:8025 |
| API directe | http://localhost:8080/api/contact |

---

# PARTIE 5 : GÉRER LES CONTAINERS

## Étape 5.1 : Via Docker Desktop (interface graphique)

### Voir les containers

1. Ouvrez Docker Desktop
2. Cliquez sur l'onglet **Containers**
3. Vous voyez la liste des containers

### Actions sur un container

Cliquez sur un container pour voir les options :

```
+------------------------------------------------------------------+
|  contact-api                                              [...]  |
+------------------------------------------------------------------+
|  [Stop]  [Restart]  [Delete]  [Open in terminal]  [View logs]    |
+------------------------------------------------------------------+
|                                                                   |
|  LOGS                                                             |
|  2026-01-20 14:30:00 INFO  Started ContactApplication in 5.2s    |
|  2026-01-20 14:30:01 INFO  Tomcat started on port 8080           |
|                                                                   |
+------------------------------------------------------------------+
```

| Bouton | Action |
|--------|--------|
| **Stop** | Arrêter le container |
| **Start** | Démarrer le container |
| **Restart** | Redémarrer le container |
| **Delete** | Supprimer le container |
| **Open in terminal** | Ouvrir un terminal DANS le container |
| **View logs** | Voir les logs du container |

## Étape 5.2 : Via ligne de commande

### Arrêter un container

```powershell
# Arrêter un container spécifique
docker stop contact-api

# Arrêter tous les containers du projet
docker compose stop
```

### Démarrer un container

```powershell
# Démarrer un container spécifique
docker start contact-api

# Démarrer tous les containers du projet
docker compose start
```

### Redémarrer un container

```powershell
# Redémarrer un container spécifique
docker restart contact-api

# Redémarrer tous les containers
docker compose restart
```

### Supprimer les containers

```powershell
# Arrêter et supprimer tous les containers du projet
docker compose down

# Supprimer aussi les volumes (ATTENTION: perte de données!)
docker compose down -v
```

## Étape 5.3 : Entrer dans un container

### Via Docker Desktop

1. Cliquez sur le container
2. Cliquez sur **Open in terminal**
3. Vous êtes maintenant DANS le container

### Via ligne de commande

```powershell
# Entrer dans le container API
docker exec -it contact-api sh

# Entrer dans le container PostgreSQL
docker exec -it contact-db bash

# Entrer dans le container MailHog
docker exec -it contact-mailhog sh
```

### Une fois dans le container

```bash
# Vous êtes maintenant dans un environnement Linux !
# Le prompt change :

/app $    # Vous êtes dans le container API
root@abc123:/# # Vous êtes dans le container PostgreSQL

# Pour sortir du container
exit
```

---

## Étape 5.4 : Commandes à l'intérieur du container API (Spring Boot)

### Entrer dans le container

```powershell
docker exec -it contact-api sh
```

### Commandes disponibles

```bash
# Voir où vous êtes
pwd
# Résultat : /app

# Lister les fichiers
ls -la
# Résultat :
# -rw-r--r-- 1 root root 45678901 Jan 20 14:30 app.jar

# Voir les variables d'environnement
env

# Voir les variables d'environnement liées à la DB
env | grep DB
# Résultat :
# DB_HOST=postgres
# DB_PORT=5432
# DB_NAME=contactdb
# DB_USER=postgres
# DB_PASSWORD=postgres

# Voir les variables d'environnement liées au mail
env | grep MAIL
# Résultat :
# MAIL_HOST=mailhog
# MAIL_PORT=1025
# MAIL_USER=noreply@example.com

# Voir l'utilisation mémoire
free -m

# Voir les processus en cours
ps aux

# Voir le processus Java
ps aux | grep java

# Tester la connexion à PostgreSQL
nc -zv postgres 5432
# Résultat : postgres (172.18.0.2:5432) open

# Tester la connexion à MailHog
nc -zv mailhog 1025
# Résultat : mailhog (172.18.0.3:1025) open

# Voir les fichiers de configuration (si présents)
cat /app/application.yml 2>/dev/null || echo "Fichier dans le JAR"

# Voir l'espace disque
df -h

# Sortir du container
exit
```

---

## Étape 5.5 : Commandes à l'intérieur du container PostgreSQL

### Entrer dans le container

```powershell
docker exec -it contact-db bash
```

### Commandes système

```bash
# Voir où vous êtes
pwd
# Résultat : /

# Voir la version de PostgreSQL
postgres --version
# Résultat : postgres (PostgreSQL) 15.x

# Voir les variables d'environnement
env | grep POSTGRES
# Résultat :
# POSTGRES_DB=contactdb
# POSTGRES_USER=postgres
# POSTGRES_PASSWORD=postgres

# Voir l'espace disque utilisé par PostgreSQL
du -sh /var/lib/postgresql/data
# Résultat : 150M /var/lib/postgresql/data

# Lister les fichiers de données
ls -la /var/lib/postgresql/data

# Voir les connexions actives
cat /var/lib/postgresql/data/pg_hba.conf
```

### Accéder à PostgreSQL (depuis l'intérieur du container)

```bash
# Se connecter à PostgreSQL
psql -U postgres -d contactdb

# Vous êtes maintenant dans psql :
contactdb=#
```

### Commandes PostgreSQL (psql)

```sql
-- Voir la version
SELECT version();

-- Lister toutes les bases de données
\l

-- Se connecter à une base
\c contactdb

-- Lister les tables
\dt

-- Résultat :
--          List of relations
--  Schema |  Name  | Type  |  Owner
-- --------+--------+-------+----------
--  public | leads  | table | postgres
--  public | users  | table | postgres

-- Voir la structure de la table leads
\d leads

-- Voir la structure de la table users
\d users

-- Compter les enregistrements
SELECT 'leads' as table_name, COUNT(*) as count FROM leads
UNION ALL
SELECT 'users', COUNT(*) FROM users;

-- Voir tous les leads
SELECT * FROM leads;

-- Voir les leads formatés
SELECT 
    id,
    full_name AS "Nom",
    email AS "Email",
    request_type AS "Type",
    status AS "Statut",
    created_at::timestamp(0) AS "Créé le"
FROM leads
ORDER BY id DESC;

-- Voir les utilisateurs (admins)
SELECT 
    id,
    first_name || ' ' || last_name AS "Nom complet",
    email AS "Email",
    role AS "Rôle",
    created_at::date AS "Créé le"
FROM users;

-- Statistiques par statut
SELECT 
    status AS "Statut",
    COUNT(*) AS "Nombre",
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM leads), 1) AS "Pourcentage"
FROM leads
GROUP BY status
ORDER BY COUNT(*) DESC;

-- Statistiques par type de demande
SELECT 
    request_type AS "Type",
    COUNT(*) AS "Nombre"
FROM leads
GROUP BY request_type
ORDER BY COUNT(*) DESC;

-- Leads des dernières 24 heures
SELECT * FROM leads 
WHERE created_at >= NOW() - INTERVAL '24 hours';

-- Rechercher par email
SELECT * FROM leads WHERE email ILIKE '%gmail%';

-- Rechercher par nom
SELECT * FROM leads WHERE full_name ILIKE '%dupont%';

-- Modifier le statut d'un lead
UPDATE leads SET status = 'CONTACTED', updated_at = NOW() WHERE id = 1;

-- Supprimer un lead
DELETE FROM leads WHERE id = 1;

-- Voir la taille des tables
SELECT 
    relname AS "Table",
    pg_size_pretty(pg_total_relation_size(relid)) AS "Taille"
FROM pg_catalog.pg_statio_user_tables
ORDER BY pg_total_relation_size(relid) DESC;

-- Voir les connexions actives à la base
SELECT 
    pid,
    usename AS "Utilisateur",
    application_name AS "Application",
    client_addr AS "IP Client",
    state AS "État"
FROM pg_stat_activity
WHERE datname = 'contactdb';

-- Quitter psql
\q
```

### Exporter/Importer depuis l'intérieur du container

```bash
# Créer un backup (depuis bash, pas psql)
pg_dump -U postgres contactdb > /tmp/backup.sql

# Voir le backup
head -50 /tmp/backup.sql

# Restaurer un backup
psql -U postgres -d contactdb < /tmp/backup.sql

# Exporter en CSV
psql -U postgres -d contactdb -c "COPY leads TO '/tmp/leads.csv' WITH CSV HEADER"

# Voir le CSV
cat /tmp/leads.csv

# Sortir du container
exit
```

---

## Étape 5.6 : Commandes à l'intérieur du container MailHog

### Entrer dans le container

```powershell
docker exec -it contact-mailhog sh
```

### Commandes disponibles

```bash
# Voir où vous êtes
pwd
# Résultat : /

# Voir les processus
ps aux
# Résultat : MailHog en cours d'exécution

# Voir les variables d'environnement
env

# Voir l'aide de MailHog
MailHog --help

# Voir les fichiers de MailHog
ls -la /

# Tester la connectivité SMTP (port 1025)
nc -zv localhost 1025

# Tester la connectivité HTTP (port 8025)
nc -zv localhost 8025

# Voir l'utilisation mémoire
free -m

# Sortir du container
exit
```

### Accéder aux emails via l'API MailHog

```bash
# Depuis l'intérieur du container MailHog ou depuis PowerShell

# Voir tous les emails (API JSON)
curl http://localhost:8025/api/v2/messages

# Compter les emails
curl http://localhost:8025/api/v2/messages | grep -o '"Total":' 

# Supprimer tous les emails
curl -X DELETE http://localhost:8025/api/v1/messages
```

---

## Étape 5.7 : Commandes utiles depuis PowerShell (sans entrer dans les containers)

### Exécuter des commandes directement

```powershell
# Voir les variables d'environnement de l'API
docker exec contact-api env

# Voir les variables DB
docker exec contact-api env | Select-String "DB"

# Voir les variables MAIL
docker exec contact-api env | Select-String "MAIL"

# Tester la connexion DB depuis l'API
docker exec contact-api nc -zv postgres 5432

# Exécuter une requête SQL
docker exec contact-db psql -U postgres -d contactdb -c "SELECT COUNT(*) FROM leads;"

# Voir les 5 derniers leads
docker exec contact-db psql -U postgres -d contactdb -c "SELECT id, full_name, email, status FROM leads ORDER BY id DESC LIMIT 5;"

# Voir les stats par statut
docker exec contact-db psql -U postgres -d contactdb -c "SELECT status, COUNT(*) FROM leads GROUP BY status;"

# Créer un backup
docker exec contact-db pg_dump -U postgres contactdb > backup.sql

# Voir la taille des containers
docker ps --size

# Voir l'utilisation des ressources en temps réel
docker stats

# Voir l'utilisation des ressources une seule fois
docker stats --no-stream
```

---

## Étape 5.8 : Résumé des commandes par container

### Container API (contact-api)

| Commande | Description |
|----------|-------------|
| `env` | Voir toutes les variables d'environnement |
| `env \| grep DB` | Voir les variables de base de données |
| `env \| grep MAIL` | Voir les variables email |
| `ps aux` | Voir les processus |
| `nc -zv postgres 5432` | Tester connexion PostgreSQL |
| `nc -zv mailhog 1025` | Tester connexion MailHog |
| `free -m` | Voir la mémoire |
| `df -h` | Voir l'espace disque |

### Container PostgreSQL (contact-db)

| Commande | Description |
|----------|-------------|
| `psql -U postgres -d contactdb` | Entrer dans PostgreSQL |
| `\dt` | Lister les tables (dans psql) |
| `\d leads` | Structure de la table leads |
| `SELECT * FROM leads;` | Voir tous les leads |
| `SELECT * FROM users;` | Voir tous les utilisateurs |
| `\q` | Quitter psql |
| `pg_dump -U postgres contactdb` | Créer un backup |

### Container MailHog (contact-mailhog)

| Commande | Description |
|----------|-------------|
| `ps aux` | Voir les processus |
| `nc -zv localhost 1025` | Tester port SMTP |
| `nc -zv localhost 8025` | Tester port HTTP |

---

# PARTIE 6 : GÉRER LES IMAGES

## Étape 6.1 : Comprendre les images

Une **image** est un modèle pour créer des containers.
Un **container** est une instance en cours d'exécution d'une image.

Analogie :
- Image = Recette de cuisine
- Container = Plat préparé à partir de la recette

## Étape 6.2 : Via Docker Desktop

1. Ouvrez Docker Desktop
2. Cliquez sur l'onglet **Images**

```
+------------------------------------------------------------------+
|  IMAGES                                                           |
+------------------------------------------------------------------+
|  NAME                    TAG        SIZE      CREATED             |
+------------------------------------------------------------------+
|  projet_api              latest     285 MB    2 hours ago         |
|  postgres                15-alpine  232 MB    3 days ago          |
|  mailhog/mailhog         latest     45 MB     2 weeks ago         |
|  nginx                   alpine     42 MB     1 month ago         |
+------------------------------------------------------------------+
```

### Actions sur une image

| Action | Description |
|--------|-------------|
| **Run** | Créer un container à partir de cette image |
| **Pull** | Télécharger la dernière version |
| **Delete** | Supprimer l'image |

## Étape 6.3 : Via ligne de commande

### Lister les images

```powershell
docker images
```

Résultat :

```
REPOSITORY        TAG          IMAGE ID       CREATED        SIZE
projet_api        latest       abc123def456   2 hours ago    285MB
postgres          15-alpine    def456ghi789   3 days ago     232MB
mailhog/mailhog   latest       ghi789jkl012   2 weeks ago    45MB
```

### Télécharger une image

```powershell
# Télécharger PostgreSQL
docker pull postgres:15-alpine

# Télécharger Nginx
docker pull nginx:alpine
```

### Supprimer une image

```powershell
# Supprimer une image par nom
docker rmi postgres:15-alpine

# Supprimer une image par ID
docker rmi abc123def456

# Forcer la suppression
docker rmi -f abc123def456
```

### Nettoyer les images inutilisées

```powershell
# Supprimer les images non utilisées
docker image prune

# Supprimer TOUTES les images non utilisées (avec confirmation)
docker image prune -a
```

### Reconstruire une image

```powershell
# Reconstruire l'image de l'API
docker compose build api

# Reconstruire toutes les images
docker compose build

# Reconstruire sans cache (clean build)
docker compose build --no-cache
```

---

# PARTIE 7 : ACCÉDER À LA BASE DE DONNÉES

## Étape 7.1 : Méthode 1 - Via Docker Desktop

1. Ouvrez Docker Desktop
2. Cliquez sur le container **contact-db**
3. Cliquez sur **Open in terminal**
4. Tapez :

```bash
psql -U postgres -d contactdb
```

Vous êtes maintenant dans PostgreSQL :

```
contactdb=#
```

## Étape 7.2 : Méthode 2 - Via PowerShell (une seule commande)

```powershell
docker exec -it contact-db psql -U postgres -d contactdb
```

## Étape 7.3 : Commandes PostgreSQL

### Navigation de base

```sql
-- Lister les tables
\dt

-- Résultat :
--          List of relations
--  Schema |  Name  | Type  |  Owner
-- --------+--------+-------+----------
--  public | leads  | table | postgres
--  public | users  | table | postgres

-- Voir la structure d'une table
\d leads

-- Quitter PostgreSQL
\q
```

### Voir les données

```sql
-- Voir tous les leads
SELECT * FROM leads;

-- Voir les leads de façon lisible
SELECT 
    id,
    full_name AS "Nom",
    email AS "Email",
    status AS "Statut",
    created_at::date AS "Date"
FROM leads
ORDER BY id;

-- Compter les leads
SELECT COUNT(*) AS "Total" FROM leads;

-- Voir les utilisateurs
SELECT id, first_name, last_name, email, role FROM users;
```

### Modifier les données

```sql
-- Changer le statut d'un lead
UPDATE leads SET status = 'CONTACTED' WHERE id = 1;

-- Supprimer un lead
DELETE FROM leads WHERE id = 1;

-- Supprimer TOUS les leads (attention!)
DELETE FROM leads;
```

## Étape 7.4 : Méthode 3 - Commandes one-liner depuis PowerShell

```powershell
# Voir tous les leads
docker exec -it contact-db psql -U postgres -d contactdb -c "SELECT * FROM leads;"

# Compter les leads
docker exec -it contact-db psql -U postgres -d contactdb -c "SELECT COUNT(*) FROM leads;"

# Voir les utilisateurs
docker exec -it contact-db psql -U postgres -d contactdb -c "SELECT * FROM users;"
```

## Étape 7.5 : Méthode 4 - Via un outil graphique (DBeaver)

### Installer DBeaver

1. Téléchargez DBeaver : https://dbeaver.io/download/
2. Installez-le
3. Lancez DBeaver

### Configurer la connexion

1. Cliquez sur **Database** > **New Database Connection**
2. Sélectionnez **PostgreSQL**
3. Remplissez :

| Champ | Valeur |
|-------|--------|
| Host | localhost |
| Port | 5432 |
| Database | contactdb |
| Username | postgres |
| Password | postgres |

4. Cliquez sur **Test Connection**
5. Cliquez sur **Finish**

### Utiliser DBeaver

1. Dans le panneau de gauche, développez la connexion
2. Développez **contactdb** > **Schemas** > **public** > **Tables**
3. Double-cliquez sur **leads** pour voir les données
4. Vous pouvez modifier directement dans l'interface

## Étape 7.6 : Exporter/Importer les données

### Exporter en CSV

```powershell
# Exporter les leads en CSV
docker exec -it contact-db psql -U postgres -d contactdb -c "COPY leads TO STDOUT WITH CSV HEADER" > leads_export.csv
```

### Créer un backup SQL

```powershell
# Backup complet de la base
docker exec contact-db pg_dump -U postgres contactdb > backup.sql

# Backup avec date
docker exec contact-db pg_dump -U postgres contactdb > backup_%date:~-4,4%%date:~-7,2%%date:~-10,2%.sql
```

### Restaurer un backup

```powershell
# Restaurer depuis un fichier SQL
Get-Content backup.sql | docker exec -i contact-db psql -U postgres -d contactdb
```

---

# PARTIE 8 : GÉRER LES VOLUMES

## Étape 8.1 : Comprendre les volumes

Les **volumes** stockent les données de façon persistante.
Sans volume, les données sont perdues quand le container est supprimé.

## Étape 8.2 : Via Docker Desktop

1. Ouvrez Docker Desktop
2. Cliquez sur l'onglet **Volumes**

```
+------------------------------------------------------------------+
|  VOLUMES                                                          |
+------------------------------------------------------------------+
|  NAME                                    SIZE      CREATED        |
+------------------------------------------------------------------+
|  projet-e-contact-backend_postgres_data  150 MB    2 hours ago    |
+------------------------------------------------------------------+
```

### Actions sur un volume

| Action | Description |
|--------|-------------|
| **View data** | Voir le contenu du volume |
| **Delete** | Supprimer le volume (PERTE DE DONNÉES!) |

## Étape 8.3 : Via ligne de commande

### Lister les volumes

```powershell
docker volume ls
```

Résultat :

```
DRIVER    VOLUME NAME
local     projet-e-contact-backend_postgres_data
```

### Inspecter un volume

```powershell
docker volume inspect projet-e-contact-backend_postgres_data
```

Résultat :

```json
[
    {
        "CreatedAt": "2026-01-20T14:30:00Z",
        "Driver": "local",
        "Labels": {
            "com.docker.compose.project": "projet-e-contact-backend",
            "com.docker.compose.volume": "postgres_data"
        },
        "Mountpoint": "/var/lib/docker/volumes/projet-e-contact-backend_postgres_data/_data",
        "Name": "projet-e-contact-backend_postgres_data",
        "Options": null,
        "Scope": "local"
    }
]
```

### Supprimer un volume

```powershell
# Supprimer un volume spécifique (ATTENTION!)
docker volume rm projet-e-contact-backend_postgres_data

# Supprimer les volumes non utilisés
docker volume prune
```

---

# PARTIE 9 : VOIR LES LOGS

## Étape 9.1 : Via Docker Desktop

1. Cliquez sur le container souhaité
2. Les logs s'affichent automatiquement

```
+------------------------------------------------------------------+
|  contact-api - Logs                                               |
+------------------------------------------------------------------+
|                                                                   |
|  2026-01-20 14:30:00.123 INFO  --- [main] c.e.c.ContactApplication |
|  : Starting ContactApplication v1.0.0                             |
|                                                                   |
|  2026-01-20 14:30:02.456 INFO  --- [main] o.s.b.w.e.t.TomcatWeb   |
|  : Tomcat started on port(s): 8080 (http)                         |
|                                                                   |
|  2026-01-20 14:30:02.789 INFO  --- [main] c.e.c.ContactApplication |
|  : Started ContactApplication in 5.234 seconds                    |
|                                                                   |
+------------------------------------------------------------------+
```

## Étape 9.2 : Via ligne de commande

### Voir les logs d'un container

```powershell
# Logs du container API
docker logs contact-api

# Logs du container PostgreSQL
docker logs contact-db

# Logs de MailHog
docker logs contact-mailhog
```

### Options utiles

```powershell
# Voir les 50 dernières lignes
docker logs contact-api --tail 50

# Suivre les logs en temps réel
docker logs contact-api -f

# Suivre les 20 dernières lignes en temps réel
docker logs contact-api --tail 20 -f

# Voir les logs avec timestamp
docker logs contact-api -t
```

### Logs de tous les services (Docker Compose)

```powershell
# Voir tous les logs
docker compose logs

# Suivre tous les logs en temps réel
docker compose logs -f

# Logs d'un service spécifique
docker compose logs api
docker compose logs postgres
```

### Filtrer les logs

```powershell
# Chercher les erreurs (PowerShell)
docker logs contact-api 2>&1 | Select-String "ERROR"

# Chercher les emails
docker logs contact-api 2>&1 | Select-String "email"
```

---

# PARTIE 10 : RÉSOUDRE LES PROBLÈMES

## Problème 1 : Docker ne démarre pas

### Symptôme
```
error during connect: This error may indicate that the docker daemon is not running
```

### Solution

1. Ouvrez Docker Desktop
2. Attendez que l'icône devienne verte
3. Si ça ne fonctionne pas, redémarrez Docker Desktop
4. En dernier recours, redémarrez Windows

## Problème 2 : Port déjà utilisé

### Symptôme
```
Error response from daemon: Ports are not available: exposing port TCP 0.0.0.0:8080
```

### Solution

```powershell
# Trouver le processus qui utilise le port 8080
netstat -ano | findstr :8080

# Résultat exemple :
# TCP    0.0.0.0:8080    0.0.0.0:0    LISTENING    12345

# Tuer le processus (remplacez 12345 par le PID trouvé)
taskkill /PID 12345 /F
```

## Problème 3 : Container ne démarre pas

### Symptôme
```
Container contact-api exited with code 1
```

### Solution

```powershell
# Voir les logs pour comprendre l'erreur
docker logs contact-api

# Vérifier la configuration
docker compose config

# Reconstruire l'image
docker compose up --build -d
```

## Problème 4 : Pas de connexion à la base de données

### Symptôme
```
Connection refused: localhost:5432
```

### Solution

```powershell
# Vérifier que PostgreSQL tourne
docker ps | findstr postgres

# Vérifier les logs de PostgreSQL
docker logs contact-db

# Redémarrer PostgreSQL
docker restart contact-db
```

## Problème 5 : Espace disque plein

### Symptôme
```
no space left on device
```

### Solution

```powershell
# Voir l'utilisation de Docker
docker system df

# Nettoyer tout ce qui n'est pas utilisé
docker system prune -a

# Nettoyer aussi les volumes (ATTENTION: perte de données!)
docker system prune -a --volumes
```

## Problème 6 : Image ne se reconstruit pas

### Symptôme
Les modifications du code ne sont pas prises en compte.

### Solution

```powershell
# Forcer la reconstruction sans cache
docker compose build --no-cache

# Ou tout arrêter et relancer proprement
docker compose down
docker compose up --build -d
```

---

# RÉSUMÉ DES COMMANDES ESSENTIELLES

## Commandes quotidiennes

```powershell
# Lancer le projet
docker compose up -d

# Lancer avec rebuild
docker compose up --build -d

# Arrêter le projet
docker compose down

# Voir les containers
docker ps

# Voir les logs
docker compose logs -f
```

## Accès base de données

```powershell
# Entrer dans PostgreSQL
docker exec -it contact-db psql -U postgres -d contactdb

# Requête rapide
docker exec -it contact-db psql -U postgres -d contactdb -c "SELECT * FROM leads;"
```

## Maintenance

```powershell
# Nettoyer Docker
docker system prune -a

# Backup base de données
docker exec contact-db pg_dump -U postgres contactdb > backup.sql

# Redémarrer un service
docker restart contact-api
```

---

# AIDE-MÉMOIRE VISUEL

```
+------------------------------------------------------------------+
|                    WORKFLOW DOCKER DESKTOP                        |
+------------------------------------------------------------------+
|                                                                   |
|  1. LANCER                                                        |
|     PowerShell> cd C:\projet                                      |
|     PowerShell> docker compose up --build -d                      |
|                                                                   |
|  2. VÉRIFIER                                                      |
|     PowerShell> docker ps                                         |
|     -> 3 containers running                                       |
|                                                                   |
|  3. UTILISER                                                      |
|     Navigateur> http://localhost:8080/swagger-ui.html             |
|     Navigateur> http://localhost:8025 (emails)                    |
|                                                                   |
|  4. DEBUG                                                         |
|     PowerShell> docker logs contact-api -f                        |
|                                                                   |
|  5. BASE DE DONNÉES                                               |
|     PowerShell> docker exec -it contact-db psql -U postgres -d    |
|                 contactdb                                         |
|                                                                   |
|  6. ARRÊTER                                                       |
|     PowerShell> docker compose down                               |
|                                                                   |
+------------------------------------------------------------------+
```

Ce guide couvre tout ce dont vous avez besoin pour utiliser Docker Desktop sur Windows.
