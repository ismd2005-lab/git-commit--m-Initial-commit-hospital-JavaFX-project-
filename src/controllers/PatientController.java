package controllers;

import dao.PatientDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Patient;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.regex.Pattern;

public class PatientController {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");

    private final PatientDAO patientDAO = new PatientDAO();

    @FXML private Accordion patientAccordion;
    @FXML private TitledPane personalPane;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField telephoneField;
    @FXML private TextField emailField;
    @FXML private DatePicker birthDatePicker;
    @FXML private Spinner<Integer> ageSpinner;
    @FXML private RadioButton hommeRadio;
    @FXML private RadioButton femmeRadio;
    @FXML private ToggleGroup sexeGroup;
    @FXML private CheckBox actifCheckBox;
    @FXML private TextArea descriptionArea;
    @FXML private Slider prioritySlider;
    @FXML private Label priorityLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterSexeCombo;
    @FXML private ComboBox<String> filterActiveCombo;
    @FXML private TableView<Patient> patientsTable;
    @FXML private TableColumn<Patient, String> nomColumn;
    @FXML private TableColumn<Patient, String> prenomColumn;
    @FXML private TableColumn<Patient, Integer> ageColumn;
    @FXML private TableColumn<Patient, String> telephoneColumn;
    @FXML private TableColumn<Patient, String> statutColumn;
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        ageSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 130, 30));
        filterSexeCombo.setItems(FXCollections.observableArrayList("Tous", "Homme", "Femme"));
        filterSexeCombo.getSelectionModel().selectFirst();
        filterActiveCombo.setItems(FXCollections.observableArrayList("Tous", "Actifs", "Inactifs"));
        filterActiveCombo.getSelectionModel().selectFirst();
        actifCheckBox.setSelected(true);
        patientAccordion.setExpandedPane(personalPane);

        setupColumns();
        setupListeners();
        loadPatients();
    }

    @FXML
    private void handleAddPatient() {
        Patient patient = buildPatientFromForm();
        if (patient == null) {
            return;
        }

        try {
            patientDAO.save(patient);
            loadPatients();
            clearForm();
            statusLabel.setText("Patient ajouté avec succès.");
        } catch (SQLException exception) {
            showError("Ajout impossible", exception.getMessage());
        }
    }

    @FXML
    private void handleUpdatePatient() {
        Patient selected = patientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Sélection requise", "Sélectionnez un patient à modifier.");
            return;
        }

        Patient patient = buildPatientFromForm();
        if (patient == null) {
            return;
        }
        patient.setIdPatient(selected.getIdPatient());
        patient.setDateCreation(selected.getDateCreation());

        try {
            patientDAO.update(patient);
            loadPatients();
            statusLabel.setText("Patient modifié avec succès.");
        } catch (SQLException exception) {
            showError("Modification impossible", exception.getMessage());
        }
    }

    @FXML
    private void handleDeletePatient() {
        Patient selected = patientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Sélection requise", "Sélectionnez un patient à supprimer.");
            return;
        }

        ButtonType deleteButton = new ButtonType("Supprimer", ButtonBar.ButtonData.OK_DONE);
        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Supprimer le patient " + selected.getNomComplet() + " et ses rendez-vous associés ?",
                ButtonType.CANCEL,
                deleteButton
        );
        confirmation.setTitle("Confirmation suppression");
        confirmation.setHeaderText("Action irréversible");

        Optional<ButtonType> choice = confirmation.showAndWait();
        if (choice.isEmpty() || choice.get() != deleteButton) {
            return;
        }

        try {
            patientDAO.delete(selected.getIdPatient());
            loadPatients();
            clearForm();
            statusLabel.setText("Patient supprimé.");
        } catch (SQLException exception) {
            showError("Suppression impossible", exception.getMessage());
        }
    }

    @FXML
    private void handleSearchPatient() {
        loadPatients();
    }

    @FXML
    private void handleExportPatients() {
        try {
            File file = CsvExporter.exportTable(patientsTable, patientsTable.getScene().getWindow(), "medvision-patients.csv");
            if (file != null) {
                statusLabel.setText("Export CSV généré : " + file.getName());
            }
        } catch (IOException exception) {
            showError("Export impossible", exception.getMessage());
        }
    }

    @FXML
    private void handleClearPatient() {
        clearForm();
    }

    private void setupColumns() {
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        statutColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isActif() ? "Actif" : "Inactif"));
    }

    private void setupListeners() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> loadPatients());
        filterSexeCombo.valueProperty().addListener((observable, oldValue, newValue) -> loadPatients());
        filterActiveCombo.valueProperty().addListener((observable, oldValue, newValue) -> loadPatients());

        patientsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selected) -> {
            if (selected != null) {
                fillForm(selected);
            }
        });

        birthDatePicker.valueProperty().addListener((observable, oldValue, date) -> {
            if (date != null && !date.isAfter(LocalDate.now())) {
                int years = Period.between(date, LocalDate.now()).getYears();
                ageSpinner.getValueFactory().setValue(years);
            }
        });

        prioritySlider.valueProperty().addListener((observable, oldValue, newValue) ->
                priorityLabel.setText(Math.round(newValue.doubleValue()) + "/100"));
    }

    private void loadPatients() {
        try {
            patientsTable.setItems(FXCollections.observableArrayList(
                    patientDAO.findByCriteria(searchField.getText(), filterSexeCombo.getValue(), selectedActiveFilter())
            ));
            statusLabel.setText(patientsTable.getItems().size() + " patient(s) affiché(s).");
        } catch (SQLException exception) {
            patientsTable.setItems(FXCollections.observableArrayList());
            statusLabel.setText("Connexion MySQL indisponible.");
        }
    }

    private Boolean selectedActiveFilter() {
        String selected = filterActiveCombo.getValue();
        if ("Actifs".equals(selected)) {
            return true;
        }
        if ("Inactifs".equals(selected)) {
            return false;
        }
        return null;
    }

    private Patient buildPatientFromForm() {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String email = emailField.getText().trim();

        if (nom.isBlank() || prenom.isBlank()) {
            showWarning("Champs requis", "Le nom et le prénom sont obligatoires.");
            return null;
        }
        if (!email.isBlank() && !EMAIL_PATTERN.matcher(email).matches()) {
            showWarning("Email invalide", "Veuillez saisir une adresse email valide.");
            return null;
        }
        if (sexeGroup.getSelectedToggle() == null) {
            showWarning("Sexe requis", "Choisissez Homme ou Femme.");
            return null;
        }

        String sexe = hommeRadio.isSelected() ? "Homme" : "Femme";
        return new Patient(
                0,
                nom,
                prenom,
                ageSpinner.getValue(),
                sexe,
                telephone,
                email,
                descriptionArea.getText().trim(),
                LocalDate.now(),
                actifCheckBox.isSelected()
        );
    }

    private void fillForm(Patient patient) {
        nomField.setText(patient.getNom());
        prenomField.setText(patient.getPrenom());
        telephoneField.setText(patient.getTelephone());
        emailField.setText(patient.getEmail());
        ageSpinner.getValueFactory().setValue(patient.getAge());
        if ("Femme".equalsIgnoreCase(patient.getSexe())) {
            femmeRadio.setSelected(true);
        } else {
            hommeRadio.setSelected(true);
        }
        actifCheckBox.setSelected(patient.isActif());
        descriptionArea.setText(patient.getDescription());
    }

    private void clearForm() {
        patientsTable.getSelectionModel().clearSelection();
        nomField.clear();
        prenomField.clear();
        telephoneField.clear();
        emailField.clear();
        birthDatePicker.setValue(null);
        ageSpinner.getValueFactory().setValue(30);
        sexeGroup.selectToggle(null);
        actifCheckBox.setSelected(true);
        descriptionArea.clear();
        prioritySlider.setValue(45);
        priorityLabel.setText("45/100");
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
