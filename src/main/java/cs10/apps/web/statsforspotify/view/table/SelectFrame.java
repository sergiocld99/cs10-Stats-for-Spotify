package cs10.apps.web.statsforspotify.view.table;

import cs10.apps.desktop.statsforspotify.view.CustomTableModel;
import cs10.apps.web.statsforspotify.app.AppFrame;
import cs10.apps.web.statsforspotify.model.BigRanking;
import cs10.apps.web.statsforspotify.model.SimpleRanking;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class SelectFrame extends AppFrame {
    private final SimpleRanking[] rankings;
    private final Calendar calendar;
    private final BigRanking actualR;

    public SelectFrame(SimpleRanking[] rankings, BigRanking actualR) throws HeadlessException {
        this.rankings = rankings;
        this.actualR = actualR;
        calendar = Calendar.getInstance();
    }

    public void init(){
        setTitle("Please, select one of these");
        setSize(400, 600);

        System.out.println("Preparing table...");
        String[] columnsNames = new String[]{"Rank Code", "Date"};
        CustomTableModel model = new CustomTableModel(columnsNames, 0);
        JTable table = new JTable(model);
        table.setRowHeight(50);
        super.customizeTexts(table, model);

        for (int i=0; i<rankings.length-1; i++) {
            model.addRow(toRow(rankings[i]));
        }

        System.out.println("All done");
        getContentPane().add(BorderLayout.CENTER, new JScrollPane(table));
        setResizable(false);
        setVisible(true);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) return;
                new Thread(() -> {
                    String selectCode = rankings[table.getSelectedRow()].getCode();
                    System.out.println("Opening ranking #"+selectCode);
                    setTitle("Please wait...");
                    table.setEnabled(false);
                    RankingFrame rankingFrame = new RankingFrame(selectCode, actualR);
                    rankingFrame.init();
                    dispose();
                }).start();
            }
        });
    }

    private Object[] toRow(SimpleRanking ranking){
        return new Object[]{"#"+ranking.getCode(), formatCustomDate(ranking.getDate())};
    }

    private String formatCustomDate(String date){
        String[] params = date.split("-");
        calendar.set(Calendar.YEAR, Integer.parseInt(params[0]));
        calendar.set(Calendar.MONTH, Integer.parseInt(params[1])-1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(params[2]));
        Date date1 = calendar.getTime();
        DateFormat simpleDateFormat = DateFormat.getDateInstance(DateFormat.LONG);
        return simpleDateFormat.format(date1);
    }
}
