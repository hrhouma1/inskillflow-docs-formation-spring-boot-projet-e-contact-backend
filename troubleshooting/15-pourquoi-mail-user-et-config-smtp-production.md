# 15 - Pourquoi MAIL_USER est obligatoire + Configuration SMTP Production

## 🎯 Objectif

1. Comprendre pourquoi `MAIL_USER` est obligatoire
2. Configurer un vrai serveur SMTP pour la production (Gmail, SendGrid, etc.)

---

# PARTIE 1 : POURQUOI MAIL_USER EST OBLIGATOIRE

## 📬 Anatomie d'un email

Un email a **toujours** besoin de ces éléments :

```
┌─────────────────────────────────────────────────┐
│                    EMAIL                         │
├─────────────────────────────────────────────────┤
│                                                  │
│  FROM: noreply@example.com    ← OBLIGATOIRE !   │
│  TO:   admin@example.com      ← OBLIGATOIRE !   │
│  SUBJECT: Nouveau contact                        │
│                                                  │
│  ─────────────────────────────────────────────  │
│                                                  │
│  Bonjour,                                        │
│  Un nouveau contact a été reçu...                │
│                                                  │
└─────────────────────────────────────────────────┘
```

**Sans FROM (expéditeur)** → L'email est **invalide** → Erreur !

---

## 🔴 Ce qui se passe SANS MAIL_USER

### 1. Configuration application.yml

```yaml
spring:
  mail:
    username: ${MAIL_USER:}   # ← Si MAIL_USER n'existe pas, valeur = ""
```

### 2. EmailService.java

```java
@Value("${spring.mail.username:noreply@example.com}")
private String fromEmail;  // ← Devient "" si MAIL_USER est vide

public void sendEmail() {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromEmail);   // ← setFrom("") = INVALIDE !
    message.setTo("admin@example.com");
    mailSender.send(message);     // ← ERREUR: Could not parse mail
}
```

### 3. Résultat

```
ERROR - Could not parse mail
```

Spring Mail **refuse** d'envoyer un email sans expéditeur valide.

---

## ✅ Ce qui se passe AVEC MAIL_USER

```yaml
# docker-compose.yml
MAIL_USER: noreply@example.com
```

```
MAIL_USER=noreply@example.com
↓
spring.mail.username = "noreply@example.com"
↓
fromEmail = "noreply@example.com"
↓
message.setFrom("noreply@example.com")  ← VALIDE !
↓
Email envoyé avec succès ✅
```

---

## 📊 Comparaison

| Sans MAIL_USER | Avec MAIL_USER |
|----------------|----------------|
| `fromEmail = ""` | `fromEmail = "noreply@example.com"` |
| `setFrom("")` → ERREUR | `setFrom("noreply@example.com")` → OK |
| `Could not parse mail` | `Email envoyé` |
| MailHog: Inbox (0) | MailHog: Inbox (2) |

---

## 🤔 Pourquoi MailHog n'en a pas besoin techniquement ?

MailHog est un **faux serveur SMTP** qui accepte tout.

**MAIS** Spring Mail **valide l'email AVANT** de l'envoyer :

```
┌──────────────┐                    ┌──────────────┐
│  Spring Mail │                    │   MailHog    │
└──────┬───────┘                    └──────────────┘
       │
       │ 1. Valide le format FROM
       │    FROM="" → ERREUR ❌
       │    (L'email n'est jamais envoyé)
       │
       │ 2. Si valide, envoie via SMTP
       │────────────────────────────────►
```

L'erreur se produit **côté Spring**, pas côté MailHog.

---

## 📝 Analogie : La lettre postale

```
┌─────────────────────────────┐
│                             │
│  EXPÉDITEUR: ???            │  ← Sans adresse retour
│                             │
│  DESTINATAIRE:              │
│  Jean Dupont                │
│  123 rue Example            │
│                             │
└─────────────────────────────┘
```

La poste **refuserait** cette lettre : pas d'expéditeur = pas d'envoi !

---

# PARTIE 2 : CONFIGURATION SMTP EN PRODUCTION

## 🌐 Différence Dev vs Production

| Environnement | Serveur SMTP | Emails réels ? |
|---------------|--------------|----------------|
| **Développement** | MailHog | ❌ Non (capturés) |
| **Production** | Gmail, SendGrid, AWS SES | ✅ Oui (envoyés) |

---

## 📧 Option 1 : Gmail SMTP

