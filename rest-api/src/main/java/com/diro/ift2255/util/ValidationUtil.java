package com.diro.ift2255.util;

import java.util.regex.Pattern;

public class ValidationUtil {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");
    
    private static final Pattern SEMESTER_PATTERN =
            Pattern.compile("^[HAEhae]\\d{2}$");

    public static boolean isEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Valide le format d'un code de semestre.
     * 
     * Format valide: 3 caractères au total
     * - Premier caractère: H (Hiver), A (Automne), ou E (Été) - insensible à la casse
     * - Deux caractères suivants: deux chiffres représentant l'année (ex: 24, 25)
     * 
     * Exemples valides:
     * - H25 (Hiver 2025)
     * - A24 (Automne 2024)
     * - E24 (Été 2024)
     * - h25, a24, e24 (insensible à la casse)
     * 
     * @param semester Le code de semestre à valider
     * @return true si le format est valide, false sinon
     */
    public static boolean isValidSemester(String semester) {
        return semester != null && SEMESTER_PATTERN.matcher(semester).matches();
    }
}
