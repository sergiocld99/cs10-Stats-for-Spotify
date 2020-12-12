package cs10.apps.web.statsforspotify.service;

import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.web.statsforspotify.model.BigRanking;
import cs10.apps.web.statsforspotify.model.PopularityStatus;
import cs10.apps.web.statsforspotify.utils.ApiUtils;
import cs10.apps.web.statsforspotify.utils.Maintenance;
import cs10.apps.web.statsforspotify.view.OptionPanes;

import javax.swing.*;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class AutoQueueService {
    private final BigRanking ranking;
    private final ApiUtils apiUtils;
    private final JButton button;
    private Thread thread;

    private int lastTracksPercentage;

    public AutoQueueService(BigRanking originalRanking, ApiUtils apiUtils, JButton button) {
        this.apiUtils = apiUtils;
        this.button = button;

        this.ranking = new BigRanking();
        ranking.addAll(originalRanking);
    }

    public void execute() {
        if (thread == null || !thread.isAlive()) {
            Collections.shuffle(ranking);
            lastTracksPercentage = apiUtils.analyzeRecentTracks();
            thread = new Thread(new HardAutoQueueRunnable(), "AutoQueue Service");
            thread.start();
            OptionPanes.message(thread.getName() + " is running now");
        } else {
            thread.interrupt();
            OptionPanes.message(thread.getName() + " stopped");
        }
    }

    private class HardAutoQueueRunnable implements Runnable {

        @Override
        public void run() {
            button.setEnabled(false);
            int count = 0;

            for (Song song : ranking){
                button.setText("AutoQueue (" + count * 100 / ranking.size() + "%)");
                count++;

                System.out.println("AQ || Index #" + count + ": " + song.toStringWithArtist());

                try {
                    TimeUnit.SECONDS.sleep(song.getPopularity());
                } catch (InterruptedException e) {
                    Maintenance.writeErrorFile(e, false);
                }

                if (song.isRepeated()){
                    Song otherSelected = ranking.get(song.getRank()-1);

                    if (lastTracksPercentage > ranking.size()) {
                        Maintenance.log("AQ || Other: " + otherSelected.toStringWithArtist());
                        if (!otherSelected.isRepeated()){
                            if (!apiUtils.playThis(otherSelected.getId(), false)){
                                Maintenance.log("AQ || Unable to execute");
                                break;
                            } else continue;
                        }
                    }

                    if (song.getPopularityStatus() == PopularityStatus.DECREASING) {
                        apiUtils.autoQueue(ranking, null);
                        continue;
                    }

                    if (song.getPopularityStatus() == PopularityStatus.NORMAL){
                        if (otherSelected.getPopularityStatus() != PopularityStatus.INCREASING){
                            Maintenance.log("AQ || Other: " + otherSelected.toStringWithArtist());
                            if (!apiUtils.playThis(otherSelected.getId(), false)){
                                Maintenance.log("AQ || Unable to execute");
                                break;
                            } else continue;
                        } else Maintenance.log("AQ || Bad other: " + otherSelected);
                    }

                    Maintenance.log("AQ || Selected: " + song.toStringWithArtist());
                    if (!apiUtils.playThis(song.getId(), false)){
                        Maintenance.log("AQ || Unable to execute");
                        break;
                    }
                }
            }

            button.setText("AutoQueue (Premium only)");
            button.setEnabled(true);
        }
    }
}
