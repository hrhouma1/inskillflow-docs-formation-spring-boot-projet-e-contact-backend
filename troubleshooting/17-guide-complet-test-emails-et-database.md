# 17 - Guide Complet : Tester Vrais Emails + Accéder à la Base de Données

## 🎯 Objectif

Ce guide vous montre comment :
1. Envoyer de vrais emails via Gmail
2. Accéder à la base de données PostgreSQL
3. Voir toutes les données avec des requêtes SQL

---

# PARTIE A : TESTER VRAIS EMAILS

## 📍 Étape A1 : Vérifier que Gmail est configuré

```bash
docker exec contact-api env | grep MAIL
```

**Résultat attendu pour Gmail :**
```
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USER=votre-email@gmail.com
MAIL_PASSWORD=votre-mot-de-passe-app
MAIL_AUTH=true
```

> Si vous voyez `MAIL_HOST=mailhog`, suivez d'abord le guide 16 pour configurer Gmail.

---

## 📍 Étape A2 : Créer un lead via curl

```bash
curl -X POST "http://localhost:8080/api/contact" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Jean Dupont",
    "company": "MonEntreprise",
    "email": "votre-vrai-email@gmail.com",
    "phone": "514-555-1234",
    "requestType": "INFO",
    "message": "Test envoi email réel depuis Codespaces"
  }'
```

**Réponse attendue :**
```json
{
  "id": 1,
  "fullName": "Jean Dupont",
  "company": "MonEntreprise",
  "email": "votre-vrai-email@gmail.com",
  "phone": "514-555-1234",
  "requestType": "INFO",
  "message": "Test envoi email réel depuis Codespaces",
  "status": "NEW",
  "createdAt": "2026-01-20T12:00:00",
  "updatedAt": "2026-01-20T12:00:00"
}
```

---

## 📍 Étape A3 : Vérifier les logs d'envoi

```bash
docker logs contact-api --tail 20 | grep -i email
```

**Résultat attendu (succès) :**
```
INFO  - Email de notification envoyé à l'admin pour le lead: votre-vrai-email@gmail.com
INFO  - Email de confirmation envoyé à: votre-vrai-email@gmail.com
```

---

## 📍 Étape A4 : Vérifier votre boîte Gmail

Ouvrez https://mail.google.com et vérifiez que vous avez reçu 2 emails !

---

# PARTIE B : ACCÉDER À LA BASE DE DONNÉES

## 📍 Étape B1 : Se connecter à PostgreSQL

### Option 1 : Via docker exec (recommandé)

```bash
docker exec -it contact-db psql -U postgres -d contactdb
```

Vous êtes maintenant dans le client PostgreSQL :
```
contactdb=#
```

### Option 2 : Via bash puis psql

```bash
docker exec -it contact-db bash
psql -U postgres -d contactdb
```

---

## 📍 Étape B2 : Commandes PostgreSQL de base

| Commande | Description |
|----------|-------------|
| `\dt` | Lister toutes les tables |
| `\d nom_table` | Voir la structure d'une table |
| `\q` | Quitter psql |
| `\?` | Aide sur les commandes |

---

# PARTIE C : VOIR TOUTES LES DONNÉES

## 📍 Étape C1 : Lister les tables

```sql
\dt
```

**Résultat attendu :**
```
          List of relations
 Schema |  Name  | Type  |  Owner
--------+--------+-------+----------
 public | leads  | table | postgres
 public | users  | table | postgres
(2 rows)
```

---

## 📍 Étape C2 : Voir la structure de la table LEADS

```sql
\d leads
```

**Résultat :**
```
                                        Table "public.leads"
    Column     |            Type             | Collation | Nullable |              Default
---------------+-----------------------------+-----------+----------+-----------------------------------
 id            | bigint                      |           | not null | nextval('leads_id_seq'::regclass)
 full_name     | character varying(255)      |           | not null |
 company       | character varying(255)      |           |          |
 email         | character varying(255)      |           | not null |
 phone         | character varying(255)      |           |          |
 request_type  | character varying(255)      |           | not null |
 message       | text                        |           | not null |
 status        | character varying(255)      |           | not null |
 created_at    | timestamp(6) with time zone |           |          |
 updated_at    | timestamp(6) with time zone |           |          |
```

---

## 📍 Étape C3 : Voir TOUS les leads

```sql
SELECT * FROM leads;
```

