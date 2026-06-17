package model;

public record NotificationItem(String severity, String title, String message) {
    public String displayText() {
        return severity + "  |  " + title + " - " + message;
    }
}
