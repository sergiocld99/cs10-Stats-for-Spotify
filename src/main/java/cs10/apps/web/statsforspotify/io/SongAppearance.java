package cs10.apps.web.statsforspotify.io;

public class SongAppearance {
    private final int popularity, rankingCode, chartPosition;

    public SongAppearance(String line){
        String[] params = line.split("--");
        chartPosition = Integer.parseInt(params[0]);
        popularity = Integer.parseInt(params[1]);
        rankingCode = Integer.parseInt(params[2]);
    }

    public SongAppearance(int popularity, long rankingCode, int chartPosition) {
        this.popularity = popularity;
        this.rankingCode = (int) rankingCode;
        this.chartPosition = chartPosition;
    }

    public boolean isValid(){
        return getPopularity() > 0 && getRankingCode() > 0 && getChartPosition() > 0;
    }

    public int getPopularity() {
        return popularity;
    }

    public int getRankingCode() {
        return rankingCode;
    }

    public int getChartPosition() {
        return chartPosition;
    }
}
