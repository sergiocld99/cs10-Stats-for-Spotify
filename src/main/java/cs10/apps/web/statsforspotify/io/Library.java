package cs10.apps.web.statsforspotify.io;

import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.web.statsforspotify.model.CustomList;
import cs10.apps.web.statsforspotify.model.ranking.BigRanking;
import cs10.apps.web.statsforspotify.utils.IOUtils;
import cs10.apps.web.statsforspotify.utils.Maintenance;
import cs10.apps.web.statsforspotify.view.CustomPlayer;
import cs10.apps.web.statsforspotify.view.OptionPanes;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Library {
    private static final File BASE_DIR = new File("library//");
    private CustomList<ArtistDirectory> artistDirectories;
    private final List<Song> trends = new LinkedList<>();
    private static Library instance;
    private final int rankingsAmount;

    private Library(File rootFile, CustomPlayer player){
        this.rankingsAmount = IOUtils.getRankingsAmount();
        this.explore(rootFile, player);
    }

    public synchronized static Library getInstance(CustomPlayer player){
        if (instance == null){
            instance = new Library(BASE_DIR, player);
        }

        return instance;
    }

    private void explore(File rootFile, CustomPlayer player){
        artistDirectories = new CustomList<>();

        File[] internalDirectories = rootFile.listFiles();
        if (internalDirectories != null){
            int progress = 0, max = internalDirectories.length;
            player.getProgressBar().setMaximum(max);
            for (File f : internalDirectories){
                ArtistDirectory artistDirectory = new ArtistDirectory(f, rankingsAmount);
                artistDirectories.add(artistDirectory);
                player.setProgress(++progress);
            }
        }
    }

    public void analyze(){
        for (ArtistDirectory a : artistDirectories){
            a.analyzeSongs();
        }
    }

    /**
     * @param artistName the exactly name of the artist
     * @return their directory (null if not exists)
     */
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

    public void sort(){
        Collections.sort(artistDirectories);
        int rank = 1;
        for (ArtistDirectory ad : artistDirectories) ad.setRank(rank++);
    }

    public List<ArtistDirectory> getTop(int size){
        return artistDirectories.subList(0, Math.min(size, artistDirectories.size()));
    }

    public void update(BigRanking bigRanking){
        for (Song s : bigRanking){
            updateSongFile(bigRanking.getCode(), s);
        }
    }

    private void updateSongFile(long rankingCode, Song s) {
        String artist = s.getArtists().split(", ")[0];
            ArtistDirectory a = getArtistByName(artist);
            if (a == null) {
                File dirCreated = ArtistDirectory.makeDirectory(BASE_DIR, artist);
                a = new ArtistDirectory(dirCreated, rankingsAmount);
                artistDirectories.add(a);
            }

            SongFile songFile = a.getSongById(s.getId());
            if (songFile == null){
                try {
                    File fileCreated = SongFile.createFile(a.getFile(), s);
                    songFile = new SongFile(fileCreated, a);
                    a.addSongFile(songFile);
                } catch (IOException e){
                    Maintenance.writeErrorFile(e, true);
                    OptionPanes.message("Unable to create " + s.getName() + " song file");
                    return;
                }
            }

            songFile.update(s, rankingCode);
            s.setSongFile(songFile);
    }


    public void relink(BigRanking bigRanking){
        for (Song s : bigRanking){
            if (!relinkSongFile(s)){
                System.err.println(s + " not found");
                updateSongFile(bigRanking.getCode(), s);
            }
        }
    }

    private boolean relinkSongFile(Song s) {
        String artist = s.getArtists().split(", ")[0];
            ArtistDirectory a = getArtistByName(artist);
            if (a == null) return false;
            SongFile songFile = a.getSongById(s.getId());
            s.setSongFile(songFile);

        return true;
    }

    public void addTrend(Song song){
        trends.add(song);
    }

    public void shuffleTrends(){
        Collections.shuffle(trends);
    }

    public Song getTrend(){
        return trends.remove(0);
    }

    public boolean isTrendsEmpty(){
        return trends.isEmpty();
    }

    public SongFile findById(String songId){
        for (ArtistDirectory a : artistDirectories){
            for (SongFile sf : a.getSongFiles()){
                if (sf.getTrackId().equals(songId))
                    return sf;
            }
        }

        return null;
    }

    public String selectBestId(String id1, String id2, String id3, String id4){
        SongFile sf1 = findById(id1);
        if (sf1 == null) return id1;
        SongFile sf2 = findById(id2);
        if (sf2 == null) return id2;
        SongFile sf3 = findById(id3);
        if (sf3 == null) return id3;
        SongFile sf4 = findById(id4);
        if (sf4 == null) return id4;
        return selectBestSong(sf1, sf2, sf3, sf4).getTrackId();
    }

    public SongFile selectBestSong(SongFile sf1, SongFile sf2){
        float score1 = getScore(sf1);
        float score2 = getScore(sf2);
        return (score1 > score2 ? sf1 : sf2);
    }

    public SongFile selectBestSong(SongFile sf1, SongFile sf2, SongFile sf3, SongFile sf4){
        return selectBestSong(selectBestSong(sf1, sf2), selectBestSong(sf3, sf4));
    }

    private float getScore(SongFile sf){
        int diff = sf.getLastAppearance().getPopularity() - sf.getMediumAppearance().getPopularity() + 1;
        return diff * sf.getRandomAppearance().getPopularity() - sf.getAppearancesCount();
    }

    public SongFile getRandomSong(){
        SongFile[] arr = new SongFile[4];
        for (int i=0; i<4; i++) arr[i] = artistDirectories.getRandomElement().getRandom();
        return selectBestSong(arr[0], arr[1], arr[2], arr[3]);
    }
}
