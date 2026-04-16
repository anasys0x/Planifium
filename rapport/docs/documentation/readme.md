# Explorateur de Cours

**UdeM | Explorateur de Cours** est un outil destiné aux étudiants de l'Université de Montréal pour les aider à faire des choix de cours éclairés. Cette application combine des données officielles (résultats académiques, informations de l'API Planifium) et des avis étudiants collectés via Discord pour offrir une vue complète et utile des cours disponibles.

## Table des matières

- [Fonctionnalités](#fonctionnalités)
- [Organisation des fichiers](#organisation-des-fichiers)
- [Prérequis](#prérequis)
- [Installation](#installation)
- [Exécution de l'application](#exécution-de-lapplication)
- [Exécution des tests](#exécution-des-tests)
- [Bot Discord](#bot-discord)
- [Documentation](#documentation)
- [Équipe](#équipe)

---

## Fonctionnalités

L'application offre les fonctionnalités suivantes pour le rôle **Étudiant** :

### Recherche et consultation de cours

| # | Fonctionnalité | Description |
|---|----------------|-------------|
| 1 | **Rechercher des cours** | Recherche par sigle partiel (ex: "IFT" retourne tous les IFT*) ou par mots-clés dans le titre/description |
| 2 | **Voir les cours offerts dans un programme** | Affiche la liste des cours disponibles pour un programme donné |
| 3 | **Voir les cours offerts pour un trimestre** | Affiche les cours disponibles pour un trimestre donné (format: H25, A24, E24), avec possibilité de filtrer par programme |
| 4 | **Voir l'horaire d'un cours** | Affiche l'horaire d'un cours pour un trimestre donné, en distinguant les sections et les types d'activité |
| 5 | **Vérifier son éligibilité à un cours** | L'étudiant fournit sa liste de cours complétés et son cycle; l'outil vérifie si les prérequis sont satisfaits et si le cycle est approprié |

### Résultats et avis

| # | Fonctionnalité | Description |
|---|----------------|-------------|
| 6 | **Voir les résultats académiques d'un cours** | Affiche les statistiques agrégées (moyenne, score, participants) pour un cours donné |
| 7 | **Voir les avis étudiants pour un cours** | Consulte les avis étudiants agrégés pour un cours donné |
| 8 | **Soumettre un avis pour un cours** | Permet de soumettre un nouvel avis via le bot Discord |

### Comparaison et planification

| # | Fonctionnalité | Description |
|---|----------------|-------------|
| 9 | **Comparer deux cours** | Compare deux cours en utilisant les avis (charge de travail), les résultats agrégés (difficulté) et le catalogue (autres critères) |
| 10 | **Créer un ensemble de cours** | Permet de créer des ensembles de cours (max 6 cours) et voir l'horaire résultant pour le trimestre visé |
| 11 | **Détecter les conflits d'horaire** | Détecte automatiquement les conflits d'horaire dans un ensemble de cours *(Bonus 2%)* |
| 12 | **Comparer des ensembles de cours** | Compare des ensembles de cours de manière similaire à la comparaison de deux cours |

---

## Organisation des fichiers

```
Devoir-2/
├── rapport/                                    # Documentation et rapport
│   ├── docs/
│   │   ├── conception/                         # Diagrammes et documents de conception
│   │   │   ├── C4-niveau-3.png
│   │   │   ├── diagramme-classes.png
│   │   │   └── diagrammes-sequence/
│   │   └── javadoc/                            # Documentation Javadoc générée
│   └── ...
│
├── rest-api/                                   # Code source principal
│   ├── pom.xml                                 # Configuration Maven
│   ├── data/
│   │   └── reviews.xml                         # Stockage des avis étudiants (XML)
│   │
│   └── src/
│       ├── main/
│       │   └── java/
│       │       └── com/
│       │           └── diro/
│       │               └── ift2255/
│       │                   ├── Main.java       # Point d'entrée de l'application
│       │                   │
│       │                   ├── controller/     # Contrôleurs REST (endpoints API)
│       │                   │   ├── EnsembleCoursController.java
│       │                   │   ├── ConflitHoraireController.java
│       │                   │   ├── ComparerEnsemblesController.java
│       │                   │   └── ...
│       │                   │
│       │                   ├── service/        # Logique métier
│       │                   │   ├── EnsembleCoursService.java
│       │                   │   ├── ConflitHoraireService.java
│       │                   │   ├── ComparerEnsemblesService.java
│       │                   │   └── ...
│       │                   │
│       │                   ├── repository/     # Accès aux données
│       │                   │   ├── EnsembleCoursRepository.java
│       │                   │   ├── ConflitHoraireRepository.java
│       │                   │   ├── ComparerEnsemblesRepository.java
│       │                   │   └── ...
│       │                   │
│       │                   ├── model/          # Classes de modèle (entités)
│       │                   │   ├── Course.java
│       │                   │   ├── EnsembleCours.java
│       │                   │   ├── Horaire.java
│       │                   │   ├── ConflitHoraire.java
│       │                   │   ├── ComparaisonEnsembles.java
│       │                   │   └── ...
│       │                   │
│       │                   ├── discord/        # Bot Discord
│       │                   │   └── ...
│       │                   │
│       │                   ├── util/           # Classes utilitaires
│       │                   │   └── HttpClientApi.java
│       │                   │
│       │                   └── resources/
│       │                       ├── data/       # Données (CSV des résultats académiques)
│       │                       │   └── historique_cours_prog_117510.csv
│       │                       │
│       │                       └── public/     # Interface graphique (GUI)
│       │                           ├── index.html
│       │                           ├── app.js
│       │                           └── style.css
│       │
│       └── test/
│           └── java/
│               └── com/
│                   └── diro/
│                       └── ift2255/
│                           └── service/        # Tests unitaires
│                               ├── EnsembleCoursServiceTest.java
│                               ├── ConflitHoraireServiceTest.java
│                               ├── ComparerEnsemblesServiceTest.java
│                               └── ...
│
└── README.md                                   # Ce fichier
```

---

## Prérequis

Avant d'installer et d'exécuter l'application, assurez-vous d'avoir les éléments suivants installés :

| Outil | Version requise | Vérification |
|-------|-----------------|--------------|
| **Java JDK** | 17 ou supérieur | `java -version` |
| **Apache Maven** | 3.8.0 ou supérieur | `mvn -version` |
| **Git** | Toute version récente | `git --version` |

### Librairies externes (gérées automatiquement par Maven)

Les dépendances suivantes sont téléchargées automatiquement lors de la compilation :

| Librairie | Version | Description |
|-----------|---------|-------------|
| Javalin | 6.7.0 | Framework web léger pour l'API REST |
| Jackson | 2.17.2 | Sérialisation/désérialisation JSON |
| JDA | 5.0.0-beta.20 | Java Discord API pour le bot |
| SLF4J | 2.0.16 | Logging |
| JUnit 5 | 5.10.2 | Tests unitaires |
| Mockito | 5.18.0 | Mocking pour les tests |

---

## Installation

### 1. Cloner le dépôt

```bash
git clone https://github.com/anasys0x/Devoir-2.git
cd Devoir-2
```

### 2. Compiler le projet

```bash
cd rest-api
mvn clean compile
```

Cette commande télécharge automatiquement toutes les dépendances et compile le code source.

### 3. Vérifier l'installation

```bash
mvn validate
```

Si aucune erreur n'apparaît, l'installation est réussie.

---

## Exécution de l'application

### Démarrer le serveur

Depuis le répertoire `rest-api/` :

```bash
mvn exec:java -Dexec.mainClass="com.diro.ift2255.Main"
```

Le serveur démarre et l'application est accessible à l'adresse :

```
http://localhost:8070
```

> **Note** : Le port par défaut est 8070. Vérifiez la console pour confirmer le port utilisé.

### Accéder à l'interface graphique

Une fois le serveur démarré, ouvrez votre navigateur et accédez à :

```
http://localhost:8070
```

L'interface graphique (GUI) se charge automatiquement et vous permet d'interagir avec toutes les fonctionnalités de l'application.

> **Important** : N'essayez pas d'ouvrir directement le fichier `index.html` via un live server ou en double-cliquant dessus. L'interface doit être servie par le serveur Javalin pour fonctionner correctement.

### Arrêter le serveur

Appuyez sur `Ctrl+C` dans le terminal où le serveur est en cours d'exécution.

---

## Exécution des tests

### Exécuter tous les tests

Depuis le répertoire `rest-api/` :

```bash
mvn test
```

### Exécuter les tests d'une classe spécifique

```bash
# Tests pour CU#10 - Créer un ensemble de cours
mvn test -Dtest=EnsembleCoursServiceTest

# Tests pour CU#11 - Détecter les conflits d'horaire
mvn test -Dtest=ConflitHoraireServiceTest

# Tests pour CU#12 - Comparer des ensembles de cours
mvn test -Dtest=ComparerEnsemblesServiceTest
```

### Exécuter les tests avec rapport détaillé

```bash
mvn test -X
```

### Structure des tests

Chaque fonctionnalité dispose de **5 cas de tests distincts** avec JUnit 5 :

| Classe de test | Fonctionnalité testée |
|----------------|----------------------|
| `EnsembleCoursServiceTest` | Création et gestion des ensembles de cours |
| `ConflitHoraireServiceTest` | Détection des conflits d'horaire |
| `ComparerEnsemblesServiceTest` | Comparaison des ensembles de cours |

### Oracle de tests

Pour chaque test, l'oracle définit :
- **Jeu d'arguments** : Les données d'entrée
- **Retour attendu** : Le résultat attendu de la méthode
- **Effets de bord** : Les modifications d'état attendues

Consultez le rapport pour les détails complets de chaque oracle de test.

---

## Bot Discord

Le bot Discord s'active automatiquement au démarrage du serveur et permet aux étudiants de soumettre des avis sur les cours.

### Serveur Discord de test

Rejoignez le serveur Discord pour tester le bot :

```
[LIEN D'INVITATION DISCORD À AJOUTER]
```

### Canal pour les avis

Les avis doivent être soumis dans le canal **#avis-cours**.

### Configuration du bot

Le bot nécessite un token d'autorisation Discord. Consultez la configuration dans `Main.java` ou les variables d'environnement selon l'implémentation actuelle.

> **Note pour les auxiliaires** : Assurez-vous d'avoir accès au serveur Discord pour tester la fonctionnalité de soumission d'avis.

---

## Documentation

### Javadoc

La documentation Javadoc est présent dans les annotations de chaque fichier java en notation html.

Pour générer la documentation Javadoc :

```bash
cd rest-api
mvn javadoc:javadoc
```

La documentation générée se trouve dans `target/site/apidocs/` ou sous rest-api, sinon il est déjà présent directement dans les fichiers java.

### Conception

Les diagrammes de conception sont disponibles dans `rapport/docs/conception/` :

- Diagramme C4 (niveau 3)
- Diagramme de classes
- Diagrammes de séquence

### Rapport

Le rapport complet du projet est disponible dans le dossier `rapport/`.

---

## API REST - Endpoints principaux

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/api/courses` | Rechercher des cours |
| `GET` | `/api/courses/{id}` | Détails d'un cours |
| `GET` | `/api/courses/{id}/schedule` | Horaire d'un cours |
| `GET` | `/api/courses/{id}/results` | Résultats académiques |
| `GET` | `/api/courses/{id}/reviews` | Avis étudiants |
| `POST` | `/api/reviews` | Soumettre un avis |
| `GET` | `/api/courses/compare` | Comparer deux cours |
| `POST` | `/api/ensembles` | Créer un ensemble de cours |
| `GET` | `/api/ensembles/{id}` | Détails d'un ensemble |
| `GET` | `/api/ensembles/{id}/schedule` | Horaire combiné |
| `GET` | `/api/ensembles/{id}/conflicts` | Détecter les conflits |
| `GET` | `/api/ensembles/compare` | Comparer des ensembles |

> **Note** : Les endpoints exacts peuvent varier. Consultez le code source pour la liste complète.

---

## Sources de données

| Source | Format | Emplacement | Description |
|--------|--------|-------------|-------------|
| **API Planifium** | REST API | Externe | Catalogue des cours, programmes et horaires de l'UdeM |
| **Résultats académiques** | CSV | `resources/data/` | Statistiques agrégées des cours |
| **Avis étudiants** | XML | `rest-api/data/reviews.xml` | Avis collectés via Discord |

---

### 👥 Équipe

- **`M1`:** Aymane, Rachidi (20261554) #Aym
- **`M2`:** Anas, Mrani Alaoui (20257568) #Anasys0x
- **`M3`:** Elimelach, Lowen (20320193) #Halloween Lime
- **`M4`:** Samuel, Udeme (20294883) #udeme_sml

---

## Licence

Ce projet a été réalisé dans le cadre du cours IFT2255 - Génie logiciel à l'Université de Montréal.

---

*Dernière mise à jour : Décembre 2025*
