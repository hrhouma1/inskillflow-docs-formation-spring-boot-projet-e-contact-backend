# 11 - Guide Complet : Scénario de Test avec Base de Données

## 🎯 Objectif

Réaliser un scénario complet de bout en bout :
1. Créer plusieurs leads via l'API
2. Les consulter et les modifier
3. Vérifier directement dans la base de données PostgreSQL
4. Voir les emails dans MailHog

---

## 📋 Prérequis

- ✅ Application démarrée (`docker compose up -d`)
- ✅ Tous les conteneurs running (`docker ps`)
- ✅ Swagger UI accessible

---

# PARTIE 1 : CRÉER DES LEADS

## 📍 Étape 1.1 : Ouvrir Swagger UI

```
https://expert-acorn-v6g97rv5x577fp4pg-8080.app.github.dev/swagger-ui.html
```

---

## 📍 Étape 1.2 : Créer le Lead #1 (Demande d'information)

1. Cliquez sur **contact-controller**
2. Cliquez sur `POST /api/contact`
3. **Try it out**
4. Collez ce JSON :

```json
{
  "fullName": "Marie Tremblay",
  "company": "ABC Inc.",
  "email": "marie@abc.com",
  "phone": "514-555-1001",
  "requestType": "INFO",
  "message": "Bonjour, je voudrais des informations sur vos services."
}
```

5. **Execute**
6. ✅ Vérifiez : **200 OK**

---

## 📍 Étape 1.3 : Créer le Lead #2 (Demande de devis)

Même procédure avec ce JSON :

```json
{
  "fullName": "Jean Dupont",
  "company": "XYZ Corp",
  "email": "jean@xyz.com",
  "phone": "514-555-1002",
  "requestType": "QUOTE",
  "message": "Je souhaite un devis pour 50 utilisateurs."
}
```

---

## 📍 Étape 1.4 : Créer le Lead #3 (Support technique)

```json
{
  "fullName": "Sophie Martin",
  "company": "Tech Solutions",
  "email": "sophie@techsol.com",
  "phone": "514-555-1003",
  "requestType": "SUPPORT",
  "message": "J'ai un problème avec mon compte, pouvez-vous m'aider ?"
}
```

---

## 📍 Étape 1.5 : Créer le Lead #4 (Partenariat)

```json
{
  "fullName": "Pierre Bernard",
  "company": "Partner Co",
  "email": "pierre@partner.com",
  "phone": "514-555-1004",
  "requestType": "PARTNERSHIP",
  "message": "Nous souhaitons discuter d'un partenariat stratégique."
}
```

---

## 📍 Étape 1.6 : Créer le Lead #5 (Autre)

```json
{
  "fullName": "Lucie Gagnon",
  "email": "lucie@gmail.com",
  "requestType": "OTHER",
  "message": "Question générale sur votre entreprise."
}
```

> Note : Ce lead n'a pas de `company` ni de `phone` (champs optionnels)

---

## ✅ Checkpoint Partie 1

- [ ] 5 leads créés avec succès
- [ ] Chaque création a retourné 200 OK

---

# PARTIE 2 : VÉRIFIER LES EMAILS

## 📍 Étape 2.1 : Ouvrir MailHog

```
https://expert-acorn-v6g97rv5x577fp4pg-8025.app.github.dev
```

---

## 📍 Étape 2.2 : Vérifier les emails

Vous devriez voir **10 emails** (2 par lead) :

| Destinataire | Sujet | Type |
|--------------|-------|------|
| admin@example.com | Nouveau contact: Marie Tremblay | Notification |
| marie@abc.com | Confirmation - Nous avons bien reçu... | Confirmation |
| admin@example.com | Nouveau contact: Jean Dupont | Notification |
| jean@xyz.com | Confirmation - Nous avons bien reçu... | Confirmation |
| ... | ... | ... |

---

## 📍 Étape 2.3 : Cliquer sur un email

Cliquez sur un email pour voir son contenu complet :

```
Nouveau contact reçu!

Nom: Marie Tremblay
Entreprise: ABC Inc.
Email: marie@abc.com
Téléphone: 514-555-1001
Type de demande: INFO

Message:
Bonjour, je voudrais des informations sur vos services.

---
Reçu le: 2026-01-15T16:30:00
```

