package cs10.apps.web.statsforspotify.model.ranking;

import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import cs10.apps.desktop.statsforspotify.model.Ranking;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.desktop.statsforspotify.model.Status;
import cs10.apps.web.statsforspotify.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BigRanking extends Ranking {
    private static final int TOP_INDEX = 1;
    private BigRanking rankingToCompare;
    private String date;

    private long code;
    private int repeatedQuantity;

    public BigRanking(){ }

    public BigRanking(Collection<Track> tracks){
        for (Track track : tracks){
            Song song = new Song(track);
            song.setRank(size() + 1);
            //if (size() % 10 == 0) code += track.getPopularity();
            this.add(song);
        }
    }

    public void updateRepeated(Collection<Track> repeats){
        this.repeatedQuantity = repeats.size();

        for (Track t : repeats){
            Song s = getSong(t.getId());
            if (s == null) System.err.println("Something gone wrong with " +
                    CommonUtils.toString(t));
            else s.setRepeated(true);
        }
    }

    public void updateAllStatus(BigRanking rankingToCompare){
        for (Song song : this){
            Song prevS = rankingToCompare.getSong(song.getId());
            if (prevS == null) song.setStatus(Status.NEW);
            else {
                prevS.setMark(true);
                song.setChange(prevS.getRank() - song.getRank());
                if (song.getChange() == 0) song.setStatus(Status.NOTHING);
                else if (song.getChange() < 0) song.setStatus(Status.DOWN);
                else song.setStatus(Status.UP);
            }
        }
    }

    /**
     * Inverted operation of "updateAllStatus(rankingToCompare)"
     * @param actualRanking the current user's ranking
     */
    public void retrieveAllStatus(BigRanking actualRanking){
        for (Song song : this){
            Song prevS = actualRanking.getSong(song.getId());
            if (prevS == null) song.setStatus(Status.LEFT);
            else {
                // inverted
                song.setChange(song.getRank() - prevS.getRank());
                if (song.getChange() == 0) song.setStatus(Status.NOTHING);
                else if (song.getChange() < 0) song.setStatus(Status.DOWN);
                else song.setStatus(Status.UP);
            }
        }
    }

    public List<Song> getNonMarked(){
        List<Song> result = new ArrayList<>();

        for (Song song : this){
            if (song.isNotMarked())
                result.add(song);
        }

        return result;
    }

    public void addRankingToCompare(BigRanking ranking){
        rankingToCompare = ranking;
    }

    public void add(Paging<Track> paging){
        if (super.isEmpty()) addWithoutCheckingRepeats(paging);
        else addCheckingRepeats(paging);
    }

    private void addWithoutCheckingRepeats(Paging<Track> paging){
        for (Track track : paging.getItems()){
            add(track);
        }
    }

    private void addCheckingRepeats(Paging<Track> paging){
        int limit = super.size();

        for (Track track : paging.getItems()){
            if (!alreadyAdded(track.getId(), limit)){
                add(track);
            }
        }
    }

    private boolean alreadyAdded(String id, int limit){
        int checked = 0;

        for (Song s : this){
            if (s.getId().equals(id)) return true;
            if (++checked == limit) return false;
        }

        return false;
    }

    private void add(Track track){
        Song song = new Song();
        song.setId(track.getId());
        song.setRank(size()+TOP_INDEX);
        song.setName(track.getName());
        song.setArtists(CommonUtils.combineArtists(track.getArtists()));
        song.setImageUrl(track.getAlbum().getImages()[0].getUrl());
        song.setPopularity(track.getPopularity());
        song.setStatus(Status.NOTHING);

        // compare with previous ranking
        Song prevS = rankingToCompare.getSong(track.getId());
        if (prevS == null) song.setStatus(Status.NEW);
        else {
            prevS.setMark(true);
            song.setChange(prevS.getRank() - song.getRank());
            if (song.getChange() == 0) song.setStatus(Status.NOTHING);
            else if (song.getChange() < 0) song.setStatus(Status.DOWN);
            else song.setStatus(Status.UP);
        }

        //code += song.getPopularity();
        super.add(song);
    }

    @Override
    public long getCode() {
        return code;
    }

    public int getAverage() {
        return (int) (code / 16);
    }

    @Override
    public boolean addAll(Collection<? extends Song> c) {
        for (Song s : c) this.add(s);
        return true;
    }

    @Override
    public boolean add(Song song) {
        if (size() % 10 == 0) code += song.getPopularity();
        return super.add(song);
    }

    public List<String> getLefts(){
        List<String> ids = new ArrayList<>();
        for (Song s : rankingToCompare){
            if (s.isNotMarked()){
                ids.add(s.getId());
            }
        }
        return ids;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public int getRepeatedQuantity() {
        return repeatedQuantity;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }
}
