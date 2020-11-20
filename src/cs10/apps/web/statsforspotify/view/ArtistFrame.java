package cs10.apps.web.statsforspotify.view;

import java.awt.*;

public class ArtistFrame extends DetailsFrame {
    private final float[] scores;

    public ArtistFrame(String artist, float[] scores) throws HeadlessException {
        super("Stats for " + artist, "Rank and Popularity", 400, 300);
        this.scores = scores;
    }

    @Override
    protected void fillDetails() {
        Color[] colors = new Color[]{Color.RED, Color.ORANGE, Color.GREEN, Color.CYAN, Color.MAGENTA};
        for (int i=0; i<scores.length; i++){
            String tag = "#" + (i*10+1) + "-" + ((i+1)*10);
            histograma.agregarColumna(tag, scores[i], colors[i % colors.length]);
        }
    }
}
