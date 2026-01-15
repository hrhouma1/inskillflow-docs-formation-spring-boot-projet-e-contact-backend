# 07 - Guide Pédagogique : Tester l'API pas à pas

## 🎯 Objectif

Apprendre à utiliser l'API Contact Form en suivant un parcours structuré qui couvre tous les endpoints.

---

## 📋 Prérequis

- ✅ Swagger UI accessible : https://expert-acorn-v6g97rv5x577fp4pg-8080.app.github.dev/swagger-ui.html
- ✅ MailHog accessible : https://expert-acorn-v6g97rv5x577fp4pg-8025.app.github.dev
- ✅ Tous les conteneurs Docker en cours d'exécution

---

## 🚀 Étape 1 : Tester l'endpoint PUBLIC (sans authentification)

### 1.1 Soumettre un formulaire de contact

C'est le seul endpoint **public** - pas besoin de token !

1. Dans Swagger UI, cliquez sur **contact-controller**
2. Cliquez sur `POST /api/contact`
3. Cliquez sur **Try it out**
4. Collez ce JSON dans le body :

```json
{
  "fullName": "Marie Tremblay",
  "company": "ABC Inc.",
  "email": "marie@example.com",
  "phone": "514-555-1234",
  "requestType": "QUOTE",
  "message": "Bonjour, je voudrais un devis pour 10 personnes."
}
```

5. Cliquez sur **Execute**

### 1.2 Vérifier la réponse

**Réponse attendue (Code 200) :**
```json
{
  "message": "Merci! Votre message a été envoyé. Nous vous répondrons bientôt."
}
```

### 1.3 Vérifier les emails dans MailHog

1. Ouvrez **MailHog** : https://expert-acorn-v6g97rv5x577fp4pg-8025.app.github.dev
2. Vous devriez voir **2 emails** :
   - 📧 **Notification admin** - "Nouveau contact: Marie Tremblay"
   - 📧 **Confirmation visiteur** - "Confirmation - Nous avons bien reçu votre message"

### ✅ Checkpoint 1
- [ ] Formulaire soumis avec succès (200 OK)
- [ ] 2 emails visibles dans MailHog

---

## 🔐 Étape 2 : S'authentifier (obtenir un token JWT)

### 2.1 Se connecter en tant qu'admin

1. Dans Swagger UI, cliquez sur **auth-controller**
2. Cliquez sur `POST /api/auth/login`
3. Cliquez sur **Try it out**
4. Entrez ces identifiants :

```json
{
  "email": "admin@example.com",
  "password": "admin123"
}
```

5. Cliquez sur **Execute**

### 2.2 Copier le token

