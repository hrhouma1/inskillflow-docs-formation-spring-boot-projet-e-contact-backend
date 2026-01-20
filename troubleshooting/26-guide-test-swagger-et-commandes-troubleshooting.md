# 26 - Guide : Test via Swagger + Commandes Troubleshooting Docker

## Recommandation

**Utilisez Swagger UI pour tester l'API sur Codespaces.** La commande `curl` avec `${CODESPACE_NAME}` ne fonctionne pas toujours correctement.

---

# PARTIE 1 : TESTER VIA SWAGGER UI

## Étape 1 : Ouvrir Swagger UI

```bash
echo "https://${CODESPACE_NAME}-8080.app.github.dev/swagger-ui.html"
```

Cliquez sur le lien affiché ou ouvrez l'onglet **PORTS** et cliquez sur le port 8080.

---

## Étape 2 : Créer un contact

1. Cliquez sur **contact-controller**
2. Cliquez sur **POST /api/contact**
3. Cliquez sur **Try it out**
4. Collez ce JSON (avec votre vrai email) :

```json
{
  "fullName": "Haythem REHOUMA",
  "company": "ABC Inc.",
  "email": "votre-email@gmail.com",
  "phone": "514-555-1234",
  "requestType": "QUOTE",
  "message": "Bonjour, je voudrais un devis pour 30 personnes."
}
```

5. Cliquez sur **Execute**

---

## Étape 3 : Vérifier la réponse

**Code 200** = Succès

```json
{
  "id": 1,
  "fullName": "Haythem REHOUMA",
  "email": "votre-email@gmail.com",
  "status": "NEW",
  ...
}
```

---

## Étape 4 : Vérifier Gmail

1. Ouvrez https://mail.google.com
2. Vérifiez :
   - Email du visiteur (confirmation)
   - Email de l'admin (notification)
3. Vérifiez aussi le dossier **Spam**

---

# PARTIE 2 : COMMANDES DE VÉRIFICATION

## Vérifier les variables d'environnement

```bash
# Toutes les variables MAIL
docker exec contact-api env | grep MAIL

# Variables spécifiques
docker exec contact-api env | grep -E "MAIL_USER|ADMIN_EMAIL"

# Vérifier GMAIL_USER exporté
echo $GMAIL_USER
```

---

## Vérifier les logs

```bash
# 10 dernières lignes
docker logs contact-api --tail 10

# 30 dernières lignes
docker logs contact-api --tail 30

# 50 dernières lignes
docker logs contact-api --tail 50

# Suivre en temps réel
docker logs contact-api -f
```

---

## Vérifier la base de données

```bash
# Voir tous les leads
docker exec contact-db psql -U postgres -d contactdb -c "SELECT * FROM leads;"

# Voir les 5 derniers leads
docker exec contact-db psql -U postgres -d contactdb -c "SELECT id, full_name, email, created_at FROM leads ORDER BY id DESC LIMIT 5;"

# Compter les leads
docker exec contact-db psql -U postgres -d contactdb -c "SELECT COUNT(*) FROM leads;"

# Supprimer un lead par email
docker exec contact-db psql -U postgres -d contactdb -c "DELETE FROM leads WHERE email = 'marie@example.com';"

# Supprimer tous les leads
docker exec contact-db psql -U postgres -d contactdb -c "DELETE FROM leads;"
```

---

## Vérifier les containers

```bash
# Containers en cours
docker ps

# Tous les containers
docker ps -a

# État des services
docker compose -f docker-compose.gmail.yml ps
```

---

# PARTIE 3 : COMMANDES DE DÉMARRAGE/ARRÊT

## Démarrer avec Gmail

```bash
# Exporter les variables (OBLIGATOIRE sur Codespaces)
export GMAIL_USER=votre-email@gmail.com
export GMAIL_PASSWORD=votre-mot-de-passe-app

# Lancer
docker compose -f docker-compose.gmail.yml up -d

# Lancer avec rebuild
docker compose -f docker-compose.gmail.yml up --build -d
```

---

## Arrêter

```bash
# Arrêter les containers
docker compose -f docker-compose.gmail.yml down

# Arrêter et supprimer les volumes (ATTENTION: perte de données)
docker compose -f docker-compose.gmail.yml down -v
```

---

## Redémarrer

```bash
# Redémarrer tous les services
docker compose -f docker-compose.gmail.yml restart

# Redémarrer un service spécifique
docker restart contact-api
docker restart contact-db
```

---

# PARTIE 4 : WORKFLOW COMPLET (COPIER-COLLER)

## Configuration initiale (une seule fois)

```bash
# 1. Exporter les variables
export GMAIL_USER=h7924012@gmail.com
export GMAIL_PASSWORD=rbfartnmqposobqb

# 2. Lancer Docker
docker compose -f docker-compose.gmail.yml up --build -d

# 3. Attendre
sleep 15

# 4. Vérifier la configuration
docker exec contact-api env | grep -E "MAIL_USER|ADMIN_EMAIL"
```

