# Chapitre 8.1 - Types d'exceptions en Java

## Objectifs du chapitre

- Comprendre la hierarchie des exceptions
- Distinguer checked et unchecked exceptions
- Creer des exceptions personnalisees

---

## 1. Hierarchie des exceptions

### Arbre complet

```
Throwable
|
+-- Error (erreurs systeme, ne pas attraper)
|   |
|   +-- OutOfMemoryError
|   +-- StackOverflowError
|
+-- Exception
    |
    +-- RuntimeException (unchecked)
    |   |
    |   +-- NullPointerException
    |   +-- IllegalArgumentException
    |   +-- IllegalStateException
    |   +-- ResourceNotFoundException (custom)
    |
    +-- IOException (checked)
    +-- SQLException (checked)
```

---

## 2. Checked vs Unchecked

### Checked Exceptions

- Heritent de `Exception` (pas `RuntimeException`)
- Doivent etre declarees ou attrapees
- Utilisees pour des erreurs recuperables

```java
// DOIT etre gere
public void readFile() throws IOException {
    FileReader reader = new FileReader("file.txt");
}

// OU
public void readFile() {
    try {
        FileReader reader = new FileReader("file.txt");
    } catch (IOException e) {
        // Gestion de l'erreur
    }
}
```

### Unchecked Exceptions (RuntimeException)

- Heritent de `RuntimeException`
- N'ont pas besoin d'etre declarees
- Utilisees pour des erreurs de programmation

```java
// Pas besoin de try/catch ou throws
public void process(String data) {
    if (data == null) {
        throw new NullPointerException("Data cannot be null");
    }
}
```

---

## 3. Exceptions courantes

### RuntimeException

| Exception | Cause |
|-----------|-------|
| NullPointerException | Acces a un objet null |
| IllegalArgumentException | Argument invalide |
| IllegalStateException | Etat invalide |
| IndexOutOfBoundsException | Index hors limites |
| NumberFormatException | Format de nombre invalide |
| ClassCastException | Cast impossible |

### Checked Exceptions

| Exception | Cause |
|-----------|-------|
| IOException | Erreur I/O |
| SQLException | Erreur SQL |
| FileNotFoundException | Fichier non trouve |
| ParseException | Erreur de parsing |

---

## 4. Creer des exceptions personnalisees

### Pour les APIs REST

Heriter de `RuntimeException` (unchecked) est recommande:

```java
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s non trouve avec l'ID %d", resource, id));
    }
}
```

### Utilisation

```java
@Service
public class LeadService {
    
    public Lead findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Lead", id));
    }
}
```

---

## 5. Exceptions du projet

### ResourceNotFoundException.java

```java
package com.example.contact.exception;

public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

### Autres exceptions utiles

```java
// Doublon
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}

// Validation metier
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}

// Authentification
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
```

---

## 6. Lancer des exceptions

### throw

Lance une exception.

```java
public void validate(String email) {
    if (email == null || email.isBlank()) {
        throw new IllegalArgumentException("Email cannot be null or empty");
    }
}
```

### throws

Declare qu'une methode peut lever une exception (checked).

```java
public void sendEmail() throws MessagingException {
    // Code qui peut lever MessagingException
}
```

---

## 7. Attraper des exceptions

### try-catch

```java
try {
    Lead lead = service.findById(id);
} catch (ResourceNotFoundException e) {
    log.error("Lead non trouve: {}", e.getMessage());
    // Gestion de l'erreur
}
```

### try-catch-finally

```java
Connection conn = null;
try {
    conn = dataSource.getConnection();
    // Utilisation
} catch (SQLException e) {
    log.error("Erreur SQL", e);
} finally {
    if (conn != null) {
        conn.close();  // Toujours execute
    }
}
```

### try-with-resources

```java
try (Connection conn = dataSource.getConnection()) {
    // conn est automatiquement ferme
} catch (SQLException e) {
    log.error("Erreur SQL", e);
}
```

### Multi-catch

```java
try {
    // Code
} catch (IOException | SQLException e) {
    log.error("Erreur I/O ou SQL", e);
}
```

---

## 8. Bonnes pratiques

### 8.1 Preferer RuntimeException pour les APIs

```java
// BON pour une API REST
public class ResourceNotFoundException extends RuntimeException { }

// MOINS BON (oblige a gerer partout)
public class ResourceNotFoundException extends Exception { }
```

### 8.2 Messages clairs

```java
// MAUVAIS
throw new RuntimeException("Error");

// BON
throw new ResourceNotFoundException("Lead non trouve avec l'ID 123");
```

### 8.3 Ne pas attraper Exception generique

```java
// MAUVAIS
try {
    // Code
} catch (Exception e) {
    // Attrape TOUT, meme les bugs
}

// BON
try {
    // Code
} catch (ResourceNotFoundException e) {
    // Gestion specifique
}
```

### 8.4 Logger avant de relancer

```java
try {
    externalService.call();
} catch (ExternalServiceException e) {
    log.error("Erreur service externe: {}", e.getMessage());
    throw new ServiceUnavailableException("Service temporairement indisponible");
}
```

---

## 9. Exceptions et Spring

### Dans un controller

```java
@GetMapping("/{id}")
public ResponseEntity<LeadDto> getLead(@PathVariable Long id) {
    // Si ResourceNotFoundException est levee, 
    // elle sera geree par GlobalExceptionHandler
    return ResponseEntity.ok(service.findById(id));
}
```

### Gestion globale

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(e.getMessage()));
    }
}
```

---

## 10. Points cles a retenir

1. **RuntimeException** pour les APIs (unchecked)
2. **Messages clairs** dans les exceptions
3. **Exceptions personnalisees** pour chaque cas
4. **@RestControllerAdvice** pour gerer globalement
5. **Ne pas attraper Exception** generique

---

## QUIZ 8.1 - Types d'exceptions

**1. Quelle est la difference entre checked et unchecked?**
   - a) Aucune
   - b) Checked doit etre declaree, unchecked non
   - c) Unchecked doit etre declaree, checked non
   - d) Checked est plus grave

**2. De quoi herite RuntimeException?**
   - a) Throwable
   - b) Error
   - c) Exception
   - d) Object

**3. ResourceNotFoundException devrait heriter de quoi?**
   - a) Exception
   - b) RuntimeException
   - c) Error
   - d) Throwable

**4. Quelle exception pour un objet null?**
   - a) NullException
   - b) NullPointerException
   - c) ObjectNullException
   - d) EmptyException

**5. VRAI ou FAUX: Les unchecked exceptions doivent etre declarees avec throws.**

**6. Quel mot-cle lance une exception?**
   - a) raise
   - b) throw
   - c) throws
   - d) exception

**7. Quel mot-cle declare qu'une methode peut lever une exception?**
   - a) raise
   - b) throw
   - c) throws
   - d) exception

**8. Completez: try-with-resources ferme automatiquement les _______.**

**9. Pourquoi preferer RuntimeException pour les APIs?**
   - a) Plus performant
   - b) Pas besoin de try/catch partout
   - c) Plus securise
   - d) Standard Java

**10. Qu'est-ce qu'une checked exception?**
   - a) Exception verifiee a l'execution
   - b) Exception qui doit etre declaree ou attrapee
   - c) Exception de securite
   - d) Exception systeme

---

### REPONSES QUIZ 8.1

1. b) Checked doit etre declaree, unchecked non
2. c) Exception
3. b) RuntimeException
4. b) NullPointerException
5. FAUX
6. b) throw
7. c) throws
8. ressources (ou AutoCloseable)
9. b) Pas besoin de try/catch partout
10. b) Exception qui doit etre declaree ou attrapee

