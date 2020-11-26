package cs10.apps.web.statsforspotify.view;

import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;
import cs10.apps.web.statsforspotify.utils.IOUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.text.DecimalFormat;

public class CustomPlayer extends JPanel {
    private final DecimalFormat decimalFormat = new DecimalFormat("#00");
    private final CustomThumbnail thumbnail;
    private final CircleLabel popularityLabel, scoreLabel;
    private final JProgressBar progressBar;

    public CustomPlayer(int thumbSize) {
        this.thumbnail = new CustomThumbnail(thumbSize);
        this.popularityLabel = new CircleLabel("Popularity");
        this.scoreLabel = new CircleLabel("Artist Score");
        this.progressBar = new JProgressBar(0, 100);
        this.customizeProgressBar();
        this.add(thumbnail);
        this.add(popularityLabel);
        this.add(scoreLabel);
        this.add(progressBar);
    }

    private void customizeProgressBar(){
        progressBar.setPreferredSize(new Dimension(400,30));
        progressBar.setBorder(new EmptyBorder(0,10,0,25));
        progressBar.setStringPainted(true);
        progressBar.setForeground(Color.GREEN);
        progressBar.setUI(new BasicProgressBarUI() {
            protected Color getSelectionBackground() { return Color.black; }
            protected Color getSelectionForeground() { return Color.black; }
        });
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public void setString(String s){
        this.progressBar.setString(s);
    }

    public void setAverage(int average){
        this.popularityLabel.setAverage(average);
        this.scoreLabel.setAverage(average);
    }

    /**
     * Updates thumbnail and circle labels
     * @param track current track from playback
     * @return current artist score
     */
    public int setTrack(Track track){
        if (track == null){
            this.progressBar.setValue(0);
            this.progressBar.setString("");
            this.scoreLabel.setValue(0);
            this.popularityLabel.setValue(0);
            this.thumbnail.setUnknown();
            return 0;
        }

        if (track.getAlbum().getImages().length > 0){
            this.thumbnail.setCover(track.getAlbum().getImages()[0].getUrl());
        } else this.thumbnail.setUnknown();

        this.progressBar.setMaximum(track.getDurationMs() / 1000 + 1);
        this.progressBar.setValue(0);

        float score = 0, multiplier = 1;

        // find previous popularity
        int previousPop = IOUtils.getFirstPopularity(track);

        for (ArtistSimplified a : track.getArtists()){
            score += IOUtils.getArtistScore(a.getName()) * multiplier;
            multiplier /= 2;
        }

        this.scoreLabel.setValue((int) score);
        this.popularityLabel.setOriginalValue(previousPop);
        this.popularityLabel.setValue(track.getPopularity());
        return (int) score;
    }

    public void setProgress(int value){
        this.progressBar.setValue(value);
    }

    public void setTime(int value){
        this.setProgress(value);
        int seconds = value % 60;
        int minutes = value / 60;
        progressBar.setString(minutes + ":" + decimalFormat.format(seconds));
    }

    public void changeProgressColor(Color color){
        this.progressBar.setForeground(color);
    }
}
