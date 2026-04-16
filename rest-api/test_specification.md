# Spécification des Tests - Devoir 2 IFT2255

## Vue d'ensemble

Ce document présente la spécification complète des tests unitaires couvrant les cas d'utilisation implémentés dans ce jalon. Conformément à l'approche TDD (Test-Driven Development), chaque cas d'utilisation est couvert par des tests spécifiques validant à la fois les scénarios nominaux et les cas d'erreur.

---

## Cas d'utilisation implémentés dans ce jalon

| ID   | Nom                             | Statut                              |
| ---- | ------------------------------- | ----------------------------------- |
| CU04 | Rechercher un cours             | Implémenté                          |
| CU02 | Voir les détails d'un cours     | Implémenté                          |
| CU05 | Comparer des cours              | Implémenté                          |
| CU03 | Consulter les avis sur un cours | Implémenté                          |
| CU07 | Soumettre un avis étudiant      | Partiellement (backend uniquement)  |
| CU06 | Personnaliser son profil        | Partiellement (gestion utilisateur) |

---

## 1. CU04 - Rechercher un cours

### Description du cas d'utilisation

**Acteur principal**: Étudiant(e)  
**But**: Permettre à l'étudiant de rechercher des cours par sigle, nom ou mots-clés.

**Scénario principal**:

1. Étudiant entre un sigle, nom de cours, ou mot clé dans la barre de recherche
2. Système cherche dans le catalogue via l'API Planifium
3. Système affiche liste de cours contenant sigle, nom, crédits, prérequis
4. Étudiant sélectionne un cours pour voir les détails

---

### Tests pour CU04

#### Test 1.1: Recherche sans paramètres (getAllCourses)

**Classe testée**: `CourseController`  
**Méthode testée**: `getAllCourses(Context ctx)`  
**Scénario**: Récupérer tous les cours disponibles sans filtre

**Jeu d'arguments**:

```java
Context ctx (mocké)
ctx.queryParamMap() retourne: {}
```

**Retour attendu**:

```java
List<Course> courses
- Non null
- Contient au moins 2 cours
- Chaque cours a: id, name
```

**Effets de bord**:

- Appel à `mockService.getAllCourses(Map.empty())`
- Appel à `ctx.json(courses)`
- Code HTTP: 200 (par défaut)

**Cas d'utilisation**: CU04 - Rechercher un cours

---

#### Test 1.2: Recherche avec paramètre de session

**Classe testée**: `CourseController`  
**Méthode testée**: `getAllCourses(Context ctx)`  
**Scénario**: Filtrer les cours par session spécifique

**Jeu d'arguments**:

```java
Context ctx (mocké)
ctx.queryParamMap() retourne: {"session": ["A2025"]}
```

**Retour attendu**:

```java
List<Course> courses filtrés
- Service appelé avec Map contenant {"session": "A2025"}
- Response JSON avec cours filtrés
```

**Effets de bord**:

- Appel à `mockService.getAllCourses(Map.of("session", "A2025"))`
- Paramètres correctement transmis au service
- `ctx.json(courses)` appelé

**Cas d'utilisation**: CU04 - Rechercher un cours (avec filtre)

---

#### Test 1.3: Recherche par sigle valide (getCourseById - succès)

**Classe testée**: `CourseController`  
**Méthode testée**: `getCourseById(Context ctx)`  
**Scénario**: Rechercher un cours existant par son sigle

**Jeu d'arguments**:

```java
Context ctx (mocké)
ctx.pathParam("id") retourne: "IFT2255"
mockService.getCourseById("IFT2255") retourne: Optional.of(Course("IFT2255", "Génie logiciel"))
```

**Retour attendu**:

```java
Course course
- id = "IFT2255"
- name = "Génie logiciel"
- Response JSON du cours complet
```

**Effets de bord**:

- Appel à `ctx.pathParam("id")`
- Appel à `mockService.getCourseById("IFT2255")`
- Appel à `ctx.json(course)`
- Aucun appel à `ctx.status()`

