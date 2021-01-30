package cs10.apps.web.statsforspotify.model.ranking;

public class SimpleRanking implements Comparable<SimpleRanking> {
    private final String code;
    private String date;

    public SimpleRanking(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public int compareTo(SimpleRanking o) {
        return this.getDate().compareTo(o.getDate());
    }
}
