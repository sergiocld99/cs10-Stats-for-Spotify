package cs10.apps.web.statsforspotify.view;

import cs10.apps.web.statsforspotify.utils.CommonUtils;

import javax.swing.*;
import java.awt.*;

public class CircleLabel extends JLabel {
    private final String title;
    private int value, average = 60, originalValue;

    private static final Color RED_COLOR = Color.red;
    private static final Color ORANGE_COLOR = new Color(250,100,0);
    private static final Color GREEN_COLOR = new Color(0,200,100);
    private static final Color DARK_GREEN_COLOR = new Color(0,100,0);
    private static final Color LIGHT_BLUE_COLOR = new Color(0,100,200);

    public CircleLabel(String title){
        this.title = title;
        setPreferredSize(new Dimension(85, 100));
    }

    public void setValue(int value) {
        this.value = value;
        if (value == 0){
            System.out.println("Empty value for " + title);
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
            if (value == originalValue) graphics2D.setColor(LIGHT_BLUE_COLOR);
            else if (value < originalValue) graphics2D.setColor(RED_COLOR);
            else graphics2D.setColor(GREEN_COLOR);
        } else {
            if (value < average) graphics2D.setColor(ORANGE_COLOR);
            else graphics2D.setColor(DARK_GREEN_COLOR);
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
