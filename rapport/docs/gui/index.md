# Interface utilisateur

Aucune interface en ligne de commande (CLI) n’a été implémentée dans le
cadre de ce projet.

L’interaction avec le système se fait principalement via :<br>
- une **interface web** utilisée pour la démonstration et la consultation des données ;<br>
- un **bot Discord** permettant de collecter et de soumettre des avis étudiants.

## Rôle des interfaces
Ces interfaces agissent comme des **clients** de l’API REST :<br>
- elles envoient des requêtes HTTP vers l’API ;<br>
- elles ne contiennent aucune logique métier propre ;<br>
- toute la validation et le traitement sont centralisés côté API.

## Justification
Le choix d’une interface web et d’un bot Discord permet :<br>
- une interaction plus naturelle pour les utilisateurs ;<br>
- une démonstration visuelle des fonctionnalités clés ;<br>
- une meilleure cohérence avec les objectifs du projet.

> L’API REST constitue le point central du système et reste entièrement
utilisable indépendamment des interfaces.
