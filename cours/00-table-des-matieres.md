# COURS: API REST Spring Boot - Formulaire de Contact

## Table des matieres

---

### MODULE 1: INTRODUCTION ET FONDAMENTAUX

| Chapitre | Fichier | Quiz |
|----------|---------|------|
| 1.1 Presentation du projet | `01-presentation-projet.md` | Quiz 1.1 |
| 1.2 Spring Boot: Introduction | `02-spring-boot-introduction.md` | Quiz 1.2 |
| 1.3 Maven et gestion des dependances | `03-maven-dependances.md` | Quiz 1.3 |
| 1.4 Structure d'un projet Spring Boot | `04-structure-projet.md` | Quiz 1.4 |

---

### MODULE 2: ARCHITECTURE EN COUCHES

| Chapitre | Fichier | Quiz |
|----------|---------|------|
| 2.1 Architecture n-tiers (Layered) | `05-architecture-couches.md` | Quiz 2.1 |
| 2.2 Couche Model (Entites JPA) | `06-couche-model.md` | Quiz 2.2 |
| 2.3 Couche Repository (Spring Data JPA) | `07-couche-repository.md` | Quiz 2.3 |
| 2.4 Couche Service (Logique metier) | `08-couche-service.md` | Quiz 2.4 |
| 2.5 Couche Controller (API REST) | `09-couche-controller.md` | Quiz 2.5 |
| 2.6 Pattern DTO (Data Transfer Object) | `10-pattern-dto.md` | Quiz 2.6 |

---

### MODULE 3: API REST ET SPRING MVC

| Chapitre | Fichier | Quiz |
|----------|---------|------|
| 3.1 Principes REST | `11-principes-rest.md` | Quiz 3.1 |
| 3.2 Annotations Spring MVC | `12-annotations-spring-mvc.md` | Quiz 3.2 |
| 3.3 Methodes HTTP (GET, POST, PUT, DELETE) | `13-methodes-http.md` | Quiz 3.3 |
| 3.4 Codes de reponse HTTP | `14-codes-reponse-http.md` | Quiz 3.4 |
| 3.5 Validation des donnees | `15-validation-donnees.md` | Quiz 3.5 |

---

### MODULE 4: PERSISTANCE DES DONNEES

| Chapitre | Fichier | Quiz |
|----------|---------|------|
| 4.1 JPA et Hibernate | `16-jpa-hibernate.md` | Quiz 4.1 |
| 4.2 Annotations JPA (@Entity, @Id, @Column) | `17-annotations-jpa.md` | Quiz 4.2 |
| 4.3 Spring Data JPA Repositories | `18-spring-data-repositories.md` | Quiz 4.3 |
| 4.4 Requetes personnalisees | `19-requetes-personnalisees.md` | Quiz 4.4 |
| 4.5 Pagination et tri | `20-pagination-tri.md` | Quiz 4.5 |
| 4.6 Bases de donnees (H2 vs PostgreSQL) | `21-bases-donnees.md` | Quiz 4.6 |

---

### MODULE 5: SECURITE AVEC SPRING SECURITY

| Chapitre | Fichier | Quiz |
|----------|---------|------|
| 5.1 Introduction a Spring Security | `22-spring-security-intro.md` | Quiz 5.1 |
| 5.2 Configuration SecurityFilterChain | `23-security-filter-chain.md` | Quiz 5.2 |
| 5.3 Authentification vs Autorisation | `24-auth-vs-authz.md` | Quiz 5.3 |
| 5.4 UserDetails et UserDetailsService | `25-userdetails.md` | Quiz 5.4 |
| 5.5 Password Encoding (BCrypt) | `26-password-encoding.md` | Quiz 5.5 |
| 5.6 CORS (Cross-Origin Resource Sharing) | `27-cors.md` | Quiz 5.6 |

---

### MODULE 6: AUTHENTIFICATION JWT

| Chapitre | Fichier | Quiz |
|----------|---------|------|
| 6.1 Qu'est-ce que JWT? | `28-jwt-introduction.md` | Quiz 6.1 |
| 6.2 Structure d'un token JWT | `29-structure-jwt.md` | Quiz 6.2 |
| 6.3 Generation de tokens | `30-generation-tokens.md` | Quiz 6.3 |
| 6.4 Validation de tokens | `31-validation-tokens.md` | Quiz 6.4 |
| 6.5 Filtre JWT (JwtAuthFilter) | `32-filtre-jwt.md` | Quiz 6.5 |
| 6.6 Flux d'authentification complet | `33-flux-authentification.md` | Quiz 6.6 |

---

### MODULE 7: ENVOI D'EMAILS

