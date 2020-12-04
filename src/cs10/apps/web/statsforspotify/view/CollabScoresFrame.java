package cs10.apps.web.statsforspotify.view;

import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.desktop.statsforspotify.view.CustomTableModel;
import cs10.apps.web.statsforspotify.app.AppFrame;
import cs10.apps.web.statsforspotify.model.BigRanking;
import cs10.apps.web.statsforspotify.model.Collab;
import cs10.apps.web.statsforspotify.utils.IOUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
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
                float score = 0, multiplier = 1;

                for (String a : s.getArtists().split(", ")){
                    score += IOUtils.getArtistScore(a) * multiplier;
                    multiplier /= 2;
                }

                Collab collab = new Collab();
                collab.setArtists(s.getArtists());
                collab.setName(s.getName());
                collab.setTotalScore(score);
                list.add(collab);
            }
        }
    }

    public void init(){
        setTitle("Collab Score from Current Ranking");
        setSize(1000, 600);

        System.out.println("Preparing table...");
        String[] columnsNames = new String[]{"Song Name", "Artists", "Score"};
        CustomTableModel model = new CustomTableModel(columnsNames, 0);
        JTable table = new JTable(model);
        table.setRowHeight(50);
        super.customizeTexts(table, model);

        for (Collab c : list)
            model.addRow(toRow(c));

        System.out.println("All done");
        getContentPane().add(BorderLayout.CENTER, new JScrollPane(table));
        setResizable(false);
        setVisible(true);
    }

    private Object[] toRow(Collab collab){
        return new Object[]{collab.getName(), collab.getArtists(), collab.getTotalScore()};
    }
}
