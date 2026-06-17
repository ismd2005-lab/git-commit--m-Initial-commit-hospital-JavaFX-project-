package controller;

import config.ApplicationContext;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Window;
import javafx.util.StringConverter;
import model.Invoice;
import model.Patient;
import service.BillingService;
import service.PatientService;
import util.AlertFactory;
import util.CsvExporter;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

public class BillingController {
    private final ApplicationContext context = ApplicationContext.getInstance();
    private final BillingService billingService = context.billingService();
    private final PatientService patientService = context.patientService();
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);

    @FXML private ComboBox<Patient> patientCombo;
    @FXML private TextField amountField;
    @FXML private DatePicker dueDatePicker;
    @FXML private TextArea descriptionArea;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private TableView<Invoice> invoicesTable;
    @FXML private TableColumn<Invoice, String> invoiceColumn;
    @FXML private TableColumn<Invoice, String> patientColumn;
    @FXML private TableColumn<Invoice, String> dateColumn;
    @FXML private TableColumn<Invoice, String> dueColumn;
    @FXML private TableColumn<Invoice, String> amountColumn;
    @FXML private TableColumn<Invoice, String> statusColumn;
    @FXML private Label paidRevenueLabel;
    @FXML private Label outstandingLabel;
    @FXML private Label detailPatientLabel;
    @FXML private Label detailAmountLabel;
    @FXML private Label detailStatusLabel;
    @FXML private Label detailDescriptionLabel;
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        setupControls();
        setupTable();
        setupListeners();
        refreshInvoices();
        clearDetail();
    }

    @FXML
    private void handleGenerateInvoice() {
        Patient patient = patientCombo.getValue();
        if (patient == null) {
            AlertFactory.warning("Invoice", "Select a patient.");
            return;
        }
        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            billingService.generateInvoice(patient.getId(), amount, descriptionArea.getText(), dueDatePicker.getValue());
            refreshInvoices();
            amountField.clear();
            descriptionArea.clear();
            statusLabel.setText("Invoice generated.");
        } catch (NumberFormatException exception) {
            AlertFactory.warning("Invoice", "Amount must be numeric.");
        } catch (IllegalArgumentException exception) {
            AlertFactory.warning("Invoice", exception.getMessage());
        }
    }

    @FXML
    private void handleMarkPaid() {
        updateSelectedStatus("Paid");
    }

    @FXML
    private void handleMarkPending() {
        updateSelectedStatus("Pending");
    }

    @FXML
    private void handleMarkOverdue() {
        updateSelectedStatus("Overdue");
    }

    @FXML
    private void handleExportInvoices() {
        try {
            File file = CsvExporter.exportTable(invoicesTable, window(), "medvision-invoices.csv");
            if (file != null) {
                statusLabel.setText("CSV exported: " + file.getName());
            }
        } catch (IOException exception) {
            AlertFactory.error("Export failed", exception.getMessage());
        }
    }

    @FXML
    private void handleExportInvoicePdf() {
        Invoice selected = invoicesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertFactory.warning("Selection required", "Select an invoice to export.");
            return;
        }
        File file = CsvExporter.choosePdf(window(), "medvision-invoice-" + selected.getId() + ".pdf");
        if (file == null) {
            return;
        }
        try {
            billingService.exportInvoicePdf(selected, file);
            statusLabel.setText("Invoice PDF exported: " + file.getName());
        } catch (IOException exception) {
            AlertFactory.error("PDF export failed", exception.getMessage());
        }
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
        dueDatePicker.setValue(LocalDate.now().plusDays(14));
        statusFilterCombo.setItems(FXCollections.observableArrayList("All"));
        statusFilterCombo.getItems().addAll(BillingService.STATUSES);
        statusFilterCombo.getSelectionModel().selectFirst();
    }

    private void setupTable() {
        invoiceColumn.setCellValueFactory(data -> new SimpleStringProperty("#" + data.getValue().getId()));
        patientColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPatientName()));
        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getInvoiceDate())));
        dueColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getDueDate())));
        amountColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAmountLabel()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
    }

    private void setupListeners() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> refreshInvoices());
        statusFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> refreshInvoices());
        invoicesTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, invoice) -> {
            if (invoice == null) {
                clearDetail();
            } else {
                showDetail(invoice);
            }
        });
    }

    private void refreshInvoices() {
        invoicesTable.setItems(billingService.search(searchField.getText(), statusFilterCombo.getValue()));
        paidRevenueLabel.setText(currency.format(billingService.paidRevenue()));
        outstandingLabel.setText(currency.format(billingService.outstandingRevenue()));
        statusLabel.setText(invoicesTable.getItems().size() + " invoice(s) displayed.");
    }

    private void updateSelectedStatus(String status) {
        Invoice selected = invoicesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertFactory.warning("Selection required", "Select an invoice first.");
            return;
        }
        billingService.updateStatus(selected.getId(), status);
        refreshInvoices();
        showDetail(selected);
    }

    private void showDetail(Invoice invoice) {
        detailPatientLabel.setText(invoice.getPatientName());
        detailAmountLabel.setText(invoice.getAmountLabel());
        detailStatusLabel.setText(invoice.getStatus());
        detailDescriptionLabel.setText(invoice.getDescription());
    }

    private void clearDetail() {
        detailPatientLabel.setText("Select an invoice");
        detailAmountLabel.setText("--");
        detailStatusLabel.setText("--");
        detailDescriptionLabel.setText("Invoice details and PDF export preview.");
    }

    private Window window() {
        return invoicesTable.getScene().getWindow();
    }
}
