package model;

public record SearchResult(String type, String title, String subtitle) {
    public String displayText() {
        return "[" + type + "] " + title + " - " + subtitle;
    }
}
