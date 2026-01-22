# Chapitre 11.1 - Lombok et reduction du boilerplate

## Objectifs du chapitre

- Comprendre le role de Lombok
- Maitriser les annotations principales
- Eviter les pieges courants

---

## 1. Qu'est-ce que Lombok?

### Definition

**Lombok** est une bibliotheque Java qui genere automatiquement le code repetitif (boilerplate) a la compilation.

### Probleme resolu

```java
// Sans Lombok: 50+ lignes
public class Lead {
    private Long id;
    private String fullName;
    private String email;
    
    public Lead() {}
    
    public Lead(Long id, String fullName, String email) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    @Override
    public boolean equals(Object o) { /* ... */ }
    @Override
    public int hashCode() { /* ... */ }
    @Override
    public String toString() { /* ... */ }
}

// Avec Lombok: 5 lignes
@Data
public class Lead {
    private Long id;
    private String fullName;
    private String email;
}
```

---

## 2. Configuration

### Dependance Maven

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

### Plugin IDE

Installer le plugin Lombok dans votre IDE:
- IntelliJ: Settings > Plugins > "Lombok"
- VS Code: Extension "Lombok Annotations Support"

---

## 3. Annotations principales

### @Getter / @Setter

```java
@Getter @Setter
public class Lead {
    private Long id;
    private String name;
}

// Genere:
// public Long getId() { return id; }
// public void setId(Long id) { this.id = id; }
// ...
```

### @ToString

```java
@ToString
public class Lead {
    private Long id;
    private String name;
}

// Genere:
// public String toString() {
//     return "Lead(id=" + id + ", name=" + name + ")";
// }
```

### @EqualsAndHashCode

```java
@EqualsAndHashCode
public class Lead {
    private Long id;
    private String name;
}

// Genere equals() et hashCode() bases sur tous les champs
```

### @NoArgsConstructor

```java
@NoArgsConstructor
public class Lead {
    private Long id;
}

// Genere:
// public Lead() {}
```

### @AllArgsConstructor

```java
@AllArgsConstructor
public class Lead {
    private Long id;
    private String name;
}

// Genere:
// public Lead(Long id, String name) {
//     this.id = id;
//     this.name = name;
// }
```

### @RequiredArgsConstructor

```java
@RequiredArgsConstructor
public class LeadService {
    private final LeadRepository repository;  // final = dans le constructeur
    private EmailService emailService;        // pas final = pas dans le constructeur
}

// Genere:
// public LeadService(LeadRepository repository) {
//     this.repository = repository;
// }
```

---

## 4. Annotations composees

### @Data

Combine plusieurs annotations:

```java
@Data
// Equivalent a:
@Getter
@Setter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
```

### @Value

Pour les objets immutables:

```java
@Value
public class LeadId {
    Long id;
}

// Equivalent a:
@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
// + tous les champs sont final et private
// + pas de setters
```

---

## 5. @Builder

### Pattern Builder

```java
@Builder
@Data
public class Lead {
    private Long id;
    private String fullName;
    private String email;
    private LeadStatus status;
}

// Utilisation:
Lead lead = Lead.builder()
    .fullName("Jean Dupont")
    .email("jean@example.com")
    .status(LeadStatus.NEW)
    .build();
```

### Avec valeurs par defaut

```java
@Builder
@Data
public class Lead {
    private Long id;
    private String fullName;
    
    @Builder.Default
    private LeadStatus status = LeadStatus.NEW;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
```

---

## 6. @Slf4j (Logging)

### Annotation

```java
@Slf4j
@Service
public class LeadService {
    
    public void createLead(Lead lead) {
        log.info("Creation du lead: {}", lead.getEmail());
        log.debug("Details: {}", lead);
        log.error("Erreur!", exception);
    }
}

// Genere:
// private static final Logger log = LoggerFactory.getLogger(LeadService.class);
```

### Niveaux de log

| Niveau | Usage |
|--------|-------|
| trace | Details fins (rarement utilise) |
| debug | Informations de debug |
| info | Informations generales |
| warn | Avertissements |
| error | Erreurs |

---

## 7. Cas d'usage dans le projet

### Entite JPA

