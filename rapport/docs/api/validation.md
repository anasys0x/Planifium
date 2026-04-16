# Validation et gestion des erreurs

Cette section décrit les principes généraux de validation des entrées ainsi que
le format standard utilisé pour la gestion des erreurs dans l’API REST.

---

## Règles générales de validation
- Tous les champs requis sont **explicitement validés** (présence et type).
- Les identifiants de cours doivent respecter le **format attendu**  
  (ex. `IFT2255`, insensible à la casse selon l’implémentation).
- Les champs de notation tels que `rating`, `workload` ou `difficulty` doivent être
  des **entiers compris entre 1 et 5**.
- Les codes de trimestre doivent respecter le format :
  `H25`, `A24`, `E24`, etc.
- Les paramètres reçus via le corps (`POST`) ou les paramètres de requête (`GET`)
  sont validés avant tout traitement métier.

---

## Gestion des erreurs
L’API ne doit pas s’arrêter abruptement lorsqu’une entrée invalide est reçue.
Toute erreur est interceptée et retournée sous une forme contrôlée, avec un
message explicite permettant au client de comprendre la cause du problème.

---

## Format d’erreur standard

En cas d’erreur, l’API retourne un objet JSON standardisé.

### Exemple — Erreur de validation (400 Bad Request)
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Le champ 'courseId' est requis.",
  "details": [
    "courseId: missing"
  ]
}
