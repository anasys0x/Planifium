# Endpoints

Cette section présente une vue d’ensemble des endpoints exposés par l’API REST.
La description reste volontairement synthétique ; les détails d’implémentation et
les exemples d’appels sont présentés dans les autres sections du rapport.

---

## Cours

### `GET /courses`
Récupère la liste des cours.
> Endpoint générique, à utiliser avec précaution selon le volume de données.

---

### `GET /courses/search`
Recherche avancée de cours.

**Critères possibles (query parameters)** :
- sigle partiel
- mots-clés dans le titre ou la description
- filtres supplémentaires selon l’implémentation

---

### `GET /courses/{id}`
Récupère les informations détaillées d’un cours.

**Path parameter** :
- `id` : sigle du cours (ex. `IFT2255`)

---

### `GET /courses/{id}/schedule`
Récupère l’horaire d’un cours.

---

### `POST /courses/{id}/eligibility`
Vérifie l’éligibilité d’un étudiant à un cours.

**Données fournies** :
- liste des cours complétés
- cycle de l’étudiant

---

### `GET /courses/{id}/results`
Récupère les résultats académiques agrégés d’un cours.

---

### `GET /courses/compare`
Compare deux cours.

**Critères de comparaison** :
- résultats académiques agrégés
- avis étudiants
- informations issues du catalogue

---

## Programmes

### `GET /programs/search`
Recherche de programmes.

---

### `GET /programs/courses`
Récupère les cours associés à un programme donné.

---

## Trimestres

### `GET /semesters/{code}/courses`
Récupère les cours offerts pour un trimestre donné.

**Path parameter** :
- `code` : code du trimestre (ex. `A25`, `H25`, `E25`)

---

## Avis étudiants

### `GET /reviews/{courseId}`
Récupère les avis étudiants associés à un cours.

**Path parameter** :
- `courseId` : sigle du cours

---

### `POST /reviews`
Soumet un nouvel avis étudiant.

**Données fournies** :<br>
- identifiant du cours<br>
- notes et indicateurs (charge, difficulté, etc.)<br>
- commentaire optionnel

---

