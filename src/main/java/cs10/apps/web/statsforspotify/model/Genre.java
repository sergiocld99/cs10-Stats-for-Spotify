package cs10.apps.web.statsforspotify.model;

import java.util.LinkedList;
import java.util.List;

public class Genre implements Comparable<Genre> {
    private final String name;
    private final List<String> artists = new LinkedList<>();
    private float count;

    public Genre(String name, float count, String artist) {
        this.name = name;
        this.count = count;
        addArtist(artist);
    }

    public void addArtist(String name){
        artists.add(name);
    }

    public void increment(double v){
        count += v;
    }

    public String getName() {
        return name;
    }

    public float getCount() {
        return count;
    }

    public List<String> getArtists() {
        return artists;
    }

    @Override
    public int compareTo(Genre o) {
        return Double.compare(o.getCount(), this.getCount());
    }

    @Override
    public String toString() {
        return name + ", score: " + count + ", artists: " + artists;
    }
}
