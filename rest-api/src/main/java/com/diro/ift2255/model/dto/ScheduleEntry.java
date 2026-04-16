package com.diro.ift2255.model.dto;

public class ScheduleEntry {
    public String day;
    public String startTime;
    public String endTime;
    public String room;

    public ScheduleEntry() {}

    public ScheduleEntry(String day, String startTime, String endTime, String room) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room != null ? room : "";
    }
}

