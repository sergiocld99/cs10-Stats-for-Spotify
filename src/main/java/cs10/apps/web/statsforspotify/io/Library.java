package cs10.apps.web.statsforspotify.io;

import com.wrapper.spotify.model_objects.specification.Track;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.web.statsforspotify.model.BigRanking;
import cs10.apps.web.statsforspotify.utils.IOUtils;
import cs10.apps.web.statsforspotify.utils.Maintenance;
import cs10.apps.web.statsforspotify.view.OptionPanes;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Library {
    private static final File BASE_DIR = new File("library//");
    private List<ArtistDirectory> artistDirectories;
    private static Library instance;

    private final int rankingsAmount;

    private Library(File rootFile){
        this.rankingsAmount = IOUtils.getRankingsAmount();
        this.explore(rootFile);
    }

    public synchronized static Library getInstance(){
        if (instance == null){
            instance = new Library(BASE_DIR);
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

    public void analyze(){
        for (ArtistDirectory a : artistDirectories){
            a.analyzeSongs();
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

    public int getArtistRank(String artistName){
        artistName = artistName.replace("/\\","");

        for (int i=0; i<artistDirectories.size(); i++){
            if (artistDirectories.get(i).getArtistName().equals(artistName))
                return i+1;
        }

        return 0;
    }

    public SongFile getSongFile(Track track){
        ArtistDirectory d = getArtistByName(track.getArtists()[0].getName());
        if (d != null) return d.getSongById(track.getId());
        else return null;
    }

    public void sort(){
        Collections.sort(artistDirectories);
    }

    public List<ArtistDirectory> getTop(int size){
        return artistDirectories.subList(0, Math.min(size, artistDirectories.size()));
    }

    public void update(BigRanking bigRanking){
        for (Song s : bigRanking){
            for (String artist : s.getArtists().split(", ")) {
                ArtistDirectory a = getArtistByName(artist);
                if (a == null) {
                    File dirCreated = ArtistDirectory.makeDirectory(BASE_DIR, artist);
                    a = new ArtistDirectory(dirCreated, rankingsAmount);
                }

                SongFile songFile = a.getSongById(s.getId());
                if (songFile == null){
                    try {
                        File fileCreated = SongFile.createFile(a.getFile(), s);
                        songFile = new SongFile(fileCreated);
                        a.addSongFile(songFile);
                    } catch (IOException e){
                        Maintenance.writeErrorFile(e, true);
                        OptionPanes.message("Unable to create " + s.getName() + " song file");
                        continue;
                    }
                }

                songFile.update(s, bigRanking.getCode());
                s.setSongFile(songFile);
            }
        }
    }

    public void relink(BigRanking bigRanking){
        for (Song s : bigRanking){
            for (String artist : s.getArtists().split(", ")) {
                ArtistDirectory a = getArtistByName(artist);
                SongFile songFile = a.getSongById(s.getId());
                s.setSongFile(songFile);
            }
        }
    }

    public int getRankingsAmount() {
        return rankingsAmount;
    }
}
