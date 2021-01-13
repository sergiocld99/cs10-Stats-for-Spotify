package cs10.apps.web.statsforspotify.view.histogram;

import cs10.apps.web.statsforspotify.model.Genre;

import java.awt.*;
import java.util.List;

public class GenreFrame extends DetailsFrame {
    private final List<Genre> genreList;

    public GenreFrame(List<Genre> genreList){
        super("Current Top 10 Genres", "Appearances and Popularity", 400, 300);
        this.genreList = genreList;
    }

    @Override
    protected void fillDetails() {
        Color[] colors = new Color[]{Color.RED, Color.ORANGE, Color.GREEN, Color.CYAN, Color.MAGENTA};
        int i=0;

        for (Genre g : genreList){
            String tag = g.getName();
            histograma.agregarColumna(tag, g.getCount(), colors[i % colors.length]);
            if (++i == 10) break;
        }
    }
}
