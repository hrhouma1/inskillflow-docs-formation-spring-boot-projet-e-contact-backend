# Module 9 — Tests & validation (version longue, très pédagogique)

## Objectif du module

Comprendre pourquoi tester la sécurité est indispensable, quels types de tests écrire (unitaires vs intégration), comment valider les cas 401/403, comment éviter les "fausses sécurités" (config qui marche en dev mais pas en prod), et comment construire une couverture réaliste sans exploser ton temps.

---

## 1. Pourquoi tester la sécurité (au-delà du "ça marche chez moi")

La sécurité, c'est l'endroit où les bugs coûtent cher.

**Un bug classique dans une feature :**

- une page affiche mal, un endpoint renvoie un mauvais champ → gênant

**Un bug classique en sécurité :**

- un endpoint sensible devient public
- un USER peut appeler une action ADMIN
- une règle est trop permissive
- → tu as une faille, pas un bug visuel

Et le plus dangereux :
Ces bugs passent souvent inaperçus parce que :

- tu testes surtout avec ton compte ADMIN
- tu n'essaies pas l'accès avec un compte "faible"
- tu n'essaies pas les chemins négatifs (refus)

**Conclusion :**
Tester la sécurité, c'est tester ce que l'utilisateur **n'a pas le droit** de faire.

---

## 2. Les 4 scénarios incontournables à tester

Quand tu as une route protégée, tu dois tester systématiquement :

| Cas | Description | Résultat attendu |
|-----|-------------|------------------|
| A | pas authentifié | refus (401) |
| B | authentifié mais mauvais rôle | refus (403) |
| C | authentifié avec bon rôle | autorisé (200/201/204) |
| D | route publique | accessible sans auth (200) |

Ces 4 tests te donnent une base solide pour chaque endpoint critique.

---

## 3. Différence tests unitaires vs tests d'intégration (vulgarisé)

### Tests unitaires

**But :** vérifier une règle ou une méthode isolée.

**Exemple :**

- "si user n'a pas permission course:delete, alors deleteCourse() doit être interdit"

Ça te donne une logique fine, rapide, mais sans simuler tout HTTP.

### Tests d'intégration

**But :** simuler des requêtes HTTP et vérifier la réponse réelle.

**Exemple :**

- "POST /courses renvoie 403 quand le user est ROLE_USER"

Ça valide que la configuration Spring Security + controllers + filtres fonctionne.

> En sécurité, les tests d'intégration sont souvent les plus précieux, parce que :
> la faille apparaît souvent au niveau configuration (URL, filtres, ordre des règles)

---

## 4. Ce que tu veux prouver avec tes tests de sécurité

### A) Que tes routes publiques le sont vraiment

Exemple : /courses en GET doit être accessible à tous.

### B) Que tes routes privées sont réellement protégées

