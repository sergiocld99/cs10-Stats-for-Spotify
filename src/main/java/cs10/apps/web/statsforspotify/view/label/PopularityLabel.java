package cs10.apps.web.statsforspotify.view.label;

public class PopularityLabel extends CircleLabel {

    public PopularityLabel(){
        super("Popularity", false);
        setVisible(true);
    }

    @Override
    public void setValue(int value) {
        if (getOriginalValue() != 0 && value > getOriginalValue() + 2) setTitle("Trending");
        else setTitle("Popularity");

        super.setValue(value);
    }
}
