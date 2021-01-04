package cs10.apps.web.statsforspotify.service;

import cs10.apps.web.statsforspotify.model.BigRanking;
import cs10.apps.web.statsforspotify.utils.ApiUtils;
import cs10.apps.web.statsforspotify.utils.Maintenance;
import cs10.apps.web.statsforspotify.view.OptionPanes;

import javax.swing.*;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class AutoPlayService {
    public static final String NAME = "AutoPlay";
    private final BigRanking ranking;
    private final ApiUtils apiUtils;
    private final JButton button;
    private Thread thread;

    public AutoPlayService(BigRanking originalRanking, ApiUtils apiUtils, JButton button) {
        this.apiUtils = apiUtils;
        this.button = button;

        this.ranking = new BigRanking();
        ranking.addAll(originalRanking);
    }

    public void execute() {
        if (thread == null || !thread.isAlive()) {
            Collections.shuffle(ranking);
            thread = new Thread(new AutoPlayRunnable(), NAME + " Service");
            thread.start();
            OptionPanes.message(thread.getName() + " is running now");
        } else {
            thread.interrupt();
            OptionPanes.message(thread.getName() + " stopped");
        }
    }

    private class AutoPlayRunnable implements Runnable {

        @Override
        public void run() {
            button.setEnabled(false);

            int offset = (int) ranking.getCode() / 1000;
            if (offset == 0) offset = 10;

            for (int i=0; i<ranking.size(); i+=offset){
                int minCount = 100, minIndex = 0;

                for (int j=0; j<offset; j++){
                    int actualCount = ranking.get(i+j).getSongFile().getAppearancesCount();
                    if (actualCount < minCount){
                        minCount = actualCount;
                        minIndex = j;
                    }
                }

                try {
                    apiUtils.playThis(ranking.get(i+minIndex).getId(), false);
                    int percentage = i * 100 / ranking.size();
                    button.setText(NAME + " (" + percentage + "%)");
                    TimeUnit.SECONDS.sleep(150);
                    apiUtils.autoQueue(ranking, null);
                    percentage += offset / 2;
                    button.setText(NAME + " (" + percentage + "%)");
                    TimeUnit.SECONDS.sleep(300);
                } catch (InterruptedException e){
                    Maintenance.writeErrorFile(e, false);
                    break;
                }
            }

            button.setText(NAME + " (Premium)");
            button.setEnabled(true);
        }
    }
}
