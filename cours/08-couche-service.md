# Chapitre 2.4 - Couche Service (Logique metier)

## Objectifs du chapitre

- Comprendre le role de la couche Service
- Implementer la logique metier
- Gerer les transactions

---

## 1. Role de la couche Service

### Responsabilites

1. **Logique metier**: Regles et calculs specifiques au domaine
2. **Coordination**: Orchestrer les appels aux repositories
3. **Transactions**: Garantir la coherence des donnees
4. **Conversion**: Transformer entites en DTOs

### Ce que le Service NE fait PAS

- Gerer les requetes HTTP (role du Controller)
- Acceder directement a la base (role du Repository)
- Connaitre le format des reponses HTTP

---

## 2. Structure d'un Service

### Anatomie

```java
@Service                              // 1. Annotation Spring
@RequiredArgsConstructor              // 2. Injection par constructeur
@Transactional                        // 3. Gestion des transactions (optionnel)
public class LeadService {

    private final LeadRepository leadRepository;  // 4. Dependances
    private final EmailService emailService;

    public LeadDto createLead(ContactFormRequest request) {  // 5. Methodes metier
        // ...
    }
}
```

---

## 3. LeadService - Implementation complete

```java
package com.example.contact.service;

import com.example.contact.dto.request.ContactFormRequest;
import com.example.contact.dto.request.UpdateStatusRequest;
import com.example.contact.dto.response.LeadDto;
import com.example.contact.dto.response.LeadStatsDto;
import com.example.contact.exception.ResourceNotFoundException;
import com.example.contact.model.Lead;
import com.example.contact.model.LeadStatus;
import com.example.contact.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepository leadRepository;
    private final EmailService emailService;

    // ==================== CREATE ====================
    
    @Transactional
    public LeadDto createLead(ContactFormRequest request) {
        // 1. Creer l'entite a partir du DTO
        Lead lead = new Lead();
        lead.setFullName(request.getFullName());
        lead.setEmail(request.getEmail());
        lead.setCompany(request.getCompany());
        lead.setPhone(request.getPhone());
        lead.setRequestType(request.getRequestType());
        lead.setMessage(request.getMessage());
        lead.setStatus(LeadStatus.NEW);
        
        // 2. Sauvegarder en base
        Lead savedLead = leadRepository.save(lead);
        
        // 3. Envoyer les emails (asynchrone)
        emailService.sendAdminNotification(savedLead);
        emailService.sendVisitorConfirmation(savedLead);
        
        // 4. Retourner le DTO
        return mapToDto(savedLead);
    }

    // ==================== READ ====================
    
    public Page<LeadDto> getAllLeads(Pageable pageable) {
        return leadRepository.findAll(pageable)
                .map(this::mapToDto);
    }
    
    public LeadDto getLeadById(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead non trouve: " + id));
        return mapToDto(lead);
    }

    // ==================== UPDATE ====================
    
    @Transactional
    public LeadDto updateStatus(Long id, UpdateStatusRequest request) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead non trouve: " + id));
        
        lead.setStatus(request.getStatus());
        Lead updated = leadRepository.save(lead);
        
        return mapToDto(updated);
    }

    // ==================== DELETE ====================
    
    @Transactional
    public void deleteLead(Long id) {
        if (!leadRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lead non trouve: " + id);
        }
        leadRepository.deleteById(id);
    }

    // ==================== STATS ====================
    
    public LeadStatsDto getStats() {
        return LeadStatsDto.builder()
                .total(leadRepository.count())
                .newCount(leadRepository.countByStatus(LeadStatus.NEW))
                .contactedCount(leadRepository.countByStatus(LeadStatus.CONTACTED))
                .convertedCount(leadRepository.countByStatus(LeadStatus.CONVERTED))
                .lostCount(leadRepository.countByStatus(LeadStatus.LOST))
                .build();
    }

    // ==================== MAPPING ====================
    
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
}
```

---

## 4. Gestion des transactions

### @Transactional

```java
@Transactional
public LeadDto createLead(ContactFormRequest request) {
    // Si une exception est levee, tout est annule (rollback)
    Lead saved = leadRepository.save(lead);
    emailService.sendNotification(saved);
    return mapToDto(saved);
}
```

### Comportement par defaut

- **Rollback**: Sur RuntimeException et Error
- **Pas de rollback**: Sur Exception verifiee (checked)

### Configuration avancee

```java
// Rollback sur toutes les exceptions
@Transactional(rollbackFor = Exception.class)

// Lecture seule (optimisation)
@Transactional(readOnly = true)

// Timeout en secondes
@Transactional(timeout = 30)
```

---

## 5. Conversion Entite <-> DTO

### Pourquoi convertir?

1. **Securite**: Ne pas exposer les champs sensibles
2. **Flexibilite**: L'API peut evoluer sans modifier l'entite
3. **Performance**: Ne transferer que les champs necessaires

### Methode manuelle

```java
private LeadDto mapToDto(Lead lead) {
    return LeadDto.builder()
            .id(lead.getId())
            .fullName(lead.getFullName())
            .email(lead.getEmail())
            // ...
            .build();
}

private Lead mapToEntity(ContactFormRequest request) {
    Lead lead = new Lead();
    lead.setFullName(request.getFullName());
    lead.setEmail(request.getEmail());
    // ...
    return lead;
}
```

### Alternative: ModelMapper ou MapStruct

