package cs10.apps.web.statsforspotify.service;

import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.model_objects.specification.Track;
import cs10.apps.desktop.statsforspotify.model.Ranking;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.web.statsforspotify.utils.ApiUtils;
import cs10.apps.web.statsforspotify.utils.CommonUtils;
import cs10.apps.web.statsforspotify.view.CustomPlayer;
import cs10.apps.web.statsforspotify.view.OptionPanes;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlaybackService implements Runnable {
    private static final int QUEUE_RATE = 20;

    private final CustomPlayer player;
    private final ApiUtils apiUtils;
    private final JTable table;
    private final JFrame frame;
    private Ranking ranking;
    private ScheduledExecutorService progressScheduler;
    private ArrayList<String> autoQueueUris;

    private boolean running;
    private int time, requestsCount;

    public PlaybackService(ApiUtils apiUtils, JTable table, JFrame frame, CustomPlayer player) {
        this.apiUtils = apiUtils;
        this.table = table;
        this.frame = frame;
        this.player = player;
    }

    public void allowAutoUpdate(){
        ScheduledExecutorService scheduler2 = Executors.newSingleThreadScheduledExecutor();
        scheduler2.scheduleAtFixedRate(this, 30, 29, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        running = true;
        getCurrentData();
    }

    public void setRanking(Ranking ranking) {
        this.ranking = ranking;
    }

    private void getCurrentData() {
        requestsCount++;

        System.out.println("Current Song - Request #" + requestsCount);
        CurrentlyPlaying currentlyPlaying = apiUtils.getCurrentSong();

        if (currentlyPlaying == null || !currentlyPlaying.getIs_playing()) {
            frame.setTitle("Ranking #" + ranking.getCode());
            running = false;
            return;
        }

        try {
            Track track = (Track) currentlyPlaying.getItem();

            if (track == null){
                if (progressScheduler != null)
                    progressScheduler.shutdown();
                //jFrame.setTitle("Advertisement");
                frame.setVisible(false);
                player.clear();
                return;
            } else {
                if (!frame.isVisible())
                    frame.setVisible(true);
            }

            if (requestsCount % QUEUE_RATE == QUEUE_RATE / 2){
                attemptQueue(track);
            }

            // Check current
            if (player.getCurrentSongId().equals(track.getId())){
                if (player.getArtistScore() > 0 && requestsCount % 2 == 0){
                    frame.setTitle("P: " + track.getPopularity() + " / A: " + player.getArtistScore());
                } else frame.setTitle(track.getName());

                return;

            } else {
                if (progressScheduler != null) progressScheduler.shutdown();
                frame.setTitle("Now Playing: " + CommonUtils.toString(track));
            }

            System.out.println("Updating Custom Player...");
            boolean isBecomingUnpopular = player.setTrack(track);
            boolean isRecommended = checkRecommended(track);

            if (!isRecommended){
                if (isBecomingUnpopular){
                    frame.setIconImage(new ImageIcon("appicon2.png").getImage());
                    if (requestsCount % QUEUE_RATE > QUEUE_RATE / 2) apiUtils.skipCurrentTrack();
                } else frame.setIconImage(new ImageIcon("appicon.png").getImage());
            }

            // Update table scroll
            SwingUtilities.invokeLater(()->{
                selectCurrentRow(track);
                table.repaint();
                player.repaint();
            });

            time = currentlyPlaying.getProgress_ms() / 1000;
            int maximum = track.getDurationMs() / 1000;
            running = true;

            progressScheduler = Executors.newSingleThreadScheduledExecutor();
            progressScheduler.scheduleAtFixedRate(() -> {
                player.setTime(time);
                if (time >= maximum || !running){
                    progressScheduler.shutdown();
                    getCurrentData();
                } else time++;
            }, 0, 1, TimeUnit.SECONDS);
        } catch (Exception e) {
            OptionPanes.showError("Playback Service - Current Data", e);
        }
    }

    private boolean checkRecommended(Track track){
        if (autoQueueUris != null) for (String id : autoQueueUris){
            if (id.equals("spotify:track:"+track.getId())){
                frame.setIconImage(new ImageIcon("appicon3.png").getImage());
                frame.setTitle("Playing a Recommended Song: " + CommonUtils.toString(track));
                return true;
            }
        }

        return false;
    }

    private void selectCurrentRow(Track track){
        Song song = ranking.getSong(track.getId());
        if (song != null){
            int i = song.getRank()-1;
            table.getSelectionModel().setSelectionInterval(i,i);
            scrollToCenter(table, i, i % 5);
        } else table.clearSelection();
    }

    private void attemptQueue(Track currentTrack){
        if (ranking.size() > 0)
            autoQueueUris = apiUtils.autoQueue(ranking, currentTrack);
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
