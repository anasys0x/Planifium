package com.diro.ift2255.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.diro.ift2255.model.ConflitHoraire;
import com.diro.ift2255.model.EnsembleCours;
import com.diro.ift2255.model.Horaire;
import com.diro.ift2255.repository.ConflitHoraireRepository;
import com.diro.ift2255.repository.EnsembleCoursRepository;

/**
 * Tests unitaires pour {@link ConflitHoraireService} (CU#11 - Détecter les conflits d'horaire).
 * 
 * <p>Cette classe contient 5 cas de tests distincts utilisant JUnit 5 et Mockito
 * pour valider le comportement du service de détection des conflits d'horaire.</p>
 * 
 * <h2>Cas de tests couverts :</h2>
 * <ul>
 *   <li>Test 11.1 : Détection d'un conflit total (horaires identiques)</li>
 *   <li>Test 11.2 : Détection d'un conflit partiel (chevauchement)</li>
 *   <li>Test 11.3 : Aucun conflit pour des jours différents</li>
 *   <li>Test 11.4 : Génération d'un résumé des conflits</li>
 *   <li>Test 11.5 : Exception pour ensemble inexistant</li>
 * </ul>
 * 
 * @author Équipe 10
 * @version 3.0
 * @since 2025-12-27
 * @see ConflitHoraireService
 * @see ConflitHoraire
 */
@ExtendWith(MockitoExtension.class)
public class ConflitHoraireServiceTest {
    
    /**
     * Mock du repository des conflits.
     */
    @Mock
    private ConflitHoraireRepository mockConflitRepository;
    
    /**
     * Mock du repository des ensembles.
     */
    @Mock
    private EnsembleCoursRepository mockEnsembleRepository;
    