**Cas d'utilisation**: CU04 - Rechercher un cours (par sigle exact)

---

#### Test 1.4: Recherche de cours inexistant (erreur 404)

**Classe testée**: `CourseController`  
**Méthode testée**: `getCourseById(Context ctx)`  
**Scénario**: Rechercher un cours qui n'existe pas dans le catalogue

**Jeu d'arguments**:

```java
Context ctx (mocké)
ctx.pathParam("id") retourne: "IFT9999"
mockService.getCourseById("IFT9999") retourne: Optional.empty()
```

**Retour attendu**:

```json
{
  "error": "Aucun cours ne correspond à l'ID: IFT9999"
}
```

**Effets de bord**:

- Appel à `mockService.getCourseById("IFT9999")`
- Appel à `ctx.status(404)`
- Appel à `ctx.json(Map.of("error", "..."))`

**Cas d'utilisation**: CU04 - Rechercher un cours (scénario alternatif 4a - cours inexistant)

---

#### Test 1.5: Recherche avec ID null (erreur 400)

**Classe testée**: `CourseController`  
**Méthode testée**: `getCourseById(Context ctx)`  
**Scénario**: Validation côté contrôleur - ID null

**Jeu d'arguments**:

```java
Context ctx (mocké)
ctx.pathParam("id") retourne: null
```

**Retour attendu**:

```json
{
  "error": "Le paramètre id n'est pas valide."
}
```

**Effets de bord**:

- Appel à `ctx.status(400)`
- Appel à `ctx.json(Map.of("error", "..."))`
- **Aucun appel au service** (validation en amont)

**Cas d'utilisation**: CU04 - Rechercher un cours (validation des entrées)

---

#### Test 1.6: Recherche avec ID vide (erreur 400)

**Classe testée**: `CourseController`  
**Méthode testée**: `getCourseById(Context ctx)`  
**Scénario**: Validation - chaîne vide

**Jeu d'arguments**:

```java
Context ctx (mocké)
ctx.pathParam("id") retourne: ""
```

**Retour attendu**:

```json
{
  "error": "Le paramètre id n'est pas valide."
}
```

**Effets de bord**:

- Appel à `ctx.status(400)`
- **Aucun appel au service**

**Cas d'utilisation**: CU04 - Rechercher un cours (validation)

---

#### Test 1.7: Recherche au niveau service (CourseService)

**Classe testée**: `CourseService`  
**Méthode testée**: `getAllCourses(Map<String, String> queryParams)`  
**Scénario**: Vérifier que le service appelle correctement l'API HTTP

**Jeu d'arguments**:

```java
CourseService service (avec HttpClientApi mocké)
queryParams = null
mockClientApi.get(URI, TypeReference) retourne: List.of(Course1, Course2)
```

**Retour attendu**:

```java
List<Course> courses
- size = 2
- Non null
```

**Effets de bord**:

- Appel à `mockClientApi.get(URI, TypeReference)`
- URI construite correctement

**Cas d'utilisation**: CU04 - Rechercher un cours (couche service)

---

#### Test 1.8: Recherche par ID au niveau service

**Classe testée**: `CourseService`  
**Méthode testée**: `getCourseById(String courseId)`  
**Scénario**: Service récupère un cours existant

**Jeu d'arguments**:

```java
CourseService service (avec HttpClientApi mocké)
courseId = "IFT2255"
mockClientApi.get(URI, Course.class) retourne: Course("IFT2255", "Génie logiciel")
```

**Retour attendu**:

```java
Optional<Course> result
- isPresent() = true
- result.get().getId() = "IFT2255"
```

**Effets de bord**:

- Appel à `mockClientApi.get(URI, Course.class)`

**Cas d'utilisation**: CU04 - Rechercher un cours (service)

---

## 2. CU02 - Voir les détails d'un cours

### Description du cas d'utilisation

