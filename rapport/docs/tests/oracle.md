# Oracle de tests

Ce document décrit pour chaque test unitaire: le jeu d'arguments, le retour attendu et les effets de bord attendus.

---

## 1. Tests de EnsembleCoursService (CU#10 - Créer un ensemble de cours)

### Test 10.1 - testCreerEnsembleAvecParametresValides

**Cas d'utilisation**: CU10 - Créer un ensemble de cours

**Jeu d'arguments**:

- `nom = "Mon Trimestre A25"`
- `trimestre = "A25"`
- `coursIds = ["IFT1015", "IFT2255", "MAT1400"]`
- `mockClientApi.get(URI)` retourne des objets `Course` simulés pour chaque ID

**Retour attendu**:

- `EnsembleCours` non null avec:
  - `id` auto-généré (format "ENS-XXXXX")
  - `nom = "Mon Trimestre A25"`
  - `trimestre = "A25"`
  - `coursIds.size() = 3`
  - `dateCreation` non null

**Effets de bord**:

- Appel à `ensembleRepository.save(ensemble)` effectué une fois
- Appel à `mockClientApi.get(URI)` effectué 3 fois (une fois par cours)
- Les IDs de cours sont normalisés en majuscules
- Aucune exception lancée

---

### Test 10.2 - testCreerEnsembleEchoueSiPlusDe6Cours

**Cas d'utilisation**: CU10 - Créer un ensemble de cours (validation)

**Jeu d'arguments**:

- `nom = "Trop de cours"`
- `trimestre = "A25"`
- `coursIds = ["IFT1015", "IFT1025", "IFT2255", "IFT2505", "MAT1400", "MAT1720", "PHY1441"]` (7 cours)

**Retour attendu**:

- `IllegalArgumentException` lancée
- Message contient "6" ou "maximum" ou "limite"

**Effets de bord**:

- **Aucun** appel à `ensembleRepository.save()` (validation avant persistence)
- **Aucun** appel à l'API externe (validation avant récupération des données)
- L'ensemble n'est pas créé

---

### Test 10.3 - testCreerEnsembleEchoueSiTrimestreInvalide

**Cas d'utilisation**: CU10 - Créer un ensemble de cours (validation du trimestre)

**Jeu d'arguments**:

- `nom = "Test Invalide"`
- `trimestre = "INVALID"` (format invalide, devrait être A25, H25, E25)
- `coursIds = ["IFT1015"]`

**Retour attendu**:

- `IllegalArgumentException` lancée
- Message contient "trimestre" ou "format" ou "invalide"

**Effets de bord**:

- **Aucun** appel à `ensembleRepository.save()`
- **Aucun** appel à l'API externe
- Validation du format échoue avant toute opération

---

### Test 10.4 - testAjouterCoursAEnsembleExistant

**Cas d'utilisation**: CU10 - Créer un ensemble de cours (ajout de cours)

**Jeu d'arguments**:

- `ensembleId = "ENS-00001"` (ensemble existant avec 2 cours)
- `courseId = "MAT1400"` (cours à ajouter)
- `ensembleRepository.findById("ENS-00001")` retourne l'ensemble existant
- `mockClientApi.get(URI)` retourne les détails du cours MAT1400

**Retour attendu**:

- `EnsembleCours` mis à jour avec:
  - `coursIds.size() = 3` (2 existants + 1 nouveau)
  - `coursIds.contains("MAT1400") = true`
  - Autres propriétés inchangées

**Effets de bord**:

- Appel à `ensembleRepository.findById("ENS-00001")` effectué
- Appel à `ensembleRepository.update(ensemble)` effectué
- Appel à l'API pour récupérer les détails du nouveau cours
- Le cours est ajouté à la liste existante (pas de remplacement)

---

### Test 10.5 - testGetHoraireCombineRetourneHoraires

**Cas d'utilisation**: CU10 - Créer un ensemble de cours (consultation de l'horaire)

**Jeu d'arguments**:

