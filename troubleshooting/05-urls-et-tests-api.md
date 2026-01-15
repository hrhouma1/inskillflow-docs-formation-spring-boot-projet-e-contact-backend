# 05 - URLs et Tests de l'API

## 🌐 URLs principales

### Environnement local / Codespaces

| Service | URL | Description |
|---------|-----|-------------|
| 🚀 API | http://localhost:8080 | API REST principale |
| 📘 Swagger UI | http://localhost:8080/swagger-ui.html | Documentation interactive |
| 📄 OpenAPI JSON | http://localhost:8080/v3/api-docs | Spécification OpenAPI |
| 📧 MailHog | http://localhost:8025 | Interface emails de test |
| 🗄️ H2 Console (dev) | http://localhost:8080/h2-console | Base de données (profil dev) |

### Dans Codespaces (URLs publiques)

Remplacez `localhost:PORT` par l'URL générée par Codespaces :

```
https://<votre-codespace>-8080.app.github.dev/swagger-ui.html
https://<votre-codespace>-8025.app.github.dev
```

---

## 🧪 Tests des Endpoints

### 1️⃣ Endpoint PUBLIC : Formulaire de contact

**URL :** `POST /api/contact`

**Pas besoin de token !**

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

**Réponse attendue (200 OK) :**
```json
{
  "message": "Merci! Votre message a été envoyé. Nous vous répondrons bientôt."
}
```

**Types de demande valides :**
- `INFO` - Demande d'information
- `QUOTE` - Demande de devis
- `SUPPORT` - Support technique
- `PARTNERSHIP` - Partenariat
- `OTHER` - Autre

---

### 2️⃣ Authentification : Login admin

**URL :** `POST /api/auth/login`

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "admin123"
  }'
```

**Réponse attendue (200 OK) :**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTcwNTMxMjAwMCwiZXhwIjoxNzA1Mzk4NDAwfQ.xxxxx",
  "email": "admin@example.com",
  "expiresIn": 86400000
}
```

**⚠️ Copiez le token pour les requêtes suivantes !**

---

### 3️⃣ Endpoints ADMIN (JWT requis)

**Header requis :** `Authorization: Bearer <VOTRE_TOKEN>`

#### 📋 Lister tous les leads

**URL :** `GET /api/admin/leads`

```bash
curl http://localhost:8080/api/admin/leads \
  -H "Authorization: Bearer VOTRE_TOKEN_ICI"
```

**Avec pagination :**
```bash
curl "http://localhost:8080/api/admin/leads?page=0&size=10" \
  -H "Authorization: Bearer VOTRE_TOKEN_ICI"
```

**Filtrer par statut :**
```bash
curl "http://localhost:8080/api/admin/leads?status=NEW" \
  -H "Authorization: Bearer VOTRE_TOKEN_ICI"
```

---

#### 🔍 Détail d'un lead

**URL :** `GET /api/admin/leads/{id}`

```bash
curl http://localhost:8080/api/admin/leads/1 \
  -H "Authorization: Bearer VOTRE_TOKEN_ICI"
```

**Réponse :**
```json
{
  "id": 1,
  "fullName": "Marie Tremblay",
  "company": "ABC Inc.",
  "email": "marie@example.com",
  "phone": "514-555-1234",
  "requestType": "QUOTE",
  "message": "Bonjour, je voudrais un devis pour 10 personnes.",
  "status": "NEW",
  "createdAt": "2026-01-15T15:30:00",
  "updatedAt": null
}
```

---

#### ✏️ Changer le statut d'un lead

**URL :** `PUT /api/admin/leads/{id}/status`

```bash
curl -X PUT http://localhost:8080/api/admin/leads/1/status \
  -H "Authorization: Bearer VOTRE_TOKEN_ICI" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "CONTACTED"
  }'
```

**Statuts valides :**
- `NEW` - Nouveau
- `CONTACTED` - Contacté
- `CONVERTED` - Converti en client
- `LOST` - Perdu

