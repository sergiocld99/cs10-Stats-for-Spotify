package cs10.apps.web.statsforspotify.view.table;

import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.desktop.statsforspotify.model.Status;
import cs10.apps.desktop.statsforspotify.utils.OldIOUtils;
import cs10.apps.desktop.statsforspotify.view.CustomTableModel;
import cs10.apps.web.statsforspotify.app.AppFrame;
import cs10.apps.web.statsforspotify.app.AppOptions;
import cs10.apps.web.statsforspotify.app.PersonalChartApp;
import cs10.apps.web.statsforspotify.core.GenresTracker;
import cs10.apps.web.statsforspotify.core.Init;
import cs10.apps.web.statsforspotify.io.ArtistDirectory;
import cs10.apps.web.statsforspotify.io.Library;
import cs10.apps.web.statsforspotify.model.BigRanking;
import cs10.apps.web.statsforspotify.model.SimpleRanking;
import cs10.apps.web.statsforspotify.service.AutoPlayService;
import cs10.apps.web.statsforspotify.service.PlaybackService;
import cs10.apps.web.statsforspotify.utils.ApiUtils;
import cs10.apps.web.statsforspotify.utils.CommonUtils;
import cs10.apps.web.statsforspotify.utils.IOUtils;
import cs10.apps.web.statsforspotify.utils.Maintenance;
import cs10.apps.web.statsforspotify.view.CustomPlayer;
import cs10.apps.web.statsforspotify.view.CustomTableCellRenderer;
import cs10.apps.web.statsforspotify.view.OptionPanes;
import cs10.apps.web.statsforspotify.view.chart.SongChartHistoryView;
import cs10.apps.web.statsforspotify.view.histogram.ArtistFrame;
import cs10.apps.web.statsforspotify.view.histogram.GenreFrame;
import cs10.apps.web.statsforspotify.view.histogram.LocalTop10Frame;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

public class StatsFrame extends AppFrame {
    private final AppOptions appOptions;
    private final ApiUtils apiUtils;
    private BigRanking bigRanking;
    private CustomPlayer player;
    private CustomTableModel model;
    private JTable table;
    private PlaybackService playbackService;
    private Library library;
    private Init init;

    private static final int ALBUM_COVERS_COLUMN = 1;

    public StatsFrame(ApiUtils apiUtils) throws HeadlessException {
        this.appOptions = IOUtils.loadAppOptions();
        this.apiUtils = apiUtils;
    }

    public void init() {
        init = new Init(apiUtils);
        player = new CustomPlayer(70, appOptions);
        Thread initThread = new Thread(() -> init.execute(player), "Init Thread");
        initThread.start();

        setTitle(PersonalChartApp.APP_AUTHOR + " - " +
                PersonalChartApp.APP_NAME + " v" + PersonalChartApp.APP_VERSION);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 480);

        // Menu Bar
        JMenuBar menuBar = getCustomMenuBar();

        // Player Panel
        JPanel playerPanel = new JPanel();
        JButton autoPlayButton = new JButton(AutoPlayService.NAME + " (Premium)");
        JLabel textAboveButton = new JLabel("Loading...", JLabel.CENTER);

        //playerPanel.setBorder(new EmptyBorder(0, 16, 0, 16));
        playerPanel.add(player);
        JPanel autoPlayPanel = new JPanel(new GridLayout(2,1));
        textAboveButton.setFont(new Font("Arial",Font.BOLD,10));
        autoPlayPanel.add(textAboveButton);
        autoPlayPanel.add(autoPlayButton);
        playerPanel.add(autoPlayPanel);

        // Table
        String[] columnNames = new String[]{"Status", "Rank", "Change",
                "Song Name", "Artists", "Popularity"};

        model = new CustomTableModel(columnNames, 0);
        table = new JTable(model) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                CustomTableCellRenderer renderer = new CustomTableCellRenderer();
                renderer.setHorizontalAlignment(JLabel.CENTER);

