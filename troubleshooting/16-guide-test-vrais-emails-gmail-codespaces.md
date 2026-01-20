# 16 - Guide : Tester de Vrais Emails avec Gmail sur Codespaces

## 🎯 Objectif

Configurer Gmail pour envoyer de **vrais emails** depuis Codespaces, tout en gardant MailHog pour le développement local.

---

## 📋 Ce que vous allez faire

1. Créer un mot de passe d'application Gmail
2. Configurer les variables d'environnement dans Codespaces
3. Tester l'envoi de vrais emails
4. Recevoir les emails dans votre boîte Gmail

---

## ⚠️ Prérequis

- Un compte Gmail
- L'authentification à 2 facteurs activée sur votre compte Google

---

# ÉTAPE 1 : ACTIVER L'AUTHENTIFICATION 2 FACTEURS

## 📍 Étape 1.1 : Accéder aux paramètres de sécurité

1. Ouvrez https://myaccount.google.com
2. Cliquez sur **Sécurité** dans le menu à gauche

---

## 📍 Étape 1.2 : Activer la validation en 2 étapes

1. Dans la section "Comment vous connecter à Google"
2. Cliquez sur **Validation en 2 étapes**
3. Suivez les instructions pour l'activer (SMS ou application)

> ⚠️ **Sans cette étape, vous ne pourrez pas créer de mot de passe d'application !**

---

# ÉTAPE 2 : CRÉER UN MOT DE PASSE D'APPLICATION

## 📍 Étape 2.1 : Accéder aux mots de passe d'application

1. Allez sur https://myaccount.google.com/apppasswords
2. Ou : Compte Google → Sécurité → Mots de passe des applications

---

## 📍 Étape 2.2 : Créer un nouveau mot de passe

1. Dans "Sélectionner une application", choisissez **Autre (nom personnalisé)**
2. Tapez : `Contact API Codespaces`
3. Cliquez sur **Générer**

---

## 📍 Étape 2.3 : Copier le mot de passe

Vous verrez un mot de passe de **16 caractères** comme :

```
abcd efgh ijkl mnop
```

📋 **COPIEZ CE MOT DE PASSE** (sans les espaces) : `abcdefghijklmnop`

> ⚠️ Ce mot de passe ne sera plus affiché ! Gardez-le en sécurité.

---

# ÉTAPE 3 : CONFIGURER LES SECRETS CODESPACES

## 📍 Étape 3.1 : Accéder aux secrets Codespaces

1. Allez sur **GitHub.com**
2. Cliquez sur votre **photo de profil** (coin supérieur droit)
3. Cliquez sur **Settings**
4. Dans le menu à gauche, cliquez sur **Codespaces**
5. Descendez jusqu'à la section **Secrets**

---

## 📍 Étape 3.2 : Créer les secrets

Cliquez sur **New secret** pour chaque secret :

### Secret 1 : GMAIL_USER

| Champ | Valeur |
|-------|--------|
| Name | `GMAIL_USER` |
| Value | `votre-email@gmail.com` |
| Repository access | Sélectionnez votre repo |

Cliquez sur **Add secret**

---

### Secret 2 : GMAIL_PASSWORD

