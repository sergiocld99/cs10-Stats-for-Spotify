package cs10.apps.desktop.statsforspotify.model;

import java.text.DecimalFormat;

public class Song implements Comparable<Song> {
    private Status status;
    private String name, artists, infoStatus;
    private int rank, change, previousRank;
    private long timestamp;

    // For web
    private String imageUrl, id, releaseDate;
    private int popularity;

    public Song(){ }

    public Song(String name) {
        this.setName(name);
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

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
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
