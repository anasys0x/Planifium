package com.diro.ift2255.service;

import com.diro.ift2255.model.dto.CourseResultDto;
import com.diro.ift2255.repository.ResultsRepository;

import java.util.Locale;

public class ResultsService {

    private final ResultsRepository repo;

    public ResultsService(ResultsRepository repo) {
        this.repo = repo;
    }

    public CourseResultDto getResultsForCourse(String courseId) {
        String normalized = normalize(courseId);

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("id de cours invalide.");
        }

        return repo.findBySigle(normalized)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aucune statistique trouvée pour " + normalized +
                                " (ce cours ne fait pas partie de la liste fournie)."
                ));
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.trim().toUpperCase(Locale.ROOT);
    }
}
