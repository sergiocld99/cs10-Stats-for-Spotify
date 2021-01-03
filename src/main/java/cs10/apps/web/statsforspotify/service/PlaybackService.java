package cs10.apps.web.statsforspotify.service;

import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.model_objects.specification.PlayHistory;
import com.wrapper.spotify.model_objects.specification.Track;
import cs10.apps.desktop.statsforspotify.model.Ranking;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.web.statsforspotify.app.AppOptions;
import cs10.apps.web.statsforspotify.app.DevelopException;
import cs10.apps.web.statsforspotify.core.LastFmIntegration;
import cs10.apps.web.statsforspotify.utils.ApiUtils;
import cs10.apps.web.statsforspotify.utils.CommonUtils;
import cs10.apps.web.statsforspotify.utils.Maintenance;
import cs10.apps.web.statsforspotify.view.CustomPlayer;
import cs10.apps.web.statsforspotify.view.label.PeakLabel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlaybackService implements Runnable {
    private final CustomPlayer player;
    private final ApiUtils apiUtils;
    private final JTable table;
    private final JFrame frame;
    private Ranking ranking;
    private ScheduledExecutorService progressScheduler;
    private ArrayList<String> autoQueueUris;
    private Set<String> avoidInitials;

    private static final int AUTO_UPDATE_RATE = 24;
    private boolean running, canSkip;
    private int time, requestsCount, auxIndex;
    private long lastRequestTime;

    public PlaybackService(ApiUtils apiUtils, JTable table, JFrame frame, CustomPlayer player) {
        progressScheduler = Executors.newSingleThreadScheduledExecutor();
        this.apiUtils = apiUtils;
        this.table = table;
        this.frame = frame;
        this.player = player;
        this.canSkip = true;

        setAvoidInitials();
    }

    private void setAvoidInitials(){
        avoidInitials = new HashSet<>();
        for (PlayHistory ph : apiUtils.getRecentTracks())
            avoidInitials.add(initials(ph.getTrack().getName()));
        System.out.println("Avoid Initials Size: " + avoidInitials.size());
    }

    private void playNext(){
        auxIndex += (int) (Math.random() * 4);
        Song nextSong = ranking.get(auxIndex);
        Song avoidSong = ranking.get(ranking.size()-auxIndex);
        System.out.println(avoidSong + " added to Avoid List");
        avoidInitials.add(initials(avoidSong.getId()));
        apiUtils.playThis(nextSong.getId(),false);
        apiUtils.skipCurrentTrack();
        run();
    }

    private boolean checkInitials(Track track){
        if (!canSkip || requestsCount < 8) return false;

        for (String s : avoidInitials){
            if (track.getName().startsWith(s)){
                System.out.println(track.getName() + " skipped for initials");
                avoidInitials.remove(s);
                playNext();
                return true;
            }
        }

        return false;
    }

    private String initials(String s){
        int lastIndex = Math.min(s.length(), 3);
        return s.substring(0,lastIndex);
    }

    public void allowAutoUpdate(){
        ScheduledExecutorService scheduler2 = Executors.newSingleThreadScheduledExecutor();
        scheduler2.scheduleAtFixedRate(this, 30,
                AUTO_UPDATE_RATE, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        if (ranking == null) throw new DevelopException(this);
        ScheduledExecutorService scheduler0 = Executors.newSingleThreadScheduledExecutor();
        scheduler0.schedule(this::getCurrentData, 500, TimeUnit.MILLISECONDS);
        lastRequestTime = 0;
        running = true;
    }

    public void restart(){
        System.out.println("Restarting Playback Service");
        player.setCurrentSongId("..");
        canSkip = false;
        run();
    }

    public boolean isRunning() {
        return running;
    }

    public void setRanking(Ranking ranking) {
        this.ranking = ranking;
    }

    private void getCurrentData() {
        if (System.currentTimeMillis() - lastRequestTime < 6400) return;
        CurrentlyPlaying currentlyPlaying = apiUtils.getCurrentSong();
        lastRequestTime = System.currentTimeMillis();
        requestsCount++;

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

            // Check current
            if (player.getCurrentSongId().equals(track.getId())){
                time = currentlyPlaying.getProgress_ms() / 1000;
                switch (requestsCount % 3){
                    case 0:
                        if (track.getPopularity() > 9)
                            frame.setTitle(track.getName().split(" \\(")[0]
                                    + " (P: " + track.getPopularity() + ")");
                        else frame.setTitle(track.getName() + " (" +
                                track.getAlbum().getAlbumType().getType() + ")");
                        return;
                    case 1:
                        String year = track.getAlbum().getReleaseDate().split("-")[0];
                        frame.setTitle(track.getAlbum().getArtists()[0].getName() + " - " + year);
                        return;
                    case 2:
                        frame.setTitle(track.getAlbum().getName());
                        return;
                }
            } else {
                if (progressScheduler != null) progressScheduler.shutdown();
            }

            System.out.println(track.getName() + " set in Playback Service");
            if (checkInitials(track)) return;

            player.setCurrentSongId(track.getId());
            boolean isBecomingUnpopular = player.setTrack(track);
            boolean isRecommended = checkRecommended(track);
            this.attemptGetMinutes(track);

            if (!isRecommended){
                if (canSkip && isBecomingUnpopular){
                    //attemptQueue(track);
                    if (requestsCount % 2 == 0) {
                        System.out.println("Attempting to skip current track...");
                        if (apiUtils.skipCurrentTrack()){
                            ScheduledExecutorService skipDelayedExecutor
                                    = Executors.newSingleThreadScheduledExecutor();
                            skipDelayedExecutor.schedule(this::getCurrentData,
                                    1, TimeUnit.SECONDS);
                            return;
                        }
                    }
                    frame.setIconImage(new ImageIcon("appicon2.png").getImage());
                } else {
                    frame.setIconImage(new ImageIcon("appicon.png").getImage());
                    setCanSkip(true);
                }
            }

            // Only if the track wasn't skipped
            frame.setTitle("Now Playing: " + CommonUtils.toString(track));

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
                    player.setCurrentSongId("--");
                    progressScheduler.shutdown();
                    getCurrentData();
                } else time++;
            }, 0, 1, TimeUnit.SECONDS);
        } catch (Exception e) {
            Maintenance.writeErrorFile(e, true);
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
        if (ranking.size() > 0) new Thread(() ->
                autoQueueUris = apiUtils.autoQueue(ranking, currentTrack)).start();
    }

    private void scrollToCenter(JTable table, int rowIndex, int vColIndex) {
        if (!(table.getParent() instanceof JViewport)) return;
        JViewport viewport = (JViewport) table.getParent();
        Rectangle rect = table.getCellRect(rowIndex, vColIndex, true);
        Rectangle viewRect = viewport.getViewRect();
        rect.setLocation(rect.x - viewRect.x, rect.y - viewRect.y);

        int centerX = (viewRect.width - rect.width) / 2;
        int centerY = (viewRect.height - rect.height) / 2;
        if (rect.x < centerX) centerX = -centerX;
        if (rect.y < centerY) centerY = -centerY;
        rect.translate(centerX, centerY);
        viewport.scrollRectToVisible(rect);
    }

    public void setCanSkip(boolean canSkip) {
        this.canSkip = canSkip;
    }

    private void attemptGetMinutes(Track track){
        AppOptions appOptions = player.getAppOptions();
        PeakLabel peakLabel = player.getPeakLabel();

        new Thread(() -> {
            //int playCount = LastFmIntegration.getLastFmCount();
            int playCount = LastFmIntegration.getPlayCount(track, appOptions.getLastFmUser());
            if (playCount > 0) {
                int averageTime = player.getAverage() * 3;
                float minutes = playCount * track.getDurationMs() / 60000f;
                peakLabel.changeToLastFM(minutes, playCount, averageTime);
                //peakLabel.repaint();

                if (minutes > 100 && minutes < averageTime){
                    apiUtils.enqueueTwoTracksOfTheSameAlbum(track);
                }
            }
        }, "Set Minutes Label").start();
    }
}
