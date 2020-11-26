package cs10.apps.web.statsforspotify.view;

import com.wrapper.spotify.model_objects.specification.Track;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.desktop.statsforspotify.model.Status;
import cs10.apps.desktop.statsforspotify.utils.OldIOUtils;
import cs10.apps.desktop.statsforspotify.view.RankingModel;
import cs10.apps.web.statsforspotify.app.PersonalChartApp;
import cs10.apps.web.statsforspotify.model.Artist;
import cs10.apps.web.statsforspotify.model.BigRanking;
import cs10.apps.web.statsforspotify.model.TopTerms;
import cs10.apps.web.statsforspotify.service.PlaybackService;
import cs10.apps.web.statsforspotify.utils.ApiUtils;
import cs10.apps.web.statsforspotify.utils.CommonUtils;
import cs10.apps.web.statsforspotify.utils.IOUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

public class StatsFrame extends JFrame {
    private final ApiUtils apiUtils;
    private RankingModel model;
    private JTable table;
    private PlaybackService playbackService;

    // version 3
    private BigRanking bigRanking;

    // version 5
    private CustomPlayer player;

    public StatsFrame(ApiUtils apiUtils) throws HeadlessException {
        this.apiUtils = apiUtils;
    }

    public void init() {
        setTitle(PersonalChartApp.APP_AUTHOR + " - " +
                PersonalChartApp.APP_NAME + " v" + PersonalChartApp.APP_VERSION);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 600);

        // Menu Bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem jmiOpen = new JMenuItem("Open...");
        JMenuItem jmiSave = new JMenuItem("Save");
        JMenuItem jmiSaveAs = new JMenuItem("Save As...");
        JMenu viewMenu = new JMenu("View");
        JMenuItem jmiLocalTop10 = new JMenuItem("Local Top 10 Artists");
        jmiOpen.addActionListener(e -> System.out.println("Open pressed"));
        jmiSave.addActionListener(e -> System.out.println("Save pressed"));
        jmiSaveAs.addActionListener(e -> System.out.println("Save As pressed"));
        jmiLocalTop10.addActionListener(e -> openLocalTop10());
        fileMenu.add(jmiOpen);
        fileMenu.add(jmiSave);
        fileMenu.add(jmiSaveAs);
        viewMenu.add(jmiLocalTop10);
        JMenu helpMenu = new JMenu("Help");
        helpMenu.addActionListener(e -> System.out.println("Help pressed"));
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);

        JPanel playerPanel = new JPanel();
        player = new CustomPlayer(70);
        playerPanel.add(player);

        // Ranking Buttons
        JButton buttonNowPlaying = new JButton("Show what I'm listening to");
        playerPanel.setBorder(new EmptyBorder(0, 16, 0, 16));
        playerPanel.add(buttonNowPlaying);

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
        getContentPane().add(BorderLayout.CENTER, playerPanel);
        getContentPane().add(BorderLayout.SOUTH, new JScrollPane(table));

        // Show all
        playbackService = new PlaybackService(apiUtils, table, this, player);
        setResizable(false);
        setVisible(true);
        //show(TopTerms.SHORT);

        // Version 4
        init4();

        // Update Custom Thumbnail Properties
        player.setAverage((int) (bigRanking.getCode() / 100));

        // Set Listeners (buttons will be deleted on Version 3)
        buttonNowPlaying.addActionListener(e -> {
            playbackService.run();
            OptionPanes.showPlaybackUpdated();
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                openArtistWindow();
            }
        });

        // Start Playback Service
        player.setString("");
        playbackService.setRanking(bigRanking);
        playbackService.run();
    }

    private void init4(){
        // Step 1: get actual top tracks from Spotify
        player.setString("Connecting to Spotify...");
        Track[] tracks1 = apiUtils.getUntilMostPopular(TopTerms.SHORT.getKey(), 50);
        player.setProgress(50);
        Track[] tracks2 = apiUtils.getTopTracks(TopTerms.MEDIUM.getKey());
        player.setProgress(100);

        // Step 2: build actual ranking
        bigRanking = new BigRanking(CommonUtils.combineWithoutRepeats(tracks1, tracks2, 100));

        // Step 3: read last code
        boolean showSummary = false;
        long[] savedCodes = IOUtils.readLastRankingCode();
        if (bigRanking.getCode() != savedCodes[1]){
            IOUtils.saveLastRankingCode(savedCodes[1], bigRanking.getCode());
            IOUtils.saveRanking(bigRanking, true);
            player.setString("Updating Library Files...");
            IOUtils.makeLibraryFiles(bigRanking, player.getProgressBar());
            showSummary = true;
        }

        // Step 4: load compare ranking
        BigRanking rankingToCompare = IOUtils.loadPreviousRanking();
        bigRanking.updateAllStatus(rankingToCompare);

        // Step 5: build and show UI
        buildTable();

        // Step 6: show songs that left the chart
        //if (showSummary)
            CommonUtils.summary(bigRanking, rankingToCompare, apiUtils);
    }

    private void buildTable(){
        player.setString("Loading ranking...");
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

            player.setProgress((++i) * 100 / bigRanking.size());
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

    private Object[] toRow(Song song){
        return new Object[]{OldIOUtils.getImageIcon(song.getStatus()),
                song.getRank(), song.getName(), song.getArtists(),
                song.getPopularity(), song.getInfoStatus()};
    }

    private void openLocalTop10(){
        new Thread(() -> {
            Artist[] artists = IOUtils.getAllArtistsScore();
            if (artists == null) return;

            Arrays.sort(artists);
            Artist[] top10 = new Artist[10];
            System.arraycopy(artists, 0, top10, 0, 10);
            LocalTop10Frame localTop10Frame = new LocalTop10Frame(top10);
            localTop10Frame.init();
            localTop10Frame.setVisible(true);
        }).start();
    }

    private void openArtistWindow(){
        new Thread(() -> {
            String artistsNames = (String) model.getValueAt(table.getSelectedRow(), 3);
            String[] artists = artistsNames.split(", ");
            String mainName = artists[0];
            ArtistFrame artistFrame = new ArtistFrame(mainName, IOUtils.getScores(mainName));
            artistFrame.init();
            artistFrame.setVisible(true);
        }).start();
    }
}
