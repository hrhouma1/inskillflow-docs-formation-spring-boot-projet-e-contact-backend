# 19 - Architecture : Les différentes options Docker Compose

## 🎯 Comprendre les fichiers Docker Compose

Vous avez maintenant **3 fichiers docker-compose** pour différents usages :

---

## 📊 Tableau récapitulatif

| Fichier | Frontend | Backend | DB | Email | Usage |
|---------|----------|---------|----|----|-------|
| `docker-compose.yml` | ❌ Non | ✅ Oui | ✅ PostgreSQL | ✅ MailHog | Dev rapide |
| `docker-compose.gmail.yml` | ❌ Non | ✅ Oui | ✅ PostgreSQL | ✅ Gmail | Test emails réels |
| `docker-compose.full.yml` | ✅ Nginx | ✅ Oui | ✅ PostgreSQL | ✅ MailHog | **Production** |

---

## 🏗️ Architecture visuelle

### Option 1 : `docker-compose.yml` (Dev rapide)

```
┌─────────────────────────────────────────────────────────┐
│                    DOCKER                                │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │
│  │    API      │  │  PostgreSQL │  │   MailHog   │      │
│  │   :8080     │  │   :5432     │  │ :1025/:8025 │      │
│  └─────────────┘  └─────────────┘  └─────────────┘      │
└─────────────────────────────────────────────────────────┘
          ▲
          │ HTTP
          │
┌─────────────────┐
│    NAVIGATEUR   │  ← Frontend ouvert manuellement
│  index.html     │     (double-clic ou Live Server)
└─────────────────┘
```

**Commande :**
```bash
docker compose up --build -d
```

**Frontend :** Ouvrez `frontend/index.html` manuellement

---

### Option 2 : `docker-compose.gmail.yml` (Test emails réels)

```
┌─────────────────────────────────────────────────────────┐
│                    DOCKER                                │
│  ┌─────────────┐  ┌─────────────┐                       │
│  │    API      │  │  PostgreSQL │     ┌──────────┐     │
│  │   :8080     │  │   :5432     │     │  Gmail   │     │
│  └─────────────┘  └─────────────┘     │ (externe)│     │
│                                        └──────────┘     │
└─────────────────────────────────────────────────────────┘
          ▲                                    │
          │ HTTP                               │ SMTP
          │                                    ▼
┌─────────────────┐                    ┌──────────────┐
│    NAVIGATEUR   │                    │ Votre boîte  │
│  index.html     │                    │    Gmail     │
└─────────────────┘                    └──────────────┘
```

**Commande :**
```bash
docker compose -f docker-compose.gmail.yml up --build -d
```

**Frontend :** Ouvrez `frontend/index.html` manuellement

---

### Option 3 : `docker-compose.full.yml` (Production) ⭐ RECOMMANDÉ

```
┌─────────────────────────────────────────────────────────┐
│                    DOCKER                                │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │
│  │  Frontend   │  │    API      │  │  PostgreSQL │      │
│  │   Nginx     │──│   :8080     │  │   :5432     │      │
│  │    :80      │  └─────────────┘  └─────────────┘      │
│  └─────────────┘                                         │
│         │         ┌─────────────┐                        │
│         │         │   MailHog   │                        │
│         │         │ :1025/:8025 │                        │
│         │         └─────────────┘                        │
└─────────┼───────────────────────────────────────────────┘
          │
          ▼
┌─────────────────┐
│    NAVIGATEUR   │
│  localhost:80   │  ← Tout passe par Nginx !
└─────────────────┘
```

**Commande :**
```bash
docker compose -f docker-compose.full.yml up --build -d
```

**Frontend :** Automatiquement servi sur http://localhost (port 80)

---

## 🎯 Quelle option choisir ?

### Sur Codespaces (développement)

**Recommandation : Option 1 (`docker-compose.yml`)**

Pourquoi ?
- Plus simple à débugger
- Rechargement rapide du frontend
- Pas besoin de rebuild pour modifier le HTML

```bash
# Backend
docker compose up --build -d

# Frontend (dans un autre terminal)
cd frontend
python -m http.server 3000
```

Accès :
- Frontend : Port 3000 (forwarded)
- API : Port 8080 (forwarded)
- MailHog : Port 8025 (forwarded)

---

### Pour déploiement sur VPS (production)

