package cs10.apps.web.statsforspotify.core;

import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.model_objects.specification.Track;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.web.statsforspotify.io.ArtistDirectory;
import cs10.apps.web.statsforspotify.io.Library;
import cs10.apps.web.statsforspotify.model.BigRanking;
import cs10.apps.web.statsforspotify.service.AutoPlayService;
import cs10.apps.web.statsforspotify.utils.ApiUtils;
import cs10.apps.web.statsforspotify.view.OptionPanes;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoPlaySelector {
    private final Library library;
    private final ApiUtils apiUtils;
    private final BigRanking ranking;
    private int dailyMixIndex, itemIndex, minScore, minPopularity;
    private final List<List<PlaylistTrack>> data;
    private final List<Integer> magicNumbers;
    private ScheduledExecutorService service;
    private final AutoPlayService.AutoPlayRunnable runnable;
    //private final boolean switcher;

    public AutoPlaySelector(Library library, ApiUtils apiUtils, BigRanking ranking,
                            AutoPlayService.AutoPlayRunnable runnable) {
        this.library = library;
        this.apiUtils = apiUtils;
        this.ranking = ranking;
        this.data = new ArrayList<>(6);
        this.magicNumbers = new LinkedList<>();
        this.runnable = runnable;

        /*int hour = Calendar.getInstance().get(Calendar.HOUR);
        this.switcher = hour % 2 == 0;
        this.itemIndex = hour;*/

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
    }

    private void setConstants(){
        minPopularity = (int) ranking.getCode() / 100;
        minScore = minPopularity / 10;
    }

    public void run(){
        if (data.size() < 6) {
            OptionPanes.message("Unable to start AutoPlay: " +
                    "Make sure that your Daily Mixes are at the top of your library");
            return;
        }

        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(this::runOnce, 0, 3, TimeUnit.MINUTES);
    }

    private void runOnce(){
        if (!library.isTrendsEmpty()){
            apiUtils.playThis(library.getTrend().getId(), false);
            return;
        }

        List<PlaylistTrack> dailyMix = data.get(dailyMixIndex++);
        if (dailyMixIndex == 6) dailyMixIndex = 0;

        Track selectedTrack = (Track) dailyMix.get(itemIndex++).getTrack();
        if (itemIndex == 50) shutdown();

        boolean condition1 = isArtistSaved(selectedTrack.getArtists()[0].getName());
        boolean condition2 = isGoodPopularity(selectedTrack.getPopularity());

        if (condition1 || condition2)
            apiUtils.playThis(selectedTrack.getId(), false);
        else {
            System.out.println(selectedTrack.getName() + " was skipped for AutoPlay");
            Song random = ranking.getRandomElement();

            if (random.getSongFile().getPeak().getChartPosition() < minScore * 2)
                apiUtils.playThis(random.getId(), false);
            else apiUtils.autoQueue(ranking, selectedTrack);
                //apiUtils.playThis(library.next().getRandom().getTrackId(), false);
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
