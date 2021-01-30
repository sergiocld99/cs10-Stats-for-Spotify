package cs10.apps.web.statsforspotify.model.ranking;

import cs10.apps.web.statsforspotify.model.Artist;

public class RankingArtist extends Artist {
    private final String id;

    public RankingArtist(String id, String name, float score) {
        this.id = id;
        setName(name);
        setScore(score);
    }

    public void incrementScore(float v){
        setScore(getScore() + v);
    }

    public String getId() {
        return id;
    }
}
