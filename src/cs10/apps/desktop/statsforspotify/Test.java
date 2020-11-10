package cs10.apps.desktop.statsforspotify;

import cs10.apps.desktop.statsforspotify.model.Ranking;
import cs10.apps.desktop.statsforspotify.utils.IOUtils;
import cs10.apps.desktop.statsforspotify.view.StatsFrame;

import javax.swing.*;
import java.io.IOException;

public class Test {

    public static void main(String[] args) {
        try {
            Ranking ranking = IOUtils.parseSongs("Top Tracks - Stats for Spotify.html");
            StatsFrame statsFrame = new StatsFrame(ranking);
            statsFrame.init();
            statsFrame.setVisible(true);
            IOUtils.saveFile(ranking);
        } catch (IOException e){
            JOptionPane.showMessageDialog(new JTextField("Error"), e.getMessage());
        }
    }
}
