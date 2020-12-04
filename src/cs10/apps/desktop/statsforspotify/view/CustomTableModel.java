package cs10.apps.desktop.statsforspotify.view;

import javax.swing.table.DefaultTableModel;

public class CustomTableModel extends DefaultTableModel {

    public CustomTableModel(Object[] columnNames, int rowCount) {
        super(columnNames, rowCount);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return this.getValueAt(0, columnIndex).getClass();
    }
}
