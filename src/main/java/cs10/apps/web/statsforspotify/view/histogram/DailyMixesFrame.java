package cs10.apps.web.statsforspotify.view.histogram;

import cs10.apps.web.statsforspotify.utils.IOUtils;

import java.awt.*;

public class DailyMixesFrame extends DetailsFrame {
    private final int[] tracksTimes;

    public DailyMixesFrame(int[] tracksTimes){
        super("Your Daily Mixes", "Average appearances of the tracks " +
                        "in your saved rankings", 400, 300);
        this.tracksTimes = tracksTimes;
    }

    @Override
    protected void fillDetails() {
        int amount = IOUtils.getRankingsAmount();

        Color[] colors = new Color[]{
                Color.RED, Color.ORANGE, Color.GREEN,
                Color.CYAN, Color.BLUE, Color.MAGENTA
        };

        for (int i=0; i<tracksTimes.length; i++){
            String tag = "Mix #" + (i+1);
            histograma.agregarColumna(tag,
                    (float) tracksTimes[i] / amount, colors[i % colors.length]);
        }
    }
}
