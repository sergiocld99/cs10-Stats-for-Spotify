package cs10.apps.web.statsforspotify.model;

public class Collab implements Comparable<Collab> {
    private float totalScore;
    private String name, artists;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtists() {
        return artists;
    }

    public void setArtists(String artists) {
        this.artists = artists;
    }

    public int getTotalScore() {
        return (int) totalScore;
    }

    public void setTotalScore(float totalScore) {
        this.totalScore = totalScore;
    }

    @Override
    public int compareTo(Collab o) {
        return Float.compare(o.getTotalScore(), getTotalScore());
    }
}