                if (column == model.getColumnCount() - 2 || column == model.getColumnCount() - 3)
                    getColumnModel().getColumn(column).setPreferredWidth(250);
                return renderer;
            }
        };

        table.setRowHeight(50);
        table.getTableHeader().setReorderingAllowed(false);
        //customizeTexts();

        // Add Components
        getContentPane().add(BorderLayout.NORTH, menuBar);
        getContentPane().add(BorderLayout.CENTER, playerPanel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(1000, 310));
        getContentPane().add(BorderLayout.SOUTH, scrollPane);

        // Set Playback
        playbackService = new PlaybackService(apiUtils, table, this, player);
        playbackService.allowAutoUpdate();

        try {
            initThread.join();
        } catch (InterruptedException e){
            Maintenance.writeErrorFile(e, false);
            OptionPanes.message("Unable to load data, please check the maintenance log");
            System.exit(4);
        }

        bigRanking = init.getProcessedRanking();
        player.setLastRankingCode(bigRanking.getCode());
        library = init.getLibrary();
        textAboveButton.setText(library.getSongCount() + " songs in your charts");
        //apiUtils.addToMissedTracks(apiUtils.findDailyMix(
        //        library.getSongCount() % 10, (int) bigRanking.getCode() / 100));

        // When ranking is totally loaded
        buildTable();
        player.setAverage((int) (bigRanking.getCode() / 100));
        player.enableLibrary();

        // Hard tasks
        if (appOptions.isAlbumCovers())
            new Thread(this::addAlbumCoversColumn, "Load Album Covers").start();
        startPlayback();

        // Important for maintain order
        library.sort();

        // Set Listeners
        if (bigRanking.getRepeatedQuantity() < 5)
            autoPlayButton.setEnabled(false);
        else {
            AutoPlayService autoPlayService = new AutoPlayService(bigRanking, apiUtils, autoPlayButton);
            autoPlayButton.addActionListener(e -> {
                if (apiUtils.playThis(bigRanking.getRandomElement().getId(), true)){
                    autoPlayService.execute();
                    playbackService.setCanSkip(false);
                    playbackService.run();
                } else {
                    OptionPanes.message("No active devices found or unable to modify current playback");
                }
            });
        }

        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2){
                    int selected = table.getSelectedRow();

                    int result = JOptionPane.showOptionDialog(null,
                            "Select an option", PersonalChartApp.APP_NAME,
                            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                            null, new String[]{"Play this now", "View Song Chart",
                                    "View Artist Histogram"}, null);

                    table.setRowSelectionInterval(selected,selected);

                    switch (result) {
                        case 0:
                            if (apiUtils.playThis(bigRanking.get(table.getSelectedRow()).getId(), true)) {
                                playbackService.setCanSkip(false);
                                playbackService.run();
                                OptionPanes.message("Current Playback updated");
                            } else OptionPanes.message("Unable to play this song");
                            break;
                        case 1:
                            openSongChartHistory();
                            break;
                        case 2:
                            openArtistWindow();
                            break;
                    }
                }
            }
        });
    }

    private JMenuBar getCustomMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem jmiOpen = new JMenuItem("Open...");
        JMenuItem jmiSave = new JMenuItem("Save");
        JMenuItem jmiSaveAs = new JMenuItem("Save As...");
        JMenu viewMenu = new JMenu("View");
        JMenuItem jmiLocalTop10 = new JMenuItem("Local Top 10 Artists");
        JMenuItem jmiLocalTop100 = new JMenuItem("Local Top 100 Artists");
        JMenuItem jmiTopGenres = new JMenuItem("Current Top Genres");
        JMenuItem jmiCurrentCollab = new JMenuItem("Current Collab Scores");
        JMenuItem jmiDailyMixes = new JMenuItem("Current Daily Mixes Stats");
        JMenuItem jmiFanaticism = new JMenuItem("Current Fanaticism");
        JMenu optionsMenu = new JMenu("Options");
        JMenuItem jmiAlbumCovers = new JMenuItem("Toggle Album Covers");
        JMenuItem jmiLastFmUser = new JMenuItem("Last FM Username");
        JMenu helpMenu = new JMenu("Help");

        jmiOpen.addActionListener(e -> openRankingsWindow());
        jmiSave.addActionListener(e -> System.out.println("Save pressed"));
        jmiSaveAs.addActionListener(e -> System.out.println("Save As pressed"));
        jmiAlbumCovers.addActionListener(e -> changeAlbumCoversOption());
        jmiLocalTop10.addActionListener(e -> openLocalTop10());
        jmiLocalTop100.addActionListener(e -> openLocalTop100());
        jmiTopGenres.addActionListener(e -> openCurrentTopGenres());
        jmiCurrentCollab.addActionListener(e -> openCurrentCollabScores());
        jmiDailyMixes.addActionListener(e -> openCurrentDailyMixesStats());
        jmiFanaticism.addActionListener(e -> openFanaticismWindow());
        jmiLastFmUser.addActionListener(e -> {
            boolean restart = OptionPanes.inputUsername(appOptions);
            if (restart) playbackService.restart();
        });
        helpMenu.addActionListener(e -> System.out.println("Help pressed"));

        fileMenu.add(jmiOpen);
        fileMenu.add(jmiSave);
        fileMenu.add(jmiSaveAs);
        viewMenu.add(jmiLocalTop10);
        viewMenu.add(jmiLocalTop100);
        viewMenu.add(jmiTopGenres);
        viewMenu.add(jmiCurrentCollab);
        viewMenu.add(jmiDailyMixes);
        viewMenu.add(jmiFanaticism);
        optionsMenu.add(jmiAlbumCovers);
        optionsMenu.add(jmiLastFmUser);
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(optionsMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private void experimentalModifyArtistScore1() {
        float average = bigRanking.getCode() / 100f;
        int offset = (int) (average / 10);
        for (int i=0; i<average; i+=offset){
            List<Song> sublist = bigRanking.subList(i,i+offset);
            int maxP = 0, maxIndex = 0;
            for (Song s : sublist){
                if (s.getPopularity() > maxP){
                    maxP = s.getPopularity();
                    maxIndex = s.getRank()-1;
                }
            }
            Song selected = bigRanking.get(maxIndex);
            for (String artist : selected.getArtists().split(", ")){
                ArtistDirectory a = library.getArtistByName(artist);
                a.multiplyScore(Math.sqrt(maxP / average));
            }
        }
    }

    private void experimentalModifyArtistScore2(){
        float average = bigRanking.getCode() / 100f;
        int max = (int) average;

        for (Song s : bigRanking){
            if (s.getPopularity() > max){
                max = s.getPopularity();
                double delta = s.getPopularity() - average;
                for (String artist : s.getArtists().split(", ")){
                    ArtistDirectory a = library.getArtistByName(artist);
                    a.incrementScore(delta);
                }
            }
        }
    }

    private void startPlayback(){
        player.setString("");
        playbackService.setRanking(bigRanking);
        playbackService.run();
    }

    private void buildTable(){
        player.setString("Loading ranking...");
        table.getTableHeader().setDefaultRenderer(new CustomHeaderRenderer(table));
        float average = bigRanking.getCode() / 100f;

        int min = 100;
        for (int j=0; j<Math.min(5, bigRanking.size()); j++)
            min = Math.min(min, bigRanking.get(j).getSongFile().getAppearancesCount());

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

            if (s.isRepeated()) {
                //System.out.println(s + " is repeated");
                double multiplier = Math.sqrt(s.getPopularity() / average);
                for (String artist : s.getArtists().split(", ")){
                    ArtistDirectory a = library.getArtistByName(artist);
                    a.multiplyScore(multiplier);
                }
            }

            if (s.getSongFile().getAppearancesCount() <= min){
                int row = s.getRank()-1;
                if (row % 2 == 0) model.setRowColor(row, s.getPopularityStatus().getTablePairColor());
                else model.setRowColor(row, s.getPopularityStatus().getTableUnpairColor());
            }

            // BETA 1.04.2
            int comp = s.getSongFile().getMediumAppearance().getPopularity();
            if (s.getPopularity() > comp + 2) library.addTrend(s);

            model.addRow(toRow(s));
        }
    }

    private Object[] toRow(Song song){
        int comp = song.getSongFile().getPreviousAppearance().getPopularity();
        int diff = song.getPopularity() - comp;
        String popStr;

        if (diff != 0 && Math.abs(diff) < 10){
            if (diff > 0) popStr = song.getPopularity() + " (+" + diff + ")";
            else popStr = song.getPopularity() + " (" + diff + ")";
        } else popStr = String.valueOf(song.getPopularity());

        return new Object[]{OldIOUtils.getImageIcon(song.getStatus()),
                "#" + song.getRank(), song.getInfoStatus(),
                song.getName().split(" \\(")[0],
                song.getArtists(), popStr};
    }

    private void changeAlbumCoversOption() {
        if (appOptions.isAlbumCovers()){
            appOptions.setAlbumCovers(false);
            this.removeAlbumCoversColumn();
        } else {
            appOptions.setAlbumCovers(true);
            this.addAlbumCoversColumn();
        }
    }

    private void addAlbumCoversColumn(){
        TableColumn column = new TableColumn(model.getColumnCount());
        column.setPreferredWidth(50);
        column.setHeaderValue("Cover");

        ImageIcon[] coversImages = new ImageIcon[bigRanking.size()];

        for (int i=0; i<bigRanking.size(); i++){
            coversImages[i] = CommonUtils.downloadImage(bigRanking.get(i).getImageUrl(),50);
        }

        SwingUtilities.invokeLater(() -> {
            table.addColumn(column);
            model.addColumn(column.getHeaderValue().toString(), coversImages);
            table.moveColumn(table.getColumnCount()-1, 1);
        });
    }

    private void removeAlbumCoversColumn(){
        table.removeColumn(table.getColumnModel().getColumn(ALBUM_COVERS_COLUMN));
        model.setColumnCount(model.getColumnCount()-1);
    }

    private void openRankingsWindow(){
        new Thread(() -> {
            setTitle("Please wait...");
            SimpleRanking[] fromDisk = IOUtils.getAvailableRankings();
            if (fromDisk.length == 0) OptionPanes.message("There are no rankings yet");
            else {
                Arrays.sort(fromDisk);
                SelectFrame selectFrame = new SelectFrame(fromDisk, bigRanking);
                selectFrame.init();
            }
            setTitle("Done");
        }, "Open Rankings Window").start();
    }

    private void openCurrentTopGenres(){
        new Thread(() -> {
            setTitle("Please wait...");
            GenresTracker genresTracker = new GenresTracker();
            genresTracker.build(init.getResultTracks(), apiUtils);
            new GenreFrame(genresTracker.getGenres()).init();
            setTitle("Done");
        }, "Open Top Genres").start();
    }

    private void openLocalTop10(){
        ArtistDirectory[] artists = library.getTop(10).toArray(new ArtistDirectory[0]);
        LocalTop10Frame localTop10Frame = new LocalTop10Frame(artists);
        localTop10Frame.init();
    }

    private void openLocalTop100(){
        ArtistDirectory[] artists = library.getTop(100).toArray(new ArtistDirectory[0]);
        new LocalTop100Frame(artists).init();
    }

    private void openCurrentCollabScores(){
        new CollabScoresFrame(bigRanking).init();
    }

    private void openCurrentDailyMixesStats(){
        new Thread(() -> {
            setTitle("Please wait...");
            apiUtils.analyzeDailyMixes();
            setTitle("Done");
        }, "Open Current Daily Mixes Stats").start();
    }

    private void openFanaticismWindow(){
        new Thread(() -> {
            setTitle("Please wait...");
            new FanaticismFrame(bigRanking, appOptions).init();
            setTitle("Done");
        }, "Open Fanaticism Window").start();
    }

    private void openArtistWindow(){
        new Thread(() -> {
            setTitle("Please wait...");
            String artistsNames = bigRanking.get(table.getSelectedRow()).getArtists();
            //String artistsNames = (String) model.getValueAt(table.getSelectedRow(), 3);
            String[] artists = artistsNames.split(", ");
            String mainName = artists[0];
            //float[] scores = IOUtils.getDetailedArtistScores(mainName);
            float[] scores = library.getArtistByName(mainName).getPopularitySumByRank();
            ArtistFrame artistFrame = new ArtistFrame(mainName, scores);
            artistFrame.init();
            setTitle("Done");
        }, "Open Artist Window").start();
    }

    private void openSongChartHistory(){
        SwingUtilities.invokeLater(() -> {
            Song selected = bigRanking.get(table.getSelectedRow());
            SongChartHistoryView view = new SongChartHistoryView(
                    library, selected.getMainArtist(), selected.getId());

            if (view.init()){
                view.setSize(1000,500);
                view.setVisible(true);
            }
        });
    }
}
