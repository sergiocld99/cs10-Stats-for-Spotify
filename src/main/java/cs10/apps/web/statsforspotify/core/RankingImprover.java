package cs10.apps.web.statsforspotify.core;

import com.wrapper.spotify.model_objects.specification.Track;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.desktop.statsforspotify.model.Status;

import java.util.HashSet;
import java.util.Set;

public class RankingImprover {
    private final Set<Track> blockedTracks = new HashSet<>();
    private Track targetTrack;

    public void addBlockedTracks(Track[] result){
        blockedTracks.add(result[result.length-2]);
        blockedTracks.add(result[result.length-3]);
        blockedTracks.add(result[result.length-4]);
        blockedTracks.add(result[result.length-5]);
        targetTrack = result[result.length-1];
    }

    public boolean isPopularityBlocked(int popularity){
        for (Track t : blockedTracks){
            if (Math.abs(popularity - t.getPopularity()) < 2)
                return true;
        }

        return false;
    }

    public Track getTargetTrack() {
        return targetTrack;
    }

    public void analyze(Song song){
        if (isTrackBlocked(song.getId())){
            if (song.getRank() < 16 && removeTrack(song.getId()))
                System.out.println(song + " removed from Blocked Tracks");
            else song.setStatus(Status.LEFT);
        }
    }

    public boolean removeTrack(String trackId){
        return blockedTracks.removeIf(t -> t.getId().equals(trackId));
    }

    public boolean isTrackBlocked(String trackId){
        for (Track t : blockedTracks){
            if (t.getId().equals(trackId)) return true;
        }

        return false;
    }

    public boolean isArtistBlocked(String artistName){
        for (Track t : blockedTracks){
            if (t.getArtists()[0].getName().equals(artistName))
                return true;
        }

        return false;
    }
}
