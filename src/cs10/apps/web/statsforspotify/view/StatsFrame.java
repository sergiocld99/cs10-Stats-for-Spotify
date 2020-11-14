package cs10.apps.web.statsforspotify.view;

import cs10.apps.desktop.statsforspotify.model.*;
import cs10.apps.desktop.statsforspotify.view.ArtistFrame;
import cs10.apps.desktop.statsforspotify.view.RankingModel;
import cs10.apps.web.statsforspotify.model.TopTerms;
import cs10.apps.web.statsforspotify.utils.ApiUtils;
import cs10.apps.web.statsforspotify.utils.IOUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class StatsFrame extends JFrame {
    private final ApiUtils apiUtils;
    private final Library library;
    private RankingModel model;
    private Ranking ranking;
    private JTable table;
    private String username;

    public StatsFrame(ApiUtils apiUtils, Library library) throws HeadlessException {
        this.apiUtils = apiUtils;
        this.library = library;
    }

    public void init() throws Exception {
        username = apiUtils.getUser().getDisplayName();
        setTitle(username + " - Your Spotify Stats");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);

        // Menu Bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem jmiOpen = new JMenuItem("Open...");
        JMenuItem jmiSave = new JMenuItem("Save");
        JMenuItem jmiSaveAs = new JMenuItem("Save As...");
        jmiOpen.addActionListener(e -> System.out.println("Open pressed"));
        jmiSave.addActionListener(e -> {
            if (IOUtils.saveRanking(ranking, true)){
                OptionPanes.showSavedSuccessfully();
            } else OptionPanes.showSaveError();
        });
        jmiSaveAs.addActionListener(e -> System.out.println("Save As pressed"));
        fileMenu.add(jmiOpen);
        fileMenu.add(jmiSave);
        fileMenu.add(jmiSaveAs);
        JMenu helpMenu = new JMenu("Help");
        helpMenu.addActionListener(e -> System.out.println("Help pressed"));
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        // Ranking Buttons
        JPanel buttonsPanel = new JPanel();
        JButton buttonShortTerm = new JButton(TopTerms.SHORT.getDescription());
        JButton buttonMediumTerm = new JButton(TopTerms.MEDIUM.getDescription());
        JButton buttonLongTerm = new JButton(TopTerms.LONG.getDescription());
        buttonShortTerm.addActionListener(e -> show(TopTerms.SHORT));
        buttonMediumTerm.addActionListener(e -> show(TopTerms.MEDIUM));
        buttonLongTerm.addActionListener(e -> show(TopTerms.LONG));
        buttonsPanel.setBorder(new EmptyBorder(8, 16, 0, 16));
        buttonsPanel.add(buttonShortTerm);
        buttonsPanel.add(buttonMediumTerm);
        buttonsPanel.add(buttonLongTerm);

        // Table
        String[] columnNames = new String[]{
                "Status", "Album", "Rank", "Song Name", "Artists", "Popularity"
        };

        model = new RankingModel(columnNames, 0);
        table = new JTable(model);
        table.setRowHeight(50);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                String artistsNames = (String) model.getValueAt(table.getSelectedRow(), 4);
                String mainName = artistsNames.split(", ")[0];
                Artist artist = library.findByName(mainName);
                if (artist != null) openArtistWindow(artist);
            }
        });
        customizeTexts();

        // Add Components
        getContentPane().add(BorderLayout.NORTH, menuBar);
        getContentPane().add(BorderLayout.CENTER, buttonsPanel);
        getContentPane().add(BorderLayout.SOUTH, new JScrollPane(table));
        setVisible(true);
    }

    private void customizeTexts(){
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setHorizontalAlignment(JLabel.CENTER);

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(0);
        table.getColumnModel().getColumn(3).setPreferredWidth(250);
        table.getColumnModel().getColumn(4).setPreferredWidth(250);

        for (int i=2; i<model.getColumnCount(); i++){
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }

    private void show(TopTerms term) {
        setTitle("Please wait...");
        this.ranking = apiUtils.getRanking(term);
        this.ranking.setTitle(term.getDescription());

        // auto save
        IOUtils.saveRanking(ranking, false);

        // load previous
        this.loadChanges(term);

        while (model.getRowCount() > 0){
            model.removeRow(0);
        }

        for (Song s : ranking){
            model.addRow(toRow(s));
        }

        setTitle(username + " - " + term.getDescription());
    }

    private Object[] toRow(Song song){
        return new Object[]{IOUtils.getImageIcon(song.getStatus()),
                downloadImage(song.getImageUrl()), song.getRank(),
                song.getName(), song.getArtists(), song.getPopularity()};
    }

    private ImageIcon downloadImage(String url){
        try {
            BufferedImage bi = ImageIO.read(new URL(url));
            Image image = bi.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            return new ImageIcon(image);
        } catch (MalformedURLException e){
            System.err.println("Invalid format: " + url);
            e.printStackTrace();
        } catch (IOException e){
            System.err.println("Error while trying to download from web");
            e.printStackTrace();
        }

        return null;
    }

    private void loadChanges(TopTerms term){
        Ranking previousR = IOUtils.getLastSavedRanking(term);
        if (previousR == null) return;

        for (Song s : ranking){
            Song prevS = previousR.getSong(s.getId());
            if (prevS == null) s.setStatus(Status.NEW);
            else {
                s.setPreviousRank(prevS.getRank());
                s.validateWeb();
            }
        }
    }

    private void openArtistWindow(Artist artist){
        ArtistFrame artistFrame = new ArtistFrame(artist);
        artistFrame.init();
        artistFrame.setVisible(true);
    }
}
