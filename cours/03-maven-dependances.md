# Chapitre 1.3 - Maven et gestion des dependances

## Objectifs du chapitre

- Comprendre le role de Maven
- Savoir lire et modifier un fichier pom.xml
- Connaitre les commandes Maven essentielles

---

## 1. Qu'est-ce que Maven?

### Definition

**Maven** est un outil de gestion de projet Java qui:

- Gere les dependances (bibliotheques externes)
- Standardise la structure du projet
- Automatise le build (compilation, tests, packaging)
- Gere le cycle de vie du projet

### Alternatives

| Outil | Langage | Fichier config |
|-------|---------|----------------|
| Maven | Java | pom.xml |
| Gradle | Java/Kotlin | build.gradle |
| npm | JavaScript | package.json |
| pip | Python | requirements.txt |

---

## 2. Le fichier pom.xml

### Structure de base

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <!-- Identite du projet -->
    <groupId>com.example</groupId>
    <artifactId>contact-api</artifactId>
    <version>1.0.0</version>
    
    <!-- Heritage Spring Boot -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <!-- Proprietes -->
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <!-- Dependances -->
    <dependencies>
        <!-- ... -->
    </dependencies>
    
    <!-- Configuration du build -->
    <build>
        <!-- ... -->
    </build>
</project>
```

### Coordonnees Maven (GAV)

Chaque artefact Maven est identifie par trois elements:

| Element | Description | Exemple |
|---------|-------------|---------|
| **G**roupId | Organisation/domaine | com.example |
| **A**rtifactId | Nom du projet | contact-api |
| **V**ersion | Version | 1.0.0 |

---

## 3. Les dependances

### Syntaxe d'une dependance

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <!-- version heritee du parent -->
</dependency>
```

### Scopes (portees)

| Scope | Compilation | Test | Runtime | Exemple |
|-------|-------------|------|---------|---------|
| compile (defaut) | Oui | Oui | Oui | spring-boot-starter-web |
| test | Non | Oui | Non | spring-boot-starter-test |
| runtime | Non | Oui | Oui | postgresql |
| provided | Oui | Oui | Non | lombok |

```xml
<!-- Exemple avec scope -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## 4. Dependances de notre projet

### Liste complete

```xml
<!-- Spring Web (REST API) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring Data JPA (persistence) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Spring Security (authentification) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Validation (contraintes sur les donnees) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Spring Mail (envoi emails) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- PostgreSQL (production) -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- H2 (developpement) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<!-- Swagger/OpenAPI -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

---

## 5. Commandes Maven essentielles

### Cycle de vie

```bash
# Nettoyer le projet (supprime target/)
mvn clean

# Compiler le code source
mvn compile

# Executer les tests
mvn test

# Creer le package (JAR/WAR)
mvn package

# Installer dans le repo local
mvn install
```

### Commandes Spring Boot

```bash
# Demarrer l'application
mvn spring-boot:run

# Demarrer avec un profil
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Creer le JAR executable
mvn clean package -DskipTests
```

### Commandes utiles

```bash
# Afficher l'arbre des dependances
mvn dependency:tree

# Telecharger les sources des dependances
mvn dependency:sources

# Verifier les mises a jour disponibles
mvn versions:display-dependency-updates
```

---

## 6. Structure standard Maven

```
projet/
|-- pom.xml
|-- src/
|   |-- main/
|   |   |-- java/           # Code source Java
|   |   |-- resources/      # Fichiers de configuration
|   |-- test/
|       |-- java/           # Tests unitaires
|       |-- resources/      # Ressources de test
|-- target/                 # Fichiers compiles (genere)
```

Cette structure est une **convention**. Maven sait ou trouver les fichiers sans configuration supplementaire.

---

## 7. Le parent Spring Boot

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>
```

Le parent fournit:

- Versions par defaut des dependances Spring
- Configuration du plugin Maven
- Profils de build
- Encoding UTF-8

Grace au parent, on n'a pas besoin de specifier les versions des starters Spring.

---

## 8. Properties (proprietes)

```xml
<properties>
    <java.version>17</java.version>
    <jjwt.version>0.12.3</jjwt.version>
</properties>
```

Les proprietes permettent de:
- Centraliser les versions
- Eviter la repetition
- Faciliter les mises a jour

Usage:

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>${jjwt.version}</version>
</dependency>
```

---

## 9. Points cles a retenir

1. **pom.xml** est le coeur du projet Maven
2. Les dependances sont identifiees par **groupId:artifactId:version**
3. Les **scopes** controlent la disponibilite des dependances
4. Le **parent Spring Boot** simplifie la gestion des versions
5. **mvn spring-boot:run** demarre l'application

---

## QUIZ 1.3 - Maven et dependances

**1. Quel fichier contient les dependances Maven?**
   - a) build.gradle
   - b) pom.xml
   - c) package.json
   - d) dependencies.xml

**2. Que signifie GAV dans Maven?**
   - a) Group, Application, Version
   - b) GroupId, ArtifactId, Version
   - c) General, Artifact, Value
   - d) Global, App, Variant

**3. Quel scope est utilise pour les tests uniquement?**
   - a) compile
   - b) runtime
   - c) test
   - d) provided

**4. Quelle commande compile le projet?**
   - a) mvn build
   - b) mvn compile
   - c) mvn make
   - d) mvn run

**5. VRAI ou FAUX: Le dossier target/ doit etre commit dans Git.**

**6. Quelle commande demarre une application Spring Boot?**
   - a) mvn start
   - b) mvn run
   - c) mvn spring-boot:run
   - d) mvn boot:start

**7. Ou se trouvent les fichiers de configuration (application.yml)?**
   - a) src/main/java
   - b) src/main/resources
   - c) src/config
   - d) config/

**8. Completez: Le parent Spring Boot fournit les _______ par defaut des dependances.**

**9. Quel scope indique qu'une dependance n'est pas necessaire au runtime?**
   - a) compile
   - b) runtime
   - c) provided
   - d) test

**10. Quelle commande affiche l'arbre des dependances?**
   - a) mvn dependencies
   - b) mvn dependency:tree
   - c) mvn list-deps
   - d) mvn show:dependencies

---

### REPONSES QUIZ 1.3

1. b) pom.xml
2. b) GroupId, ArtifactId, Version
3. c) test
4. b) mvn compile
5. FAUX (target/ est dans .gitignore)
6. c) mvn spring-boot:run
7. b) src/main/resources
8. versions
9. c) provided
10. b) mvn dependency:tree

