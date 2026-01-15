# Guide de Déploiement sur GitHub Codespaces

Ce guide explique comment exécuter le projet **Contact Form API** sur GitHub Codespaces.

## 🚀 Démarrage rapide

### 1. Ouvrir dans Codespaces

1. Cliquez sur le bouton **Code** sur GitHub
2. Sélectionnez l'onglet **Codespaces**
3. Cliquez sur **Create codespace on main**

### 2. Lancer l'application

```bash
# Démarrer tous les services
docker-compose up -d

# Vérifier que tout fonctionne
docker ps
```

### 3. Vérifier les logs

```bash
docker logs contact-api -f
```

Vous devriez voir :
```
Started ContactApplication in X seconds
```

## 📦 Services déployés

| Service | Port interne | Description |
|---------|--------------|-------------|
| **contact-api** | 8080 | API Spring Boot |
| **contact-db** | 5432 | PostgreSQL 15 |
| **contact-mailhog** | 1025 / 8025 | Serveur SMTP de test |

## 🌐 Accéder aux URLs

### Dans Codespaces

Les ports sont automatiquement exposés. Allez dans l'onglet **PORTS** en bas de VS Code pour voir les URLs publiques.

| Service | Port | URL locale |
|---------|------|------------|
| API | 8080 | `http://localhost:8080` |
| Swagger UI | 8080 | `http://localhost:8080/swagger-ui.html` |
| MailHog UI | 8025 | `http://localhost:8025` |

### Rendre un port public

1. Clic droit sur le port dans l'onglet **PORTS**
2. Sélectionnez **Port Visibility** → **Public**

## ⚙️ Variables d'environnement

### Avec Docker Compose (par défaut)

Les variables sont **déjà configurées** dans `docker-compose.yml` :

```yaml
environment:
  SPRING_PROFILES_ACTIVE: prod
  DB_HOST: postgres
  DB_PORT: 5432
  DB_NAME: contactdb
  DB_USER: postgres
  DB_PASSWORD: postgres
  MAIL_HOST: mailhog
  MAIL_PORT: 1025
  ADMIN_EMAIL: admin@example.com
  JWT_SECRET: dGhpcy1pcy1hLXZlcnktbG9uZy1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tZ2VuZXJhdGlvbi0yNTY=
  JWT_EXPIRATION: 86400000
```

**Vous n'avez rien à configurer manuellement !**

### Sans Docker (développement local)

Si vous voulez exécuter sans Docker avec `./mvnw spring-boot:run`, le profil **dev** sera utilisé automatiquement :

- Base de données : H2 (en mémoire)
- Emails : localhost:1025 (MailHog)

Pour lancer MailHog seul :
```bash
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog
```

### Secrets Codespaces (optionnel)

Pour stocker des secrets de production, utilisez les **Codespaces Secrets** :

1. Allez dans **Settings** → **Codespaces** → **Secrets**
2. Ajoutez vos secrets :

| Secret | Description |
|--------|-------------|
| `JWT_SECRET` | Clé secrète JWT (base64) |
| `DB_PASSWORD` | Mot de passe PostgreSQL |
| `MAIL_PASSWORD` | Mot de passe SMTP |

## 🧪 Tester l'API

### 1. Test du formulaire de contact (public)

```bash
curl -X POST http://localhost:8080/api/contact \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Marie Tremblay",
    "company": "ABC Inc.",
    "email": "marie@example.com",
    "phone": "514-555-1234",
    "requestType": "QUOTE",
    "message": "Bonjour, je voudrais un devis."
  }'
```

Réponse attendue :
```json
{
  "message": "Merci! Votre message a été envoyé. Nous vous répondrons bientôt."
}
```

### 2. Connexion admin

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "admin123"
  }'
```

Réponse :
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "admin@example.com",
  "expiresIn": 86400000
}
```

### 3. Lister les leads (avec token)

```bash
curl http://localhost:8080/api/admin/leads \
  -H "Authorization: Bearer VOTRE_TOKEN_ICI"
```

### 4. Vérifier les emails

Ouvrez **MailHog** : http://localhost:8025

Vous verrez :
- Email de notification à l'admin
- Email de confirmation au visiteur

## 🔄 Commandes utiles

### Gestion des conteneurs

```bash
# Démarrer
docker-compose up -d

# Arrêter
docker-compose down

# Reconstruire (après modification du code)
docker-compose up --build -d

# Voir les logs
docker logs contact-api -f

# Voir tous les logs
docker-compose logs -f
```

### Accès à la base de données

```bash
# Se connecter à PostgreSQL
docker exec -it contact-db psql -U postgres -d contactdb

# Voir les leads
SELECT * FROM leads;

# Voir les utilisateurs
SELECT * FROM users;
```

### Nettoyage

```bash
# Supprimer les conteneurs et volumes
docker-compose down -v

# Supprimer les images
docker-compose down --rmi all
```

## 🐛 Troubleshooting

### L'API ne démarre pas

```bash
# Vérifier les logs
docker logs contact-api

# Reconstruire l'image
docker-compose up --build -d
```

### Port déjà utilisé

```bash
# Voir ce qui utilise le port
lsof -i :8080

# Arrêter tous les conteneurs
docker-compose down
```

### Base de données non accessible

```bash
# Vérifier l'état de PostgreSQL
docker logs contact-db

# Redémarrer
docker-compose restart postgres
```

## 📚 Documentation

- **Swagger UI** : http://localhost:8080/swagger-ui.html
- **API Docs (JSON)** : http://localhost:8080/v3/api-docs
- **README principal** : [README.md](README.md)
- **Troubleshooting** : [troubleshooting/](troubleshooting/)

## 🔐 Credentials par défaut

| Service | Utilisateur | Mot de passe |
|---------|-------------|--------------|
| Admin API | admin@example.com | admin123 |
| PostgreSQL | postgres | postgres |
| H2 Console (dev) | sa | (vide) |

