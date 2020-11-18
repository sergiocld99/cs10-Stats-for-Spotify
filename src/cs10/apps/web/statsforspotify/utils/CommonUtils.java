package cs10.apps.web.statsforspotify.utils;

import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommonUtils {

    public static String combineArtists(ArtistSimplified[] arr){
        StringBuilder sb = new StringBuilder(arr[0].getName());
        for (int i=1; i<arr.length; i++) sb.append(", ").append(arr[i].getName());
        return sb.toString();
    }

    public static List<Track> combineWithoutRepeats(Track[] tracks1, Track[] tracks2, int maxSize){
        List<Track> tracks = new ArrayList<>(Arrays.asList(tracks1));

        for (Track track : tracks2){
            if (!alreadyExists(tracks1, track)) {
                tracks.add(track);
                if (tracks.size() == maxSize)
                    break;
            }
        }

        return tracks;
    }

    private static boolean alreadyExists(Track[] array, Track track){
        for (Track value : array) {
            if (value.getId().equals(track.getId()))
                return true;
        }

        return false;
    }
}
