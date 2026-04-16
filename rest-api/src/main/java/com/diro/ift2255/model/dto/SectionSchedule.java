package com.diro.ift2255.model.dto;

import java.util.ArrayList;
import java.util.List;

public class SectionSchedule {
    public String sectionCode;
    public List<ActivitySchedule> activities;

    public SectionSchedule() {
        this.activities = new ArrayList<>();
    }

    public SectionSchedule(String sectionCode) {
        this.sectionCode = sectionCode;
        this.activities = new ArrayList<>();
    }

    public void addActivity(ActivitySchedule activity) {
        if (activities == null) {
            activities = new ArrayList<>();
        }
        activities.add(activity);
    }
}

