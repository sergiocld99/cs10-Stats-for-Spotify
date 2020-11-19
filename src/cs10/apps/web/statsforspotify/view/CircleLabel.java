package cs10.apps.web.statsforspotify.view;

import cs10.apps.web.statsforspotify.utils.CommonUtils;

import javax.swing.*;
import java.awt.*;

public class CircleLabel extends JLabel {
    private final String title;
    private int value, average = 60;

    public CircleLabel(String title){
        this.title = title;
        setPreferredSize(new Dimension(85, 100));
    }

    public void setValue(int value) {
        this.value = value;
        //repaint();
    }

    public void setAverage(int average) {
        this.average = average;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D graphics2D = (Graphics2D) g.create();

        // Circle color
        if (value < (average / 2)){
            graphics2D.setColor(Color.red);
        } else if (value < average){
            graphics2D.setColor(new Color(250,100,0));
        } else {
            graphics2D.setColor(new Color(0,100,0));
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
