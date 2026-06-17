package models;

import java.time.LocalDate;

public class Patient {
    private int idPatient;
    private String nom;
    private String prenom;
    private int age;
    private String sexe;
    private String telephone;
    private String email;
    private String description;
    private LocalDate dateCreation;
    private boolean actif;

    public Patient() {
    }

    public Patient(int idPatient, String nom, String prenom, int age, String sexe,
                   String telephone, String email, String description,
                   LocalDate dateCreation, boolean actif) {
        this.idPatient = idPatient;
        this.nom = nom;
        this.prenom = prenom;
        this.age = age;
        this.sexe = sexe;
        this.telephone = telephone;
        this.email = email;
        this.description = description;
        this.dateCreation = dateCreation;
        this.actif = actif;
    }

    public int getIdPatient() {
        return idPatient;
    }

    public void setIdPatient(int idPatient) {
        this.idPatient = idPatient;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    @Override
    public String toString() {
        return getNomComplet();
    }
}