| Champ | Valeur |
|-------|--------|
| Name | `GMAIL_PASSWORD` |
| Value | `abcdefghijklmnop` (votre mot de passe d'app) |
| Repository access | Sélectionnez votre repo |

Cliquez sur **Add secret**

---

## 📍 Étape 3.3 : Vérifier les secrets

Vous devriez voir :

```
Codespaces secrets

GMAIL_USER          Updated just now
GMAIL_PASSWORD      Updated just now
```

---

# ÉTAPE 4 : CRÉER UN FICHIER DOCKER-COMPOSE POUR GMAIL

## 📍 Étape 4.1 : Créer docker-compose.gmail.yml

Dans Codespaces, créez un nouveau fichier :

```bash
touch docker-compose.gmail.yml
```

---

## 📍 Étape 4.2 : Contenu du fichier

Collez ce contenu dans `docker-compose.gmail.yml` :

```yaml
version: '3.8'

services:
  # Base de données PostgreSQL
  postgres:
    image: postgres:15-alpine
    container_name: contact-db
    environment:
      POSTGRES_DB: contactdb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Application Spring Boot avec Gmail
  api:
    build: .
    container_name: contact-api
    ports:
      - "8080:8080"
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
      JWT_SECRET: dGhpcy1pcy1hLXZlcnktbG9uZy1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tZ2VuZXJhdGlvbi0yNTY=
      JWT_EXPIRATION: 86400000
    depends_on:
      postgres:
        condition: service_healthy

volumes:
  postgres_data:
```

> **Note** : Pas de MailHog dans ce fichier ! Les emails vont directement sur Gmail.

---

# ÉTAPE 5 : REDÉMARRER LE CODESPACE

## 📍 Étape 5.1 : Pourquoi redémarrer ?

Les secrets Codespaces sont injectés **au démarrage** du Codespace. Il faut donc le redémarrer pour qu'ils soient disponibles.

---

## 📍 Étape 5.2 : Redémarrer le Codespace

### Option A : Via l'interface web

1. Allez sur https://github.com/codespaces
2. Trouvez votre Codespace
3. Cliquez sur les **...** (trois points)
4. Cliquez sur **Stop codespace**
5. Attendez 10 secondes
6. Cliquez sur le nom du Codespace pour le relancer

### Option B : Via VS Code

1. Appuyez sur `F1` ou `Ctrl+Shift+P`
2. Tapez : `Codespaces: Rebuild Container`
3. Confirmez

---

# ÉTAPE 6 : LANCER AVEC GMAIL

## 📍 Étape 6.1 : Vérifier que les secrets sont disponibles

Dans le terminal Codespaces :

```bash
echo $GMAIL_USER
echo $GMAIL_PASSWORD
```

Vous devriez voir votre email et `***` (masqué) pour le mot de passe.

Si c'est vide, redémarrez le Codespace (étape 5).

---

## 📍 Étape 6.2 : Arrêter l'ancien déploiement

```bash
docker compose down
```

---

## 📍 Étape 6.3 : Lancer avec le fichier Gmail

```bash
docker compose -f docker-compose.gmail.yml up --build -d
```

---

## 📍 Étape 6.4 : Vérifier les logs

```bash
docker logs contact-api -f
```

Attendez de voir : `Started ContactApplication in X seconds`

---

## 📍 Étape 6.5 : Vérifier la configuration

```bash
docker exec contact-api env | grep MAIL
```

**Résultat attendu :**
```
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USER=votre-email@gmail.com
MAIL_PASSWORD=abcdefghijklmnop
MAIL_AUTH=true
MAIL_STARTTLS=true
```

---

# ÉTAPE 7 : TESTER L'ENVOI DE VRAIS EMAILS

## 📍 Étape 7.1 : Ouvrir Swagger UI

```
https://votre-codespace-8080.app.github.dev/swagger-ui.html
```

---

## 📍 Étape 7.2 : Créer un lead avec VOTRE email

`POST /api/contact` avec :

```json
{
  "fullName": "Test Gmail Réel",
  "company": "Mon Entreprise",
  "email": "votre-vrai-email@gmail.com",
  "phone": "514-555-1234",
  "requestType": "INFO",
  "message": "Test envoi de vrai email depuis Codespaces!"
}
```

> ⚠️ **Utilisez VOTRE vraie adresse email** pour recevoir la confirmation !

---

## 📍 Étape 7.3 : Exécuter

Cliquez sur **Execute**

**Réponse attendue : 200 OK**

---

## 📍 Étape 7.4 : Vérifier les logs

```bash
docker logs contact-api --tail 10 | grep -i email
```

**Résultat attendu (succès) :**
```
INFO  - Email de notification envoyé à l'admin pour le lead: votre-vrai-email@gmail.com
INFO  - Email de confirmation envoyé à: votre-vrai-email@gmail.com
```

---

## 📍 Étape 7.5 : Vérifier votre boîte Gmail !

1. Ouvrez https://mail.google.com
2. Vous devriez avoir **2 emails** :

### Email 1 : Notification admin

```
De: votre-email@gmail.com
À: votre-email@gmail.com
Sujet: Nouveau contact: Test Gmail Réel

Nouveau contact reçu!

Nom: Test Gmail Réel
Entreprise: Mon Entreprise
Email: votre-vrai-email@gmail.com
...
```

### Email 2 : Confirmation visiteur

```
De: votre-email@gmail.com
À: votre-vrai-email@gmail.com
Sujet: Confirmation - Nous avons bien reçu votre message

Bonjour Test Gmail Réel,

Merci de nous avoir contactés!
...
```

---

# ÉTAPE 8 : REVENIR À MAILHOG (DÉVELOPPEMENT)

## 📍 Étape 8.1 : Arrêter Gmail

```bash
docker compose -f docker-compose.gmail.yml down
```

---

## 📍 Étape 8.2 : Relancer avec MailHog

```bash
docker compose up --build -d
```

---

## 📍 Étape 8.3 : Vérifier

```bash
docker exec contact-api env | grep MAIL
```

**Résultat attendu :**
```
MAIL_HOST=mailhog
MAIL_PORT=1025
```

Les emails retournent dans MailHog (pas envoyés réellement).

---

# RÉCAPITULATIF

## 🔄 Basculer entre Dev et Prod

| Mode | Commande | Emails |
|------|----------|--------|
| **Développement** | `docker compose up -d` | MailHog (capturés) |
| **Production/Test** | `docker compose -f docker-compose.gmail.yml up -d` | Gmail (envoyés) |

---

## 📊 Comparaison des fichiers

| Fichier | SMTP | Usage |
|---------|------|-------|
| `docker-compose.yml` | MailHog | Développement |
| `docker-compose.gmail.yml` | Gmail | Test emails réels |

---

## 🔐 Sécurité

| Élément | Sécurisé ? | Où ? |
|---------|------------|------|
| `GMAIL_USER` | ✅ Oui | Codespaces Secrets |
| `GMAIL_PASSWORD` | ✅ Oui | Codespaces Secrets |
| `docker-compose.gmail.yml` | ✅ Oui | Utilise les variables d'env |

---

## ⚠️ Problèmes courants

### Erreur : "Authentication failed"

**Causes possibles :**
1. Mot de passe d'application incorrect
2. Authentification 2 facteurs non activée
3. Secrets Codespaces non rechargés

**Solution :**
1. Recréez le mot de passe d'application
2. Redémarrez le Codespace

### Erreur : "Connection refused"

**Cause :** Mauvais host/port

**Solution :** Vérifiez `MAIL_HOST=smtp.gmail.com` et `MAIL_PORT=587`

### Les secrets sont vides

**Solution :** Redémarrez le Codespace (Stop → Start)

---

## ✅ Checklist

- [ ] Authentification 2 facteurs activée sur Gmail
- [ ] Mot de passe d'application créé
- [ ] Secret `GMAIL_USER` ajouté dans Codespaces
- [ ] Secret `GMAIL_PASSWORD` ajouté dans Codespaces
- [ ] Codespace redémarré
- [ ] `docker-compose.gmail.yml` créé
- [ ] Application lancée avec `-f docker-compose.gmail.yml`
- [ ] Email reçu dans Gmail !

---

## 🎉 Résultat final

```
┌─────────────────────────────────────────────────┐
│              GMAIL - BOÎTE DE RÉCEPTION          │
├─────────────────────────────────────────────────┤
│                                                  │
│ 📧 Nouveau contact: Test Gmail Réel             │
│    De: votre-email@gmail.com                     │
│    Il y a 1 minute                               │
│                                                  │
│ 📧 Confirmation - Nous avons bien reçu...       │
│    De: votre-email@gmail.com                     │
│    Il y a 1 minute                               │
│                                                  │
└─────────────────────────────────────────────────┘
```

Vous recevez maintenant de **vrais emails** ! 🎊

