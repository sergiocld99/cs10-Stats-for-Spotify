package cs10.apps.web.statsforspotify.model;

public class LastFmData {
    private final int userPlayCount;
    private final Fanaticism fanaticism;

    public LastFmData(int userPlayCount, Fanaticism fanaticism) {
        this.userPlayCount = userPlayCount;
        this.fanaticism = fanaticism;
    }

    public Fanaticism getFanaticism() {
        return fanaticism;
    }

    public int getUserPlayCount() {
        return userPlayCount;
    }
}
