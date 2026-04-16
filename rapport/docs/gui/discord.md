# Bot Discord

Un bot Discord est utilisé pour la collecte des avis étudiants.

## Rôle
- Permet aux étudiants de soumettre des avis pour un cours.
- Transmet les avis à l’API REST via `POST /reviews`.
- Ne contient aucune logique métier.

## Fonctionnement
Les avis peuvent être soumis :
- via une **commande Discord** ;
- ou dans un **canal dédié** détecté par le bot.

## Traitement
- Les données sont envoyées à l’API au format JSON.
- La validation et la persistence sont gérées côté API.
- Un message de confirmation ou d’erreur est retourné à l’utilisateur.

## Syntaxe (exemple)

Les avis peuvent être soumis via une commande simple ou un message structuré.

**Exemple de commande :**
```!review <courseId> <difficulte(0-10)> [commentaire] ```

**Exemple :**
```!review IFT2255 6 Pas trop dur malgre la charge de travail```

```

- `courseId` : sigle du cours  
- `difficulte` : (1 à 10)   
- `commentaire` : votre commentaire par rapport a la charge de travail / comportement du prof

```