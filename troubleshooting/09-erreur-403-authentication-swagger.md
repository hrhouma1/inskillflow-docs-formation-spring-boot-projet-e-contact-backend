# 09 - Erreur 403 Forbidden dans Swagger - Guide d'authentification

## 🔴 Le problème

Quand vous testez les endpoints `/api/admin/*` dans Swagger, vous obtenez :

```
403 Forbidden
```

**Cause** : Ces endpoints nécessitent un **token JWT**. Sans token = accès refusé !

---

## ✅ Solution : S'authentifier en 4 étapes

### 📍 Étape 1 : Ouvrir Swagger UI

Ouvrez votre URL Swagger :
```
https://expert-acorn-v6g97rv5x577fp4pg-8080.app.github.dev/swagger-ui.html
```

Vous devriez voir l'interface Swagger avec les différents controllers.

---

### 📍 Étape 2 : Obtenir un token JWT

#### 2.1 - Cliquez sur **auth-controller** pour le déplier

![auth-controller](https://via.placeholder.com/600x100?text=Cliquez+sur+auth-controller)

#### 2.2 - Cliquez sur `POST /api/auth/login`

![POST login](https://via.placeholder.com/600x50?text=POST+/api/auth/login)

#### 2.3 - Cliquez sur le bouton **Try it out** (à droite)

![Try it out](https://via.placeholder.com/150x40?text=Try+it+out)

#### 2.4 - Dans le champ **Request body**, entrez exactement :

```json
{
  "email": "admin@example.com",
  "password": "admin123"
}
```

#### 2.5 - Cliquez sur le bouton bleu **Execute**

![Execute](https://via.placeholder.com/100x40?text=Execute)

#### 2.6 - Regardez la réponse (Response body)

Vous devriez voir quelque chose comme :

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTcwNTMxMjAwMCwiZXhwIjoxNzA1Mzk4NDAwfQ.K7gNU3sdo-OL0wNhqoVWhr3g6s1xYv72ol_pe_Unols",
  "email": "admin@example.com",
  "expiresIn": 86400000
}
```

#### 2.7 - 📋 COPIEZ LE TOKEN

Sélectionnez et copiez **uniquement** la valeur du token (sans les guillemets) :

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTcwNTMxMjAwMCwiZXhwIjoxNzA1Mzk4NDAwfQ.K7gNU3sdo-OL0wNhqoVWhr3g6s1xYv72ol_pe_Unols
```

---

### 📍 Étape 3 : Configurer l'autorisation dans Swagger

#### 3.1 - Cliquez sur le bouton **Authorize** 🔓

Ce bouton se trouve **en haut à droite** de la page Swagger :

```
┌─────────────────────────────────────────────────────────┐
│  Swagger    /v3/api-docs           [Authorize 🔓]       │
└─────────────────────────────────────────────────────────┘
```

#### 3.2 - Une fenêtre popup s'ouvre

Vous verrez un champ de texte vide.

#### 3.3 - Tapez `Bearer ` puis collez votre token

⚠️ **TRÈS IMPORTANT** : 
- Tapez d'abord le mot `Bearer` 
- Puis UN espace
- Puis collez le token

**Format correct :**
```
Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTcwNTMxMjAwMCwiZXhwIjoxNzA1Mzk4NDAwfQ.K7gNU3sdo-OL0wNhqoVWhr3g6s1xYv72ol_pe_Unols
```

**Exemple visuel dans le champ :**
```
┌──────────────────────────────────────────────────────────────┐
│ Value: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbi...    │
└──────────────────────────────────────────────────────────────┘
```

#### 3.4 - Cliquez sur **Authorize**

#### 3.5 - Cliquez sur **Close**

Maintenant, le cadenas devrait être **fermé** 🔒 (au lieu de ouvert 🔓).

---

### 📍 Étape 4 : Tester les endpoints admin

#### 4.1 - Cliquez sur **lead-controller**

#### 4.2 - Cliquez sur `GET /api/admin/leads`

#### 4.3 - Cliquez sur **Try it out**

#### 4.4 - Cliquez sur **Execute**

#### 4.5 - ✅ Vous devriez voir **200 OK** !

```json
{
  "content": [
    {
      "id": 1,
      "fullName": "Marie Tremblay",
      "email": "marie@example.com",
      ...
    }
  ],
  "totalElements": 1
}
```

---

## ❌ Erreurs courantes et solutions

### Erreur : Toujours 403 après Authorize

| Vérifiez | Solution |
|----------|----------|
| Avez-vous mis `Bearer ` ? | Le mot "Bearer" + espace est **obligatoire** |
| Y a-t-il des guillemets ? | Enlevez tous les `"` autour du token |
| Le token est-il complet ? | Copiez-le entièrement (c'est long !) |
| Y a-t-il 2 espaces ? | Il faut exactement UN espace après Bearer |

### Erreur : 401 Unauthorized

Le token a **expiré**. Refaites l'étape 2 pour en obtenir un nouveau.

### Erreur : Le login ne fonctionne pas

Vérifiez les identifiants :
- Email : `admin@example.com`
- Password : `admin123`

---

## 📋 Récapitulatif visuel

```
┌─────────────────────────────────────────────────────────────┐
│                     SWAGGER UI                               │
├─────────────────────────────────────────────────────────────┤
│                                          [Authorize 🔒]      │
│                                                              │
│  auth-controller                                        ▼    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ POST  /api/auth/login   ← 1. LOGIN ICI              │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│  lead-controller                                        ▼    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ GET   /api/admin/leads  ← 3. TESTER ICI (après auth)│    │
│  │ GET   /api/admin/leads/{id}                    🔒   │    │
│  │ PUT   /api/admin/leads/{id}/status             🔒   │    │
│  │ DELETE /api/admin/leads/{id}                   🔒   │    │
│  │ GET   /api/admin/leads/stats                   🔒   │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│  contact-controller                                     ▼    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ POST  /api/contact      ← PAS BESOIN DE TOKEN       │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘

Flux :
1. POST /api/auth/login → Obtenir le token
2. Cliquer sur Authorize → Entrer "Bearer <token>"
3. GET /api/admin/leads → 200 OK ✅
```

---

## 🎯 Checklist

- [ ] J'ai fait POST /api/auth/login
- [ ] J'ai obtenu un token dans la réponse
- [ ] J'ai cliqué sur Authorize
- [ ] J'ai tapé `Bearer ` (avec l'espace)
- [ ] J'ai collé le token (sans guillemets)
- [ ] J'ai cliqué sur Authorize puis Close
- [ ] Le cadenas est maintenant fermé 🔒
- [ ] GET /api/admin/leads retourne 200 OK

---

## 🔄 Si ça ne marche toujours pas

### Option 1 : Tester avec curl

Dans le terminal Codespaces :

```bash
# 1. Obtenir le token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"admin123"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

echo "Token: $TOKEN"

# 2. Tester avec le token
curl http://localhost:8080/api/admin/leads \
  -H "Authorization: Bearer $TOKEN"
```

### Option 2 : Vérifier les logs

```bash
docker logs contact-api --tail 20
```

---

## 📚 Comprendre les codes HTTP

| Code | Signification | Cause |
|------|---------------|-------|
| 200 | OK | ✅ Tout fonctionne |
| 401 | Unauthorized | Token absent ou expiré |
| 403 | Forbidden | Token invalide ou pas les droits |
| 404 | Not Found | Ressource inexistante |

---

## ✅ Résumé

1. **Login** → `POST /api/auth/login` → Copier le token
2. **Authorize** → `Bearer <token>` → Valider
3. **Tester** → `GET /api/admin/leads` → 200 OK !