### Prérequis

1. Un compte Gmail
2. Activer l'authentification 2 facteurs
3. Créer un **mot de passe d'application** :
   - Allez sur https://myaccount.google.com/apppasswords
   - Créez un mot de passe pour "Mail"
   - Copiez le mot de passe généré (16 caractères)

### Configuration docker-compose.yml

```yaml
environment:
  SPRING_PROFILES_ACTIVE: prod
  
  # Gmail SMTP
  MAIL_HOST: smtp.gmail.com
  MAIL_PORT: 587
  MAIL_USER: votre-email@gmail.com
  MAIL_PASSWORD: xxxx-xxxx-xxxx-xxxx   # Mot de passe d'application
  MAIL_AUTH: true
  MAIL_STARTTLS: true
  
  ADMIN_EMAIL: admin@votreentreprise.com
```

### Configuration application.yml (déjà présente)

```yaml
spring:
  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USER}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: ${MAIL_AUTH:true}
          starttls:
            enable: ${MAIL_STARTTLS:true}
            required: true
```

---

## 📧 Option 2 : SendGrid

### Prérequis

1. Créer un compte sur https://sendgrid.com
2. Créer une API Key dans Settings → API Keys
3. Vérifier votre domaine d'envoi

### Configuration docker-compose.yml

```yaml
environment:
  # SendGrid SMTP
  MAIL_HOST: smtp.sendgrid.net
  MAIL_PORT: 587
  MAIL_USER: apikey                          # Toujours "apikey"
  MAIL_PASSWORD: SG.xxxxx.yyyyy              # Votre API Key
  MAIL_AUTH: true
  MAIL_STARTTLS: true
  
  ADMIN_EMAIL: admin@votreentreprise.com
```

---

## 📧 Option 3 : AWS SES (Simple Email Service)

### Prérequis

1. Compte AWS
2. Configurer SES dans la région souhaitée
3. Vérifier les emails/domaines dans SES
4. Créer des credentials SMTP dans SES

### Configuration docker-compose.yml

```yaml
environment:
  # AWS SES SMTP
  MAIL_HOST: email-smtp.us-east-1.amazonaws.com
  MAIL_PORT: 587
  MAIL_USER: AKIAXXXXXXXXXXXXXXXX             # Access Key ID
  MAIL_PASSWORD: XXXXXXXXXXXXXXXXXXXXXXXX      # Secret Access Key
  MAIL_AUTH: true
  MAIL_STARTTLS: true
  
  ADMIN_EMAIL: admin@votreentreprise.com
```

---

## 📧 Option 4 : Mailgun

### Configuration docker-compose.yml

```yaml
environment:
  # Mailgun SMTP
  MAIL_HOST: smtp.mailgun.org
  MAIL_PORT: 587
  MAIL_USER: postmaster@votredomaine.mailgun.org
  MAIL_PASSWORD: votre-mot-de-passe-mailgun
  MAIL_AUTH: true
  MAIL_STARTTLS: true
  
  ADMIN_EMAIL: admin@votreentreprise.com
```

---

## 📊 Tableau comparatif des services

| Service | Prix | Limite gratuite | Difficulté |
|---------|------|-----------------|------------|
| **Gmail** | Gratuit | 500/jour | ⭐ Facile |
| **SendGrid** | Freemium | 100/jour | ⭐⭐ Moyen |
| **AWS SES** | $0.10/1000 | 62,000/mois (si sur EC2) | ⭐⭐⭐ Avancé |
| **Mailgun** | Freemium | 5,000/mois | ⭐⭐ Moyen |
| **Mailjet** | Freemium | 6,000/mois | ⭐⭐ Moyen |

---

## 🔐 Sécurité : Ne jamais exposer les secrets !

### ❌ MAUVAISE PRATIQUE

```yaml
# docker-compose.yml - NE PAS FAIRE ÇA !
MAIL_PASSWORD: mon_vrai_mot_de_passe   # ← DANGER si poussé sur GitHub !
```

### ✅ BONNE PRATIQUE : Utiliser un fichier .env

**1. Créer un fichier `.env` (non versionné) :**

```env
# .env - Ce fichier n'est PAS sur GitHub
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USER=contact@monentreprise.com
MAIL_PASSWORD=xxxx-xxxx-xxxx-xxxx
ADMIN_EMAIL=admin@monentreprise.com
```

