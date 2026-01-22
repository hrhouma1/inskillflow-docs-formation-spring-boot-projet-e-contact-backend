# Chapitre 12.1 - Introduction a Docker

## Objectifs du chapitre

- Comprendre Docker et la conteneurisation
- Connaitre les concepts de base
- Preparer l'environnement

---

## 1. Qu'est-ce que Docker?

### Definition

**Docker** est une plateforme de conteneurisation qui permet d'empaqueter une application avec toutes ses dependances dans un conteneur isolé.

### Probleme resolu

"Ca marche sur ma machine!"

```
Developpeur A:                 Developpeur B:
- Java 17                      - Java 11
- PostgreSQL 15                - PostgreSQL 13
- Ubuntu                       - Windows

-> Resultat: Comportements differents!
```

Avec Docker:

```
Developpeur A:                 Developpeur B:
- Docker                       - Docker
- Meme image                   - Meme image

-> Resultat: Comportement identique!
```

---

## 2. Concepts cles

### Image

Un **template** en lecture seule contenant l'application et ses dependances.

```
Image = Recette de cuisine
- Base: Ubuntu/Alpine
- + Java 17
- + Application JAR
- + Configuration
```

### Conteneur

Une **instance** en cours d'execution d'une image.

```
Conteneur = Plat cuisine (a partir de la recette)
- Instance d'une image
- Isole du systeme hote
- A son propre systeme de fichiers
```

### Dockerfile

Fichier de **definition** pour construire une image.

```dockerfile
FROM eclipse-temurin:17-jre
COPY app.jar /app/app.jar
CMD ["java", "-jar", "/app/app.jar"]
```

### Registry

**Depot** d'images (Docker Hub, GitHub Container Registry...).

```
docker pull postgres:15-alpine    # Telecharge depuis Docker Hub
docker push monrepo/monimage:1.0  # Pousse vers un registry
```

---

## 3. Docker vs VM

| Aspect | Machine Virtuelle | Docker |
|--------|-------------------|--------|
| Isolation | Complete (OS) | Processus |
| Demarrage | Minutes | Secondes |
| Taille | Go | Mo |
| Performance | Overhead | Native |
| Portabilite | Limitee | Excellente |

```
VM:                           Docker:
+---------------------------+ +---------------------------+
|  App A   |  App B   |     | |  App A   |  App B   |     |
+----------+----------+     | +----------+----------+     |
|  Guest   |  Guest   |     | |   Bins   |   Bins   |     |
|   OS     |   OS     |     | |   Libs   |   Libs   |     |
+----------+----------+     | +----------+----------+     |
|    Hypervisor        |     | |    Docker Engine     |     |
+----------------------+     | +----------------------+     |
|      Host OS         |     | |      Host OS         |     |
+----------------------+     | +----------------------+     |
```

---

## 4. Installation

### Windows

1. Telecharger Docker Desktop: https://docker.com/products/docker-desktop
2. Installer
3. Redemarrer
4. Verifier: `docker --version`

### Linux

```bash
# Ubuntu
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
# Deconnexion/reconnexion requise

docker --version
```

### Verification

```bash
docker run hello-world
```

---

## 5. Commandes essentielles

### Images

```bash
# Lister les images
docker images

# Telecharger une image
docker pull postgres:15-alpine

# Supprimer une image
docker rmi postgres:15-alpine

# Construire une image
docker build -t monapp:1.0 .
```

### Conteneurs

```bash
# Lister les conteneurs (en cours)
docker ps

# Lister tous les conteneurs
docker ps -a

# Demarrer un conteneur
docker run -d --name mondb postgres:15-alpine

# Arreter un conteneur
docker stop mondb

# Demarrer un conteneur arrete
docker start mondb

# Supprimer un conteneur
docker rm mondb

# Logs d'un conteneur
docker logs mondb
docker logs -f mondb  # Suivre en temps reel

# Executer une commande dans un conteneur
docker exec -it mondb bash
docker exec -it mondb psql -U postgres
```

### Nettoyage

```bash
# Supprimer les conteneurs arretes
docker container prune

# Supprimer les images non utilisees
docker image prune

# Tout nettoyer
docker system prune -a
```

---

## 6. Options docker run

### Options courantes

