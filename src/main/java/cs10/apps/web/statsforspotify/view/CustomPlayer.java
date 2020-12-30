package cs10.apps.web.statsforspotify.view;

import com.wrapper.spotify.model_objects.specification.Track;
import cs10.apps.web.statsforspotify.app.AppOptions;
import cs10.apps.web.statsforspotify.app.DevelopException;
import cs10.apps.web.statsforspotify.core.LastFmIntegration;
import cs10.apps.web.statsforspotify.io.Library;
import cs10.apps.web.statsforspotify.io.SongFile;
import cs10.apps.web.statsforspotify.model.Collab;
import cs10.apps.web.statsforspotify.view.label.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.text.DecimalFormat;

public class CustomPlayer extends JPanel {
    private final DecimalFormat decimalFormat = new DecimalFormat("#00");
    private final CustomThumbnail thumbnail;
    private final CircleLabel popularityLabel;
    private final ScoreLabel scoreLabel;
    private final PeakLabel peakLabel;
    private final JProgressBar progressBar;
    private final AppOptions appOptions;
    private String currentSongId;
    private Library library;
    private int average;

    public CustomPlayer(int thumbSize, AppOptions appOptions) {
        this.appOptions = appOptions;
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
        this.average = average;
        this.popularityLabel.setAverage(average);
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
        peakLabel.changeToPeak();

        if (track.getAlbum().getImages().length > 0){
            this.thumbnail.setCover(track.getAlbum().getImages()[0].getUrl());
        } else this.thumbnail.setUnknown();

        this.progressBar.setMaximum(track.getDurationMs() / 1000 + 1);
        this.progressBar.setValue(0);

        SongFile songFile = library.getSongFile(track);
        int previousPop = 0;

        if (songFile != null)
            previousPop = songFile.getAppearances().get(0).getPopularity();

        if (track.getArtists().length > 1){
            this.scoreLabel.setCollab(true);
            this.scoreLabel.setAverage(average);
            this.scoreLabel.setValue((int) Collab.calcScore(track.getArtists(),
                    track.getPopularity()));
        } else {
            this.scoreLabel.setCollab(false);
            this.scoreLabel.setAverage(average / 3);
            this.scoreLabel.setValue(library.getArtistRank(track.getArtists()[0].getName()));
        }

        this.popularityLabel.setOriginalValue(previousPop);
        this.popularityLabel.setValue(track.getPopularity());
        this.peakLabel.setValue(-1);

        if (songFile != null){
            peakLabel.changeToPeak();
            peakLabel.setAverage(average / 3);
            peakLabel.setValue(songFile.getPeak().getChartPosition());
            peakLabel.repaint();
        } else peakLabel.setValue(0);

        //this.peakLabel.setValue(LastFmIntegration.getLastFmCount());
        //this.peakLabel.setValue(songFile == null ? 0 : songFile.getPeak().getChartPosition());

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

    public void setTime(int timeInSeconds){
        this.setProgress(timeInSeconds);
        int seconds = timeInSeconds % 60;
        int minutes = timeInSeconds / 60;
        progressBar.setString(minutes + ":" + decimalFormat.format(seconds));

        if (peakLabel.isMinutes()){
            peakLabel.updateMinutes(timeInSeconds);
        }
    }

    public void setCurrentSongId(String currentSongId) {
        this.currentSongId = currentSongId;
    }

    public String getCurrentSongId() {
        return currentSongId == null ? "" : currentSongId;
    }

    public void changeProgressColor(Color color){
        this.progressBar.setForeground(color);
    }

    public AppOptions getAppOptions() {
        return appOptions;
    }

    public PeakLabel getPeakLabel() {
        return peakLabel;
    }

    public int getAverage() {
        return average;
    }
}
