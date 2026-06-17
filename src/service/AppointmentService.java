package service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Appointment;
import model.Patient;
import repository.AppointmentRepository;
import repository.PatientRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AppointmentService {
    public static final List<String> STATUSES = List.of("Pending", "Confirmed", "Cancelled");
    private static final Map<String, List<String>> DOCTORS_BY_SPECIALTY = new LinkedHashMap<>();

    static {
        DOCTORS_BY_SPECIALTY.put("Cardiology", List.of("Dr. Lina Berrada", "Dr. Sami Karim"));
        DOCTORS_BY_SPECIALTY.put("Dermatology", List.of("Dr. Salma Naciri", "Dr. Amal Kettani"));
        DOCTORS_BY_SPECIALTY.put("Endocrinology", List.of("Dr. Omar Rami", "Dr. Rania Fassi"));
        DOCTORS_BY_SPECIALTY.put("Neurology", List.of("Dr. Ines Tazi", "Dr. Mehdi Lahlou"));
        DOCTORS_BY_SPECIALTY.put("Pediatrics", List.of("Dr. Yasmine Barakat", "Dr. Adam Bennani"));
        DOCTORS_BY_SPECIALTY.put("Radiology", List.of("Dr. Sofia El Fassi", "Dr. Walid Nadif"));
        DOCTORS_BY_SPECIALTY.put("Surgery", List.of("Dr. Amine Saidi", "Dr. Noura El Ghazali"));
        DOCTORS_BY_SPECIALTY.put("General Medicine", List.of("Dr. Hajar Alaoui", "Dr. Anas Bakkali"));
    }

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;

    public AppointmentService(AppointmentRepository appointmentRepository, PatientRepository patientRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
    }

    public ObservableList<Appointment> findAll() {
        return appointmentRepository.findAll().sorted(Comparator.comparing(Appointment::getDate)
                .thenComparing(Appointment::getTime));
    }

    public Appointment addAppointment(Appointment appointment) {
        validate(appointment);
        enrichPatientName(appointment);
        ensureNoConflict(appointment, 0);
        return appointmentRepository.save(appointment);
    }

    public Appointment updateAppointment(Appointment appointment) {
        validate(appointment);
        enrichPatientName(appointment);
        ensureNoConflict(appointment, appointment.getId());
        return appointmentRepository.update(appointment);
    }

    public void deleteAppointment(int id) {
        appointmentRepository.delete(id);
    }

    public ObservableList<Appointment> search(String keyword, String specialty, String status, LocalDate date) {
        String safeKeyword = normalize(keyword);
        return FXCollections.observableArrayList(appointmentRepository.findAll().stream()
                .filter(appointment -> matchesKeyword(appointment, safeKeyword))
                .filter(appointment -> specialty == null || specialty.equals("All")
                        || specialty.equals(appointment.getSpecialty()))
                .filter(appointment -> status == null || status.equals("All") || status.equals(appointment.getStatus()))
                .filter(appointment -> date == null || date.equals(appointment.getDate()))
                .sorted(Comparator.comparing(Appointment::getDate).thenComparing(Appointment::getTime))
                .toList());
    }

    public ObservableList<Appointment> appointmentsOn(LocalDate date) {
        return search("", "All", "All", date);
    }

    public List<Appointment> upcoming(int limit) {
        return appointmentRepository.findAll().stream()
                .filter(appointment -> appointment.getDate() != null && !appointment.getDate().isBefore(LocalDate.now()))
                .sorted(Comparator.comparing(Appointment::getDate).thenComparing(Appointment::getTime))
                .limit(limit)
                .toList();
    }

    public long countToday() {
        LocalDate today = LocalDate.now();
        return appointmentRepository.findAll().stream()
                .filter(appointment -> today.equals(appointment.getDate()))
                .filter(appointment -> !"Cancelled".equals(appointment.getStatus()))
                .count();
    }

    public List<String> specialties() {
        return DOCTORS_BY_SPECIALTY.keySet().stream().toList();
    }

    public List<String> doctorsFor(String specialty) {
        return DOCTORS_BY_SPECIALTY.getOrDefault(specialty, List.of());
    }

    public List<String> availableDoctors(String specialty, LocalDate date, LocalTime time) {
        return doctorsFor(specialty).stream()
                .filter(doctor -> appointmentRepository.findAll().stream().noneMatch(appointment ->
                        appointment.getDoctor().equals(doctor)
                                && appointment.getDate().equals(date)
                                && appointment.getTime().equals(time)
                                && !"Cancelled".equals(appointment.getStatus())))
                .toList();
    }

    public Map<String, Integer> countBySpecialty() {
        Map<String, Integer> result = new LinkedHashMap<>();
        specialties().forEach(specialty -> result.put(specialty, 0));
        appointmentRepository.findAll().forEach(appointment ->
                result.computeIfPresent(appointment.getSpecialty(), (key, value) -> value + 1));
        return result;
    }

    public Map<String, Integer> countByStatus() {
        Map<String, Integer> result = new LinkedHashMap<>();
        STATUSES.forEach(status -> result.put(status, 0));
        appointmentRepository.findAll().forEach(appointment ->
                result.computeIfPresent(appointment.getStatus(), (key, value) -> value + 1));
        return result;
    }

    public Map<String, Integer> countByDay(int days) {
        Map<String, Integer> result = new LinkedHashMap<>();
        LocalDate start = LocalDate.now().minusDays(Math.max(0, days - 1));
        for (int i = 0; i < days; i++) {
            LocalDate date = start.plusDays(i);
            result.put(date.getDayOfWeek().toString().substring(0, 3), 0);
        }
        appointmentRepository.findAll().stream()
                .filter(appointment -> appointment.getDate() != null)
                .filter(appointment -> !appointment.getDate().isBefore(start))
                .filter(appointment -> !appointment.getDate().isAfter(LocalDate.now()))
                .forEach(appointment -> {
                    String key = appointment.getDate().getDayOfWeek().toString().substring(0, 3);
                    result.computeIfPresent(key, (day, value) -> value + 1);
                });
        return result;
    }

    private void validate(Appointment appointment) {
        if (appointment.getPatientId() <= 0) {
            throw new IllegalArgumentException("Select a patient for the appointment.");
        }
        if (appointment.getDate() == null || appointment.getTime() == null) {
            throw new IllegalArgumentException("Appointment date and time are required.");
        }
        if (appointment.getDoctor() == null || appointment.getDoctor().isBlank()) {
            throw new IllegalArgumentException("Doctor is required.");
        }
        if (appointment.getSpecialty() == null || appointment.getSpecialty().isBlank()) {
            throw new IllegalArgumentException("Specialty is required.");
        }
        if (appointment.getStatus() == null || !STATUSES.contains(appointment.getStatus())) {
            throw new IllegalArgumentException("Appointment status is invalid.");
        }
    }

    private void ensureNoConflict(Appointment candidate, int ignoreId) {
        boolean conflict = appointmentRepository.findAll().stream().anyMatch(existing ->
                existing.getId() != ignoreId
                        && !"Cancelled".equals(existing.getStatus())
                        && existing.getDoctor().equalsIgnoreCase(candidate.getDoctor())
                        && existing.getDate().equals(candidate.getDate())
                        && existing.getTime().equals(candidate.getTime()));
        if (conflict) {
            throw new IllegalArgumentException("Scheduling conflict: this doctor already has an appointment at that time.");
        }
    }

    private void enrichPatientName(Appointment appointment) {
        Patient patient = patientRepository.findById(appointment.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found."));
        appointment.setPatientName(patient.getFullName());
    }

    private boolean matchesKeyword(Appointment appointment, String keyword) {
        if (keyword.isBlank()) {
            return true;
        }
        return normalize(appointment.getPatientName()).contains(keyword)
                || normalize(appointment.getDoctor()).contains(keyword)
                || normalize(appointment.getSpecialty()).contains(keyword)
                || normalize(appointment.getNotes()).contains(keyword);
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }
}
