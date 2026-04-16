# Instructions pour l'exécution des tests

## Prérequis

- **Java**: JDK 17 ou supérieur
- **Maven**: Version 3.8.x ou supérieur
- **Dépendances**: JUnit 5, Mockito 5.x (automatiquement gérées par Maven)

## Structure des tests

Les tests unitaires pour les cas d'utilisation #10, #11 et #12 sont organisés comme suit:

```
rest-api/src/test/java/com/diro/ift2255/
├── service/
│   ├── EnsembleCoursServiceTest.java      # CU#10 - Créer un ensemble de cours
│   ├── ConflitHoraireServiceTest.java     # CU#11 - Détecter les conflits d'horaire
│   └── ComparerEnsemblesServiceTest.java  # CU#12 - Comparer des ensembles de cours
```

## Exécution des tests

### Exécuter tous les tests

```bash
# Depuis le répertoire rest-api/
mvn test
```

### Exécuter les tests d'un CU spécifique

```bash
# CU#10 - Créer un ensemble de cours
mvn test -Dtest=EnsembleCoursServiceTest

# CU#11 - Détecter les conflits d'horaire
mvn test -Dtest=ConflitHoraireServiceTest

# CU#12 - Comparer des ensembles de cours
mvn test -Dtest=ComparerEnsemblesServiceTest
```

### Exécuter un test individuel

```bash
# Exemple: Exécuter uniquement le test 10.1
mvn test -Dtest=EnsembleCoursServiceTest#testCreerEnsembleAvecParametresValides

# Exemple: Exécuter uniquement le test 11.2
mvn test -Dtest=ConflitHoraireServiceTest#testDetecterConflitPartielChevauchement
```

### Exécuter les tests avec rapport détaillé

```bash
mvn test -Dsurefire.useFile=false
```

## Liste des tests par fonctionnalité

### CU#10 - Créer un ensemble de cours (5 tests)

| ID Test | Nom de la méthode | Description |
|---------|-------------------|-------------|
| 10.1 | `testCreerEnsembleAvecParametresValides` | Création d'un ensemble avec nom, trimestre et liste de cours valides |
| 10.2 | `testCreerEnsembleEchoueSiPlusDe6Cours` | Échec si plus de 6 cours sont fournis |
| 10.3 | `testCreerEnsembleEchoueSiTrimestreInvalide` | Échec si format de trimestre invalide |
| 10.4 | `testAjouterCoursAEnsembleExistant` | Ajout d'un cours à un ensemble existant |
| 10.5 | `testGetHoraireCombineRetourneHoraires` | Récupération de l'horaire combiné d'un ensemble |

### CU#11 - Détecter les conflits d'horaire (5 tests)

| ID Test | Nom de la méthode | Description |
|---------|-------------------|-------------|
| 11.1 | `testDetecterConflitTotalHorairesIdentiques` | Détection d'un conflit total (horaires identiques) |
| 11.2 | `testDetecterConflitPartielChevauchement` | Détection d'un conflit partiel (chevauchement) |
| 11.3 | `testAucunConflitHorairesSansChevauchement` | Aucun conflit pour horaires sur jours différents |
| 11.4 | `testGetResumeConflits` | Génération d'un résumé des conflits |
| 11.5 | `testDetecterConflitsEnsembleInexistant` | Exception pour ensemble inexistant |

### CU#12 - Comparer des ensembles de cours (5 tests)

| ID Test | Nom de la méthode | Description |
|---------|-------------------|-------------|
| 12.1 | `testComparerDeuxEnsemblesRetourneMetriques` | Comparaison retourne les métriques correctes |
| 12.2 | `testIdentifierMeilleurEnsemble` | Identification du meilleur ensemble |
| 12.3 | `testGenererTableauComparatif` | Génération d'un tableau comparatif |
| 12.4 | `testComparerEnsembleInexistant` | Exception si un ensemble n'existe pas |
| 12.5 | `testRecommandationPersonnalisee` | Recommandation selon préférences utilisateur |

## Configuration Maven requise

Ajoutez les dépendances suivantes dans le `pom.xml`:

```xml
<dependencies>
    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.10.0</version>
        <scope>test</scope>
    </dependency>
    
    <!-- Mockito -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <version>5.5.0</version>
        <scope>test</scope>
    </dependency>
    
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-junit-jupiter</artifactId>
        <version>5.5.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.1.2</version>
        </plugin>
    </plugins>
</build>
```

## Sortie attendue

Lors de l'exécution des tests, vous devriez voir une sortie similaire à:

```
================================================================================
EnsembleCoursService Tests (CU#10 - Créer un ensemble de cours)
================================================================================

TEST: Test 10.1 - Devrait créer un ensemble avec des paramètres valides
    ├─ Method: testCreerEnsembleAvecParametresValides
    ├─ Assertions:
    │   ├─ [PASS] Ensemble non null
    │   ├─ [PASS] Nom de l'ensemble correct
    │   ├─ [PASS] Trimestre correct
    │   └─ [PASS] Nombre de cours = 3
    └─ Duration: 45 ms

[... autres tests ...]

================================================================================
COMPLETED: EnsembleCoursService Tests (CU#10)
================================================================================
```

## Résolution des problèmes courants

### Erreur: "No tests were executed"
- Vérifiez que le plugin maven-surefire est correctement configuré
- Assurez-vous que les fichiers de test se terminent par `Test.java`

### Erreur: "Class not found"
- Exécutez `mvn clean compile` avant les tests
- Vérifiez que les packages correspondent à la structure de répertoires

### Tests qui échouent avec NullPointerException
- Vérifiez que les mocks sont correctement initialisés avec `@ExtendWith(MockitoExtension.class)`
- Assurez-vous que tous les `when()` nécessaires sont configurés
