package cs10.apps.web.statsforspotify.io;

import com.wrapper.spotify.model_objects.specification.Track;
import cs10.apps.web.statsforspotify.utils.IOUtils;

import java.io.File;
import java.util.*;

public class Library {
    private List<ArtistDirectory> artistDirectories;
    private static Library instance;

    private final int rankingsAmount;

    private Library(File rootFile){
        long startTime = System.currentTimeMillis();

        rankingsAmount = IOUtils.getRankingsAmount();
        this.explore(rootFile);
        System.out.println("Library loaded in " + (System.currentTimeMillis()-startTime) + " ms");
    }

    public synchronized static Library getInstance(){
        if (instance == null){
            instance = new Library(new File("library//"));
        }

        return instance;
    }

    private void explore(File rootFile){
        artistDirectories = new ArrayList<>();

        File[] internalDirectories = rootFile.listFiles();
        if (internalDirectories != null){
            for (File f : internalDirectories){
                ArtistDirectory artistDirectory = new ArtistDirectory(f, rankingsAmount);
                artistDirectories.add(artistDirectory);
            }
        }
    }

    public ArtistDirectory getArtistByName(String artistName){
        artistName = artistName.replace("/\\","");

        for (ArtistDirectory d : artistDirectories){
            if (d.getArtistName().equals(artistName))
                return d;
        }

        return null;
    }

    public SongFile getSongFile(Track track){
        ArtistDirectory d = getArtistByName(track.getArtists()[0].getName());
        if (d != null) return d.getSongById(track.getId());
        else return null;
    }

    public List<ArtistDirectory> getTop(int size){
        Collections.sort(artistDirectories);
        return artistDirectories.subList(0, Math.min(size, artistDirectories.size()));
    }
}
