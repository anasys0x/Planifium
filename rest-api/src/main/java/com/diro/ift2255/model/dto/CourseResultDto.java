package com.diro.ift2255.model.dto;

public class CourseResultDto {
    public String sigle;
    public String nom;
    public String moyenne;     // ex: "A-", "B+"
    public double score;       // ex: 3.58
    public int participants;
    public int trimestres;

    public CourseResultDto(String sigle, String nom, String moyenne, double score, int participants, int trimestres) {
        this.sigle = sigle;
        this.nom = nom;
        this.moyenne = moyenne;
        this.score = score;
        this.participants = participants;
        this.trimestres = trimestres;
    }
}
