package com.diro.ift2255.util;

import java.text.Normalizer;

public final class StringUtil {

    private StringUtil() {}

    public static String normalize(String s) {
        if (s == null) return "";
        return Normalizer
                .normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase();
    }
}
