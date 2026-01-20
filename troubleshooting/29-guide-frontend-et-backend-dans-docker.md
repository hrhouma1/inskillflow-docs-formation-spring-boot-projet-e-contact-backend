# 29 - Guide : Frontend et Backend dans Docker (même réseau)

## Scénario

Vous avez :
- Un backend Spring Boot dans Docker
- Un frontend (React, Next.js, Svelte, HTML) dans Docker
- Les deux doivent communiquer

---

# PARTIE 1 : ARCHITECTURE

## Architecture avec Nginx (recommandée)

```
                    Internet
                        |
                        v
              +-----------------+
              |     Nginx       |
              |    Port 80      |
              +--------+--------+
                       |
         +-------------+-------------+
         |                           |
         v                           v
   /api/* proxy              /* fichiers statiques
         |                           |
         v                           v
+-----------------+         +-----------------+
|    Backend      |         |    Frontend     |
| Spring Boot     |         |  (servi par     |
|   Port 8080     |         |    Nginx)       |
+-----------------+         +-----------------+
         |
         v
+-----------------+
|   PostgreSQL    |
|   Port 5432     |
+-----------------+
```

---

# PARTIE 2 : DOCKER-COMPOSE COMPLET

## Fichier : `docker-compose.full.yml`

```yaml
version: '3.8'

services:
  # ===========================================
  # FRONTEND + REVERSE PROXY (Nginx)
  # ===========================================
  frontend:
    image: nginx:alpine
    container_name: contact-frontend
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./frontend:/usr/share/nginx/html:ro
      - ./nginx.conf:/etc/nginx/conf.d/default.conf:ro
    depends_on:
      - api
    restart: unless-stopped
    networks:
      - contact-network

  # ===========================================
  # BACKEND (Spring Boot)
  # ===========================================
  api:
    build: .
    container_name: contact-api
    expose:
      - "8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      # Base de données
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: contactdb
      DB_USER: postgres
      DB_PASSWORD: postgres
      # Gmail SMTP
      MAIL_HOST: smtp.gmail.com
      MAIL_PORT: 587
      MAIL_USER: ${GMAIL_USER}
      MAIL_PASSWORD: ${GMAIL_PASSWORD}
      MAIL_AUTH: true
      MAIL_STARTTLS: true
      # Admin
      ADMIN_EMAIL: ${GMAIL_USER}
      # JWT
      JWT_SECRET: votre-secret-jwt-tres-long-et-securise
      JWT_EXPIRATION: 86400000
    depends_on:
      postgres:
        condition: service_healthy
    restart: unless-stopped
    networks:
      - contact-network

  # ===========================================
  # BASE DE DONNÉES (PostgreSQL)
  # ===========================================
  postgres:
    image: postgres:15-alpine
    container_name: contact-db
    environment:
      POSTGRES_DB: contactdb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped
    networks:
      - contact-network

  # ===========================================
  # MAILHOG (optionnel, pour dev)
  # ===========================================
  mailhog:
    image: mailhog/mailhog:latest
    container_name: contact-mailhog
    ports:
      - "8025:8025"
    restart: unless-stopped
    networks:
      - contact-network

networks:
  contact-network:
    driver: bridge

volumes:
  postgres_data:
```

---

# PARTIE 3 : CONFIGURATION NGINX

## Fichier : `nginx.conf`

```nginx
server {
    listen 80;
    server_name localhost;

    # Fichiers statiques du frontend
    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # Proxy vers le backend API
    location /api/ {
        proxy_pass http://api:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # CORS headers (si nécessaire)
        add_header Access-Control-Allow-Origin *;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS";
        add_header Access-Control-Allow-Headers "Authorization, Content-Type";
        
        # Timeout
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # Proxy vers Swagger UI
    location /swagger-ui/ {
        proxy_pass http://api:8080/swagger-ui/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # Proxy vers OpenAPI docs
    location /v3/api-docs {
        proxy_pass http://api:8080/v3/api-docs;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
    }

    # Gestion des erreurs
    error_page 500 502 503 504 /50x.html;
    location = /50x.html {
        root /usr/share/nginx/html;
    }
}
```

