package cs10.apps.web.statsforspotify.view;

import java.awt.*;

public enum CircleColors {
    ORANGE_COLOR(new Color(250,100,0)),
    GREEN_COLOR(new Color(0,200,100)),
    DARK_GREEN_COLOR(new Color(0,100,0)),
    LIGHT_BLUE_COLOR(new Color(0,100,200));

    private final Color color;

    CircleColors(Color color){
        this.color = color;
    }

    public Color get() {
        return color;
    }
}
