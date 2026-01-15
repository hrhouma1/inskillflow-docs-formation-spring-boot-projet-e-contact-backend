# 08 - Bonnes pratiques pour les fichiers .env

## 🔐 Règle d'or

> **Ne JAMAIS pousser de secrets sur GitHub !**

---

## 📁 Les différents fichiers

| Fichier | Sur GitHub ? | Usage |
|---------|--------------|-------|
| `.env.example` | ✅ Oui | Template avec valeurs fictives |
| `.env` | ❌ **NON** | Vraies valeurs (secrets) |
| `.env.local` | ❌ **NON** | Surcharges locales |
| `.env.production` | ❌ **NON** | Valeurs de production |

---

## 🚀 Comment utiliser dans Codespaces

### Étape 1 : Copier le fichier exemple

```bash
cp .env.example .env
```

### Étape 2 : Modifier si nécessaire

```bash
# Ouvrir le fichier
nano .env
# ou
code .env
```

### Étape 3 : Utiliser avec Docker Compose

```bash
docker compose up --build -d
```

Docker Compose lit automatiquement le fichier `.env` !

---

## 📝 Contenu de `.env.example`

```env
# ============================================
# Contact Form API - Variables d'environnement
# ============================================
# 
# INSTRUCTIONS:
# 1. Copiez ce fichier : cp .env.example .env
# 2. Modifiez les valeurs selon votre environnement
# 3. Ne commitez JAMAIS le fichier .env !
#
# ============================================

# --- Profil Spring ---
SPRING_PROFILES_ACTIVE=prod

# --- Base de données PostgreSQL ---
DB_HOST=postgres
DB_PORT=5432
DB_NAME=contactdb
DB_USER=postgres
DB_PASSWORD=postgres

# --- Email (MailHog pour dev/test) ---
MAIL_HOST=mailhog
MAIL_PORT=1025
MAIL_USER=
MAIL_PASSWORD=
MAIL_AUTH=false
MAIL_STARTTLS=false

# --- Email admin (notifications) ---
ADMIN_EMAIL=admin@example.com

# --- JWT (Sécurité) ---
JWT_SECRET=dGhpcy1pcy1hLXZlcnktbG9uZy1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tZ2VuZXJhdGlvbi0yNTY=
JWT_EXPIRATION=86400000
```

---

## ⚠️ Ce qui est dans `.gitignore`

```gitignore
# Environment variables (secrets)
.env
.env.local
.env.*.local
```

---

## 🔄 Alternatives dans ce projet

### Option A : Fichier `.env` (recommandé)

```bash
# Copier et utiliser
cp .env.example .env
docker compose up -d
```

### Option B : Variables dans `docker-compose.yml` (déjà configuré)

Les variables sont déjà définies avec des valeurs par défaut dans :
- `docker-compose.yml` → section `environment:`
- `application.yml` → syntaxe `${VAR:default}`

**C'est pourquoi ça fonctionne même sans fichier `.env` !**

### Option C : Secrets Codespaces (pour vrai secrets)

1. GitHub → Settings → Codespaces → Secrets
2. Ajoutez vos secrets (ils seront injectés automatiquement)

---

## 🏭 Configuration par environnement

### Développement local (sans Docker)

```bash
# Utiliser le profil dev (H2 + MailHog)
./mvnw spring-boot:run
# Par défaut, spring.profiles.active=dev
```

### Développement avec Docker (Codespaces)

```bash
# Le fichier docker-compose.yml définit tout
docker compose up -d
# Pas besoin de .env, les valeurs sont dans docker-compose.yml
```

### Production

```bash
# Créer un .env avec les vraies valeurs
cp .env.example .env
nano .env  # Modifier avec les vrais secrets

# OU utiliser des variables d'environnement système
export DB_PASSWORD=mon_vrai_mot_de_passe
export JWT_SECRET=ma_vraie_cle_secrete
export MAIL_USER=mon@email.com
export MAIL_PASSWORD=mon_app_password
```

---

## 🔑 Générer un JWT_SECRET sécurisé

```bash
# Linux/Mac
openssl rand -base64 32

# Résultat exemple:
# K7gNU3sdo+OL0wNhqoVWhr3g6s1xYv72ol/pe/Unols=
```

---

## 📊 Tableau récapitulatif

| Variable | Dev (H2) | Docker (MailHog) | Production |
|----------|----------|------------------|------------|
| `SPRING_PROFILES_ACTIVE` | dev | prod | prod |
| `DB_HOST` | - | postgres | db.example.com |
| `DB_PASSWORD` | - | postgres | **secret** |
| `MAIL_HOST` | localhost | mailhog | smtp.gmail.com |
| `MAIL_AUTH` | false | false | true |
| `MAIL_USER` | - | - | **email** |
| `MAIL_PASSWORD` | - | - | **secret** |
| `JWT_SECRET` | (défaut) | (défaut) | **secret** |

---

## ✅ Checklist sécurité

- [ ] `.env` est dans `.gitignore`
- [ ] `.env.example` ne contient PAS de vrais secrets
- [ ] Les mots de passe de production sont différents de "postgres"
- [ ] `JWT_SECRET` est unique et généré aléatoirement
- [ ] Les secrets de production sont dans un gestionnaire sécurisé

---

## 🔗 Références

- [Docker Compose - Environment variables](https://docs.docker.com/compose/environment-variables/)
- [Spring Boot - Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [GitHub Codespaces - Secrets](https://docs.github.com/en/codespaces/managing-your-codespaces/managing-encrypted-secrets-for-your-codespaces)

