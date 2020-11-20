package cs10.apps.web.statsforspotify.view;

import com.wrapper.spotify.model_objects.specification.Track;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.desktop.statsforspotify.model.Status;
import cs10.apps.desktop.statsforspotify.utils.OldIOUtils;
import cs10.apps.desktop.statsforspotify.view.RankingModel;
import cs10.apps.web.statsforspotify.app.PersonalChartApp;
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
import java.util.Random;

public class StatsFrame extends JFrame {
    private final ApiUtils apiUtils;
    private RankingModel model;
    private JTable table;
    private PlaybackService playbackService;

    // version 3
    private BigRanking bigRanking;
    private final Random random;

    // version 5
    private CustomPlayer player;

    public StatsFrame(ApiUtils apiUtils) throws HeadlessException {
        this.apiUtils = apiUtils;
        this.random = new Random();
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
        long[] savedCodes = IOUtils.readLastRankingCode();
        if (bigRanking.getCode() != savedCodes[1]){
            IOUtils.saveLastRankingCode(savedCodes[1], bigRanking.getCode());
            IOUtils.saveRanking(bigRanking, true);
            player.setString("Updating Library Files...");
            IOUtils.makeLibraryFiles(bigRanking, player.getProgressBar());
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
                song.getRank(), song.getName(), song.getArtists(), song.getPopularity(), song.getInfoStatus()};
    }

    private void openArtistWindow(String artist, float[] scores){
        ArtistFrame artistFrame = new ArtistFrame(artist, scores);
        artistFrame.init();
        artistFrame.setVisible(true);
    }
}
