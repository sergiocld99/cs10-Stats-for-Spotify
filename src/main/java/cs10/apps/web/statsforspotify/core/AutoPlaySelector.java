package cs10.apps.web.statsforspotify.core;

import com.wrapper.spotify.model_objects.specification.PlayHistory;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.model_objects.specification.Track;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.web.statsforspotify.app.DevelopException;
import cs10.apps.web.statsforspotify.io.ArtistDirectory;
import cs10.apps.web.statsforspotify.io.Library;
import cs10.apps.web.statsforspotify.io.SongAppearance;
import cs10.apps.web.statsforspotify.io.SongFile;
import cs10.apps.web.statsforspotify.model.CustomList;
import cs10.apps.web.statsforspotify.model.ranking.BigRanking;
import cs10.apps.web.statsforspotify.service.AutoPlayService;
import cs10.apps.web.statsforspotify.utils.ApiUtils;
import cs10.apps.web.statsforspotify.utils.IOUtils;
import cs10.apps.web.statsforspotify.utils.Maintenance;
import cs10.apps.web.statsforspotify.view.OptionPanes;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoPlaySelector {
    private final Library library;
    private final ApiUtils apiUtils;
    private final BigRanking ranking;
    private int itemIndex, minScore, minPopularity;
    private final CustomList<List<PlaylistTrack>> data;
    private final List<Integer> magicNumbers;
    private ScheduledExecutorService service;
    private final AutoPlayService.AutoPlayRunnable runnable;
    private final Set<String> ids = new HashSet<>();
    private final Set<String> recentIds = new HashSet<>();
    private final Set<String> names = new HashSet<>();
    private final Queue<String> pendingIds = new LinkedList<>();
    private static final int MAX_ITERATIONS = 64;

    public AutoPlaySelector(Library library, ApiUtils apiUtils, BigRanking ranking,
                            AutoPlayService.AutoPlayRunnable runnable) {
        this.library = library;
        this.apiUtils = apiUtils;
        this.ranking = ranking;
        this.data = new CustomList<>();
        this.magicNumbers = new LinkedList<>();
        this.runnable = runnable;
        prepare();
        setConstants();
    }

    private void prepare(){
        List<Playlist> list = apiUtils.getDailyMixes();

        for (Playlist p : list){
            List<PlaylistTrack> tracks = Arrays.asList(p.getTracks().getItems());
            Collections.shuffle(tracks);
            data.add(tracks);
        }

        PlayHistory[] ph = apiUtils.getRecentTracks();
        for (PlayHistory p : ph) ids.add(p.getTrack().getId());
        recentIds.addAll(ids);
    }

    private void setConstants(){
        minPopularity = ranking.getAverage();
        minScore = minPopularity / 8;
    }

    private boolean areDailyMixesMissing(){
        if (data.size() == 0) {
            OptionPanes.message("Unable to start AutoPlay: Make sure that your Daily Mixes are at the top of your library");
            return true;
        }

        library.shuffleTrends();
        return false;
    }

    public void run(){
        if (areDailyMixesMissing()) return;
        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> run2(true), 0, 150, TimeUnit.SECONDS);
    }

    public void createPlaylist(){
        if (areDailyMixesMissing()) return;
        new Thread(() -> {
            for (int i=0; i<MAX_ITERATIONS; i++){
                System.out.println("Creating Playlist: " + (i+1) + "/" + MAX_ITERATIONS);
                run2(false);
            }
            ids.removeAll(recentIds);
            apiUtils.createPlaylist("AutoPlay " + ranking.getCode(), ids);
        }, "Creating AutoPlay Playlist").start();
    }

    private void run2(boolean queue){
        if (pendingIds.isEmpty()){
            final Set<String> relationIds = new HashSet<>();
            //runSimplified(queue);

            if (!library.isTrendsEmpty()){
                Song s = library.getTrend();
                relationIds.add(s.getId());
                SongAppearance[] appearances = new SongAppearance[2];
                appearances[0] = s.getSongFile().getFirstAppearance();
                appearances[1] = s.getSongFile().getLastAppearance();
                for (SongAppearance a : appearances)
                    relationIds.addAll(IOUtils.getRelationIds(a.getRankingCode(), a.getChartPosition(), library));
                Maintenance.log(s + " selected from Library Trends");
            } else {
                Song song = ranking.getRandomElement();
                SongAppearance appearance = song.getSongFile().getMediumAppearance();

                if (song.getPopularity() > appearance.getPopularity()){
                    relationIds.add(song.getId());
                    Maintenance.log(song + " is a trending song. Avoiding relation ids");
                } else {
                    relationIds.addAll(IOUtils.getRelationIds(appearance.getRankingCode(), appearance.getChartPosition(), library));
                    if (relationIds.contains(song.getId())) throw new DevelopException("Incorrect Relation Ids");
                }
            }

            for (String s : relationIds) {
                if (!ids.contains(s)) {
                    try {
                        Track t = apiUtils.getTrackByID(s);
                        String artistName = t.getArtists()[0].getName();

                        if (apiUtils.getRankingImprover().isArtistBlocked(artistName)){
                            System.err.println(artistName + " is blocked by ranking improver");
                            ids.addAll(apiUtils.autoQueue(ranking, apiUtils.getRankingImprover().getTargetTrack(), queue));
                            return;
                        }

                        ArtistDirectory a = library.getArtistByName(artistName);
                        SongFile songFile = (a == null ? null : a.getSongById(t.getId()));
                        if (!names.contains(t.getName()) && !isBecomingVeryUnpopular(songFile, t.getPopularity())) queue(t.getName(), s);
                    } catch (Exception e){
                        Maintenance.writeErrorFile(e, true);
                        break;
                    }
                }
            }
        }

        if (pendingIds.isEmpty()) runSimplified(queue);
        else {
            String nextId = pendingIds.remove();
            if (queue) apiUtils.playThis(nextId, false);
        }
    }

    private void queue(String name, String id){
        names.add(name);
        ids.add(id);
        pendingIds.add(id);
    }

    private void runSimplified(boolean queue){
        List<PlaylistTrack> dailyMix = data.getRandomElement();
        Track selectedTrack = (Track) dailyMix.get(itemIndex++).getTrack();
        if (itemIndex == 50) shutdown();

        if (ids.contains(selectedTrack.getId()) || names.contains(selectedTrack.getName())) return;
        boolean condition1 = isArtistSaved(selectedTrack.getArtists()[0].getName());
        boolean condition2 = isGoodPopularity(selectedTrack.getPopularity());
        boolean condition3 = !apiUtils.getRankingImprover().isPopularityBlocked(selectedTrack.getPopularity());
        boolean condition4 = condition1 || condition2;

        if (condition3 && condition4) {
            System.out.println(selectedTrack.getName() + " queued from Daily Mixes");
            names.add(selectedTrack.getName());
            ids.add(selectedTrack.getId());
            if (queue) apiUtils.playThis(selectedTrack.getId(), false);
        } else ids.addAll(apiUtils.autoQueue(ranking, selectedTrack, queue));
    }

    private boolean isArtistSaved(String artistName){
        ArtistDirectory d = library.getArtistByName(artistName);
        if (d != null){
            if (d.getArtistScore() < minScore) return false;
            if (!magicNumbers.contains(d.getSongCount())){
                magicNumbers.add(d.getSongCount() + magicNumbers.size());
                return true;
            }
        }

        return false;
    }

    private boolean isGoodPopularity(int popularity){
        return popularity > minPopularity && popularity < (minPopularity + 200) / 3;
    }

    private void shutdown(){
        service.shutdown();
        runnable.enable();
    }

    private boolean isBecomingVeryUnpopular(SongFile songFile, int popularity){
        if (songFile == null) return false;
        return songFile.getMediumAppearance().getPopularity() > popularity + 8;
    }

}