Exemple : /enroll/** doit être bloqué sans token.

### C) Que tes règles de rôle/permission sont exactes

Exemple : USER ne peut pas DELETE /courses/{id}.

### D) Que ton API répond correctement (JSON, 401/403)

Exemple : pas de redirection vers une page HTML.

### E) Que ton JWT est correctement validé

Exemple : token expiré → 401, token invalide → 401.

---

## 5. Tester 401 vs 403 correctement (point très important)

Beaucoup de projets confondent les deux dans les tests.

**Pour avoir 401 :**

- ne pas fournir d'authentification
- ou fournir un token invalide

**Pour avoir 403 :**

- fournir une authentification valide
- mais avec un rôle/permission insuffisant

Donc tes tests doivent clairement construire ces deux situations.

---

## 6. Stratégie de tests REST : "scénarios, pas endpoints isolés"

Au lieu de tester chaque route dans le vide, pense en scénarios métier.

**Exemple scénario "Gestion de cours" :**

**Un visiteur :**

- peut GET /courses
- ne peut pas POST /courses (401)

**Un USER :**

- peut POST /enroll/{courseId} (200/201)
- ne peut pas POST /courses (403)

**Un ADMIN :**

- peut POST /courses (201)
- peut DELETE /courses/{id} (204)

Ce type de tests couvre plus de valeur métier et te protège mieux.

---

## 7. Tester les règles par URL vs par méthode

**Si tu sécurises par URL uniquement :**

- tests d'intégration suffisent souvent

**Si tu sécurises aussi par méthode (PreAuthorize) :**

- tests d'intégration + parfois tests ciblés sur la couche service

**Pourquoi ?**
Parce qu'une sécurité par méthode doit rester vraie même si demain quelqu'un ajoute un endpoint différent qui appelle le même service.

En entreprise, c'est un vrai risque :

- un endpoint mal protégé
- mais la méthode service est protégée → défense en profondeur

---

## 8. Validation des composants JWT (théorique)

Ton système JWT a plusieurs points de défaillance :

- lecture du header Authorization
- extraction du token Bearer
- validation signature
- validation expiration
- reconstruction de l'identité (SecurityContext)
- application des rôles

**Les tests doivent couvrir au moins :**

| Cas | Résultat attendu |
|-----|------------------|
| requête sans header | 401 |
| requête avec token invalide | 401 |
| requête avec token expiré | 401 |
| requête avec token valide + rôle insuffisant | 403 |
| requête avec token valide + bon rôle | 200 |

Même si tu ne testes pas toute la crypto, tu testes le comportement.

---

## 9. Tests de "régression sécurité" (les plus utiles)

Une **régression sécurité**, c'est quand :

- tu ajoutes une nouvelle route
- tu modifies une règle
- et sans le vouloir, tu ouvres une route sensible

**Exemples classiques :**

- un pattern /api/** trop permissif
- ordre des règles changé
- /admin/** devient accessible

Les tests doivent te servir de filet :
**Si quelqu'un modifie la config, les tests cassent immédiatement.**

Ça, c'est l'énorme valeur des tests security.

---

## 10. Comment choisir quoi tester sans tout tester (couverture réaliste)

Tu n'as pas besoin de tester chaque endpoint CRUD si tu as une structure cohérente.

### Priorités :

**Niveau 1 (critique)**

- endpoints destructifs : DELETE, actions admin
- endpoints de paiement / export / données sensibles
- endpoints d'admin

**Niveau 2 (important)**

- endpoints "write" : POST/PUT
- endpoints "mon compte", données privées

**Niveau 3 (moins sensible)**

- endpoints publics GET simples (à tester quand même mais moins lourd)

**Idée :**
Tu testes fort ce qui est risqué.

---

## 11. Tests sur les erreurs (format JSON)

En API, tu veux une réponse stable.

Donc tu testes aussi :

- le status code (401/403)
- le type de contenu (JSON)
- la structure minimale (error, message, path)

**Pourquoi ?**
Parce qu'un changement accidentel qui renvoie HTML peut casser ton front.

---

## 12. Tests sur CORS (théorique mais utile)

Quand tu as un front séparé, CORS doit être correct.
Un bug de CORS peut rendre ton application "inutilisable" côté navigateur.

Tu peux tester :

- les headers Access-Control-Allow-Origin
- les méthodes autorisées
- le comportement des requêtes OPTIONS (preflight)

Même si ce n'est pas "auth", c'est crucial pour la sécurité et le fonctionnement.

---

## 13. Résumé Module 9

- la sécurité se teste en scénarios : public / 401 / 403 / OK
- 401 = pas authentifié, 403 = authentifié sans droits
- les tests d'intégration sont essentiels pour valider la config réelle
- JWT doit être testé en comportements (sans header, token invalide, expiré, etc.)
- les tests protègent contre les régressions de configuration
- priorité aux endpoints sensibles et destructifs
- vérifier aussi le format JSON des erreurs et, si nécessaire, CORS

---

## Mini quiz

1. Pourquoi les tests d'intégration sont si importants pour Spring Security ?
2. Quelle est la différence de setup entre un test 401 et un test 403 ?
3. Donne 3 endpoints qui doivent absolument être testés en priorité.
