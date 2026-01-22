# Chapitre 2.2 - Couche Model (Entites JPA)

## Objectifs du chapitre

- Creer des entites JPA
- Utiliser les annotations de mapping
- Comprendre les relations entre entites

---

## 1. Qu'est-ce qu'une entite?

### Definition

Une **entite** est une classe Java qui represente une table de la base de donnees. Chaque instance de la classe correspond a une ligne de la table.

### Mapping Objet-Relationnel (ORM)

```
Classe Java (Entite)     <---->     Table SQL
--------------------------------   --------------------------------
Lead.java                          leads
  - Long id                          - id BIGINT PRIMARY KEY
  - String fullName                  - full_name VARCHAR(255)
  - String email                     - email VARCHAR(255)
```

---

## 2. Anatomie d'une entite

### Exemple complet: Lead.java

```java
package com.example.contact.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity                              // 1. Marque la classe comme entite
@Table(name = "leads")               // 2. Nom de la table
@Data                                // 3. Lombok: getters, setters, etc.
@NoArgsConstructor
@AllArgsConstructor
public class Lead {

    @Id                              // 4. Cle primaire
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 5. Auto-increment
    private Long id;

    @Column(nullable = false)        // 6. Colonne NOT NULL
    private String fullName;

    @Column(nullable = false)
    private String email;

    private String company;          // 7. Colonne nullable par defaut

    private String phone;

    @Enumerated(EnumType.STRING)     // 8. Enum stocke comme texte
    @Column(nullable = false)
    private RequestType requestType;

    @Column(columnDefinition = "TEXT")  // 9. Type TEXT pour longs textes
    private String message;

    @Enumerated(EnumType.STRING)
    private LeadStatus status = LeadStatus.NEW;  // 10. Valeur par defaut

    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt;
}
```

---

## 3. Annotations essentielles

### 3.1 @Entity

Indique que la classe est une entite JPA.

```java
@Entity
public class Lead {
    // ...
}
```

### 3.2 @Table

Specifie le nom de la table (optionnel si nom = classe).

```java
@Table(name = "leads")
// ou avec schema
@Table(name = "leads", schema = "public")
```

### 3.3 @Id

Marque le champ comme cle primaire.

```java
@Id
private Long id;
```

### 3.4 @GeneratedValue

Definit la strategie de generation de l'ID.

| Strategie | Description |
|-----------|-------------|
| IDENTITY | Auto-increment (PostgreSQL, MySQL) |
| SEQUENCE | Sequence (PostgreSQL, Oracle) |
| TABLE | Table de sequences |
| AUTO | Choix automatique |

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

### 3.5 @Column

Configure une colonne.

```java
@Column(
    name = "full_name",       // Nom de la colonne
    nullable = false,         // NOT NULL
    unique = true,           // UNIQUE
    length = 100,            // VARCHAR(100)
    columnDefinition = "TEXT" // Type exact
)
private String fullName;
```

### 3.6 @Enumerated

Stocke un enum dans la base.

```java
// Stocke "NEW", "CONTACTED", etc. (recommande)
@Enumerated(EnumType.STRING)
private LeadStatus status;

// Stocke 0, 1, 2... (deconseille)
@Enumerated(EnumType.ORDINAL)
private LeadStatus status;
```

---

## 4. Les enums du projet

### RequestType.java

```java
public enum RequestType {
    INFO,        // Demande d'information
    DEMO,        // Demande de demo
    SUPPORT,     // Support technique
    PARTNERSHIP, // Partenariat
    OTHER        // Autre
}
```

### LeadStatus.java

```java
public enum LeadStatus {
    NEW,        // Nouveau lead
    CONTACTED,  // Contacte
    CONVERTED,  // Converti en client
    LOST        // Perdu
}
```

---

## 5. Entite User

### User.java

```java
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role = Role.ADMIN;

    // Implementation de UserDetails pour Spring Security
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    
    @Override
    public boolean isAccountNonLocked() { return true; }
    
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    
    @Override
    public boolean isEnabled() { return true; }
}
```

### Role.java

```java
public enum Role {
    ADMIN,
    USER
}
```

---

## 6. Conventions de nommage

### Java vs SQL

| Java (camelCase) | SQL (snake_case) |
|------------------|------------------|
| fullName | full_name |
| createdAt | created_at |
| requestType | request_type |

Spring Boot convertit automatiquement camelCase en snake_case.

### Configuration explicite

```java
// Si vous voulez un nom different
@Column(name = "customer_full_name")
private String fullName;
```

---

## 7. Types de donnees

### Correspondance Java-SQL

