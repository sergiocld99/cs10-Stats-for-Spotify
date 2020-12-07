package cs10.apps.web.statsforspotify.view;

import cs10.apps.desktop.statsforspotify.view.CustomTableModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class CustomTableCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        CustomTableModel model = (CustomTableModel) table.getModel();
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (model.isHighlighted(row)) c.setBackground(Color.yellow);
        else c.setBackground(c.getBackground());

        if (value instanceof Icon){
            Icon i = (Icon) value;
            JLabel label = (JLabel) c;
            label.setIcon(i);
            label.setText("");
        }

        return c;
    }
}
