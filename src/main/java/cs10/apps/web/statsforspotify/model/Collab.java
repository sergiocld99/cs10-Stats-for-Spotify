package cs10.apps.web.statsforspotify.model;

import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import cs10.apps.web.statsforspotify.io.ArtistDirectory;
import cs10.apps.web.statsforspotify.io.Library;

public class Collab implements Comparable<Collab> {
    private float totalScore;
    private String name, artists;

    public static float calcScore(String artists, int actualPopularity){
        Library library = Library.getInstance();
        float score = 0, multiplier = 1;

        for (String artist : artists.split(", ")){
            ArtistDirectory a = library.getArtistByName(artist);
            if (a != null) score += a.getArtistScore() * multiplier;
            multiplier /= 2;
        }

        return score * actualPopularity / 100f;
    }

    public static float calcScore(ArtistSimplified[] artists, int actualPopularity){
        Library library = Library.getInstance();
        float score = 0, multiplier = 1;

        for (ArtistSimplified artist : artists){
            ArtistDirectory a = library.getArtistByName(artist.getName());
            if (a != null) score += a.getArtistScore() * multiplier;
            multiplier /= 2;
        }

        return score * actualPopularity / 100f;
    }

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
