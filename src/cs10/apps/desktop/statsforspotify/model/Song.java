package cs10.apps.desktop.statsforspotify.model;

import java.text.DecimalFormat;

public class Song {
    private Status status;
    private String name, artists, infoStatus;
    private int rank, change, previousRank;
    private long timestamp;

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

    // PERSONAL METHODS
    public void validate(){
        if (infoStatus.isEmpty()){
            setStatus(Status.NOTHING);
        } else if (infoStatus.charAt(0) == 'T'){
            setStatus(Status.NEW);
        } else {
            setChange(Integer.parseInt(infoStatus));
            if (infoStatus.charAt(0) == '-'){
                setStatus(Status.DOWN);
            } else setStatus(Status.UP);
        }

        setPreviousRank(getRank() + getChange());
    }

    public String toStringWithoutArtists(){
        return "#" + new DecimalFormat("#00").format(getRank()) + " - " + getName() +
                " (" + (getChange() > 0 ? "+" + getChange() : getChange()) + ")";
    }

    @Override
    public String toString() {
        return "#" + new DecimalFormat("#00").format(getRank()) + " - " + getName() +
                " - " + getArtists() + " (" + (getChange() > 0 ? "+" + getChange() : getChange()) + ")";
    }
}