---

## Après chaque test

```bash
# Voir les logs
docker logs contact-api --tail 10

# Voir les leads créés
docker exec contact-db psql -U postgres -d contactdb -c "SELECT id, full_name, email, status FROM leads ORDER BY id DESC LIMIT 5;"
```

---

## Si problème : réinitialiser

```bash
# Tout arrêter
docker compose -f docker-compose.gmail.yml down

# Ré-exporter les variables
export GMAIL_USER=h7924012@gmail.com
export GMAIL_PASSWORD=rbfartnmqposobqb

# Relancer
docker compose -f docker-compose.gmail.yml up -d

# Vérifier
docker exec contact-api env | grep -E "MAIL_USER|ADMIN_EMAIL"
```

---

# ANNEXE : HISTORIQUE DES COMMANDES UTILISÉES

## Session de troubleshooting complète

```bash
# Lancement initial
docker compose -f docker-compose.gmail.yml up --build -d

# Vérification variables (problème : vides)
docker exec contact-api env | grep MAIL

# Vérifier l'export
echo $GMAIL_USER

# Afficher l'URL Swagger
echo "https://${CODESPACE_NAME}-8080.app.github.dev/swagger-ui.html"

# Test curl (ne fonctionne pas toujours sur Codespaces)
curl -X POST https://${CODESPACE_NAME}-8080.app.github.dev/api/contact \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test Gmail Codespaces",
    "company": "Test",
    "email": "rhoumahaythem@gmail.com",
    "phone": "514-555-1234",
    "requestType": "INFO",
    "message": "Test envoi email depuis Codespaces"
  }'

# Vérifier les logs
docker logs contact-api --tail 50
docker logs contact-api --tail 30
docker logs contact-api --tail 10

# Vérifier la base de données
docker exec contact-db psql -U postgres -d contactdb -c "SELECT * FROM leads;"
docker exec contact-db psql -U postgres -d contactdb -c "SELECT id, full_name, email, created_at FROM leads ORDER BY id DESC LIMIT 5;"

# Test curl avec URL complète (fonctionne mieux)
curl -X POST "https://fluffy-palm-tree-97qr459xpx472xqq5-8080.app.github.dev/api/contact" \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test Gmail","email":"h7924012@gmail.com","requestType":"INFO","message":"Test"}'

# Vérifier les variables spécifiques
docker exec contact-api env | grep -E "MAIL_USER|ADMIN_EMAIL"

# Arrêter et relancer (pour recharger les variables)
docker compose -f docker-compose.gmail.yml down
docker compose -f docker-compose.gmail.yml up -d

# Supprimer un lead de test
docker exec contact-db psql -U postgres -d contactdb -c "DELETE FROM leads WHERE email = 'marie@example.com';"
```

---

# ANNEXE : COMMANDES UTILES SUPPLÉMENTAIRES

## Entrer dans les containers

```bash
# Entrer dans le container API
docker exec -it contact-api sh

# Entrer dans le container PostgreSQL
docker exec -it contact-db bash

# Entrer directement dans psql
docker exec -it contact-db psql -U postgres -d contactdb
```

---

## Nettoyage Docker

```bash
# Supprimer les containers arrêtés
docker container prune

# Supprimer les images non utilisées
docker image prune

# Supprimer les volumes non utilisés
docker volume prune

# Tout nettoyer
docker system prune -a
```

---

## Voir les ressources

```bash
# Utilisation des ressources
docker stats

# Taille des images
docker images

# Taille des volumes
docker volume ls
```

---

# RÉSUMÉ

| Action | Commande |
|--------|----------|
| Lancer Gmail | `docker compose -f docker-compose.gmail.yml up -d` |
| Arrêter | `docker compose -f docker-compose.gmail.yml down` |
| Voir logs | `docker logs contact-api --tail 20` |
| Voir leads | `docker exec contact-db psql -U postgres -d contactdb -c "SELECT * FROM leads;"` |
| Vérifier config | `docker exec contact-api env \| grep MAIL` |
| Supprimer lead | `docker exec contact-db psql -U postgres -d contactdb -c "DELETE FROM leads WHERE id = 1;"` |

---

# CHECKLIST

- [ ] Variables exportées (`export GMAIL_USER=...`)
- [ ] Docker relancé après l'export
- [ ] `docker exec contact-api env | grep MAIL` montre les bonnes valeurs
- [ ] Test via Swagger (pas curl)
- [ ] Code 200 dans Swagger
- [ ] Lead créé en base (`SELECT * FROM leads`)
- [ ] Logs sans erreur (`docker logs contact-api --tail 10`)
- [ ] Email reçu dans Gmail

