package com.diro.ift2255.repository;

import com.diro.ift2255.model.dto.CourseResultDto;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Dépôt (repository) responsable du chargement et de l’accès aux résultats académiques agrégés.
 *
 * <p>Cette classe lit un fichier CSV placé dans les ressources (classpath) et construit un index en mémoire
 * permettant de retrouver rapidement les statistiques d’un cours via son sigle.</p>
 *
 * <p>Comportement volontairement “tolérant” :</p>
 * <ul>
 *   <li>Si la ressource est introuvable, le dépôt reste vide (pas de crash).</li>
 *   <li>Si une ligne est mal formée, elle est ignorée.</li>
 *   <li>Si un nombre est invalide, une valeur par défaut est utilisée (0 ou 0.0).</li>
 * </ul>
 *
 * <p>Le service qui utilise ce dépôt peut ensuite retourner une erreur plus conviviale à l’API.</p>
 *
 * @author Notre équipe
 * @version 1.0
 * @since 2025-12-28
 * @see CourseResultDto
 */
public class ResultsRepository {

    /**
     * Index principal : sigle normalisé (ex: "IFT2255") -> résultats agrégés.
     */
    private final Map<String, CourseResultDto> bySigle = new HashMap<>();

    /**
     * Construit le dépôt et tente de charger les résultats depuis une ressource CSV.
     *
     * <p>Exemple de chemin : {@code "data/results.csv"} (dans {@code src/main/resources}).</p>
     *
     * @param resourcePath chemin relatif vers la ressource CSV dans le classpath
     */
    public ResultsRepository(String resourcePath) {
        loadFromResource(resourcePath);
    }

    /**
     * Retourne les résultats associés à un sigle de cours.
     *
     * <p>La recherche est insensible à la casse et aux espaces (normalisation en majuscules).</p>
     *
     * @param sigle sigle du cours (ex: "IFT2255")
     * @return un {@link Optional} contenant le résultat si trouvé, sinon {@link Optional#empty()}
     */
    public Optional<CourseResultDto> findBySigle(String sigle) {
        if (sigle == null) return Optional.empty();
        return Optional.ofNullable(bySigle.get(normalize(sigle)));
    }

    /**
     * Charge le CSV depuis le classpath et remplit l'index interne.
     *
     * <p>Format attendu (6 colonnes minimum) :</p>
     * <pre>
     * sigle,nom,moyenne,score,participants,trimestres
     * IFT2255,Nom du cours,A-,3.58,120,6
     * </pre>
     *
     * @param resourcePath chemin relatif vers la ressource dans le classpath
     */
    private void loadFromResource(String resourcePath) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            // Pas de crash : repo vide, le service pourra renvoyer une erreur conviviale.
            return;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String header = br.readLine(); // skip header
            if (header == null) return;

            String line;
            while ((line = br.readLine()) != null) {
                // split simple et tolérant (séparateur = virgule, espaces optionnels)
                String[] p = line.split("\\s*,\\s*", -1);
                if (p.length < 6) continue;

                String sigle = normalize(p[0]);
                String nom = p[1].trim();
                String moyenne = p[2].trim();
                double score = parseDoubleSafe(p[3]);
                int participants = parseIntSafe(p[4]);
                int trimestres = parseIntSafe(p[5]);

                bySigle.put(sigle, new CourseResultDto(sigle, nom, moyenne, score, participants, trimestres));
            }
        } catch (Exception ignored) {
            // volontairement silencieux : un dépôt vide est acceptable, le service gère l'erreur.
        }
    }

    /**
     * Normalise un sigle pour l'utiliser comme clé.
     *
     * @param s texte à normaliser
     * @return texte en majuscules, sans espaces inutiles
     */
    private static String normalize(String s) {
        return s.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Parse un entier sans lever d'exception.
     *
     * @param s texte à convertir
     * @return valeur entière, ou 0 si invalide
     */
    private static int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Parse un double sans lever d'exception.
     *
     * @param s texte à convertir
     * @return valeur double, ou 0.0 si invalide
     */
    private static double parseDoubleSafe(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}
