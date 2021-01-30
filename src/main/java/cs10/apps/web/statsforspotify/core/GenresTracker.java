package cs10.apps.web.statsforspotify.core;

import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;
import cs10.apps.web.statsforspotify.model.Genre;
import cs10.apps.web.statsforspotify.model.ranking.RankingArtist;
import cs10.apps.web.statsforspotify.utils.ApiUtils;

import java.util.*;

public class GenresTracker {
    private final List<Genre> genres = new LinkedList<>();

    public void build(List<Track> resultTracks, ApiUtils apiUtils){
        List<RankingArtist> list = new LinkedList<>();

        for (Track t : resultTracks){
            for (ArtistSimplified a : t.getArtists()){
                update(a, list, t.getPopularity() / 10f);
            }
        }

        double maxCount = 0;
        Collections.sort(list);
        for (RankingArtist r : list.subList(0, Math.min(10, list.size()))){
            update(apiUtils.getArtist(r.getId()), r.getScore());
            maxCount += r.getScore();
        }

        System.out.println("The maximum score is " + maxCount);
        printStats();
    }

    private void update(ArtistSimplified a, List<RankingArtist> list, float score){
        for (RankingArtist r : list){
            if (r.getId().equals(a.getId())){
                r.incrementScore(score);
                return;
            }
        }

        list.add(new RankingArtist(a.getId(), a.getName(), score));
    }

    public void printStats(){
        if (genres.isEmpty()) {
            System.err.println("No genres found");
            return;
        }

        int count = 1;
        Collections.sort(genres);
        for (Genre g : genres) System.out.println("#" + (count++) + " - " + g);
    }

    public void update(Artist artist, float score){
        float divider = 1;

        for (String s : artist.getGenres()){
            update(s, score / divider, artist.getName());
            divider += 0.1;
        }
    }

    private void update(String genreName, float score, String artist){
        for (Genre g : genres){
            if (g.getName().equals(genreName)){
                g.increment(score);
                g.addArtist(artist);
                return;
            }
        }

        // not found
        genres.add(new Genre(genreName, score, artist));
    }

    public List<Genre> getGenres() {
        return genres;
    }
}
