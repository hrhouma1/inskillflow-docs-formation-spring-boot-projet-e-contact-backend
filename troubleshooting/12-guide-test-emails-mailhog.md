# 12 - Guide Complet : Tester les Emails avec MailHog

## 🎯 Objectif

Tester le système d'envoi d'emails de bout en bout :
1. Comprendre comment fonctionne MailHog
2. Créer un lead pour déclencher l'envoi d'emails
3. Vérifier les emails dans MailHog
4. Analyser le contenu des emails

---

## 📧 Qu'est-ce que MailHog ?

### Définition

MailHog est un **serveur SMTP de test** qui :
- ✅ Capture tous les emails envoyés
- ✅ Les affiche dans une interface web
- ❌ Ne les envoie PAS réellement sur Internet

### Pourquoi l'utiliser ?

| En développement | En production |
|------------------|---------------|
| MailHog (capture) | Gmail, SendGrid (envoi réel) |
| Gratuit | Payant ou limité |
| Pas de spam | Emails réels |
| Test immédiat | Délai possible |

### Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        ARCHITECTURE                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐      ┌──────────────┐      ┌──────────────┐   │
│  │   Swagger    │      │  Spring Boot │      │   MailHog    │   │
│  │   (Client)   │      │   (API)      │      │   (SMTP)     │   │
│  └──────┬───────┘      └──────┬───────┘      └──────┬───────┘   │
│         │                     │                     │            │
│         │ POST /api/contact   │                     │            │
│         │────────────────────►│                     │            │
│         │                     │                     │            │
│         │                     │ sendEmail()         │            │
│         │                     │────────────────────►│            │
│         │                     │     (port 1025)     │            │
│         │                     │                     │            │
│         │                     │                     │ Stocke     │
│         │                     │                     │ l'email    │
│         │                     │                     │            │
│         │                     │                     ▼            │
│         │                     │              ┌──────────────┐   │
│         │                     │              │ Interface Web│   │
│         │                     │              │ (port 8025)  │   │
│         │                     │              └──────────────┘   │
│         │                     │                     │            │
│         │  200 OK             │                     │            │
│         │◄────────────────────│                     │            │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

# ÉTAPE 1 : VÉRIFIER QUE MAILHOG FONCTIONNE

## 📍 Étape 1.1 : Vérifier les conteneurs Docker

Dans le terminal Codespaces :

```bash
docker ps
```

**Résultat attendu :**
```
CONTAINER ID   IMAGE             STATUS         PORTS                    NAMES
xxxx           ...-api           Up X minutes   0.0.0.0:8080->8080/tcp   contact-api
xxxx           postgres          Up X minutes   0.0.0.0:5432->5432/tcp   contact-db
xxxx           mailhog/mailhog   Up X minutes   0.0.0.0:1025->1025/tcp,  contact-mailhog
                                                0.0.0.0:8025->8025/tcp
```

✅ Vérifiez que `contact-mailhog` est **Up**

---

## 📍 Étape 1.2 : Ouvrir l'interface MailHog

### Option A : Via l'onglet PORTS

1. En bas de VS Code, cliquez sur l'onglet **PORTS**
2. Trouvez le port **8025**
3. Cliquez sur l'icône 🌐 (globe) pour ouvrir dans le navigateur

### Option B : URL directe

```
https://expert-acorn-v6g97rv5x577fp4pg-8025.app.github.dev
```

> ⚠️ Remplacez `expert-acorn-v6g97rv5x577fp4pg` par le nom de votre Codespace

---

## 📍 Étape 1.3 : Vérifier l'interface

Vous devriez voir :

```
┌─────────────────────────────────────────────────┐
│ 🐷 MailHog                      🔍 Search       │
├─────────────────────────────────────────────────┤
│                                                 │
│ ⟳ Connected                                    │
│                                                 │
│ Inbox (0)                                       │
│                                                 │
│ ⊗ Delete all messages                          │
│                                                 │
└─────────────────────────────────────────────────┘
```

✅ **Inbox (0)** = Aucun email pour l'instant (c'est normal !)

---

# ÉTAPE 2 : CRÉER UN LEAD POUR ENVOYER DES EMAILS

## 📍 Étape 2.1 : Ouvrir Swagger UI

```
https://expert-acorn-v6g97rv5x577fp4pg-8080.app.github.dev/swagger-ui.html
```

---

## 📍 Étape 2.2 : Accéder à l'endpoint contact

1. Cliquez sur **contact-controller** pour le déplier
2. Cliquez sur `POST /api/contact`

---

## 📍 Étape 2.3 : Préparer la requête

1. Cliquez sur **Try it out**
2. Dans le champ **Request body**, collez :

```json
{
  "fullName": "Alice Martin",
  "company": "Test Company",
  "email": "alice@test.com",
  "phone": "514-555-0001",
  "requestType": "INFO",
  "message": "Ceci est un test pour vérifier que les emails fonctionnent correctement."
}
```

---

## 📍 Étape 2.4 : Envoyer la requête

