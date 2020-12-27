package cs10.apps.desktop.statsforspotify.model;

import com.wrapper.spotify.model_objects.specification.Track;
import cs10.apps.web.statsforspotify.io.SongFile;
import cs10.apps.web.statsforspotify.model.PopularityStatus;
import cs10.apps.web.statsforspotify.utils.CommonUtils;

import java.text.DecimalFormat;

public class Song implements Comparable<Song> {
    private String name, artists, infoStatus, imageUrl, id;
    private SongFile songFile;

    // status is necessary for the new ones
    private Status status;

    private int rank, change, previousRank, popularity;
    private long timestamp;
    private boolean mark, repeated;

    public Song(){ }

    public Song(String name) {
        this.setName(name);
    }

    public Song(Track track){
        this.id = track.getId();
        this.name = track.getName();
        this.artists = CommonUtils.combineArtists(track.getArtists());
        this.imageUrl = track.getAlbum().getImages()[track.getAlbum().getImages().length-1].getUrl();
        this.popularity = track.getPopularity();
    }

    public SongFile getSongFile() {
        return songFile;
    }

    public void setSongFile(SongFile songFile) {
        this.songFile = songFile;
    }

    public int getPreviousRank() {
        return previousRank;
    }

    public void setPreviousRank(int previousRank) {
        this.previousRank = previousRank;
    }

    public int getChange() {
        return change;
    }

    public void setChange(int change) {
        this.change = change;
    }

    public String getInfoStatus() {
        return infoStatus;
    }

    public void setInfoStatus(String infoStatus) {
        this.infoStatus = infoStatus;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
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

    public String getMainArtist(){
        return artists.split(", ")[0];
    }

    public void setArtists(String artists) {
        this.artists = artists;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public PopularityStatus getPopularityStatus(){
        int firstPopularity = songFile.getAppearances().get(0).getPopularity();

        if (firstPopularity == popularity) return PopularityStatus.NORMAL;
        else if (firstPopularity > popularity) return PopularityStatus.DECREASING;
        else return PopularityStatus.INCREASING;
    }

    public boolean isMark() {
        return mark;
    }

    public void setMark(boolean mark) {
        this.mark = mark;
    }

    public boolean isRepeated() {
        return repeated;
    }

    public void setRepeated(boolean repeated) {
        this.repeated = repeated;
    }

    // PERSONAL METHODS
    public void validate(){
        if (infoStatus.isEmpty()){
            setStatus(Status.NOTHING);
        } else if (infoStatus.charAt(0) == 'T'){
            setInfoStatus("NEW");
            setStatus(Status.NEW);
        } else {
            setChange(Integer.parseInt(infoStatus));
            if (infoStatus.charAt(0) == '-'){
                setStatus(Status.DOWN);
            } else setStatus(Status.UP);
        }

        setPreviousRank(getRank() + getChange());
    }

    public void validateWeb() {
        setChange(getPreviousRank() - getRank());
        if (getChange() == 0) setStatus(Status.NOTHING);
        else if (getChange() < 0) setStatus(Status.DOWN);
        else setStatus(Status.UP);
    }

    public String toStringWithArtist(){
        return getName() + " by " + getArtists();
    }

    public String toStringWithoutArtists(){
        return "#" + new DecimalFormat("#00").format(getRank()) + " - " + getName() +
                " (" + (getChange() > 0 ? "+" + getChange() : getChange()) + ")";
    }

    public String toStringForRanking() {
        return "#" + new DecimalFormat("#00").format(getRank()) + " - " + getName() +
                " - " + getArtists() + " (" + (getChange() > 0 ? "+" + getChange() : getChange()) + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Song song = (Song) o;

        if (!name.equals(song.name)) return false;
        return artists.equals(song.artists);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + artists.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(Song o) {
        return this.getName().compareTo(o.getName());
    }
}
