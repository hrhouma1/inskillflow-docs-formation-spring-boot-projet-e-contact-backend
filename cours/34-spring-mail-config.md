# Chapitre 7.1 - Spring Mail Configuration

## Objectifs du chapitre

- Configurer Spring Mail
- Comprendre les parametres SMTP
- Tester avec MailHog

---

## 1. Introduction a Spring Mail

### Qu'est-ce que Spring Mail?

Spring Mail est un module qui simplifie l'envoi d'emails en Java. Il s'integre avec JavaMail API.

### Dependance

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

---

## 2. Configuration SMTP

### Parametres de base

| Parametre | Description |
|-----------|-------------|
| host | Serveur SMTP (ex: smtp.gmail.com) |
| port | Port SMTP (25, 465, 587, 1025...) |
| username | Nom d'utilisateur |
| password | Mot de passe |
| auth | Authentification requise |
| starttls | Chiffrement TLS |

### application.yml - Developpement (MailHog)

```yaml
spring:
  mail:
    host: localhost        # ou mailhog (dans Docker)
    port: 1025            # Port SMTP de MailHog
    username:             # Pas d'authentification
    password:
    properties:
      mail:
        smtp:
          auth: false
          starttls:
            enable: false
```

### application.yml - Production (Gmail)

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${GMAIL_USER}
    password: ${GMAIL_PASSWORD}  # Mot de passe d'application
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
```

---

## 3. Configuration par profils

### application.yml complet

```yaml
spring:
  profiles:
    active: dev

---
# Profil DEV
spring:
  config:
    activate:
      on-profile: dev
  mail:
    host: localhost
    port: 1025
    properties:
      mail:
        smtp:
          auth: false

---
# Profil PROD
spring:
  config:
    activate:
      on-profile: prod
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USER}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

---

## 4. MailHog pour le developpement

### Qu'est-ce que MailHog?

MailHog est un serveur SMTP de test qui capture tous les emails sans les envoyer vraiment. Il offre une interface web pour les visualiser.

### Docker Compose

```yaml
services:
  mailhog:
    image: mailhog/mailhog
    container_name: contact-mailhog
    ports:
      - "1025:1025"  # SMTP
      - "8025:8025"  # Interface web
```

### Acces

- **SMTP**: localhost:1025
- **Interface web**: http://localhost:8025

### Avantages

1. Pas d'emails reels envoyes
2. Visualisation immediate
3. Pas de configuration complexe
4. Gratuit et leger

---

## 5. Gmail SMTP

### Prerequis

1. Compte Gmail
2. Authentification 2 facteurs activee
3. Mot de passe d'application genere

### Generer un mot de passe d'application

1. Aller sur https://myaccount.google.com/apppasswords
2. Selectionner "Autre (nom personnalise)"
3. Nommer l'application (ex: "Contact API")
4. Copier le mot de passe genere (16 caracteres)

### Configuration

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: votre.email@gmail.com
    password: abcd efgh ijkl mnop  # Mot de passe d'application
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

### Variables d'environnement

```bash
export GMAIL_USER=votre.email@gmail.com
export GMAIL_PASSWORD="abcd efgh ijkl mnop"
```

---

## 6. JavaMailSender

### Bean auto-configure

Spring Boot configure automatiquement un `JavaMailSender` a partir des proprietes.

### Utilisation basique

```java
@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("noreply@example.com");
        
        mailSender.send(message);
    }
}
```

---

## 7. Proprietes personnalisees

### application.yml

```yaml
app:
  mail:
    from: noreply@example.com
    admin-email: admin@example.com
    company-name: Mon Entreprise
```

### Utilisation avec @Value

```java
@Service
public class EmailService {
    
    @Value("${app.mail.from}")
    private String fromEmail;
    
    @Value("${app.mail.admin-email}")
    private String adminEmail;
    
    @Value("${app.mail.company-name}")
    private String companyName;
}
```

---

## 8. Docker Compose complet

### docker-compose.yml

```yaml
version: '3.8'

services:
  db:
    image: postgres:15-alpine
    container_name: contact-db
    environment:
      POSTGRES_DB: contact_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 5s
      retries: 5

  mailhog:
    image: mailhog/mailhog
    container_name: contact-mailhog
    ports:
      - "1025:1025"
      - "8025:8025"

  api:
    build: .
    container_name: contact-api
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_HOST: db
      DB_PORT: 5432
      DB_NAME: contact_db
      DB_USERNAME: postgres
      DB_PASSWORD: postgres
      MAIL_HOST: mailhog
      MAIL_PORT: 1025
      MAIL_USER: noreply@example.com
      ADMIN_EMAIL: admin@example.com
    depends_on:
      db:
        condition: service_healthy
      mailhog:
        condition: service_started

volumes:
  postgres_data:
```

---

## 9. Tester la configuration

### Test unitaire

```java
@SpringBootTest
class EmailServiceTest {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Test
    void shouldSendEmail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("test@example.com");
        message.setSubject("Test");
        message.setText("Contenu de test");
        message.setFrom("noreply@example.com");
        
        assertDoesNotThrow(() -> mailSender.send(message));
    }
}
```

### Verifier dans MailHog

1. Demarrer MailHog: `docker compose up -d mailhog`
2. Envoyer un email via l'API
3. Ouvrir http://localhost:8025
4. Verifier que l'email est arrive

---

## 10. Points cles a retenir

1. **spring-boot-starter-mail** pour les emails
2. **MailHog** pour le developpement (pas d'emails reels)
3. **Gmail** necessite un mot de passe d'application
4. **Variables d'environnement** pour les credentials
5. **Profils** pour separer dev et prod

---

## QUIZ 7.1 - Spring Mail Configuration

**1. Quelle dependance ajouter pour Spring Mail?**
   - a) spring-boot-starter-email
   - b) spring-boot-starter-mail
   - c) spring-boot-starter-smtp
   - d) spring-mail

**2. Quel est le port SMTP de MailHog?**
   - a) 25
   - b) 587
   - c) 1025
   - d) 8025

**3. Quel est le port de l'interface web de MailHog?**
   - a) 25
   - b) 587
   - c) 1025
   - d) 8025

**4. Quel port utiliser pour Gmail SMTP avec TLS?**
   - a) 25
   - b) 465
   - c) 587
   - d) 1025

**5. VRAI ou FAUX: MailHog envoie reellement les emails.**

**6. Qu'est-ce qu'un mot de passe d'application Gmail?**
   - a) Le mot de passe Gmail normal
   - b) Un mot de passe genere pour les applications tierces
   - c) Un mot de passe temporaire
   - d) Le code de verification 2FA

**7. Quelle interface Spring Boot utilise pour envoyer des emails?**
   - a) MailSender
   - b) EmailSender
   - c) JavaMailSender
   - d) SmtpSender

**8. Completez: starttls active le chiffrement _______.**

**9. Pourquoi utiliser MailHog en developpement?**
   - a) C'est plus rapide
   - b) Pour ne pas envoyer de vrais emails
   - c) C'est gratuit
   - d) b et c

**10. Comment configurer le serveur SMTP dans Spring Boot?**
   - a) spring.smtp.host
   - b) spring.mail.host
   - c) mail.server.host
   - d) smtp.server.host

---

### REPONSES QUIZ 7.1

1. b) spring-boot-starter-mail
2. c) 1025
3. d) 8025
4. c) 587
5. FAUX (il les capture sans les envoyer)
6. b) Un mot de passe genere pour les applications tierces
7. c) JavaMailSender
8. TLS
9. d) b et c
10. b) spring.mail.host

