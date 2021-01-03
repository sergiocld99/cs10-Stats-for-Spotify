package cs10.apps.web.statsforspotify.view.table;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class CustomHeaderRenderer implements TableCellRenderer {
    DefaultTableCellRenderer renderer;

    public CustomHeaderRenderer(JTable table){
        renderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return renderer.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
    }
}
