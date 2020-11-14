package cs10.apps.web.statsforspotify.model;

public enum TopTerms {
    SHORT("short_term", "Last 4 Weeks"),
    MEDIUM("medium_term", "Last 6 Months"),
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
