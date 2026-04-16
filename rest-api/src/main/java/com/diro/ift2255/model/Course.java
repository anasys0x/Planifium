package com.diro.ift2255.model;

import java.util.List; //Rajouter psk des fois les prerequis sont en liste
import java.util.Map; // Pour les sessions available on map string et bool 
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;




@JsonIgnoreProperties(ignoreUnknown = true)
public class Course {
    @JsonAlias({ "_id", "id" })
    private String id;
    private String name;
    private String credits;
    private String description;
    private List<String> prerequisite_courses;
    private Map<String, Boolean> available_terms;
    private List<Map<String, Object>> schedule;

    public Course() {}

    public Course(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Course(String id, String name, String desc) {
        this(id, name);
        this.description = desc;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCredits() { return credits; }
    public void setCredits(String credits) { this.credits = credits; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getPrerequisite_courses() { return prerequisite_courses; }
    public void setPrerequisite_courses(List<String> prerequisite_courses) { this.prerequisite_courses = prerequisite_courses; }

    public Map<String, Boolean> getAvailable_terms() {return available_terms; }
    public void setAvailable_terms(Map<String, Boolean> available_terms) {this.available_terms = available_terms; }

    @JsonAlias({"schedule", "schedules", "sections"})
    public List<Map<String, Object>> getSchedule() { return schedule; }
    public void setSchedule(List<Map<String, Object>> schedule) { this.schedule = schedule; }
}
