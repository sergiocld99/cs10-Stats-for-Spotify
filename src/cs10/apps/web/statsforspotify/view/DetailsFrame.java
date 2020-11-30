package cs10.apps.web.statsforspotify.view;

import cs10.apps.desktop.statsforspotify.view.Histograma;

import javax.swing.*;
import java.awt.*;

public abstract class DetailsFrame extends JFrame {
    private final JLabel lblHistograma;
    protected Histograma histograma;

    public DetailsFrame(String title, String labelText, int width, int height) {
        this.lblHistograma = new JLabel(labelText);
        this.histograma = new Histograma();
        setIconImage(new ImageIcon("appicon.png").getImage());
        setMinimumSize(new Dimension(width, height));
        setTitle(title);
    }

    public void init(){
        JScrollPane spPrincipal = new JScrollPane();
        JPanel pContenedorPrincipal = new JPanel();
        JPanel pContenedorHistograma = new JPanel();

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

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;
        pContenedorHistograma.add(lblHistograma, gbc);

        fillDetails();
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
        setVisible(true);
        pack();
    }

    protected abstract void fillDetails();
}