- `ensembleId = "ENS-00001"`
- L'ensemble contient les cours `["IFT1015", "IFT2255"]`
- Les horaires sont déjà chargés dans l'ensemble

**Retour attendu**:

- `Map<String, List<Horaire>>` non null contenant:
  - Clé `"IFT1015"` avec liste d'horaires non vide
  - Clé `"IFT2255"` avec liste d'horaires non vide
  - Chaque `Horaire` contient: jour, heureDebut, heureFin, local

**Effets de bord**:

- Appel à `ensembleRepository.findById("ENS-00001")` effectué
- Lecture seule de la Map `horaires` de l'ensemble
- Aucune modification des données

---

## 2. Tests de ConflitHoraireService (CU#11 - Détecter les conflits d'horaire)

### Test 11.1 - testDetecterConflitTotalHorairesIdentiques

**Cas d'utilisation**: CU11 - Détecter les conflits d'horaire (conflit total)

**Jeu d'arguments**:

- `ensembleId = "ENS-00001"`
- L'ensemble contient 2 cours avec horaires identiques:
  - IFT1015: Lundi 09:00-12:00
  - IFT2255: Lundi 09:00-12:00

**Retour attendu**:

- `List<ConflitHoraire>` de taille 1
- `conflit.getType() = ConflitHoraire.TypeConflit.TOTAL`
- `conflit.getHoraire1().getCourseId() = "IFT1015"`
- `conflit.getHoraire2().getCourseId() = "IFT2255"`
- `conflit.getDureeChevauchementsMinutes() = 180` (3 heures)

**Effets de bord**:

- Appel à `ensembleRepository.findById(ensembleId)` effectué
- Appel à `conflitRepository.detecterConflitsPourEnsemble(ensemble)` effectué
- Lecture seule, aucune modification de données

---

### Test 11.2 - testDetecterConflitPartielChevauchement

**Cas d'utilisation**: CU11 - Détecter les conflits d'horaire (conflit partiel)

**Jeu d'arguments**:

- `ensembleId = "ENS-00002"`
- L'ensemble contient 2 cours avec chevauchement partiel:
  - IFT1015: Mardi 09:00-12:00
  - MAT1400: Mardi 11:00-14:00 (chevauchement de 1 heure)

**Retour attendu**:

- `List<ConflitHoraire>` de taille 1
- `conflit.getType() = ConflitHoraire.TypeConflit.PARTIEL`
- `conflit.getDureeChevauchementsMinutes() = 60` (1 heure)

**Effets de bord**:

- Appel à `ensembleRepository.findById(ensembleId)` effectué
- Appel à `conflitRepository.detecterConflitsPourEnsemble(ensemble)` effectué
- Aucune modification de l'ensemble

---

### Test 11.3 - testAucunConflitHorairesSansChevauchement

**Cas d'utilisation**: CU11 - Détecter les conflits d'horaire (aucun conflit)

**Jeu d'arguments**:

- `ensembleId = "ENS-00003"`
- L'ensemble contient 2 cours sur jours différents:
  - IFT1015: Lundi 09:00-12:00
  - MAT1400: Mercredi 09:00-12:00

**Retour attendu**:

- `List<ConflitHoraire>` vide (size = 0)
- `conflits.isEmpty() = true`

**Effets de bord**:

- Appel à `ensembleRepository.findById(ensembleId)` effectué
- Appel à `conflitRepository.detecterConflitsPourEnsemble(ensemble)` retourne liste vide
- Aucune exception lancée

---

### Test 11.4 - testGetResumeConflits

**Cas d'utilisation**: CU11 - Détecter les conflits d'horaire (résumé)

**Jeu d'arguments**:

- `ensembleId = "ENS-00004"`
- L'ensemble a 3 cours avec 2 conflits partiels détectés

**Retour attendu**:

- `Map<String, Object>` contenant:
  - `"nombreConflits" = 2`
  - `"tempsChevauchementsMinutes"` = valeur positive
  - `"coursImpliques"` = liste des cours concernés
  - `"aDesConflits" = true`

