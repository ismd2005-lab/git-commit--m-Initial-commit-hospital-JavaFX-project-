package util;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public final class CsvExporter {
    private CsvExporter() {
    }

    public static <T> File exportTable(TableView<T> tableView, Window owner, String defaultName) throws IOException {
        File file = chooseFile(owner, defaultName, "CSV file", "*.csv");
        if (file == null) {
            return null;
        }

        List<TableColumn<T, ?>> columns = tableView.getColumns()
                .stream()
                .filter(TableColumn::isVisible)
                .toList();

        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write(String.join(",", columns.stream().map(TableColumn::getText).map(CsvExporter::escape).toList()));
            writer.newLine();
            for (T item : tableView.getItems()) {
                writer.write(String.join(",", columns.stream()
                        .map(column -> column.getCellData(item))
                        .map(value -> value == null ? "" : value.toString())
                        .map(CsvExporter::escape)
                        .toList()));
                writer.newLine();
            }
        }
        return file;
    }

    public static File choosePdf(Window owner, String defaultName) {
        return chooseFile(owner, defaultName, "PDF file", "*.pdf");
    }

    private static File chooseFile(Window owner, String defaultName, String label, String extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export");
        fileChooser.setInitialFileName(defaultName);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(label, extension));
        return fileChooser.showSaveDialog(owner);
    }

    private static String escape(String value) {
        String safe = value == null ? "" : value;
        if (safe.contains(",") || safe.contains("\"") || safe.contains("\n")) {
            return "\"" + safe.replace("\"", "\"\"") + "\"";
        }
        return safe;
    }
}