**2. Modifier docker-compose.yml pour utiliser le .env :**

```yaml
environment:
  SPRING_PROFILES_ACTIVE: prod
  MAIL_HOST: ${MAIL_HOST}
  MAIL_PORT: ${MAIL_PORT}
  MAIL_USER: ${MAIL_USER}
  MAIL_PASSWORD: ${MAIL_PASSWORD}
  ADMIN_EMAIL: ${ADMIN_EMAIL}
```

**3. S'assurer que .env est dans .gitignore :**

```gitignore
# .gitignore
.env
.env.local
.env.production
```

---

## 🔄 Exemple complet : Passer de Dev à Prod

### Développement (MailHog)

```yaml
# docker-compose.yml
environment:
  MAIL_HOST: mailhog
  MAIL_PORT: 1025
  MAIL_USER: noreply@example.com
  # Pas de MAIL_PASSWORD (MailHog n'en a pas besoin)
  MAIL_AUTH: false
  MAIL_STARTTLS: false
```

### Production (Gmail)

```yaml
# docker-compose.yml
environment:
  MAIL_HOST: smtp.gmail.com
  MAIL_PORT: 587
  MAIL_USER: ${MAIL_USER}           # Depuis .env
  MAIL_PASSWORD: ${MAIL_PASSWORD}   # Depuis .env
  MAIL_AUTH: true
  MAIL_STARTTLS: true
```

---

## 🧪 Tester la configuration production

### Étape 1 : Vérifier les variables

```bash
docker exec contact-api env | grep MAIL
```

**Résultat attendu :**
```
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USER=contact@monentreprise.com
MAIL_PASSWORD=xxxx-xxxx-xxxx-xxxx
MAIL_AUTH=true
MAIL_STARTTLS=true
```

### Étape 2 : Envoyer un email de test

Créez un lead via Swagger avec **votre vraie adresse email** :

```json
{
  "fullName": "Test Production",
  "email": "votre-vrai-email@gmail.com",
  "requestType": "INFO",
  "message": "Test envoi email en production"
}
```

### Étape 3 : Vérifier votre boîte email

Vous devriez recevoir un **vrai email** dans votre boîte de réception !

---

## ⚠️ Problèmes courants en production

### Erreur : "Authentication failed"

**Cause** : Mauvais identifiants ou mot de passe d'application non créé (Gmail)

**Solution** :
1. Vérifiez `MAIL_USER` et `MAIL_PASSWORD`
2. Pour Gmail : créez un mot de passe d'application

### Erreur : "Connection refused"

**Cause** : Mauvais host ou port

**Solution** : Vérifiez `MAIL_HOST` et `MAIL_PORT`

### Erreur : "Relay access denied"

**Cause** : Le domaine d'envoi n'est pas vérifié

**Solution** : Vérifiez votre domaine dans SendGrid/AWS SES

### Les emails arrivent en SPAM

**Solutions** :
1. Configurez SPF, DKIM, DMARC pour votre domaine
2. Utilisez un service comme SendGrid avec réputation établie
3. Évitez les mots "spam" dans le sujet

---

## 📋 Checklist passage en production

- [ ] Choisir un service SMTP (Gmail, SendGrid, AWS SES...)
- [ ] Créer un compte et obtenir les credentials
- [ ] Configurer les variables dans `.env` (pas dans docker-compose.yml)
- [ ] Vérifier que `.env` est dans `.gitignore`
- [ ] Tester l'envoi avec une vraie adresse email
- [ ] Vérifier que l'email n'arrive pas en spam
- [ ] Configurer SPF/DKIM/DMARC si nécessaire

---

## 🎓 Résumé

### Pourquoi MAIL_USER ?

| Question | Réponse |
|----------|---------|
| C'est quoi ? | L'adresse de l'expéditeur (FROM) |
| Obligatoire ? | Oui, sinon `Could not parse mail` |
| En dev ? | `noreply@example.com` (fictif) |
| En prod ? | Votre vraie adresse email |

### Configuration par environnement

| Variable | Développement | Production |
|----------|---------------|------------|
| `MAIL_HOST` | mailhog | smtp.gmail.com |
| `MAIL_PORT` | 1025 | 587 |
| `MAIL_USER` | noreply@example.com | votre@email.com |
| `MAIL_PASSWORD` | (vide) | mot_de_passe_app |
| `MAIL_AUTH` | false | true |
| `MAIL_STARTTLS` | false | true |

