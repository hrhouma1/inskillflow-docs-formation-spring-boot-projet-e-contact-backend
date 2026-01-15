# 14 - Erreur "Could not parse mail" - Emails non envoyés

## 🔴 Le problème

Les leads sont créés avec succès (200 OK), mais les emails n'apparaissent **jamais** dans MailHog.

### Symptôme dans MailHog

```
Inbox (0)
```

Même après avoir créé plusieurs leads, la boîte reste vide.

---

## 🔍 Diagnostic

### Étape 1 : Vérifier la configuration MAIL

```bash
docker exec contact-api env | grep MAIL
```

**Résultat observé :**
```
MAIL_PORT=1025
ADMIN_EMAIL=admin@example.com
MAIL_HOST=mailhog
```

⚠️ **Problème** : `MAIL_USER` est **absent** !

---

### Étape 2 : Vérifier les logs de l'API

```bash
docker logs contact-api --tail 30 | grep -i mail
```

**Erreur trouvée :**
```
ERROR - Erreur lors de l'envoi de l'email à l'admin: Could not parse mail
ERROR - Erreur lors de l'envoi de l'email de confirmation à marie@abc.com: Could not parse mail
ERROR - Erreur lors de l'envoi de l'email à l'admin: Could not parse mail
ERROR - Erreur lors de l'envoi de l'email de confirmation à jean@xyz.com: Could not parse mail
...
```

Tous les emails échouent avec l'erreur `Could not parse mail`.

---

## 🔎 Analyse de la cause

### Le code EmailService

```java
@Service
public class EmailService {

    @Value("${spring.mail.username:noreply@example.com}")
    private String fromEmail;  // ← Adresse de l'expéditeur

    public void sendNotificationToAdmin(Lead lead) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);  // ← Si fromEmail est vide = ERREUR
        message.setTo(adminEmail);
        // ...
        mailSender.send(message);
    }
}
```

### La configuration application.yml (profil prod)

```yaml
spring:
  mail:
    host: ${MAIL_HOST:localhost}
    port: ${MAIL_PORT:1025}
    username: ${MAIL_USER:}       # ← Valeur par défaut VIDE !
    password: ${MAIL_PASSWORD:}
```

### Le problème

1. `spring.mail.username` prend la valeur de `${MAIL_USER:}` (vide par défaut)
2. Dans `EmailService`, `fromEmail` devient une **chaîne vide** `""`
3. Spring Mail ne peut pas parser un email avec un expéditeur vide
4. Erreur : `Could not parse mail`

---

## ✅ Solution

### Correction dans docker-compose.yml

**Avant (problématique) :**
```yaml
environment:
  SPRING_PROFILES_ACTIVE: prod
  DB_HOST: postgres
  # ...
  MAIL_HOST: mailhog
  MAIL_PORT: 1025
  # MAIL_USER manquant !
  ADMIN_EMAIL: admin@example.com
```

**Après (corrigé) :**
```yaml
environment:
  SPRING_PROFILES_ACTIVE: prod
  DB_HOST: postgres
  # ...
  MAIL_HOST: mailhog
  MAIL_PORT: 1025
  MAIL_USER: noreply@example.com   # ← AJOUTÉ !
  ADMIN_EMAIL: admin@example.com
```

---

## 🚀 Appliquer la correction

### Dans Codespaces

```bash
# 1. Récupérer les changements
git pull

# 2. Reconstruire et relancer
docker compose down
docker compose up --build -d

# 3. Attendre le démarrage
docker logs contact-api -f
```

Attendez de voir : `Started ContactApplication in X seconds`

---

### En local (Docker Desktop)

```bash
# 1. Mettre à jour docker-compose.yml manuellement
# Ajouter MAIL_USER: noreply@example.com

# 2. Reconstruire
docker compose down
docker compose up --build -d
```

---

## 🧪 Tester la correction

### Étape 1 : Vérifier la nouvelle configuration

```bash
docker exec contact-api env | grep MAIL
```

**Résultat attendu :**
```
MAIL_PORT=1025
MAIL_USER=noreply@example.com    ← PRÉSENT !
ADMIN_EMAIL=admin@example.com
MAIL_HOST=mailhog
```

---

### Étape 2 : Créer un nouveau lead

Dans Swagger UI (`POST /api/contact`) :

```json
{
  "fullName": "Test Après Fix",
  "email": "test@apresfix.com",
  "requestType": "INFO",
  "message": "Test pour vérifier que les emails fonctionnent maintenant"
}
```

**Execute** → 200 OK

---

### Étape 3 : Vérifier les logs

```bash
docker logs contact-api --tail 10 | grep -i email
```

