package controllers;

import dao.PatientDAO;
import dao.RendezVousDAO;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import models.RendezVous;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DashboardController {
    private final PatientDAO patientDAO = new PatientDAO();
    private final RendezVousDAO rendezVousDAO = new RendezVousDAO();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private BorderPane rootPane;
    @FXML private StackPane contentPane;
    @FXML private VBox dashboardHome;
    @FXML private Button dashboardNavButton;
    @FXML private Button patientsNavButton;
    @FXML private Button rendezVousNavButton;
    @FXML private Button statsNavButton;
    @FXML private Button exportNavButton;
    @FXML private Button settingsNavButton;
    @FXML private Label totalPatientsLabel;
    @FXML private Label todayAppointmentsLabel;
    @FXML private Label activePatientsLabel;
    @FXML private Label healthScoreLabel;
    @FXML private Label connectionStatusLabel;
    @FXML private ProgressBar statsProgressBar;
    @FXML private ProgressBar healthProgressBar;
    @FXML private LineChart<String, Number> patientsLineChart;
    @FXML private PieChart genderPieChart;
    @FXML private ListView<String> upcomingAppointmentsList;
    @FXML private ColorPicker themePicker;

    @FXML
    private void initialize() {
        themePicker.setValue(Color.web("#1A8BFF"));
        markActive(dashboardNavButton);
        animate(dashboardHome);
        Platform.runLater(this::refreshDashboard);
    }

    @FXML
    private void showDashboard() {
        contentPane.getChildren().setAll(dashboardHome);
        markActive(dashboardNavButton);
        animate(dashboardHome);
        refreshDashboard();
    }

    @FXML
    private void showPatients() {
        loadCenterView("/views/patients.fxml", patientsNavButton);
    }

    @FXML
    private void showRendezVous() {
        loadCenterView("/views/rendezvous.fxml", rendezVousNavButton);
    }

    @FXML
    private void showStats() {
        contentPane.getChildren().setAll(dashboardHome);
        markActive(statsNavButton);
        animate(dashboardHome);
        refreshDashboard();
    }

    @FXML
    private void showExport() {
        markActive(exportNavButton);
        try {
            List<List<String>> rows = List.of(
                    List.of("Patients total", totalPatientsLabel.getText()),
                    List.of("Rendez-vous aujourd'hui", todayAppointmentsLabel.getText()),
                    List.of("Patients actifs", activePatientsLabel.getText()),
                    List.of("Indice santé global", healthScoreLabel.getText())
            );
            File file = CsvExporter.exportRows(
                    rootPane.getScene().getWindow(),
                    "medvision-dashboard.csv",
                    List.of("Indicateur", "Valeur"),
                    rows
            );
            if (file != null) {
                showInfo("Export terminé", "Le fichier CSV a été généré :\n" + file.getAbsolutePath());
            }
        } catch (IOException exception) {
            showError("Export impossible", exception.getMessage());
        }
    }

    @FXML
    private void showSettings() {
        markActive(settingsNavButton);
        showInfo("Paramètres", "Utilisez le sélecteur de couleur en bas du menu pour personnaliser l'accent visuel.");
    }

    @FXML
    private void handleThemeChanged() {
        Color color = themePicker.getValue();
        rootPane.setStyle("-med-accent: " + toHex(color) + ";");
    }

    @FXML
    private void refreshDashboard() {
        statsProgressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        try {
            int totalPatients = patientDAO.countAll();
            int activePatients = patientDAO.countActive();
            int todayAppointments = rendezVousDAO.countToday();

            totalPatientsLabel.setText(String.valueOf(totalPatients));
            todayAppointmentsLabel.setText(String.valueOf(todayAppointments));
            activePatientsLabel.setText(String.valueOf(activePatients));

            double healthScore = totalPatients == 0
                    ? 0
                    : Math.min(1.0, (activePatients / (double) totalPatients) * 0.75 + Math.min(todayAppointments / 20.0, 0.25));
            healthProgressBar.setProgress(healthScore);
            healthScoreLabel.setText(Math.round(healthScore * 100) + "%");

            loadPatientsChart(patientDAO.countCreatedByMonth(6));
            loadGenderChart(patientDAO.countBySexe());
            loadUpcomingAppointments();

            connectionStatusLabel.setText("Base connectée - données synchronisées");
            statsProgressBar.setProgress(1);
        } catch (SQLException exception) {
            totalPatientsLabel.setText("0");
            todayAppointmentsLabel.setText("0");
            activePatientsLabel.setText("0");
            healthScoreLabel.setText("0%");
            healthProgressBar.setProgress(0);
            patientsLineChart.getData().clear();
            genderPieChart.setData(FXCollections.observableArrayList());
            upcomingAppointmentsList.setItems(FXCollections.observableArrayList("Connexion MySQL indisponible"));
            connectionStatusLabel.setText("Base non connectée - vérifiez MySQL et init_db.sql");
            statsProgressBar.setProgress(0);
        }
    }

    @FXML
    private void handleAbout() {
        showInfo(
                "MedVision AI",
                "Dashboard JavaFX MVC pour la gestion intelligente d'une clinique médicale."
        );
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    private void loadPatientsChart(Map<String, Integer> monthlyPatients) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Patients");
        monthlyPatients.forEach((month, total) -> series.getData().add(new XYChart.Data<>(month, total)));
        patientsLineChart.getData().setAll(series);
    }

    private void loadGenderChart(Map<String, Integer> genderStats) {
        genderPieChart.setData(FXCollections.observableArrayList(
                genderStats.entrySet()
                        .stream()
                        .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                        .toList()
        ));
    }

    private void loadUpcomingAppointments() throws SQLException {
        List<String> upcoming = rendezVousDAO.findUpcoming(6)
                .stream()
                .map(this::formatAppointment)
                .toList();
        upcomingAppointmentsList.setItems(FXCollections.observableArrayList(
                upcoming.isEmpty() ? List.of("Aucun rendez-vous planifié") : upcoming
        ));
    }

    private String formatAppointment(RendezVous rendezVous) {
        String date = rendezVous.getDateRdv() == null ? "-" : rendezVous.getDateRdv().format(dateFormatter);
        return date + "  " + rendezVous.getHeure() + "  •  "
                + rendezVous.getPatientNomComplet() + "  •  " + rendezVous.getMedecin();
    }

    private void loadCenterView(String resource, Button activeButton) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(resource));
            contentPane.getChildren().setAll(view);
            markActive(activeButton);
            animate(view);
        } catch (IOException exception) {
            showError("Vue indisponible", "Impossible de charger " + resource + "\n" + exception.getMessage());
        }
    }

    private void markActive(Button activeButton) {
        List<Button> buttons = List.of(
                dashboardNavButton,
                patientsNavButton,
                rendezVousNavButton,
                statsNavButton,
                exportNavButton,
                settingsNavButton
        );
        buttons.forEach(button -> button.getStyleClass().remove("nav-button-active"));
        activeButton.getStyleClass().add("nav-button-active");
    }

    private void animate(Parent view) {
        view.setOpacity(0);
        FadeTransition transition = new FadeTransition(Duration.millis(320), view);
        transition.setFromValue(0);
        transition.setToValue(1);
        transition.play();
    }

    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
                Math.round(color.getRed() * 255),
                Math.round(color.getGreen() * 255),
                Math.round(color.getBlue() * 255));
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
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
