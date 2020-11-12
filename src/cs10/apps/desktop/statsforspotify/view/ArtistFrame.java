package cs10.apps.desktop.statsforspotify.view;

import cs10.apps.desktop.statsforspotify.model.Artist;

import javax.swing.*;
import java.awt.*;

public class ArtistFrame extends JFrame {
    private final Artist artist;

    public ArtistFrame(Artist artist) throws HeadlessException {
        this.artist = artist;
    }

    public void init(){
        JScrollPane spPrincipal = new JScrollPane();
        JPanel pContenedorPrincipal = new JPanel();
        JPanel pContenedorHistograma = new JPanel();
        JLabel lblHistograma = new JLabel();
        Histograma histograma = new Histograma();

        setTitle("Stats for " + artist.getName());
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
        lblHistograma.setText("Histograma");

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;
        pContenedorHistograma.add(lblHistograma, gbc);

        histograma.agregarColumna("#1-10", artist.getTimesOn(0), Color.RED);
        histograma.agregarColumna("#11-20", artist.getTimesOn(1), Color.BLUE);
        histograma.agregarColumna("#21-30", artist.getTimesOn(2), Color.PINK);
        histograma.agregarColumna("#31-40", artist.getTimesOn(3), Color.ORANGE);
        histograma.agregarColumna("#41-50", artist.getTimesOn(4), Color.CYAN);
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
        pack();
    }
}
