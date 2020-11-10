package cs10.apps.desktop.statsforspotify;

import cs10.apps.desktop.statsforspotify.model.Artist;
import cs10.apps.desktop.statsforspotify.model.Library;
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

            // Save and update logs
            IOUtils.saveFile(ranking);

            // Select random
            Library library = IOUtils.getArtistsFromLogs();
            Artist random = library.selectRandomByScore();
            JOptionPane.showMessageDialog(null, "I've selected this song: " +
                            random.selectRandomSong() + " by " + random.getName().toUpperCase(),
                    "Test says...", JOptionPane.QUESTION_MESSAGE);

        } catch (IOException e){
            JOptionPane.showMessageDialog(null, e.getMessage(),
                    "Test says...", JOptionPane.WARNING_MESSAGE);
        }
    }
}
