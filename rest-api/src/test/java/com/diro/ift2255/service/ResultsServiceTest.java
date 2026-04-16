package com.diro.ift2255.service;

import com.diro.ift2255.model.dto.CourseResultDto;
import com.diro.ift2255.repository.ResultsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultsServiceTest {

    private ResultsService service;

    @BeforeEach
    void setup() {
        ResultsRepository repo = new ResultsRepository("data/historique_cours_prog_117510.csv");
        service = new ResultsService(repo);
    }

    @Test
    void RA1_courseExists_returnsDtoWithExpectedFields() {
        CourseResultDto dto = service.getResultsForCourse("IFT2255");

        assertNotNull(dto);
        assertEquals("IFT2255", dto.sigle);
        assertNotNull(dto.nom);
        assertFalse(dto.nom.isBlank());
        assertNotNull(dto.moyenne);
        assertFalse(dto.moyenne.isBlank());
    }

    @Test
    void RA2_courseMissing_throwsFriendlyError() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.getResultsForCourse("IFT0000")
        );

        String msg = ex.getMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("ce cours ne fait pas partie de la liste fournie"));
        assertTrue(msg.contains("IFT0000"));
    }

    @Test
    void RA3_lowercaseId_isAcceptedAndNormalized() {
        CourseResultDto dto = service.getResultsForCourse("ift2255");

        assertNotNull(dto);
        assertEquals("IFT2255", dto.sigle);
    }

    @Test
    void RA4_idWithSpaces_isAcceptedAndTrimmed() {
        CourseResultDto dto = service.getResultsForCourse("   IFT2255   ");

        assertNotNull(dto);
        assertEquals("IFT2255", dto.sigle);
    }

    @Test
    void RA5_dataConsistency_participantsTrimestresScoreArePositive() {
        CourseResultDto dto = service.getResultsForCourse("IFT2255");

        assertTrue(dto.participants > 0, "participants devrait être > 0");
        assertTrue(dto.trimestres > 0, "trimestres devrait être > 0");
        assertTrue(dto.score > 0, "score devrait être > 0");
    }
}