    /**
     * Instance du service à tester.
     */
    private ConflitHoraireService conflitService;
    
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
        System.out.println("ConflitHoraireService Tests (CU#11 - Détecter les conflits d'horaire)");
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
        conflitService = new ConflitHoraireService(mockConflitRepository, mockEnsembleRepository);
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
     * Test 11.1 : Vérifie la détection d'un conflit total (horaires identiques).
     * 
     * <p><b>Jeu d'arguments :</b></p>
     * <ul>
     *   <li>Horaire 1 : Lundi 10h30-12h30 (IFT1015)</li>
     *   <li>Horaire 2 : Lundi 10h30-12h30 (IFT2255)</li>
     * </ul>
     * 
     * <p><b>Retour attendu :</b> 1 conflit de type TOTAL</p>
     * <p><b>Effets de bord :</b> Repository appelé pour détecter les conflits</p>
     */
    @Test
    @DisplayName("Test 11.1 - Devrait détecter un conflit total pour des horaires identiques")
    void testDetecterConflitTotal() {
        // ARRANGE
        String ensembleId = "ENS-00001";
        
        Horaire h1 = new Horaire("IFT1015", DayOfWeek.MONDAY, 
                LocalTime.of(10, 30), LocalTime.of(12, 30));
        Horaire h2 = new Horaire("IFT2255", DayOfWeek.MONDAY, 
                LocalTime.of(10, 30), LocalTime.of(12, 30));
        
        EnsembleCours ensemble = creerEnsembleAvecHoraires(ensembleId, h1, h2);
        
        ConflitHoraire conflitTotal = new ConflitHoraire(h1, h2, ConflitHoraire.TypeConflit.TOTAL);
        
        when(mockEnsembleRepository.findById(ensembleId)).thenReturn(Optional.of(ensemble));
        when(mockConflitRepository.detecterConflitsPourEnsemble(ensemble))
                .thenReturn(Arrays.asList(conflitTotal));
        
        // ACT
        List<ConflitHoraire> result = conflitService.detecterConflits(ensembleId);
        
        // ASSERT
        try {
            assertNotNull(result, "Résultat ne devrait pas être null");
            OK("Résultat non null", false);
            
            assertEquals(1, result.size(), "Devrait y avoir 1 conflit");
            OK("1 conflit détecté", false);
            
            assertEquals(ConflitHoraire.TypeConflit.TOTAL, result.get(0).getType(),
                "Le conflit devrait être de type TOTAL");
            OK("Type de conflit: TOTAL");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    /**
     * Test 11.2 : Vérifie la détection d'un conflit partiel (chevauchement).
     * 
     * <p><b>Jeu d'arguments :</b></p>
     * <ul>
     *   <li>Horaire 1 : Lundi 10h30-12h30</li>
     *   <li>Horaire 2 : Lundi 11h30-13h30 (1h de chevauchement)</li>
     * </ul>
     * 
     * <p><b>Retour attendu :</b> 1 conflit de type PARTIEL</p>
     */
    @Test
    @DisplayName("Test 11.2 - Devrait détecter un conflit partiel pour des horaires qui se chevauchent")
    void testDetecterConflitPartiel() {
        // ARRANGE
        String ensembleId = "ENS-00002";
        
        Horaire h1 = new Horaire("IFT1015", DayOfWeek.MONDAY, 
                LocalTime.of(10, 30), LocalTime.of(12, 30));
        Horaire h2 = new Horaire("IFT2255", DayOfWeek.MONDAY, 
                LocalTime.of(11, 30), LocalTime.of(13, 30));
        
        EnsembleCours ensemble = creerEnsembleAvecHoraires(ensembleId, h1, h2);
        
        ConflitHoraire conflitPartiel = new ConflitHoraire(h1, h2, ConflitHoraire.TypeConflit.PARTIEL);
        
        when(mockEnsembleRepository.findById(ensembleId)).thenReturn(Optional.of(ensemble));
        when(mockConflitRepository.detecterConflitsPourEnsemble(ensemble))
                .thenReturn(Arrays.asList(conflitPartiel));
        
        // ACT
        List<ConflitHoraire> result = conflitService.detecterConflits(ensembleId);
        
        // ASSERT
        try {
            assertEquals(1, result.size(), "Devrait y avoir 1 conflit");
            OK("1 conflit partiel détecté", false);
            
            assertEquals(ConflitHoraire.TypeConflit.PARTIEL, result.get(0).getType(),
                "Le conflit devrait être de type PARTIEL");
            OK("Type de conflit: PARTIEL");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    /**
     * Test 11.3 : Vérifie qu'aucun conflit n'est détecté pour des jours différents.
     * 
     * <p><b>Jeu d'arguments :</b></p>
     * <ul>
     *   <li>Horaire 1 : Lundi 10h30-12h30</li>
     *   <li>Horaire 2 : Mardi 10h30-12h30 (même heure, jour différent)</li>
     * </ul>
     * 
     * <p><b>Retour attendu :</b> Liste vide (aucun conflit)</p>
     */
    @Test
    @DisplayName("Test 11.3 - Devrait ne pas détecter de conflit pour des jours différents")
    void testPasDeConflitPourJoursDifferents() {
        // ARRANGE
        String ensembleId = "ENS-00003";
        
        Horaire h1 = new Horaire("IFT1015", DayOfWeek.MONDAY, 
                LocalTime.of(10, 30), LocalTime.of(12, 30));
        Horaire h2 = new Horaire("IFT2255", DayOfWeek.TUESDAY, 
                LocalTime.of(10, 30), LocalTime.of(12, 30));
        
        EnsembleCours ensemble = creerEnsembleAvecHoraires(ensembleId, h1, h2);
        
        when(mockEnsembleRepository.findById(ensembleId)).thenReturn(Optional.of(ensemble));
        when(mockConflitRepository.detecterConflitsPourEnsemble(ensemble))
                .thenReturn(new ArrayList<>());
        
        // ACT
        List<ConflitHoraire> result = conflitService.detecterConflits(ensembleId);
        
        // ASSERT
        try {
            assertNotNull(result, "Résultat ne devrait pas être null");
            OK("Résultat non null", false);
            
            assertTrue(result.isEmpty(), "Ne devrait pas y avoir de conflits");
            OK("Aucun conflit pour jours différents");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    /**
     * Test 11.4 : Vérifie la génération du résumé des conflits.
     * 
     * <p><b>Jeu d'arguments :</b> Ensemble avec 2 conflits</p>
     * <p><b>Retour attendu :</b> Map avec nombreConflits, tempsTotal, coursImpliques</p>
     */
    @Test
    @DisplayName("Test 11.4 - Devrait générer un résumé des conflits avec statistiques")
    void testGetResumeConflits() {
        // ARRANGE
        String ensembleId = "ENS-00004";
        
        Horaire h1 = new Horaire("IFT1015", DayOfWeek.MONDAY, 
                LocalTime.of(10, 30), LocalTime.of(12, 30));
        Horaire h2 = new Horaire("IFT2255", DayOfWeek.MONDAY, 
                LocalTime.of(11, 30), LocalTime.of(13, 30));
        
        EnsembleCours ensemble = creerEnsembleAvecHoraires(ensembleId, h1, h2);
        
        ConflitHoraire conflit = new ConflitHoraire(h1, h2, ConflitHoraire.TypeConflit.PARTIEL);
        List<ConflitHoraire> conflits = Arrays.asList(conflit);
        
        when(mockEnsembleRepository.findById(ensembleId)).thenReturn(Optional.of(ensemble));
        when(mockConflitRepository.detecterConflitsPourEnsemble(ensemble)).thenReturn(conflits);
        when(mockConflitRepository.calculerTempsTotal(conflits)).thenReturn(60L);
        when(mockConflitRepository.compterParType(conflits)).thenReturn(
                Map.of(ConflitHoraire.TypeConflit.PARTIEL, 1L));
        
        // ACT
        Map<String, Object> resume = conflitService.getResumeConflits(ensembleId);
        
        // ASSERT
        try {
            assertNotNull(resume, "Résumé ne devrait pas être null");
            OK("Résumé généré", false);
            
            assertEquals(1, resume.get("nombreConflits"), "Devrait avoir 1 conflit");
            OK("nombreConflits = 1", false);
            
            assertEquals(60L, resume.get("tempsChevauchementsMinutes"), 
                "Temps total devrait être 60 minutes");
            OK("tempsChevauchementsMinutes = 60", false);
            
            assertTrue((Boolean) resume.get("aDesConflits"), "aDesConflits devrait être true");
            OK("aDesConflits = true");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    /**
     * Test 11.5 : Vérifie qu'une exception est lancée pour un ensemble inexistant.
     * 
     * <p><b>Jeu d'arguments :</b> ensembleId = "ENS-INEXISTANT"</p>
     * <p><b>Retour attendu :</b> IllegalArgumentException</p>
     */
    @Test
    @DisplayName("Test 11.5 - Devrait lancer une exception pour un ensemble inexistant")
    void testExceptionPourEnsembleInexistant() {
        // ARRANGE
        String ensembleId = "ENS-INEXISTANT";
        
        when(mockEnsembleRepository.findById(ensembleId)).thenReturn(Optional.empty());
        
        // ACT & ASSERT
        try {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> conflitService.detecterConflits(ensembleId),
                "Devrait lancer une exception pour ensemble inexistant"
            );
            OK("Exception lancée pour ensemble inexistant", false);
            
            assertTrue(exception.getMessage().contains(ensembleId),
                "Le message devrait contenir l'ID de l'ensemble");
            OK("Message d'erreur contient l'ID");
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
        System.out.println("COMPLETED: ConflitHoraireService Tests (CU#11)");
        System.out.println("=".repeat(80) + "\n");
    }

    /**
     * Crée un ensemble de test avec les horaires spécifiés.
     * 
     * @param id L'identifiant de l'ensemble
     * @param horaires Les horaires à ajouter
     * @return L'ensemble créé
     */
    private EnsembleCours creerEnsembleAvecHoraires(String id, Horaire... horaires) {
        EnsembleCours ensemble = new EnsembleCours(id, "Test", "A25");
        Map<String, List<Horaire>> horairesMap = new HashMap<>();
        
        for (Horaire h : horaires) {
            horairesMap.computeIfAbsent(h.getCourseId(), k -> new ArrayList<>()).add(h);
        }
        
        ensemble.setHoraires(horairesMap);
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