| Chapitre | Fichier | Quiz |
|----------|---------|------|
| 7.1 Spring Mail Configuration | `34-spring-mail-config.md` | Quiz 7.1 |
| 7.2 JavaMailSender et MimeMessage | `35-javamail-sender.md` | Quiz 7.2 |
| 7.3 Emails HTML | `36-emails-html.md` | Quiz 7.3 |
| 7.4 Envoi asynchrone (@Async) | `37-envoi-asynchrone.md` | Quiz 7.4 |
| 7.5 MailHog pour les tests | `38-mailhog-tests.md` | Quiz 7.5 |
| 7.6 Gmail SMTP en production | `39-gmail-smtp.md` | Quiz 7.6 |

---

### MODULE 8: GESTION DES EXCEPTIONS

| Chapitre | Fichier | Quiz |
|----------|---------|------|
| 8.1 Types d'exceptions en Java | `40-types-exceptions.md` | Quiz 8.1 |
| 8.2 @ControllerAdvice | `41-controller-advice.md` | Quiz 8.2 |
| 8.3 @ExceptionHandler | `42-exception-handler.md` | Quiz 8.3 |
| 8.4 Exceptions personnalisees | `43-exceptions-personnalisees.md` | Quiz 8.4 |
| 8.5 Reponses d'erreur standardisees | `44-reponses-erreur.md` | Quiz 8.5 |

---

### MODULE 9: CONFIGURATION ET PROFILS

| Chapitre | Fichier | Quiz |
|----------|---------|------|
| 9.1 application.yml vs application.properties | `45-fichiers-config.md` | Quiz 9.1 |
| 9.2 Profils Spring (dev, prod) | `46-profils-spring.md` | Quiz 9.2 |
| 9.3 Variables d'environnement | `47-variables-environnement.md` | Quiz 9.3 |
| 9.4 @Value et @ConfigurationProperties | `48-injection-config.md` | Quiz 9.4 |
| 9.5 Externalisation de la configuration | `49-externalisation-config.md` | Quiz 9.5 |

---

### MODULE 10: DOCUMENTATION API

| Chapitre | Fichier | Quiz |
|----------|---------|------|
| 10.1 OpenAPI et Swagger | `50-openapi-swagger.md` | Quiz 10.1 |
| 10.2 Annotations OpenAPI | `51-annotations-openapi.md` | Quiz 10.2 |
| 10.3 Swagger UI | `52-swagger-ui.md` | Quiz 10.3 |
| 10.4 Documentation des endpoints | `53-documentation-endpoints.md` | Quiz 10.4 |

---

### MODULE 11: OUTILS DE DEVELOPPEMENT

| Chapitre | Fichier | Quiz |
|----------|---------|------|
| 11.1 Lombok et reduction du boilerplate | `54-lombok.md` | Quiz 11.1 |
| 11.2 DevTools et rechargement a chaud | `55-devtools.md` | Quiz 11.2 |
| 11.3 Tests unitaires | `56-tests-unitaires.md` | Quiz 11.3 |
| 11.4 Tests d'integration | `57-tests-integration.md` | Quiz 11.4 |

---

### MODULE 12: DOCKER ET DEPLOIEMENT

| Chapitre | Fichier | Quiz |
|----------|---------|------|
| 12.1 Introduction a Docker | `58-docker-introduction.md` | Quiz 12.1 |
| 12.2 Dockerfile pour Spring Boot | `59-dockerfile.md` | Quiz 12.2 |
| 12.3 Docker Compose | `60-docker-compose.md` | Quiz 12.3 |
| 12.4 Multi-stage builds | `61-multi-stage-builds.md` | Quiz 12.4 |
| 12.5 Deploiement en production | `62-deploiement-production.md` | Quiz 12.5 |

---

### MODULE 13: PATTERNS DE CONCEPTION

| Chapitre | Fichier | Quiz |
|----------|---------|------|
| 13.1 Pattern Repository | `63-pattern-repository.md` | Quiz 13.1 |
| 13.2 Pattern Service | `64-pattern-service.md` | Quiz 13.2 |
| 13.3 Pattern DTO/VO | `65-pattern-dto-vo.md` | Quiz 13.3 |
| 13.4 Injection de dependances | `66-injection-dependances.md` | Quiz 13.4 |
| 13.5 Inversion of Control (IoC) | `67-inversion-control.md` | Quiz 13.5 |

---

### MODULE 14: PROJET PRATIQUE

| Chapitre | Fichier | Quiz |
|----------|---------|------|
| 14.1 Analyse du code source | `68-analyse-code.md` | Quiz 14.1 |
| 14.2 Flux complet: soumission formulaire | `69-flux-soumission.md` | Quiz 14.2 |
| 14.3 Flux complet: authentification admin | `70-flux-authentification.md` | Quiz 14.3 |
| 14.4 Flux complet: gestion des leads | `71-flux-gestion-leads.md` | Quiz 14.4 |
| 14.5 Exercices pratiques | `72-exercices-pratiques.md` | - |

---

### ANNEXES