---

#### 🗑️ Supprimer un lead

**URL :** `DELETE /api/admin/leads/{id}`

```bash
curl -X DELETE http://localhost:8080/api/admin/leads/1 \
  -H "Authorization: Bearer VOTRE_TOKEN_ICI"
```

**Réponse (204 No Content)** : Pas de body

---

#### 📊 Statistiques des leads

**URL :** `GET /api/admin/leads/stats`

```bash
curl http://localhost:8080/api/admin/leads/stats \
  -H "Authorization: Bearer VOTRE_TOKEN_ICI"
```

**Réponse :**
```json
{
  "totalLeads": 10,
  "newLeads": 5,
  "contactedLeads": 3,
  "convertedLeads": 1,
  "lostLeads": 1,
  "conversionRate": 10.0
}
```

---

## 📧 Vérifier les emails (MailHog)

### URL : http://localhost:8025

Après avoir soumis un formulaire de contact, vous verrez 2 emails :

1. **Notification admin** - Envoyé à `admin@example.com`
2. **Confirmation visiteur** - Envoyé à l'email du formulaire

### Interface MailHog

![MailHog Interface](https://raw.githubusercontent.com/mailhog/MailHog/master/docs/MailHog.png)

---

## 📘 Swagger UI - Test interactif

### URL : http://localhost:8080/swagger-ui.html

### Comment tester avec Swagger :

1. **Ouvrez** http://localhost:8080/swagger-ui.html
2. **Testez** `POST /api/contact` (pas besoin de token)
3. **Authentifiez-vous** avec `POST /api/auth/login`
4. **Copiez** le token de la réponse
5. **Cliquez** sur le bouton **Authorize** 🔓 (en haut à droite)
6. **Collez** le token : `Bearer eyJhbG...`
7. **Testez** les endpoints admin

---

## 🔐 Credentials de test

| Service | Email / User | Mot de passe |
|---------|--------------|--------------|
| Admin API | `admin@example.com` | `admin123` |
| PostgreSQL | `postgres` | `postgres` |
| H2 Console | `sa` | *(vide)* |

---

## ⚠️ Codes d'erreur courants

| Code | Signification | Solution |
|------|---------------|----------|
| 400 | Bad Request | Vérifiez le JSON envoyé |
| 401 | Unauthorized | Token manquant ou invalide |
| 403 | Forbidden | Token valide mais pas les droits |
| 404 | Not Found | Lead ID inexistant |
| 500 | Server Error | Vérifiez les logs : `docker logs contact-api` |

---

## 🧪 Script de test complet

```bash
#!/bin/bash

BASE_URL="http://localhost:8080"

echo "=== 1. Test formulaire de contact ==="
curl -s -X POST $BASE_URL/api/contact \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "email": "test@example.com",
    "requestType": "INFO",
    "message": "Test automatique"
  }' | jq .

echo ""
echo "=== 2. Login admin ==="
TOKEN=$(curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "admin123"
  }' | jq -r '.token')

echo "Token: ${TOKEN:0:50}..."

echo ""
echo "=== 3. Liste des leads ==="
curl -s $BASE_URL/api/admin/leads \
  -H "Authorization: Bearer $TOKEN" | jq .

echo ""
echo "=== 4. Statistiques ==="
curl -s $BASE_URL/api/admin/leads/stats \
  -H "Authorization: Bearer $TOKEN" | jq .

echo ""
echo "=== Tests terminés ! ==="
```

Sauvegardez ce script dans `test-api.sh` et exécutez :
```bash
chmod +x test-api.sh
./test-api.sh
```

---

## ✅ Checklist de validation

- [ ] `POST /api/contact` retourne 200
- [ ] `POST /api/auth/login` retourne un token
- [ ] `GET /api/admin/leads` retourne la liste (avec token)
- [ ] `GET /api/admin/leads/stats` retourne les stats
- [ ] MailHog affiche les 2 emails
- [ ] Swagger UI est accessible