**Acteur principal**: Étudiant(e)  
**But**: Afficher les informations complètes d'un cours (horaires, prérequis, description, crédits).

**Scénario principal**:

1. Étudiant clique sur un cours depuis les résultats de recherche
2. Système récupère les informations via API Planifium
3. Système affiche: sigle, titre, crédits, description, prérequis, sessions disponibles

---

### Tests pour CU02

#### Test 2.1: Récupération des détails complets d'un cours

**Classe testée**: `CourseController`  
**Méthode testée**: `getCourseById(Context ctx)`  
**Scénario**: Afficher tous les détails d'un cours existant

_Note: Ce test est identique au Test 1.3 car getCourseById sert à la fois pour la recherche et l'affichage des détails_

**Jeu d'arguments**:

```java
ctx.pathParam("id") = "IFT2255"
Service retourne: Course complet avec toutes les propriétés
```

**Retour attendu**:

```java
Course avec:
- id: "IFT2255"
- name: non vide
- credits: non vide
- description: présent
- prerequisite_courses: List (peut être vide)
- available_terms: Map<String, Boolean>
```

**Effets de bord**:

- Récupération depuis API Planifium via service
- Affichage JSON complet

**Cas d'utilisation**: CU02 - Voir les détails d'un cours

---

#### Test 2.2: Vérification de la structure du modèle Course

**Classe testée**: `Course` (modèle)  
**Méthode testée**: Getters/Setters  
**Scénario**: Validation de la structure des données

_Note: Ce test n'est pas implémenté car les getters/setters ne sont pas testables selon les règles du devoir_

**Cas d'utilisation**: CU02 - Voir les détails d'un cours

---

## 3. CU05 - Comparer des cours

### Description du cas d'utilisation

**Acteur principal**: Étudiant(e)  
**But**: Comparer plusieurs cours pour estimer la charge totale et les caractéristiques.

**Scénario principal**:

1. Étudiant sélectionne plusieurs cours (2-4)
2. Système récupère les données de chaque cours
3. Système affiche tableau comparatif avec charge, difficulté, crédits, prérequis

---

### Tests pour CU05

#### Test 3.1: Comparaison de 2 cours valides (contrôleur)

**Classe testée**: `CourseController`  
**Méthode testée**: `compareCourses(Context ctx)`  
**Scénario**: Comparer IFT2255 et IFT1015

**Jeu d'arguments**:

```java
Context ctx (mocké)
ctx.queryParams("id") retourne: ["IFT2255", "IFT1015"]
mockService.compareCourses(["IFT2255", "IFT1015"]) retourne:
{
  "courses": [Course1, Course2]
}
```

**Retour attendu**:

```json
{
  "courses": [
    {"id": "IFT2255", "name": "Génie logiciel", ...},
    {"id": "IFT1015", "name": "Programmation I", ...}
  ]
}
```

**Effets de bord**:

- Appel à `ctx.queryParams("id")`
- Appel à `mockService.compareCourses(List)`
- Appel à `ctx.json(result)`

**Cas d'utilisation**: CU05 - Comparer des cours

---

#### Test 3.2: Comparaison au niveau service

**Classe testée**: `CourseService`  
**Méthode testée**: `compareCourses(List<String> ids)`  
**Scénario**: Service récupère et agrège les cours

**Jeu d'arguments**:

```java
CourseService service (avec HttpClientApi mocké)
ids = ["IFT2255", "IFT1015"]
mockClientApi.get(...) retourne: Course1, puis Course2
```

**Retour attendu**:

```java
Map<String, Object> result
- result.containsKey("courses") = true
- result.get("courses") instanceof List
- ((List<Course>) result.get("courses")).size() = 2
```

**Effets de bord**:

- 2 appels à `mockClientApi.get(URI, Course.class)`
- Un appel par cours

**Cas d'utilisation**: CU05 - Comparer des cours

---

#### Test 3.3: Comparaison avec cours inexistant (robustesse)