```bash
docker run [OPTIONS] IMAGE [COMMAND]

# -d : Detached (arriere-plan)
docker run -d postgres

# --name : Nom du conteneur
docker run --name mondb postgres

# -p : Mapping de ports (hote:conteneur)
docker run -p 5432:5432 postgres

# -e : Variable d'environnement
docker run -e POSTGRES_PASSWORD=secret postgres

# -v : Volume (persistance)
docker run -v pgdata:/var/lib/postgresql/data postgres

# --rm : Supprimer apres arret
docker run --rm postgres

# -it : Interactive + TTY
docker run -it ubuntu bash
```

### Exemple complet

```bash
docker run -d \
  --name contact-db \
  -p 5432:5432 \
  -e POSTGRES_DB=contact_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -v postgres_data:/var/lib/postgresql/data \
  postgres:15-alpine
```

---

## 7. Volumes

### Types de volumes

```bash
# Volume nomme (recommande)
docker run -v monvolume:/data postgres

# Bind mount (dossier local)
docker run -v /chemin/local:/data postgres

# Volume anonyme
docker run -v /data postgres
```

### Gestion des volumes

```bash
# Lister les volumes
docker volume ls

# Creer un volume
docker volume create monvolume

# Supprimer un volume
docker volume rm monvolume

# Inspecter un volume
docker volume inspect monvolume
```

---

## 8. Reseaux

### Reseaux par defaut

```bash
# Lister les reseaux
docker network ls

# bridge : Reseau par defaut
# host : Partage le reseau de l'hote
# none : Pas de reseau
```

### Creer un reseau

```bash
docker network create monreseau

docker run --network monreseau --name app1 myapp
docker run --network monreseau --name app2 myapp

# app1 peut atteindre app2 par son nom
```

---

## 9. Workflow typique

```bash
# 1. Ecrire le Dockerfile
# 2. Construire l'image
docker build -t monapp:1.0 .

# 3. Tester localement
docker run -p 8080:8080 monapp:1.0

# 4. Pousser vers un registry
docker tag monapp:1.0 registry.example.com/monapp:1.0
docker push registry.example.com/monapp:1.0

# 5. Deployer en production
docker pull registry.example.com/monapp:1.0
docker run -d monapp:1.0
```

---

## 10. Points cles a retenir

1. **Image** = template, **Conteneur** = instance
2. **Dockerfile** definit comment construire l'image
3. **docker run** cree et demarre un conteneur
4. **Volumes** pour la persistance des donnees
5. **docker-compose** pour orchestrer plusieurs conteneurs

---

## QUIZ 12.1 - Introduction a Docker

**1. Quelle est la difference entre une image et un conteneur?**
   - a) Aucune
   - b) Image = template, Conteneur = instance en cours
   - c) Conteneur = template, Image = instance
   - d) Image est plus leger

**2. Quel fichier definit comment construire une image?**
   - a) docker.yml
   - b) Containerfile
   - c) Dockerfile
   - d) image.config

**3. Quelle commande liste les conteneurs en cours?**
   - a) docker list
   - b) docker ps
   - c) docker containers
   - d) docker running

**4. Comment mapper le port 8080 du conteneur vers 3000 de l'hote?**
   - a) -p 8080:3000
   - b) -p 3000:8080
   - c) --port 8080=3000
   - d) -m 3000:8080

**5. VRAI ou FAUX: Les donnees dans un conteneur sont persistantes par defaut.**

**6. Quelle option pour executer en arriere-plan?**
   - a) -b
   - b) -d
   - c) --background
   - d) --daemon

**7. Comment voir les logs d'un conteneur?**
   - a) docker output monconteneur
   - b) docker logs monconteneur
   - c) docker print monconteneur
   - d) docker show monconteneur

**8. Completez: Un _______ permet de persister les donnees entre les redemarrages.**

**9. Quelle commande pour entrer dans un conteneur?**
   - a) docker enter
   - b) docker exec -it
   - c) docker shell
   - d) docker connect

**10. Que fait docker-compose?**
   - a) Compose des images
   - b) Orchestre plusieurs conteneurs
   - c) Compile le code
   - d) Cree des reseaux

---

### REPONSES QUIZ 12.1

1. b) Image = template, Conteneur = instance en cours
2. c) Dockerfile
3. b) docker ps
4. b) -p 3000:8080 (hote:conteneur)
5. FAUX (il faut des volumes)
6. b) -d
7. b) docker logs monconteneur
8. volume
9. b) docker exec -it
10. b) Orchestre plusieurs conteneurs

