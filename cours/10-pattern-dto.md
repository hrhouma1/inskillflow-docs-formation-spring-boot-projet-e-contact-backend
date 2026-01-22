# Chapitre 2.6 - Pattern DTO (Data Transfer Object)

## Objectifs du chapitre

- Comprendre le pattern DTO
- Separer les DTOs de requete et de reponse
- Implementer les DTOs du projet

---

## 1. Qu'est-ce qu'un DTO?

### Definition

Un **DTO (Data Transfer Object)** est un objet qui transporte des donnees entre les couches de l'application, notamment entre le client et le serveur.

### Pourquoi utiliser des DTOs?

```
Sans DTO:
Client <---> Controller <---> Service <---> Repository
              [Entity]         [Entity]      [Entity]
              
              PROBLEME: On expose tout, meme le password!

Avec DTO:
Client <---> Controller <---> Service <---> Repository
              [DTO]           [DTO/Entity]   [Entity]
              
              SOLUTION: On controle ce qu'on expose
```

---

## 2. Avantages des DTOs

### 2.1 Securite

```java
// Entite (avec champs sensibles)
@Entity
public class User {
    private Long id;
    private String email;
    private String password;  // Sensible!
    private String role;
}

// DTO (sans champs sensibles)
public class UserDto {
    private Long id;
    private String email;
    private String role;
    // Pas de password!
}
```

### 2.2 Flexibilite

L'API peut evoluer independamment de la base de donnees.

```java
// L'entite a un champ "fullName"
@Entity
public class Lead {
    private String fullName;
}

// Le DTO peut le separer
public class LeadDto {
    private String firstName;  // Calcule a partir de fullName
    private String lastName;   // Calcule a partir de fullName
}
```

### 2.3 Performance

Ne transferer que les champs necessaires.

```java
// Entite complete avec beaucoup de champs
@Entity
public class Lead {
    private Long id;
    private String fullName;
    private String email;
    private String company;
    private String phone;
    private String message;
    private LeadStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // + relations, etc.
}

// DTO leger pour la liste
public class LeadSummaryDto {
    private Long id;
    private String fullName;
    private LeadStatus status;
    // Seulement les champs pour l'affichage en liste
}
```

### 2.4 Validation

Les contraintes de validation sont sur les DTOs, pas sur les entites.

```java
public class ContactFormRequest {
    @NotBlank(message = "Le nom est obligatoire")
    private String fullName;
    
    @Email(message = "Email invalide")
    private String email;
}
```

---

## 3. Types de DTOs

### 3.1 Request DTOs (entrants)

Donnees recues du client.

```
Client --> [Request DTO] --> Controller --> Service
```

### 3.2 Response DTOs (sortants)

Donnees envoyees au client.

```
Service --> Controller --> [Response DTO] --> Client
```

---

## 4. DTOs du projet

### Structure

```
dto/
|-- request/
|   |-- ContactFormRequest.java
|   |-- LoginRequest.java
|   |-- UpdateStatusRequest.java
|
|-- response/
    |-- AuthResponse.java
    |-- LeadDto.java
    |-- LeadStatsDto.java
    |-- MessageResponse.java
```

---

## 5. Request DTOs

### ContactFormRequest.java

```java
package com.example.contact.dto.request;

import com.example.contact.model.RequestType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ContactFormRequest {
    
    @NotBlank(message = "Le nom complet est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit faire entre 2 et 100 caracteres")
    private String fullName;
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;
    
    @Size(max = 100, message = "Le nom de l'entreprise ne peut pas depasser 100 caracteres")
    private String company;
    
    @Pattern(regexp = "^[+]?[0-9\\s-]{0,20}$", message = "Format de telephone invalide")
    private String phone;
    
    @NotNull(message = "Le type de demande est obligatoire")
    private RequestType requestType;
    
    @NotBlank(message = "Le message est obligatoire")
    @Size(min = 10, max = 5000, message = "Le message doit faire entre 10 et 5000 caracteres")
    private String message;
}
```

### LoginRequest.java

```java
package com.example.contact.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;
}
```

### UpdateStatusRequest.java

```java
package com.example.contact.dto.request;

import com.example.contact.model.LeadStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    
    @NotNull(message = "Le statut est obligatoire")
    private LeadStatus status;
}
```

---

## 6. Response DTOs

### LeadDto.java

```java
package com.example.contact.dto.response;

import com.example.contact.model.LeadStatus;
import com.example.contact.model.RequestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadDto {
    
    private Long id;
    private String fullName;
    private String email;
    private String company;
    private String phone;
    private RequestType requestType;
    private String message;
    private LeadStatus status;
    private LocalDateTime createdAt;
}
```

### LeadStatsDto.java

```java
package com.example.contact.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadStatsDto {
    
    private long total;
    private long newCount;
    private long contactedCount;
    private long convertedCount;
    private long lostCount;
}
```

### AuthResponse.java

```java
package com.example.contact.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
}
```

### MessageResponse.java

```java
package com.example.contact.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageResponse {
    
    private String message;
}
```

---

## 7. Annotations de validation

### Annotations courantes

| Annotation | Description | Exemple |
|------------|-------------|---------|
| @NotNull | Ne doit pas etre null | @NotNull private Status status; |
| @NotBlank | Non null, non vide, non espaces | @NotBlank private String name; |
| @NotEmpty | Non null et non vide | @NotEmpty private List<String> items; |
| @Size | Taille min/max | @Size(min=2, max=100) |
| @Min / @Max | Valeur min/max | @Min(0) @Max(100) |
| @Email | Format email valide | @Email private String email; |
| @Pattern | Expression reguliere | @Pattern(regexp="[0-9]+") |
| @Past / @Future | Date dans le passe/futur | @Past private LocalDate birthDate; |
| @Positive | Nombre positif | @Positive private BigDecimal price; |