**Classe testée**: `CourseService`  
**Méthode testée**: `compareCourses(List<String> ids)`  
**Scénario**: Un cours n'existe pas - le système l'ignore silencieusement

**Jeu d'arguments**:

```java
ids = ["IFT2255", "IFT9999"]
mockClientApi.get(...) pour IFT2255: retourne Course
mockClientApi.get(...) pour IFT9999: lance RuntimeException
```

**Retour attendu**:

```java
Map<String, Object> result
- result.get("courses") contient seulement IFT2255
- Pas d'exception lancée
```

**Effets de bord**:

- Exception capturée par Optional.empty()
- Cours valide ajouté, cours invalide ignoré

**Cas d'utilisation**: CU05 - Comparer des cours (scénario alternatif 3.2 - cours inexistant)

---

#### Test 3.4: Comparaison avec liste vide

**Classe testée**: `CourseService`  
**Méthode testée**: `compareCourses(List<String> ids)`  
**Scénario**: Aucun ID fourni

**Jeu d'arguments**:

```java
ids = []
```

**Retour attendu**:

```java
Map<String, Object> result
- result.get("courses") = []
- Liste vide, pas null
```

**Effets de bord**:

- Aucun appel à l'API HTTP
- Retour immédiat avec structure valide

**Cas d'utilisation**: CU05 - Comparer des cours (cas limite)

---

## 4. CU03 - Consulter les avis sur un cours

### Description du cas d'utilisation

**Acteur principal**: Étudiant(e)  
**But**: Consulter les avis d'autres étudiants pour un cours donné.

**Scénario principal**:

1. Étudiant clique sur "Voir les avis"
2. Système vérifie qu'il y a au moins 5 avis
3. Système affiche liste des avis avec difficulté, charge, session, commentaire

---

### Tests pour CU03

#### Test 4.1: Récupération des avis pour un cours avec avis

**Classe testée**: `ReviewService`  
**Méthode testée**: `getReviewsForCourse(String courseId)`  
**Scénario**: Obtenir les avis pour IFT2255

**Jeu d'arguments**:

```java
ReviewService service (avec reviews.db)
courseId = "IFT2255"
```

**Retour attendu**:

```java
List<Review> reviews
- reviews != null
- reviews.size() >= 2
- Tous les reviews ont courseId = "IFT2255"
- Chaque review contient: courseId, author, difficulty, comment
```

**Effets de bord**:

- Lecture de la base de données SQLite
- Filtrage par courseId

**Cas d'utilisation**: CU03 - Consulter les avis sur un cours

---

#### Test 4.2: Récupération des avis pour un cours sans avis

**Classe testée**: `ReviewService`  
**Méthode testée**: `getReviewsForCourse(String courseId)`  
**Scénario**: Cours sans aucun avis disponible

**Jeu d'arguments**:

```java
courseId = "IFT9999"
```

**Retour attendu**:

```java
List<Review> reviews
- reviews != null
- reviews.isEmpty() = true
```

**Effets de bord**:

- Lecture de la base de données
- Aucun résultat trouvé

**Cas d'utilisation**: CU03 - Consulter les avis sur un cours (scénario alternatif 2b - aucun avis)

---

#### Test 4.3: Ajout d'un nouvel avis

**Classe testée**: `ReviewService`  
**Méthode testée**: `addReview(Review review)`  
**Scénario**: Ajouter un avis en mémoire

**Jeu d'arguments**:

```java
ReviewService service
Review newReview = new Review("IFT2255", "testStudent", 6, "Test comment")
int initialSize = service.getReviewsForCourse("IFT2255").size()
```

**Retour attendu**:

```java
Après addReview(newReview):
- getReviewsForCourse("IFT2255").size() = initialSize + 1
- Liste contient le nouvel avis
- review.getAuthor() = "testStudent"
```

**Effets de bord**:

- Modification de la collection en mémoire
- Nouvel avis récupérable via getReviewsForCourse()

