package cs10.apps.web.statsforspotify.io;

import cs10.apps.web.statsforspotify.model.CustomList;

import java.io.File;

public class ArtistDirectory implements Comparable<ArtistDirectory> {
    private CustomList<SongFile> songFiles;
    private final File file;

    private float artistScore, averagePopularity, averagePeak;
    private float[] popularitySumByRank;
    private final int rankingsAmount;
    private int rank;

    public ArtistDirectory(File file, int rankingsAmount){
        this.rankingsAmount = rankingsAmount;
        this.file = file;
        this.explore();
    }

    public static File makeDirectory(File parentFolder, String artistName){
        artistName = artistName.replace("/\\","");
        File f = new File(parentFolder, artistName);
        System.out.println(f.getPath() + " created: " + f.mkdirs());
        return f;
    }

    public void addSongFile(SongFile songFile){
        songFiles.add(songFile);
    }

    private void explore(){
        songFiles = new CustomList<>();

        File[] filesInsideDirectory = file.listFiles();
        if (filesInsideDirectory != null){
            for (File f : filesInsideDirectory){
                SongFile songFile = new SongFile(f, this);
                songFiles.add(songFile);
            }
        }
    }

    public void analyzeSongs(){
        popularitySumByRank = new float[10];
        float popularitySum = 0, peakSum = 0;

        for (SongFile f : songFiles){
            f.analyzeAppearances();
            peakSum += f.getPeak().getChartPosition();
            popularitySum += f.getLastAppearance().getPopularity();
            for (int i=0; i<popularitySumByRank.length; i++) popularitySumByRank[i] += f.getPopularitySumByRank()[i];
        }

        for (int i=1; i<=popularitySumByRank.length; i++){
            artistScore += popularitySumByRank[10-i] * i;
        }

        artistScore = artistScore * 3 / rankingsAmount;
        averagePopularity = popularitySum / songFiles.size();
        averagePeak = peakSum / songFiles.size();
    }

    public float getAveragePeak() {
        return averagePeak;
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

    public int getSongCount(){
        return songFiles.size();
    }

    public SongFile getRandom(){
        return songFiles.getRandomElement();
    }

    public float getAveragePopularity() {
        return averagePopularity;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

    public CustomList<SongFile> getSongFiles() {
        return songFiles;
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
