package cs10.apps.web.statsforspotify.view.label;

import java.awt.*;

public class PeakLabel extends CircleLabel {

    public PeakLabel(){
        super("--", true);
        setVisible(false);
    }

    public void changeToPeak(){
        setTitle("Track Peak");
        setInverted(true);
        setAverage(1);
    }

    public void changeToLastFM(){
        setTitle("Scrobbles");
        setInverted(false);
        setAverage(50);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }
}
