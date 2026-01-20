# 25 - Problème : Variables Gmail Vides sur Codespaces

## Le problème

Après avoir lancé `docker compose -f docker-compose.gmail.yml up --build -d`, les variables Gmail sont vides :

```bash
docker exec contact-api env | grep MAIL
```

**Résultat problématique :**
```
MAIL_PASSWORD=
MAIL_PORT=587
ADMIN_EMAIL=
MAIL_HOST=smtp.gmail.com
MAIL_STARTTLS=true
MAIL_AUTH=true
MAIL_USER=
```

Les variables `MAIL_USER`, `MAIL_PASSWORD` et `ADMIN_EMAIL` sont **vides**.

---

## Pourquoi ça arrive

Le fichier `docker-compose.gmail.yml` utilise des variables d'environnement :

```yaml
MAIL_USER: ${GMAIL_USER}
MAIL_PASSWORD: ${GMAIL_PASSWORD}
ADMIN_EMAIL: ${GMAIL_USER}
```

Sur Codespaces, ces variables ne sont pas définies par défaut. Elles doivent être :
1. Exportées dans le terminal, OU
2. Configurées comme Codespaces Secrets (nécessite redémarrage)

---

## Solution 1 : Exporter les variables (Rapide)

### Étape 1 : Arrêter les containers

```bash
docker compose -f docker-compose.gmail.yml down
```

### Étape 2 : Exporter les variables

**Remplacez les valeurs par vos vraies informations :**

```bash
export GMAIL_USER=votre-email@gmail.com
export GMAIL_PASSWORD=votre-mot-de-passe-app
```

**Exemple concret :**
```bash
export GMAIL_USER=hrhouma@gmail.com
export GMAIL_PASSWORD=rbfartnmqposobqb
```

### Étape 3 : Vérifier les exports

```bash
echo $GMAIL_USER
echo $GMAIL_PASSWORD
```

**Résultat attendu :**
```
hrhouma@gmail.com
rbfartnmqposobqb
```

### Étape 4 : Relancer Docker Compose

```bash
docker compose -f docker-compose.gmail.yml up --build -d
```

### Étape 5 : Vérifier la configuration

```bash
docker exec contact-api env | grep MAIL
```

**Résultat attendu (succès) :**
```
MAIL_PASSWORD=rbfartnmqposobqb
MAIL_PORT=587
ADMIN_EMAIL=hrhouma@gmail.com
MAIL_HOST=smtp.gmail.com
MAIL_STARTTLS=true
MAIL_AUTH=true
MAIL_USER=hrhouma@gmail.com
```

---

## Solution 2 : Codespaces Secrets (Permanent)

Cette solution persiste entre les redémarrages du Codespace.

### Étape 1 : Aller sur GitHub

1. Ouvrez https://github.com
2. Cliquez sur votre **photo de profil** (coin supérieur droit)
3. Cliquez sur **Settings**

### Étape 2 : Accéder aux Codespaces Secrets

1. Dans le menu à gauche, cliquez sur **Codespaces**
2. Descendez jusqu'à **Secrets**

### Étape 3 : Créer les secrets

Cliquez sur **New secret** pour chaque secret :

**Secret 1 :**
| Champ | Valeur |
|-------|--------|
| Name | `GMAIL_USER` |
| Value | `votre-email@gmail.com` |
| Repository access | Sélectionnez votre repo |

**Secret 2 :**
| Champ | Valeur |
|-------|--------|
| Name | `GMAIL_PASSWORD` |
| Value | `rbfartnmqposobqb` |
| Repository access | Sélectionnez votre repo |

### Étape 4 : Redémarrer le Codespace

1. Allez sur https://github.com/codespaces
2. Trouvez votre Codespace
3. Cliquez sur les **...** (trois points)
4. Cliquez sur **Stop codespace**
5. Attendez 10 secondes
6. Cliquez sur le nom du Codespace pour le relancer

### Étape 5 : Vérifier

Après redémarrage, les variables seront disponibles automatiquement :

```bash
echo $GMAIL_USER
```

---

## Commandes complètes (Copier-Coller)

### Script complet pour Solution 1

```bash
# 1. Arrêter
docker compose -f docker-compose.gmail.yml down

# 2. Exporter (REMPLACEZ PAR VOS VALEURS)
export GMAIL_USER=votre-email@gmail.com
export GMAIL_PASSWORD=votre-mot-de-passe-app

# 3. Relancer
docker compose -f docker-compose.gmail.yml up --build -d

# 4. Attendre 10 secondes
sleep 10

# 5. Vérifier
docker exec contact-api env | grep MAIL
```

### Script avec exemple concret

```bash
docker compose -f docker-compose.gmail.yml down
export GMAIL_USER=hrhouma@gmail.com
export GMAIL_PASSWORD=rbfartnmqposobqb
docker compose -f docker-compose.gmail.yml up --build -d
sleep 10
docker exec contact-api env | grep MAIL
```

---

## Tester l'envoi d'email

### Via curl

```bash
curl -X POST https://${CODESPACE_NAME}-8080.app.github.dev/api/contact \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test Gmail Codespaces",
    "company": "Test",
    "email": "votre-email@gmail.com",
    "phone": "514-555-1234",
    "requestType": "INFO",
    "message": "Test envoi email depuis Codespaces"
  }'
```

### Via Swagger UI

1. Ouvrez Swagger :
```bash
echo "https://${CODESPACE_NAME}-8080.app.github.dev/swagger-ui.html"
```

2. Cliquez sur le lien affiché
3. POST /api/contact > Try it out
4. Collez le JSON avec votre vraie adresse email
5. Execute

---

## Vérifier les logs

```bash
docker logs contact-api --tail 30
```

**Succès :**
```
INFO  - Email de notification envoyé à l'admin pour le lead: votre-email@gmail.com
INFO  - Email de confirmation envoyé à: votre-email@gmail.com
```

**Erreur d'authentification :**
```
ERROR - Authentication failed
```
→ Vérifiez le mot de passe d'application (sans espaces)

---

## Vérifier Gmail

1. Ouvrez https://mail.google.com
2. Vérifiez :
   - Boîte de réception
   - Dossier Spam
3. Vous devriez avoir **2 emails** :
   - Notification admin
   - Confirmation visiteur

---

## Résumé des solutions

| Solution | Avantage | Inconvénient |
|----------|----------|--------------|
| **Export** | Rapide, immédiat | Perdu après fermeture du terminal |
| **Codespaces Secrets** | Permanent | Nécessite redémarrage |

---

## Checklist

- [ ] Variables exportées (`export GMAIL_USER=...`)
- [ ] Docker compose relancé
- [ ] `docker exec contact-api env | grep MAIL` montre les valeurs
- [ ] MAIL_USER non vide
- [ ] MAIL_PASSWORD non vide
- [ ] Test via Swagger ou curl
- [ ] Logs sans erreur
- [ ] Email reçu dans Gmail

---

## Erreurs courantes

### Variables toujours vides après export

**Cause :** Export fait dans un terminal différent

**Solution :** Faire l'export ET docker compose dans le même terminal

### Authentication failed

**Cause :** Mot de passe incorrect ou avec espaces

**Solution :** 
- Mot de passe sans espaces : `rbfartnmqposobqb` (pas `rbfa rtnm qpos obqb`)
- Recréer un mot de passe sur https://myaccount.google.com/apppasswords

### Could not connect to SMTP host

**Cause :** Problème réseau ou mauvais port

**Solution :** Vérifier `MAIL_HOST=smtp.gmail.com` et `MAIL_PORT=587`

