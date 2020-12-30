package cs10.apps.web.statsforspotify.view.table;

import cs10.apps.desktop.statsforspotify.view.CustomTableModel;
import cs10.apps.web.statsforspotify.app.AppFrame;
import cs10.apps.web.statsforspotify.app.AppOptions;
import cs10.apps.web.statsforspotify.core.LastFmIntegration;
import cs10.apps.web.statsforspotify.model.BigRanking;
import cs10.apps.web.statsforspotify.model.Fanaticism;
import cs10.apps.web.statsforspotify.view.CustomTableCellRenderer;
import cs10.apps.web.statsforspotify.view.OptionPanes;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

public class FanaticismFrame extends AppFrame {
    private final List<Fanaticism> list;

    public FanaticismFrame(BigRanking ranking, AppOptions appOptions){
        String user = appOptions.getLastFmUser();
        if (user == null || user.isEmpty()){
            OptionPanes.message("Unknown Last FM username");
            list = null;
        } else list = LastFmIntegration.analyzeFanaticism(ranking, user);
    }

    public void init(){
        setTitle("Your Fanaticism on Current Ranking");
        setSize(650, 600);

        System.out.println("Preparing table...");
        String[] columnsNames = new String[]{"Status", "Details", "Song Name", "Artists"};
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

                if (column == model.getColumnCount() - 1 || column == model.getColumnCount() - 2)
                    getColumnModel().getColumn(column).setPreferredWidth(250);
                return renderer;
            }
        };

        table.setRowHeight(50);
        table.getTableHeader().setReorderingAllowed(false);

        for (Fanaticism fanaticism : list) {
            model.addRow(toRow(fanaticism));
        }

        System.out.println("All done");
        getContentPane().add(BorderLayout.CENTER, new JScrollPane(table));
        setResizable(false);
        setVisible(true);
    }

    private Object[] toRow(Fanaticism f){
        return new Object[]{
                new ImageIcon(f.getIconName()), f.getLabel(),
                f.getSong().getName(), f.getSong().getArtists()
        };
    }
}