---

## ✅ Checkpoint Partie 2

- [ ] 10 emails visibles dans MailHog
- [ ] Contenu des emails correct

---

# PARTIE 3 : CONSULTER LES LEADS (API ADMIN)

## 📍 Étape 3.1 : S'authentifier

Dans Swagger :

1. `POST /api/auth/login`
2. **Try it out**
3. Body :
```json
{
  "email": "admin@example.com",
  "password": "admin123"
}
```
4. **Execute**
5. **Copiez le token**

---

## 📍 Étape 3.2 : Configurer l'autorisation

1. Cliquez sur **Authorize** 🔓
2. Entrez : `Bearer VOTRE_TOKEN`
3. **Authorize** → **Close**

---

## 📍 Étape 3.3 : Lister tous les leads

1. `GET /api/admin/leads`
2. **Try it out**
3. Dans **pageable**, mettez :
```json
{
  "page": 0,
  "size": 10
}
```
4. **Execute**

---

## 📍 Étape 3.4 : Vérifier la réponse

Vous devriez voir 5 leads :

```json
{
  "content": [
    {
      "id": 1,
      "fullName": "Marie Tremblay",
      "status": "NEW",
      ...
    },
    {
      "id": 2,
      "fullName": "Jean Dupont",
      "status": "NEW",
      ...
    },
    ...
  ],
  "totalElements": 5,
  "totalPages": 1
}
```

---

## 📍 Étape 3.5 : Voir les statistiques

1. `GET /api/admin/leads/stats`
2. **Try it out** → **Execute**

```json
{
  "totalLeads": 5,
  "newLeads": 5,
  "contactedLeads": 0,
  "convertedLeads": 0,
  "lostLeads": 0,
  "conversionRate": 0.0
}
```

---

## ✅ Checkpoint Partie 3

- [ ] Authentification réussie
- [ ] 5 leads visibles dans la liste
- [ ] Statistiques : 5 nouveaux leads

---

# PARTIE 4 : VÉRIFIER DANS LA BASE DE DONNÉES

## 📍 Étape 4.1 : Se connecter à PostgreSQL

Dans le terminal Codespaces :

```bash
docker exec -it contact-db psql -U postgres -d contactdb
```

Vous êtes maintenant dans le client PostgreSQL :
```
contactdb=#
```

---

## 📍 Étape 4.2 : Voir toutes les tables

```sql
\dt
```

Résultat :
```
          List of relations
 Schema |  Name  | Type  |  Owner   
--------+--------+-------+----------
 public | leads  | table | postgres
 public | users  | table | postgres
```

---

## 📍 Étape 4.3 : Compter les leads

```sql
SELECT COUNT(*) FROM leads;
```

Résultat :
```
 count 
-------
     5
```

---

## 📍 Étape 4.4 : Voir tous les leads

```sql
SELECT id, full_name, email, request_type, status, created_at 
FROM leads 
ORDER BY id;
```

Résultat :
```
 id |    full_name     |       email        | request_type |  status  |         created_at         
----+------------------+--------------------+--------------+----------+----------------------------
  1 | Marie Tremblay   | marie@abc.com      | INFO         | NEW      | 2026-01-15 16:30:00.123456
  2 | Jean Dupont      | jean@xyz.com       | QUOTE        | NEW      | 2026-01-15 16:31:00.234567
  3 | Sophie Martin    | sophie@techsol.com | SUPPORT      | NEW      | 2026-01-15 16:32:00.345678
  4 | Pierre Bernard   | pierre@partner.com | PARTNERSHIP  | NEW      | 2026-01-15 16:33:00.456789
  5 | Lucie Gagnon     | lucie@gmail.com    | OTHER        | NEW      | 2026-01-15 16:34:00.567890
```

---

## 📍 Étape 4.5 : Voir les détails complets d'un lead

```sql
SELECT * FROM leads WHERE id = 1;
```

---

## 📍 Étape 4.6 : Statistiques par statut

```sql
SELECT status, COUNT(*) as total 
FROM leads 
GROUP BY status;
```

Résultat :
```
 status | total 
--------+-------
 NEW    |     5
```

---

## 📍 Étape 4.7 : Statistiques par type de demande