**Effets de bord**:

- Appels à `ensembleRepository.findById()` et `conflitRepository.detecterConflitsPourEnsemble()`
- Calcul et agrégation des statistiques
- Lecture seule, aucune modification

---

### Test 11.5 - testDetecterConflitsEnsembleInexistant

**Cas d'utilisation**: CU11 - Détecter les conflits d'horaire (erreur)

**Jeu d'arguments**:

- `ensembleId = "ENS-INEXISTANT"`
- `ensembleRepository.findById("ENS-INEXISTANT")` retourne `Optional.empty()`

**Retour attendu**:

- `IllegalArgumentException` lancée
- Message contient "ENS-INEXISTANT" ou "inexistant" ou "non trouvé"

**Effets de bord**:

- Appel à `ensembleRepository.findById("ENS-INEXISTANT")` effectué
- **Aucun** appel à `conflitRepository` (arrêt avant détection)
- Gestion d'erreur propre, pas de crash

---

## 3. Tests de ComparerEnsemblesService (CU#12 - Comparer des ensembles)

### Test 12.1 - testComparerDeuxEnsemblesRetourneMetriques

**Cas d'utilisation**: CU12 - Comparer des ensembles de cours

**Jeu d'arguments**:

- `id1 = "ENS-00001"` (ensemble avec 2 cours: IFT1015, IFT1025)
- `id2 = "ENS-00002"` (ensemble avec 3 cours: IFT2255, IFT2505, MAT1400)
- Les deux ensembles existent dans le repository

**Retour attendu**:

- `ComparaisonEnsembles` non null avec:
  - `ensemble1` = référence à l'ensemble 1
  - `ensemble2` = référence à l'ensemble 2
  - `creditsEnsemble1 = 6` (2 cours × 3 crédits)
  - `creditsEnsemble2 = 9` (3 cours × 3 crédits)
  - `conflitsEnsemble1` et `conflitsEnsemble2` calculés

**Effets de bord**:

- Appels à `ensembleRepository.findById()` pour les deux IDs
- Appels à `conflitService.detecterConflits()` pour les deux ensembles
- Appel à `comparerRepository.save(comparaison)` pour mise en cache
- Calcul des métriques de comparaison

---

### Test 12.2 - testIdentifierMeilleurEnsemble

**Cas d'utilisation**: CU12 - Comparer des ensembles (recommandation)

**Jeu d'arguments**:

- `id1 = "ENS-00001"` (ensemble sans conflit)
- `id2 = "ENS-00002"` (ensemble avec 1 conflit)

**Retour attendu**:

- `ComparaisonEnsembles` avec:
  - `meilleurEnsembleId = "ENS-00001"`
  - `raisonRecommandation` contient justification (ex: "Moins de conflits")
  - `conflitsEnsemble1 = 0`
  - `conflitsEnsemble2 > 0`

**Effets de bord**:

- Analyse comparative effectuée
- Le meilleur ensemble est déterminé selon l'algorithme de scoring
- Résultat mis en cache

---

### Test 12.3 - testGenererTableauComparatif

**Cas d'utilisation**: CU12 - Comparer des ensembles (tableau)

**Jeu d'arguments**:

- `id1 = "ENS-00001"`
- `id2 = "ENS-00002"`

**Retour attendu**:

- `Map<String, Object>` contenant:
  - `"criteres"` = liste des critères comparés (crédits, conflits, charge, difficulté)
  - `"ensemble1"` = Map des valeurs pour ensemble 1
  - `"ensemble2"` = Map des valeurs pour ensemble 2
  - `"differences"` = différences calculées pour chaque critère

**Effets de bord**:

- Appels aux repositories pour récupérer les ensembles
- Calcul et formatage du tableau comparatif
- Lecture seule des données

---

### Test 12.4 - testComparerEnsembleInexistant