| Annexe | Fichier |
|--------|---------|
| A. Glossaire des termes | `annexe-a-glossaire.md` |
| B. Commandes utiles | `annexe-b-commandes.md` |
| C. Erreurs frequentes | `annexe-c-erreurs.md` |
| D. Ressources supplementaires | `annexe-d-ressources.md` |

---

## CONCEPTS COUVERTS PAR MODULE

### Correspondance concepts / modules

| Concept | Module(s) |
|---------|-----------|
| Spring Boot | 1 |
| Maven | 1 |
| Architecture en couches (Layered) | 2 |
| Spring MVC | 3 |
| API REST | 3 |
| Bean Validation | 3 |
| JPA / Hibernate | 4 |
| Spring Data JPA | 2, 4 |
| Spring Security | 5 |
| JWT (JSON Web Tokens) | 6 |
| CORS | 5 |
| Spring Mail | 7 |
| Programmation asynchrone (@Async) | 7 |
| Gestion des exceptions | 8 |
| @ControllerAdvice | 8 |
| Configuration et profils | 9 |
| Variables d'environnement | 9 |
| OpenAPI / Swagger | 10 |
| Lombok | 11 |
| Tests | 11 |
| Docker | 12 |
| Docker Compose | 12 |
| Design Patterns | 13 |
| Injection de dependances (DI) | 13 |
| Inversion of Control (IoC) | 13 |

---

## STRUCTURE DES FICHIERS DU PROJET

```
com.example.contact/
|
|-- ContactApplication.java          [Module 1, 2]
|
|-- config/
|   |-- DataInitializer.java         [Module 4, 9]
|   |-- OpenApiConfig.java           [Module 10]
|   |-- SecurityConfig.java          [Module 5, 6]
|   |-- UserDetailsConfig.java       [Module 5]
|
|-- controller/
|   |-- AuthController.java          [Module 3, 6]
|   |-- ContactController.java       [Module 3]
|   |-- LeadController.java          [Module 3, 5]
|
|-- dto/
|   |-- request/
|   |   |-- ContactFormRequest.java  [Module 2, 3]
|   |   |-- LoginRequest.java        [Module 2, 6]
|   |   |-- UpdateStatusRequest.java [Module 2]
|   |-- response/
|       |-- AuthResponse.java        [Module 2, 6]
|       |-- LeadDto.java             [Module 2]
|       |-- LeadStatsDto.java        [Module 2]
|       |-- MessageResponse.java     [Module 2]
|
|-- exception/
|   |-- GlobalExceptionHandler.java  [Module 8]
|   |-- ResourceNotFoundException.java [Module 8]
|
|-- model/
|   |-- Lead.java                    [Module 2, 4]
|   |-- User.java                    [Module 2, 4, 5]
|
|-- repository/
|   |-- LeadRepository.java          [Module 2, 4]
|   |-- UserRepository.java          [Module 2, 4]
|
|-- security/
|   |-- JwtAuthFilter.java           [Module 5, 6]
|   |-- JwtService.java              [Module 6]
|
|-- service/
    |-- EmailService.java            [Module 2, 7]
    |-- LeadService.java             [Module 2]
```

---

## FORMAT DES QUIZ

Chaque quiz contient:
- 5 a 10 questions
- Types: QCM, Vrai/Faux, Code a completer
- Reponses a la fin du fichier du chapitre

### Exemple de structure quiz

```
## QUIZ 2.1 - Architecture en couches

1. Combien de couches principales dans une architecture Spring Boot typique?
   a) 2
   b) 3
   c) 4
   d) 5

2. Quelle couche contient la logique metier?
   a) Controller
   b) Repository
   c) Service
   d) Model

3. VRAI ou FAUX: Un Controller peut appeler directement un Repository.

4. Completez: La couche _______ communique avec la base de donnees.

5. Dans quel ordre les couches sont-elles traversees lors d'une requete HTTP?
   a) Controller -> Service -> Repository -> Model
   b) Model -> Repository -> Service -> Controller
   c) Repository -> Service -> Controller -> Model
   d) Service -> Controller -> Repository -> Model

---
REPONSES: 1-c, 2-c, 3-Vrai (mais mauvaise pratique), 4-Repository, 5-a
```

---

## PROGRESSION RECOMMANDEE

| Semaine | Modules | Heures estimees |
|---------|---------|-----------------|
| 1 | 1, 2 | 6h |
| 2 | 3, 4 | 8h |
| 3 | 5, 6 | 8h |
| 4 | 7, 8 | 6h |
| 5 | 9, 10, 11 | 6h |
| 6 | 12, 13 | 6h |
| 7 | 14 (Projet) | 8h |

**Total: 48 heures**

---

## PREREQUIS

- Java 17 ou superieur
- Notions de programmation orientee objet
- Bases de SQL
- IDE (IntelliJ IDEA ou VS Code)
- Docker Desktop installe

