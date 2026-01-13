# Guide de démarrage rapide

## Prérequis

- Docker Desktop installé ([télécharger ici](https://www.docker.com/products/docker-desktop/))
- Git installé

---

## Étape 1: Cloner le projet

```bash
git clone https://github.com/hrhouma1/inskillflow-docs-formation-spring-boot.git
cd inskillflow-docs-formation-spring-boot/solutions/projet-e-contact-backend
```

---

## Étape 2: Démarrer avec Docker

```bash
docker-compose up -d
```

Attendez environ 1-2 minutes que tout démarre.

---

## Étape 3: Vérifier que ça fonctionne

Ouvrez ces URLs dans votre navigateur:

| Service | URL | Description |
|---------|-----|-------------|
| **API** | http://localhost:8080 | Doit afficher une erreur 401 (normal) |
| **Swagger** | http://localhost:8080/swagger-ui.html | Interface de test |
| **MailHog** | http://localhost:8025 | Voir les emails |

---

## Étape 4: Tester l'API

### Test 1: Soumettre un formulaire (sans token)

```bash
curl -X POST http://localhost:8080/api/contact \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "email": "test@example.com",
    "requestType": "INFO",
    "message": "Ceci est un message de test pour verifier que tout fonctionne."
  }'
```

**Résultat attendu:**
```json
{"message":"Merci! Votre message a été envoyé. Nous vous répondrons bientôt."}
```

### Test 2: Vérifier l'email

Ouvrez http://localhost:8025 - vous devriez voir 2 emails:
1. Notification à l'admin
2. Confirmation au visiteur

### Test 3: Se connecter comme admin

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "admin123"
  }'
```

**Résultat attendu:**
```json
{
  "token": "eyJhbGciOiJIUzI1...",
  "type": "Bearer",
  "expiresIn": 86400000,
  "email": "admin@example.com",
  "role": "ADMIN"
}
```

### Test 4: Voir les leads (avec token)

Copiez le token de l'étape précédente et remplacez `VOTRE_TOKEN`:

```bash
curl http://localhost:8080/api/admin/leads \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

---

## Étape 5: Utiliser Swagger UI (plus facile)

1. Ouvrez http://localhost:8080/swagger-ui.html
2. Cliquez sur `POST /api/contact` → "Try it out" → "Execute"
3. Cliquez sur `POST /api/auth/login` → Entrez les credentials → "Execute"
4. Copiez le token de la réponse
5. Cliquez sur le bouton "Authorize" (cadenas en haut à droite)
6. Entrez: `Bearer VOTRE_TOKEN`
7. Testez les endpoints admin!

---

## Commandes utiles

```bash
# Voir les logs
docker-compose logs -f

# Voir les logs de l'API seulement
docker-compose logs -f api

# Arrêter tout
docker-compose down

# Arrêter et supprimer les données
docker-compose down -v

# Redémarrer
docker-compose restart

# Reconstruire après modification
docker-compose up -d --build
```

---

## En cas de problème

### Port déjà utilisé

```bash
# Vérifier ce qui utilise le port 8080
netstat -ano | findstr :8080

# Ou changer le port dans docker-compose.yml
ports:
  - "8081:8080"  # Utiliser 8081 au lieu de 8080
```

### Docker ne démarre pas

```bash
# Vérifier que Docker est lancé
docker ps

# Si erreur, redémarrer Docker Desktop
```

### Base de données vide après redémarrage

C'est normal en mode développement (H2 en mémoire). Les données sont recréées à chaque démarrage.

---

## Credentials par défaut

| Type | Email | Password |
|------|-------|----------|
| Admin | admin@example.com | admin123 |

---

## Structure de l'API

```
POST /api/contact          → Public (formulaire)
POST /api/auth/login       → Public (connexion)
GET  /api/admin/leads      → Admin (liste leads)
GET  /api/admin/leads/{id} → Admin (détail)
PUT  /api/admin/leads/{id}/status → Admin (changer statut)
DELETE /api/admin/leads/{id} → Admin (supprimer)
GET  /api/admin/leads/stats → Admin (statistiques)
```

---

## Ça fonctionne? 

Si vous voyez:
- ✅ Le formulaire retourne "Merci!"
- ✅ Les emails apparaissent dans MailHog
- ✅ Swagger UI s'affiche
- ✅ Le login retourne un token

**Alors tout est bon!**

