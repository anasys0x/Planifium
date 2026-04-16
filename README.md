# Planifium UdeM

API REST de gestion de cours universitaires pour le cours **IFT2255 -- Genie logiciel** a l'Universite de Montreal.

L'application est un backend Javalin qui enrichit les donnees de l'**API Planifium** avec des avis etudiants (persistes en XML), un bot Discord pour soumettre des avis, la detection de conflits d'horaire, et un frontend web statique.

## Fonctionnalites

- **Recherche de cours** -- par sigle (`IFT2255`), mot-cle, code programme (`117510`) ou programme + trimestre (`117510 H25`)
- **Consultation d'horaires** -- grille synthetique par trimestre pour chaque cours
- **Verification d'eligibilite** -- validation des prerequis et du cycle
- **Resultats academiques** -- moyennes, scores, historique par trimestre
- **Avis etudiants** -- consultation et soumission (via API ou bot Discord)
- **Ensembles de cours (CU#10)** -- creation d'ensembles de max 6 cours par trimestre
- **Detection de conflits (CU#11)** -- detection automatique des chevauchements d'horaire
- **Comparaison d'ensembles (CU#12)** -- comparaison cote a cote avec scoring et recommandation

## Prerequis

- **Java 17** (JDK)
- **Maven 3.8+**
- Connexion internet (l'API Planifium est hebergee sur Render)

## Demarrage rapide

```bash
# Cloner le depot
git clone https://github.com/<votre-utilisateur>/Devoir-2.git
cd Devoir-2/rest-api

# Compiler
mvn clean compile

# Lancer les tests
mvn test

# Packager le uber-jar
mvn clean package

# Demarrer le serveur (port 8070)
java -jar target/rest-api-1.0-SNAPSHOT.jar
```

Le frontend est accessible a **http://localhost:8070**.

## Commandes utiles

| Commande | Description |
|---|---|
| `mvn clean compile` | Compilation |
| `mvn test` | Tests (JUnit 5 + Mockito) |
| `mvn test -Dtest=CourseServiceTest` | Un seul fichier de test |
| `mvn test -Dtest=CourseServiceTest#testMethodName` | Une seule methode de test |
| `mvn clean package` | Uber-jar (maven-shade-plugin) |
| `mvn javadoc:javadoc` | Generation de la JavaDoc |

## Architecture

Architecture MVC sous `com.diro.ift2255` :

```
rest-api/
├── src/main/java/com/diro/ift2255/
│   ├── Main.java                    # Point d'entree (Javalin + Discord bot)
│   ├── config/Routes.java           # Registration centralisee des endpoints
│   ├── controller/                  # Handlers HTTP
│   ├── service/                     # Logique metier
│   ├── repository/                  # Acces aux donnees (XML, CSV, in-memory)
│   ├── model/                       # Objets du domaine
│   │   └── dto/                     # DTOs requete/reponse
│   ├── util/                        # HttpClient, validation, reponses
│   └── discord/ReviewBot.java       # Bot Discord (JDA 5)
│
├── src/main/resources/
│   ├── data/                        # reviews.xml, historique CSV
│   └── public/                      # Frontend statique (HTML/CSS/JS)
│
├── src/test/                        # Tests unitaires et integration
└── pom.xml                          # Configuration Maven
```

### Flux de donnees

1. Le frontend (`resources/public/`) envoie des requetes aux endpoints Javalin sur `:8070`
2. Les controllers delegent aux services
3. `CourseService` proxy vers l'API Planifium et enrichit les resultats localement
4. `ReviewService` persiste les avis dans `data/reviews.xml`
5. Le bot Discord alimente les avis via le meme `ReviewService`

## Endpoints principaux

| Methode | Route | Description |
|---|---|---|
| `GET` | `/courses/{id}` | Detail d'un cours par sigle |
| `GET` | `/courses/search?q=...` | Recherche de cours par mot-cle |
| `GET` | `/courses/compare?id=...&id=...` | Comparaison de deux cours |
| `GET` | `/courses/{id}/schedule?semester=...` | Horaire d'un cours par trimestre |
| `POST` | `/courses/{id}/eligibility` | Verification d'eligibilite |
| `GET` | `/courses/{id}/results` | Resultats academiques |
| `GET` | `/programs/search?q=...` | Recherche de programmes |
| `GET` | `/programs/courses?program=...` | Cours d'un programme |
| `GET` | `/semesters/{code}/courses?program=...` | Cours par trimestre et programme |
| `GET` | `/reviews/{courseId}` | Avis pour un cours |
| `POST` | `/reviews` | Soumettre un avis |

## Codes trimestre

| Code | Trimestre |
|---|---|
| `H25` | Hiver 2025 |
| `A25` | Automne 2025 |
| `E25` | Ete 2025 |

Format valide : `^[AHE]\d{2}$`

## Bot Discord (optionnel)

Le bot Discord permet de soumettre des avis via la commande `!review` dans un canal dedie. Il est **optionnel** -- le serveur demarre normalement sans lui.

Pour l'activer, vous devez **remplacer les valeurs ci-dessous par vos propres identifiants Discord** (token de votre bot et ID du canal cible), puis definir ces variables d'environnement avant de lancer le serveur :

```bash
# Linux / macOS
export DISCORD_BOT_TOKEN="REMPLACER_PAR_VOTRE_TOKEN_DISCORD"
export DISCORD_CHANNEL_ID="REMPLACER_PAR_VOTRE_CHANNEL_ID"
java -jar target/rest-api-1.0-SNAPSHOT.jar

# Windows (cmd)
set DISCORD_BOT_TOKEN=REMPLACER_PAR_VOTRE_TOKEN_DISCORD
set DISCORD_CHANNEL_ID=REMPLACER_PAR_VOTRE_CHANNEL_ID
java -jar target/rest-api-1.0-SNAPSHOT.jar

# Windows (PowerShell)
$env:DISCORD_BOT_TOKEN="REMPLACER_PAR_VOTRE_TOKEN_DISCORD"
$env:DISCORD_CHANNEL_ID="REMPLACER_PAR_VOTRE_CHANNEL_ID"
java -jar target/rest-api-1.0-SNAPSHOT.jar
```

> **Note** : Le token du bot et l'ID du canal ne sont pas inclus dans le depot pour des raisons de securite. Si vous etes correcteur, ces valeurs vous seront communiquees separement.

## Dependances externes

- **API Planifium** (`https://planifium-api.onrender.com/api/v1/`) -- donnees de cours, horaires, programmes
- **Ollama** (optionnel) -- LLM local (`mistral:7b` sur `localhost:11434`) pour le parsing d'avis en langage naturel via Discord
- **Discord / JDA 5** (optionnel) -- bot pour la soumission d'avis, active via variables d'environnement

## CI/CD

Le pipeline GitHub Actions (`.github/workflows/ci-cd.yml`) execute sur chaque push/PR vers `main` :

1. `mvn clean compile`
2. `mvn test`
3. `mvn javadoc:javadoc`

## Rapport

Le dossier `rapport/` contient un site MkDocs Material :

```bash
cd rapport
pip install -r requirements.txt
mkdocs serve
```

## Equipe

Projet realise dans le cadre du cours IFT2255 -- Genie logiciel, Universite de Montreal.
