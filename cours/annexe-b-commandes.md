# Annexe B - Commandes utiles

## Maven

```bash
# Compiler le projet
mvn compile

# Executer les tests
mvn test

# Creer le package JAR
mvn package

# Creer le JAR sans tests
mvn package -DskipTests

# Nettoyer et construire
mvn clean install

# Demarrer l'application
mvn spring-boot:run

# Demarrer avec un profil
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Voir l'arbre des dependances
mvn dependency:tree
```

## Java

```bash
# Executer le JAR
java -jar app.jar

# Avec un profil
java -jar app.jar --spring.profiles.active=prod

# Avec plus de memoire
java -Xmx512m -jar app.jar

# Avec des variables
java -jar app.jar --server.port=9090
```

## Docker

```bash
# Images
docker images                          # Lister les images
docker pull postgres:15-alpine         # Telecharger une image
docker build -t monapp:1.0 .           # Construire une image
docker rmi monapp:1.0                  # Supprimer une image

# Conteneurs
docker ps                              # Conteneurs en cours
docker ps -a                           # Tous les conteneurs
docker run -d --name db postgres       # Demarrer un conteneur
docker stop db                         # Arreter
docker start db                        # Redemarrer
docker rm db                           # Supprimer
docker logs db                         # Voir les logs
docker logs -f db                      # Suivre les logs
docker exec -it db bash                # Entrer dans le conteneur

# Nettoyage
docker system prune -a                 # Tout nettoyer
docker container prune                 # Conteneurs arretes
docker image prune                     # Images non utilisees
docker volume prune                    # Volumes non utilises
```

## Docker Compose

```bash
# Demarrer les services
docker compose up -d

# Demarrer avec rebuild
docker compose up -d --build

# Arreter les services
docker compose down

# Arreter et supprimer les volumes
docker compose down -v

# Voir les logs
docker compose logs

# Logs d'un service
docker compose logs api

# Suivre les logs
docker compose logs -f api

# Etat des services
docker compose ps

# Executer une commande
docker compose exec api bash
docker compose exec db psql -U postgres
```

## Git

```bash
# Cloner un repo
git clone https://github.com/user/repo.git

# Etat du repo
git status

# Ajouter des fichiers
git add .
git add fichier.java

# Commit
git commit -m "Message"

# Pousser
git push
git push origin main

# Tirer
git pull

# Branches
git branch                    # Lister
git checkout -b feature       # Creer et changer
git checkout main             # Changer
git merge feature             # Fusionner
```

## PostgreSQL (psql)

```bash
# Connexion
psql -U postgres -d contact_db

# Commandes psql
\l                    # Lister les bases
\c contact_db         # Se connecter a une base
\dt                   # Lister les tables
\d leads              # Decrire une table
\q                    # Quitter
```

```sql
-- Requetes SQL
SELECT * FROM leads;
SELECT COUNT(*) FROM leads WHERE status = 'NEW';
UPDATE leads SET status = 'CONTACTED' WHERE id = 1;
DELETE FROM leads WHERE id = 1;
```

## cURL (tester l'API)

```bash
# GET
curl http://localhost:8080/api/leads

# GET avec header
curl -H "Authorization: Bearer TOKEN" http://localhost:8080/api/admin/leads

# POST avec JSON
curl -X POST http://localhost:8080/api/contact \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Jean","email":"jean@ex.com","requestType":"INFO","message":"Test"}'

# PUT
curl -X PUT http://localhost:8080/api/leads/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{"status":"CONTACTED"}'

# DELETE
curl -X DELETE http://localhost:8080/api/admin/leads/1 \
  -H "Authorization: Bearer TOKEN"
```

## Variables d'environnement

### Windows (PowerShell)

```powershell
$env:DB_PASSWORD = "secret"
$env:SPRING_PROFILES_ACTIVE = "prod"
```

### Linux/Mac

```bash
export DB_PASSWORD=secret
export SPRING_PROFILES_ACTIVE=prod
```

### Fichier .env

```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=contact_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
MAIL_HOST=mailhog
MAIL_PORT=1025
JWT_SECRET=ma-cle-secrete
```

