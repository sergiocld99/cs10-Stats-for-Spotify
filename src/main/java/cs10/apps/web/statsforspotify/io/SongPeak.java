package cs10.apps.web.statsforspotify.io;

public class SongPeak {
    private int chartPosition, rankingCode;

    public SongPeak(){
        this.chartPosition = 100;
    }

    public boolean isPeak(int chartPosition){
        return chartPosition < this.chartPosition;
    }

    public int getChartPosition() {
        return chartPosition;
    }

    public int getRankingCode() {
        return rankingCode;
    }

    public void setChartPosition(int chartPosition) {
        this.chartPosition = chartPosition;
    }

    public void setRankingCode(int rankingCode) {
        this.rankingCode = rankingCode;
    }
}
