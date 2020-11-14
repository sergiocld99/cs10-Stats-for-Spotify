package cs10.apps.desktop.statsforspotify.view;

import cs10.apps.desktop.statsforspotify.model.Artist;
import cs10.apps.desktop.statsforspotify.model.Library;
import cs10.apps.desktop.statsforspotify.model.Ranking;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.desktop.statsforspotify.utils.OldIOUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class StatsFrame extends JFrame {
    private final Ranking ranking;
    private final Library library;
    private RankingModel model;
    private JTable table;

    public StatsFrame(Ranking ranking, Library library){
        this.ranking = ranking;
        this.library = library;
    }

    public void init(){
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle(ranking.getTitle() + " - Rank #" + ranking.getCode());
        setSize(800,600);

        String[] columnNames = new String[]{
            "Icon", "Rank", "Song Name", "Artists", "Status Info"
        };

        model = new RankingModel(columnNames, 0);
        table = new JTable(model);
        table.setRowHeight(50);
        table.setAutoCreateRowSorter(true);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                String artistsNames = (String) model.getValueAt(table.getSelectedRow(), 3);
                String mainName = artistsNames.split(",")[0];
                Artist artist = library.findByName(mainName.toLowerCase());
                if (artist != null) openArtistWindow(artist);
            }
        });
        customizeTexts();

        for (Song s : ranking){
            model.addRow(toRow(s));
        }

        // LAYOUT
        setLayout(new BorderLayout());
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private Object[] toRow(Song song){
        return new Object[]{OldIOUtils.getImageIcon(song.getStatus()), song.getRank(),
                song.getName(), song.getArtists(), song.getInfoStatus()};
    }


    private void customizeTexts(){
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setHorizontalAlignment(JLabel.CENTER);

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(0);
        table.getColumnModel().getColumn(2).setPreferredWidth(250);
        table.getColumnModel().getColumn(3).setPreferredWidth(250);

        for (int i=1; i<model.getColumnCount(); i++){
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }

    private void openArtistWindow(Artist artist){
        ArtistFrame artistFrame = new ArtistFrame(artist);
        artistFrame.init();
        artistFrame.setVisible(true);
    }
}