**Résultat exemple :**
```
 id | full_name    | company       | email                | phone        | request_type | message                  | status | created_at                    | updated_at
----+--------------+---------------+----------------------+--------------+--------------+--------------------------+--------+-------------------------------+-------------------------------
  1 | Jean Dupont  | MonEntreprise | votre@gmail.com      | 514-555-1234 | INFO         | Test envoi email réel... | NEW    | 2026-01-20 12:00:00.000000+00 | 2026-01-20 12:00:00.000000+00
```

---

## 📍 Étape C4 : Voir les leads formatés (plus lisible)

```sql
SELECT 
  id,
  full_name AS "Nom",
  email AS "Email",
  request_type AS "Type",
  status AS "Statut",
  created_at::date AS "Date"
FROM leads
ORDER BY created_at DESC;
```

---

## 📍 Étape C5 : Compter les leads par statut

```sql
SELECT 
  status AS "Statut",
  COUNT(*) AS "Nombre"
FROM leads
GROUP BY status;
```

**Résultat exemple :**
```
 Statut     | Nombre
------------+--------
 NEW        |      3
 CONTACTED  |      1
 CONVERTED  |      1
```

---

## 📍 Étape C6 : Compter les leads par type de demande

```sql
SELECT 
  request_type AS "Type de demande",
  COUNT(*) AS "Nombre"
FROM leads
GROUP BY request_type;
```

---

## 📍 Étape C7 : Voir la structure de la table USERS

```sql
\d users
```

---

## 📍 Étape C8 : Voir les utilisateurs (admins)

```sql
SELECT 
  id,
  first_name AS "Prénom",
  last_name AS "Nom",
  email AS "Email",
  role AS "Rôle",
  created_at::date AS "Créé le"
FROM users;
```

**Résultat :**
```
 id | Prénom | Nom   | Email             | Rôle       | Créé le
----+--------+-------+-------------------+------------+------------
  1 | Admin  | User  | admin@example.com | ROLE_ADMIN | 2026-01-20
```

---

# PARTIE D : REQUÊTES UTILES

## 📊 Statistiques complètes

```sql
-- Nombre total de leads
SELECT COUNT(*) AS "Total leads" FROM leads;

-- Leads aujourd'hui
SELECT COUNT(*) AS "Leads aujourd'hui" 
FROM leads 
WHERE created_at::date = CURRENT_DATE;

-- Leads cette semaine
SELECT COUNT(*) AS "Leads cette semaine" 
FROM leads 
WHERE created_at >= NOW() - INTERVAL '7 days';

-- Leads ce mois
SELECT COUNT(*) AS "Leads ce mois" 
FROM leads 
WHERE created_at >= DATE_TRUNC('month', CURRENT_DATE);
```

---

## 📋 Leads par statut détaillé

```sql
SELECT 
  status AS "Statut",
  COUNT(*) AS "Nombre",
  ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) AS "Pourcentage %"
FROM leads
GROUP BY status
ORDER BY COUNT(*) DESC;
```

---

## 🔍 Rechercher un lead par email

```sql
SELECT * FROM leads WHERE email LIKE '%gmail%';
```

---

## 🔍 Rechercher un lead par nom

```sql
SELECT * FROM leads WHERE full_name ILIKE '%dupont%';
```

> `ILIKE` = recherche insensible à la casse

---

## 📅 Leads des 24 dernières heures

```sql
SELECT 
  id,
  full_name,
  email,
  request_type,
  status,
  created_at
FROM leads
WHERE created_at >= NOW() - INTERVAL '24 hours'
ORDER BY created_at DESC;
```

---

## 🗑️ Supprimer un lead (test)

```sql
-- Supprimer un lead spécifique par ID
DELETE FROM leads WHERE id = 1;

-- Supprimer tous les leads de test
DELETE FROM leads WHERE email LIKE '%test%';
```

---

# PARTIE E : COMMANDES RAPIDES (COPIER-COLLER)

## 📋 Script complet pour tout voir

Copiez et collez ce script dans psql :

