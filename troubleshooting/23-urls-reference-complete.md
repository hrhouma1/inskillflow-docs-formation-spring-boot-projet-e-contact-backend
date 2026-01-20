# 23 - Référence Complète des URLs

## Docker Desktop (Local)

### Services principaux

| Service | URL | Port |
|---------|-----|------|
| Swagger UI | http://localhost:8080/swagger-ui.html | 8080 |
| OpenAPI JSON | http://localhost:8080/v3/api-docs | 8080 |
| MailHog | http://localhost:8025 | 8025 |
| Frontend (Docker full) | http://localhost | 80 |
| Frontend (Python) | http://localhost:3000 | 3000 |
| PostgreSQL | localhost:5432 | 5432 |
| Console H2 (dev) | http://localhost:8080/h2-console | 8080 |

### Endpoints API

| Endpoint | Méthode | Auth | Description |
|----------|---------|------|-------------|
| /api/contact | POST | Non | Soumettre un contact |
| /api/auth/register | POST | Non | Créer un admin |
| /api/auth/login | POST | Non | Obtenir un token JWT |
| /api/admin/leads | GET | JWT | Liste des leads |
| /api/admin/leads/{id} | GET | JWT | Détail d'un lead |
| /api/admin/leads/{id}/status | PUT | JWT | Modifier le statut |
| /api/admin/leads/{id} | DELETE | JWT | Supprimer un lead |
| /api/admin/stats | GET | JWT | Statistiques |

### URLs complètes API (local)

```
POST   http://localhost:8080/api/contact
POST   http://localhost:8080/api/auth/register
POST   http://localhost:8080/api/auth/login
GET    http://localhost:8080/api/admin/leads
GET    http://localhost:8080/api/admin/leads/1
PUT    http://localhost:8080/api/admin/leads/1/status
DELETE http://localhost:8080/api/admin/leads/1
GET    http://localhost:8080/api/admin/stats
```

---

## GitHub Codespaces

### Format d'URL

```
https://{CODESPACE_NAME}-{PORT}.app.github.dev{PATH}
```

### Exemple avec Codespace réel

Nom du Codespace : `refactored-space-funicular-7vr6459xgwprhr75j`

| Service | URL |
|---------|-----|
| Swagger UI | https://refactored-space-funicular-7vr6459xgwprhr75j-8080.app.github.dev/swagger-ui.html |
| OpenAPI JSON | https://refactored-space-funicular-7vr6459xgwprhr75j-8080.app.github.dev/v3/api-docs |
| MailHog | https://refactored-space-funicular-7vr6459xgwprhr75j-8025.app.github.dev |
| Frontend (80) | https://refactored-space-funicular-7vr6459xgwprhr75j-80.app.github.dev |
| Frontend (3000) | https://refactored-space-funicular-7vr6459xgwprhr75j-3000.app.github.dev |

### Endpoints API (Codespaces)

```
POST   https://refactored-space-funicular-7vr6459xgwprhr75j-8080.app.github.dev/api/contact
POST   https://refactored-space-funicular-7vr6459xgwprhr75j-8080.app.github.dev/api/auth/register
POST   https://refactored-space-funicular-7vr6459xgwprhr75j-8080.app.github.dev/api/auth/login
GET    https://refactored-space-funicular-7vr6459xgwprhr75j-8080.app.github.dev/api/admin/leads
GET    https://refactored-space-funicular-7vr6459xgwprhr75j-8080.app.github.dev/api/admin/leads/1
PUT    https://refactored-space-funicular-7vr6459xgwprhr75j-8080.app.github.dev/api/admin/leads/1/status
DELETE https://refactored-space-funicular-7vr6459xgwprhr75j-8080.app.github.dev/api/admin/leads/1
GET    https://refactored-space-funicular-7vr6459xgwprhr75j-8080.app.github.dev/api/admin/stats
```

---

## Trouver votre nom de Codespace

