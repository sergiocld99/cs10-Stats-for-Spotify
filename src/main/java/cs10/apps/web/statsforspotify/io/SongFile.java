package cs10.apps.web.statsforspotify.io;

import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.web.statsforspotify.utils.Maintenance;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SongFile {
    private String trackName;
    private List<SongAppearance> appearances;
    private SongPeak peak;
    private final File file;
    private float[] popularitySumByRank;

    public SongFile(File file){
        this.file = file;

        try {
            this.read();
        } catch (IOException e){
            Maintenance.writeErrorFile(e, false);
        }
    }

    public static File createFile(File parentFolder, Song song) throws IOException {
        File f = new File(parentFolder, song.getId());
        System.out.println(f.getPath() + " created: " + f.createNewFile());
        PrintWriter pw = new PrintWriter(new FileWriter(f, true));
        pw.println(song.getName());
        pw.close();
        return f;
    }

    private void read() throws IOException {
        appearances = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(file));
        trackName = br.readLine();

        String line;
        while ((line = br.readLine()) != null){
            appearances.add(new SongAppearance(line));
        }
    }

    public void update(Song song, long rankingCode) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))){
            pw.println(song.getRank()+"--"+song.getPopularity()+"--"+rankingCode);
            appearances.add(new SongAppearance(song.getPopularity(), rankingCode, song.getRank()));
        } catch (FileNotFoundException e){
            System.err.println(file.getPath() + " doesn't exist!");
        } catch (IOException e){
            Maintenance.writeErrorFile(e, false);
        }
    }

    public void analyzeAppearances(){
        popularitySumByRank = new float[10];
        peak = new SongPeak();

        for (SongAppearance a : appearances){
            if (peak.isPeak(a.getChartPosition())){
                peak.setChartPosition(a.getChartPosition());
                peak.setRankingCode(a.getRankingCode());
            }

            int arrayIndex = (a.getChartPosition()-1) / 10;
            popularitySumByRank[arrayIndex] += a.getPopularity() * 0.01;
        }
    }

    public SongAppearance getLastAppearance(){
        return this.appearances.get(getAppearancesCount()-1);
    }

    public String getTrackName() {
        return trackName;
    }

    public String getTrackId() {
        return file.getName();
    }

    public List<SongAppearance> getAppearances() {
        return appearances;
    }

    public int getAppearancesCount() {
        return appearances.size();
    }

    public SongPeak getPeak() {
        return peak;
    }

    public float[] getPopularitySumByRank() {
        return popularitySumByRank;
    }

    @Override
    public String toString() {
        return getTrackName() + " - Peak: " + getPeak().getChartPosition();
    }
}
