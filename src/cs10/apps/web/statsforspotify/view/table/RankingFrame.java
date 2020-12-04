package cs10.apps.web.statsforspotify.view.table;

import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.desktop.statsforspotify.model.Status;
import cs10.apps.desktop.statsforspotify.utils.OldIOUtils;
import cs10.apps.desktop.statsforspotify.view.CustomTableModel;
import cs10.apps.web.statsforspotify.model.BigRanking;
import cs10.apps.web.statsforspotify.utils.IOUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class RankingFrame extends JFrame {
    private final BigRanking bigRanking, actualRanking;
    private CustomTableModel model;
    private JTable table;

    public RankingFrame(String rankingCode, BigRanking actualRanking) throws HeadlessException {
        this.bigRanking = IOUtils.getRanking(rankingCode, true);
        this.actualRanking = actualRanking;
    }

    public void init() {
        setTitle("Ranking #"+bigRanking.getCode());
        setIconImage(new ImageIcon("appicon.png").getImage());
        setSize(1200, 600);

        // Table
        String[] columnNames = new String[]{
                "Status", "Current Change", "Rank", "Song Name", "Artist", "Popularity", "Preference"
        };

        model = new CustomTableModel(columnNames, 0);
        table = new JTable(model);
        table.setEnabled(false);
        table.setRowHeight(50);
        table.setAutoCreateRowSorter(true);
        customizeTexts();

        bigRanking.retrieveAllStatus(actualRanking);
        getContentPane().add(BorderLayout.CENTER, new JScrollPane(table));

        setResizable(false);
        setVisible(true);
        buildTable();
    }

    private void buildTable(){

        for (Song s : bigRanking){
            if (s.getStatus() == Status.LEFT){
                s.setInfoStatus("LEFT");
            } else {
                if (s.getChange() == 0) s.setInfoStatus("");
                else if (s.getChange() > 0) s.setInfoStatus("+"+s.getChange());
                else s.setInfoStatus(String.valueOf(s.getChange()));
            }

            model.addRow(toRow(s));
        }
    }

    private void customizeTexts(){
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setHorizontalAlignment(JLabel.CENTER);

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(3).setPreferredWidth(250);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);

        for (int i=1; i<model.getColumnCount(); i++){
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }

    private Object[] toRow(Song song){
        return new Object[]{OldIOUtils.getImageIcon(song.getStatus()),
                song.getRank(), song.getInfoStatus(), song.getName(), song.getArtists(),
                song.getPopularity()};
    }
}
