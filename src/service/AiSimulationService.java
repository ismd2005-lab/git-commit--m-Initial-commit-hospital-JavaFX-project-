package service;

import model.Appointment;
import model.Invoice;
import model.Patient;
import repository.AppointmentRepository;
import repository.InvoiceRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class AiSimulationService {
    private final AppointmentRepository appointmentRepository;
    private final InvoiceRepository invoiceRepository;

    public AiSimulationService(AppointmentRepository appointmentRepository, InvoiceRepository invoiceRepository) {
        this.appointmentRepository = appointmentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public int noShowRiskScore(Patient patient) {
        if (patient == null) {
            return 0;
        }
        List<Appointment> appointments = appointmentRepository.findAll().stream()
                .filter(appointment -> appointment.getPatientId() == patient.getId())
                .toList();
        List<Invoice> invoices = invoiceRepository.findAll().stream()
                .filter(invoice -> invoice.getPatientId() == patient.getId())
                .toList();

        int score = 15;
        score += patient.getPriorityScore() / 3;
        score += patient.isActive() ? 0 : 12;
        score += patient.getAge() > 55 ? 8 : 0;
        score += (int) appointments.stream().filter(appointment -> "Cancelled".equals(appointment.getStatus())).count() * 10;
        score += invoices.stream().anyMatch(Invoice::isOverdue) ? 18 : 0;
        score += new Random(patient.getId() * 37L + LocalDate.now().getDayOfYear()).nextInt(11);
        return Math.max(3, Math.min(96, score));
    }

    public String riskBand(int score) {
        if (score >= 70) {
            return "High";
        }
        if (score >= 42) {
            return "Moderate";
        }
        return "Low";
    }

    public String diagnosisSuggestion(Patient patient) {
        if (patient == null) {
            return "Select a patient to run the simulation.";
        }
        String notes = (patient.getMedicalNotes() == null ? "" : patient.getMedicalNotes()).toLowerCase(Locale.ROOT);
        if (notes.contains("diabetes") || notes.contains("glucose") || notes.contains("glyc")) {
            return "Rule simulation: endocrine follow-up, HbA1c check, nutrition review.";
        }
        if (notes.contains("cardio") || notes.contains("pressure") || notes.contains("ecg")) {
            return "Rule simulation: cardiology review, ECG comparison, blood pressure tracking.";
        }
        if (notes.contains("skin") || notes.contains("allergy") || notes.contains("dermatology")) {
            return "Rule simulation: dermatology review, allergy trigger mapping, topical care.";
        }
        if (notes.contains("neuro") || notes.contains("headache") || notes.contains("sleep")) {
            return "Rule simulation: neurology follow-up, sleep quality review, imaging if symptoms persist.";
        }
        if (patient.getAge() > 60) {
            return "Rule simulation: geriatric screening, medication reconciliation, fall-risk review.";
        }
        return "Rule simulation: general medicine review and routine prevention checklist.";
    }
}