---

# PARTIE 4 : CONFIGURATION DU FRONTEND

## Important : URL relative

Quand le frontend est dans le même Docker que le backend (via Nginx proxy), utilisez des **URLs relatives** :

### Fichier : `frontend/index.html`

```javascript
// IMPORTANT : URL relative (pas de domaine)
const API_URL = '/api/contact';

// PAS ceci :
// const API_URL = 'http://localhost:8080/api/contact';
// const API_URL = 'https://monsite.com/api/contact';
```

## Pourquoi URL relative ?

```
Requête du navigateur : https://monsite.com/api/contact
                              |
                              v
                         Nginx (port 80)
                              |
                    location /api/ {
                        proxy_pass http://api:8080/api/;
                    }
                              |
                              v
                    Backend Spring Boot (port 8080)
```

L'utilisateur accède à `https://monsite.com`, Nginx sert le frontend.
Quand le frontend appelle `/api/contact`, Nginx fait le proxy vers le backend.

---

# PARTIE 5 : STRUCTURE DES FICHIERS

```
projet/
├── docker-compose.full.yml      <- Configuration Docker complète
├── nginx.conf                   <- Configuration Nginx
├── Dockerfile                   <- Build du backend Spring Boot
├── pom.xml
├── src/                         <- Code source backend
│   └── main/
│       └── java/
│           └── ...
├── frontend/                    <- Fichiers du frontend
│   ├── index.html              <- Page principale
│   ├── styles.css              <- Styles (optionnel)
│   └── script.js               <- JavaScript (optionnel)
└── troubleshooting/            <- Documentation
```

---

# PARTIE 6 : LANCER LE PROJET

## Étape 1 : Exporter les variables Gmail (si besoin)

```bash
export GMAIL_USER=votre-email@gmail.com
export GMAIL_PASSWORD=votre-mot-de-passe-app
```

## Étape 2 : Lancer Docker Compose

```bash
docker compose -f docker-compose.full.yml up --build -d
```

## Étape 3 : Vérifier les containers

```bash
docker ps
```

**Résultat attendu :**
```
CONTAINER ID   IMAGE           STATUS          PORTS
xxxx           nginx:alpine    Up 2 minutes    0.0.0.0:80->80/tcp
xxxx           contact-api     Up 2 minutes    8080/tcp
xxxx           postgres:15     Up 2 minutes    5432/tcp
xxxx           mailhog         Up 2 minutes    0.0.0.0:8025->8025/tcp
```

## Étape 4 : Accéder au site

| Service | URL |
|---------|-----|
| Frontend | http://localhost |
| API (via Nginx) | http://localhost/api/contact |
| Swagger UI | http://localhost/swagger-ui/ |
| MailHog | http://localhost:8025 |

---

# PARTIE 7 : FRONTEND REACT/NEXT.JS DANS DOCKER

## Si votre frontend est une application React/Next.js

### Option A : Build statique (recommandé)

1. Buildez votre application React :

```bash
npm run build
```

2. Copiez le dossier `build/` ou `out/` dans `frontend/`

3. Nginx servira les fichiers statiques

### Option B : Container séparé pour le frontend

```yaml
services:
  frontend:
    build:
      context: ./frontend-app
      dockerfile: Dockerfile
    container_name: contact-frontend
    ports:
      - "3000:3000"
    networks:
      - contact-network
```

#### Dockerfile pour React :

```dockerfile
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

#### Dockerfile pour Next.js :

```dockerfile
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM node:18-alpine AS runner
WORKDIR /app
ENV NODE_ENV production
COPY --from=builder /app/next.config.js ./
COPY --from=builder /app/public ./public
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static

