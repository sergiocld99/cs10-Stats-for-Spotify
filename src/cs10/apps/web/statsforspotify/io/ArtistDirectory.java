package cs10.apps.web.statsforspotify.io;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

public class ArtistDirectory implements Comparable<ArtistDirectory> {
    private Collection<SongFile> songFiles;
    private String artistName;

    private float artistScore;
    private float[] popularitySumByRank;
    private final int rankingsAmount;

    public ArtistDirectory(File file, int rankingsAmount){
        this.rankingsAmount = rankingsAmount;
        this.explore(file);
        this.analyzeSongs();
    }

    private void explore(File file){
        songFiles = new HashSet<>();
        artistName = file.getName();

        File[] filesInsideDirectory = file.listFiles();
        if (filesInsideDirectory != null){
            for (File f : filesInsideDirectory){
                SongFile songFile = new SongFile(f);
                songFiles.add(songFile);
            }
        }
    }

    private void analyzeSongs(){
        popularitySumByRank = new float[10];

        for (SongFile f : songFiles){
            for (int i=0; i<popularitySumByRank.length; i++){
                popularitySumByRank[i] += f.getPopularitySumByRank()[i];
            }
        }

        for (int i=1; i<=popularitySumByRank.length; i++){
            artistScore += popularitySumByRank[10-i] * i;
        }

        artistScore = artistScore * 3 / rankingsAmount;
    }

    public String getArtistName() {
        return artistName;
    }

    public float getArtistScore() {
        return artistScore;
    }

    public float[] getPopularitySumByRank() {
        return popularitySumByRank;
    }

    public SongFile getSongById(String trackId){
        for (SongFile f : songFiles){
            if (f.getTrackId().equals(trackId))
                return f;
        }

        return null;
    }

    @Override
    public int compareTo(ArtistDirectory o) {
        return Float.compare(o.getArtistScore(), artistScore);
    }
}
