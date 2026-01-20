# 24 - Guide : Tester l'Envoi de Vrais Emails avec Gmail

## Prérequis

- Un compte Gmail
- Un mot de passe d'application Gmail (16 caractères)
- Docker Desktop ou Codespaces

---

## Étape 1 : Votre mot de passe d'application

Votre mot de passe d'application Gmail ressemble à :
```
rbfa rtnm qpos obqb
```

**Sans les espaces** : `rbfartnmqposobqb`

> Ce mot de passe est créé dans Google Account > Sécurité > Mots de passe des applications

---

## Étape 2 : Configurer les variables d'environnement

### Option A : Docker Desktop (fichier .env)

Créez un fichier `.env` à la racine du projet :

```env
# Gmail SMTP
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USER=votre-email@gmail.com
MAIL_PASSWORD=rbfartnmqposobqb
MAIL_AUTH=true

# Admin (recevra les notifications)
ADMIN_EMAIL=votre-email@gmail.com

# Base de données
DB_HOST=postgres
DB_PORT=5432
DB_NAME=contactdb
DB_USER=postgres
DB_PASSWORD=postgres

# JWT
JWT_SECRET=dGhpcy1pcy1hLXZlcnktbG9uZy1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tZ2VuZXJhdGlvbi0yNTY=
JWT_EXPIRATION=86400000
```

### Option B : Codespaces (Secrets)

1. Allez sur GitHub.com > Settings > Codespaces > Secrets
2. Créez ces secrets :

| Nom | Valeur |
|-----|--------|
| GMAIL_USER | votre-email@gmail.com |
| GMAIL_PASSWORD | rbfartnmqposobqb |

3. Redémarrez le Codespace

---

## Étape 3 : Lancer avec Gmail

### Docker Desktop

```powershell
# Arrêter l'ancien déploiement
docker compose down

# Lancer avec le fichier Gmail
docker compose -f docker-compose.gmail.yml up --build -d
```

### Codespaces

```bash
# Arrêter l'ancien déploiement
docker compose down

# Lancer avec Gmail
docker compose -f docker-compose.gmail.yml up --build -d
```

---

## Étape 4 : Vérifier la configuration

```bash
docker exec contact-api env | grep MAIL
```

**Résultat attendu :**
```
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USER=votre-email@gmail.com
MAIL_PASSWORD=rbfartnmqposobqb
MAIL_AUTH=true
```

---

## Étape 5 : Tester l'envoi d'email

### Méthode 1 : Via Swagger UI

1. Ouvrez Swagger UI :
   - Local : http://localhost:8080/swagger-ui.html
   - Codespaces : https://CODESPACE-8080.app.github.dev/swagger-ui.html

2. Cliquez sur **POST /api/contact**

3. Cliquez sur **Try it out**

4. Collez ce JSON (avec VOTRE vraie adresse email) :

```json
{
  "fullName": "Test Email Gmail",
  "company": "Mon Entreprise",
  "email": "votre-vraie-adresse@gmail.com",
  "phone": "514-555-1234",
  "requestType": "INFO",
  "message": "Test envoi email réel via Gmail SMTP"
}
```

5. Cliquez sur **Execute**

### Méthode 2 : Via curl

```bash
curl -X POST http://localhost:8080/api/contact \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test Email Gmail",
    "company": "Mon Entreprise",
    "email": "votre-vraie-adresse@gmail.com",
    "phone": "514-555-1234",
    "requestType": "INFO",
    "message": "Test envoi email réel via Gmail SMTP"
  }'
```

### Méthode 3 : Via PowerShell

```powershell
$body = @{
    fullName = "Test Email Gmail"
    company = "Mon Entreprise"
    email = "votre-vraie-adresse@gmail.com"
    phone = "514-555-1234"
    requestType = "INFO"
    message = "Test envoi email réel via Gmail SMTP"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/contact" -Method Post -Body $body -ContentType "application/json"
```

---

## Étape 6 : Vérifier les logs