**Cas d'utilisation**: CU07 - Soumettre un avis étudiant (backend)

---

#### Test 4.4: Affichage des avis via contrôleur

**Classe testée**: `ReviewController`  
**Méthode testée**: `getReviews(Context ctx)`  
**Scénario**: Endpoint HTTP pour récupérer les avis

**Jeu d'arguments**:

```java
Context ctx (mocké)
ctx.pathParam("courseId") = "IFT2255"
mockService.getReviewsForCourse("IFT2255") retourne: List<Review>
```

**Retour attendu**:

```java
JSON array de reviews
```

**Effets de bord**:

- Appel à `mockService.getReviewsForCourse("IFT2255")`
- Appel à `ctx.json(reviews)`

**Cas d'utilisation**: CU03 - Consulter les avis sur un cours (via API REST)

---

## 5. CU06 - Personnaliser son profil (Gestion utilisateur)

### Description du cas d'utilisation

**Acteur principal**: Étudiant(e)  
**But**: Créer et gérer son profil utilisateur.

**Note**: Dans ce jalon, seule la gestion de base des utilisateurs est implémentée (CRUD). La personnalisation complète (préférences, cours complétés) sera ajoutée dans un jalon futur.

---

### Tests pour CU06

#### Test 5.1: Récupération de tous les utilisateurs

**Classe testée**: `UserService`  
**Méthode testée**: `getAllUsers()`  
**Scénario**: Obtenir la liste complète des utilisateurs

**Jeu d'arguments**:

```java
UserService service (avec données mock)
```

**Retour attendu**:

```java
List<User> users
- users.size() = 2
- Contient Alice et Bob
```

**Effets de bord**:

- Aucun (lecture seule)

**Cas d'utilisation**: CU06 - Personnaliser son profil (gestion utilisateur)

---

#### Test 5.2: Récupération d'un utilisateur existant

**Classe testée**: `UserService`  
**Méthode testée**: `getUserById(int id)`  
**Scénario**: Obtenir l'utilisateur avec ID=1

**Jeu d'arguments**:

```java
UserService service
id = 1
```

**Retour attendu**:

```java
Optional<User> user
- user.isPresent() = true
- user.get().getName() = "Alice"
- user.get().getEmail() = "alice@example.com"
```

**Effets de bord**:

- Aucun (lecture seule)

**Cas d'utilisation**: CU06 - Personnaliser son profil (récupération du profil)

---

#### Test 5.3: Récupération d'un utilisateur inexistant

**Classe testée**: `UserService`  
**Méthode testée**: `getUserById(int id)`  
**Scénario**: Chercher un utilisateur qui n'existe pas

**Jeu d'arguments**:

```java
id = 999
```

**Retour attendu**:

```java
Optional<User> user
- user.isEmpty() = true
- Pas d'exception lancée
```

**Effets de bord**:

- Aucun

**Cas d'utilisation**: CU06 - Personnaliser son profil (cas d'erreur - utilisateur non trouvé)

---

#### Test 5.4: Création d'un nouvel utilisateur (service)

**Classe testée**: `UserService`  
**Méthode testée**: `createUser(User user)`  
**Scénario**: Ajouter un nouvel utilisateur

_Note: Ce test n'est pas implémenté dans ce jalon mais devrait être ajouté_

**Jeu d'arguments**:

```java
User newUser = new User(0, "Charlie", "charlie@example.com")
int initialSize = service.getAllUsers().size()
```

**Retour attendu**:

```java
Après createUser(newUser):
- newUser.getId() > 0 (auto-incrémenté)
- getAllUsers().size() = initialSize + 1
- getUserById(newUser.getId()).isPresent() = true
```

**Effets de bord**:

- Ajout à la collection interne
- ID auto-généré

**Cas d'utilisation**: CU10 - Créer un compte

---

#### Test 5.5: Validation de l'email

**Classe testée**: `ValidationUtil`  
**Méthode testée**: `isEmail(String email)`  
**Scénario**: Valider différents formats d'email