### Messages personnalises

```java
@NotBlank(message = "Le nom est obligatoire")
@Size(min = 2, max = 100, message = "Le nom doit faire entre {min} et {max} caracteres")
private String fullName;
```

---

## 8. Conversion Entity <-> DTO

### Methode manuelle

```java
@Service
public class LeadService {
    
    // Entity -> DTO
    private LeadDto mapToDto(Lead lead) {
        return LeadDto.builder()
                .id(lead.getId())
                .fullName(lead.getFullName())
                .email(lead.getEmail())
                .company(lead.getCompany())
                .phone(lead.getPhone())
                .requestType(lead.getRequestType())
                .message(lead.getMessage())
                .status(lead.getStatus())
                .createdAt(lead.getCreatedAt())
                .build();
    }
    
    // Request DTO -> Entity
    private Lead mapToEntity(ContactFormRequest request) {
        Lead lead = new Lead();
        lead.setFullName(request.getFullName());
        lead.setEmail(request.getEmail());
        lead.setCompany(request.getCompany());
        lead.setPhone(request.getPhone());
        lead.setRequestType(request.getRequestType());
        lead.setMessage(request.getMessage());
        return lead;
    }
}
```

### Avec Stream pour les listes

```java
public List<LeadDto> getAllLeads() {
    return leadRepository.findAll()
            .stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
}

// Ou avec Page
public Page<LeadDto> getAllLeads(Pageable pageable) {
    return leadRepository.findAll(pageable)
            .map(this::mapToDto);
}
```

---

## 9. Bonnes pratiques

### 9.1 Separer Request et Response

```java
// Request: ce qu'on recoit
public class CreateLeadRequest {
    private String fullName;
    private String email;
    // Pas d'id, pas de status, pas de createdAt
}

// Response: ce qu'on renvoie
public class LeadDto {
    private Long id;           // Genere par la base
    private String fullName;
    private String email;
    private LeadStatus status; // Defini par le systeme
    private LocalDateTime createdAt;  // Genere automatiquement
}
```

### 9.2 Un DTO par cas d'usage

```java
// Pour la liste (leger)
public class LeadSummaryDto {
    private Long id;
    private String fullName;
    private LeadStatus status;
}

// Pour le detail (complet)
public class LeadDetailDto {
    private Long id;
    private String fullName;
    private String email;
    private String company;
    private String phone;
    private String message;
    private LeadStatus status;
    private LocalDateTime createdAt;
    private List<CommentDto> comments;
}
```

### 9.3 Immutabilite (optionnel mais recommande)

```java
// Avec Lombok
@Value  // Rend tous les champs final et private
public class LeadDto {
    Long id;
    String fullName;
    String email;
}
```

### 9.4 Validation cote serveur obligatoire

Meme si le frontend valide, toujours valider cote serveur!

---

## 10. Points cles a retenir

1. **DTO** = objet pour transporter des donnees entre couches
2. **Request DTO** = donnees entrantes (avec validation)
3. **Response DTO** = donnees sortantes (sans champs sensibles)
4. **Validation** avec @NotBlank, @Email, @Size, etc.
5. **Conversion** manuelle ou avec mapper (MapStruct, ModelMapper)

---

## QUIZ 2.6 - Pattern DTO

**1. Que signifie DTO?**
   - a) Data Transfer Object
   - b) Data Transport Operator
   - c) Direct Table Object
   - d) Domain Transfer Object

**2. Pourquoi utiliser des DTOs?**
   - a) Securite (ne pas exposer les champs sensibles)
   - b) Flexibilite (API independante de la base)
   - c) Validation des donnees
   - d) Toutes les reponses ci-dessus

**3. Quelle annotation valide qu'une chaine n'est pas vide?**
   - a) @NotNull
   - b) @NotEmpty
   - c) @NotBlank
   - d) @Required

**4. Ou placer les annotations de validation?**
   - a) Sur l'entite
   - b) Sur le Request DTO
   - c) Sur le Response DTO
   - d) Sur le Controller

**5. VRAI ou FAUX: Un Response DTO peut contenir le mot de passe de l'utilisateur.**

**6. Quelle annotation valide un format email?**
   - a) @Mail
   - b) @Email
   - c) @ValidEmail
   - d) @EmailFormat

**7. Quelle annotation Lombok genere un builder?**
   - a) @Data
   - b) @Builder
   - c) @AllArgsConstructor
   - d) @Value

**8. Completez: La validation des DTOs est activee avec l'annotation _______ dans le controller.**

**9. Quelle est la difference entre @NotNull et @NotBlank?**
   - a) Aucune
   - b) @NotBlank verifie aussi que la chaine n'est pas vide ou que des espaces
   - c) @NotNull est pour les chaines, @NotBlank pour les objets
   - d) @NotBlank permet null

**10. Pourquoi separer Request et Response DTOs?**
   - a) Les champs sont differents (id, createdAt)
   - b) La validation ne s'applique qu'aux Request
   - c) Meilleure evolution de l'API
   - d) Toutes les reponses ci-dessus

---

### REPONSES QUIZ 2.6

1. a) Data Transfer Object
2. d) Toutes les reponses ci-dessus
3. c) @NotBlank
4. b) Sur le Request DTO
5. FAUX (jamais!)
6. b) @Email
7. b) @Builder
8. @Valid
9. b) @NotBlank verifie aussi que la chaine n'est pas vide ou que des espaces
10. d) Toutes les reponses ci-dessus

