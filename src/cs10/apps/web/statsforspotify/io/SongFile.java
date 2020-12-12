package cs10.apps.web.statsforspotify.io;

import cs10.apps.web.statsforspotify.utils.Maintenance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SongFile {
    private String trackName, trackId;
    private List<SongAppearance> appearances;
    private SongPeak peak;

    private int appearancesCount;
    private float[] popularitySumByRank;

    public SongFile(File file){
        try {
            this.read(file);
            this.analyzeAppearances();
        } catch (IOException e){
            Maintenance.writeErrorFile(e, false);
        }
    }

    private void read(File file) throws IOException {
        appearances = new ArrayList<>();
        trackId = file.getName();

        BufferedReader br = new BufferedReader(new FileReader(file));
        trackName = br.readLine();

        String line;
        while ((line = br.readLine()) != null){
            appearances.add(new SongAppearance(line));
        }
    }

    private void analyzeAppearances(){
        popularitySumByRank = new float[10];
        peak = new SongPeak();

        for (SongAppearance a : appearances){
            if (peak.isPeak(a.getChartPosition())){
                peak.setChartPosition(a.getChartPosition());
                peak.setRankingCode(a.getRankingCode());
            }

            int arrayIndex = (a.getChartPosition()-1) / 10;
            popularitySumByRank[arrayIndex] += a.getPopularity() * 0.01;

            appearancesCount++;
        }
    }

    public String getTrackName() {
        return trackName;
    }

    public String getTrackId() {
        return trackId;
    }

    public List<SongAppearance> getAppearances() {
        return appearances;
    }

    public int getAppearancesCount() {
        return appearancesCount;
    }

    public SongPeak getPeak() {
        return peak;
    }

    public float[] getPopularitySumByRank() {
        return popularitySumByRank;
    }
}
