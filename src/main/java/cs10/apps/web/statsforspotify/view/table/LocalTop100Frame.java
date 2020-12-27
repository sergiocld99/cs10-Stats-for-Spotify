package cs10.apps.web.statsforspotify.view.table;

import cs10.apps.desktop.statsforspotify.view.CustomTableModel;
import cs10.apps.web.statsforspotify.app.AppFrame;
import cs10.apps.web.statsforspotify.io.ArtistDirectory;
import cs10.apps.web.statsforspotify.model.Artist;

import javax.swing.*;
import java.awt.*;

public class LocalTop100Frame extends AppFrame {
    private final ArtistDirectory[] artists;

    public LocalTop100Frame(ArtistDirectory[] artists) throws HeadlessException {
        this.artists = artists;
    }

    public void init(){
        setTitle("Top 100 Artists - Local Data");
        setSize(600, 600);

        System.out.println("Preparing table...");
        String[] columnsNames = new String[]{"Rank", "Artist", "Score", "Preference"};
        CustomTableModel model = new CustomTableModel(columnsNames, 0);
        JTable table = new JTable(model);
        table.setRowHeight(50);
        super.customizeTexts(table, model);

        double maxPreference = Math.log(1 + artists[0].getArtistScore()) +
                Math.log(1 + artists[artists.length / 2].getArtistScore());

        for (int i=0; i<artists.length; i++)
            model.addRow(toRow(artists[i], maxPreference,i+1));

        System.out.println("All done");
        getContentPane().add(BorderLayout.CENTER, new JScrollPane(table));
        setResizable(false);
        setVisible(true);
    }

    private Object[] toRow(ArtistDirectory artist, double maxPreference, int number){
        return new Object[]{"#"+number, artist.getArtistName(),
                String.format("%.2f", artist.getArtistScore()),
                String.format("%.2f", Math.log(1 + artist.getArtistScore()) * 100
                        / maxPreference) + "%"};
    }
}
