package cs10.apps.web.statsforspotify.io;

public class SongPeak {
    private int chartPosition, rankingCode, times;

    public SongPeak(){
        this.chartPosition = 200;
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

    public void incrementTimes(){
        times++;
    }

    public void resetTimes(){
        times = 0;
    }

    public int getTimes() {
        return times;
    }

    @Override
    public String toString() {
        return "#" + chartPosition + (times > 1 ? " (x" + times + ")" : "");
    }
}
