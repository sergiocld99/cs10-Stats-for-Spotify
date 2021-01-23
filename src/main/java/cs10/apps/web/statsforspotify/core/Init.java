package cs10.apps.web.statsforspotify.core;

import com.wrapper.spotify.model_objects.specification.Track;
import cs10.apps.web.statsforspotify.io.Library;
import cs10.apps.web.statsforspotify.model.BigRanking;
import cs10.apps.web.statsforspotify.model.TopTerms;
import cs10.apps.web.statsforspotify.utils.ApiUtils;
import cs10.apps.web.statsforspotify.utils.CommonUtils;
import cs10.apps.web.statsforspotify.utils.IOUtils;
import cs10.apps.web.statsforspotify.utils.Maintenance;
import cs10.apps.web.statsforspotify.view.CustomPlayer;
import cs10.apps.web.statsforspotify.view.OptionPanes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Init {
    private Library library;
    private BigRanking apiRanking, diskRanking;
    private final ApiUtils apiUtils;
    private List<Track> resultTracks;
    private String userId;

    public Init(ApiUtils apiUtils) {
        this.apiUtils = apiUtils;
    }

    public void execute(CustomPlayer player){
        long startTime = System.currentTimeMillis();
        Thread apiThread = new Thread(() ->
                apiRanking = select2(), "Build Ranking from API (Beta 1.04.6)");
        Thread libraryThread = new Thread(() ->
                library = Library.getInstance(player), "Get Instance of Library");

        apiThread.start();
        libraryThread.start();

        try {
            userId = apiUtils.getUser().getId();
            apiThread.join();
            libraryThread.join();
        } catch (Exception e){
            Maintenance.writeErrorFile(e, true);
            OptionPanes.message("Unable to get user ID");
            System.exit(1);
        }

        boolean showSummary = false;
        long[] savedCodes = IOUtils.getSavedRankingCodes(userId);
        long actualCode = apiRanking.getCode();

        System.out.println("Actual Code is " + actualCode);
        System.out.println("Saved Code is " + savedCodes[1]);

        if (actualCode > 0 && actualCode != savedCodes[1]){
            IOUtils.updateRankingCodes(savedCodes[1], actualCode, userId);
            IOUtils.save(apiRanking, true);
            library.update(apiRanking);
            showSummary = true;
        } else library.relink(apiRanking);

        //new Thread(() -> ).start();
        diskRanking = getLastRankingFromDisk();
        apiRanking.updateAllStatus(diskRanking);
        library.analyze();

        if (showSummary) new Thread(() ->
                CommonUtils.summary(apiRanking, diskRanking, apiUtils),
                "Summary of New Ranking").start();

        System.out.println("Init -- " + (System.currentTimeMillis() - startTime) + " ms");
    }

    private BigRanking selectCustomRankingFromAPI(){
        Track[] tracks1 = apiUtils.getUntilMostPopular(TopTerms.SHORT.getKey(), 50);
        Track[] tracks2 = apiUtils.getTopTracks(TopTerms.MEDIUM.getKey());

        if (tracks1.length == 0 || tracks2.length == 0) {
            OptionPanes.message("Unable to build Ranking. Cause: not enough songs");
            System.exit(1);
        }

        return buildRanking(tracks1, tracks2, 100);
    }

    private BigRanking select2(){
        Track[] tracks2 = apiUtils.getUntilMostPopular(TopTerms.MEDIUM.getKey(), 50);
        if (tracks2.length == 0) {
            OptionPanes.message("Unable to build Ranking. Cause: not enough songs");
            System.exit(1);
        }

        Track[] tracks1 = apiUtils.getUntilPosition(TopTerms.SHORT.getKey(), 50 + CommonUtils.countArtists(tracks2));
        return buildRanking(tracks1, tracks2, 200);
    }

    private BigRanking buildRanking(Track[] tracks1, Track[] tracks2, int maxSize){
        resultTracks = new ArrayList<>(Arrays.asList(tracks1));
        List<Track> repeatTracks = new ArrayList<>();
        CommonUtils.combineWithoutRepeats(tracks1, tracks2, maxSize, resultTracks, repeatTracks);
        BigRanking bigRanking = new BigRanking(resultTracks);
        bigRanking.updateRepeated(repeatTracks);
        return bigRanking;
    }

    private BigRanking getLastRankingFromDisk(){

        try {
            return IOUtils.getLastRanking(userId);
        } catch (Exception e){
            Maintenance.writeErrorFile(e, true);
            OptionPanes.message("Unable to get your user ID. Please, check the app permissions");
            System.exit(2);
            return null;
        }
    }

    public Library getLibrary() {
        return library;
    }

    public BigRanking getProcessedRanking(){
        return apiRanking;
    }

    public List<Track> getResultTracks() {
        return resultTracks;
    }
}