**Recommandation : Option 3 (`docker-compose.full.yml`)**

Pourquoi ?
- Une seule commande déploie tout
- Nginx gère le proxy (plus sécurisé)
- Configuration professionnelle
- Un seul port à exposer (80)

```bash
docker compose -f docker-compose.full.yml up --build -d
```

Accès :
- Tout sur le port 80 !
- `http://votredomaine.com` → Frontend
- `http://votredomaine.com/api/` → API
- `http://votredomaine.com:8025` → MailHog (à bloquer en prod)

---

## 📋 Commandes essentielles

### Lancer chaque option

```bash
# Option 1 : Dev rapide (sans frontend Docker)
docker compose up --build -d

# Option 2 : Test emails Gmail (sans frontend Docker)
docker compose -f docker-compose.gmail.yml up --build -d

# Option 3 : Production complète (avec frontend Docker)
docker compose -f docker-compose.full.yml up --build -d
```

### Arrêter chaque option

```bash
# Option 1
docker compose down

# Option 2
docker compose -f docker-compose.gmail.yml down

# Option 3
docker compose -f docker-compose.full.yml down
```

### Voir les logs

```bash
# Tous les services
docker compose -f docker-compose.full.yml logs -f

# Un service spécifique
docker compose -f docker-compose.full.yml logs -f frontend
docker compose -f docker-compose.full.yml logs -f api
```

---

## 🌐 URLs selon l'option

### Option 1 & 2 (frontend séparé)

| Service | URL |
|---------|-----|
| Frontend | `http://localhost:3000` (avec Python) |
| API | `http://localhost:8080/api/contact` |
| Swagger | `http://localhost:8080/swagger-ui.html` |
| MailHog | `http://localhost:8025` |

### Option 3 (tout dans Docker)

| Service | URL |
|---------|-----|
| Frontend | `http://localhost` (port 80) |
| API | `http://localhost/api/contact` (via nginx) |
| API direct | `http://localhost:8080/api/contact` |
| Swagger | `http://localhost/swagger-ui/` (via nginx) |
| MailHog | `http://localhost:8025` |

---

## 📁 Structure du projet

```
projet/
├── frontend/
│   └── index.html              ← Formulaire HTML
├── src/                        ← Code Spring Boot
├── nginx.conf                  ← Config Nginx (pour option 3)
├── docker-compose.yml          ← Option 1 : Dev rapide
├── docker-compose.gmail.yml    ← Option 2 : Gmail
├── docker-compose.full.yml     ← Option 3 : Production
└── Dockerfile                  ← Build Spring Boot
```

---

## ⚡ Workflow recommandé

```
┌─────────────────────────────────────────────────────────────┐
│                    WORKFLOW DE DÉVELOPPEMENT                 │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  1. DÉVELOPPEMENT (Codespaces/Local)                         │
│     └── docker compose up -d                                 │
│     └── Frontend séparé (Live Server ou Python)              │
│                                                              │
│  2. TEST EMAILS RÉELS                                        │
│     └── docker compose -f docker-compose.gmail.yml up -d     │
│     └── Vérifier emails dans Gmail                           │
│                                                              │
│  3. DÉPLOIEMENT PRODUCTION                                   │
│     └── docker compose -f docker-compose.full.yml up -d      │
│     └── Tout fonctionne sur le port 80                       │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## ✅ Checklist

### Pour Codespaces (maintenant)
- [ ] Utiliser `docker compose up -d`
- [ ] Ouvrir frontend avec Python : `cd frontend && python -m http.server 3000`
- [ ] Modifier `API_URL` dans `index.html` si nécessaire

### Pour production (plus tard)
- [ ] Utiliser `docker compose -f docker-compose.full.yml up -d`
- [ ] Configurer un nom de domaine
- [ ] Ajouter HTTPS (Let's Encrypt)
- [ ] Désactiver MailHog (utiliser Gmail)

---

## 🎉 Résumé final

| Situation | Commande | Frontend |
|-----------|----------|----------|
| Dev rapide | `docker compose up -d` | Ouvrir manuellement |
| Test Gmail | `docker compose -f docker-compose.gmail.yml up -d` | Ouvrir manuellement |
| **Production** | `docker compose -f docker-compose.full.yml up -d` | **Automatique sur :80** |

C'est plus clair maintenant ? 🎊

