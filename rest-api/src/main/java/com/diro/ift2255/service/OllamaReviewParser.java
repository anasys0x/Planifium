package com.diro.ift2255.service;

import com.diro.ift2255.model.dto.ReviewExtraction;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OllamaReviewParser implements LlmReviewParser {

    private final HttpClient http;
    private final String baseUrl;
    private final String model;

    // Extrait un objet JSON depuis le texte (premier {...})
    private static final Pattern JSON_OBJ = Pattern.compile("\\{.*?\\}", Pattern.DOTALL);

    // Champs dans le JSON retourné
    private static final Pattern COURSE = Pattern.compile("\"courseCode\"\\s*:\\s*(null|\"([^\"]+)\")");
    private static final Pattern DIFF = Pattern.compile("\"difficulty\"\\s*:\\s*(null|([0-9]+))");
    private static final Pattern COMM = Pattern.compile("\"comment\"\\s*:\\s*(null|\"([^\"]*)\")");

    public OllamaReviewParser(String baseUrl, String model) {
        this.baseUrl = baseUrl;
        this.model = model;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public ReviewExtraction extract(String text) {
        try {
            String system =
                    "Tu extrais des champs et tu réponds UNIQUEMENT en JSON valide.\n" +
                    "Champs: courseCode (ex: IFT2255), difficulty (entier 1-10), comment (string).\n" +
                    "Si un champ est introuvable: null.\n" +
                    "Accepte '8/10' comme difficulty=8.\n" +
                    "Ne traduis pas et ne reformule pas: si tu remplis comment, garde le texte original.\n";

            String prompt =
                    "Texte:\n" + text + "\n\n" +
                    "Réponds en JSON uniquement, exemple:\n" +
                    "{\"courseCode\":\"IFT2255\",\"difficulty\":8,\"comment\":null}";

            String body = "{"
                    + "\"model\":\"" + escapeJson(model) + "\","
                    + "\"system\":\"" + escapeJson(system) + "\","
                    + "\"prompt\":\"" + escapeJson(prompt) + "\","
                    + "\"stream\":false"
                    + "}";

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/generate"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (res.statusCode() >= 300) return new ReviewExtraction(null, null, null);

            String responseText = extractField(res.body(), "\"response\"");
            if (responseText == null) return new ReviewExtraction(null, null, null);

            String json = firstJsonObject(responseText);
            if (json == null) return new ReviewExtraction(null, null, null);

            String course = matchString(COURSE, json);
            Integer diff = matchInt(DIFF, json);
            String comment = matchString(COMM, json);

            return new ReviewExtraction(course, diff, comment);
        } catch (Exception e) {
            return new ReviewExtraction(null, null, null);
        }
    }

    private static String firstJsonObject(String s) {
        Matcher m = JSON_OBJ.matcher(s);
        return m.find() ? m.group() : null;
    }

    private static String matchString(Pattern p, String json) {
        Matcher m = p.matcher(json);
        if (!m.find()) return null;
        if ("null".equals(m.group(1))) return null;
        return m.group(2);
    }

    private static Integer matchInt(Pattern p, String json) {
        Matcher m = p.matcher(json);
        if (!m.find()) return null;
        if ("null".equals(m.group(1))) return null;
        return Integer.parseInt(m.group(2));
    }

    // Extrait une valeur string d’un champ JSON simple: "key":"value"
    private static String extractField(String json, String fieldNameQuoted) {
        int idx = json.indexOf(fieldNameQuoted);
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx);
        if (colon < 0) return null;

        int firstQuote = json.indexOf('"', colon + 1);
        if (firstQuote < 0) return null;

        int i = firstQuote + 1;
        StringBuilder sb = new StringBuilder();
        boolean escape = false;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (escape) {
                sb.append(c);
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
            i++;
        }
        return sb.toString();
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
