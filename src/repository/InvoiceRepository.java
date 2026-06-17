package repository;

import javafx.collections.ObservableList;
import model.Invoice;

import java.util.Optional;

public interface InvoiceRepository {
    ObservableList<Invoice> findAll();

    Optional<Invoice> findById(int id);

    Invoice save(Invoice invoice);

    Invoice update(Invoice invoice);

    void delete(int id);
}
