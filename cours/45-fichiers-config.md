# Chapitre 9.1 - application.yml vs application.properties

## Objectifs du chapitre

- Connaitre les formats de configuration
- Maitriser la syntaxe YAML
- Organiser la configuration

---

## 1. Deux formats disponibles

### application.properties

Format cle=valeur traditionnel.

```properties
server.port=8080
spring.datasource.url=jdbc:postgresql://localhost:5432/db
spring.datasource.username=postgres
spring.jpa.hibernate.ddl-auto=update
```

### application.yml

Format YAML hierarchique.

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/db
    username: postgres
  jpa:
    hibernate:
      ddl-auto: update
```

---

## 2. Comparaison

| Aspect | .properties | .yml |
|--------|-------------|------|
| Syntaxe | Plate | Hierarchique |
| Lisibilite | Moyenne | Bonne |
| Repetition | Oui | Non |
| Listes | Difficile | Facile |
| Profils | Fichiers separes | Dans le meme fichier |

---

## 3. Syntaxe YAML

### Hierarchie (indentation)

```yaml
spring:
  datasource:          # 2 espaces
    url: jdbc:...      # 4 espaces
    username: user     # 4 espaces
```

### Listes

```yaml
# Liste simple
cors:
  allowed-origins:
    - http://localhost:3000
    - https://monsite.com
    
# Liste inline
allowed-methods: [GET, POST, PUT, DELETE]
```

### Variables d'environnement

```yaml
spring:
  datasource:
    username: ${DB_USERNAME:postgres}    # Defaut: postgres
    password: ${DB_PASSWORD}             # Pas de defaut
```

### Multilignes

```yaml
description: |
  Premiere ligne
  Deuxieme ligne
  
message: >
  Ceci est une
  seule ligne
```

---

## 4. Configuration complete du projet

### application.yml

```yaml
spring:
  application:
    name: contact-api
    
  profiles:
    active: dev

---
# Profil DEV
spring:
  config:
    activate:
      on-profile: dev
      
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
    
  h2:
    console:
      enabled: true
      path: /h2-console
      
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    
  mail:
    host: localhost
    port: 1025

server:
  port: 8080

app:
  jwt:
    secret: dev-secret-key-for-testing-only
    expiration: 86400000
  admin-email: admin@example.com

---
# Profil PROD
spring:
  config:
    activate:
      on-profile: prod
      
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:contact_db}
    driver-class-name: org.postgresql.Driver
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
    
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    
  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USER:}
    password: ${MAIL_PASSWORD:}
    properties:
      mail:
        smtp:
          auth: ${MAIL_AUTH:true}
          starttls:
            enable: true

server:
  port: ${PORT:8080}

app:
  jwt:
    secret: ${JWT_SECRET}
    expiration: ${JWT_EXPIRATION:86400000}
  admin-email: ${ADMIN_EMAIL:admin@example.com}
```

---

## 5. Profils dans un seul fichier

### Separateur

```yaml
# Configuration commune
spring:
  application:
    name: my-app

---
# Profil dev
spring:
  config:
    activate:
      on-profile: dev
# ...

---
# Profil prod
spring:
  config:
    activate:
      on-profile: prod
# ...
```

Le `---` separe les documents YAML.

---

## 6. Priorite des sources

Du moins prioritaire au plus prioritaire:

1. application.yml (dans le JAR)
2. application-{profile}.yml
3. Variables d'environnement
4. Arguments de ligne de commande

```bash
# Les arguments CLI ont la priorite
java -jar app.jar --server.port=9090
```

---

## 7. Lire la configuration

### @Value

```java
@Service
public class EmailService {
    
    @Value("${app.admin-email}")
    private String adminEmail;
    
    @Value("${app.jwt.expiration:3600000}")  // Avec defaut
    private long jwtExpiration;
}
```

### @ConfigurationProperties

```java
@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {
    private String adminEmail;
    private Jwt jwt;
    
    @Data
    public static class Jwt {
        private String secret;
        private long expiration;
    }
}
```

Utilisation:

```java
@Service
@RequiredArgsConstructor
public class JwtService {
    private final AppProperties props;
    
    public String getSecret() {
        return props.getJwt().getSecret();
    }
}
```

---

## 8. Bonnes pratiques

### 8.1 Valeurs par defaut

```yaml
# Toujours avoir des defauts pour le dev
database:
  host: ${DB_HOST:localhost}
  port: ${DB_PORT:5432}
```

### 8.2 Pas de secrets en dur

```yaml
# MAUVAIS
jwt:
  secret: ma-cle-secrete

# BON
jwt:
  secret: ${JWT_SECRET}
```

### 8.3 Grouper par fonctionnalite

```yaml
# BON: groupe par domaine
app:
  jwt:
    secret: ...
    expiration: ...
  mail:
    from: ...
    admin: ...
```

### 8.4 Commenter la configuration

```yaml
server:
  port: 8080  # Port d'ecoute de l'API

spring:
  jpa:
    hibernate:
      ddl-auto: update  # 'validate' en production!
```

---

## 9. Points cles a retenir

1. **YAML** est plus lisible que properties
2. **Profils** permettent des configs differentes
3. **Variables d'environnement** pour les secrets
4. **Valeurs par defaut** avec ${VAR:default}
5. **@Value** ou **@ConfigurationProperties** pour lire

---

## QUIZ 9.1 - Fichiers de configuration

**1. Quel format est hierarchique?**
   - a) .properties
   - b) .yml
   - c) Les deux
   - d) Aucun

**2. Quelle est la syntaxe pour une valeur par defaut?**
   - a) ${VAR||default}
   - b) ${VAR:-default}
   - c) ${VAR:default}
   - d) ${VAR?default}

**3. Quel caractere separe les documents YAML?**
   - a) ===
   - b) ---
   - c) ***
   - d) +++

**4. Quelle annotation lit une propriete?**
   - a) @Property
   - b) @Config
   - c) @Value
   - d) @Setting

**5. VRAI ou FAUX: Les variables d'environnement ont priorite sur application.yml.**

**6. Combien d'espaces pour l'indentation YAML?**
   - a) 1
   - b) 2
   - c) 4
   - d) Tabulation

**7. Comment activer un profil?**
   - a) spring.profile=dev
   - b) spring.profiles.active=dev
   - c) profile.active=dev
   - d) active.profile=dev

**8. Completez: @ConfigurationProperties permet de mapper vers un _______.**

**9. Ou NE PAS mettre de secrets?**
   - a) Variables d'environnement
   - b) Fichiers de config commites
   - c) Secrets managers
   - d) .env (gitignore)

**10. Quel format supporte mieux les listes?**
   - a) .properties
   - b) .yml
   - c) Pareil
   - d) Ni l'un ni l'autre

---

### REPONSES QUIZ 9.1

1. b) .yml
2. c) ${VAR:default}
3. b) ---
4. c) @Value
5. VRAI
6. b) 2
7. b) spring.profiles.active=dev
8. objet (ou POJO)
9. b) Fichiers de config commites
10. b) .yml

