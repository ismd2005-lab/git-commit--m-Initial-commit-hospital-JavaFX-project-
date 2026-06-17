package service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Appointment;
import model.Invoice;
import model.Patient;
import repository.AppointmentRepository;
import repository.InvoiceRepository;
import repository.PatientRepository;
import util.PdfExporter;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class BillingService {
    public static final List<String> STATUSES = List.of("Pending", "Paid", "Overdue", "Cancelled");

    private final InvoiceRepository invoiceRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    public BillingService(InvoiceRepository invoiceRepository, PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository) {
        this.invoiceRepository = invoiceRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public ObservableList<Invoice> findAll() {
        return invoiceRepository.findAll().sorted(Comparator.comparing(Invoice::getInvoiceDate).reversed());
    }

    public Invoice generateInvoice(int patientId, double amount, String description, LocalDate dueDate) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Invoice amount must be greater than zero.");
        }
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found."));
        String safeDescription = description == null || description.isBlank()
                ? "Clinical services"
                : description.trim();
        Invoice invoice = new Invoice(0, patientId, patient.getFullName(), LocalDate.now(), dueDate,
                amount, dueDate != null && dueDate.isBefore(LocalDate.now()) ? "Overdue" : "Pending",
                safeDescription);
        return invoiceRepository.save(invoice);
    }

    public Invoice updateInvoice(Invoice invoice) {
        if (!STATUSES.contains(invoice.getStatus())) {
            throw new IllegalArgumentException("Invoice status is invalid.");
        }
        return invoiceRepository.update(invoice);
    }

    public void updateStatus(int invoiceId, String status) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found."));
        invoice.setStatus(status);
        updateInvoice(invoice);
    }

    public ObservableList<Invoice> search(String keyword, String status) {
        String safeKeyword = normalize(keyword);
        return FXCollections.observableArrayList(invoiceRepository.findAll().stream()
                .filter(invoice -> matchesKeyword(invoice, safeKeyword))
                .filter(invoice -> status == null || status.equals("All") || status.equals(invoice.getStatus()))
                .sorted(Comparator.comparing(Invoice::getInvoiceDate).reversed())
                .toList());
    }

    public double paidRevenue() {
        return invoiceRepository.findAll().stream()
                .filter(invoice -> "Paid".equals(invoice.getStatus()))
                .mapToDouble(Invoice::getAmount)
                .sum();
    }

    public double outstandingRevenue() {
        return invoiceRepository.findAll().stream()
                .filter(invoice -> !"Paid".equals(invoice.getStatus()) && !"Cancelled".equals(invoice.getStatus()))
                .mapToDouble(Invoice::getAmount)
                .sum();
    }

    public List<Invoice> overdueInvoices() {
        return invoiceRepository.findAll().stream()
                .filter(invoice -> invoice.isOverdue() || "Overdue".equals(invoice.getStatus()))
                .sorted(Comparator.comparing(Invoice::getDueDate))
                .toList();
    }

    public Map<String, Double> revenueByStatus() {
        return invoiceRepository.findAll().stream()
                .collect(Collectors.groupingBy(Invoice::getStatus, LinkedHashMapCollector::newLinkedHashMap,
                        Collectors.summingDouble(Invoice::getAmount)));
    }

    public void exportInvoicePdf(Invoice invoice, File file) throws IOException {
        Patient patient = patientRepository.findById(invoice.getPatientId()).orElse(null);
        List<Appointment> appointments = appointmentRepository.findAll().stream()
                .filter(appointment -> appointment.getPatientId() == invoice.getPatientId())
                .sorted(Comparator.comparing(Appointment::getDate).thenComparing(Appointment::getTime))
                .limit(5)
                .toList();
        PdfExporter.writeInvoice(file, invoice, patient, appointments);
    }

    public void exportPatientReportPdf(Patient patient, File file, List<String> timeline, int riskScore,
                                       String diagnosisSuggestion) throws IOException {
        PdfExporter.writePatientReport(file, patient, timeline, riskScore, diagnosisSuggestion);
    }

    private boolean matchesKeyword(Invoice invoice, String keyword) {
        if (keyword.isBlank()) {
            return true;
        }
        return normalize(invoice.getPatientName()).contains(keyword)
                || normalize(invoice.getDescription()).contains(keyword)
                || String.valueOf(invoice.getId()).contains(keyword);
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    private static final class LinkedHashMapCollector {
        private LinkedHashMapCollector() {
        }

        private static <K, V> java.util.LinkedHashMap<K, V> newLinkedHashMap() {
            return new java.util.LinkedHashMap<>();
        }
    }
}
