package cs10.apps.web.statsforspotify.model;

public class Artist implements Comparable<Artist> {
    private String name;
    private float score;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @Override
    public int compareTo(Artist o) {
        // Reverse order
        return Float.compare(o.getScore(), getScore());
    }
}