| Java | SQL (PostgreSQL) |
|------|------------------|
| Long | BIGINT |
| Integer | INTEGER |
| String | VARCHAR(255) |
| String (TEXT) | TEXT |
| Boolean | BOOLEAN |
| LocalDateTime | TIMESTAMP |
| LocalDate | DATE |
| BigDecimal | NUMERIC |
| byte[] | BYTEA |

### Exemple avec types

```java
@Entity
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    // BIGINT
    
    private String name;                // VARCHAR(255)
    
    @Column(columnDefinition = "TEXT")
    private String description;         // TEXT
    
    private BigDecimal price;           // NUMERIC
    
    private Integer quantity;           // INTEGER
    
    private Boolean active;             // BOOLEAN
    
    private LocalDate releaseDate;      // DATE
    
    private LocalDateTime createdAt;    // TIMESTAMP
}
```

---

## 8. Lombok et les entites

### Annotations Lombok utiles

```java
@Data           // @Getter + @Setter + @ToString + @EqualsAndHashCode
@NoArgsConstructor  // Constructeur sans arguments (requis par JPA)
@AllArgsConstructor // Constructeur avec tous les arguments
@Builder        // Pattern Builder pour creer des instances
```

### Utilisation du Builder

```java
Lead lead = Lead.builder()
    .fullName("Jean Dupont")
    .email("jean@example.com")
    .requestType(RequestType.INFO)
    .message("Je voudrais des informations")
    .build();
```

---

## 9. Bonnes pratiques

### 9.1 Toujours utiliser un wrapper pour l'ID

```java
// BON
private Long id;

// MAUVAIS
private long id;  // Ne peut pas etre null
```

### 9.2 Initialiser les valeurs par defaut

```java
private LeadStatus status = LeadStatus.NEW;
private LocalDateTime createdAt = LocalDateTime.now();
```

### 9.3 Utiliser @Enumerated(EnumType.STRING)

```java
// BON: lisible en base
@Enumerated(EnumType.STRING)
private LeadStatus status;  // Stocke "NEW"

// MAUVAIS: fragile si l'ordre change
@Enumerated(EnumType.ORDINAL)
private LeadStatus status;  // Stocke 0
```

### 9.4 Constructeur sans argument

JPA necessite un constructeur sans argument (Lombok @NoArgsConstructor).

---

## 10. Points cles a retenir

1. **@Entity** marque une classe comme entite JPA
2. **@Id** + **@GeneratedValue** pour la cle primaire auto-incrementee
3. **@Column** configure les proprietes de la colonne
4. **@Enumerated(EnumType.STRING)** pour stocker les enums en texte
5. **Lombok** reduit le code boilerplate

---

## QUIZ 2.2 - Couche Model

**1. Quelle annotation marque une classe comme entite JPA?**
   - a) @Table
   - b) @Entity
   - c) @Model
   - d) @Persistent

**2. Quelle annotation definit la cle primaire?**
   - a) @PrimaryKey
   - b) @Key
   - c) @Id
   - d) @Primary

**3. Quelle strategie de generation utilise l'auto-increment?**
   - a) AUTO
   - b) SEQUENCE
   - c) IDENTITY
   - d) TABLE

**4. Comment stocker un enum de maniere lisible en base?**
   - a) @Enumerated(EnumType.ORDINAL)
   - b) @Enumerated(EnumType.STRING)
   - c) @Enum
   - d) Sans annotation

**5. VRAI ou FAUX: Une entite doit avoir un constructeur sans argument.**

**6. Quel type Java correspond a BIGINT en SQL?**
   - a) int
   - b) Integer
   - c) Long
   - d) BigInteger

**7. Comment specifier une colonne NOT NULL?**
   - a) @NotNull
   - b) @Column(nullable = false)
   - c) @Required
   - d) @Mandatory

**8. Completez: Spring Boot convertit automatiquement _______ en snake_case.**

**9. Quelle annotation Lombok genere getters, setters et toString?**
   - a) @Getter
   - b) @Setter
   - c) @Data
   - d) @AllArgsConstructor

**10. Pourquoi utiliser Long au lieu de long pour l'ID?**
   - a) Plus performant
   - b) Peut etre null (avant sauvegarde)
   - c) Requis par JPA
   - d) b et c

---

### REPONSES QUIZ 2.2

1. b) @Entity
2. c) @Id
3. c) IDENTITY
4. b) @Enumerated(EnumType.STRING)
5. VRAI
6. c) Long
7. b) @Column(nullable = false)
8. camelCase
9. c) @Data
10. d) b et c

