package util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

public final class AlertFactory {
    private AlertFactory() {
    }

    public static void info(String title, String message) {
        show(Alert.AlertType.INFORMATION, title, message);
    }

    public static void warning(String title, String message) {
        show(Alert.AlertType.WARNING, title, message);
    }

    public static void error(String title, String message) {
        show(Alert.AlertType.ERROR, title, message);
    }

    public static boolean confirm(String title, String message, String actionLabel) {
        ButtonType action = new ButtonType(actionLabel, ButtonBar.ButtonData.OK_DONE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.CANCEL, action);
        alert.setTitle(title);
        alert.setHeaderText(title);
        return alert.showAndWait().filter(action::equals).isPresent();
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
