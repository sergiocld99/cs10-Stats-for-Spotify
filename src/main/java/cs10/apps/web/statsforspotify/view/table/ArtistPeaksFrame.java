package cs10.apps.web.statsforspotify.view.table;

import cs10.apps.desktop.statsforspotify.view.CustomTableModel;
import cs10.apps.web.statsforspotify.app.AppFrame;
import cs10.apps.web.statsforspotify.io.ArtistDirectory;
import cs10.apps.web.statsforspotify.io.SongFile;
import cs10.apps.web.statsforspotify.utils.IOUtils;
import cs10.apps.web.statsforspotify.view.CustomTableCellRenderer;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ArtistPeaksFrame extends AppFrame {
    private final ArtistDirectory data;

    public ArtistPeaksFrame(ArtistDirectory data) throws HeadlessException {
        this.data = data;
    }

    public void init(){
        setTitle(data.getArtistName());
        setSize(800, 400);
        String[] columnsNames = new String[]{"Rank", "Song Name", "Peak", "Date", "Popularity"};
        CustomTableModel model = new CustomTableModel(columnsNames, 0);

        JTable table = new JTable(model) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                CustomTableCellRenderer renderer = new CustomTableCellRenderer();
                renderer.setHorizontalAlignment(JLabel.CENTER);

                switch (column){
                    case 0:
                        getColumnModel().getColumn(column).setPreferredWidth(50);
                        break;
                    case 1:
                        getColumnModel().getColumn(column).setPreferredWidth(400);
                        break;
                    case 2:
                        getColumnModel().getColumn(column).setPreferredWidth(150);
                        break;
                    case 3:
                    case 4:
                        getColumnModel().getColumn(column).setPreferredWidth(100);
                        break;
                }

                return renderer;
            }
        };

        table.getTableHeader().setDefaultRenderer(new CustomHeaderRenderer(table));
        table.setRowHeight(50);
        Collections.sort(data.getSongFiles());
        Set<String> names = new HashSet<>();
        int i = 1;

        for (SongFile s : data.getSongFiles()) {
            if (!names.contains(s.getTrackName())){
                names.add(s.getTrackName());
                model.addRow(toRow(s, i++));
            }
        }

        getContentPane().add(BorderLayout.CENTER, new JScrollPane(table));
        setResizable(false);
        setVisible(true);
    }

    private void modifyColumnsWidth(JTable table){
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(400);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
    }

    private Object[] toRow(SongFile s, int index){
        return new Object[]{"#"+index, s.getTrackName(), s.getPeak().toString(),
                IOUtils.getRankingDate(s.getPeak().getRankingCode()), s.getLastAppearance().getPopularity()};
    }
}
