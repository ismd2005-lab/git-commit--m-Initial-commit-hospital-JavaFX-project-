package model;

import java.time.LocalDate;

public class MedicalEvent {
    private int id;
    private int patientId;
    private LocalDate date;
    private String title;
    private String details;
    private String category;

    public MedicalEvent() {
    }

    public MedicalEvent(int id, int patientId, LocalDate date, String title, String details, String category) {
        this.id = id;
        this.patientId = patientId;
        this.date = date;
        this.title = title;
        this.details = details;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTimelineLabel() {
        return date + "  |  " + category + "  |  " + title + " - " + details;
    }
}
