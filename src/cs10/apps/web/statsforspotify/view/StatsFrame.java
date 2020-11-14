package cs10.apps.web.statsforspotify.view;

import cs10.apps.desktop.statsforspotify.model.Ranking;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.desktop.statsforspotify.utils.IOUtils;
import cs10.apps.desktop.statsforspotify.view.RankingModel;
import cs10.apps.web.statsforspotify.model.TopTerms;
import cs10.apps.web.statsforspotify.utils.ApiUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class StatsFrame extends JFrame {
    private final ApiUtils apiUtils;
    private RankingModel model;
    private Ranking ranking;
    private JTable table;

    public StatsFrame(ApiUtils apiUtils) throws HeadlessException {
        this.apiUtils = apiUtils;
    }

    public void init() throws Exception {
        if (apiUtils != null) {
            setTitle(apiUtils.getUser().getDisplayName() + " - Your Spotify Stats");
        } else return;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);

        // Menu Bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem jmiOpen = new JMenuItem("Open...");
        JMenuItem jmiSave = new JMenuItem("Save");
        JMenuItem jmiSaveAs = new JMenuItem("Save As...");
        jmiOpen.addActionListener(e -> System.out.println("Open pressed"));
        jmiSave.addActionListener(e -> System.out.println("Saved as " + ranking.getCode() + ".txt"));
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
        buttonShortTerm.addActionListener(e -> show(apiUtils.getRanking(TopTerms.SHORT)));
        buttonMediumTerm.addActionListener(e -> show(apiUtils.getRanking(TopTerms.MEDIUM)));
        buttonLongTerm.addActionListener(e -> show(apiUtils.getRanking(TopTerms.LONG)));
        buttonsPanel.setBorder(new EmptyBorder(8, 16, 0, 16));
        buttonsPanel.add(buttonShortTerm);
        buttonsPanel.add(buttonMediumTerm);
        buttonsPanel.add(buttonLongTerm);

        // Table
        String[] columnNames = new String[]{
                "Album", "Rank", "Song Name", "Artists", "Popularity"
        };

        model = new RankingModel(columnNames, 0);
        table = new JTable(model);
        table.setRowHeight(50);
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
        table.getColumnModel().getColumn(1).setPreferredWidth(0);
        table.getColumnModel().getColumn(2).setPreferredWidth(250);
        table.getColumnModel().getColumn(3).setPreferredWidth(250);

        for (int i=1; i<model.getColumnCount(); i++){
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }

    private void show(Ranking ranking){
        this.ranking = ranking;

        while (model.getRowCount() > 0){
            model.removeRow(0);
        }

        for (Song s : ranking){
            model.addRow(toRow(s));
        }
    }

    private Object[] toRow(Song song){
        return new Object[]{downloadImage(song.getImageUrl()), song.getRank(),
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

    public static void main(String[] args) throws Exception {
        StatsFrame statsFrame = new StatsFrame(null);
        statsFrame.init();
    }
}