```sql
SELECT request_type, COUNT(*) as total 
FROM leads 
GROUP BY request_type 
ORDER BY total DESC;
```

Résultat :
```
 request_type | total 
--------------+-------
 INFO         |     1
 QUOTE        |     1
 SUPPORT      |     1
 PARTNERSHIP  |     1
 OTHER        |     1
```

---

## 📍 Étape 4.8 : Voir l'utilisateur admin

```sql
SELECT id, email, first_name, last_name, role, created_at 
FROM users;
```

Résultat :
```
 id |        email        | first_name | last_name | role  |         created_at         
----+---------------------+------------+-----------+-------+----------------------------
  1 | admin@example.com   | Admin      | User      | ADMIN | 2026-01-15 16:00:00.000000
```

---

## 📍 Étape 4.9 : Quitter PostgreSQL

```sql
\q
```

---

## ✅ Checkpoint Partie 4

- [ ] Connexion à PostgreSQL réussie
- [ ] 5 leads visibles dans la table `leads`
- [ ] 1 utilisateur admin dans la table `users`

---

# PARTIE 5 : MODIFIER LES STATUTS DES LEADS

## 📍 Étape 5.1 : Passer le Lead #1 à "CONTACTED"

Dans Swagger :

1. `PUT /api/admin/leads/{id}/status`
2. **Try it out**
3. `id` = `1`
4. Body :
```json
{
  "status": "CONTACTED"
}
```
5. **Execute** → **200 OK**

---

## 📍 Étape 5.2 : Passer le Lead #2 à "CONVERTED"

1. `id` = `2`
2. Body :
```json
{
  "status": "CONVERTED"
}
```

---

## 📍 Étape 5.3 : Passer le Lead #3 à "CONTACTED"

1. `id` = `3`
2. Body :
```json
{
  "status": "CONTACTED"
}
```

---

## 📍 Étape 5.4 : Passer le Lead #4 à "LOST"

1. `id` = `4`
2. Body :
```json
{
  "status": "LOST"
}
```

---

## 📍 Étape 5.5 : Laisser le Lead #5 à "NEW"

(Ne rien faire)

---

## 📍 Étape 5.6 : Vérifier les nouvelles statistiques

`GET /api/admin/leads/stats`

```json
{
  "totalLeads": 5,
  "newLeads": 1,
  "contactedLeads": 2,
  "convertedLeads": 1,
  "lostLeads": 1,
  "conversionRate": 20.0
}
```

---

## 📍 Étape 5.7 : Vérifier dans la base de données

```bash
docker exec -it contact-db psql -U postgres -d contactdb
```

```sql
SELECT id, full_name, status, updated_at 
FROM leads 
ORDER BY id;
```

Résultat :
```
 id |    full_name     |   status   |         updated_at         
----+------------------+------------+----------------------------
  1 | Marie Tremblay   | CONTACTED  | 2026-01-15 16:40:00.123456
  2 | Jean Dupont      | CONVERTED  | 2026-01-15 16:41:00.234567
  3 | Sophie Martin    | CONTACTED  | 2026-01-15 16:42:00.345678
  4 | Pierre Bernard   | LOST       | 2026-01-15 16:43:00.456789
  5 | Lucie Gagnon     | NEW        | NULL
```

```sql
SELECT status, COUNT(*) as total 
FROM leads 
GROUP BY status 
ORDER BY total DESC;
```

```
   status   | total 
------------+-------
 CONTACTED  |     2
 NEW        |     1
 CONVERTED  |     1
 LOST       |     1
```

```sql
\q
```

---

## ✅ Checkpoint Partie 5

- [ ] 4 leads ont changé de statut
- [ ] Statistiques mises à jour (conversionRate = 20%)
- [ ] Base de données reflète les changements

---

# PARTIE 6 : SUPPRIMER UN LEAD

## 📍 Étape 6.1 : Supprimer le Lead #5

Dans Swagger :

1. `DELETE /api/admin/leads/{id}`
2. **Try it out**
3. `id` = `5`
4. **Execute** → **204 No Content**

---

## 📍 Étape 6.2 : Vérifier la suppression

`GET /api/admin/leads/stats`

```json
{
  "totalLeads": 4,
  "newLeads": 0,
  "contactedLeads": 2,
  "convertedLeads": 1,
  "lostLeads": 1,
  "conversionRate": 25.0
}
```

