package repository;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.MedicalEvent;
import model.Patient;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;

public class InMemoryPatientRepository implements PatientRepository {
    private final ObservableList<Patient> patients = FXCollections.observableArrayList();
    private final ObservableList<MedicalEvent> timeline = FXCollections.observableArrayList();
    private int nextPatientId = 1;
    private int nextEventId = 1;

    public InMemoryPatientRepository() {
        seedPatients();
        seedTimeline();
    }

    @Override
    public ObservableList<Patient> findAll() {
        return patients;
    }

    @Override
    public Optional<Patient> findById(int id) {
        return patients.stream().filter(patient -> patient.getId() == id).findFirst();
    }

    @Override
    public Patient save(Patient patient) {
        patient.setId(nextPatientId++);
        if (patient.getCreatedAt() == null) {
            patient.setCreatedAt(LocalDate.now());
        }
        patients.add(patient);
        addTimelineEvent(new MedicalEvent(0, patient.getId(), LocalDate.now(),
                "Patient profile created", "Clinical intake completed", "Intake"));
        return patient;
    }

    @Override
    public Patient update(Patient patient) {
        for (int i = 0; i < patients.size(); i++) {
            if (patients.get(i).getId() == patient.getId()) {
                patients.set(i, patient);
                addTimelineEvent(new MedicalEvent(0, patient.getId(), LocalDate.now(),
                        "Profile updated", "Administrative or medical data changed", "Record"));
                return patient;
            }
        }
        throw new IllegalArgumentException("Patient not found.");
    }

    @Override
    public void delete(int id) {
        patients.removeIf(patient -> patient.getId() == id);
        timeline.removeIf(event -> event.getPatientId() == id);
    }

    @Override
    public ObservableList<MedicalEvent> findTimelineByPatientId(int patientId) {
        return timeline.filtered(event -> event.getPatientId() == patientId)
                .sorted(Comparator.comparing(MedicalEvent::getDate).reversed());
    }

    @Override
    public MedicalEvent addTimelineEvent(MedicalEvent event) {
        event.setId(nextEventId++);
        timeline.add(event);
        return event;
    }

    private void seedPatients() {
        save(new Patient(0, "Sara", "Benali", 31, "Female", "0601000001",
                "sara.benali@example.com", "A+", "Cardiology follow-up. Stable blood pressure.",
                LocalDate.now().minusMonths(5), true, 42));
        save(new Patient(0, "Youssef", "El Amrani", 46, "Male", "0601000002",
                "youssef.elamrani@example.com", "O+", "Post-operative check. Wound healing review.",
                LocalDate.now().minusMonths(4), true, 55));
        save(new Patient(0, "Nadia", "Mansouri", 27, "Female", "0601000003",
                "nadia.mansouri@example.com", "B-", "Dermatology consultation. Seasonal allergy symptoms.",
                LocalDate.now().minusMonths(3), true, 34));
        save(new Patient(0, "Karim", "Idrissi", 58, "Male", "0601000004",
                "karim.idrissi@example.com", "AB+", "Type 2 diabetes. Glucose monitoring required.",
                LocalDate.now().minusMonths(2), false, 78));
        save(new Patient(0, "Meryem", "Alaoui", 39, "Female", "0601000005",
                "meryem.alaoui@example.com", "O-", "Neurology review with headache and sleep concerns.",
                LocalDate.now().minusMonths(1), true, 63));
    }

    private void seedTimeline() {
        timeline.clear();
        addTimelineEvent(new MedicalEvent(0, 1, LocalDate.now().minusDays(40),
                "ECG screening", "Normal rhythm, continue monitoring", "Cardiology"));
        addTimelineEvent(new MedicalEvent(0, 1, LocalDate.now().minusDays(12),
                "Blood pressure review", "Stable response to care plan", "Vitals"));
        addTimelineEvent(new MedicalEvent(0, 2, LocalDate.now().minusDays(30),
                "Surgery follow-up", "No infection signal detected", "Surgery"));
        addTimelineEvent(new MedicalEvent(0, 3, LocalDate.now().minusDays(21),
                "Allergy panel", "Seasonal trigger suspected", "Lab"));
        addTimelineEvent(new MedicalEvent(0, 4, LocalDate.now().minusDays(14),
                "Glucose control", "Elevated trend, endocrinology follow-up", "Endocrinology"));
        addTimelineEvent(new MedicalEvent(0, 5, LocalDate.now().minusDays(9),
                "Neurology intake", "Sleep hygiene and imaging review planned", "Neurology"));
    }
}
