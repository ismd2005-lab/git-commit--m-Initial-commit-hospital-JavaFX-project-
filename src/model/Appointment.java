package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Appointment {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private int id;
    private int patientId;
    private String patientName;
    private LocalDate date;
    private LocalTime time;
    private String doctor;
    private String specialty;
    private String status;
    private String notes;

    public Appointment() {
    }

    public Appointment(int id, int patientId, String patientName, LocalDate date, LocalTime time,
                       String doctor, String specialty, String status, String notes) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.date = date;
        this.time = time;
        this.doctor = doctor;
        this.specialty = specialty;
        this.status = status;
        this.notes = notes;
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

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getDoctor() {
        return doctor;
    }

    public void setDoctor(String doctor) {
        this.doctor = doctor;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getTimeLabel() {
        return time == null ? "-" : time.format(TIME_FORMATTER);
    }

    public String getScheduleLabel() {
        return (date == null ? "-" : date) + "  " + getTimeLabel();
    }
}
