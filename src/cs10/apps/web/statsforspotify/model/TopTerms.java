package cs10.apps.web.statsforspotify.model;

public enum TopTerms {
    SHORT("short_term", "Last 30 Days"),
    MEDIUM("medium_term", "Last 180 Days"),
    LONG("long_term", "All Time");

    private final String key, description;

    TopTerms(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }
}
