package cs10.apps.web.statsforspotify.view;

import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;
import cs10.apps.web.statsforspotify.app.DevelopException;
import cs10.apps.web.statsforspotify.io.ArtistDirectory;
import cs10.apps.web.statsforspotify.io.Library;
import cs10.apps.web.statsforspotify.io.SongFile;
import cs10.apps.web.statsforspotify.view.label.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.text.DecimalFormat;

public class CustomPlayer extends JPanel {
    private final DecimalFormat decimalFormat = new DecimalFormat("#00");
    private final CustomThumbnail thumbnail;
    private final CircleLabel popularityLabel, peakLabel;
    private final ScoreLabel scoreLabel;
    private final JProgressBar progressBar;
    private String currentSongId;
    private Library library;

    public CustomPlayer(int thumbSize) {
        this.thumbnail = new CustomThumbnail(thumbSize);
        this.popularityLabel = new PopularityLabel();
        this.scoreLabel = new ScoreLabel();
        this.peakLabel = new PeakLabel();
        this.progressBar = new JProgressBar(0, 100);

        this.customizeProgressBar();
        this.add(thumbnail);
        this.add(popularityLabel);
        this.add(scoreLabel);
        this.add(peakLabel);
        this.add(progressBar);
    }

    public void enableLibrary(){
        new Thread(() -> library = Library.getInstance()).start();
    }

    private void customizeProgressBar(){
        progressBar.setPreferredSize(new Dimension(360,30));
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
        this.peakLabel.setAverage(average / 3);
    }

    public void clear(){
        this.currentSongId = "";
        this.progressBar.setValue(0);
        this.progressBar.setString("");
        this.scoreLabel.setValue(0);
        this.popularityLabel.setValue(0);
        this.peakLabel.setValue(0);
        this.thumbnail.setUnknown();
    }

    /**
     * Updates thumbnail and circle labels
     * @param track current track from playback
     * @return if the current song is becoming unpopular
     */
    public boolean setTrack(Track track){
        if (track == null) throw new DevelopException(this);
        currentSongId = track.getId();

        if (track.getAlbum().getImages().length > 0){
            this.thumbnail.setCover(track.getAlbum().getImages()[0].getUrl());
        } else this.thumbnail.setUnknown();

        this.progressBar.setMaximum(track.getDurationMs() / 1000 + 1);
        this.progressBar.setValue(0);

        float score = 0, multiplier = 1;

        // find previous popularity
        //int previousPop = IOUtils.getFirstPopularity(track);
        SongFile songFile = library.getSongFile(track);
        int previousPop = 0;

        if (songFile != null)
            previousPop = songFile.getAppearances().get(0).getPopularity();

        for (ArtistSimplified a : track.getArtists()){
            //score += IOUtils.getArtistScore(a.getName()) * multiplier;
            ArtistDirectory d = library.getArtistByName(a.getName());
            if (d != null) score += d.getArtistScore() * multiplier;
            multiplier /= 2;
        }

        this.scoreLabel.setCollab(track.getArtists().length > 1);
        this.scoreLabel.setValue((int) score);
        this.popularityLabel.setOriginalValue(previousPop);
        this.popularityLabel.setValue(track.getPopularity());
        this.peakLabel.setValue(songFile == null ? 0 : songFile.getPeak().getChartPosition());

        if (previousPop > 0 && track.getPopularity() < previousPop){
            changeProgressColor(Color.orange);
            return true;
        } else {
            if (previousPop == track.getPopularity())
                changeProgressColor(Color.cyan);
            else changeProgressColor(Color.green);
            return false;
        }
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

    public String getCurrentSongId() {
        return currentSongId == null ? "" : currentSongId;
    }

    public int getArtistScore(){
        return scoreLabel.getValue();
    }

    public void changeProgressColor(Color color){
        this.progressBar.setForeground(color);
    }
}