```java
// Avec ModelMapper (configuration globale)
@Configuration
public class MapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}

// Utilisation
private LeadDto mapToDto(Lead lead) {
    return modelMapper.map(lead, LeadDto.class);
}
```

---

## 6. Gestion des erreurs

### Lancer des exceptions

```java
public LeadDto getLeadById(Long id) {
    return leadRepository.findById(id)
            .map(this::mapToDto)
            .orElseThrow(() -> new ResourceNotFoundException("Lead non trouve: " + id));
}
```

### Exception personnalisee

```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

L'exception sera interceptee par le GlobalExceptionHandler (voir Module 8).

---

## 7. Injection de dependances

### Injection par constructeur (recommandee)

```java
@Service
@RequiredArgsConstructor  // Lombok genere le constructeur
public class LeadService {
    
    private final LeadRepository leadRepository;
    private final EmailService emailService;
    
    // Lombok genere:
    // public LeadService(LeadRepository leadRepository, EmailService emailService) {
    //     this.leadRepository = leadRepository;
    //     this.emailService = emailService;
    // }
}
```

### Avantages

1. **Immutabilite**: Les dependances sont final
2. **Testabilite**: Facile a mocker
3. **Visibilite**: On voit toutes les dependances

### Injection par @Autowired (deconseille)

```java
@Service
public class LeadService {
    
    @Autowired
    private LeadRepository leadRepository;  // Moins testable
}
```

---

## 8. Services multiples

### Appeler un autre service

```java
@Service
@RequiredArgsConstructor
public class LeadService {
    
    private final LeadRepository leadRepository;
    private final EmailService emailService;      // Autre service
    private final AuditService auditService;      // Autre service
    
    @Transactional
    public LeadDto createLead(ContactFormRequest request) {
        Lead saved = leadRepository.save(lead);
        
        // Appeler d'autres services
        emailService.sendNotification(saved);
        auditService.logCreation("Lead", saved.getId());
        
        return mapToDto(saved);
    }
}
```

---

## 9. Bonnes pratiques

### 9.1 Un service = un domaine

```java
// BON: Service dedie aux leads
@Service
public class LeadService { }

// BON: Service dedie aux emails
@Service
public class EmailService { }

// MAUVAIS: Service fourre-tout
@Service
public class ApplicationService { }
```

### 9.2 Methodes courtes et focalisees

```java
// BON: Methodes specifiques
public LeadDto createLead(...) { }
public LeadDto updateStatus(...) { }
public void deleteLead(...) { }

// MAUVAIS: Methode qui fait tout
public void processLead(String action, ...) { }
```

### 9.3 Validation dans le service si necessaire

```java
public LeadDto createLead(ContactFormRequest request) {
    // Validation metier supplementaire
    if (leadRepository.existsByEmail(request.getEmail())) {
        throw new BusinessException("Un lead avec cet email existe deja");
    }
    // ...
}
```

### 9.4 Logs pour le debugging

```java
@Slf4j
@Service
public class LeadService {
    
    public LeadDto createLead(ContactFormRequest request) {
        log.info("Creation d'un nouveau lead: {}", request.getEmail());
        // ...
        log.debug("Lead cree avec ID: {}", saved.getId());
        return mapToDto(saved);
    }
}
```

---

## 10. Points cles a retenir

1. **@Service** marque la couche metier
2. **@Transactional** garantit la coherence des donnees
3. **Les DTOs** separent l'API des entites
4. **Injection par constructeur** avec @RequiredArgsConstructor
5. **Un service = un domaine** metier

---

## QUIZ 2.4 - Couche Service

**1. Quelle annotation marque un service Spring?**
   - a) @Component
   - b) @Service
   - c) @Business
   - d) @Logic

**2. Que fait @Transactional?**
   - a) Optimise les performances
   - b) Garantit la coherence des donnees (rollback si erreur)
   - c) Securise la methode
   - d) Met en cache les resultats

**3. Pourquoi convertir les entites en DTOs?**
   - a) Securite (ne pas exposer les champs sensibles)
   - b) Flexibilite (API independante de la base)
   - c) Performance
   - d) Toutes les reponses ci-dessus

**4. Quelle est la meilleure methode d'injection?**
   - a) @Autowired sur le champ
   - b) Setter injection
   - c) Injection par constructeur
   - d) Peu importe

**5. VRAI ou FAUX: Un service peut appeler un autre service.**

**6. Sur quel type d'exception @Transactional fait un rollback par defaut?**
   - a) Toutes les exceptions
   - b) RuntimeException seulement
   - c) Exception seulement
   - d) Aucune

**7. Quelle annotation Lombok genere le constructeur avec les champs final?**
   - a) @AllArgsConstructor
   - b) @NoArgsConstructor
   - c) @RequiredArgsConstructor
   - d) @Constructor

**8. Completez: Le service coordonne les appels aux _______ et applique la logique metier.**

**9. Que retourne orElseThrow() si l'Optional est vide?**
   - a) null
   - b) Une exception
   - c) Une valeur par defaut
   - d) Un Optional vide

**10. Ou placer la logique de calcul des statistiques?**
   - a) Controller
   - b) Service
   - c) Repository
   - d) Model

---

### REPONSES QUIZ 2.4

1. b) @Service
2. b) Garantit la coherence des donnees (rollback si erreur)
3. d) Toutes les reponses ci-dessus
4. c) Injection par constructeur
5. VRAI
6. b) RuntimeException seulement
7. c) @RequiredArgsConstructor
8. repositories
9. b) Une exception
10. b) Service

