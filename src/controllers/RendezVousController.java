package controllers;

import dao.PatientDAO;
import dao.RendezVousDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import models.Patient;
import models.RendezVous;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class RendezVousController {
    private static final Pattern HOUR_PATTERN = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");
    private static final List<String> SPECIALITES = List.of(
            "Cardiologie",
            "Dermatologie",
            "Endocrinologie",
            "Neurologie",
            "Pédiatrie",
            "Radiologie",
            "Chirurgie",
            "Médecine générale"
    );
    private static final List<String> STATUTS = List.of("Planifié", "Confirmé", "En attente", "Terminé", "Annulé");
    private static final Map<String, List<String>> MEDECINS = Map.of(
            "Cardiologie", List.of("Dr. Lina Berrada", "Dr. Sami Karim"),
            "Dermatologie", List.of("Dr. Salma Naciri", "Dr. Amal Kettani"),
            "Endocrinologie", List.of("Dr. Omar Rami", "Dr. Rania Fassi"),
            "Neurologie", List.of("Dr. Ines Tazi", "Dr. Mehdi Lahlou"),
            "Pédiatrie", List.of("Dr. Yasmine Barakat", "Dr. Adam Bennani"),
            "Radiologie", List.of("Dr. Sofia El Fassi", "Dr. Walid Nadif"),
            "Chirurgie", List.of("Dr. Amine Saidi", "Dr. Noura El Ghazali"),
            "Médecine générale", List.of("Dr. Hajar Alaoui", "Dr. Anas Bakkali")
    );

    private final PatientDAO patientDAO = new PatientDAO();
    private final RendezVousDAO rendezVousDAO = new RendezVousDAO();

    @FXML private ComboBox<Patient> patientComboBox;
    @FXML private DatePicker dateRdvPicker;
    @FXML private TextField heureField;
    @FXML private ComboBox<String> specialiteCombo;
    @FXML private TextField medecinField;
    @FXML private ListView<String> medecinsListView;
    @FXML private ComboBox<String> statutCombo;
    @FXML private TextArea remarqueArea;
    @FXML private TextField searchRdvField;
    @FXML private ComboBox<String> filterSpecialiteCombo;
    @FXML private ComboBox<String> filterStatutCombo;
    @FXML private TableView<RendezVous> rendezVousTable;
    @FXML private TableColumn<RendezVous, String> patientColumn;
    @FXML private TableColumn<RendezVous, LocalDate> dateColumn;
    @FXML private TableColumn<RendezVous, String> medecinColumn;
    @FXML private TableColumn<RendezVous, String> statutColumn;
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        setupComboBoxes();
        setupColumns();
        setupListeners();
        loadPatients();
        loadRendezVous();
        dateRdvPicker.setValue(LocalDate.now());
        heureField.setText("09:00");
    }

    @FXML
    private void handleAddRendezVous() {
        RendezVous rendezVous = buildRendezVousFromForm();
        if (rendezVous == null) {
            return;
        }

        try {
            rendezVousDAO.save(rendezVous);
            loadRendezVous();
            clearForm();
            statusLabel.setText("Rendez-vous ajouté avec succès.");
        } catch (SQLException exception) {
            showError("Ajout impossible", exception.getMessage());
        }
    }

    @FXML
    private void handleUpdateRendezVous() {
        RendezVous selected = rendezVousTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Sélection requise", "Sélectionnez un rendez-vous à modifier.");
            return;
        }

        RendezVous rendezVous = buildRendezVousFromForm();
        if (rendezVous == null) {
            return;
        }
        rendezVous.setIdRdv(selected.getIdRdv());

        try {
            rendezVousDAO.update(rendezVous);
            loadRendezVous();
            statusLabel.setText("Rendez-vous modifié avec succès.");
        } catch (SQLException exception) {
            showError("Modification impossible", exception.getMessage());
        }
    }

    @FXML
    private void handleDeleteRendezVous() {
        RendezVous selected = rendezVousTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Sélection requise", "Sélectionnez un rendez-vous à supprimer.");
            return;
        }

        ButtonType deleteButton = new ButtonType("Supprimer", ButtonBar.ButtonData.OK_DONE);
        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Supprimer le rendez-vous de " + selected.getPatientNomComplet() + " ?",
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
            rendezVousDAO.delete(selected.getIdRdv());
            loadRendezVous();
            clearForm();
            statusLabel.setText("Rendez-vous supprimé.");
        } catch (SQLException exception) {
            showError("Suppression impossible", exception.getMessage());
        }
    }

    @FXML
    private void handleSearchRendezVous() {
        loadRendezVous();
    }

    @FXML
    private void handleExportRendezVous() {
        try {
            File file = CsvExporter.exportTable(rendezVousTable, rendezVousTable.getScene().getWindow(), "medvision-rendezvous.csv");
            if (file != null) {
                statusLabel.setText("Export CSV généré : " + file.getName());
            }
        } catch (IOException exception) {
            showError("Export impossible", exception.getMessage());
        }
    }

    @FXML
    private void handleClearRendezVous() {
        clearForm();
    }

    private void setupComboBoxes() {
        specialiteCombo.setItems(FXCollections.observableArrayList(SPECIALITES));
        specialiteCombo.getSelectionModel().selectFirst();

        statutCombo.setItems(FXCollections.observableArrayList(STATUTS));
        statutCombo.getSelectionModel().selectFirst();

        filterSpecialiteCombo.setItems(FXCollections.observableArrayList("Toutes"));
        filterSpecialiteCombo.getItems().addAll(SPECIALITES);
        filterSpecialiteCombo.getSelectionModel().selectFirst();

        filterStatutCombo.setItems(FXCollections.observableArrayList("Tous"));
        filterStatutCombo.getItems().addAll(STATUTS);
        filterStatutCombo.getSelectionModel().selectFirst();

        patientComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Patient patient) {
                return patient == null ? "" : patient.getNomComplet();
            }

            @Override
            public Patient fromString(String string) {
                return null;
            }
        });
    }

    private void setupColumns() {
        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patientNomComplet"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateRdv"));
        medecinColumn.setCellValueFactory(new PropertyValueFactory<>("medecin"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
    }

    private void setupListeners() {
        searchRdvField.textProperty().addListener((observable, oldValue, newValue) -> loadRendezVous());
        filterSpecialiteCombo.valueProperty().addListener((observable, oldValue, newValue) -> loadRendezVous());
        filterStatutCombo.valueProperty().addListener((observable, oldValue, newValue) -> loadRendezVous());

        specialiteCombo.valueProperty().addListener((observable, oldValue, specialite) -> updateDoctors(specialite));
        medecinsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, medecin) -> {
            if (medecin != null) {
                medecinField.setText(medecin);
            }
        });

        rendezVousTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selected) -> {
            if (selected != null) {
                fillForm(selected);
            }
        });
    }

    private void loadPatients() {
        try {
            patientComboBox.setItems(FXCollections.observableArrayList(patientDAO.findAll()));
            if (!patientComboBox.getItems().isEmpty()) {
                patientComboBox.getSelectionModel().selectFirst();
            }
        } catch (SQLException exception) {
            patientComboBox.setItems(FXCollections.observableArrayList());
            statusLabel.setText("Impossible de charger les patients.");
        }
    }

    private void loadRendezVous() {
        try {
            rendezVousTable.setItems(FXCollections.observableArrayList(
                    rendezVousDAO.findByCriteria(
                            searchRdvField.getText(),
                            filterSpecialiteCombo.getValue(),
                            filterStatutCombo.getValue()
                    )
            ));
            statusLabel.setText(rendezVousTable.getItems().size() + " rendez-vous affiché(s).");
        } catch (SQLException exception) {
            rendezVousTable.setItems(FXCollections.observableArrayList());
            statusLabel.setText("Connexion MySQL indisponible.");
        }
    }

    private void updateDoctors(String specialite) {
        List<String> doctors = MEDECINS.getOrDefault(specialite, List.of());
        medecinsListView.setItems(FXCollections.observableArrayList(doctors));
        if (!doctors.isEmpty()) {
            medecinsListView.getSelectionModel().selectFirst();
            medecinField.setText(doctors.get(0));
        }
    }

    private RendezVous buildRendezVousFromForm() {
        Patient patient = patientComboBox.getValue();
        String heure = heureField.getText().trim();
        String medecin = medecinField.getText().trim();
        String specialite = specialiteCombo.getValue();
        String statut = statutCombo.getValue();

        if (patient == null) {
            showWarning("Patient requis", "Choisissez un patient pour le rendez-vous.");
            return null;
        }
        if (dateRdvPicker.getValue() == null) {
            showWarning("Date requise", "Choisissez une date de rendez-vous.");
            return null;
        }
        if (!HOUR_PATTERN.matcher(heure).matches()) {
            showWarning("Heure invalide", "Utilisez le format HH:mm, par exemple 09:30.");
            return null;
        }
        if (medecin.isBlank() || specialite == null || statut == null) {
            showWarning("Champs requis", "La spécialité, le médecin et le statut sont obligatoires.");
            return null;
        }

        return new RendezVous(
                0,
                patient.getIdPatient(),
                patient.getNomComplet(),
                dateRdvPicker.getValue(),
                heure,
                medecin,
                specialite,
                statut,
                remarqueArea.getText().trim()
        );
    }

    private void fillForm(RendezVous rendezVous) {
        patientComboBox.getItems()
                .stream()
                .filter(patient -> patient.getIdPatient() == rendezVous.getIdPatient())
                .findFirst()
                .ifPresent(patient -> patientComboBox.getSelectionModel().select(patient));

        dateRdvPicker.setValue(rendezVous.getDateRdv());
        heureField.setText(rendezVous.getHeure());
        specialiteCombo.setValue(rendezVous.getSpecialite());
        medecinField.setText(rendezVous.getMedecin());
        statutCombo.setValue(rendezVous.getStatut());
        remarqueArea.setText(rendezVous.getRemarque());
    }

    private void clearForm() {
        rendezVousTable.getSelectionModel().clearSelection();
        if (!patientComboBox.getItems().isEmpty()) {
            patientComboBox.getSelectionModel().selectFirst();
        }
        dateRdvPicker.setValue(LocalDate.now());
        heureField.setText("09:00");
        specialiteCombo.getSelectionModel().selectFirst();
        statutCombo.getSelectionModel().selectFirst();
        remarqueArea.clear();
        updateDoctors(specialiteCombo.getValue());
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
