package cs10.apps.web.statsforspotify.view.chart;

import cs10.apps.web.statsforspotify.app.AppFrame;
import cs10.apps.web.statsforspotify.io.Library;
import cs10.apps.web.statsforspotify.io.SongAppearance;
import cs10.apps.web.statsforspotify.io.SongFile;
import cs10.apps.web.statsforspotify.io.SongPeak;
import cs10.apps.web.statsforspotify.utils.IOUtils;
import cs10.apps.web.statsforspotify.view.OptionPanes;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;

public class SongChartHistoryView extends AppFrame {
    private final SongFile songFile;

    public SongChartHistoryView(Library library, String artist, String songId){
        songFile = library.getArtistByName(artist).getSongById(songId);
    }

    public void init(){
        if (songFile.getAppearancesCount() < 4) {
            OptionPanes.message("Not enough song appearances yet");
            return;
        }

        SongPeak peak = songFile.getPeak();
        setResizable(false);
        setTitle("Peak #" + peak.getChartPosition() + " on " +
                IOUtils.getRankingDate(peak.getRankingCode()));

        XYSeries series = new XYSeries("Rank");

        int count = 1;
        for (SongAppearance a : songFile.getAppearances()){
            series.add(count++, a.getChartPosition());
        }

        int firstCode = songFile.getAppearances().get(0).getRankingCode();
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                songFile.getTrackName() + " by " + songFile.getArtistName(),
                "Appearances since " + IOUtils.getRankingDate(firstCode),
                "Rank", dataset, PlotOrientation.VERTICAL,
                true,true,false
        );

        XYPlot xyPlot = chart.getXYPlot();
        render(xyPlot, dataset);

        xyPlot.getDomainAxis().setRange(1,songFile.getAppearancesCount());
        xyPlot.getRangeAxis().setRange(0,100);
        xyPlot.getRangeAxis().setInverted(true);
        ChartPanel chartPanel = new ChartPanel(chart);
        setContentPane(chartPanel);
    }

    private void render(XYPlot plot, XYSeriesCollection dataset){
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(){
            final Stroke solid = new BasicStroke(2.0f);
            final Stroke dashed =  new BasicStroke(1.0f,
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10.0f, new float[] {10.0f}, 0.0f);

            @Override
            public Stroke getItemStroke(int row, int column) {
                if (row == 2){
                    double x = dataset.getXValue(row, column);
                    if ( x > 4){
                        return dashed;
                    } else {
                        return solid;
                    }
                } else
                    return super.getItemStroke(row, column);
            }
        };

        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultShapesFilled(true);
        //renderer.setDefaultStroke(new BasicStroke(10));
        renderer.setSeriesStroke(0, new BasicStroke(8));
        plot.setRenderer(renderer);
    }
}