**Réponse attendue :**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6...",
  "email": "admin@example.com",
  "expiresIn": 86400000
}
```

📋 **COPIEZ le token** (la longue chaîne commençant par `eyJ...`)

### 2.3 Configurer l'autorisation dans Swagger

1. Cliquez sur le bouton **Authorize** 🔓 (en haut à droite)
2. Dans le champ, tapez : `Bearer ` suivi du token
   ```
   Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIs...
   ```
3. Cliquez sur **Authorize**
4. Cliquez sur **Close**

### ✅ Checkpoint 2
- [ ] Token JWT obtenu
- [ ] Autorisation configurée dans Swagger (cadenas fermé 🔒)

---

## 📊 Étape 3 : Consulter les leads (endpoints ADMIN)

### 3.1 Lister tous les leads

1. Cliquez sur **lead-controller**
2. Cliquez sur `GET /api/admin/leads`
3. Cliquez sur **Try it out**
4. Cliquez sur **Execute**

**Réponse attendue :**
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
      "message": "Bonjour, je voudrais un devis pour 10 personnes.",
      "status": "NEW",
      "createdAt": "2026-01-15T15:45:00",
      "updatedAt": null
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

### 3.2 Voir les statistiques

1. Cliquez sur `GET /api/admin/leads/stats`
2. **Try it out** → **Execute**

**Réponse attendue :**
```json
{
  "totalLeads": 1,
  "newLeads": 1,
  "contactedLeads": 0,
  "convertedLeads": 0,
  "lostLeads": 0,
  "conversionRate": 0.0
}
```

### 3.3 Voir un lead spécifique

1. Cliquez sur `GET /api/admin/leads/{id}`
2. **Try it out**
3. Entrez `1` dans le champ **id**
4. **Execute**

### ✅ Checkpoint 3
- [ ] Liste des leads récupérée
- [ ] Statistiques affichées
- [ ] Détail d'un lead visible

---

## ✏️ Étape 4 : Modifier un lead

### 4.1 Changer le statut d'un lead

1. Cliquez sur `PUT /api/admin/leads/{id}/status`
2. **Try it out**
3. Entrez `1` dans le champ **id**
4. Dans le body, entrez :

```json
{
  "status": "CONTACTED"
}
```

5. **Execute**

**Statuts disponibles :**
| Statut | Description |
|--------|-------------|
| `NEW` | Nouveau lead |
| `CONTACTED` | Lead contacté |
| `CONVERTED` | Converti en client |
| `LOST` | Lead perdu |

### 4.2 Vérifier le changement

1. Retournez à `GET /api/admin/leads/stats`
2. **Execute**

**Nouvelle réponse attendue :**
```json
{
  "totalLeads": 1,
  "newLeads": 0,        // ← Changé de 1 à 0
  "contactedLeads": 1,  // ← Changé de 0 à 1
  "convertedLeads": 0,
  "lostLeads": 0,
  "conversionRate": 0.0
}
```

### ✅ Checkpoint 4
- [ ] Statut du lead modifié
- [ ] Statistiques mises à jour

---

## 🗑️ Étape 5 : Supprimer un lead

### 5.1 Créer un nouveau lead pour le test

Retournez à l'étape 1 et créez un nouveau contact :

```json
{
  "fullName": "Test Suppression",
  "email": "delete@test.com",
  "requestType": "OTHER",
  "message": "Ce lead sera supprimé"
}
```

### 5.2 Vérifier qu'il existe

`GET /api/admin/leads` → Vous devriez voir 2 leads

### 5.3 Supprimer le lead

1. Cliquez sur `DELETE /api/admin/leads/{id}`
2. **Try it out**
3. Entrez `2` dans le champ **id** (ou l'id du nouveau lead)
4. **Execute**

**Réponse attendue : Code 204 No Content**

### 5.4 Vérifier la suppression

`GET /api/admin/leads` → Il ne reste plus qu'1 lead

### ✅ Checkpoint 5
- [ ] Lead supprimé avec succès
- [ ] Vérification : le lead n'apparaît plus

---

## 🔄 Étape 6 : Tester les erreurs

### 6.1 Erreur 401 - Non authentifié

1. Cliquez sur **Authorize** → **Logout**
2. Essayez `GET /api/admin/leads`

**Réponse attendue : 401 Unauthorized**

### 6.2 Erreur 404 - Lead non trouvé

1. Ré-authentifiez-vous
2. Essayez `GET /api/admin/leads/999`

**Réponse attendue : 404 Not Found**
```json
{
  "message": "Lead non trouvé avec l'id: 999"
}
```

### 6.3 Erreur 400 - Données invalides

1. `POST /api/contact` avec un email invalide :

```json
{
  "fullName": "",
  "email": "pas-un-email",
  "requestType": "INFO",
  "message": ""
}
```

**Réponse attendue : 400 Bad Request** avec les erreurs de validation

### ✅ Checkpoint 6
- [ ] Erreur 401 comprise (pas de token)
- [ ] Erreur 404 comprise (ressource inexistante)
- [ ] Erreur 400 comprise (validation échouée)

---

## 📈 Étape 7 : Scénario complet - Cycle de vie d'un lead

### Scénario : Convertir un prospect en client

```
1. [PUBLIC]  POST /api/contact         → Lead créé (status: NEW)
2. [ADMIN]   GET /api/admin/leads      → Voir le nouveau lead
3. [ADMIN]   PUT /leads/{id}/status    → Passer à CONTACTED
4. [ADMIN]   PUT /leads/{id}/status    → Passer à CONVERTED
5. [ADMIN]   GET /api/admin/leads/stats → conversionRate > 0 !
```

### Exécution

1. Créez 3 nouveaux leads via `POST /api/contact`
2. Passez-en 1 à `CONTACTED`, puis `CONVERTED`
3. Passez-en 1 à `CONTACTED`, puis `LOST`
4. Laissez-en 1 à `NEW`
5. Vérifiez les stats :

```json
{
  "totalLeads": 4,
  "newLeads": 1,
  "contactedLeads": 0,
  "convertedLeads": 1,
  "lostLeads": 1,
  "conversionRate": 25.0  // 1 converti sur 4 = 25%
}
```

### ✅ Checkpoint 7
- [ ] Cycle de vie complet testé
- [ ] Taux de conversion calculé correctement

---

## 🎓 Récapitulatif des apprentissages

### Endpoints testés

| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| POST | `/api/contact` | ❌ Non | Soumettre formulaire |
| POST | `/api/auth/login` | ❌ Non | Obtenir token JWT |
| GET | `/api/admin/leads` | ✅ Oui | Lister leads |
| GET | `/api/admin/leads/{id}` | ✅ Oui | Détail lead |
| PUT | `/api/admin/leads/{id}/status` | ✅ Oui | Modifier statut |
| DELETE | `/api/admin/leads/{id}` | ✅ Oui | Supprimer lead |
| GET | `/api/admin/leads/stats` | ✅ Oui | Statistiques |

### Concepts appris

1. **API REST** - Méthodes HTTP (GET, POST, PUT, DELETE)
2. **Authentification JWT** - Token Bearer
3. **Swagger/OpenAPI** - Documentation interactive
4. **Codes HTTP** - 200, 201, 204, 400, 401, 404
5. **Validation** - Erreurs de données
6. **CRUD** - Create, Read, Update, Delete

---

## 🏆 Exercices bonus

### Exercice 1 : Filtrer par statut
Utilisez `GET /api/admin/leads?status=NEW` pour ne voir que les nouveaux leads.

### Exercice 2 : Pagination
Utilisez `GET /api/admin/leads?page=0&size=2` pour paginer les résultats.

### Exercice 3 : Intégration frontend
Créez un formulaire HTML qui appelle `POST /api/contact` avec `fetch()`.

### Exercice 4 : Postman
Importez la collection `postman-collection.json` et rejouez tous les tests.

---

## ✅ Checklist finale

- [ ] Endpoint public testé (formulaire)
- [ ] Authentification JWT maîtrisée
- [ ] CRUD sur les leads compris
- [ ] Gestion des erreurs comprise
- [ ] Emails vérifiés dans MailHog
- [ ] Cycle de vie d'un lead simulé

🎉 **Félicitations !** Vous maîtrisez maintenant l'API Contact Form !

