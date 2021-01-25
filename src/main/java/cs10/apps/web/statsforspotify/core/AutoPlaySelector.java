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
import cs10.apps.web.statsforspotify.model.BigRanking;
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
    private int dailyMixIndex, itemIndex, minScore, minPopularity, trendsOffset;
    private final List<List<PlaylistTrack>> data;
    private final List<Integer> magicNumbers;
    private ScheduledExecutorService service;
    private final AutoPlayService.AutoPlayRunnable runnable;
    private final Set<String> ids = new HashSet<>();
    private final Queue<String> pendingIds = new LinkedList<>();

    public AutoPlaySelector(Library library, ApiUtils apiUtils, BigRanking ranking,
                            AutoPlayService.AutoPlayRunnable runnable) {
        this.library = library;
        this.apiUtils = apiUtils;
        this.ranking = ranking;
        this.data = new ArrayList<>(6);
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
            Collections.shuffle(tracks);
            Collections.shuffle(tracks);
            data.add(tracks);
        }

        if (library.isTrendsEmpty()) for (Song s : ranking){
            ArtistDirectory ad = s.getSongFile().getArtistReference();
            if (s.getPopularity() > ad.getAveragePopularity() + ad.getRank()) library.addTrend(s);
        }

        PlayHistory[] ph = apiUtils.getRecentTracks();
        for (PlayHistory p : ph) ids.add(p.getTrack().getId());
    }

    private void setConstants(){
        minPopularity = ranking.getAverage();
        minScore = minPopularity / 8;
        trendsOffset = (minScore + 3) / 2;
    }

    private void run2(){
        if (pendingIds.isEmpty()){
            Song random = ranking.getRandomElement();
            SongAppearance medium = random.getSongFile().getMediumAppearance();
            Maintenance.log(random + " was selected for Relation Ids, using " + medium.getRankingCode() + " as code");
            List<String> relationIds = IOUtils.getRelationIds(medium.getRankingCode(), medium.getChartPosition());
            if (relationIds.contains(random.getId())) throw new DevelopException("Incorrect Relation Ids");
            for (String s : relationIds) {
                if (!ids.contains(s)) {
                    pendingIds.add(s);
                    ids.add(s);
                } else {
                    runSimplified();
                    return;
                }
            }
            run2();
        } else apiUtils.playThis(pendingIds.remove(), false);
    }

    public void run(){
        if (data.size() < 6) {
            OptionPanes.message("Unable to start AutoPlay: Make sure that your Daily Mixes are at the top of your library");
            return;
        }

        library.shuffleTrends();
        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(this::run2, 0, 3, TimeUnit.MINUTES);
    }

    private void runSimplified(){
        List<PlaylistTrack> dailyMix = data.get(dailyMixIndex++);
        if (dailyMixIndex == 6) dailyMixIndex = 0;

        Track selectedTrack = (Track) dailyMix.get(itemIndex++).getTrack();
        if (itemIndex == 50) shutdown();

        boolean condition1 = isArtistSaved(selectedTrack.getArtists()[0].getName());
        boolean condition2 = isGoodPopularity(selectedTrack.getPopularity());

        if (condition1 || condition2) {
            System.out.println(selectedTrack.getName() + " selected from Daily Mixes");
            apiUtils.playThis(selectedTrack.getId(), false);
        } else {
            System.out.println("Asking Spotify for recommendations...");
            apiUtils.autoQueue(ranking, selectedTrack);
        }
    }

    private void runOnce(){
        if (!library.isTrendsEmpty() && System.currentTimeMillis() % trendsOffset == 0){
            Song s = library.getTrend();
            System.out.println(s.getName() + " selected by Trends");
            apiUtils.playThis(s.getId(), false);
            minPopularity = s.getPopularity();
            return;
        }

        List<PlaylistTrack> dailyMix = data.get(dailyMixIndex++);
        if (dailyMixIndex == 6) dailyMixIndex = 0;

        Track selectedTrack = (Track) dailyMix.get(itemIndex++).getTrack();
        if (itemIndex == 50) shutdown();

        boolean condition1 = isArtistSaved(selectedTrack.getArtists()[0].getName());
        boolean condition2 = isGoodPopularity(selectedTrack.getPopularity());

        if (condition1 || condition2) {
            System.out.println(selectedTrack.getName() + " selected from Daily Mixes");
            apiUtils.playThis(selectedTrack.getId(), false);
        } else {
            Song random = ranking.getRandomElement();
            ranking.remove(random.getRank());

            if (random.getPopularity() >= random.getSongFile().getMediumAppearance().getPopularity()) {
                System.out.println(random.getName() + " selected from Current Ranking");
                apiUtils.playThis(random.getId(), false);
            } else {
                System.out.println("Asking Spotify for recommendations...");
                apiUtils.autoQueue(ranking, selectedTrack);
            }
        }
    }

    private boolean isArtistSaved(String artistName){
        ArtistDirectory d = library.getArtistByName(artistName);
        if (d != null){
            if (d.getArtistScore() < minScore)
                return false;

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

}
