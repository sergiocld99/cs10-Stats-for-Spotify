package cs10.apps.web.statsforspotify.model;

import cs10.apps.web.statsforspotify.view.CircleColors;

import java.awt.*;

public enum PopularityStatus {
    NORMAL(CircleColors.LIGHT_BLUE_COLOR.get(), Color.yellow, Color.orange),
    DECREASING(Color.red, new Color(255,125,125).brighter(),
            new Color(255,145,145)),
    INCREASING(CircleColors.GREEN_COLOR.get(), new Color(125,255,125).brighter(),
            new Color(145,255,145));

    private final Color circleLabelColor, tablePairColor, tableUnpairColor;

    PopularityStatus(Color circleLabelColor, Color tablePairColor, Color tableUnpairColor) {
        this.circleLabelColor = circleLabelColor;
        this.tablePairColor = tablePairColor;
        this.tableUnpairColor = tableUnpairColor;
    }

    public Color getCircleLabelColor() {
        return circleLabelColor;
    }

    public Color getTablePairColor() {
        return tablePairColor;
    }

    public Color getTableUnpairColor() {
        return tableUnpairColor;
    }
}
