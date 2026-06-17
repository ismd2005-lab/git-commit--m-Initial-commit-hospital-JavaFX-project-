package service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.SearchResult;
import repository.AppointmentRepository;
import repository.InvoiceRepository;
import repository.PatientRepository;

import java.util.Locale;
import java.util.stream.Stream;

public class GlobalSearchService {
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final InvoiceRepository invoiceRepository;

    public GlobalSearchService(PatientRepository patientRepository, AppointmentRepository appointmentRepository,
                               InvoiceRepository invoiceRepository) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public ObservableList<SearchResult> search(String keyword) {
        String query = keyword == null ? "" : keyword.toLowerCase(Locale.ROOT).trim();
        if (query.isBlank()) {
            return FXCollections.observableArrayList();
        }

        Stream<SearchResult> patients = patientRepository.findAll().stream()
                .filter(patient -> contains(patient.getFullName(), query)
                        || contains(patient.getPhone(), query)
                        || contains(patient.getMedicalNotes(), query))
                .map(patient -> new SearchResult("Patient", patient.getFullName(),
                        patient.getStatusLabel() + " | " + patient.getPhone()));

        Stream<SearchResult> appointments = appointmentRepository.findAll().stream()
                .filter(appointment -> contains(appointment.getPatientName(), query)
                        || contains(appointment.getDoctor(), query)
                        || contains(appointment.getSpecialty(), query))
                .map(appointment -> new SearchResult("Appointment", appointment.getPatientName(),
                        appointment.getScheduleLabel() + " | " + appointment.getDoctor()));

        Stream<SearchResult> invoices = invoiceRepository.findAll().stream()
                .filter(invoice -> contains(invoice.getPatientName(), query)
                        || contains(invoice.getDescription(), query)
                        || String.valueOf(invoice.getId()).contains(query))
                .map(invoice -> new SearchResult("Invoice", "#" + invoice.getId() + " " + invoice.getPatientName(),
                        invoice.getAmountLabel() + " | " + invoice.getStatus()));

        return FXCollections.observableArrayList(Stream.of(patients, appointments, invoices)
                .flatMap(stream -> stream)
                .limit(12)
                .toList());
    }

    private boolean contains(String source, String query) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(query);
    }
}
