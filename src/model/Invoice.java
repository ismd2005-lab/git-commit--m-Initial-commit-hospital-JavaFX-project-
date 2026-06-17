package model;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

public class Invoice {
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.US);

    private int id;
    private int patientId;
    private String patientName;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private double amount;
    private String status;
    private String description;

    public Invoice() {
    }

    public Invoice(int id, int patientId, String patientName, LocalDate invoiceDate, LocalDate dueDate,
                   double amount, String status, String description) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.invoiceDate = invoiceDate;
        this.dueDate = dueDate;
        this.amount = amount;
        this.status = status;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAmountLabel() {
        return CURRENCY.format(amount);
    }

    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDate.now()) && !"Paid".equalsIgnoreCase(status);
    }
}
