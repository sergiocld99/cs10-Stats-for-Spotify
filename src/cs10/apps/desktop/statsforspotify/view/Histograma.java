package cs10.apps.desktop.statsforspotify.view;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Juan Martín Ercoli
 * @version 1
 */
public class Histograma extends JPanel{

    private int alturaHistograma = 200;

    private int anchoBarra = 50;
    private int gapEntreBarra = 5;

    private JPanel panelBarra;
    private JPanel panelEtiqueta;

    private List<Barra> barras = new ArrayList<>();

    public Histograma() {
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(new BorderLayout());

        panelBarra = new JPanel(new GridLayout(1, 0, gapEntreBarra, 0));
        Border externo = new MatteBorder(1, 1, 1, 1, Color.BLACK);
        Border interno = new EmptyBorder(10, 10, 0, 10);
        Border compuesto = new CompoundBorder(externo, interno);
        panelBarra.setBorder(compuesto);

        panelEtiqueta = new JPanel(new GridLayout(1, 0, gapEntreBarra, 0));
        panelEtiqueta.setBorder(new EmptyBorder(5, 10, 0, 10));

        add(panelBarra, BorderLayout.CENTER);
        add(panelEtiqueta, BorderLayout.PAGE_END);
    }

    public void agregarColumna(String etiqueta, int valor, Color color) {
        Barra barra = new Barra(etiqueta, valor, color);
        barras.add(barra);
    }

    public void formalizarHistograma() {
        panelBarra.removeAll();
        panelEtiqueta.removeAll();

        int max = 0;
        for(Barra barra: barras)
            max = Math.max(max, barra.getValor());

        if(max > 0)
            for(Barra barra: barras) {
                JLabel etiqueta = new JLabel(Integer.toString(barra.getValor()));

                etiqueta.setHorizontalTextPosition(JLabel.CENTER);
                etiqueta.setHorizontalAlignment(JLabel.CENTER);

                etiqueta.setVerticalTextPosition(JLabel.TOP);
                etiqueta.setVerticalAlignment(JLabel.BOTTOM);

                int alturaBarra = (barra.getValor() * alturaHistograma) / max;
                Icon icon = new ColorIcon(barra.getColor(), anchoBarra, alturaBarra);
                etiqueta.setIcon(icon);

                panelBarra.add(etiqueta);

                JLabel etiquetaBarra = new JLabel(barra.getEtiqueta());
                etiquetaBarra.setHorizontalAlignment(JLabel.CENTER);
                panelEtiqueta.add(etiquetaBarra);
            }
        else
            for(Barra barra: barras) {
                JLabel etiqueta = new JLabel(Integer.toString(barra.getValor()));

                etiqueta.setHorizontalTextPosition(JLabel.CENTER);
                etiqueta.setHorizontalAlignment(JLabel.CENTER);

                etiqueta.setVerticalTextPosition(JLabel.TOP);
                etiqueta.setVerticalAlignment(JLabel.BOTTOM);

                int alturaBarra = 0;
                Icon icon = new ColorIcon(barra.getColor(), anchoBarra, alturaBarra);
                etiqueta.setIcon(icon);

                panelBarra.add(etiqueta);

                JLabel etiquetaBarra = new JLabel(barra.getEtiqueta());
                etiquetaBarra.setHorizontalAlignment(JLabel.CENTER);
                panelEtiqueta.add(etiquetaBarra);
            }
    }

    public void cambiarValorBarra(int pos, int valor){
        barras.get(pos-1).setValor(valor);
    }

    private class Barra {

        private String etiqueta;
        private int valor;
        private Color color;

        public Barra(String etiqueta, int valor, Color color) {
            this.etiqueta = etiqueta;
            this.valor = valor;
            this.color = color;
        }

        public String getEtiqueta() {
            return etiqueta;
        }

        public void setEtiqueta(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        public int getValor() {
            return valor;
        }

        public void setValor(int valor) {
            this.valor = valor;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }
    }

    private class ColorIcon implements Icon {

        private int sombra = 5;

        private Color color;
        private int ancho;
        private int alto;

        public ColorIcon(Color color, int ancho, int alto) {
            this.color = color;
            this.ancho = ancho;
            this.alto = alto;
        }

        public int getSombra() {
            return sombra;
        }

        public void setSombra(int sombra) {
            this.sombra = sombra;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public int getAncho() {
            return ancho;
        }

        public void setAncho(int ancho) {
            this.ancho = ancho;
        }

        public int getAlto() {
            return alto;
        }

        public void setAlto(int alto) {
            this.alto = alto;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillRect(x, y, ancho - sombra, alto);
            g.setColor(Color.GRAY);
            g.fillRect(x + ancho - sombra, y + sombra, sombra, alto - sombra);
        }

        @Override
        public int getIconWidth() {
            return getAncho();
        }

        @Override
        public int getIconHeight() {
            return getAlto();
        }
    }

}
