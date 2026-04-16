package com.diro.ift2255.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.diro.ift2255.model.ComparaisonEnsembles;
import com.diro.ift2255.model.EnsembleCours;
import com.diro.ift2255.repository.ComparerEnsemblesRepository;
import com.diro.ift2255.repository.EnsembleCoursRepository;

/**
 * Tests unitaires pour {@link ComparerEnsemblesService} (CU#12 - Comparer des ensembles de cours).
 * 
 * <p>Cette classe contient 5 cas de tests distincts utilisant JUnit 5 et Mockito
 * pour valider le comportement du service de comparaison d'ensembles.</p>
 * 
 * <h2>Cas de tests couverts :</h2>
 * <ul>
 *   <li>Test 12.1 : Comparaison de deux ensembles avec métriques</li>
 *   <li>Test 12.2 : Identification du meilleur ensemble</li>
 *   <li>Test 12.3 : Génération d'un tableau comparatif</li>
 *   <li>Test 12.4 : Exception pour ensemble inexistant</li>
 *   <li>Test 12.5 : Recommandation personnalisée selon préférences</li>
 * </ul>
 * 
 * @author Équipe 10
 * @version 3.0
 * @since 2025-12-27
 * @see ComparerEnsemblesService
 * @see ComparaisonEnsembles
 */
@ExtendWith(MockitoExtension.class)
public class ComparerEnsemblesServiceTest {
    
    /**
     * Mock du repository des comparaisons.
     */
    @Mock
    private ComparerEnsemblesRepository mockComparerRepository;
    
    /**
     * Mock du repository des ensembles.
     */
    @Mock
    private EnsembleCoursRepository mockEnsembleRepository;
    
    /**
     * Mock du service de conflits.
     */
    @Mock
    private ConflitHoraireService mockConflitService;
    
