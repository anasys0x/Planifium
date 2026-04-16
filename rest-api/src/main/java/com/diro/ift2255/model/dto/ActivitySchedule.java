package com.diro.ift2255.model.dto;

import java.util.ArrayList;
import java.util.List;

public class ActivitySchedule {
    public String type;
    public List<ScheduleEntry> entries;

    public ActivitySchedule() {
        this.entries = new ArrayList<>();
    }

    public ActivitySchedule(String type) {
        this.type = type;
        this.entries = new ArrayList<>();
    }

    public void addEntry(ScheduleEntry entry) {
        if (entries == null) {
            entries = new ArrayList<>();
        }
        entries.add(entry);
    }
}