### Terminal

```bash
echo $CODESPACE_NAME
```

### Script pour afficher toutes les URLs

```bash
#!/bin/bash
echo "=========================================="
echo "SWAGGER UI"
echo "https://${CODESPACE_NAME}-8080.app.github.dev/swagger-ui.html"
echo ""
echo "MAILHOG"
echo "https://${CODESPACE_NAME}-8025.app.github.dev"
echo ""
echo "API CONTACT"
echo "https://${CODESPACE_NAME}-8080.app.github.dev/api/contact"
echo ""
echo "API AUTH"
echo "https://${CODESPACE_NAME}-8080.app.github.dev/api/auth/login"
echo ""
echo "API ADMIN"
echo "https://${CODESPACE_NAME}-8080.app.github.dev/api/admin/leads"
echo ""
echo "FRONTEND (80)"
echo "https://${CODESPACE_NAME}-80.app.github.dev"
echo ""
echo "FRONTEND (3000)"
echo "https://${CODESPACE_NAME}-3000.app.github.dev"
echo "=========================================="
```

---

## Comparaison Local vs Codespaces

| Service | Local | Codespaces |
|---------|-------|------------|
| Protocole | http:// | https:// |
| Swagger | localhost:8080/swagger-ui.html | CODESPACE-8080.app.github.dev/swagger-ui.html |
| MailHog | localhost:8025 | CODESPACE-8025.app.github.dev |
| API | localhost:8080/api/contact | CODESPACE-8080.app.github.dev/api/contact |
| Frontend | localhost:80 | CODESPACE-80.app.github.dev |

---

## Ports utilisés

| Port | Service | Protocole |
|------|---------|-----------|
| 80 | Frontend (Nginx) | HTTP |
| 3000 | Frontend (Python dev) | HTTP |
| 5432 | PostgreSQL | TCP |
| 8025 | MailHog (Web UI) | HTTP |
| 1025 | MailHog (SMTP) | SMTP |
| 8080 | Spring Boot API | HTTP |

---

## Copier-Coller rapide

### Local - Swagger
```
http://localhost:8080/swagger-ui.html
```

### Local - MailHog
```
http://localhost:8025
```

### Local - API Contact
```
http://localhost:8080/api/contact
```

### Local - API Login
```
http://localhost:8080/api/auth/login
```

### Local - API Leads
```
http://localhost:8080/api/admin/leads
```

---

## Tester avec curl

### Créer un contact (local)

```bash
curl -X POST http://localhost:8080/api/contact \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test","email":"test@test.com","requestType":"INFO","message":"Test"}'
```

### Créer un contact (Codespaces)

```bash
curl -X POST https://${CODESPACE_NAME}-8080.app.github.dev/api/contact \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test","email":"test@test.com","requestType":"INFO","message":"Test"}'
```

### Se connecter (local)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"admin123"}'
```

### Voir les leads (avec token)

```bash
curl http://localhost:8080/api/admin/leads \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

---

## Aide-mémoire

```
+------------------------------------------------------------------+
|                         URLS LOCALES                              |
+------------------------------------------------------------------+
|  Swagger    : http://localhost:8080/swagger-ui.html               |
|  MailHog    : http://localhost:8025                               |
|  API        : http://localhost:8080/api/contact                   |
|  Frontend   : http://localhost                                    |
+------------------------------------------------------------------+

+------------------------------------------------------------------+
|                      URLS CODESPACES                              |
+------------------------------------------------------------------+
|  Format : https://{CODESPACE}-{PORT}.app.github.dev               |
|                                                                   |
|  Swagger    : https://CODESPACE-8080.app.github.dev/swagger-ui    |
|  MailHog    : https://CODESPACE-8025.app.github.dev               |
|  API        : https://CODESPACE-8080.app.github.dev/api/contact   |
|  Frontend   : https://CODESPACE-80.app.github.dev                 |
+------------------------------------------------------------------+
```