1. Cliquez sur le bouton bleu **Execute**
2. Attendez la réponse

---

## 📍 Étape 2.5 : Vérifier la réponse

**Réponse attendue (Code 200) :**

```json
{
  "message": "Merci! Votre message a été envoyé. Nous vous répondrons bientôt."
}
```

✅ Si vous voyez ce message, le lead a été créé et les emails ont été envoyés !

---

# ÉTAPE 3 : VÉRIFIER LES EMAILS DANS MAILHOG

## 📍 Étape 3.1 : Retourner sur MailHog

Rafraîchissez la page MailHog (F5) ou cliquez sur le bouton ⟳

---

## 📍 Étape 3.2 : Vérifier la boîte de réception

Vous devriez maintenant voir :

```
┌─────────────────────────────────────────────────┐
│ 🐷 MailHog                      🔍 Search       │
├─────────────────────────────────────────────────┤
│                                                 │
│ ⟳ Connected                                    │
│                                                 │
│ Inbox (2)                    ← 2 emails !      │
│                                                 │
│ ┌─────────────────────────────────────────────┐ │
│ │ To: admin@example.com                       │ │
│ │ Subject: Nouveau contact: Alice Martin      │ │
│ │ Date: 2026-01-15 16:45                      │ │
│ └─────────────────────────────────────────────┘ │
│                                                 │
│ ┌─────────────────────────────────────────────┐ │
│ │ To: alice@test.com                          │ │
│ │ Subject: Confirmation - Nous avons bien... │ │
│ │ Date: 2026-01-15 16:45                      │ │
│ └─────────────────────────────────────────────┘ │
│                                                 │
└─────────────────────────────────────────────────┘
```

✅ **2 emails** sont apparus !

---

# ÉTAPE 4 : ANALYSER LE CONTENU DES EMAILS

## 📍 Étape 4.1 : Ouvrir l'email de notification admin

Cliquez sur l'email avec **To: admin@example.com**

---

## 📍 Étape 4.2 : Analyser l'email admin

**En-têtes :**
```
From: noreply@example.com
To: admin@example.com
Subject: Nouveau contact: Alice Martin
Date: Thu, 15 Jan 2026 16:45:00 +0000
```

**Corps du message :**
```
Nouveau contact reçu!

Nom: Alice Martin
Entreprise: Test Company
Email: alice@test.com
Téléphone: 514-555-0001
Type de demande: INFO

Message:
Ceci est un test pour vérifier que les emails fonctionnent correctement.

---
Reçu le: 2026-01-15T16:45:00.123456
```

---

## 📍 Étape 4.3 : Ouvrir l'email de confirmation visiteur

Cliquez sur le bouton **← Back** ou **Inbox**

Puis cliquez sur l'email avec **To: alice@test.com**

---

## 📍 Étape 4.4 : Analyser l'email de confirmation

**En-têtes :**
```
From: noreply@example.com
To: alice@test.com
Subject: Confirmation - Nous avons bien reçu votre message
Date: Thu, 15 Jan 2026 16:45:00 +0000
```

**Corps du message :**
```
Bonjour Alice Martin,

Merci de nous avoir contactés!

Nous avons bien reçu votre message concernant: INFO

Notre équipe vous répondra dans les plus brefs délais.

Cordialement,
L'équipe Support
```

---

# ÉTAPE 5 : TESTER PLUSIEURS TYPES DE DEMANDES

## 📍 Étape 5.1 : Créer un lead QUOTE (Devis)

Dans Swagger, `POST /api/contact` :

```json
{
  "fullName": "Bob Tremblay",
  "company": "Acme Corp",
  "email": "bob@acme.com",
  "phone": "514-555-0002",
  "requestType": "QUOTE",
  "message": "Je souhaite un devis pour 100 utilisateurs."
}
```

**Execute** → Vérifiez MailHog → **Inbox (4)**

---

## 📍 Étape 5.2 : Créer un lead SUPPORT

```json
{
  "fullName": "Claire Dubois",
  "email": "claire@email.com",
  "requestType": "SUPPORT",
  "message": "J'ai un problème de connexion à mon compte."
}
```

> Note : Pas de `company` ni `phone` (champs optionnels)

**Execute** → Vérifiez MailHog → **Inbox (6)**

---

## 📍 Étape 5.3 : Créer un lead PARTNERSHIP

```json
{
  "fullName": "David Roy",
  "company": "Partner Inc",
  "email": "david@partner.com",
  "phone": "514-555-0004",
  "requestType": "PARTNERSHIP",
  "message": "Nous souhaitons discuter d'un partenariat."
}
```

**Execute** → Vérifiez MailHog → **Inbox (8)**

---

# ÉTAPE 6 : FONCTIONNALITÉS AVANCÉES DE MAILHOG

## 📍 Étape 6.1 : Rechercher un email

1. Dans MailHog, utilisez la barre de recherche 🔍
2. Tapez : `bob@acme.com`
3. Seuls les emails liés à Bob apparaîtront

---

## 📍 Étape 6.2 : Voir les détails techniques

