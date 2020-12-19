package cs10.apps.web.statsforspotify.view.table;

import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.desktop.statsforspotify.view.CustomTableModel;
import cs10.apps.web.statsforspotify.app.AppFrame;
import cs10.apps.web.statsforspotify.model.BigRanking;
import cs10.apps.web.statsforspotify.model.Collab;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class CollabScoresFrame extends AppFrame {
    private final BigRanking bigRanking;
    private final ArrayList<Collab> list;

    public CollabScoresFrame(BigRanking bigRanking) throws HeadlessException {
        this.bigRanking = bigRanking;
        list = new ArrayList<>();
        buildArray();
        Collections.sort(list);
    }

    private void buildArray(){
        for (Song s : bigRanking){
            if (s.getArtists().contains(",")){
                Collab collab = new Collab();
                collab.setArtists(s.getArtists());
                collab.setName(s.getName());
                collab.setTotalScore(Collab.calcScore(s.getArtists(), s.getPopularity()));
                list.add(collab);
            }
        }
    }

    public void init(){
        setTitle("Collab Score from Current Ranking");
        setSize(1100, 600);

        System.out.println("Preparing table...");
        String[] columnsNames = new String[]{"Rank", "Song Name", "Artists", "Score", "Preference"};
        CustomTableModel model = new CustomTableModel(columnsNames, 0);
        JTable table = new JTable(model);
        table.setRowHeight(50);
        super.customizeTexts(table, model);
        this.modifyColumnsWidth(table);

        double maxPreference = Math.log(1 + list.get(0).getTotalScore()) +
                Math.log(1 + list.get(list.size() / 2).getTotalScore());
        for (int i=0; i<list.size(); i++)
            model.addRow(toRow(list.get(i), maxPreference,i+1));

        System.out.println("All done");
        getContentPane().add(BorderLayout.CENTER, new JScrollPane(table));
        setResizable(false);
        setVisible(true);
    }

    private void modifyColumnsWidth(JTable table){
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(400);
        table.getColumnModel().getColumn(2).setPreferredWidth(420);
        table.getColumnModel().getColumn(3).setPreferredWidth(70);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);
    }

    private Object[] toRow(Collab collab, double maxPreference, int number){
        return new Object[]{"#"+number, collab.getName(), collab.getArtists(),
                collab.getTotalScore(), String.format("%.2f",
                Math.log(1 + collab.getTotalScore()) * 100 / maxPreference) + "%"};
    }
}