```bash
docker logs contact-api --tail 30
```

### Résultat attendu (succès)

```
INFO  - Création du lead pour: votre-vraie-adresse@gmail.com
INFO  - Email de notification envoyé à l'admin pour le lead: votre-vraie-adresse@gmail.com
INFO  - Email de confirmation envoyé à: votre-vraie-adresse@gmail.com
```

### Si erreur d'authentification

```
ERROR - Could not connect to SMTP host: smtp.gmail.com, port: 587
ERROR - Authentication failed
```

**Solutions :**
1. Vérifiez le mot de passe d'application (sans espaces)
2. Vérifiez que l'authentification 2FA est activée sur Gmail
3. Recréez un mot de passe d'application

---

## Étape 7 : Vérifier votre boîte Gmail

1. Ouvrez https://mail.google.com
2. Vérifiez **Boîte de réception** et **Spam**

### Emails attendus

**Email 1 : Notification admin**
```
De: votre-email@gmail.com
À: votre-email@gmail.com (ADMIN_EMAIL)
Sujet: Nouveau contact: Test Email Gmail

Nouveau contact reçu!

Nom: Test Email Gmail
Entreprise: Mon Entreprise
Email: votre-vraie-adresse@gmail.com
Téléphone: 514-555-1234
Type: INFO

Message:
Test envoi email réel via Gmail SMTP
```

**Email 2 : Confirmation visiteur**
```
De: votre-email@gmail.com
À: votre-vraie-adresse@gmail.com
Sujet: Confirmation - Nous avons bien reçu votre message

Bonjour Test Email Gmail,

Merci de nous avoir contactés!
Nous avons bien reçu votre message et nous vous répondrons dans les plus brefs délais.

Cordialement,
L'équipe
```

---

## Étape 8 : Revenir à MailHog (développement)

```bash
# Arrêter Gmail
docker compose -f docker-compose.gmail.yml down

# Relancer avec MailHog
docker compose up --build -d
```

Vérifier :
```bash
docker exec contact-api env | grep MAIL
```

**Résultat attendu :**
```
MAIL_HOST=mailhog
MAIL_PORT=1025
```

---

## Résolution des problèmes

### Erreur : Authentication failed

**Causes possibles :**
1. Mot de passe incorrect
2. Espaces dans le mot de passe
3. Authentification 2FA non activée

**Solution :**
1. Allez sur https://myaccount.google.com/apppasswords
2. Créez un nouveau mot de passe d'application
3. Copiez-le SANS les espaces

### Erreur : Connection refused

**Causes possibles :**
1. Mauvais host ou port
2. Pare-feu bloque le port 587

**Solution :**
Vérifiez :
```
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
```

### Erreur : Could not parse mail

**Cause :**
MAIL_USER est vide ou invalide

**Solution :**
```
MAIL_USER=votre-email@gmail.com
```

### Email dans les spams

**Solution :**
1. Marquez l'email comme "Non spam"
2. Ajoutez l'expéditeur aux contacts

---

## Récapitulatif

| Étape | Action |
|-------|--------|
| 1 | Créer mot de passe application Gmail |
| 2 | Configurer .env ou Codespaces Secrets |
| 3 | Lancer docker-compose.gmail.yml |
| 4 | Vérifier env avec `docker exec contact-api env \| grep MAIL` |
| 5 | Créer un contact via Swagger ou curl |
| 6 | Vérifier les logs |
| 7 | Vérifier Gmail (Inbox + Spam) |
| 8 | Revenir à MailHog pour le dev |

---

## Checklist

- [ ] Mot de passe d'application créé (16 caractères, sans espaces)
- [ ] Variables configurées (.env ou Codespaces Secrets)
- [ ] docker-compose.gmail.yml lancé
- [ ] Configuration vérifiée (`docker exec contact-api env | grep MAIL`)
- [ ] Contact créé via Swagger/curl
- [ ] Logs vérifiés (pas d'erreur)
- [ ] Email reçu dans Gmail