**Cas d'utilisation**: CU12 - Comparer des ensembles (erreur)

**Jeu d'arguments**:

- `id1 = "ENS-00001"` (existe)
- `idInexistant = "ENS-FAKE"` (n'existe pas)

**Retour attendu**:

- `IllegalArgumentException` lancée
- Message indique l'ID invalide ou "inexistant" / "non trouvé"

**Effets de bord**:

- Appel à `ensembleRepository.findById()` pour les deux IDs
- **Aucune** comparaison effectuée (échec de validation)
- **Aucun** appel à `comparerRepository.save()`

---

### Test 12.5 - testRecommandationPersonnalisee

**Cas d'utilisation**: CU12 - Comparer des ensembles (personnalisation)

**Jeu d'arguments**:

- `ensembleIds = ["ENS-00001", "ENS-00002"]`
- `preferences = { "priorite": "chargeMinimale", "maxCours": 3 }`
- Ensemble 1: 4 cours (charge lourde)
- Ensemble 2: 2 cours (charge légère)

**Retour attendu**:

- `Map<String, Object>` contenant:
  - `"ensembleRecommande" = "ENS-00002"` (respecte préférence chargeMinimale)
  - `"raison"` = explication de la recommandation
  - `"score"` = score calculé pour chaque ensemble

**Effets de bord**:

- Analyse selon les préférences utilisateur
- Algorithme de scoring pondéré par les préférences
- Résultat personnalisé (peut différer du test 12.2)

---

## 4. Tests de CourseService (CU#2 - Rechercher des cours dans un programme)

### Test 2.1 - testGetCoursesInProgramAvecCodeValide

**Cas d'utilisation**: CU2 - Rechercher des cours dans un programme

**Jeu d'arguments**:

- `programCode = "117510"` (code de programme valide à 6 chiffres)
- `mockClientApi.get(URI)` retourne un objet `Program` simulé contenant une liste de cours

**Retour attendu**:

- `List<Course>` non vide contenant:
  - Au moins un cours avec `id` non null
  - Chaque cours a `name`, `id`, et autres propriétés valides
  - Tous les cours appartiennent au programme spécifié

**Effets de bord**:

- Appel à `mockClientApi.get(URI)` effectué une fois avec:
  - Paramètre `programs_list = "117510"`
  - Paramètre `include_courses_detail = "true"`
  - Paramètre `response_level = "full"`
- Extraction des cours depuis l'objet `Program` effectuée
- Aucune exception lancée

---

### Test 2.2 - testGetCoursesInProgramEchoueSiCodeInvalide

**Cas d'utilisation**: CU2 - Rechercher des cours dans un programme (validation)

**Jeu d'arguments**:

- `programCode = "ABC123"` (code invalide, contient des lettres)
- OU `programCode = "12345"` (code invalide, moins de 6 chiffres)
- OU `programCode = "1234567"` (code invalide, plus de 6 chiffres)

**Retour attendu**:

- `List<Course>` vide (`Collections.emptyList()`)
- `result.size() = 0`
- `result.isEmpty() = true`

**Effets de bord**:

- **Aucun** appel à `mockClientApi.get()` (validation du format avant appel API)
- La méthode retourne immédiatement une liste vide
- Aucune exception lancée

---

### Test 2.3 - testGetCoursesInProgramRetourneListeVideSiProgrammeInexistant

**Cas d'utilisation**: CU2 - Rechercher des cours dans un programme (programme inexistant)

**Jeu d'arguments**:

- `programCode = "999999"` (code valide mais programme inexistant)
- `mockClientApi.get(URI)` retourne `null` ou un `Program` sans cours

**Retour attendu**:

- `List<Course>` vide (`Collections.emptyList()`)
- `result.isEmpty() = true`

**Effets de bord**:

- Appel à `mockClientApi.get(URI)` effectué une fois
- Si `Program` est null, retour immédiat d'une liste vide
- Si `Program` existe mais sans cours, extraction retourne liste vide
- Aucune exception lancée (gestion gracieuse)

---

### Test 2.4 - testGetCoursesInProgramGereErreurAPI

**Cas d'utilisation**: CU2 - Rechercher des cours dans un programme (erreur API)

**Jeu d'arguments**:

- `programCode = "117510"` (code valide)
- `mockClientApi.get(URI)` lance une `RuntimeException`

**Retour attendu**:

- `List<Course>` vide (`Collections.emptyList()`)
- L'exception est capturée et loggée (pas propagée)

**Effets de bord**:

- Appel à `mockClientApi.get(URI)` effectué une fois
- Exception capturée et gérée gracieusement
- Message d'erreur loggé (si logging configuré)
- Retour d'une liste vide plutôt qu'une exception

---

## 5. Tests de CourseService (CU#3 - Rechercher des cours dans un trimestre)

### Test 3.1 - testGetCoursesByTrimesterAvecTrimestreEtProgrammeValides

**Cas d'utilisation**: CU3 - Rechercher des cours dans un trimestre

**Jeu d'arguments**:

- `trimester = "H25"` (trimestre hiver 2025)
- `programCode = "117510"` (code de programme valide)
- Les cours retournés par `getCoursesInProgram()` ont `available_terms` avec `winter = true`

**Retour attendu**:

- `List<Course>` non vide contenant uniquement les cours:
  - Du programme spécifié (`programCode`)
  - Disponibles pour le trimestre spécifié (`available_terms.get("winter") = true`)
- Chaque cours a `available_terms` non null

**Effets de bord**:

- Appel à `getCoursesInProgram(programCode)` effectué
- Filtrage des cours selon `available_terms` effectué
- Conversion du code trimestre (`H25` → `winter`) effectuée
- Aucune exception lancée

---

### Test 3.2 - testGetCoursesByTrimesterSansProgramme

**Cas d'utilisation**: CU3 - Rechercher des cours dans un trimestre (tous les programmes)

**Jeu d'arguments**:

- `trimester = "A24"` (trimestre automne 2024)
- `programCode = null` (aucun programme spécifié)
- `mockClientApi.get(URI)` retourne tous les cours avec `response_level=full`

**Retour attendu**:

- `List<Course>` non vide contenant tous les cours:
  - Disponibles pour le trimestre spécifié (`available_terms.get("autumn") = true`)
- Filtrage effectué sur tous les cours disponibles

**Effets de bord**:

- Appel à `mockClientApi.get(URI)` avec `response_level=full` effectué
- **Aucun** appel à `getCoursesInProgram()` (programme non spécifié)
- Filtrage selon `available_terms` effectué sur tous les cours
- Conversion du code trimestre (`A24` → `autumn`) effectuée

---

### Test 3.3 - testGetCoursesByTrimesterEchoueSiCodeTrimestreInvalide

**Cas d'utilisation**: CU3 - Rechercher des cours dans un trimestre (validation)

**Jeu d'arguments**:

- `trimester = "INVALID"` (format invalide)
- OU `trimester = "X25"` (lettre invalide, pas H/A/E)
- `programCode = "117510"` (optionnel)

**Retour attendu**:

- `List<Course>` vide (`Collections.emptyList()`)
- `result.isEmpty() = true`

**Effets de bord**:

- **Aucun** appel à l'API (validation avant récupération)
- **Aucun** appel à `getCoursesInProgram()` (validation avant traitement)
- La méthode retourne immédiatement une liste vide
- Conversion échoue (`trimesterCodeToTerm()` retourne `null`)

---

### Test 3.4 - testGetCoursesByTrimesterFiltreCoursNonDisponibles

**Cas d'utilisation**: CU3 - Rechercher des cours dans un trimestre (filtrage)

**Jeu d'arguments**:

- `trimester = "E24"` (trimestre été 2024)
- `programCode = "117510"`
- Le programme contient 5 cours, mais seulement 2 ont `available_terms.get("summer") = true`

**Retour attendu**:

- `List<Course>` de taille 2
- Seuls les cours avec `available_terms.get("summer") = true` sont inclus
- Les cours avec `available_terms.get("summer") = false` ou `null` sont exclus

**Effets de bord**:

- Appel à `getCoursesInProgram(programCode)` effectué
- Filtrage selon `isAvailableForTerm(course, "summer")` effectué
- Conversion du code trimestre (`E24` → `summer`) effectuée
- Les cours non disponibles pour le trimestre sont exclus

---

### Test 3.5 - testGetCoursesByTrimesterGereErreurAPI

**Cas d'utilisation**: CU3 - Rechercher des cours dans un trimestre (erreur API)

**Jeu d'arguments**:

- `trimester = "H25"`
- `programCode = null`
- `mockClientApi.get(URI)` lance une `RuntimeException`

**Retour attendu**:

- `List<Course>` vide (`Collections.emptyList()`)
- L'exception est capturée et loggée (pas propagée)

**Effets de bord**:

- Appel à `mockClientApi.get(URI)` effectué
- Exception capturée dans le bloc try-catch
- Message d'erreur loggé
- Retour d'une liste vide plutôt qu'une exception

---

## 6. Tests de CourseService (CU#4 - Afficher l'horaire d'un cours)

### Test 4.1 - testGetCourseScheduleAvecParametresValides

**Cas d'utilisation**: CU4 - Afficher l'horaire d'un cours

**Jeu d'arguments**:

- `courseId = "IFT2255"`
- `semester = "A25"` (trimestre automne 2025)
- `mockClientApi.get(URI)` retourne une réponse JSON avec structure `schedules[0].sections[]`

**Retour attendu**:

- `CourseScheduleResponse` non null avec:
  - `courseId = "IFT2255"` (normalisé en majuscules)
  - `semester = "A25"` (normalisé en majuscules)
  - `sections` non vide contenant au moins une `SectionSchedule`
  - Chaque section contient des `ActivitySchedule` avec des `ScheduleEntry`
  - Chaque entrée contient: jour, heureDebut, heureFin, local

**Effets de bord**:

- Appel à `mockClientApi.get(URI)` avec:
  - Paramètre `include_schedule = "true"`
  - Paramètre `schedule_semester = "A25"`
- Parsing JSON effectué (`parseScheduleFromJSON()`)
- Conversion des jours (ex: "LU" → "Lundi") effectuée
- Structure hiérarchique construite (sections → activités → entrées)

---

### Test 4.2 - testGetCourseScheduleEchoueSiCoursInexistant

**Cas d'utilisation**: CU4 - Afficher l'horaire d'un cours (cours inexistant)

**Jeu d'arguments**:

- `courseId = "IFT9999"` (cours inexistant)
- `semester = "H25"`
- `mockClientApi.get(URI)` retourne un statut HTTP >= 300 ou null

**Retour attendu**:

- `CourseScheduleResponse` = `null`

**Effets de bord**:

- Appel à `mockClientApi.get(URI)` effectué
- Vérification du statut HTTP effectuée
- Si statut invalide, retour immédiat de `null`
- **Aucun** parsing JSON effectué

---

### Test 4.3 - testGetCourseScheduleEchoueSiParametresVides

**Cas d'utilisation**: CU4 - Afficher l'horaire d'un cours (validation)

**Jeu d'arguments**:

- `courseId = null` OU `courseId = ""` OU `courseId = "   "`
- OU `semester = null` OU `semester = ""` OU `semester = "   "`

**Retour attendu**:

- `CourseScheduleResponse` = `null`

**Effets de bord**:

- **Aucun** appel à `mockClientApi.get()` (validation avant appel)
- La méthode retourne immédiatement `null`
- Validation effectuée avec `isBlank()`

---

### Test 4.4 - testGetCourseScheduleRetourneStructureVideSiPasDeSections

**Cas d'utilisation**: CU4 - Afficher l'horaire d'un cours (pas d'horaire disponible)

