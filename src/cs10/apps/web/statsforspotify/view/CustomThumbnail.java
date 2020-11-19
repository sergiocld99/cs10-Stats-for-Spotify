package cs10.apps.web.statsforspotify.view;

import cs10.apps.web.statsforspotify.utils.CommonUtils;

import javax.swing.*;
import java.awt.*;

public class CustomThumbnail extends JLabel {
    private final int size;

    public CustomThumbnail(int size) {
        this.size = size;
    }

    public void setCover(String imageUrl){
        this.setIcon(CommonUtils.downloadImage(imageUrl, size));
    }

    @Override
    public void setIcon(Icon icon) {
        super.setIcon(icon);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        /*Graphics2D graphics2D = (Graphics2D) g.create();
        graphics2D.setPaint(Color.BLACK);

        // Transparent
        Composite originalComposite = graphics2D.getComposite();
        AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f);
        graphics2D.setComposite(alphaComposite);

        // Draw
        graphics2D.fillRect(0,0,80,80);

        // Text
        graphics2D.setComposite(originalComposite);

        if (popularity > average){
            graphics2D.setColor(Color.green);
        } else graphics2D.setColor(Color.orange);

        //graphics2D.setFont(new Font("Arial", Font.BOLD, 40));
        CommonUtils.drawCenteredString(graphics2D, String.valueOf(popularity),
                new Rectangle(40,0,40,40), new Font("Arial", Font.BOLD, 24));
        //graphics2D.drawString(String.valueOf(popularity), 20, 30);*/
    }
}
