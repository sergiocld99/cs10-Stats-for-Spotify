package cs10.apps.desktop.statsforspotify.model;

import java.util.*;

public class Artist {
    private String name;
    private final List<Song> songs;
    private final long[] scores;

    public Artist() {
        this.songs = new ArrayList<>();
        this.scores = new long[3];
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // PERSONAL METHODS
    public void addScore(long score, int index) {
        scores[index] = score;
    }

    public long getRecentScore(){
        return scores[0] + scores[1] / 2 - scores[2];
    }

    public void addSong(String name){
        this.songs.add(new Song(name));
    }

    public boolean hasSong(String name){
        for (Song s : songs){
            if (s.getName().equals(name)){
                return true;
            }
        }

        return false;
    }

    public Song selectRandomSong(){
        return songs.get(new Random().nextInt(songs.size()));
    }

    public void sortSongs(){
        Collections.sort(songs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Artist artist = (Artist) o;

        return name.equals(artist.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }
}