**Jeu d'arguments**:

- `courseId = "IFT1015"`
- `semester = "E24"`
- `mockClientApi.get(URI)` retourne un JSON valide mais sans sections ou avec sections vides

**Retour attendu**:

- `CourseScheduleResponse` non null avec:
  - `courseId = "IFT1015"`
  - `semester = "E24"`
  - `sections` vide (`sections.isEmpty() = true`)

**Effets de bord**:

- Appel à `mockClientApi.get(URI)` effectué
- Parsing JSON effectué
- Si `schedules` est vide ou absent, `sections` reste vide
- L'objet est retourné même avec sections vides (pas `null`)

---

### Test 4.5 - testGetCourseScheduleParseStructureComplexe

**Cas d'utilisation**: CU4 - Afficher l'horaire d'un cours (structure complète)

**Jeu d'arguments**:

- `courseId = "IFT2255"`
- `semester = "A25"`
- JSON contient: sections avec volets, volets avec activités, activités avec jours/heures/local

**Retour attendu**:

- `CourseScheduleResponse` avec structure hiérarchique complète:
  - Section "A01" contenant:
    - Activité "Cours" avec entrées (ex: Lundi 09:00-12:00, local "PK-1234")
    - Activité "Laboratoire" avec entrées (ex: Mercredi 14:00-17:00, local "PK-5678")
  - Section "A02" contenant ses propres activités
