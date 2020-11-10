package cs10.apps.desktop.statsforspotify.view;

import cs10.apps.desktop.statsforspotify.model.Ranking;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.desktop.statsforspotify.utils.IOUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class StatsFrame extends JFrame {
    private final Ranking ranking;
    private RankingModel model;
    private JTable table;

    // SORT BUTTONS
    //JButton btnSort1, btnSort2, btnSort3, btnSort4, btnSort5;

    public StatsFrame(Ranking ranking){
        this.ranking = ranking;
    }

    public void init(){
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle(ranking.getTitle() + " - Rank #" + ranking.getCode());
        setSize(800,600);

        String[] columnNames = new String[]{
            "Icon", "Actual Rank", "Song Name", "Artists", "Change from Previous"
        };

        model = new RankingModel(columnNames, 0);
        table = new JTable(model);
        table.setRowHeight(50);
        table.setAutoCreateRowSorter(true);
        customizeTexts();

        for (Song s : ranking){
            model.addRow(toRow(s));
        }

        // LAYOUT
        setLayout(new BorderLayout());
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private Object[] toRow(Song song){
        return new Object[]{IOUtils.getImageIcon(song.getStatus()), song.getRank(),
                song.getName(), song.getArtists(), song.getChange()};
    }


    private void customizeTexts(){
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setHorizontalAlignment(JLabel.CENTER);
        //cellRenderer.setFont(new Font("Arial", Font.BOLD, 20));

        // ignoramos la 0 porque es un icon

        for (int i=1; i<model.getColumnCount(); i++){
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }
}
