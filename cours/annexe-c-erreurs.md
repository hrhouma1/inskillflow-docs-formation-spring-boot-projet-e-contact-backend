# Annexe C - Erreurs frequentes

## Erreurs de demarrage

### Circular Dependency

**Erreur:**
```
The dependencies of some of the beans in the application context form a cycle
```

**Solution:**
- Utiliser l'injection par methode au lieu du constructeur
- Extraire les beans dans des classes @Configuration separees
- Revoir l'architecture pour eliminer la dependance circulaire

---

### Could not resolve placeholder

**Erreur:**
```
Could not resolve placeholder 'MAIL_USER' in value "${MAIL_USER}"
```

**Solution:**
- Ajouter une valeur par defaut: `${MAIL_USER:}`
- Definir la variable d'environnement
- Verifier le fichier application.yml

---

### Port already in use

**Erreur:**
```
Web server failed to start. Port 8080 was already in use.
```

**Solution:**
```bash
# Trouver le processus
netstat -ano | findstr :8080  # Windows
lsof -i :8080                 # Linux/Mac

# Tuer le processus
taskkill /PID <pid> /F        # Windows
kill -9 <pid>                 # Linux/Mac

# Ou changer le port
server.port=8081
```

---

### Database connection refused

**Erreur:**
```
Connection refused to host: localhost port: 5432
```

**Solution:**
- Verifier que PostgreSQL/Docker est demarre
- Verifier les credentials (username, password)
- Verifier le nom de la base de donnees

---

## Erreurs JPA

### No default constructor

**Erreur:**
```
No default constructor for entity
```

**Solution:**
```java
@Entity
@NoArgsConstructor  // Ajouter cette annotation
public class Lead {
}
```

---

### LazyInitializationException

**Erreur:**
```
could not initialize proxy - no Session
```

**Solution:**
- Ajouter @Transactional sur le service
- Utiliser fetch join dans la requete
- Configurer FetchType.EAGER (avec precaution)

---

## Erreurs REST

### 401 Unauthorized

**Cause:** Token absent ou invalide.

**Solution:**
- Verifier le header `Authorization: Bearer TOKEN`
- Verifier que le token n'est pas expire
- Verifier la cle secrete JWT

---

### 403 Forbidden

**Cause:** Utilisateur authentifie mais pas autorise.

**Solution:**
- Verifier le role de l'utilisateur
- Verifier les regles @PreAuthorize

---

### 404 Not Found

**Cause:** URL incorrecte ou ressource inexistante.

**Solution:**
- Verifier l'URL
- Verifier que l'ID existe
- Verifier le @RequestMapping

---

### 400 Bad Request / Validation errors

**Cause:** Donnees invalides.

**Solution:**
- Verifier les contraintes @NotBlank, @Email, etc.
- Verifier le format JSON
- Verifier les types de donnees

---

## Erreurs Docker

### Image not found

**Erreur:**
```
pull access denied for myimage
```

**Solution:**
```bash
docker build -t myimage .  # Construire l'image d'abord
```

---

### Container exited immediately

**Solution:**
```bash
docker logs container_name  # Voir la cause
docker run -it image bash   # Debugger interactivement
```

---

### Volume permission denied

**Solution:**
```bash
# Linux: ajouter l'utilisateur au groupe docker
sudo usermod -aG docker $USER
```

---

## Erreurs Swagger

### Failed to fetch

**Cause:** CORS ou URL incorrecte.

**Solution:**
- Configurer CORS dans SecurityConfig
- Verifier que l'API est demarree
- Utiliser une URL relative dans OpenApiConfig

---

### No operations defined

**Cause:** Controller non scanne.

**Solution:**
- Verifier que le controller est dans le bon package
- Ajouter @RestController

---

## Erreurs Email

### Could not parse mail

**Cause:** Adresse email invalide ou manquante.

**Solution:**
- Verifier MAIL_USER dans les variables d'environnement
- Verifier le format de l'adresse

---

### Connection refused (SMTP)

**Cause:** Serveur mail non accessible.

**Solution:**
- Verifier que MailHog/SMTP est demarre
- Verifier host et port
- Verifier les credentials Gmail

---

## Debug general

### Activer les logs de debug

```yaml
logging:
  level:
    org.springframework: DEBUG
    org.hibernate.SQL: DEBUG
    com.example: DEBUG
```

### Voir les requetes SQL

```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

### Tester avec curl

```bash
curl -v http://localhost:8080/api/endpoint
```

