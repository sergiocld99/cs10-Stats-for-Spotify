package cs10.apps.web.statsforspotify.model;

public class BlockedItem {
    private final String id;
    private int timesUntilUnlock;

    public BlockedItem(String id) {
        this.id = id;
    }

    public void setTimesUntilUnlock(int timesUntilUnlock) {
        this.timesUntilUnlock = timesUntilUnlock;
    }

    public String getId() {
        return id;
    }

    public int getTimesUntilUnlock() {
        return timesUntilUnlock;
    }

    public void decrementTimesUntilUnlock(){
        timesUntilUnlock--;
    }

    @Override
    public String toString() {
        return timesUntilUnlock + "--" + id;
    }
}
