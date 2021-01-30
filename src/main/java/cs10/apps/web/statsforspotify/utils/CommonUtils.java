package cs10.apps.web.statsforspotify.utils;

import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.desktop.statsforspotify.model.Status;
import cs10.apps.web.statsforspotify.model.ranking.BigRanking;
import cs10.apps.web.statsforspotify.view.OptionPanes;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommonUtils {

    public static String combineArtists(ArtistSimplified[] arr){
        StringBuilder sb = new StringBuilder(arr[0].getName());
        for (int i=1; i<arr.length; i++) sb.append(", ").append(arr[i].getName());
        return sb.toString();
    }

    public static void combineWithoutRepeats(Track[] tracks1, Track[] tracks2, int maxSize,
                                                    List<Track> result, List<Track> repeats){
        for (Track track : tracks2){
            if (!alreadyExists(tracks1, track)) {
                result.add(track);
                if (result.size() == maxSize)
                    break;
            } else repeats.add(track);
        }
    }

    private static boolean alreadyExists(Track[] array, Track track){
        for (Track value : array) {
            if (value.getId().equals(track.getId()))
                return true;
        }

        return false;
    }

    public static ImageIcon downloadImage(String url, int size){
        try {
            BufferedImage bi = ImageIO.read(new URL(url));
            Image image = bi.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(image);
        } catch (MalformedURLException e){
            System.err.println("Invalid format: " + url);
            e.printStackTrace();
        } catch (IOException e){
            System.err.println("Error while trying to download from web");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Draw a String centered in the middle of a Rectangle.
     * @param g The Graphics instance.
     * @param text The String to draw.
     * @param rect The Rectangle to center the text in.
     */
    public static void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
        // Get the FontMetrics
        FontMetrics metrics = g.getFontMetrics(font);
        // Determine the X coordinate for the text
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent,
        // as in java 2d 0 is top of the screen)
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        // Set the font
        g.setFont(font);
        // Draw the String
        g.drawString(text, x, y);
    }

    /**
     * Informs biggest gain and loss, and list the songs that left the chart
     */
    public static void summary(BigRanking bigRanking, BigRanking compare, ApiUtils apiUtils){
        int bigLoss = 0, bigGain = 0;
        Song songBigLoss = null, songBigGain = null;
        StringBuilder sb = new StringBuilder();

        for (Song s : bigRanking){
            if (s.getChange() == 0) continue;
            if (s.getChange() < 0){
                if (s.getChange() < bigLoss){
                    bigLoss = s.getChange();
                    songBigLoss = s;
                }
            } else {
                if (s.getChange() > bigGain){
                    bigGain = s.getChange();
                    songBigGain = s;
                }
            }
        }

        if (songBigGain != null){
            sb.append("Biggest Gain: ").append(songBigGain)
                    .append(" (+").append(bigGain).append(")").append('\n');
        }

        if (songBigLoss != null){
            sb.append("Biggest Loss: ").append(songBigLoss)
                    .append(" (").append(bigLoss).append(")").append("\n\n");
        }

        List<Song> nonMarkedSongs = compare.getNonMarked();
        sb.append(nonMarkedSongs.size()).append(" songs left the chart").append('\n');

        int entries = 0;

        for (Song s : bigRanking){
            if (s.getStatus() == Status.NEW){
                entries++;
            }
        }

        sb.append(entries).append(" entries or re-entries");

        /*if (!nonMarkedSongs.isEmpty()){
            sb.append("Songs that left the chart: ").append('\n');
            for (Song s : nonMarkedSongs){
                Track t = apiUtils.getTrackById(s.getId());
                sb.append(t.getName()).append(" by ")
                        .append(t.getArtists()[0].getName()).append('\n');
            }
        }*/

        String message = sb.toString();
        if (!message.isEmpty()) OptionPanes.message(message);
    }

    public static int countArtists(Track[] tracks){
        Set<String> set = new HashSet<>();
        for (Track t : tracks) set.add(t.getArtists()[0].getId());
        return set.size();
    }

    public static String toString(Track track){
        return track.getName() +
                " by " + track.getArtists()[0].getName();
    }

    public static String toString(TrackSimplified trackSimplified){
        return trackSimplified.getName() + " by " + trackSimplified.getArtists()[0].getName();
    }
}
