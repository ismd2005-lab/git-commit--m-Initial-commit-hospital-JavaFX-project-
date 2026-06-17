package controller;

import config.ApplicationContext;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Window;
import model.MedicalEvent;
import model.Patient;
import service.AiSimulationService;
import service.BillingService;
import service.PatientService;
import util.AlertFactory;
import util.CsvExporter;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class PatientController {
    private final ApplicationContext context = ApplicationContext.getInstance();
    private final PatientService patientService = context.patientService();
    private final AiSimulationService aiSimulationService = context.aiSimulationService();
    private final BillingService billingService = context.billingService();

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private Spinner<Integer> ageSpinner;
    @FXML private ComboBox<String> genderCombo;
    @FXML private ComboBox<String> bloodTypeCombo;
    @FXML private CheckBox activeCheckBox;
    @FXML private Slider prioritySlider;
    @FXML private Label priorityLabel;
    @FXML private TextArea notesArea;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> genderFilterCombo;
    @FXML private ComboBox<String> activeFilterCombo;
    @FXML private TableView<Patient> patientsTable;
    @FXML private TableColumn<Patient, String> nameColumn;
    @FXML private TableColumn<Patient, String> ageColumn;
    @FXML private TableColumn<Patient, String> phoneColumn;
    @FXML private TableColumn<Patient, String> priorityColumn;
    @FXML private TableColumn<Patient, String> statusColumn;
    @FXML private Label statusLabel;
    @FXML private Label profileNameLabel;
    @FXML private Label profileMetaLabel;
    @FXML private Label riskScoreLabel;
    @FXML private Label diagnosisLabel;
    @FXML private ListView<String> timelineList;

    @FXML
    private void initialize() {
        setupControls();
        setupTable();
        setupListeners();
        refreshPatients();
        clearForm();
    }

    @FXML
    private void handleAddPatient() {
        try {
            patientService.addPatient(buildPatient(0));
            refreshPatients();
            clearForm();
            statusLabel.setText("Patient added.");
        } catch (IllegalArgumentException exception) {
            AlertFactory.warning("Patient validation", exception.getMessage());
        }
    }

    @FXML
    private void handleUpdatePatient() {
        Patient selected = patientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertFactory.warning("Selection required", "Select a patient to update.");
            return;
        }
        try {
            patientService.updatePatient(buildPatient(selected.getId()));
            refreshPatients();
            statusLabel.setText("Patient updated.");
        } catch (IllegalArgumentException exception) {
            AlertFactory.warning("Patient validation", exception.getMessage());
        }
    }

    @FXML
    private void handleDeletePatient() {
        Patient selected = patientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertFactory.warning("Selection required", "Select a patient to delete.");
            return;
        }
        if (AlertFactory.confirm("Delete patient", "Delete " + selected.getFullName()
                + " with related appointments and invoices?", "Delete")) {
            patientService.deletePatient(selected.getId());
            refreshPatients();
            clearForm();
            statusLabel.setText("Patient deleted.");
        }
    }

    @FXML
    private void handleClearPatient() {
        clearForm();
    }

    @FXML
    private void handleExportPatients() {
        try {
            File file = CsvExporter.exportTable(patientsTable, window(), "medvision-patients.csv");
            if (file != null) {
                statusLabel.setText("CSV exported: " + file.getName());
            }
        } catch (IOException exception) {
            AlertFactory.error("Export failed", exception.getMessage());
        }
    }

    @FXML
    private void handleExportPatientReport() {
        Patient selected = patientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertFactory.warning("Selection required", "Select a patient to export a report.");
            return;
        }
        File file = CsvExporter.choosePdf(window(), "medvision-patient-" + selected.getId() + ".pdf");
        if (file == null) {
            return;
        }
        try {
            List<String> timeline = patientService.timelineFor(selected.getId()).stream()
                    .map(MedicalEvent::getTimelineLabel)
                    .toList();
            int risk = aiSimulationService.noShowRiskScore(selected);
            billingService.exportPatientReportPdf(selected, file, timeline, risk,
                    aiSimulationService.diagnosisSuggestion(selected));
            statusLabel.setText("Patient PDF exported: " + file.getName());
        } catch (IOException exception) {
            AlertFactory.error("PDF export failed", exception.getMessage());
        }
    }

    private void setupControls() {
        ageSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 130, 30));
        genderCombo.setItems(FXCollections.observableArrayList("Female", "Male", "Other"));
        genderCombo.getSelectionModel().selectFirst();
        bloodTypeCombo.setItems(FXCollections.observableArrayList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
        bloodTypeCombo.getSelectionModel().selectFirst();
        genderFilterCombo.setItems(FXCollections.observableArrayList("All", "Female", "Male", "Other"));
        genderFilterCombo.getSelectionModel().selectFirst();
        activeFilterCombo.setItems(FXCollections.observableArrayList("All", "Active", "Inactive"));
        activeFilterCombo.getSelectionModel().selectFirst();
        activeCheckBox.setSelected(true);
    }

    private void setupTable() {
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));
        ageColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getAge())));
        phoneColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));
        priorityColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPriorityScore() + "%"));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatusLabel()));
    }

    private void setupListeners() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> refreshPatients());
        genderFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> refreshPatients());
        activeFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> refreshPatients());
        prioritySlider.valueProperty().addListener((observable, oldValue, value) ->
                priorityLabel.setText(Math.round(value.doubleValue()) + "%"));
        patientsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, patient) -> {
            if (patient != null) {
                fillForm(patient);
                updateProfile(patient);
            }
        });
    }

    private void refreshPatients() {
        patientsTable.setItems(patientService.search(searchField.getText(), genderFilterCombo.getValue(),
                activeFilterCombo.getValue()));
        statusLabel.setText(patientsTable.getItems().size() + " patient(s) displayed.");
    }

    private Patient buildPatient(int id) {
        return new Patient(id,
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                ageSpinner.getValue(),
                genderCombo.getValue(),
                phoneField.getText().trim(),
                emailField.getText().trim(),
                bloodTypeCombo.getValue(),
                notesArea.getText().trim(),
                LocalDate.now(),
                activeCheckBox.isSelected(),
                (int) Math.round(prioritySlider.getValue()));
    }

    private void fillForm(Patient patient) {
        firstNameField.setText(patient.getFirstName());
        lastNameField.setText(patient.getLastName());
        phoneField.setText(patient.getPhone());
        emailField.setText(patient.getEmail());
        ageSpinner.getValueFactory().setValue(patient.getAge());
        genderCombo.setValue(patient.getGender());
        bloodTypeCombo.setValue(patient.getBloodType());
        activeCheckBox.setSelected(patient.isActive());
        prioritySlider.setValue(patient.getPriorityScore());
        notesArea.setText(patient.getMedicalNotes());
    }

    private void updateProfile(Patient patient) {
        int risk = aiSimulationService.noShowRiskScore(patient);
        profileNameLabel.setText(patient.getFullName());
        profileMetaLabel.setText(patient.getAge() + " years | " + patient.getGender()
                + " | " + patient.getBloodType() + " | " + patient.getStatusLabel());
        riskScoreLabel.setText(risk + "% " + aiSimulationService.riskBand(risk));
        diagnosisLabel.setText(aiSimulationService.diagnosisSuggestion(patient));
        timelineList.setItems(FXCollections.observableArrayList(patientService.timelineFor(patient.getId())
                .stream()
                .map(MedicalEvent::getTimelineLabel)
                .toList()));
    }

    private void clearForm() {
        patientsTable.getSelectionModel().clearSelection();
        firstNameField.clear();
        lastNameField.clear();
        phoneField.clear();
        emailField.clear();
        ageSpinner.getValueFactory().setValue(30);
        genderCombo.getSelectionModel().selectFirst();
        bloodTypeCombo.getSelectionModel().selectFirst();
        activeCheckBox.setSelected(true);
        prioritySlider.setValue(45);
        notesArea.clear();
        profileNameLabel.setText("Select a patient");
        profileMetaLabel.setText("Profile and medical timeline");
        riskScoreLabel.setText("--");
        diagnosisLabel.setText("No simulation selected.");
        timelineList.setItems(FXCollections.observableArrayList());
    }

    private Window window() {
        return patientsTable.getScene().getWindow();
    }
}
