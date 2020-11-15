package cs10.apps.web.statsforspotify.view;

import cs10.apps.desktop.statsforspotify.view.Histograma;

import javax.swing.*;
import java.awt.*;

public class ArtistFrame extends JFrame {
    private final String artist;
    private final float[] scores;

    public ArtistFrame(String artist, float[] scores) throws HeadlessException {
        this.artist = artist;
        this.scores = scores;
    }

    public void init(){
        JScrollPane spPrincipal = new JScrollPane();
        JPanel pContenedorPrincipal = new JPanel();
        JPanel pContenedorHistograma = new JPanel();
        JLabel lblHistograma = new JLabel();
        Histograma histograma = new Histograma();

        setTitle("Stats for " + artist);
        setMinimumSize(new Dimension(400, 300));

        GridBagConstraints gbc = new GridBagConstraints();
        GridBagLayout layoutPrincipal = new GridBagLayout();
        getContentPane().setLayout(layoutPrincipal);

        spPrincipal.setViewportView(pContenedorPrincipal);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(10,5,10,5);
        getContentPane().add(spPrincipal, gbc);

        GridBagLayout layoutPContenedorHistograma = new GridBagLayout();
        pContenedorHistograma.setLayout(layoutPContenedorHistograma);
        lblHistograma.setText("Score by Rank and Popularity");

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;
        pContenedorHistograma.add(lblHistograma, gbc);

        histograma.agregarColumna("#1-10", scores[0], Color.RED);
        histograma.agregarColumna("#11-20", scores[1], Color.ORANGE);
        histograma.agregarColumna("#21-30", scores[2], Color.GREEN);
        histograma.agregarColumna("#31-40", scores[3], Color.CYAN);
        histograma.agregarColumna("#41-50", scores[4], Color.MAGENTA);
        histograma.formalizarHistograma();

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.gridheight = 8;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        pContenedorHistograma.add(histograma, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 30, 0, 30);
        pContenedorPrincipal.add(pContenedorHistograma, gbc);
        setResizable(false);
        pack();
    }
}
