package controller;

import config.ApplicationContext;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Window;
import javafx.util.StringConverter;
import model.Appointment;
import model.Patient;
import service.AppointmentService;
import service.PatientService;
import util.AlertFactory;
import util.CsvExporter;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentController {
    private final ApplicationContext context = ApplicationContext.getInstance();
    private final AppointmentService appointmentService = context.appointmentService();
    private final PatientService patientService = context.patientService();

    @FXML private ComboBox<Patient> patientCombo;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeCombo;
    @FXML private ComboBox<String> specialtyCombo;
    @FXML private ComboBox<String> doctorCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextArea notesArea;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterSpecialtyCombo;
    @FXML private ComboBox<String> filterStatusCombo;
    @FXML private DatePicker filterDatePicker;
    @FXML private DatePicker calendarDatePicker;
    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, String> patientColumn;
    @FXML private TableColumn<Appointment, String> dateColumn;
    @FXML private TableColumn<Appointment, String> timeColumn;
    @FXML private TableColumn<Appointment, String> doctorColumn;
    @FXML private TableColumn<Appointment, String> statusColumn;
    @FXML private ListView<String> dayScheduleList;
    @FXML private ListView<String> availableDoctorsList;
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        setupControls();
        setupTable();
        setupListeners();
        refreshAppointments();
        refreshDaySchedule();
        clearForm();
    }

    @FXML
    private void handleAddAppointment() {
        try {
            appointmentService.addAppointment(buildAppointment(0));
            refreshAppointments();
            refreshDaySchedule();
            clearForm();
            statusLabel.setText("Appointment added.");
        } catch (IllegalArgumentException exception) {
            AlertFactory.warning("Scheduling", exception.getMessage());
        }
    }

    @FXML
    private void handleUpdateAppointment() {
        Appointment selected = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertFactory.warning("Selection required", "Select an appointment to update.");
            return;
        }
        try {
            appointmentService.updateAppointment(buildAppointment(selected.getId()));
            refreshAppointments();
            refreshDaySchedule();
            statusLabel.setText("Appointment updated.");
        } catch (IllegalArgumentException exception) {
            AlertFactory.warning("Scheduling", exception.getMessage());
        }
    }

    @FXML
    private void handleDeleteAppointment() {
        Appointment selected = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertFactory.warning("Selection required", "Select an appointment to delete.");
            return;
        }
        if (AlertFactory.confirm("Delete appointment", "Delete appointment for " + selected.getPatientName() + "?",
                "Delete")) {
            appointmentService.deleteAppointment(selected.getId());
            refreshAppointments();
            refreshDaySchedule();
            clearForm();
            statusLabel.setText("Appointment deleted.");
        }
    }

    @FXML
    private void handleClearAppointment() {
        clearForm();
    }

    @FXML
    private void handleExportAppointments() {
        try {
            File file = CsvExporter.exportTable(appointmentsTable, window(), "medvision-appointments.csv");
            if (file != null) {
                statusLabel.setText("CSV exported: " + file.getName());
            }
        } catch (IOException exception) {
            AlertFactory.error("Export failed", exception.getMessage());
        }
    }

    @FXML
    private void handleClearDateFilter() {
        filterDatePicker.setValue(null);
        refreshAppointments();
    }

    private void setupControls() {
        patientCombo.setItems(patientService.findAll());
        patientCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Patient patient) {
                return patient == null ? "" : patient.getFullName();
            }

            @Override
            public Patient fromString(String string) {
                return null;
            }
        });
        if (!patientCombo.getItems().isEmpty()) {
            patientCombo.getSelectionModel().selectFirst();
        }
        datePicker.setValue(LocalDate.now());
        calendarDatePicker.setValue(LocalDate.now());
        timeCombo.setItems(FXCollections.observableArrayList(timeSlots()));
        timeCombo.setValue("09:00");
        specialtyCombo.setItems(FXCollections.observableArrayList(appointmentService.specialties()));
        specialtyCombo.getSelectionModel().selectFirst();
        doctorCombo.setItems(FXCollections.observableArrayList(
                appointmentService.doctorsFor(specialtyCombo.getValue())));
        doctorCombo.getSelectionModel().selectFirst();
        statusCombo.setItems(FXCollections.observableArrayList(AppointmentService.STATUSES));
        statusCombo.getSelectionModel().selectFirst();
        filterSpecialtyCombo.setItems(FXCollections.observableArrayList("All"));
        filterSpecialtyCombo.getItems().addAll(appointmentService.specialties());
        filterSpecialtyCombo.getSelectionModel().selectFirst();
        filterStatusCombo.setItems(FXCollections.observableArrayList("All"));
        filterStatusCombo.getItems().addAll(AppointmentService.STATUSES);
        filterStatusCombo.getSelectionModel().selectFirst();
    }

    private void setupTable() {
        patientColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPatientName()));
        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getDate())));
        timeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTimeLabel()));
        doctorColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDoctor()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
    }

    private void setupListeners() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> refreshAppointments());
        filterSpecialtyCombo.valueProperty().addListener((observable, oldValue, newValue) -> refreshAppointments());
        filterStatusCombo.valueProperty().addListener((observable, oldValue, newValue) -> refreshAppointments());
        filterDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> refreshAppointments());
        calendarDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> refreshDaySchedule());
        specialtyCombo.valueProperty().addListener((observable, oldValue, specialty) -> {
            doctorCombo.setItems(FXCollections.observableArrayList(appointmentService.doctorsFor(specialty)));
            doctorCombo.getSelectionModel().selectFirst();
            refreshAvailableDoctors();
        });
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> refreshAvailableDoctors());
        timeCombo.valueProperty().addListener((observable, oldValue, newValue) -> refreshAvailableDoctors());
        appointmentsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, appointment) -> {
            if (appointment != null) {
                fillForm(appointment);
            }
        });
    }

    private void refreshAppointments() {
        appointmentsTable.setItems(appointmentService.search(searchField.getText(), filterSpecialtyCombo.getValue(),
                filterStatusCombo.getValue(), filterDatePicker.getValue()));
        statusLabel.setText(appointmentsTable.getItems().size() + " appointment(s) displayed.");
    }

    private void refreshDaySchedule() {
        LocalDate date = calendarDatePicker.getValue() == null ? LocalDate.now() : calendarDatePicker.getValue();
        dayScheduleList.setItems(FXCollections.observableArrayList(appointmentService.appointmentsOn(date).stream()
                .map(appointment -> appointment.getTimeLabel() + " | " + appointment.getPatientName()
                        + " | " + appointment.getDoctor() + " | " + appointment.getStatus())
                .toList()));
    }

    private void refreshAvailableDoctors() {
        if (specialtyCombo.getValue() == null || datePicker.getValue() == null || timeCombo.getValue() == null) {
            return;
        }
        availableDoctorsList.setItems(FXCollections.observableArrayList(appointmentService.availableDoctors(
                specialtyCombo.getValue(), datePicker.getValue(), LocalTime.parse(timeCombo.getValue()))));
    }

    private Appointment buildAppointment(int id) {
        Patient patient = patientCombo.getValue();
        int patientId = patient == null ? 0 : patient.getId();
        return new Appointment(id, patientId, patient == null ? "" : patient.getFullName(),
                datePicker.getValue(), LocalTime.parse(timeCombo.getValue()),
                doctorCombo.getValue(), specialtyCombo.getValue(), statusCombo.getValue(),
                notesArea.getText().trim());
    }

    private void fillForm(Appointment appointment) {
        patientCombo.getItems().stream()
                .filter(patient -> patient.getId() == appointment.getPatientId())
                .findFirst()
                .ifPresent(patient -> patientCombo.getSelectionModel().select(patient));
        datePicker.setValue(appointment.getDate());
        timeCombo.setValue(appointment.getTimeLabel());
        specialtyCombo.setValue(appointment.getSpecialty());
        doctorCombo.setValue(appointment.getDoctor());
        statusCombo.setValue(appointment.getStatus());
        notesArea.setText(appointment.getNotes());
        refreshAvailableDoctors();
    }

    private void clearForm() {
        appointmentsTable.getSelectionModel().clearSelection();
        if (!patientCombo.getItems().isEmpty()) {
            patientCombo.getSelectionModel().selectFirst();
        }
        datePicker.setValue(LocalDate.now());
        timeCombo.setValue("09:00");
        specialtyCombo.getSelectionModel().selectFirst();
        statusCombo.getSelectionModel().selectFirst();
        notesArea.clear();
        refreshAvailableDoctors();
    }

    private List<String> timeSlots() {
        List<String> slots = new ArrayList<>();
        for (int hour = 8; hour <= 17; hour++) {
            slots.add(String.format("%02d:00", hour));
            slots.add(String.format("%02d:30", hour));
        }
        return slots;
    }

    private Window window() {
        return appointmentsTable.getScene().getWindow();
    }
}
