package cs10.apps.desktop.statsforspotify.view;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class CustomTableModel extends DefaultTableModel {
    private final List<Boolean> highlighted = new ArrayList<>();

    public CustomTableModel(Object[] columnNames, int rowCount) {
        super(columnNames, rowCount);

        for (int i=0; i<100; i++)
            highlighted.add(false);
    }

    public void setHighlighted(int row) {
        highlighted.set(row, true);
        fireTableRowsUpdated(row, row);
    }

    public boolean isHighlighted(int row){
        return highlighted.get(row);
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
