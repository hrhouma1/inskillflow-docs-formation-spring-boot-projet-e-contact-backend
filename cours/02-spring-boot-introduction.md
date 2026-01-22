# Chapitre 1.2 - Spring Boot: Introduction

## Objectifs du chapitre

- Comprendre ce qu'est Spring Boot
- Connaitre les avantages par rapport a Spring classique
- Identifier les composants principaux

---

## 1. Qu'est-ce que Spring Boot?

### Definition

**Spring Boot** est un framework Java qui simplifie la creation d'applications Spring. Il fournit:

- Une configuration automatique
- Des serveurs embarques (Tomcat, Jetty)
- Des starters pour les dependances
- Une approche "convention over configuration"

### Spring vs Spring Boot

| Aspect | Spring (classique) | Spring Boot |
|--------|-------------------|-------------|
| Configuration | XML ou Java verbose | Auto-configuration |
| Serveur | Externe (WAR) | Embarque (JAR) |
| Demarrage | Long et complexe | Rapide et simple |
| Dependances | Manuelles | Starters |

---

## 2. Principes fondamentaux

### 2.1 Convention over Configuration

Spring Boot fait des choix par defaut sensibles. Si vous ne specifiez rien:

- Port: 8080
- Contexte: /
- Logging: INFO
- Base H2: en memoire

Vous ne configurez que ce qui differe des conventions.

### 2.2 Starters

Les starters sont des ensembles de dependances pre-configures:

```xml
<!-- Un seul starter au lieu de 10+ dependances -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

Starters utilises dans notre projet:

| Starter | Fournit |
|---------|---------|
| spring-boot-starter-web | Spring MVC, Tomcat, JSON |
| spring-boot-starter-data-jpa | JPA, Hibernate, Spring Data |
| spring-boot-starter-security | Spring Security |
| spring-boot-starter-mail | JavaMail |
| spring-boot-starter-validation | Bean Validation |

### 2.3 Auto-configuration

Spring Boot detecte les dependances et configure automatiquement:

```
Dependance detectee         Configuration automatique
-------------------         -------------------------
spring-boot-starter-web --> DispatcherServlet, Jackson
spring-boot-starter-jpa --> EntityManagerFactory, DataSource
H2 dans le classpath    --> Base H2 en memoire
```

---

## 3. Structure d'une application Spring Boot

### 3.1 Classe principale

```java
@SpringBootApplication
public class ContactApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContactApplication.class, args);
    }
}
```

### 3.2 L'annotation @SpringBootApplication

Cette annotation combine trois annotations:

```java
@SpringBootApplication
// Equivalent a:
@Configuration           // Cette classe est une source de configuration
@EnableAutoConfiguration // Active l'auto-configuration
@ComponentScan          // Scanne les composants dans ce package et sous-packages
```

### 3.3 La methode main()

```java
public static void main(String[] args) {
    SpringApplication.run(ContactApplication.class, args);
}
```

Cette ligne:
1. Cree le contexte Spring (ApplicationContext)
2. Effectue l'auto-configuration
3. Scanne les composants (@Controller, @Service, etc.)
4. Demarre le serveur embarque

---

## 4. Les annotations de base

### 4.1 Annotations de composants

| Annotation | Role |
|------------|------|
| @Component | Composant generique |
| @Controller | Controleur web (renvoie des vues) |
| @RestController | Controleur REST (renvoie du JSON) |
| @Service | Logique metier |
| @Repository | Acces aux donnees |
| @Configuration | Classe de configuration |

### 4.2 Injection de dependances

```java
// Injection par constructeur (recommandee)
@Service
public class LeadService {
    private final LeadRepository repository;
    
    public LeadService(LeadRepository repository) {
        this.repository = repository;
    }
}

// Avec Lombok (simplifie)
@Service
@RequiredArgsConstructor
public class LeadService {
    private final LeadRepository repository;
}
```

---

## 5. Cycle de vie de l'application

```
1. main() appellee
       |
       v
2. SpringApplication.run()
       |
       v
3. Chargement de l'environnement
       |
       v
4. Creation du contexte Spring
       |
       v
5. Auto-configuration
       |
       v
6. Scan des composants
       |
       v
7. Creation des beans
       |
       v
8. Demarrage du serveur embarque
       |
       v
9. Application prete!
```

---

## 6. Fichier de configuration

### application.yml vs application.properties

```yaml
# application.yml (format YAML)
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:testdb
```

```properties
# application.properties (format properties)
server.port=8080
spring.datasource.url=jdbc:h2:mem:testdb
```

Les deux formats sont equivalents. YAML est plus lisible pour les configurations complexes.

---

## 7. Demarrer l'application

### En ligne de commande

```bash
# Avec Maven
mvn spring-boot:run

# Avec le JAR
java -jar app.jar

# Avec un profil specifique
java -jar app.jar --spring.profiles.active=prod
```

### Dans un IDE

Executer la methode `main()` de la classe principale.

---

## 8. Points cles a retenir

1. **Spring Boot simplifie Spring** avec l'auto-configuration
2. **Les starters** regroupent les dependances coherentes
3. **@SpringBootApplication** est le point d'entree
4. **Le serveur est embarque** (pas de deploiement WAR)
5. **Configuration par fichiers** application.yml ou .properties

---

## QUIZ 1.2 - Spring Boot Introduction

**1. Qu'est-ce que Spring Boot?**
   - a) Un nouveau langage de programmation
   - b) Un framework qui simplifie Spring
   - c) Un serveur d'applications
   - d) Une base de donnees

**2. Quelle annotation marque la classe principale?**
   - a) @SpringApplication
   - b) @MainClass
   - c) @SpringBootApplication
   - d) @Application

**3. Quel est le port par defaut de Spring Boot?**
   - a) 80
   - b) 443
   - c) 3000
   - d) 8080

**4. Qu'est-ce qu'un starter?**
   - a) Un type de base de donnees
   - b) Un ensemble de dependances pre-configures
   - c) Un outil de test
   - d) Un serveur web

**5. VRAI ou FAUX: Spring Boot necessite un serveur externe comme Tomcat.**

**6. Quelle annotation est utilisee pour un service?**
   - a) @Component
   - b) @Controller
   - c) @Service
   - d) @Bean

**7. @SpringBootApplication combine quelles annotations?**
   - a) @Configuration, @EnableAutoConfiguration, @ComponentScan
   - b) @Controller, @Service, @Repository
   - c) @Bean, @Autowired, @Component
   - d) @RestController, @RequestMapping, @GetMapping

**8. Completez: L'approche de Spring Boot est "_______ over Configuration".**

**9. Quel format de configuration est plus lisible pour les structures complexes?**
   - a) .properties
   - b) .xml
   - c) .yml
   - d) .json

**10. Quelle commande Maven demarre l'application?**
   - a) mvn run
   - b) mvn start
   - c) mvn spring-boot:run
   - d) mvn boot:start

---

### REPONSES QUIZ 1.2

1. b) Un framework qui simplifie Spring
2. c) @SpringBootApplication
3. d) 8080
4. b) Un ensemble de dependances pre-configures
5. FAUX (le serveur est embarque)
6. c) @Service
7. a) @Configuration, @EnableAutoConfiguration, @ComponentScan
8. Convention
9. c) .yml
10. c) mvn spring-boot:run

