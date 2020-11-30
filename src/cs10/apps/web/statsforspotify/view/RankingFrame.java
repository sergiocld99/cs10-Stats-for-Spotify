package cs10.apps.web.statsforspotify.view;

import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.desktop.statsforspotify.model.Status;
import cs10.apps.desktop.statsforspotify.utils.OldIOUtils;
import cs10.apps.desktop.statsforspotify.view.RankingModel;
import cs10.apps.web.statsforspotify.app.PersonalChartApp;
import cs10.apps.web.statsforspotify.model.BigRanking;
import cs10.apps.web.statsforspotify.utils.IOUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class RankingFrame extends JFrame {
    private final BigRanking bigRanking, actualRanking;
    private RankingModel model;
    private JTable table;

    public RankingFrame(String rankingCode, BigRanking actualRanking) throws HeadlessException {
        this.bigRanking = IOUtils.getRanking(rankingCode, true);
        this.actualRanking = actualRanking;
    }

    public void init() {
        setTitle("Ranking #"+bigRanking.getCode());
        setIconImage(new ImageIcon("appicon.png").getImage());
        setSize(1000, 600);

        // Table
        String[] columnNames = new String[]{
                "Status", "Rank", "Song Name", "Artist", "Popularity", "Current Change"
        };

        model = new RankingModel(columnNames, 0);
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
        table.getColumnModel().getColumn(2).setPreferredWidth(250);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);

        for (int i=1; i<model.getColumnCount(); i++){
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }

    private Object[] toRow(Song song){
        return new Object[]{OldIOUtils.getImageIcon(song.getStatus()),
                song.getRank(), song.getName(), song.getArtists(),
                song.getPopularity(), song.getInfoStatus()};
    }
}