    /**
     * Instance du service à tester.
     */
    private ComparerEnsemblesService comparerService;
    
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
        System.out.println("ComparerEnsemblesService Tests (CU#12 - Comparer des ensembles de cours)");
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
        comparerService = new ComparerEnsemblesService(
                mockComparerRepository, mockEnsembleRepository, mockConflitService);
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
     * Test 12.1 : Vérifie la comparaison de deux ensembles avec métriques.
     * 
     * <p><b>Jeu d'arguments :</b></p>
     * <ul>
     *   <li>Ensemble 1 : 2 cours, 0 conflit</li>
     *   <li>Ensemble 2 : 3 cours, 1 conflit</li>
     * </ul>
     * 
     * <p><b>Retour attendu :</b> ComparaisonEnsembles avec crédits, conflits, scores</p>
     * <p><b>Effets de bord :</b> Comparaison sauvegardée dans le repository</p>
     */
    @Test
    @DisplayName("Test 12.1 - Devrait comparer deux ensembles et retourner les métriques")
    void testComparerDeuxEnsemblesRetourneMetriques() {
        // ARRANGE
        String id1 = "ENS-00001";
        String id2 = "ENS-00002";
        
        EnsembleCours ensemble1 = creerEnsemble(id1, "Ensemble A", 2);
        EnsembleCours ensemble2 = creerEnsemble(id2, "Ensemble B", 3);
        
        when(mockComparerRepository.findByEnsembleIds(id1, id2)).thenReturn(Optional.empty());
        when(mockEnsembleRepository.findById(id1)).thenReturn(Optional.of(ensemble1));
        when(mockEnsembleRepository.findById(id2)).thenReturn(Optional.of(ensemble2));
        when(mockConflitService.detecterConflits(id1)).thenReturn(new ArrayList<>());
        when(mockConflitService.detecterConflits(id2)).thenReturn(new ArrayList<>());
        when(mockComparerRepository.save(any(ComparaisonEnsembles.class)))
                .thenAnswer(i -> i.getArgument(0));
        
        // ACT
        ComparaisonEnsembles result = comparerService.comparerEnsembles(id1, id2);
        
        // ASSERT
        try {
            assertNotNull(result, "Résultat ne devrait pas être null");
            OK("Comparaison retournée non null", false);
            
            assertEquals(6, result.getCreditsEnsemble1(), "Crédits ensemble 1 = 6");
            OK("Crédits ensemble 1: 6", false);
            
            assertEquals(9, result.getCreditsEnsemble2(), "Crédits ensemble 2 = 9");
            OK("Crédits ensemble 2: 9", false);
            
            assertNotNull(result.getMeilleurEnsembleId(), "Meilleur ensemble devrait être identifié");
            OK("Meilleur ensemble identifié");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    /**
     * Test 12.2 : Vérifie l'identification du meilleur ensemble.
     * 
     * <p><b>Jeu d'arguments :</b></p>
     * <ul>
     *   <li>Ensemble 1 : 4 cours, 0 conflit (score plus élevé)</li>
     *   <li>Ensemble 2 : 2 cours, 2 conflits (score plus bas)</li>
     * </ul>
     * 
     * <p><b>Retour attendu :</b> meilleurEnsembleId = id1</p>
     */
    @Test
    @DisplayName("Test 12.2 - Devrait identifier le meilleur ensemble basé sur les critères")
    void testIdentifierMeilleurEnsemble() {
        // ARRANGE
        String id1 = "ENS-00001";
        String id2 = "ENS-00002";
        
        EnsembleCours ensemble1 = creerEnsemble(id1, "Meilleur", 4);
        EnsembleCours ensemble2 = creerEnsemble(id2, "Moins bon", 2);
        
        when(mockComparerRepository.findByEnsembleIds(id1, id2)).thenReturn(Optional.empty());
        when(mockEnsembleRepository.findById(id1)).thenReturn(Optional.of(ensemble1));
        when(mockEnsembleRepository.findById(id2)).thenReturn(Optional.of(ensemble2));
        when(mockConflitService.detecterConflits(id1)).thenReturn(new ArrayList<>());
        when(mockConflitService.detecterConflits(id2)).thenReturn(new ArrayList<>());
        when(mockComparerRepository.save(any(ComparaisonEnsembles.class)))
                .thenAnswer(i -> i.getArgument(0));
        
        // ACT
        ComparaisonEnsembles result = comparerService.comparerEnsembles(id1, id2);
        
        // ASSERT
        try {
            assertEquals(id1, result.getMeilleurEnsembleId(), 
                "L'ensemble 1 devrait être le meilleur");
            OK("Meilleur ensemble: " + id1, false);
            
            assertTrue(result.getScoreEnsemble1() > result.getScoreEnsemble2(),
                "Le score de l'ensemble 1 devrait être supérieur");
            OK("Score ensemble1 > Score ensemble2");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    /**
     * Test 12.3 : Vérifie la génération du tableau comparatif.
     * 
     * <p><b>Jeu d'arguments :</b> 2 ensembles à comparer</p>
     * <p><b>Retour attendu :</b> Map avec critères, valeurs par ensemble, différences</p>
     */
    @Test
    @DisplayName("Test 12.3 - Devrait générer un tableau comparatif avec les différences")
    void testGetTableauComparatif() {
        // ARRANGE
        String id1 = "ENS-00001";
        String id2 = "ENS-00002";
        
        EnsembleCours ensemble1 = creerEnsemble(id1, "Ensemble A", 3);
        EnsembleCours ensemble2 = creerEnsemble(id2, "Ensemble B", 4);
        
        when(mockEnsembleRepository.findById(id1)).thenReturn(Optional.of(ensemble1));
        when(mockEnsembleRepository.findById(id2)).thenReturn(Optional.of(ensemble2));
        when(mockConflitService.detecterConflits(anyString())).thenReturn(new ArrayList<>());
        
        // ACT
        Map<String, Object> tableau = comparerService.getTableauComparatif(id1, id2);
        
        // ASSERT
        try {
            assertNotNull(tableau, "Tableau ne devrait pas être null");
            OK("Tableau comparatif généré", false);
            
            assertTrue(tableau.containsKey("criteres"), "Devrait contenir 'criteres'");
            OK("Contient clé 'criteres'", false);
            
            assertTrue(tableau.containsKey("ensemble1"), "Devrait contenir 'ensemble1'");
            OK("Contient clé 'ensemble1'", false);
            
            assertTrue(tableau.containsKey("ensemble2"), "Devrait contenir 'ensemble2'");
            OK("Contient clé 'ensemble2'", false);
            
            assertTrue(tableau.containsKey("differences"), "Devrait contenir 'differences'");
            OK("Contient clé 'differences'");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    /**
     * Test 12.4 : Vérifie qu'une exception est lancée pour un ensemble inexistant.
     * 
     * <p><b>Jeu d'arguments :</b> ensembleId1 = "ENS-INEXISTANT"</p>
     * <p><b>Retour attendu :</b> IllegalArgumentException</p>
     */
    @Test
    @DisplayName("Test 12.4 - Devrait lancer une exception si un ensemble n'existe pas")
    void testExceptionPourEnsembleInexistant() {
        // ARRANGE
        String id1 = "ENS-INEXISTANT";
        String id2 = "ENS-00002";
        
        when(mockComparerRepository.findByEnsembleIds(id1, id2)).thenReturn(Optional.empty());
        when(mockEnsembleRepository.findById(id1)).thenReturn(Optional.empty());
        
        // ACT & ASSERT
        try {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> comparerService.comparerEnsembles(id1, id2),
                "Devrait lancer une exception pour ensemble inexistant"
            );
            OK("Exception lancée pour ensemble inexistant", false);
            
            assertTrue(exception.getMessage().contains(id1),
                "Le message devrait contenir l'ID de l'ensemble");
            OK("Message d'erreur contient l'ID");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    /**
     * Test 12.5 : Vérifie la recommandation personnalisée selon les préférences.
     * 
     * <p><b>Jeu d'arguments :</b></p>
     * <ul>
     *   <li>ensembleIds = ["ENS-00001", "ENS-00002"]</li>
     *   <li>preferences = { priorite: "chargeMinimale" }</li>
     * </ul>
     * 
     * <p><b>Retour attendu :</b> Map avec ensembleRecommande, raison, score</p>
     */
    @Test
    @DisplayName("Test 12.5 - Devrait générer une recommandation personnalisée basée sur les préférences")
    void testRecommandationPersonnalisee() {
        // ARRANGE
        String id1 = "ENS-00001";
        String id2 = "ENS-00002";
        List<String> ensembleIds = Arrays.asList(id1, id2);
        
        EnsembleCours ensemble1 = creerEnsemble(id1, "Léger", 2);
        EnsembleCours ensemble2 = creerEnsemble(id2, "Chargé", 5);
        
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("priorite", "chargeMinimale");
        
        when(mockEnsembleRepository.findById(id1)).thenReturn(Optional.of(ensemble1));
        when(mockEnsembleRepository.findById(id2)).thenReturn(Optional.of(ensemble2));
        when(mockConflitService.detecterConflits(anyString())).thenReturn(new ArrayList<>());
        
        // ACT
        Map<String, Object> recommandation = comparerService.genererRecommandationPersonnalisee(
                ensembleIds, preferences);
        
        // ASSERT
        try {
            assertNotNull(recommandation, "Recommandation ne devrait pas être null");
            OK("Recommandation générée", false);
            
            assertEquals(id1, recommandation.get("ensembleRecommande"),
                "Ensemble avec charge minimale devrait être recommandé");
            OK("Ensemble recommandé: " + id1, false);
            
            assertTrue(recommandation.containsKey("raison"), "Devrait contenir une raison");
            OK("Raison fournie", false);
            
            assertEquals("chargeMinimale", recommandation.get("prioriteUtilisee"),
                "La priorité utilisée devrait être retournée");
            OK("Priorité utilisée: chargeMinimale");
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
        System.out.println("COMPLETED: ComparerEnsemblesService Tests (CU#12)");
        System.out.println("=".repeat(80) + "\n");
    }

    /**
     * Crée un ensemble de test avec le nombre de cours spécifié.
     * 
     * @param id L'identifiant de l'ensemble
     * @param nom Le nom de l'ensemble
     * @param nombreCours Le nombre de cours à simuler
     * @return L'ensemble créé
     */
    private EnsembleCours creerEnsemble(String id, String nom, int nombreCours) {
        EnsembleCours ensemble = new EnsembleCours(id, nom, "A25");
        List<String> coursIds = new ArrayList<>();
        for (int i = 1; i <= nombreCours; i++) {
            coursIds.add("IFT" + (1000 + i));
        }
        ensemble.setCoursIds(coursIds);
        return ensemble;
    }

    private void printMessage(String message, boolean isOk, boolean isLast) {
        String symbol = isLast ? "└─" : "├─";
        String status = isOk ? "[PASS]" : "[FAIL]";
        System.out.println("    │   " + symbol + " " + status + " " + message);
    }

    private void OK(String message) {
        printMessage(message, true, true);
    }

    private void OK(String message, boolean isLast) {
        printMessage(message, true, isLast);
    }

    private void Err(String message) {
        printMessage(message, false, true);
    }
}
