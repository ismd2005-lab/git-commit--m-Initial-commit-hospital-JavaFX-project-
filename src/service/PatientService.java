package service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Appointment;
import model.Invoice;
import model.MedicalEvent;
import model.Patient;
import repository.AppointmentRepository;
import repository.InvoiceRepository;
import repository.PatientRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Pattern;

public class PatientService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final InvoiceRepository invoiceRepository;

    public PatientService(PatientRepository patientRepository, AppointmentRepository appointmentRepository,
                          InvoiceRepository invoiceRepository) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public ObservableList<Patient> findAll() {
        return patientRepository.findAll().sorted(Comparator.comparing(Patient::getLastName)
                .thenComparing(Patient::getFirstName));
    }

    public Patient addPatient(Patient patient) {
        validate(patient);
        patient.setCreatedAt(patient.getCreatedAt() == null ? LocalDate.now() : patient.getCreatedAt());
        return patientRepository.save(patient);
    }

    public Patient updatePatient(Patient patient) {
        validate(patient);
        patientRepository.findById(patient.getId())
                .ifPresent(existing -> patient.setCreatedAt(existing.getCreatedAt()));
        return patientRepository.update(patient);
    }

    public void deletePatient(int patientId) {
        appointmentRepository.findAll().stream()
                .filter(appointment -> appointment.getPatientId() == patientId)
                .map(Appointment::getId)
                .toList()
                .forEach(appointmentRepository::delete);
        invoiceRepository.findAll().stream()
                .filter(invoice -> invoice.getPatientId() == patientId)
                .map(Invoice::getId)
                .toList()
                .forEach(invoiceRepository::delete);
        patientRepository.delete(patientId);
    }

    public ObservableList<Patient> search(String keyword, String gender, String activeFilter) {
        String safeKeyword = normalize(keyword);
        return FXCollections.observableArrayList(patientRepository.findAll().stream()
                .filter(patient -> matchesKeyword(patient, safeKeyword))
                .filter(patient -> matchesGender(patient, gender))
                .filter(patient -> matchesActive(patient, activeFilter))
                .sorted(Comparator.comparing(Patient::getLastName).thenComparing(Patient::getFirstName))
                .toList());
    }

    public Patient findById(int id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found."));
    }

    public ObservableList<MedicalEvent> timelineFor(int patientId) {
        return patientRepository.findTimelineByPatientId(patientId);
    }

    public MedicalEvent addTimelineEvent(int patientId, String title, String details, String category) {
        return patientRepository.addTimelineEvent(new MedicalEvent(0, patientId, LocalDate.now(),
                title, details, category));
    }

    private void validate(Patient patient) {
        if (patient.getFirstName() == null || patient.getFirstName().isBlank()
                || patient.getLastName() == null || patient.getLastName().isBlank()) {
            throw new IllegalArgumentException("First name and last name are required.");
        }
        if (patient.getAge() < 0 || patient.getAge() > 130) {
            throw new IllegalArgumentException("Age must be between 0 and 130.");
        }
        if (patient.getGender() == null || patient.getGender().isBlank()) {
            throw new IllegalArgumentException("Gender is required.");
        }
        if (patient.getEmail() != null && !patient.getEmail().isBlank()
                && !EMAIL_PATTERN.matcher(patient.getEmail()).matches()) {
            throw new IllegalArgumentException("Email address is invalid.");
        }
        patient.setPriorityScore(Math.max(0, Math.min(100, patient.getPriorityScore())));
    }

    private boolean matchesKeyword(Patient patient, String keyword) {
        if (keyword.isBlank()) {
            return true;
        }
        return normalize(patient.getFullName()).contains(keyword)
                || normalize(patient.getPhone()).contains(keyword)
                || normalize(patient.getEmail()).contains(keyword)
                || normalize(patient.getMedicalNotes()).contains(keyword);
    }

    private boolean matchesGender(Patient patient, String gender) {
        return gender == null || gender.equals("All") || gender.equalsIgnoreCase(patient.getGender());
    }

    private boolean matchesActive(Patient patient, String activeFilter) {
        if (activeFilter == null || activeFilter.equals("All")) {
            return true;
        }
        return activeFilter.equals("Active") == patient.isActive();
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }
}