```java
@Entity
@Table(name = "leads")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String fullName;
    private String email;
    
    @Builder.Default
    private LeadStatus status = LeadStatus.NEW;
}
```

### Service

```java
@Service
@RequiredArgsConstructor  // Injection par constructeur
@Slf4j                    // Logger
public class LeadService {
    
    private final LeadRepository repository;
    private final EmailService emailService;
    
    public LeadDto createLead(ContactFormRequest request) {
        log.info("Nouveau lead: {}", request.getEmail());
        // ...
    }
}
```

### DTO

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadDto {
    private Long id;
    private String fullName;
    private String email;
    private LeadStatus status;
    private LocalDateTime createdAt;
}
```

---

## 8. Pieges a eviter

### 8.1 @Data sur les entites JPA

```java
// ATTENTION avec les relations
@Entity
@Data  // Peut causer des problemes avec @OneToMany
public class Lead {
    @OneToMany(mappedBy = "lead")
    private List<Comment> comments;  // toString/equals peut boucler!
}

// SOLUTION: Exclure les relations
@ToString(exclude = "comments")
@EqualsAndHashCode(exclude = "comments")
```

### 8.2 Oublier @NoArgsConstructor pour JPA

```java
// MAUVAIS: JPA a besoin d'un constructeur sans arg
@AllArgsConstructor
public class Lead { }

// BON
@NoArgsConstructor
@AllArgsConstructor
public class Lead { }
```

### 8.3 @Builder.Default oublie

```java
@Builder
public class Lead {
    private LeadStatus status = LeadStatus.NEW;  // IGNORE par Builder!
}

// Le builder cree un lead avec status = null

// SOLUTION
@Builder.Default
private LeadStatus status = LeadStatus.NEW;
```

---

## 9. Points cles a retenir

1. **@Data** = @Getter + @Setter + @ToString + @EqualsAndHashCode + @RequiredArgsConstructor
2. **@RequiredArgsConstructor** pour l'injection de dependances
3. **@Builder** pour creer des objets facilement
4. **@Slf4j** pour le logging
5. **Attention** aux relations JPA avec @Data

---

## QUIZ 11.1 - Lombok

**1. Que genere @Data?**
   - a) Seulement getters/setters
   - b) Getters, setters, toString, equals, hashCode
   - c) Seulement toString
   - d) Constructeurs

**2. Quelle annotation pour l'injection par constructeur?**
   - a) @AllArgsConstructor
   - b) @NoArgsConstructor
   - c) @RequiredArgsConstructor
   - d) @Inject

**3. Comment utiliser le pattern Builder?**
   - a) @Build
   - b) @Builder
   - c) @Pattern(Builder)
   - d) @BuilderPattern

**4. Quelle annotation pour le logging?**
   - a) @Log
   - b) @Logger
   - c) @Slf4j
   - d) @Logging

**5. VRAI ou FAUX: Lombok genere le code a l'execution.**

**6. Pourquoi @NoArgsConstructor est necessaire pour JPA?**
   - a) Performance
   - b) JPA cree les entites avec ce constructeur
   - c) Obligatoire par Java
   - d) Pour le logging

**7. Que fait @Builder.Default?**
   - a) Definit le builder par defaut
   - b) Preserve les valeurs par defaut dans le builder
   - c) Cree un builder vide
   - d) Rien

**8. Completez: @Value cree des objets _______.**

**9. Quel probleme avec @Data et @OneToMany?**
   - a) Erreur de compilation
   - b) Boucle infinie dans toString/equals
   - c) Pas de probleme
   - d) Erreur JPA

**10. Comment exclure un champ de toString?**
   - a) @ToString.Exclude
   - b) @Exclude
   - c) @ToString(exclude = "champ")
   - d) a ou c

---

### REPONSES QUIZ 11.1

1. b) Getters, setters, toString, equals, hashCode
2. c) @RequiredArgsConstructor
3. b) @Builder
4. c) @Slf4j
5. FAUX (a la compilation)
6. b) JPA cree les entites avec ce constructeur
7. b) Preserve les valeurs par defaut dans le builder
8. immutables
9. b) Boucle infinie dans toString/equals
10. d) a ou c