- Tous les jours convertis (ex: "LU" → "Lundi", "MA" → "Mardi")

**Effets de bord**:

- Parsing récursif de la structure JSON effectué
- Conversion des abréviations de jours effectuée
- Construction de la hiérarchie: SectionSchedule → ActivitySchedule → ScheduleEntry
- Seules les sections/activités avec entrées valides sont incluses

---

### Test 4.6 - testGetCourseScheduleGereErreurParsingJSON

**Cas d'utilisation**: CU4 - Afficher l'horaire d'un cours (erreur parsing)

**Jeu d'arguments**:

- `courseId = "IFT2255"`
- `semester = "H25"`
- `mockClientApi.get(URI)` retourne un JSON malformé ou invalide

**Retour attendu**:

- `CourseScheduleResponse` non null mais avec `sections` vide
- OU `CourseScheduleResponse` = `null` si erreur critique

**Effets de bord**:

- Appel à `mockClientApi.get(URI)` effectué
- Tentative de parsing JSON effectuée
- Exception capturée dans `parseScheduleFromJSON()`
- Message d'erreur loggé
- Retour d'un objet avec sections vides ou `null` selon la gravité

---

## Résumé des conventions de test

| Aspect     | Convention                                      |
| ---------- | ----------------------------------------------- |
| Framework  | JUnit 5 + Mockito                               |
| Pattern    | AAA (Arrange, Act, Assert)                      |
| Isolation  | Mocks pour toutes les dépendances               |
| Nommage    | `test<Fonctionnalité>` ou description explicite |
| Exceptions | `assertThrows()` pour les cas d'erreur          |
| Messages   | En français, explicites                         |

## Mapping Cas d'utilisation ↔ Tests

| CU    | Fonctionnalité                         | Tests                        |
| ----- | -------------------------------------- | ---------------------------- |
| CU#2  | Rechercher des cours dans un programme | 2.1, 2.2, 2.3, 2.4           |
| CU#3  | Rechercher des cours dans un trimestre | 3.1, 3.2, 3.3, 3.4, 3.5      |
| CU#4  | Afficher l'horaire d'un cours          | 4.1, 4.2, 4.3, 4.4, 4.5, 4.6 |
| CU#10 | Créer un ensemble de cours             | 10.1, 10.2, 10.3, 10.4, 10.5 |
| CU#11 | Détecter les conflits d'horaire        | 11.1, 11.2, 11.3, 11.4, 11.5 |
| CU#12 | Comparer des ensembles de cours        | 12.1, 12.2, 12.3, 12.4, 12.5 |
