package cs10.apps.web.statsforspotify.view;

import com.wrapper.spotify.model_objects.specification.Track;
import cs10.apps.desktop.statsforspotify.model.Ranking;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.desktop.statsforspotify.model.Status;
import cs10.apps.desktop.statsforspotify.utils.OldIOUtils;
import cs10.apps.desktop.statsforspotify.view.RankingModel;
import cs10.apps.web.statsforspotify.model.BigRanking;
import cs10.apps.web.statsforspotify.model.TopTerms;
import cs10.apps.web.statsforspotify.service.PlaybackService;
import cs10.apps.web.statsforspotify.utils.ApiUtils;
import cs10.apps.web.statsforspotify.utils.CommonUtils;
import cs10.apps.web.statsforspotify.utils.IOUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class StatsFrame extends JFrame {
    private final ApiUtils apiUtils;
    private RankingModel model;
    private Ranking ranking;
    private JTable table;
    private String username;
    private PlaybackService playbackService;

    // version 3
    private JProgressBar progressBar;
    private BigRanking bigRanking;
    private Random random;

    public StatsFrame(ApiUtils apiUtils) throws HeadlessException {
        this.apiUtils = apiUtils;
        this.random = new Random();
    }

    public void init() throws Exception {
        username = apiUtils.getUser().getDisplayName();
        setTitle(username + " - Your Spotify Stats");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 600);

        // Menu Bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem jmiOpen = new JMenuItem("Open...");
        JMenuItem jmiSave = new JMenuItem("Save");
        JMenuItem jmiSaveAs = new JMenuItem("Save As...");
        jmiOpen.addActionListener(e -> System.out.println("Open pressed"));
        jmiSave.addActionListener(e -> System.out.println("Save pressed"));
        jmiSaveAs.addActionListener(e -> System.out.println("Save As pressed"));
        fileMenu.add(jmiOpen);
        fileMenu.add(jmiSave);
        fileMenu.add(jmiSaveAs);
        JMenu helpMenu = new JMenu("Help");
        helpMenu.addActionListener(e -> System.out.println("Help pressed"));
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        JPanel buttonsPanel = new JPanel();

        // Custom Thumbnail
        CustomThumbnail thumbnail = new CustomThumbnail(80);
        buttonsPanel.add(thumbnail);

        // Progress Bar
        progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(0);
        progressBar.setPreferredSize(new Dimension(650,30));
        progressBar.setBorder(new EmptyBorder(0,30,0,50));
        progressBar.setStringPainted(true);
        progressBar.setForeground(Color.GREEN);
        progressBar.setUI(new BasicProgressBarUI() {
            protected Color getSelectionBackground() { return Color.black; }
            protected Color getSelectionForeground() { return Color.black; }
        });

        buttonsPanel.add(progressBar);

        // Ranking Buttons
        JButton buttonShortTerm = new JButton(TopTerms.SHORT.getDescription());
        JButton buttonMediumTerm = new JButton(TopTerms.MEDIUM.getDescription());
        JButton buttonLongTerm = new JButton(TopTerms.LONG.getDescription());
        JButton buttonNowPlaying = new JButton("Show what I'm listening to");
        buttonsPanel.setBorder(new EmptyBorder(0, 16, 0, 16));
        //buttonsPanel.add(buttonShortTerm);
        //buttonsPanel.add(buttonMediumTerm);
        //buttonsPanel.add(buttonLongTerm);
        buttonsPanel.add(buttonNowPlaying);

        // Table
        String[] columnNames = new String[]{
                "Status", "Rank", "Song Name", "Artists", "Popularity", "Change"
        };

        model = new RankingModel(columnNames, 0);
        table = new JTable(model);
        table.setRowHeight(50);
        customizeTexts();

        // Add Components
        getContentPane().add(BorderLayout.NORTH, menuBar);
        getContentPane().add(BorderLayout.CENTER, buttonsPanel);
        getContentPane().add(BorderLayout.SOUTH, new JScrollPane(table));

        // Show all
        playbackService = new PlaybackService(apiUtils, table, this, progressBar, thumbnail);
        setResizable(false);
        setVisible(true);
        //show(TopTerms.SHORT);

        // Version 4
        init4();

        // Update Custom Thumbnail Properties
        thumbnail.setAverage((int) (bigRanking.getCode() / 100));

        // Set Listeners (buttons will be deleted on Version 3)
        buttonShortTerm.addActionListener(e -> show(TopTerms.SHORT));
        buttonMediumTerm.addActionListener(e -> show(TopTerms.MEDIUM));
        buttonLongTerm.addActionListener(e -> show(TopTerms.LONG));
        buttonNowPlaying.addActionListener(e -> {
            //playbackService.setRanking(ranking);
            playbackService.run();
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                String artistsNames = (String) model.getValueAt(table.getSelectedRow(), 3);
                String[] artists = artistsNames.split(", ");
                String mainName = artists[random.nextInt(artists.length)];
                openArtistWindow(mainName, IOUtils.getScores(mainName));
                //String mainName = artistsNames.split(", ")[0];
                //Artist artist = library.findByName(mainName);
                //if (artist != null) openArtistWindow(artist);

            }
        });

        // Start Playback Service
        progressBar.setString("");
        playbackService.setRanking(bigRanking);
        playbackService.run();
    }

    private void init3(){
        bigRanking = new BigRanking();
        bigRanking.addRankingToCompare(IOUtils.loadPreviousRanking());

        progressBar.setString("Connecting to Spotify...");
        bigRanking.add(apiUtils.getPaging(TopTerms.SHORT));
        progressBar.setValue(40);
        bigRanking.add(apiUtils.getPaging(TopTerms.MEDIUM));
        progressBar.setValue(80);
        bigRanking.add(apiUtils.getPaging(TopTerms.LONG));
        progressBar.setValue(100);

        // save file
        long prevR = IOUtils.readLastRankingCode()[1];
        if (prevR == 0 || prevR != bigRanking.getCode()){
            for (String id : bigRanking.getLefts()) apiUtils.printLeftTrackInfo(id);
            IOUtils.saveLastRankingCode(prevR, bigRanking.getCode());
            IOUtils.makeLibraryFiles(bigRanking, progressBar);
            IOUtils.saveRanking(bigRanking, true);
        } else {
            System.out.println("Nothing to save");
        }

        // show on table
        buildTable();
    }

    private void init4(){
        // Step 1: get actual top tracks from Spotify
        progressBar.setString("Connecting to Spotify...");
        Track[] tracks1 = apiUtils.getUntilMostPopular(TopTerms.SHORT.getKey(), 50);
        progressBar.setValue(50);
        Track[] tracks2 = apiUtils.getTopTracks(TopTerms.MEDIUM.getKey());
        progressBar.setValue(100);

        // Step 2: build actual ranking
        bigRanking = new BigRanking(CommonUtils.combineWithoutRepeats(tracks1, tracks2, 100));

        // Step 3: read last code
        long[] savedCodes = IOUtils.readLastRankingCode();
        if (bigRanking.getCode() != savedCodes[1]){
            IOUtils.saveLastRankingCode(savedCodes[1], bigRanking.getCode());
            IOUtils.saveRanking(bigRanking, true);
            progressBar.setString("Creating Library Files...");
            IOUtils.makeLibraryFiles(bigRanking, progressBar);
        }

        // Step 4: load compare ranking
        BigRanking rankingToCompare = IOUtils.loadPreviousRanking();
        bigRanking.updateAllStatus(rankingToCompare);

        // Step 5: build and show UI
        buildTable();

        // Step 6: show songs that left the chart
        for (Song s : rankingToCompare.getNonMarked()){
            apiUtils.printLeftTrackInfo(s.getId());
        }
    }

    private void buildTable(){
        progressBar.setString("Loading ranking...");
        int i = 0;

        for (Song s : bigRanking){
            if (s.getStatus() == Status.NEW){
                int times = IOUtils.getTimesOnRanking(s.getArtists(), s.getId());
                if (times <= 1) s.setInfoStatus("NEW");
                else s.setInfoStatus("RE-ENTRY");
            } else {
                if (s.getChange() == 0) s.setInfoStatus("");
                else if (s.getChange() > 0) s.setInfoStatus("+"+s.getChange());
                else s.setInfoStatus(String.valueOf(s.getChange()));
            }

            progressBar.setValue((++i) * 100 / bigRanking.size());
            model.addRow(toRow(s));
        }
    }

    private void customizeTexts(){
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setHorizontalAlignment(JLabel.CENTER);

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        //table.getColumnModel().getColumn(1).setPreferredWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(250);
        table.getColumnModel().getColumn(3).setPreferredWidth(250);

        for (int i=1; i<model.getColumnCount(); i++){
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }

    private void show(TopTerms term) {
        if (ranking != null && ranking.getTitle().equals(term.getDescription())){
            System.out.println("Skipping a new research");
            return;
        }

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
        return new Object[]{OldIOUtils.getImageIcon(song.getStatus()),
                song.getRank(), song.getName(), song.getArtists(), song.getPopularity(), song.getInfoStatus()};
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

    private void openArtistWindow(String artist, float[] scores){
        ArtistFrame artistFrame = new ArtistFrame(artist, scores);
        artistFrame.init();
        artistFrame.setVisible(true);
    }
}
