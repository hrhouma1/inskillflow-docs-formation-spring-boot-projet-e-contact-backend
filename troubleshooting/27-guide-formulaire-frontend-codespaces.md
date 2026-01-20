# 27 - Guide : Utiliser le Formulaire Frontend sur Codespaces

## Prérequis

- Gmail fonctionne (docker-compose.gmail.yml lancé)
- Emails reçus via Swagger

---

# ÉTAPE 1 : VÉRIFIER QUE LE BACKEND FONCTIONNE

```bash
docker ps
```

**Résultat attendu :**
```
CONTAINER ID   IMAGE          STATUS          PORTS
xxxx           contact-api    Up X minutes    0.0.0.0:8080->8080
xxxx           postgres:15    Up X minutes    0.0.0.0:5432->5432
```

Si pas lancé :
```bash
export GMAIL_USER=h7924012@gmail.com
export GMAIL_PASSWORD=rbfartnmqposobqb
docker compose -f docker-compose.gmail.yml up -d
```

---

# ÉTAPE 2 : TROUVER L'URL DE L'API

```bash
echo "https://${CODESPACE_NAME}-8080.app.github.dev"
```

**Exemple de résultat :**
```
https://fluffy-palm-tree-97qr459xpx472xqq5-8080.app.github.dev
```

**Copiez cette URL !**

---

# ÉTAPE 3 : MODIFIER LE FRONTEND

## 3.1 Ouvrir le fichier frontend

```bash
code frontend/index.html
```

Ou dans VS Code, naviguez vers `frontend/index.html`

## 3.2 Trouver la ligne API_URL (vers la ligne 220)

Cherchez :
```javascript
// Option 1: Docker Compose Full (nginx proxy) - RECOMMANDÉ
const API_URL = '/api/contact';
```

## 3.3 Modifier l'URL

Remplacez par **votre URL Codespaces** :

```javascript
// Option 1: Docker Compose Full (nginx proxy) - RECOMMANDÉ
// const API_URL = '/api/contact';

// Codespaces
const API_URL = 'https://fluffy-palm-tree-97qr459xpx472xqq5-8080.app.github.dev/api/contact';
```

**Remplacez `fluffy-palm-tree-97qr459xpx472xqq5` par VOTRE nom de Codespace !**

## 3.4 Sauvegarder

`Ctrl + S`

---

# ÉTAPE 4 : LANCER LE SERVEUR FRONTEND

## 4.1 Ouvrir un nouveau terminal

Dans VS Code : `Ctrl + Shift + ù` ou Terminal > New Terminal

## 4.2 Aller dans le dossier frontend

```bash
cd frontend
```

## 4.3 Lancer le serveur Python

```bash
python -m http.server 3000
```

**Résultat :**
```
Serving HTTP on 0.0.0.0 port 3000 (http://0.0.0.0:3000/) ...
```

**Laissez ce terminal ouvert !**

---

# ÉTAPE 5 : RENDRE LE PORT PUBLIC

## 5.1 Aller dans l'onglet PORTS

En bas de VS Code, cliquez sur **PORTS**

## 5.2 Trouver le port 3000

Vous devriez voir :
```
Port    Local Address     Visibility
3000    localhost:3000    Private
8080    localhost:8080    Public
```

## 5.3 Rendre le port 3000 public

1. Clic droit sur le port **3000**
2. Cliquez sur **Port Visibility**
3. Sélectionnez **Public**

---

# ÉTAPE 6 : OUVRIR LE FORMULAIRE

## 6.1 Obtenir l'URL du frontend

```bash
echo "https://${CODESPACE_NAME}-3000.app.github.dev"
```

**Ou** cliquez sur l'icône globe (🌐) à côté du port 3000 dans l'onglet PORTS.

## 6.2 Ouvrir dans le navigateur

L'URL ressemble à :
```
https://fluffy-palm-tree-97qr459xpx472xqq5-3000.app.github.dev
```

---