1. Ouvrez un email
2. Cliquez sur l'onglet **Source**
3. Vous verrez les en-têtes SMTP complets :

```
MIME-Version: 1.0
Content-Type: text/plain; charset=UTF-8
From: noreply@example.com
To: admin@example.com
Subject: Nouveau contact: Alice Martin
...
```

---

## 📍 Étape 6.3 : Supprimer tous les emails

1. Cliquez sur **Delete all messages**
2. Confirmez
3. **Inbox (0)** - Tous les emails sont supprimés

Utile pour repartir à zéro avant un nouveau test !

---

## 📍 Étape 6.4 : Tester "Jim" (Chaos Monkey)

Jim est un outil de test qui simule des problèmes :

1. Cliquez sur **Enable Jim**
2. Les prochains emails peuvent :
   - Être rejetés
   - Arriver lentement
   - Échouer aléatoirement

> ⚠️ Désactivez Jim pour les tests normaux !

---

# ÉTAPE 7 : VÉRIFIER LES LOGS D'ENVOI

## 📍 Étape 7.1 : Voir les logs de l'application

Dans le terminal Codespaces :

```bash
docker logs contact-api --tail 50 | grep -i email
```

**Résultat attendu :**
```
INFO  - Email de notification envoyé à l'admin pour le lead: alice@test.com
INFO  - Email de confirmation envoyé à: alice@test.com
INFO  - Email de notification envoyé à l'admin pour le lead: bob@acme.com
INFO  - Email de confirmation envoyé à: bob@acme.com
...
```

---

## 📍 Étape 7.2 : Voir les logs de MailHog

```bash
docker logs contact-mailhog --tail 20
```

**Résultat attendu :**
```
[APIv1] KEEPALIVE /api/v1/events
Creating message with ID: xxx
Creating message with ID: yyy
...
```

---

# ÉTAPE 8 : COMPRENDRE LE CODE D'ENVOI D'EMAILS

## 📍 Étape 8.1 : Le service EmailService

Fichier : `src/main/java/com/example/contact/service/EmailService.java`

```java
@Service
public class EmailService {

    @Async  // ← Envoi asynchrone (non-bloquant)
    public void sendNotificationToAdmin(Lead lead) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(adminEmail);           // ← admin@example.com
        message.setSubject("Nouveau contact: " + lead.getFullName());
        message.setText("...");
        
        mailSender.send(message);            // ← Envoi via SMTP
    }

    @Async
    public void sendConfirmationToVisitor(Lead lead) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(lead.getEmail());      // ← Email du visiteur
        message.setSubject("Confirmation - ...");
        message.setText("...");
        
        mailSender.send(message);
    }
}
```

---

## 📍 Étape 8.2 : Configuration SMTP

Fichier : `src/main/resources/application.yml`

```yaml
spring:
  mail:
    host: ${MAIL_HOST:mailhog}    # ← Serveur SMTP
    port: ${MAIL_PORT:1025}       # ← Port SMTP
    username: ${MAIL_USER:}       # ← (vide pour MailHog)
    password: ${MAIL_PASSWORD:}   # ← (vide pour MailHog)
```

---

## 📍 Étape 8.3 : Appel depuis LeadService

Fichier : `src/main/java/com/example/contact/service/LeadService.java`

```java
public LeadDto createLead(ContactFormRequest request) {
    // 1. Créer le lead
    Lead lead = Lead.builder()...build();
    Lead saved = leadRepository.save(lead);

    // 2. Envoyer les emails (asynchrone)
    emailService.sendNotificationToAdmin(saved);
    emailService.sendConfirmationToVisitor(saved);

    return mapToDto(saved);
}
```

---

# RÉCAPITULATIF

## 📊 Ce que vous avez appris

| Concept | Description |
|---------|-------------|
| **MailHog** | Serveur SMTP de test qui capture les emails |
| **Port 1025** | Port SMTP (envoi d'emails) |
| **Port 8025** | Interface web (lecture des emails) |
| **@Async** | Envoi asynchrone (non-bloquant) |
| **2 emails par lead** | 1 admin + 1 confirmation |

---

## ✅ Checklist finale

- [ ] MailHog accessible sur le port 8025
- [ ] Lead créé via Swagger
- [ ] 2 emails apparus dans MailHog
- [ ] Email admin avec les détails du lead
- [ ] Email confirmation envoyé au visiteur
- [ ] Logs d'envoi visibles dans les logs Docker

---

## 🔧 Dépannage

### Problème : Inbox reste à (0)

**Solutions :**
1. Vérifiez que `contact-mailhog` est running : `docker ps`
2. Vérifiez les logs : `docker logs contact-api | grep -i email`
3. Reconstruisez : `docker compose up --build -d`

### Problème : Erreur lors de l'envoi

**Vérifiez la configuration :**
```bash
docker exec contact-api env | grep MAIL
```

Devrait afficher :
```
MAIL_HOST=mailhog
MAIL_PORT=1025
```

---

## 🎉 Félicitations !

Vous maîtrisez maintenant le système d'emails avec MailHog !

