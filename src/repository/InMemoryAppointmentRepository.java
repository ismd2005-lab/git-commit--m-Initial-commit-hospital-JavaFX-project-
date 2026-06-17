package repository;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Appointment;
import model.Patient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public class InMemoryAppointmentRepository implements AppointmentRepository {
    private final ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    private int nextId = 1;

    public InMemoryAppointmentRepository(ObservableList<Patient> patients) {
        seedAppointments(patients);
    }

    @Override
    public ObservableList<Appointment> findAll() {
        return appointments;
    }

    @Override
    public Optional<Appointment> findById(int id) {
        return appointments.stream().filter(appointment -> appointment.getId() == id).findFirst();
    }

    @Override
    public Appointment save(Appointment appointment) {
        appointment.setId(nextId++);
        appointments.add(appointment);
        return appointment;
    }

    @Override
    public Appointment update(Appointment appointment) {
        for (int i = 0; i < appointments.size(); i++) {
            if (appointments.get(i).getId() == appointment.getId()) {
                appointments.set(i, appointment);
                return appointment;
            }
        }
        throw new IllegalArgumentException("Appointment not found.");
    }

    @Override
    public void delete(int id) {
        appointments.removeIf(appointment -> appointment.getId() == id);
    }

    private void seedAppointments(ObservableList<Patient> patients) {
        save(new Appointment(0, 1, nameOf(patients, 1), LocalDate.now(), LocalTime.of(9, 30),
                "Dr. Lina Berrada", "Cardiology", "Confirmed", "Check ECG and tension."));
        save(new Appointment(0, 2, nameOf(patients, 2), LocalDate.now().plusDays(1), LocalTime.of(11, 0),
                "Dr. Amine Saidi", "Surgery", "Pending", "Wound healing review."));
        save(new Appointment(0, 3, nameOf(patients, 3), LocalDate.now().plusDays(2), LocalTime.of(14, 15),
                "Dr. Salma Naciri", "Dermatology", "Pending", "Skin analysis."));
        save(new Appointment(0, 4, nameOf(patients, 4), LocalDate.now().plusDays(3), LocalTime.of(10, 45),
                "Dr. Omar Rami", "Endocrinology", "Confirmed", "Glucose control."));
        save(new Appointment(0, 5, nameOf(patients, 5), LocalDate.now().plusDays(5), LocalTime.of(16, 0),
                "Dr. Ines Tazi", "Neurology", "Confirmed", "Cognitive review."));
    }

    private String nameOf(ObservableList<Patient> patients, int id) {
        return patients.stream()
                .filter(patient -> patient.getId() == id)
                .findFirst()
                .map(Patient::getFullName)
                .orElse("Patient #" + id);
    }
}
