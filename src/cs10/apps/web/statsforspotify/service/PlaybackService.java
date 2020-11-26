package cs10.apps.web.statsforspotify.service;

import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.model_objects.specification.Track;
import cs10.apps.desktop.statsforspotify.model.Ranking;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.web.statsforspotify.utils.ApiUtils;
import cs10.apps.web.statsforspotify.view.CustomPlayer;
import cs10.apps.web.statsforspotify.view.OptionPanes;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlaybackService {
    private final ApiUtils apiUtils;
    private final JTable jTable;
    private final JFrame jFrame;
    private Ranking ranking;
    private boolean running;

    // Version 3
    private int time;

    // Version 4
    private ScheduledExecutorService scheduledExecutorService;
    private final CustomPlayer player;
    private Song lastSelectedSong;
    private int magicNumber;

    public PlaybackService(ApiUtils apiUtils, JTable jTable, JFrame jFrame, CustomPlayer player) {
        this.apiUtils = apiUtils;
        this.jTable = jTable;
        this.jFrame = jFrame;
        this.player = player;
    }

    public void run() {
        if (scheduledExecutorService != null){
            scheduledExecutorService.shutdown();
        }

        running = true;
        getCurrentData();
    }

    public void setRanking(Ranking ranking) {
        this.ranking = ranking;
    }

    private void getCurrentData() {
        CurrentlyPlaying currentlyPlaying = apiUtils.getCurrentSong();
        if (currentlyPlaying == null || !currentlyPlaying.getIs_playing()) {
            jFrame.setTitle("Ranking #" + ranking.getCode());
            running = false;
            return;
        }

        try {
            Track track = (Track) currentlyPlaying.getItem();
            int artistScore = player.setTrack(track);

            if (track == null){
                jFrame.setTitle("Advertisement");
                scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                scheduledExecutorService.schedule(this::getCurrentData, 30, TimeUnit.SECONDS);
                System.out.println("Retrying in 30 seconds...");
                return;
            } else {
                jFrame.setTitle("P: " + track.getPopularity() + " / A:" + artistScore +
                        " -- Now Playing: " + track.getName());
            }

            // Update table scroll and custom player labels
            SwingUtilities.invokeLater(()->{
                selectCurrentRow(track);
                jTable.repaint();
                player.repaint();
            });

            time = currentlyPlaying.getProgress_ms() / 1000;
            int maximum = track.getDurationMs() / 1000;
            //progressBar.setMaximum(maximum);
            running = true;

            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                player.setTime(time);
                if (time >= maximum || !running){
                    scheduledExecutorService.shutdown();
                    getCurrentData();
                } else time++;
            }, 0, 1, TimeUnit.SECONDS);
        } catch (Exception e) {
            OptionPanes.showPlaybackStopped();
            e.printStackTrace();
        }
    }

    private void selectCurrentRow(Track track){
        String id = track.getId();
        int firstCharNumber = track.getName().charAt(0)-'A'+1;
        Song song = ranking.getSong(id);
        if (song != null){
            player.changeProgressColor(Color.green);
            lastSelectedSong = song;
            magicNumber = 0;
            int i = song.getRank()-1;
            jTable.getSelectionModel().setSelectionInterval(i,i);
            scrollToCenter(jTable, i, i % 5);
        } else {
            if (lastSelectedSong != null){
                magicNumber += firstCharNumber;
                System.out.println("Current magic number: " + magicNumber);
                if (magicNumber > 30){
                    int rankSelected = (int) (lastSelectedSong.getRank() +
                            magicNumber * 0.01 * lastSelectedSong.getPopularity());
                    if (rankSelected <= ranking.size()){
                        player.changeProgressColor(Color.orange);
                        lastSelectedSong = ranking.get(rankSelected-1);
                        magicNumber = 0;
                        if (!apiUtils.addToQueue(lastSelectedSong)){
                            OptionPanes.message("Failed to queue \"" +
                                    lastSelectedSong.getName() + "\"");
                        }
                    }
                }
            }
        }
    }

    private void scrollToCenter(JTable table, int rowIndex, int vColIndex) {
        if (!(table.getParent() instanceof JViewport)) {
            return;
        }
        JViewport viewport = (JViewport) table.getParent();
        Rectangle rect = table.getCellRect(rowIndex, vColIndex, true);
        Rectangle viewRect = viewport.getViewRect();
        rect.setLocation(rect.x - viewRect.x, rect.y - viewRect.y);

        int centerX = (viewRect.width - rect.width) / 2;
        int centerY = (viewRect.height - rect.height) / 2;
        if (rect.x < centerX) {
            centerX = -centerX;
        }
        if (rect.y < centerY) {
            centerY = -centerY;
        }
        rect.translate(centerX, centerY);

        try {
            viewport.scrollRectToVisible(rect);
        } catch (ClassCastException e){
            System.err.println(e.getMessage());
        }
    }
}
