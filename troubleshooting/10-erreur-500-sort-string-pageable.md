# 10 - Erreur 500 : No property 'string' found for type 'Lead'

## 🔴 Le problème

En testant `GET /api/admin/leads` dans Swagger, vous obtenez :

```
500 Error: response status is 500

{
  "message": "Une erreur est survenue: No property 'string' found for type 'Lead'"
}
```

---

## 🔍 Cause

Le paramètre **pageable** dans Swagger contient une valeur par défaut invalide :

```json
{
  "page": 0,
  "size": 1,
  "sort": [
    "string"    ← ❌ "string" n'est pas une propriété de Lead !
  ]
}
```

Spring Data essaie de trier par une propriété appelée `string`, mais cette propriété **n'existe pas** dans l'entité `Lead`.

---

## ✅ Solution

### Option 1 : Supprimer le tri (le plus simple)

Dans le champ **pageable**, entrez :

```json
{
  "page": 0,
  "size": 10
}
```

### Option 2 : Trier par une vraie propriété

```json
{
  "page": 0,
  "size": 10,
  "sort": [
    "createdAt,desc"
  ]
}
```

### Option 3 : Laisser le champ vide

Supprimez tout le contenu du champ **pageable** et laissez-le vide. Spring utilisera les valeurs par défaut.

---

## 📋 Propriétés valides pour le tri

Voici les propriétés de l'entité `Lead` que vous pouvez utiliser :

| Propriété | Type | Description | Exemple de tri |
|-----------|------|-------------|----------------|
| `id` | Long | Identifiant | `id,asc` |
| `fullName` | String | Nom complet | `fullName,asc` |
| `company` | String | Entreprise | `company,desc` |
| `email` | String | Email | `email,asc` |
| `phone` | String | Téléphone | `phone,asc` |
| `requestType` | Enum | Type de demande | `requestType,asc` |
| `message` | String | Message | - |
| `status` | Enum | Statut | `status,asc` |
| `createdAt` | DateTime | Date création | `createdAt,desc` |
| `updatedAt` | DateTime | Date mise à jour | `updatedAt,desc` |

---

## 🧪 Exemples de requêtes valides

### Sans pagination ni tri
```
GET /api/admin/leads
```

### Avec pagination simple
```
GET /api/admin/leads?page=0&size=10
```

### Avec tri par date décroissante
```
GET /api/admin/leads?page=0&size=10&sort=createdAt,desc
```

### Avec tri par nom croissant
```
GET /api/admin/leads?page=0&size=10&sort=fullName,asc
```

### Avec filtrage par statut
```
GET /api/admin/leads?status=NEW&page=0&size=10
```

---

## 📸 Comment faire dans Swagger UI

### Étape 1 : Ouvrir GET /api/admin/leads

Cliquez sur `GET /api/admin/leads` puis **Try it out**

### Étape 2 : Modifier le paramètre pageable

Remplacez le contenu par défaut :

**❌ NE PAS laisser ça :**
```json
{
  "page": 0,
  "size": 1,
  "sort": [
    "string"
  ]
}
```

**✅ Mettre ça à la place :**
```json
{
  "page": 0,
  "size": 10
}
```

### Étape 3 : Execute

Cliquez sur **Execute** → Vous devriez avoir **200 OK** !

---

## 📊 Réponse attendue (200 OK)

```json
{
  "content": [
    {
      "id": 1,
      "fullName": "Marie Tremblay",
      "company": "ABC Inc.",
      "email": "marie@example.com",
      "phone": "514-555-1234",
      "requestType": "QUOTE",
      "message": "Bonjour, je voudrais un devis.",
      "status": "NEW",
      "createdAt": "2026-01-15T16:00:00",
      "updatedAt": null
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": false,
      "unsorted": true,
      "empty": true
    }
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true,
  "numberOfElements": 1,
  "size": 10,
  "number": 0,
  "empty": false
}
```

---

## 🔄 Alternative : Tester avec curl

Dans le terminal Codespaces :

```bash
# Obtenir le token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"admin123"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# Lister les leads (sans tri)
curl -s "http://localhost:8080/api/admin/leads?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" | jq .

# Lister les leads (tri par date)
curl -s "http://localhost:8080/api/admin/leads?page=0&size=10&sort=createdAt,desc" \
  -H "Authorization: Bearer $TOKEN" | jq .
```

---

## 📚 Comprendre l'erreur

### Pourquoi Swagger met "string" par défaut ?

Swagger génère automatiquement un exemple basé sur le type du paramètre. Comme `sort` est un tableau de `String`, il met `["string"]` comme exemple.

C'est un **placeholder**, pas une vraie valeur à utiliser !

### Comment Spring Data interprète le tri

```
sort=createdAt,desc
       ↓          ↓
   propriété   direction (asc/desc)
```

Spring cherche une propriété `createdAt` dans l'entité `Lead`. Si vous mettez `string`, il cherche une propriété `string` qui n'existe pas → **Erreur 500**.

---

## ✅ Checklist

- [ ] J'ai remplacé `"string"` par une vraie propriété ou supprimé le sort
- [ ] Le paramètre pageable contient `{"page": 0, "size": 10}`
- [ ] La requête retourne **200 OK**
- [ ] Je vois la liste des leads dans la réponse

---

## 🔗 Références

- [Spring Data - Pagination and Sorting](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#core.web.basic.paging-and-sorting)
- [Swagger UI - Parameters](https://swagger.io/docs/specification/describing-parameters/)