```sql
-- ============================================
-- RAPPORT COMPLET DE LA BASE DE DONNÉES
-- ============================================

-- 1. Liste des tables
\echo '=== TABLES ==='
\dt

-- 2. Tous les leads
\echo '\n=== TOUS LES LEADS ==='
SELECT 
  id,
  full_name AS "Nom",
  email AS "Email",
  request_type AS "Type",
  status AS "Statut",
  created_at::timestamp(0) AS "Date création"
FROM leads
ORDER BY id;

-- 3. Statistiques par statut
\echo '\n=== STATISTIQUES PAR STATUT ==='
SELECT 
  status AS "Statut",
  COUNT(*) AS "Nombre"
FROM leads
GROUP BY status
ORDER BY COUNT(*) DESC;

-- 4. Statistiques par type
\echo '\n=== STATISTIQUES PAR TYPE ==='
SELECT 
  request_type AS "Type",
  COUNT(*) AS "Nombre"
FROM leads
GROUP BY request_type
ORDER BY COUNT(*) DESC;

-- 5. Utilisateurs
\echo '\n=== UTILISATEURS (ADMINS) ==='
SELECT 
  id,
  first_name || ' ' || last_name AS "Nom complet",
  email AS "Email",
  role AS "Rôle"
FROM users;

-- 6. Résumé
\echo '\n=== RÉSUMÉ ==='
SELECT 
  (SELECT COUNT(*) FROM leads) AS "Total leads",
  (SELECT COUNT(*) FROM leads WHERE status = 'NEW') AS "Nouveaux",
  (SELECT COUNT(*) FROM users) AS "Total admins";
```

---

## 📋 Commande one-liner (sans entrer dans psql)

```bash
docker exec -it contact-db psql -U postgres -d contactdb -c "SELECT * FROM leads;"
```

---

## 📋 Exporter les données en CSV

```bash
docker exec -it contact-db psql -U postgres -d contactdb -c "COPY leads TO STDOUT WITH CSV HEADER"
```

---

## 📋 Voir les 5 derniers leads

```bash
docker exec -it contact-db psql -U postgres -d contactdb -c "SELECT id, full_name, email, status, created_at::timestamp(0) FROM leads ORDER BY created_at DESC LIMIT 5;"
```

---

# PARTIE F : INTERFACE GRAPHIQUE (OPTIONNEL)

## 📍 Option 1 : pgAdmin dans Docker

Ajoutez ceci à votre `docker-compose.yml` :

```yaml
  pgadmin:
    image: dpage/pgadmin4:latest
    container_name: contact-pgadmin
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@admin.com
      PGADMIN_DEFAULT_PASSWORD: admin
    ports:
      - "5050:80"
    depends_on:
      - postgres
```

Puis :
```bash
docker compose up -d pgadmin
```

Accédez à : http://localhost:5050

---

## 📍 Option 2 : Extension VS Code

Installez l'extension **PostgreSQL** dans VS Code et connectez-vous avec :

| Paramètre | Valeur |
|-----------|--------|
| Host | localhost |
| Port | 5432 |
| Database | contactdb |
| User | postgres |
| Password | postgres |

---

# RÉCAPITULATIF

## 🚀 Commandes essentielles

| Action | Commande |
|--------|----------|
| Se connecter à PostgreSQL | `docker exec -it contact-db psql -U postgres -d contactdb` |
| Voir les tables | `\dt` |
| Voir tous les leads | `SELECT * FROM leads;` |
| Voir tous les users | `SELECT * FROM users;` |
| Quitter | `\q` |

---

## 📊 Requêtes rapides

| Action | SQL |
|--------|-----|
| Compter les leads | `SELECT COUNT(*) FROM leads;` |
| Leads par statut | `SELECT status, COUNT(*) FROM leads GROUP BY status;` |
| Dernier lead | `SELECT * FROM leads ORDER BY id DESC LIMIT 1;` |
| Rechercher par email | `SELECT * FROM leads WHERE email LIKE '%gmail%';` |

---

## ✅ Checklist du test complet

- [ ] Gmail configuré (voir guide 16)
- [ ] Application lancée avec `docker compose -f docker-compose.gmail.yml up -d`
- [ ] Lead créé via curl ou Swagger
- [ ] Logs vérifiés (emails envoyés)
- [ ] Email reçu dans Gmail
- [ ] Connexion à PostgreSQL
- [ ] Données vérifiées avec `SELECT * FROM leads;`

---

## 🎉 Résultat final

```
┌────────────────────────────────────────────────────────────┐
│                    WORKFLOW COMPLET                         │
├────────────────────────────────────────────────────────────┤
│                                                             │
│  1. Créer un lead via API                                   │
│     └──> POST /api/contact                                  │
│                                                             │
│  2. Email envoyé via Gmail                                  │
│     └──> Notification admin + Confirmation visiteur         │
│                                                             │
│  3. Données stockées dans PostgreSQL                        │
│     └──> SELECT * FROM leads;                               │
│                                                             │
│  4. Vérification                                            │
│     └──> Email reçu ✓                                       │
│     └──> Lead en base ✓                                     │
│                                                             │
└────────────────────────────────────────────────────────────┘
```

Vous maîtrisez maintenant le flux complet : **API → Email → Base de données** ! 🎊

