package cs10.apps.web.statsforspotify.view.table;

import cs10.apps.desktop.statsforspotify.view.CustomTableModel;
import cs10.apps.web.statsforspotify.app.AppFrame;
import cs10.apps.web.statsforspotify.model.Artist;

import javax.swing.*;
import java.awt.*;

public class LocalTop100Frame extends AppFrame {
    private final Artist[] artists;

    public LocalTop100Frame(Artist[] artists) throws HeadlessException {
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

        float maxPreference = artists[0].getScore() + artists[artists.length / 2].getScore();
        for (int i=0; i<artists.length; i++)
            model.addRow(toRow(artists[i], maxPreference,i+1));

        System.out.println("All done");
        getContentPane().add(BorderLayout.CENTER, new JScrollPane(table));
        setResizable(false);
        setVisible(true);
    }

    private Object[] toRow(Artist artist, float maxPreference, int number){
        return new Object[]{"#"+number, artist.getName(),
                String.format("%.2f", artist.getScore()),
                String.format("%.2f", artist.getScore() * 100 / maxPreference) + "%"};
    }
}