_Note: Ce test n'est pas implémenté dans ce jalon mais la validation existe dans UserController_

**Jeu d'arguments**:

```java
Emails valides: "test@example.com", "user.name@domain.co.uk"
Emails invalides: "notanemail", "missing@", "@nodomain.com"
```

**Retour attendu**:

```java
isEmail("test@example.com") = true
isEmail("notanemail") = false
```

**Effets de bord**:

- Aucun (validation pure)

**Cas d'utilisation**: CU10 - Créer un compte (validation des données)

---

## 6. Tests d'intégration et utilitaires

### Test 6.1: Construction d'URI avec paramètres

**Classe testée**: `HttpClientApi`  
**Méthode testée**: `buildUri(String baseUrl, Map<String, String> params)`  
**Scénario**: Construire une URI avec query parameters

_Note: Ce test n'est pas implémenté mais la méthode existe et est critique_

**Jeu d'arguments**:

```java
baseUrl = "https://planifium-api.onrender.com/api/v1/courses"
params = Map.of("session", "A2025", "name", "prog")
```

**Retour attendu**:

```java
URI uri
- uri.toString() contient "?session=A2025&name=prog"
- Paramètres correctement encodés
```

**Effets de bord**:

- Aucun

**Cas d'utilisation**: CU04 - Rechercher un cours (construction de requêtes)

---

## Résumé de la couverture

### Tests implémentés dans ce jalon

| Classe           | Nombre de tests              | CU associés             |
| ---------------- | ---------------------------- | ----------------------- |
| CourseController | 6                            | CU04, CU02, CU05        |
| CourseService    | 3                            | CU04, CU05              |
| ReviewService    | 3                            | CU03, CU07              |
| ReviewController | (intégré dans ReviewService) | CU03                    |
| UserService      | 3                            | CU06                    |
| **TOTAL**        | **15 tests**                 | **5 cas d'utilisation** |

### Couverture des cas d'utilisation

| CU   | Nom                         | Couverture | Tests                          |
| ---- | --------------------------- | ---------- | ------------------------------ |
| CU04 | Rechercher un cours         | 100%       | 8 tests (controller + service) |
| CU02 | Voir les détails d'un cours | 100%       | Partagé avec CU04              |
| CU05 | Comparer des cours          | 100%       | 4 tests (controller + service) |
| CU03 | Consulter les avis          | 90%        | 3 tests (manque: seuil 5 avis) |
| CU07 | Soumettre un avis           | 50%        | Backend seulement              |
| CU06 | Profil utilisateur          | 40%        | CRUD basique                   |

---

## Tests à ajouter dans les prochains jalons

### CU03 - Vérification du seuil de 5 avis

- Test: Un cours avec < 5 avis ne doit pas afficher ses avis
- Logique métier à implémenter dans ReviewService ou Controller

### CU07 - Bot Discord complet

- Test: Validation des données d'avis (courseId non null)
- Test: Validation de difficulty (1-10)
- Test: Détection d'avis en double

### CU06 - Personnalisation complète du profil

- Test: Création d'utilisateur avec validation email
- Test: Mise à jour de profil
- Test: Suppression d'utilisateur
- Test: Gestion des préférences et cours complétés

### CU11 - Filtrer les avis

- Test: Filtrage par session
- Test: Filtrage par difficulté
- Test: Filtrage par charge de travail

### CU08 - Recommandations personnalisées

- Tests complets à développer

---

## Conclusion

Cette spécification TDD couvre l'ensemble des fonctionnalités implémentées dans le jalon 2. Les 15 tests unitaires valident les scénarios principaux et les cas d'erreur des 5 cas d'utilisation prioritaires. L'approche TDD est démontrée par la description précise des entrées, sorties attendues et effets de bord pour chaque test.

Les tests qui ne sont pas encore implémentés sont documentés pour guider le développement des prochains jalons, démontrant une vision complète de la stratégie de test du projet.
