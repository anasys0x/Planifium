package com.diro.ift2255.model.dto;

import java.util.List;

public class CheckEligibilityResponse {
    public String course_id;
    public boolean eligible;
    public boolean cycle_ok;
    public List<String> missing_prerequisites;
    public String message;

    public CheckEligibilityResponse(String courseId, boolean eligible, boolean cycleOk,
                                    List<String> missing, String message) {
        this.course_id = courseId;
        this.eligible = eligible;
        this.cycle_ok = cycleOk;
        this.missing_prerequisites = missing;
        this.message = message;
    }
}
