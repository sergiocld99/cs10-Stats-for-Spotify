package cs10.apps.desktop.statsforspotify.view;

import javax.swing.table.DefaultTableModel;

public class RankingModel extends DefaultTableModel {

    public RankingModel(Object[] columnNames, int rowCount) {
        super(columnNames, rowCount);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return this.getValueAt(0, columnIndex).getClass();
    }
}
