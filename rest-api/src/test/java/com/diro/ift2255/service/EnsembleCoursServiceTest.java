package com.diro.ift2255.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.diro.ift2255.model.EnsembleCours;
import com.diro.ift2255.model.Horaire;
import com.diro.ift2255.repository.EnsembleCoursRepository;
import com.diro.ift2255.util.HttpClientApi;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Tests unitaires pour {@link EnsembleCoursService} (CU#10 - Créer un ensemble de cours).
 * 
 * <p>Cette classe contient 5 cas de tests distincts utilisant JUnit 5 et Mockito
 * pour valider le comportement du service de gestion des ensembles de cours.</p>
 * 
 * <h2>Cas de tests couverts :</h2>
 * <ul>
 *   <li>Test 10.1 : Création d'un ensemble avec paramètres valides</li>
 *   <li>Test 10.2 : Échec si plus de 6 cours sont fournis</li>
 *   <li>Test 10.3 : Échec avec un format de trimestre invalide</li>
 *   <li>Test 10.4 : Ajout d'un cours à un ensemble existant</li>
 *   <li>Test 10.5 : Récupération de l'horaire combiné</li>
 * </ul>
 * 
 * @author Équipe 10
 * @version 3.0
 * @since 2025-12-27
 * @see EnsembleCoursService
 * @see EnsembleCours
 */
@ExtendWith(MockitoExtension.class)
public class EnsembleCoursServiceTest {
    
    /**
     * Mock du repository des ensembles.
     */
    @Mock
    private EnsembleCoursRepository mockRepository;
    
    /**
     * Mock du client HTTP pour l'API Planifium.
     */
    @Mock
    private HttpClientApi mockHttpClient;
    
    /**
     * Instance du service à tester.
     */
    private EnsembleCoursService ensembleService;
    
    /**
     * Timestamp de début du test pour mesurer la durée.
     */
    private long testStartTime;

    /**
     * Affiche l'en-tête de la suite de tests.
     */
    @BeforeAll
    static void printHeader() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("EnsembleCoursService Tests (CU#10 - Créer un ensemble de cours)");
        System.out.println("=".repeat(80));
    }

    /**
     * Configuration avant chaque test.
     * Initialise le service et affiche les informations du test.
     * 
     * @param testInfo Informations sur le test en cours
     */
    @BeforeEach
    void setup(TestInfo testInfo) {
        ensembleService = new EnsembleCoursService(mockRepository, mockHttpClient);
        testStartTime = System.currentTimeMillis();

        System.out.println("\nTEST: " + testInfo.getDisplayName());
        System.out.println("    ├─ Method: " + testInfo.getTestMethod().get().getName());
        System.out.println("    ├─ Assertions:");
    }

    /**
     * Nettoyage après chaque test.
     * Affiche la durée d'exécution.
     * 
     * @param testInfo Informations sur le test terminé
     */
    @AfterEach
    void tearDown(TestInfo testInfo) {
        long duration = System.currentTimeMillis() - testStartTime;
        System.out.println("    └─ Duration: " + duration + " ms");
    }

    /**
     * Test 10.1 : Vérifie la création d'un ensemble avec des paramètres valides.
     * 
     * <p><b>Jeu d'arguments :</b></p>
     * <ul>
     *   <li>nom = "Mon ensemble automne"</li>
     *   <li>trimestre = "A25"</li>
     *   <li>coursIds = ["IFT1015", "IFT2255"]</li>
     * </ul>
     * 
     * <p><b>Retour attendu :</b> EnsembleCours non null avec ID généré</p>
     * <p><b>Effets de bord :</b> repository.save() appelé une fois</p>
     */
    @Test
    @DisplayName("Test 10.1 - Devrait créer un ensemble de cours avec des paramètres valides")
    void testCreerEnsembleAvecParametresValides() {
        // ARRANGE
        String nom = "Mon ensemble automne";
        String trimestre = "A25";
        List<String> coursIds = Arrays.asList("IFT1015", "IFT2255");
        
        when(mockRepository.save(any(EnsembleCours.class))).thenAnswer(invocation -> {
            EnsembleCours e = invocation.getArgument(0);
            e.setId("ENS-00001");
            return e;
        });
        
        when(mockHttpClient.get(any(URI.class), eq(JsonNode.class))).thenReturn(null);
        
        // ACT
        EnsembleCours result = ensembleService.creerEnsemble(nom, trimestre, coursIds);
        
        // ASSERT
        try {
            assertNotNull(result, "L'ensemble créé ne devrait pas être null");
            OK("Ensemble créé non null", false);
            
            assertEquals(nom, result.getNom(), "Le nom devrait correspondre");
            OK("Nom correct: " + nom, false);
            
            assertEquals("A25", result.getTrimestre(), "Le trimestre devrait être normalisé en majuscules");
            OK("Trimestre normalisé: A25", false);
            
            assertEquals(2, result.getCoursIds().size(), "Devrait contenir 2 cours");
            OK("Nombre de cours correct: 2", false);
            
            verify(mockRepository, times(1)).save(any(EnsembleCours.class));
            OK("Repository.save() appelé une fois");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    /**
     * Test 10.2 : Vérifie l'échec lors de la création avec plus de 6 cours.
     * 
     * <p><b>Jeu d'arguments :</b> 7 cours fournis</p>
     * <p><b>Retour attendu :</b> IllegalArgumentException</p>
     * <p><b>Effets de bord :</b> repository.save() jamais appelé</p>
     */
    @Test
    @DisplayName("Test 10.2 - Devrait échouer si plus de 6 cours sont fournis")
    void testCreerEnsembleEchoueSiPlusDe6Cours() {
        // ARRANGE
        String nom = "Ensemble trop chargé";
        String trimestre = "A25";
        List<String> coursIds = Arrays.asList(
            "IFT1015", "IFT1025", "IFT2015", "IFT2255", 
            "IFT3065", "IFT3150", "IFT3395"
        );
        
        // ACT & ASSERT
        try {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ensembleService.creerEnsemble(nom, trimestre, coursIds),
                "Devrait lancer une exception pour trop de cours"
            );
            OK("Exception lancée pour > 6 cours", false);
            
            assertTrue(exception.getMessage().contains("6"), 
                "Le message devrait mentionner la limite de 6 cours");
            OK("Message d'erreur mentionne la limite", false);
            
            verify(mockRepository, never()).save(any());
            OK("Repository.save() jamais appelé");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    /**
     * Test 10.3 : Vérifie l'échec avec un format de trimestre invalide.
     * 
     * <p><b>Jeu d'arguments :</b> trimestre = "X99" (invalide)</p>
     * <p><b>Retour attendu :</b> IllegalArgumentException mentionnant le trimestre</p>
     */
    @Test
    @DisplayName("Test 10.3 - Devrait échouer avec un format de trimestre invalide")
    void testCreerEnsembleEchoueSiTrimestreInvalide() {
        // ARRANGE
        String nom = "Test trimestre";
        String trimestreInvalide = "X99";
        List<String> coursIds = Arrays.asList("IFT1015");
        
        // ACT & ASSERT
        try {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ensembleService.creerEnsemble(nom, trimestreInvalide, coursIds),
                "Devrait lancer une exception pour trimestre invalide"
            );
            OK("Exception lancée pour trimestre invalide", false);
            
            assertTrue(exception.getMessage().toLowerCase().contains("trimestre") || 
                       exception.getMessage().toLowerCase().contains("format"),
                "Le message devrait mentionner le problème de format");
            OK("Message d'erreur pertinent");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    /**
     * Test 10.4 : Vérifie l'ajout d'un cours à un ensemble existant.
     * 
     * <p><b>Jeu d'arguments :</b></p>
     * <ul>
     *   <li>ensembleId = "ENS-00001" (existant avec 2 cours)</li>
     *   <li>nouveauCours = "IFT2015"</li>
     * </ul>
     * 
     * <p><b>Retour attendu :</b> Ensemble avec 3 cours</p>
     * <p><b>Effets de bord :</b> repository.update() appelé</p>
     */
    @Test
    @DisplayName("Test 10.4 - Devrait ajouter un cours à un ensemble existant")
    void testAjouterCoursAEnsembleExistant() {
        // ARRANGE
        String ensembleId = "ENS-00001";
        String nouveauCours = "IFT2015";
        
        EnsembleCours existant = new EnsembleCours(ensembleId, "Test", "A25");
        existant.setCoursIds(new ArrayList<>(Arrays.asList("IFT1015", "IFT2255")));
        existant.setHoraires(new HashMap<>());
        
        when(mockRepository.findById(ensembleId)).thenReturn(Optional.of(existant));
        when(mockRepository.update(any(EnsembleCours.class))).thenAnswer(i -> i.getArgument(0));
        when(mockHttpClient.get(any(URI.class), eq(JsonNode.class))).thenReturn(null);
        
        // ACT
        EnsembleCours result = ensembleService.ajouterCours(ensembleId, nouveauCours);
        
        // ASSERT
        try {
            assertNotNull(result, "Résultat ne devrait pas être null");
            OK("Résultat non null", false);
            
            assertEquals(3, result.getCoursIds().size(), "Devrait contenir 3 cours après ajout");
            OK("Nombre de cours après ajout: 3", false);
            
            assertTrue(result.getCoursIds().contains("IFT2015"), 
                "Le nouveau cours devrait être présent");
            OK("Nouveau cours ajouté: IFT2015", false);
            
            verify(mockRepository, times(1)).update(any(EnsembleCours.class));
            OK("Repository.update() appelé");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    /**
     * Test 10.5 : Vérifie la récupération de l'horaire combiné.
     * 
     * <p><b>Jeu d'arguments :</b> ensembleId = "ENS-00001" avec 2 cours</p>
     * <p><b>Retour attendu :</b> Map avec horaires pour 2 cours</p>
     * <p><b>Effets de bord :</b> repository.findById() appelé</p>
     */
    @Test
    @DisplayName("Test 10.5 - Devrait récupérer l'horaire combiné de tous les cours d'un ensemble")
    void testGetHoraireCombineRetourneHoraires() {
        // ARRANGE
        String ensembleId = "ENS-00001";
        
        EnsembleCours ensemble = new EnsembleCours(ensembleId, "Test horaires", "A25");
        ensemble.setCoursIds(Arrays.asList("IFT1015", "IFT2255"));
        
        Map<String, List<Horaire>> horaires = new HashMap<>();
        horaires.put("IFT1015", Arrays.asList(
            new Horaire("IFT1015", DayOfWeek.MONDAY, 
                LocalTime.of(10, 30), LocalTime.of(12, 30))
        ));
        horaires.put("IFT2255", Arrays.asList(
            new Horaire("IFT2255", DayOfWeek.TUESDAY, 
                LocalTime.of(14, 30), LocalTime.of(16, 30))
        ));
        ensemble.setHoraires(horaires);
        
        when(mockRepository.findById(ensembleId)).thenReturn(Optional.of(ensemble));
        
        // ACT
        Map<String, List<Horaire>> result = ensembleService.getHoraireCombine(ensembleId);
        
        // ASSERT
        try {
            assertNotNull(result, "Résultat ne devrait pas être null");
            OK("Horaires récupérés non null", false);
            
            assertEquals(2, result.size(), "Devrait avoir des horaires pour 2 cours");
            OK("Horaires pour 2 cours", false);
            
            assertTrue(result.containsKey("IFT1015"), "Devrait contenir IFT1015");
            OK("Contient IFT1015", false);
            
            assertTrue(result.containsKey("IFT2255"), "Devrait contenir IFT2255");
            OK("Contient IFT2255", false);
            
            verify(mockRepository, times(1)).findById(ensembleId);
            OK("Repository.findById() appelé");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    /**
     * Affiche le pied de page de la suite de tests.
     */
    @AfterAll
    static void printFooter() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("COMPLETED: EnsembleCoursService Tests (CU#10)");
        System.out.println("=".repeat(80) + "\n");
    }

    /**
     * Affiche un message de résultat formaté.
     * 
     * @param message Le message à afficher
     * @param isOk Indique si le test a réussi
     * @param isLast Indique si c'est le dernier message
     */
    private void printMessage(String message, boolean isOk, boolean isLast) {
        String symbol = isLast ? "└─" : "├─";
        String status = isOk ? "[PASS]" : "[FAIL]";
        System.out.println("    │   " + symbol + " " + status + " " + message);
    }

    /**
     * Affiche un message de succès (dernier de la liste).
     * 
     * @param message Le message de succès
     */
    private void OK(String message) {
        printMessage(message, true, true);
    }

    /**
     * Affiche un message de succès.
     * 
     * @param message Le message de succès
     * @param isLast Indique si c'est le dernier message
     */
    private void OK(String message, boolean isLast) {
        printMessage(message, true, isLast);
    }

    /**
     * Affiche un message d'erreur.
     * 
     * @param message Le message d'erreur
     */
    private void Err(String message) {
        printMessage(message, false, true);
    }
}
