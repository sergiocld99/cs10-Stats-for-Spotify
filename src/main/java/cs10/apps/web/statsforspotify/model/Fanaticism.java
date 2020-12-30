package cs10.apps.web.statsforspotify.model;

import cs10.apps.desktop.statsforspotify.model.Song;

public class Fanaticism {
    private final Song song;
    private final double topFan;
    private static final String MSG = "Top Fan should be greater than 0.5";

    public Fanaticism(Song song, double topFan) {
        if (topFan < 0.5) throw new IllegalArgumentException(MSG);

        this.song = song;
        this.topFan = topFan;
    }

    public Song getSong() {
        return song;
    }

    public String getIconName(){
        if (topFan > 1) return "star.png";
        else return "star_half.png";
    }

    public String getLabel(){
        if (topFan > 1) return "Top Fan";
        else return "Fan (" + (int) (topFan * 100) + "%)";
    }
}
