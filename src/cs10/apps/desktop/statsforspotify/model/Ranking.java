package cs10.apps.desktop.statsforspotify.model;

import cs10.apps.web.statsforspotify.model.CustomList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Ranking extends CustomList<Song> {
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
        if (super.size() > 50) {
            try {
                throw new RuntimeException("Wrong class -- You should use BigRanking");
            } catch (RuntimeException e){
                e.printStackTrace();
            }
        }

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

    public Song getSong(String id){
        for (Song s : this){
            if (s.getId().equals(id)){
                return s;
            }
        }

        return null;
    }
}
