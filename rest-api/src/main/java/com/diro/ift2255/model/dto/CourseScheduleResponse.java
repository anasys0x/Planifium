package com.diro.ift2255.model.dto;

import java.util.ArrayList;
import java.util.List;

public class CourseScheduleResponse {
    public String courseId;
    public String semester;
    public List<SectionSchedule> sections;

    public CourseScheduleResponse() {
        this.sections = new ArrayList<>();
    }

    public CourseScheduleResponse(String courseId, String semester) {
        this.courseId = courseId;
        this.semester = semester;
        this.sections = new ArrayList<>();
    }

    public void addSection(SectionSchedule section) {
        if (sections == null) {
            sections = new ArrayList<>();
        }
        sections.add(section);
    }
}

