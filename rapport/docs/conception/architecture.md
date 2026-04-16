---
title: Conception - Architecture
---

# Architecture du système

## Vue d’ensemble

Le système **Planif+** adopte une architecture **client–serveur** de type **REST**, organisée en trois couches :
- **Frontend (application web)** : interface utilisateur permettant de rechercher, consulter et comparer des cours.  
- **Backend (API REST)** : gère la logique métier, les intégrations externes (Planifium, Discord, CSV) et la consolidation des données.  
- **Fichiers internes (des avis)** : stocke les avis étudiants.
- **Base de données** : stocke les informations agrégées sur les cours et statistiques.  

Ce choix permet une **séparation claire des responsabilités**, une **évolutivité** aisée et une **intégration fluide** avec les systèmes externes.


## Composants principaux

- **Application Web (Fronting Git repository in /home/halloweenlime/Desktop/ift2255/Devoirs/Devoir2/Devoir-2-projet-git-équipe/Devoir-2/.git/tend)** : interface de consultation et comparaison des cours.  
- **API REST (Backend)** : gère la logique métier et interagit avec les systèmes externes.  
- **Base de données interne** : conserve les données consolidées pour réduire les appels externes.  
- **Sources externes** :  
  - API Planifium → informations officielles sur les cours.  
  - Bot Discord → avis étudiants.  
  - Fichiers CSV → statistiques académiques agrégées.  

## Communication entre composants

- **Frontend ↔ Backend** : appels **HTTP/HTTPS** au format **JSON**.  
- **Backend ↔ Base de données** : requêtes **SQL** via un ORM.  
- **Backend ↔ Systèmes externes** : échanges **HTTPS/JSON** (Planifium, Discord) et **lecture de fichiers CSV**.  

## Diagramme d’architecture (Modèle C4)

### 🔹 Niveau 1 — Vue du système (Contexte)

![Diagramme C4 Niveau 1](diagrammes/C4-Niveau%201.png)

**Description :**
Ce diagramme présente la vue d’ensemble du système **Planif+** et ses interactions avec :
- l’**étudiant** (acteur principal),
- les **systèmes externes** : API Planifium, Bot Discord et fichiers CSV.

---

### 🔹 Niveau 2 — Vue des conteneurs

![Diagramme C4 Niveau 2](diagrammes/C4-Niveau%202.png)

**Description :**
Ce diagramme illustre la structure interne de **Planif+**, composée de trois conteneurs :
- l’**Application Web (Frontend)**,  
- l’**API REST (Backend)**,  
- la **Base de données interne**,  
ainsi que leurs échanges avec les systèmes externes.

### 🔹 Niveau 3 — Component

![Diagramme C4 Niveau 3](diagrammes/C4-Niveau%203.png)

# C4 Niveau 3 - Explication de l'architecture

## Vue d'ensemble de l'architecture

La plateforme d'aide au choix de cours repose sur une **architecture trois-couches** (MVC adaptée pour API REST) avec une **séparation claire des responsabilités**. Cette architecture favorise la modularité, la testabilité et l'évolutivité.

---

## Flux de communication

### 1. Entrée des utilisateurs
Les **utilisateurs** (Étudiants) et **systèmes externes** (DiscordBot) communiquent avec la plateforme via des **appels HTTP REST** adressés aux **Contrôleurs** :
- Les Étudiants envoient des requêtes GET/POST via l'interface web (recherche, consultation, comparaison de cours)
- Le DiscordBot envoie des POST pour soumettre des avis collectés depuis Discord

### 2. Couche Contrôleur (HTTP REST)
Les **Contrôleurs** reçoivent les requêtes HTTP, valident les paramètres et délèguent la logique métier aux **Services** :
- **CoursController** : gère la recherche, consultation et comparaison de cours
- **AvisController** : gère la soumission et consultation des avis
- **ProfilController** : gère la création et mise à jour des profils étudiants

### 3. Couche Service (Logique métier)
Les **Services** exécutent la **logique métier** et orchestrent les appels aux **Repositories** :
- **CoursService** : recherche, vérification d'éligibilité, comparaison
- **AvisService** : agrégation des avis, anonymisation, validation du seuil (n≥5)
- **ProfilService** : gestion des profils, recommandations personnalisées
- **PlanifiumService** : interface vers l'API externe Planifium (données officielles)

### 4. Couche Repository (Accès aux données)
Les **Repositories** abstractent l'accès aux données et communiquent avec les **sources de données** :
- **CoursRepository** : récupère les cours via Planifium API, maintient cache local
- **AvisRepository** : lit/écrit les avis depuis/vers Base de données des avis
- **ProfilRepository** : lit/écrit les profils depuis/vers fichiers JSON

### 5. Sources de données
- **Planifium API** : fournit les données officielles des cours (catalogue, préalables, horaires)
- **CSV Storage** : stocke les résultats académiques agrégés
- **JSON Storage** : stocke les profils des étudiants

---

## Avantages de cette architecture

### Modularité
Chaque couche a une responsabilité unique et bien définie. Les équipes peuvent développer en parallèle sans dépendances bloquantes (une équipe sur CoursService, une autre sur AvisService).

### Testabilité
Chaque composant peut être testé indépendamment. Les dépendances sont injectées et peuvent être mockées lors des tests unitaires.

### Séparation des sources de données
L'architecture distinguish clairement entre :
- **Planifium API** : données officielles (via HTTP REST)
- **CSV Storage** : données collectées (avis étudiants, résultats)
- **JSON Storage** : données utilisateur (profils)

Cette séparation reflète la nature différente de ces données et facilite la gestion indépendante de chaque source.

### Flexibilité technologique
Les Repositories permettent de changer facilement de technologie de persistance (CSV → JSON → SQL) sans modifier les Services ou Contrôleurs. Les interfaces des Repositories supportent plusieurs implémentations possibles.

### Évolutivité
Nouvelles fonctionnalités peuvent être ajoutées en créant de nouveaux Services et Repositories sans modifier l'existant. La plateforme peut croître sans refonte majeure.

### Maintenabilité
Les changements sont localisés et prévisibles. Un bug dans le Repository n'affecte que les Services qui l'utilisent. Les modifications dans un Service n'impactent pas les Contrôleurs tant que le contrat d'interface est préservé.

---

## Cas d'utilisation couverts par cette architecture

1. **Recherche de cours (CU01)** : 
   - Étudiant → CoursController → CoursService → CoursRepository → Planifium API
   - Cache local maintenu pour performances

2. **Comparaison de cours (CU03)** :
   - Combinaison de données officielles (Planifium) + avis agrégés (CSV)
   - CoursController → CoursService + AvisService → CoursRepository + AvisRepository

3. **Soumission d'avis (CU06)** :
   - DiscordBot → AvisController → AvisService → AvisRepository → CSV Storage
   - Anonymisation automatique lors de la sauvegarde

---

## Conclusion

L'architecture à trois couches avec séparation des sources de données (Planifium, CSV, JSON) offre une base solide pour la plateforme. Elle respecte les principes SOLID, favorise la modularité et la testabilité, et permet une évolution incrémentale sans coûts de refonte majeure.



