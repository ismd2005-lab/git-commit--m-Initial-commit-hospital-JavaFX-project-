package repository;

import javafx.collections.ObservableList;
import model.MedicalEvent;
import model.Patient;

import java.util.Optional;

public interface PatientRepository {
    ObservableList<Patient> findAll();

    Optional<Patient> findById(int id);

    Patient save(Patient patient);

    Patient update(Patient patient);

    void delete(int id);

    ObservableList<MedicalEvent> findTimelineByPatientId(int patientId);

    MedicalEvent addTimelineEvent(MedicalEvent event);
}
