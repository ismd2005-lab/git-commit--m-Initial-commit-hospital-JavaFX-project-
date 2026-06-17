package repository;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Invoice;
import model.Patient;

import java.time.LocalDate;
import java.util.Optional;

public class InMemoryInvoiceRepository implements InvoiceRepository {
    private final ObservableList<Invoice> invoices = FXCollections.observableArrayList();
    private int nextId = 1;

    public InMemoryInvoiceRepository(ObservableList<Patient> patients) {
        seedInvoices(patients);
    }

    @Override
    public ObservableList<Invoice> findAll() {
        return invoices;
    }

    @Override
    public Optional<Invoice> findById(int id) {
        return invoices.stream().filter(invoice -> invoice.getId() == id).findFirst();
    }

    @Override
    public Invoice save(Invoice invoice) {
        invoice.setId(nextId++);
        invoices.add(invoice);
        return invoice;
    }

    @Override
    public Invoice update(Invoice invoice) {
        for (int i = 0; i < invoices.size(); i++) {
            if (invoices.get(i).getId() == invoice.getId()) {
                invoices.set(i, invoice);
                return invoice;
            }
        }
        throw new IllegalArgumentException("Invoice not found.");
    }

    @Override
    public void delete(int id) {
        invoices.removeIf(invoice -> invoice.getId() == id);
    }

    private void seedInvoices(ObservableList<Patient> patients) {
        save(new Invoice(0, 1, nameOf(patients, 1), LocalDate.now().minusDays(7),
                LocalDate.now().plusDays(7), 320.00, "Paid", "Cardiology assessment"));
        save(new Invoice(0, 2, nameOf(patients, 2), LocalDate.now().minusDays(4),
                LocalDate.now().plusDays(10), 540.00, "Pending", "Surgery follow-up"));
        save(new Invoice(0, 4, nameOf(patients, 4), LocalDate.now().minusDays(20),
                LocalDate.now().minusDays(3), 210.00, "Overdue", "Endocrinology lab review"));
    }

    private String nameOf(ObservableList<Patient> patients, int id) {
        return patients.stream()
                .filter(patient -> patient.getId() == id)
                .findFirst()
                .map(Patient::getFullName)
                .orElse("Patient #" + id);
    }
}