# ÉTAPE 7 : TESTER LE FORMULAIRE

## 7.1 Remplir le formulaire

| Champ | Valeur |
|-------|--------|
| Nom complet | Votre Nom |
| Entreprise | Test Company |
| Email | **votre-vrai-email@gmail.com** |
| Téléphone | 514-555-1234 |
| Type de demande | Demande d'information |
| Message | Test depuis le formulaire |

## 7.2 Cliquer sur "Envoyer le message"

## 7.3 Résultat attendu

Message vert : **"Merci ! Votre message a été envoyé avec succès."**

---

# ÉTAPE 8 : VÉRIFIER

## 8.1 Vérifier les logs

Dans le terminal Codespaces (pas celui du frontend) :

```bash
docker logs contact-api --tail 10
```

**Attendu :**
```
Email de confirmation envoyé à: votre-email@gmail.com
Email de notification envoyé à l'admin pour le lead: votre-email@gmail.com
```

## 8.2 Vérifier la base de données

```bash
docker exec contact-db psql -U postgres -d contactdb -c "SELECT id, full_name, email, status FROM leads ORDER BY id DESC LIMIT 5;"
```

## 8.3 Vérifier Gmail

Ouvrez https://mail.google.com - vous devriez avoir 2 nouveaux emails !

---

# RÉSUMÉ DES COMMANDES

```bash
# Terminal 1 : Backend (déjà lancé)
docker compose -f docker-compose.gmail.yml up -d

# Terminal 2 : Frontend
cd frontend
python -m http.server 3000

# Obtenir les URLs
echo "Backend: https://${CODESPACE_NAME}-8080.app.github.dev"
echo "Frontend: https://${CODESPACE_NAME}-3000.app.github.dev"
```

---

# ARCHITECTURE

```
┌─────────────────────────────────────────────────────────────────┐
│                        CODESPACES                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────┐         ┌──────────────────┐              │
│  │    Frontend      │         │     Backend      │              │
│  │   Python :3000   │ ──────► │  Spring Boot     │              │
│  │   index.html     │  POST   │     :8080        │              │
│  └──────────────────┘         └────────┬─────────┘              │
│                                        │                         │
│                               ┌────────┴─────────┐              │
│                               │                  │              │
│                               ▼                  ▼              │
│                        ┌──────────┐       ┌──────────┐          │
│                        │PostgreSQL│       │  Gmail   │          │
│                        │  :5432   │       │   SMTP   │          │
│                        └──────────┘       └──────────┘          │
│                                                  │               │
└──────────────────────────────────────────────────┼───────────────┘
                                                   │
                                                   ▼
                                            ┌──────────┐
                                            │  Votre   │
                                            │  Gmail   │
                                            └──────────┘
```

---

# SI ERREUR CORS

Si vous voyez une erreur CORS dans la console du navigateur :

1. Vérifiez que le port **8080** est **Public** (pas Private)
2. Dans l'onglet PORTS, clic droit sur 8080 > Port Visibility > Public

---

# CHECKLIST

- [ ] Backend lancé (`docker ps` montre contact-api)
- [ ] Variables Gmail configurées (`docker exec contact-api env | grep MAIL`)
- [ ] `frontend/index.html` modifié avec la bonne URL
- [ ] `python -m http.server 3000` lancé
- [ ] Port 3000 rendu **Public**
- [ ] Port 8080 rendu **Public**
- [ ] Formulaire ouvert dans le navigateur
- [ ] Message "envoyé avec succès" affiché
- [ ] Logs montrent "Email envoyé"
- [ ] Email reçu dans Gmail

---

# COMMANDES RAPIDES

```bash
# Modifier le frontend (remplacez l'URL)
sed -i "s|const API_URL = '/api/contact';|const API_URL = 'https://${CODESPACE_NAME}-8080.app.github.dev/api/contact';|" frontend/index.html

# Vérifier la modification
grep "API_URL" frontend/index.html

# Lancer le frontend
cd frontend && python -m http.server 3000
```

