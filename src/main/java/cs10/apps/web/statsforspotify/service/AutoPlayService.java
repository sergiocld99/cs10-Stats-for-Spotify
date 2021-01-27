package cs10.apps.web.statsforspotify.service;

import cs10.apps.web.statsforspotify.core.AutoPlaySelector;
import cs10.apps.web.statsforspotify.io.Library;
import cs10.apps.web.statsforspotify.model.BigRanking;
import cs10.apps.web.statsforspotify.utils.ApiUtils;
import cs10.apps.web.statsforspotify.view.OptionPanes;

import javax.swing.*;
import java.util.Collections;

public class AutoPlayService {
    public static final String NAME = "AutoPlay";
    private final BigRanking ranking;
    private final ApiUtils apiUtils;
    private final JButton button;
    private Thread thread;
    private boolean modifyPlayback;

    public AutoPlayService(BigRanking originalRanking, ApiUtils apiUtils, JButton button) {
        this.apiUtils = apiUtils;
        this.button = button;

        this.ranking = new BigRanking();
        ranking.addAll(originalRanking);
    }

    public void setModifyPlayback(boolean modifyPlayback) {
        this.modifyPlayback = modifyPlayback;
    }

    public void execute() {
        if (thread == null || !thread.isAlive()) {
            Collections.shuffle(ranking);
            thread = new Thread(new AutoPlayRunnable(), NAME + " Service");
            thread.start();
            if (modifyPlayback) OptionPanes.message(thread.getName() + " is running now");
        } else {
            thread.interrupt();
            OptionPanes.message(thread.getName() + " stopped");
        }
    }

    public class AutoPlayRunnable implements Runnable {

        @Override
        public void run() {
            if (button != null) button.setEnabled(false);
            AutoPlaySelector selector = new AutoPlaySelector(Library.getInstance(null), apiUtils, ranking, this);
            if (modifyPlayback) selector.run();
            else selector.createPlaylist();
        }

        public void enable(){
            if (button == null) return;
            button.setText(NAME + " (Premium)");
            button.setEnabled(true);
        }
    }
}
