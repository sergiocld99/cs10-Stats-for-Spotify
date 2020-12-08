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

    public AutoQueueService(BigRanking originalRanking, ApiUtils apiUtils, JButton button) {
        this.apiUtils = apiUtils;
        this.button = button;

        this.ranking = new BigRanking();
        ranking.addAll(originalRanking);
        Collections.shuffle(ranking);
    }

    public void execute() {
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(new AutoQueueRunnable(), "AutoQueue Service");
            thread.start();
            OptionPanes.message(thread.getName() + " is running now");
        } else {
            thread.interrupt();
            OptionPanes.message(thread.getName() + " stopped");
        }
    }

    private class AutoQueueRunnable implements Runnable {

        @Override
        public void run() {
            button.setEnabled(false);

            for (Song song : ranking){
                try {
                    Maintenance.log("Sleeping for " + song.getPopularity() + " seconds");
                    TimeUnit.SECONDS.sleep(song.getPopularity());
                    if (song.isRepeated()){
                        if (song.getPopularityStatus() == PopularityStatus.DECREASING) {
                            apiUtils.autoQueue(ranking, null);
                            continue;
                        }

                        Song otherSelected = ranking.get(song.getRank()-1);

                        if (song.getPopularityStatus() == PopularityStatus.NORMAL){
                            if (otherSelected.getPopularityStatus() != PopularityStatus.INCREASING){
                                if (!apiUtils.playThis(otherSelected.getId(), false)){
                                    Maintenance.log("Unable to execute AutoQueue");
                                    break;
                                } else continue;
                            } else Maintenance.log("Bad selection: " + otherSelected);
                        }

                        if (!apiUtils.playThis(song.getId(), false)){
                            Maintenance.log("Unable to execute AutoQueue");
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    Maintenance.writeErrorFile(e, false);
                }
            }

            button.setEnabled(true);
        }
    }
}
