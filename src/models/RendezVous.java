package models;

import java.time.LocalDate;

public class RendezVous {
    private int idRdv;
    private int idPatient;
    private String patientNomComplet;
    private LocalDate dateRdv;
    private String heure;
    private String medecin;
    private String specialite;
    private String statut;
    private String remarque;

    public RendezVous() {
    }

    public RendezVous(int idRdv, int idPatient, String patientNomComplet, LocalDate dateRdv,
                      String heure, String medecin, String specialite, String statut,
                      String remarque) {
        this.idRdv = idRdv;
        this.idPatient = idPatient;
        this.patientNomComplet = patientNomComplet;
        this.dateRdv = dateRdv;
        this.heure = heure;
        this.medecin = medecin;
        this.specialite = specialite;
        this.statut = statut;
        this.remarque = remarque;
    }

    public int getIdRdv() {
        return idRdv;
    }

    public void setIdRdv(int idRdv) {
        this.idRdv = idRdv;
    }

    public int getIdPatient() {
        return idPatient;
    }

    public void setIdPatient(int idPatient) {
        this.idPatient = idPatient;
    }

    public String getPatientNomComplet() {
        return patientNomComplet;
    }

    public void setPatientNomComplet(String patientNomComplet) {
        this.patientNomComplet = patientNomComplet;
    }

    public LocalDate getDateRdv() {
        return dateRdv;
    }

    public void setDateRdv(LocalDate dateRdv) {
        this.dateRdv = dateRdv;
    }

    public String getHeure() {
        return heure;
    }

    public void setHeure(String heure) {
        this.heure = heure;
    }

    public String getMedecin() {
        return medecin;
    }

    public void setMedecin(String medecin) {
        this.medecin = medecin;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getRemarque() {
        return remarque;
    }

    public void setRemarque(String remarque) {
        this.remarque = remarque;
    }
}
