# Exemples de requêtes et réponses

Cette section présente quelques exemples représentatifs d’appels à l’API REST.
Les formats exacts peuvent varier selon l’implémentation, mais les exemples
ci-dessous correspondent aux **routes réellement exposées par l’application**.

---

## Exemple 1 — Recherche de cours

### Requête
```http
GET /courses/search?q=algorithmique

{
  "results": [
    {
      "id": "IFT2015",
      "sigle": "IFT2015",
      "title": "Algorithmique",
      "credits": 3
    }
  ]
}
```

## Exemple 2 — Détails d’un cours

### Requête
```http
GET /courses/IFT2255

{
  "sigle": "IFT2255",
  "title": "Génie logiciel",
  "credits": 3,
  "description": "Introduction aux principes du génie logiciel",
  "cycle": "Baccalauréat"
}
```

## Exemple 3 — Horaire d’un cours

### Requête
```http
GET /courses/IFT2255/schedule

{
  "courseId": "IFT2255",
  "sections": [
    {
      "section": "A",
      "type": "Cours",
      "day": "Lundi",
      "start": "09:00",
      "end": "12:00",
      "room": "D-123"
    }
  ]
}
```

## Exemple 4 — Résultats académiques agrégés

### Requête
```http
GET /courses/IFT2255/results

{
  "sigle": "IFT2255",
  "moyenne": "B+",
  "score": 4,
  "participants": 320,
  "trimestres": 6
}

```

## Exemple 5 — Récupérer les avis étudiants d’un cours

### Requête
```http
GET /reviews/IFT2255

{
  "courseId": "IFT2255",
  "reviews": [
    {
      "rating": 4,
      "workload": 3,
      "comment": "Bonne charge de travail",
      "author": "etudiant123"
    }
  ]
}


```

## Exemple 6 — Soumettre un avis étudiant

### Requête
```http
POST /reviews

{
  "courseId": "IFT2255",
  "reviews": [
    {
      "rating": 4,
      "workload": 3,
      "comment": "Bonne charge de travail",
      "author": "etudiant123"
    }
  ]
}


```

## Remarque
> Ces exemples sont fournis à titre illustratif afin de faciliter la compréhension
> et l’évaluation de l’API. Ils reflètent les routes réellement définies dans
> l’implémentation du projet.
 