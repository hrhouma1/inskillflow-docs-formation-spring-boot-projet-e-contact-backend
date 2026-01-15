# 06 - Erreur MAIL_USER et Reconstruction Docker

## 🔴 Erreur rencontrée

```
Caused by: java.lang.IllegalArgumentException: Could not resolve placeholder 'MAIL_USER' in value "${MAIL_USER}"
```

L'application crash au démarrage avec l'erreur `emailService: Injection of autowired dependencies failed`.

---

## 🔍 Analyse du problème

### Cause racine

Dans `application.yml`, le profil **prod** avait des variables d'environnement **sans valeurs par défaut** :

```yaml
# AVANT - Problématique
spring:
  mail:
    username: ${MAIL_USER}           # ❌ Pas de valeur par défaut
    password: ${MAIL_PASSWORD}       # ❌ Pas de valeur par défaut
    properties:
      mail:
        smtp:
          auth: true                 # ❌ Toujours true, même pour MailHog

app:
  jwt:
    secret: ${JWT_SECRET}            # ❌ Pas de valeur par défaut
```

### Pourquoi ça posait problème ?

1. **MailHog n'a pas besoin d'authentification** - Il accepte tous les emails sans login
2. **docker-compose.yml ne définissait pas** `MAIL_USER` et `MAIL_PASSWORD`
3. **Spring Boot exige une valeur** quand il n'y a pas de défaut

---

## ✅ Solution appliquée

### Modification de `application.yml`

```yaml
# APRÈS - Corrigé
spring:
  mail:
    host: ${MAIL_HOST:localhost}
    port: ${MAIL_PORT:1025}
    username: ${MAIL_USER:}              # ✅ Défaut: vide
    password: ${MAIL_PASSWORD:}          # ✅ Défaut: vide
    properties:
      mail:
        smtp:
          auth: ${MAIL_AUTH:false}       # ✅ Défaut: false (pas d'auth pour MailHog)
          starttls:
            enable: ${MAIL_STARTTLS:false}  # ✅ Défaut: false
            required: false

app:
  jwt:
    secret: ${JWT_SECRET:dGhpcy1pcy1hLXZlcnktbG9uZy1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tZ2VuZXJhdGlvbi0yNTY=}
    #                    ↑ Valeur par défaut pour le développement
```

### Syntaxe des valeurs par défaut

```yaml
${VARIABLE:valeur_par_defaut}
${VARIABLE:}                    # Valeur par défaut vide (chaîne vide)
${VARIABLE}                     # ERREUR si non définie !
```

---

## 🔴 Deuxième erreur : Image Docker non reconstruite

### Symptôme

Même après avoir corrigé `application.yml` et fait un `git push`, l'erreur persistait !

### Cause

```bash
# ❌ Cette commande NE reconstruit PAS l'image
docker compose up -d

# L'image en cache contient encore l'ancien code
```

### Solution

```bash
# ✅ Utiliser --build pour forcer la reconstruction
docker compose down
docker compose up --build -d
```

### Explication

| Commande | Comportement |
|----------|--------------|
| `docker compose up -d` | Utilise l'image **en cache** (ancien code) |
| `docker compose up --build -d` | **Reconstruit** l'image avec le nouveau code |

---

## 🚀 Procédure complète de correction

```bash
# 1. Arrêter les conteneurs
docker compose down

# 2. Reconstruire ET relancer
docker compose up --build -d

# 3. Vérifier que l'API fonctionne
docker ps

# 4. Voir les logs
docker logs contact-api -f
```

### Résultat attendu

```
CONTAINER ID   IMAGE          STATUS         PORTS
xxxx           ...-api        Up X seconds   0.0.0.0:8080->8080/tcp   ✅
xxxx           postgres       Up X seconds   0.0.0.0:5432->5432/tcp   ✅
xxxx           mailhog        Up X seconds   0.0.0.0:8025->8025/tcp   ✅
```

---

## 🌐 URLs de test (Codespaces)

Une fois l'application démarrée, les URLs suivantes sont disponibles :

### Format des URLs Codespaces

```
https://<nom-codespace>-<PORT>.app.github.dev
```

### Exemple concret

| Service | URL |
|---------|-----|
| 📘 **Swagger UI** | https://expert-acorn-v6g97rv5x577fp4pg-8080.app.github.dev/swagger-ui.html |
| 🚀 **API Contact** | https://expert-acorn-v6g97rv5x577fp4pg-8080.app.github.dev/api/contact |
| 📧 **MailHog** | https://expert-acorn-v6g97rv5x577fp4pg-8025.app.github.dev |

> ⚠️ **Note** : Remplacez `expert-acorn-v6g97rv5x577fp4pg` par le nom de votre Codespace.

### Trouver vos URLs

1. Ouvrez l'onglet **PORTS** en bas de VS Code
2. Cliquez sur l'icône 🌐 à côté du port pour ouvrir dans le navigateur
3. Ou copiez l'URL depuis la colonne "Adresse transférée"

---

## 🧪 Tests de validation

### 1. Test du formulaire de contact

```bash
curl -X POST http://localhost:8080/api/contact \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Marie Tremblay",
    "email": "marie@example.com",
    "requestType": "INFO",
    "message": "Test depuis Codespaces!"
  }'
```

**Réponse attendue :**
```json
{
  "message": "Merci! Votre message a été envoyé. Nous vous répondrons bientôt."
}
```

### 2. Vérifier les emails dans MailHog

Ouvrez https://expert-acorn-v6g97rv5x577fp4pg-8025.app.github.dev

Vous devriez voir :
- 📧 Email de notification à l'admin
- 📧 Email de confirmation au visiteur

### 3. Test d'authentification admin

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "admin123"
  }'
```

---

## 📚 Leçons apprises

### 1. Toujours mettre des valeurs par défaut

```yaml
# ❌ Dangereux - peut crasher en production
username: ${MAIL_USER}

# ✅ Sûr - fonctionne même si la variable n'est pas définie
username: ${MAIL_USER:}
```

### 2. Toujours utiliser `--build` après modification du code

```bash
# Après git pull ou modification locale
docker compose up --build -d
```

### 3. Adapter la configuration à l'environnement

| Environnement | MAIL_AUTH | MAIL_USER | MAIL_PASSWORD |
|---------------|-----------|-----------|---------------|
| Dev (MailHog) | false | (vide) | (vide) |
| Prod (Gmail) | true | user@gmail.com | app_password |

### 4. Vérifier les logs en cas d'erreur

```bash
docker logs contact-api --tail 100
```

---

## 🔗 Fichiers modifiés

- `src/main/resources/application.yml` - Ajout des valeurs par défaut

## 🔗 Références

- [Spring Boot - Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Docker Compose - Build](https://docs.docker.com/compose/reference/build/)
- [MailHog - GitHub](https://github.com/mailhog/MailHog)

