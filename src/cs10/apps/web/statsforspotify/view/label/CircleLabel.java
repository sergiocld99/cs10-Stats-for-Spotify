package cs10.apps.web.statsforspotify.view.label;

import cs10.apps.web.statsforspotify.utils.CommonUtils;

import javax.swing.*;
import java.awt.*;

public class CircleLabel extends JLabel {
    private String title;
    private int value, average = 60, originalValue;
    private final boolean inverted;

    public CircleLabel(String title, boolean inverted){
        this.title = title;
        this.inverted = inverted;
        // don't use a width minor than 85!
        setPreferredSize(new Dimension(85, 100));
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
        if (value <= 0){
            System.out.println("CL || Empty value for " + title);
            setVisible(false);
        } else setVisible(true);
    }

    public void setAverage(int average) {
        this.average = average;
    }

    public void setOriginalValue(int originalValue) {
        this.originalValue = originalValue;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D graphics2D = (Graphics2D) g.create();

        // Circle color
        if (originalValue > 0){
            if (value == originalValue) graphics2D.setColor(CircleColors.LIGHT_BLUE_COLOR.get());
            else if (value < originalValue) graphics2D.setColor(Color.red);
            else graphics2D.setColor(CircleColors.GREEN_COLOR.get());
        } else if (!inverted) {
            if (value < average) graphics2D.setColor(CircleColors.ORANGE_COLOR.get());
            else graphics2D.setColor(CircleColors.DARK_GREEN_COLOR.get());
        } else {
            if (value > average) graphics2D.setColor(CircleColors.ORANGE_COLOR.get());
            else graphics2D.setColor(CircleColors.DARK_GREEN_COLOR.get());
        }

        // Draw circle
        graphics2D.fillOval(25,30,50,50);

        // Draw Score Number
        graphics2D.setColor(Color.white);
        CommonUtils.drawCenteredString(graphics2D, String.valueOf(value),
                new Rectangle(25,30,50,50), new Font("Arial", Font.BOLD, 24));

        // Draw Score Title
        graphics2D.setColor(Color.black);
        CommonUtils.drawCenteredString(graphics2D, title,
                new Rectangle(0,10,100,20), new Font("Arial", Font.BOLD, 12));
    }
}
