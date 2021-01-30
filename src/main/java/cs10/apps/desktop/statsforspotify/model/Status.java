package cs10.apps.desktop.statsforspotify.model;

public enum Status {
    NEW("new"), UP("up"), DOWN("down"), NOTHING(""), LEFT("left");

    private final String path;

    Status(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
