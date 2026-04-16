package com.diro.ift2255.model;

public class Review {
    private String courseId;   // ex: IFT2255
    private String author;     // pseudo de l'étudiant
    private int difficulty;    // 1–10
    private String comment;    // Commentaire

    public Review() {}

    public Review(String courseId, String author, int difficulty, String comment) {
        this.courseId = courseId;
        this.author = author;
        this.difficulty = difficulty;
        this.comment = comment;
    }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
