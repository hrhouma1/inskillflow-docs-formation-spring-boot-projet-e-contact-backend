# Contact Form API

API REST Spring Boot pour formulaire de contact et gestion de leads.

## Fonctionnalités

- **Formulaire de contact public** - Endpoint accessible sans authentification
- **Gestion des leads** - CRUD complet pour les admins
- **Envoi d'emails** - Notification admin + confirmation visiteur
- **Authentification JWT** - Sécurité pour les endpoints admin
- **Docker** - Prêt pour le déploiement

## Prérequis

- Java 17+
- Maven 3.8+
- Docker & Docker Compose (optionnel)

## Démarrage rapide

### Option 1: Avec Docker (recommandé)

```bash
# Démarrer tous les services
docker-compose up -d

# L'API est disponible sur http://localhost:8080
# MailHog est disponible sur http://localhost:8025
# Swagger UI est disponible sur http://localhost:8080/swagger-ui.html
```

### Option 2: Sans Docker (développement)

```bash
# Démarrer MailHog pour les emails (optionnel)
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog

# Démarrer l'application
./mvnw spring-boot:run
```

## Tester l'API

### Option 1: Swagger UI (Interface graphique)

Ouvrez http://localhost:8080/swagger-ui.html dans votre navigateur.

1. Testez d'abord `POST /api/contact` (pas besoin de token)
2. Utilisez `POST /api/auth/login` pour obtenir un token
3. Cliquez sur "Authorize" et entrez le token
4. Testez les endpoints admin

### Option 2: Fichier .http (VS Code)

1. Installez l'extension "REST Client" dans VS Code
2. Ouvrez le fichier `api-tests.http`
3. Cliquez sur "Send Request" au-dessus de chaque requête

### Option 3: cURL (Terminal)

Voir les exemples ci-dessous.

## Endpoints

### Public (sans authentification)

| Méthode | URL | Description |
|---------|-----|-------------|
| POST | `/api/contact` | Soumettre le formulaire |

### Authentification

| Méthode | URL | Description |
|---------|-----|-------------|
| POST | `/api/auth/login` | Se connecter (retourne JWT) |

### Admin (JWT requis)

| Méthode | URL | Description |
|---------|-----|-------------|
| GET | `/api/admin/leads` | Liste des leads |
| GET | `/api/admin/leads/{id}` | Détail d'un lead |
| PUT | `/api/admin/leads/{id}/status` | Changer le statut |
| DELETE | `/api/admin/leads/{id}` | Supprimer |
| GET | `/api/admin/leads/stats` | Statistiques |

## Exemples d'utilisation

### 1. Soumettre un formulaire de contact

```bash
curl -X POST http://localhost:8080/api/contact \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Marie Tremblay",
    "company": "ABC Inc.",
    "email": "marie@example.com",
    "phone": "514-555-1234",
    "requestType": "QUOTE",
    "message": "Bonjour, je voudrais un devis pour 10 personnes."
  }'
```

### 2. Se connecter (admin)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "admin123"
  }'
```

### 3. Lister les leads (avec token)

```bash
curl http://localhost:8080/api/admin/leads \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

## Admin par défaut

- **Email:** admin@example.com
- **Password:** admin123

## Types de demande

- `INFO` - Demande d'information
- `QUOTE` - Demande de devis
- `SUPPORT` - Support technique
- `PARTNERSHIP` - Partenariat
- `OTHER` - Autre

## Statuts des leads

- `NEW` - Nouveau
- `CONTACTED` - Contacté
- `CONVERTED` - Converti en client
- `LOST` - Perdu

## Structure du projet

```
src/main/java/com/example/contact/
├── ContactApplication.java
├── config/
│   ├── SecurityConfig.java
│   └── DataInitializer.java
├── controller/
│   ├── ContactController.java
│   ├── LeadController.java
│   └── AuthController.java
├── dto/
│   ├── request/
│   └── response/
├── exception/
├── model/
│   ├── Lead.java
│   └── User.java
├── repository/
├── security/
│   ├── JwtService.java
│   └── JwtAuthFilter.java
└── service/
    ├── LeadService.java
    └── EmailService.java
```

## Configuration

### Variables d'environnement (production)

| Variable | Description | Défaut |
|----------|-------------|--------|
| `DB_HOST` | Hôte PostgreSQL | localhost |
| `DB_PORT` | Port PostgreSQL | 5432 |
| `DB_NAME` | Nom de la BD | contactdb |
| `DB_USER` | Utilisateur BD | postgres |
| `DB_PASSWORD` | Mot de passe BD | postgres |
| `MAIL_HOST` | Serveur SMTP | smtp.gmail.com |
| `MAIL_PORT` | Port SMTP | 587 |
| `MAIL_USER` | Email SMTP | - |
| `MAIL_PASSWORD` | Password SMTP | - |
| `ADMIN_EMAIL` | Email admin | admin@example.com |
| `JWT_SECRET` | Clé JWT (base64) | - |
| `JWT_EXPIRATION` | Expiration JWT (ms) | 86400000 |

## Utilisation avec un frontend

Cette API peut être utilisée avec n'importe quel frontend:
- React
- Vue.js
- Angular
- HTML/JavaScript vanilla
- Mobile (React Native, Flutter)

### Exemple avec fetch (JavaScript)

```javascript
// Soumettre le formulaire
const response = await fetch('http://localhost:8080/api/contact', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    fullName: 'Marie Tremblay',
    email: 'marie@example.com',
    requestType: 'INFO',
    message: 'Je voudrais plus d\'informations...'
  }),
});

const data = await response.json();
console.log(data.message); // "Merci! Votre message a été envoyé..."
```

## Licence

MIT

