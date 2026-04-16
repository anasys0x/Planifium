# API REST — Vue d’ensemble

L’API REST constitue le point d’accès principal aux fonctionnalités de l’application développée dans le cadre du **Devoir 3 – Implémentation de la solution**.  
Elle est conçue comme une **façade** devant les sources de données externes et internes afin de fournir des services cohérents, validés et adaptés aux cas d’utilisation du projet.

## Rôle de l’API
L’API permet notamment :<br>
- de rechercher et consulter des cours ;<br>
- d’accéder aux horaires pour un trimestre donné ;<br>
- de consulter des résultats académiques agrégés ;<br>
- de consulter et soumettre des avis étudiants ;<br>
- de comparer des cours et de gérer des ensembles de cours.

L’ensemble de la logique métier est centralisé au niveau de l’API afin d’assurer la cohérence des règles de validation et de faciliter l’évolution du système.

## Architecture générale
L’API est développée en **Java** à l’aide du framework **Javalin** et suit une architecture de type **MVC** :<br>
- les **contrôleurs** exposent les endpoints HTTP ;<br>
- les **services** encapsulent la logique métier ;<br>
- les **repositories** gèrent l’accès aux données locales ou externes.

Les appels à l’API Planifium sont effectués via un client HTTP dédié, ce qui permet d’isoler les dépendances externes.

## Sources de données
L’API s’appuie sur plusieurs sources :<br>
- une **API externe (Planifium)** pour le catalogue officiel des cours et les horaires ;<br>
- des **données locales** (CSV) pour les résultats académiques agrégés ;<br>
- des **avis étudiants** soumis via un bot Discord et persistés localement.

## Principes généraux
- Toutes les réponses sont retournées au format **JSON**.
- Les paramètres reçus sont systématiquement **validés** avant traitement.
- Les erreurs sont gérées de manière contrôlée afin d’éviter les arrêts inattendus de l’application.

> La description détaillée des endpoints, des paramètres et des formats de réponse est présentée dans la section **Endpoints** du rapport.
