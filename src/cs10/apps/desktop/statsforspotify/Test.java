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
            // Load new data
            Ranking ranking = IOUtils.parseSongs("Top Tracks - Stats for Spotify.html");

            // Save and update logs
            IOUtils.saveFile(ranking);

            // Load library with all data
            Library library = IOUtils.getArtistsFromLogs();

            // Show Main UI
            StatsFrame statsFrame = new StatsFrame(ranking, library);
            statsFrame.init();
            statsFrame.setVisible(true);

            // Select random to show
            Artist random = library.selectRandomByScore();
            if (random == null) return;

            JOptionPane.showMessageDialog(null, "I've selected this song: " +
                            random.selectRandomSong() + " by " + random.getName().toUpperCase() +
                            ". \n\n" + random.getTimesOnDetails(),
                    "Test says...", JOptionPane.QUESTION_MESSAGE);

        } catch (IOException e){
            JOptionPane.showMessageDialog(null, e.getMessage(),
                    "Test says...", JOptionPane.WARNING_MESSAGE);
        }
    }
}