**Résultat attendu (succès) :**
```
INFO  - Email de notification envoyé à l'admin pour le lead: test@apresfix.com
INFO  - Email de confirmation envoyé à: test@apresfix.com
```

Plus d'erreur `Could not parse mail` !

---

### Étape 4 : Vérifier MailHog

1. Ouvrez MailHog : http://localhost:8025 (ou URL Codespaces)
2. Rafraîchissez la page (F5)
3. Vous devriez voir **Inbox (2)** :

| To | Subject |
|----|---------|
| admin@example.com | Nouveau contact: Test Après Fix |
| test@apresfix.com | Confirmation - Nous avons bien reçu... |

---

## 📊 Comparaison Avant/Après

### Logs AVANT la correction

```
ERROR - Erreur lors de l'envoi de l'email à l'admin: Could not parse mail
ERROR - Erreur lors de l'envoi de l'email de confirmation: Could not parse mail
```

### Logs APRÈS la correction

```
INFO  - Email de notification envoyé à l'admin pour le lead: test@apresfix.com
INFO  - Email de confirmation envoyé à: test@apresfix.com
```

---

## 🔄 Résumé du flux email (corrigé)

```
1. POST /api/contact
   └── Lead créé dans la base de données

2. EmailService.sendNotificationToAdmin()
   └── from: noreply@example.com  ← Maintenant valide !
   └── to: admin@example.com
   └── Envoi via SMTP (mailhog:1025)

3. EmailService.sendConfirmationToVisitor()
   └── from: noreply@example.com  ← Maintenant valide !
   └── to: email du visiteur
   └── Envoi via SMTP (mailhog:1025)

4. MailHog capture les 2 emails
   └── Visibles sur le port 8025
```

---

## 📋 Variables d'environnement complètes

Pour éviter ce problème, assurez-vous d'avoir **toutes** ces variables :

```yaml
environment:
  # Profil
  SPRING_PROFILES_ACTIVE: prod
  
  # Base de données
  DB_HOST: postgres
  DB_PORT: 5432
  DB_NAME: contactdb
  DB_USER: postgres
  DB_PASSWORD: postgres
  
  # Email - TOUTES ces variables !
  MAIL_HOST: mailhog
  MAIL_PORT: 1025
  MAIL_USER: noreply@example.com   # ← OBLIGATOIRE !
  ADMIN_EMAIL: admin@example.com
  
  # JWT
  JWT_SECRET: votre_secret_base64
  JWT_EXPIRATION: 86400000
```

---

## 📚 Leçons apprises

### 1. Toujours vérifier les logs en cas de problème

```bash
docker logs contact-api | grep -i error
```

### 2. Vérifier les variables d'environnement

```bash
docker exec contact-api env | grep MAIL
```

### 3. L'erreur "Could not parse mail" signifie généralement

- Adresse `from` vide ou invalide
- Adresse `to` vide ou invalide
- Format d'email incorrect

### 4. Spring Mail exige une adresse d'expéditeur valide

Même si MailHog n'en a pas besoin techniquement, Spring Mail **valide** le format de l'email avant l'envoi.

---

## ✅ Checklist de validation

- [ ] `MAIL_USER` présent dans docker-compose.yml
- [ ] `docker exec contact-api env | grep MAIL_USER` retourne une valeur
- [ ] Les logs montrent `INFO - Email ... envoyé` (pas d'ERROR)
- [ ] MailHog affiche les emails (Inbox > 0)

---

## 🔗 Fichiers concernés

| Fichier | Modification |
|---------|--------------|
| `docker-compose.yml` | Ajout de `MAIL_USER: noreply@example.com` |
| `.env.example` | Déjà présent (pour référence) |
| `application.yml` | Pas de modification nécessaire |

---

## 🎉 Résultat final

Après la correction, les emails fonctionnent correctement :

```
┌─────────────────────────────────────────────────┐
│ 🐷 MailHog                                      │
├─────────────────────────────────────────────────┤
│                                                 │
│ Inbox (2)                    ← Emails reçus !  │
│                                                 │
│ ┌─────────────────────────────────────────────┐ │
│ │ From: noreply@example.com                   │ │
│ │ To: admin@example.com                       │ │
│ │ Subject: Nouveau contact: Test Après Fix    │ │
│ └─────────────────────────────────────────────┘ │
│                                                 │
│ ┌─────────────────────────────────────────────┐ │
│ │ From: noreply@example.com                   │ │
│ │ To: test@apresfix.com                       │ │
│ │ Subject: Confirmation - Nous avons bien... │ │
│ └─────────────────────────────────────────────┘ │
│                                                 │
└─────────────────────────────────────────────────┘
```

