package controller;

import config.ApplicationContext;
import config.DatabaseConfig;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.DashboardMetrics;
import model.NotificationItem;
import model.Patient;
import service.AiSimulationService;
import service.AppointmentService;
import service.BillingService;
import service.DashboardService;
import service.GlobalSearchService;
import service.NotificationService;
import service.PatientService;
import util.AlertFactory;
import util.ViewAnimations;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class DashboardController {
    private final ApplicationContext context = ApplicationContext.getInstance();
    private final DashboardService dashboardService = context.dashboardService();
    private final AppointmentService appointmentService = context.appointmentService();
    private final BillingService billingService = context.billingService();
    private final NotificationService notificationService = context.notificationService();
    private final GlobalSearchService globalSearchService = context.globalSearchService();
    private final PatientService patientService = context.patientService();
    private final AiSimulationService aiSimulationService = context.aiSimulationService();
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);
    private final DateTimeFormatter clockFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm");

    @FXML private BorderPane rootPane;
    @FXML private StackPane contentPane;
    @FXML private VBox dashboardHome;
    @FXML private Button dashboardNavButton;
    @FXML private Button patientsNavButton;
    @FXML private Button appointmentsNavButton;
    @FXML private Button billingNavButton;
    @FXML private Button notificationsNavButton;
    @FXML private TextField globalSearchField;
    @FXML private ListView<String> globalSearchResultsList;
    @FXML private Label clockLabel;
    @FXML private Label modeLabel;
    @FXML private Label totalPatientsLabel;
    @FXML private Label todayAppointmentsLabel;
    @FXML private Label revenueLabel;
    @FXML private Label occupancyLabel;
    @FXML private Label activePatientsLabel;
    @FXML private Label overdueInvoicesLabel;
    @FXML private Label averageRiskLabel;
    @FXML private ProgressBar occupancyProgress;
    @FXML private LineChart<String, Number> patientsLineChart;
    @FXML private BarChart<String, Number> specialtyBarChart;
    @FXML private PieChart billingPieChart;
    @FXML private ListView<String> notificationsList;
    @FXML private ListView<String> aiInsightsList;

    @FXML
    private void initialize() {
        modeLabel.setText(DatabaseConfig.modeLabel());
        clockLabel.setText(LocalDateTime.now().format(clockFormatter));
        markActive(dashboardNavButton);
        setupSmartSearch();
        Platform.runLater(() -> {
            installShortcuts();
            refreshDashboard();
            ViewAnimations.fadeSlideIn(dashboardHome);
        });
    }

    @FXML
    private void showDashboard() {
        contentPane.getChildren().setAll(dashboardHome);
        markActive(dashboardNavButton);
        refreshDashboard();
        ViewAnimations.fadeSlideIn(dashboardHome);
    }

    @FXML
    private void showPatients() {
        loadCenterView("/view/patients.fxml", patientsNavButton);
    }

    @FXML
    private void showAppointments() {
        loadCenterView("/view/appointments.fxml", appointmentsNavButton);
    }

    @FXML
    private void showBilling() {
        loadCenterView("/view/billing.fxml", billingNavButton);
    }

    @FXML
    private void showNotifications() {
        VBox panel = new VBox(14);
        panel.getStyleClass().addAll("screen", "notification-screen");
        Label title = new Label("Clinical command notifications");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Upcoming appointments, overdue payments and AI risk signals.");
        subtitle.getStyleClass().add("page-subtitle");
        ListView<String> list = new ListView<>();
        list.getStyleClass().add("command-list");
        list.setItems(FXCollections.observableArrayList(notificationService.notifications()
                .stream()
                .map(NotificationItem::displayText)
                .toList()));
        VBox.setVgrow(list, javafx.scene.layout.Priority.ALWAYS);
        panel.getChildren().setAll(title, subtitle, list);
        contentPane.getChildren().setAll(panel);
        markActive(notificationsNavButton);
        ViewAnimations.fadeSlideIn(panel);
    }

    @FXML
    private void refreshDashboard() {
        DashboardMetrics metrics = dashboardService.metrics();
        totalPatientsLabel.setText(String.valueOf(metrics.totalPatients()));
        todayAppointmentsLabel.setText(String.valueOf(metrics.appointmentsToday()));
        revenueLabel.setText(currency.format(metrics.revenue()));
        occupancyLabel.setText(Math.round(metrics.occupancyRate() * 100) + "%");
        activePatientsLabel.setText(String.valueOf(metrics.activePatients()));
        overdueInvoicesLabel.setText(String.valueOf(metrics.overdueInvoices()));
        averageRiskLabel.setText(metrics.averageRiskScore() + "%");
        occupancyProgress.setProgress(metrics.occupancyRate());
        loadCharts();
        loadNotifications();
        loadAiInsights();
    }

    @FXML
    private void handleAbout() {
        AlertFactory.info("MedVision AI",
                "Professional JavaFX MVC healthcare dashboard with services, repositories, analytics and simulated AI.");
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    private void setupSmartSearch() {
        globalSearchResultsList.setVisible(false);
        globalSearchResultsList.setManaged(false);
        globalSearchField.textProperty().addListener((observable, oldValue, query) -> {
            List<String> results = globalSearchService.search(query).stream()
                    .map(result -> result.displayText())
                    .toList();
            globalSearchResultsList.setItems(FXCollections.observableArrayList(results));
            boolean show = !results.isEmpty();
            globalSearchResultsList.setVisible(show);
            globalSearchResultsList.setManaged(show);
        });
        globalSearchResultsList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, value) -> {
            if (value != null) {
                AlertFactory.info("Smart search", value);
                globalSearchResultsList.getSelectionModel().clearSelection();
            }
        });
    }

    private void installShortcuts() {
        rootPane.getScene().addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.K) {
                globalSearchField.requestFocus();
                globalSearchField.selectAll();
                event.consume();
            }
        });
    }

    private void loadCharts() {
        XYChart.Series<String, Number> patientSeries = new XYChart.Series<>();
        patientSeries.setName("New patients");
        dashboardService.patientsCreatedByMonth(6).forEach((month, count) ->
                patientSeries.getData().add(new XYChart.Data<>(month, count)));
        patientsLineChart.getData().setAll(patientSeries);

        XYChart.Series<String, Number> specialtySeries = new XYChart.Series<>();
        specialtySeries.setName("Appointments");
        appointmentService.countBySpecialty().forEach((specialty, count) -> {
            if (count > 0) {
                specialtySeries.getData().add(new XYChart.Data<>(specialty, count));
            }
        });
        specialtyBarChart.getData().setAll(specialtySeries);

        billingPieChart.setData(FXCollections.observableArrayList(
                billingService.revenueByStatus().entrySet().stream()
                        .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                        .toList()));
    }

    private void loadNotifications() {
        notificationsList.setItems(FXCollections.observableArrayList(notificationService.notifications()
                .stream()
                .limit(8)
                .map(NotificationItem::displayText)
                .toList()));
    }

    private void loadAiInsights() {
        List<String> insights = patientService.findAll().stream()
                .sorted(Comparator.comparing(aiSimulationService::noShowRiskScore).reversed())
                .limit(5)
                .map(this::formatRiskInsight)
                .toList();
        aiInsightsList.setItems(FXCollections.observableArrayList(insights));
    }

    private String formatRiskInsight(Patient patient) {
        int score = aiSimulationService.noShowRiskScore(patient);
        return patient.getFullName() + " | " + score + "% " + aiSimulationService.riskBand(score)
                + " | " + aiSimulationService.diagnosisSuggestion(patient);
    }

    private void loadCenterView(String resource, Button activeButton) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(resource));
            contentPane.getChildren().setAll(view);
            markActive(activeButton);
            ViewAnimations.fadeSlideIn(view);
        } catch (IOException exception) {
            AlertFactory.error("View unavailable", "Cannot load " + resource + "\n" + exception.getMessage());
        }
    }

    private void markActive(Button activeButton) {
        List.of(dashboardNavButton, patientsNavButton, appointmentsNavButton, billingNavButton, notificationsNavButton)
                .forEach(button -> button.getStyleClass().remove("nav-button-active"));
        activeButton.getStyleClass().add("nav-button-active");
    }
}
