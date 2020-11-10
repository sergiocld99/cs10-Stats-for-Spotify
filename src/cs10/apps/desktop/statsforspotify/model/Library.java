package cs10.apps.desktop.statsforspotify.model;

import java.util.ArrayList;
import java.util.Random;

public class Library extends ArrayList<Artist> {
    private final Random random;

    public Library(){
        this.random = new Random();
    }

    public Artist findByName(String name){
        for (Artist a : this){
            if (a.getName().equals(name)){
                return a;
            }
        }

        return null;
    }

    public Artist selectRandomByScore(){
        Artist[] candidates = new Artist[]{selectRandom(), selectRandom(), selectRandom()};
        Artist result = null;
        long maxScore = 0;

        for (Artist c : candidates){
            if (c.getRecentScore() > maxScore){
                maxScore = c.getRecentScore();
                result = c;
            }
        }

        return result;
    }

    private Artist selectRandom(){
        return this.get(random.nextInt(size()));
    }
}