EXPOSE 3000
CMD ["node", "server.js"]
```

---

# PARTIE 8 : COMMUNICATION ENTRE CONTAINERS

## Comment ça fonctionne

Dans Docker Compose, les services peuvent se parler par leur nom :

```
frontend  -->  api:8080      (via le nom du service)
api       -->  postgres:5432 (via le nom du service)
```

## Configuration dans le frontend (si container séparé)

Si le frontend est dans un container séparé (pas servi par Nginx), utilisez le nom du service :

```javascript
// Dans le container frontend
const API_URL = 'http://api:8080/api/contact';
```

**Mais attention** : Cette URL fonctionne côté serveur (SSR), pas côté client (navigateur).

Pour le côté client, vous devez :
1. Utiliser Nginx comme reverse proxy (recommandé)
2. Ou exposer l'API sur un port public

---

# PARTIE 9 : EXEMPLE COMPLET AVEC REACT

## Structure du projet

```
projet/
├── docker-compose.yml
├── nginx.conf
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
└── frontend/
    ├── Dockerfile
    ├── package.json
    ├── src/
    │   ├── App.jsx
    │   └── components/
    │       └── ContactForm.jsx
    └── public/
```

## docker-compose.yml

```yaml
version: '3.8'

services:
  nginx:
    image: nginx:alpine
    container_name: nginx-proxy
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/conf.d/default.conf:ro
    depends_on:
      - frontend
      - api
    networks:
      - app-network

  frontend:
    build: ./frontend
    container_name: react-frontend
    expose:
      - "3000"
    networks:
      - app-network

  api:
    build: ./backend
    container_name: spring-api
    expose:
      - "8080"
    environment:
      DB_HOST: postgres
      MAIL_HOST: smtp.gmail.com
      MAIL_USER: ${GMAIL_USER}
      MAIL_PASSWORD: ${GMAIL_PASSWORD}
    depends_on:
      - postgres
    networks:
      - app-network

  postgres:
    image: postgres:15-alpine
    container_name: postgres-db
    environment:
      POSTGRES_DB: contactdb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - app-network

networks:
  app-network:
    driver: bridge

volumes:
  postgres_data:
```

## nginx.conf (pour React + API)

```nginx
server {
    listen 80;

    # Frontend React
    location / {
        proxy_pass http://frontend:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }

    # Backend API
    location /api/ {
        proxy_pass http://api:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

---

# PARTIE 10 : DÉPANNAGE

## Erreur : "Connection refused"

Le frontend ne peut pas atteindre le backend.

**Solution :**
1. Vérifiez que les deux sont sur le même réseau Docker
2. Utilisez le nom du service, pas `localhost`

```yaml
networks:
  - app-network  # Les deux services doivent avoir la même ligne
```

## Erreur : CORS

**Solution :**
Utilisez Nginx comme proxy. Le navigateur voit une seule origine, pas de CORS.

## Erreur : 502 Bad Gateway

Nginx ne peut pas atteindre le backend.

**Solution :**
1. Vérifiez que le backend est démarré : `docker logs contact-api`
2. Vérifiez le nom du service dans `proxy_pass`

## Le frontend affiche l'ancien code

**Solution :**
```bash
docker compose -f docker-compose.full.yml down
docker compose -f docker-compose.full.yml up --build -d
```

---

# RÉSUMÉ

## Points clés

1. **Nginx** sert de reverse proxy et de serveur de fichiers statiques
2. **URL relative** (`/api/contact`) dans le frontend
3. **Même réseau Docker** pour tous les services
4. Les services communiquent par leur **nom** (`api`, `postgres`)

## Commandes essentielles

```bash
# Lancer
docker compose -f docker-compose.full.yml up --build -d

# Arrêter
docker compose -f docker-compose.full.yml down

# Logs
docker compose -f docker-compose.full.yml logs -f

# Reconstruire
docker compose -f docker-compose.full.yml up --build -d
```

## URLs d'accès

| Service | URL |
|---------|-----|
| Frontend | http://localhost |
| API | http://localhost/api/contact |
| Swagger | http://localhost/swagger-ui/ |

