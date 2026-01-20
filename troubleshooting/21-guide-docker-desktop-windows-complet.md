# 21 - Guide Ultra Detaille : Docker Desktop sur Windows

## Table des matieres

1. [Installation de Docker Desktop](#partie-1--installation-de-docker-desktop)
2. [Interface Docker Desktop](#partie-2--interface-docker-desktop)
3. [Terminal et Commandes Linux](#partie-3--terminal-et-commandes-linux)
4. [Lancer le Projet](#partie-4--lancer-le-projet)
5. [Gerer les Containers](#partie-5--gerer-les-containers)
6. [Gerer les Images](#partie-6--gerer-les-images)
7. [Acceder a la Base de Donnees](#partie-7--acceder-a-la-base-de-donnees)
8. [Gerer les Volumes](#partie-8--gerer-les-volumes)
9. [Voir les Logs](#partie-9--voir-les-logs)
10. [Resoudre les Problemes](#partie-10--resoudre-les-problemes)

---

# PARTIE 1 : INSTALLATION DE DOCKER DESKTOP

## Etape 1.1 : Telecharger Docker Desktop

1. Ouvrez votre navigateur
2. Allez sur : https://www.docker.com/products/docker-desktop/
3. Cliquez sur **Download for Windows**
4. Le fichier `Docker Desktop Installer.exe` se telecharge

## Etape 1.2 : Installer Docker Desktop

1. Double-cliquez sur `Docker Desktop Installer.exe`
2. Cochez **Use WSL 2 instead of Hyper-V** (recommande)
3. Cochez **Add shortcut to desktop**
4. Cliquez sur **Ok**
5. Attendez l'installation (5-10 minutes)
6. Cliquez sur **Close and restart**

## Etape 1.3 : Premier lancement

1. Apres le redemarrage, Docker Desktop se lance automatiquement
2. Acceptez les conditions d'utilisation
3. Vous pouvez **Skip** la creation de compte (optionnel)
4. Attendez que Docker demarre (icone verte dans la barre des taches)

## Etape 1.4 : Verifier l'installation

Ouvrez PowerShell (clic droit sur Demarrer > Terminal) et tapez :

```powershell
docker --version
```

Resultat attendu :
```
Docker version 24.0.7, build afdd53b
```

```powershell
docker compose version
```

Resultat attendu :
```
Docker Compose version v2.23.3-desktop.2
```

---

# PARTIE 2 : INTERFACE DOCKER DESKTOP

## Etape 2.1 : Ouvrir Docker Desktop

### Option A : Depuis le bureau
- Double-cliquez sur l'icone **Docker Desktop** sur le bureau

### Option B : Depuis la barre des taches
- Cliquez sur la fleche en bas a droite (zone de notification)
- Cliquez sur l'icone de la baleine Docker

### Option C : Depuis le menu Demarrer
- Tapez "Docker" dans la recherche Windows
- Cliquez sur **Docker Desktop**

## Etape 2.2 : Comprendre l'interface

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
| **Containers** | Liste des containers en cours/arretes |
| **Images** | Liste des images Docker telecharges |
| **Volumes** | Donnees persistantes (bases de donnees) |
| **Dev Environments** | Environnements de developpement |

---

# PARTIE 3 : TERMINAL ET COMMANDES LINUX

## Etape 3.1 : Ouvrir un terminal

### Option A : PowerShell (recommande pour Windows)

1. Clic droit sur le bouton **Demarrer**
2. Cliquez sur **Terminal** ou **Windows PowerShell**

### Option B : Invite de commandes (CMD)

1. Appuyez sur `Windows + R`
2. Tapez `cmd`
3. Appuyez sur Entree

### Option C : Terminal integre VS Code

1. Ouvrez VS Code
2. Menu **Terminal** > **New Terminal**
3. Ou raccourci : `Ctrl + ù` (clavier francais) ou `Ctrl + `` (clavier anglais)

## Etape 3.2 : Naviguer dans les dossiers

### Commandes de base

| Commande Windows | Commande Linux | Description |
|------------------|----------------|-------------|
| `cd dossier` | `cd dossier` | Entrer dans un dossier |
| `cd ..` | `cd ..` | Remonter d'un niveau |
| `cd \` | `cd /` | Aller a la racine |
| `dir` | `ls` | Lister les fichiers |
| `cls` | `clear` | Effacer l'ecran |
| `type fichier` | `cat fichier` | Afficher un fichier |

### Exemple pratique

```powershell
# Aller dans le dossier du projet
cd C:\00-projetsGA\projet-e-contact-backend

# Verifier qu'on est au bon endroit
dir

# Resultat attendu :
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

## Etape 3.3 : Commandes Docker essentielles

### Commandes de base

```powershell
# Voir les containers en cours
docker ps

# Voir TOUS les containers (y compris arretes)
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

# Arreter les services
docker compose down

# Voir les logs
docker compose logs

# Voir les logs en temps reel
docker compose logs -f
```

---

# PARTIE 4 : LANCER LE PROJET

## Etape 4.1 : Cloner ou telecharger le projet

### Option A : Avec Git

```powershell
# Aller dans le dossier de vos projets
cd C:\00-projetsGA

# Cloner le repo
git clone https://github.com/VOTRE_USERNAME/projet-e-contact-backend.git

# Entrer dans le dossier
cd projet-e-contact-backend
```

### Option B : Telecharger le ZIP

1. Allez sur GitHub
2. Cliquez sur **Code** > **Download ZIP**
3. Extrayez le ZIP dans `C:\00-projetsGA\`

## Etape 4.2 : Creer le fichier .env (optionnel)

```powershell
# Copier le fichier exemple
copy .env.example .env

# Ou creer manuellement avec Notepad
notepad .env
```

Contenu du fichier `.env` :

```env
# Base de donnees
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

## Etape 4.3 : Lancer le projet

```powershell
# S'assurer d'etre dans le bon dossier
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

## Etape 4.4 : Verifier que tout fonctionne

```powershell
# Voir les containers
docker ps
```

Resultat attendu :

```
CONTAINER ID   IMAGE                  COMMAND                  STATUS          PORTS                    NAMES
abc123def456   projet_api             "java -jar app.jar"      Up 2 minutes    0.0.0.0:8080->8080/tcp   contact-api
def456ghi789   postgres:15-alpine     "docker-entrypoint.s…"   Up 2 minutes    0.0.0.0:5432->5432/tcp   contact-db
ghi789jkl012   mailhog/mailhog        "MailHog"                Up 2 minutes    0.0.0.0:8025->8025/tcp   contact-mailhog
```

## Etape 4.5 : Acceder aux services

Ouvrez votre navigateur :

| Service | URL |
|---------|-----|
| API (Swagger) | http://localhost:8080/swagger-ui.html |
| MailHog | http://localhost:8025 |
| API directe | http://localhost:8080/api/contact |

---

# PARTIE 5 : GERER LES CONTAINERS

## Etape 5.1 : Via Docker Desktop (interface graphique)

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
| **Stop** | Arreter le container |
| **Start** | Demarrer le container |
| **Restart** | Redemarrer le container |
| **Delete** | Supprimer le container |
| **Open in terminal** | Ouvrir un terminal DANS le container |
| **View logs** | Voir les logs du container |

## Etape 5.2 : Via ligne de commande

### Arreter un container

```powershell
# Arreter un container specifique
docker stop contact-api

# Arreter tous les containers du projet
docker compose stop
```

### Demarrer un container

```powershell
# Demarrer un container specifique
docker start contact-api

# Demarrer tous les containers du projet
docker compose start
```

### Redemarrer un container

```powershell
# Redemarrer un container specifique
docker restart contact-api

# Redemarrer tous les containers
docker compose restart
```

### Supprimer les containers

```powershell
# Arreter et supprimer tous les containers du projet
docker compose down

# Supprimer aussi les volumes (ATTENTION: perte de donnees!)
docker compose down -v
```

## Etape 5.3 : Entrer dans un container

### Via Docker Desktop

1. Cliquez sur le container
2. Cliquez sur **Open in terminal**
3. Vous etes maintenant DANS le container

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
# Vous etes maintenant dans un environnement Linux !
# Le prompt change :

/app $    # Vous etes dans le container API
root@abc123:/# # Vous etes dans le container PostgreSQL

# Pour sortir du container
exit
```

---

# PARTIE 6 : GERER LES IMAGES

## Etape 6.1 : Comprendre les images

Une **image** est un modele pour creer des containers.
Un **container** est une instance en cours d'execution d'une image.

Analogie :
- Image = Recette de cuisine
- Container = Plat prepare a partir de la recette

## Etape 6.2 : Via Docker Desktop

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
| **Run** | Creer un container a partir de cette image |
| **Pull** | Telecharger la derniere version |
| **Delete** | Supprimer l'image |

## Etape 6.3 : Via ligne de commande

### Lister les images

```powershell
docker images
```

Resultat :

```
REPOSITORY        TAG          IMAGE ID       CREATED        SIZE
projet_api        latest       abc123def456   2 hours ago    285MB
postgres          15-alpine    def456ghi789   3 days ago     232MB
mailhog/mailhog   latest       ghi789jkl012   2 weeks ago    45MB
```

### Telecharger une image

```powershell
# Telecharger PostgreSQL
docker pull postgres:15-alpine

# Telecharger Nginx
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

### Nettoyer les images inutilisees

```powershell
# Supprimer les images non utilisees
docker image prune

# Supprimer TOUTES les images non utilisees (avec confirmation)
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

# PARTIE 7 : ACCEDER A LA BASE DE DONNEES

## Etape 7.1 : Methode 1 - Via Docker Desktop

1. Ouvrez Docker Desktop
2. Cliquez sur le container **contact-db**
3. Cliquez sur **Open in terminal**
4. Tapez :

```bash
psql -U postgres -d contactdb
```

Vous etes maintenant dans PostgreSQL :

```
contactdb=#
```

## Etape 7.2 : Methode 2 - Via PowerShell (une seule commande)

```powershell
docker exec -it contact-db psql -U postgres -d contactdb
```

## Etape 7.3 : Commandes PostgreSQL

### Navigation de base

```sql
-- Lister les tables
\dt

-- Resultat :
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

### Voir les donnees

```sql
-- Voir tous les leads
SELECT * FROM leads;

-- Voir les leads de facon lisible
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

### Modifier les donnees

```sql
-- Changer le statut d'un lead
UPDATE leads SET status = 'CONTACTED' WHERE id = 1;

-- Supprimer un lead
DELETE FROM leads WHERE id = 1;

-- Supprimer TOUS les leads (attention!)
DELETE FROM leads;
```

## Etape 7.4 : Methode 3 - Commandes one-liner depuis PowerShell

```powershell
# Voir tous les leads
docker exec -it contact-db psql -U postgres -d contactdb -c "SELECT * FROM leads;"

# Compter les leads
docker exec -it contact-db psql -U postgres -d contactdb -c "SELECT COUNT(*) FROM leads;"

# Voir les utilisateurs
docker exec -it contact-db psql -U postgres -d contactdb -c "SELECT * FROM users;"
```

## Etape 7.5 : Methode 4 - Via un outil graphique (DBeaver)

### Installer DBeaver

1. Telechargez DBeaver : https://dbeaver.io/download/
2. Installez-le
3. Lancez DBeaver

### Configurer la connexion

1. Cliquez sur **Database** > **New Database Connection**
2. Selectionnez **PostgreSQL**
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

1. Dans le panneau de gauche, developpez la connexion
2. Developpez **contactdb** > **Schemas** > **public** > **Tables**
3. Double-cliquez sur **leads** pour voir les donnees
4. Vous pouvez modifier directement dans l'interface

## Etape 7.6 : Exporter/Importer les donnees

### Exporter en CSV

```powershell
# Exporter les leads en CSV
docker exec -it contact-db psql -U postgres -d contactdb -c "COPY leads TO STDOUT WITH CSV HEADER" > leads_export.csv
```

### Creer un backup SQL

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

# PARTIE 8 : GERER LES VOLUMES

## Etape 8.1 : Comprendre les volumes

Les **volumes** stockent les donnees de facon persistante.
Sans volume, les donnees sont perdues quand le container est supprime.

## Etape 8.2 : Via Docker Desktop

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
| **Delete** | Supprimer le volume (PERTE DE DONNEES!) |

## Etape 8.3 : Via ligne de commande

### Lister les volumes

```powershell
docker volume ls
```

Resultat :

```
DRIVER    VOLUME NAME
local     projet-e-contact-backend_postgres_data
```

### Inspecter un volume

```powershell
docker volume inspect projet-e-contact-backend_postgres_data
```

Resultat :

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
# Supprimer un volume specifique (ATTENTION!)
docker volume rm projet-e-contact-backend_postgres_data

# Supprimer les volumes non utilises
docker volume prune
```

---

# PARTIE 9 : VOIR LES LOGS

## Etape 9.1 : Via Docker Desktop

1. Cliquez sur le container souhaite
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

## Etape 9.2 : Via ligne de commande

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
# Voir les 50 dernieres lignes
docker logs contact-api --tail 50

# Suivre les logs en temps reel
docker logs contact-api -f

# Suivre les 20 dernieres lignes en temps reel
docker logs contact-api --tail 20 -f

# Voir les logs avec timestamp
docker logs contact-api -t
```

### Logs de tous les services (Docker Compose)

```powershell
# Voir tous les logs
docker compose logs

# Suivre tous les logs en temps reel
docker compose logs -f

# Logs d'un service specifique
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

# PARTIE 10 : RESOUDRE LES PROBLEMES

## Probleme 1 : Docker ne demarre pas

### Symptome
```
error during connect: This error may indicate that the docker daemon is not running
```

### Solution

1. Ouvrez Docker Desktop
2. Attendez que l'icone devienne verte
3. Si ca ne fonctionne pas, redemarrez Docker Desktop
4. En dernier recours, redemarrez Windows

## Probleme 2 : Port deja utilise

### Symptome
```
Error response from daemon: Ports are not available: exposing port TCP 0.0.0.0:8080
```

### Solution

```powershell
# Trouver le processus qui utilise le port 8080
netstat -ano | findstr :8080

# Resultat exemple :
# TCP    0.0.0.0:8080    0.0.0.0:0    LISTENING    12345

# Tuer le processus (remplacez 12345 par le PID trouve)
taskkill /PID 12345 /F
```

## Probleme 3 : Container ne demarre pas

### Symptome
```
Container contact-api exited with code 1
```

### Solution

```powershell
# Voir les logs pour comprendre l'erreur
docker logs contact-api

# Verifier la configuration
docker compose config

# Reconstruire l'image
docker compose up --build -d
```

## Probleme 4 : Pas de connexion a la base de donnees

### Symptome
```
Connection refused: localhost:5432
```

### Solution

```powershell
# Verifier que PostgreSQL tourne
docker ps | findstr postgres

# Verifier les logs de PostgreSQL
docker logs contact-db

# Redemarrer PostgreSQL
docker restart contact-db
```

## Probleme 5 : Espace disque plein

### Symptome
```
no space left on device
```

### Solution

```powershell
# Voir l'utilisation de Docker
docker system df

# Nettoyer tout ce qui n'est pas utilise
docker system prune -a

# Nettoyer aussi les volumes (ATTENTION: perte de donnees!)
docker system prune -a --volumes
```

## Probleme 6 : Image ne se reconstruit pas

### Symptome
Les modifications du code ne sont pas prises en compte.

### Solution

```powershell
# Forcer la reconstruction sans cache
docker compose build --no-cache

# Ou tout arreter et relancer proprement
docker compose down
docker compose up --build -d
```

---

# RESUME DES COMMANDES ESSENTIELLES

## Commandes quotidiennes

```powershell
# Lancer le projet
docker compose up -d

# Lancer avec rebuild
docker compose up --build -d

# Arreter le projet
docker compose down

# Voir les containers
docker ps

# Voir les logs
docker compose logs -f
```

## Acces base de donnees

```powershell
# Entrer dans PostgreSQL
docker exec -it contact-db psql -U postgres -d contactdb

# Requete rapide
docker exec -it contact-db psql -U postgres -d contactdb -c "SELECT * FROM leads;"
```

## Maintenance

```powershell
# Nettoyer Docker
docker system prune -a

# Backup base de donnees
docker exec contact-db pg_dump -U postgres contactdb > backup.sql

# Redemarrer un service
docker restart contact-api
```

---

# AIDE-MEMOIRE VISUEL

```
+------------------------------------------------------------------+
|                    WORKFLOW DOCKER DESKTOP                        |
+------------------------------------------------------------------+
|                                                                   |
|  1. LANCER                                                        |
|     PowerShell> cd C:\projet                                      |
|     PowerShell> docker compose up --build -d                      |
|                                                                   |
|  2. VERIFIER                                                      |
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
|  5. BASE DE DONNEES                                               |
|     PowerShell> docker exec -it contact-db psql -U postgres -d    |
|                 contactdb                                         |
|                                                                   |
|  6. ARRETER                                                       |
|     PowerShell> docker compose down                               |
|                                                                   |
+------------------------------------------------------------------+
```

Ce guide couvre tout ce dont vous avez besoin pour utiliser Docker Desktop sur Windows.

