package cs10.apps.web.statsforspotify.io;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

public class ArtistDirectory implements Comparable<ArtistDirectory> {
    private Collection<SongFile> songFiles;
    private final File file;

    private float artistScore;
    private float[] popularitySumByRank;
    private final int rankingsAmount;

    public ArtistDirectory(File file, int rankingsAmount){
        this.rankingsAmount = rankingsAmount;
        this.file = file;
        this.explore();
    }

    public static File makeDirectory(File parentFolder, String artistName){
        File f = new File(parentFolder, artistName);
        System.out.println(f.getPath() + " created: " + f.mkdirs());
        return f;
    }

    public void addSongFile(SongFile songFile){
        songFiles.add(songFile);
    }

    private void explore(){
        songFiles = new HashSet<>();

        File[] filesInsideDirectory = file.listFiles();
        if (filesInsideDirectory != null){
            for (File f : filesInsideDirectory){
                SongFile songFile = new SongFile(f);
                songFiles.add(songFile);
            }
        }
    }

    public void analyzeSongs(){
        popularitySumByRank = new float[10];

        for (SongFile f : songFiles){
            f.analyzeAppearances();

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
        return file.getName();
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

    public void multiplyScore(double factor){
        this.artistScore *= factor;
    }

    public void incrementScore(double delta){
        this.artistScore += delta;
    }

    public File getFile() {
        return file;
    }

    @Override
    public int compareTo(ArtistDirectory o) {
        return Float.compare(o.getArtistScore(), artistScore);
    }

    @Override
    public String toString() {
        return getArtistName() + " - Score: " + getArtistScore();
    }
}
