# 🎓 IFT2255 - Plateforme d'Aide au Choix de Cours UdeM

## 📄 Phase 2 : Conception, Implémentation & Intégration

Ce dépôt contient la **conception détaillée**, l’implémentation de **l’API REST**, **l’interface web**, **l’intégration Discord** ainsi que **les tests unitaires** requis pour le Devoir 2 du cours IFT2255.

Le système vise à offrir aux étudiants une plateforme centralisée regroupant :

- les données officielles (Planifum),

- la recherche avancée de cours,

- un comparateur dynamique,

- les avis étudiants collectés automatiquement via un bot Discord.

---

## 📁 Structure générale du projet

| Fichier/Dossier                       | Contenu Principal                                                                 |
| :------------------------------------ | :-------------------------------------------------------------------------------- |
| **`rest-api/`**                       | Code source Java (Javalin) : contrôleurs, services, modèles, routes.              |
| `rest-api/src/main/java/...`          | Architecture MVC complète (Course, User, Review).                                 |
| `rest-api/src/main/resources/public/` | Interface web (HTML, CSS, JS).                                                    |
| **`discord/`** (dans `rest-api`)      | Bot Discord permettant de collecter les avis étudiants via la commande `!review`. |
| **`src/test/java/...`**               | Tests unitaires JUnit5 + Mockito (3+ tests par membre).                           |
| `pom.xml`                             | Dépendances Maven (Javalin, JDA, JUnit, Mockito).                                 |

---

### 🚀 Fonctionnalités Implémentées

### 🔹 API REST (Javalin)

- Recherche de cours → /courses, /courses/:id

- Comparaison multi-cours → /courses/compare

- Gestion des utilisateurs mockés

- Intégration d’un ReviewService pour stocker les avis

### 🔹 Interface Web

- Recherche dynamique (sigle ou mot-clé)

- Filtres automatiques

- Cartes de cours + sessions + prérequis

- Comparateur visuel avec total des crédits

- Bouton "Voir détails" (description animée)

- Bouton “Retour en haut”

- UX cohérente et moderne (HTML/CSS/JS)

### 🔹 Bot Discord (JDA)

- Collecte des avis via la commande :

> !review <sigle> <difficulté 1-10> \<commentaire> <br>
> Example: !review IFT2255 7 Beaucoup de pratique, prof clair, j’ai bien aimé

**Le bot :**

- analyse le message,

- valide la syntaxe,

- crée un nouvel avis (Review),

- transmet l’avis au service Java,

- répond automatiquement dans Discord.

## 🔹 Tests unitaires

#### **Anas**

- `CourseServiceTest.testGetAllCourses`
- `CourseServiceTest.testGetCourseByIdNotExists`
- `CourseServiceTest.testGetAllCoursesWithQueryParams`

#### **Aymane**

- `ReviewServiceTest.testGetReviewsForCourseExists`
- `ReviewServiceTest.testGetReviewsForCourseNotExists`
- `ReviewServiceTest.testAddReview`

#### **Eli**

- `CourseServiceTest.testGetCourseByIdExists`
- `CourseServiceTest.testCompareCourses`
- `ReviewServiceTest.testAddMultipleReviews`

#### **Udeme**

- `UserControllerTest.testCreateUserWithInvalidEmail`
- `UserControllerTest.testCreateUserWithValidEmail`
- `CourseControllerTest.testCompareCourses`

---

## 🧱 Architecture utilisée

- Architecture MVC : Controller → Service → Model

- Single-page frontend avec fetch API

- JDA pour la communication Discord→API

---

La documentation complète de la Phase 1 est organisée dans le dossier `docs/` pour la génération du rapport MkDocs.

| Fichier/Dossier                  | Contenu Principal                                                                |
| :------------------------------- | :------------------------------------------------------------------------------- |
| **`besoins/`**                   | Regroupe l'analyse détaillée des exigences fonctionnelles et non-fonctionnelles. |
| `besoins/risques.md`             | Analyse détaillée des 5+ Risques du projet (Justification et Mitigation).        |
| `besoins/domaine.md`             | Description du domaine, des acteurs et des dépendances.                          |
| `besoins/glossaire.md`           | Définitions des termes clés du domaine.                                          |
| `besoins/exigences.md`           | Transformation des souhaits en exigences vérifiables.                            |
| `besoins/cas-utilisation.md`     | Description des cas d'utilisation (besoins fonctionnels).                        |
| `besoins/flux-principaux.md`     | Diagrammes d'activités pour les flux du système.                                 |
| **`conception/`**                | Regroupe les artefacts de modélisation du système.                               |
| `conception/architecture.md`     | Modèle C4 (Niveaux 1 2 3 et 4).                                                  |
| `conception/modeles.md`          | Modèles UML (Diagramme de classes ou autres).                                    |
| `index.md` (Racine)              | Page d'accueil du dépôt.                                                         |
| `docs/index.md`                  | Page d'accueil du rapport MkDocs.                                                |
| `.gitignore`, `mkdocs.yml`, etc. | Fichiers de configuration du projet.                                             |

---

### 👥 Équipe

- **`M1`:** Aymane, Rachidi (20261554) #Aym
- **`M2`:** Anas, Mrani Alaoui (20257568) #Anasys0x
- **`M3`:** Elimelach, Lowen (20320193) #Halloween Lime
- **`M4`:** Samuel, Udeme (20294883) #udeme_sml
