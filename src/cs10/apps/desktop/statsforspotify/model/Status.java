package cs10.apps.desktop.statsforspotify.model;

public enum Status {
    NEW("new.png"), UP("up.png"), DOWN("down.png"),
    NOTHING(""), LEFT("left.png");

    private final String path;

    Status(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