> Note : Le taux de conversion est passé de 20% à 25% (1/4 au lieu de 1/5)

---

## 📍 Étape 6.3 : Vérifier dans la base de données

```bash
docker exec -it contact-db psql -U postgres -d contactdb
```

```sql
SELECT COUNT(*) FROM leads;
```

```
 count 
-------
     4
```

```sql
SELECT id, full_name FROM leads ORDER BY id;
```

```
 id |    full_name    
----+-----------------
  1 | Marie Tremblay
  2 | Jean Dupont
  3 | Sophie Martin
  4 | Pierre Bernard
```

> Note : Le Lead #5 (Lucie Gagnon) n'existe plus !

```sql
\q
```

---

## ✅ Checkpoint Partie 6

- [ ] Lead #5 supprimé
- [ ] Plus que 4 leads dans la base
- [ ] Statistiques recalculées

---

# PARTIE 7 : REQUÊTES SQL AVANCÉES

## 📍 Étape 7.1 : Se reconnecter à PostgreSQL

```bash
docker exec -it contact-db psql -U postgres -d contactdb
```

---

## 📍 Étape 7.2 : Leads créés aujourd'hui

```sql
SELECT * FROM leads 
WHERE DATE(created_at) = CURRENT_DATE;
```

---

## 📍 Étape 7.3 : Leads par entreprise

```sql
SELECT company, COUNT(*) as total 
FROM leads 
WHERE company IS NOT NULL 
GROUP BY company;
```

---

## 📍 Étape 7.4 : Rechercher par email

```sql
SELECT * FROM leads 
WHERE email LIKE '%@abc.com';
```

---

## 📍 Étape 7.5 : Leads convertis

```sql
SELECT * FROM leads 
WHERE status = 'CONVERTED';
```

---

## 📍 Étape 7.6 : Derniers leads modifiés

```sql
SELECT id, full_name, status, updated_at 
FROM leads 
WHERE updated_at IS NOT NULL 
ORDER BY updated_at DESC;
```

---

## 📍 Étape 7.7 : Exporter en CSV (optionnel)

```sql
\copy (SELECT * FROM leads) TO '/tmp/leads_export.csv' WITH CSV HEADER;
```

---

## 📍 Étape 7.8 : Quitter

```sql
\q
```

---

# RÉCAPITULATIF FINAL

## 📊 État final du système

| Métrique | Valeur |
|----------|--------|
| Total leads | 4 |
| Leads NEW | 0 |
| Leads CONTACTED | 2 |
| Leads CONVERTED | 1 |
| Leads LOST | 1 |
| Taux de conversion | 25% |
| Emails envoyés | 10 |

---

## 🗃️ État de la base de données

### Table `leads`
```
 id |    full_name     |   status   
----+------------------+------------
  1 | Marie Tremblay   | CONTACTED
  2 | Jean Dupont      | CONVERTED
  3 | Sophie Martin    | CONTACTED
  4 | Pierre Bernard   | LOST
```

### Table `users`
```
 id |        email        | role  
----+---------------------+-------
  1 | admin@example.com   | ADMIN
```

---

## ✅ Checklist finale complète

### Partie 1 - Création
- [ ] 5 leads créés via POST /api/contact

### Partie 2 - Emails
- [ ] 10 emails dans MailHog (2 par lead)

### Partie 3 - Consultation API
- [ ] Authentification JWT réussie
- [ ] Liste des leads visible

### Partie 4 - Base de données
- [ ] Connexion PostgreSQL réussie
- [ ] Requêtes SQL exécutées

### Partie 5 - Modification
- [ ] 4 statuts modifiés
- [ ] conversionRate = 25%

### Partie 6 - Suppression
- [ ] 1 lead supprimé
- [ ] 4 leads restants

### Partie 7 - SQL avancé
- [ ] Requêtes de filtrage maîtrisées

---

## 🎉 Félicitations !

Vous avez terminé le scénario complet de test !

Vous maîtrisez maintenant :
- ✅ L'API REST (CRUD complet)
- ✅ L'authentification JWT
- ✅ La base de données PostgreSQL
- ✅ Les requêtes SQL
- ✅ Le système d'emails
- ✅ Swagger UI

