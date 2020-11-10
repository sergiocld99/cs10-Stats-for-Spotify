package cs10.apps.desktop.statsforspotify.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Ranking extends ArrayList<Song> {
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Song> filterByStatus(Status status){
        List<Song> songs = new ArrayList<>();

        for (Song s : this){
            if (s.getStatus() == status){
                songs.add(s);
            }
        }

        return songs;
    }

    public long getCode(){
        long sum = 0;

        for (Song s : this){
            sum += s.getRank() * s.getName().charAt(0);
        }

        return sum;
    }

    public void sortByDefault(){
        this.sort(Comparator.comparingInt(Song::getRank));
    }

    public void sortByChange(){
        this.sort(Comparator.comparingInt(Song::getChange));
    }

    public Song getBiggestGain(){
        this.sortByChange();
        return get(size()-1);
    }

    public Song getBiggestLoss(){
        this.sortByChange();
        return get(0);
    }
}
