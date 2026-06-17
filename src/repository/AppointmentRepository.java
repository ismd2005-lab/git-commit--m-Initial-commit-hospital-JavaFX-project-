package repository;

import javafx.collections.ObservableList;
import model.Appointment;

import java.util.Optional;

public interface AppointmentRepository {
    ObservableList<Appointment> findAll();

    Optional<Appointment> findById(int id);

    Appointment save(Appointment appointment);

    Appointment update(Appointment appointment);

    void delete(int id);
}
