package cs10.apps.web.statsforspotify.utils;

import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.model_objects.specification.*;
import cs10.apps.desktop.statsforspotify.model.Ranking;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.web.statsforspotify.app.Private;
import cs10.apps.web.statsforspotify.model.BigRanking;
import cs10.apps.web.statsforspotify.view.OptionPanes;
import org.apache.hc.core5.http.ParseException;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Random;

public class ApiUtils {
    private final SpotifyApi spotifyApi;
    private final boolean ready;

    // This URI should equal to the saved URI on the App Dashboard
    private static final URI redirectUri = SpotifyHttpManager.makeUri("http://localhost:8080");
    private static final String SCOPE = "user-top-read  user-read-currently-playing user-modify-playback-state";


    public ApiUtils(){
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(Private.CLIENT_ID)
                .setClientSecret(Private.CLIENT_SECRET_ID)
                .setRedirectUri(redirectUri)
                .build();

        this.ready = authenticate();
    }

    private boolean authenticate(){
        try {
            ClientCredentials credentials = spotifyApi.clientCredentials().build().execute();
            spotifyApi.setAccessToken(credentials.getAccessToken());
            return true;
        } catch (SpotifyWebApiException | ParseException | IOException e){
            System.err.println("Unable to get credentials");
            e.printStackTrace();
            return false;
        }
    }

    public boolean isReady() {
        return ready;
    }

    public void openGrantPermissionPage() throws IOException {
        URI uri = spotifyApi.authorizationCodeUri()
                .scope(SCOPE)
                .show_dialog(true)
                .build().execute();

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(uri);
        }
    }

    public void openReconfirmPermissionPage() throws IOException {
        URI uri = spotifyApi.authorizationCodeUri()
                .scope(SCOPE)
                .build().execute();

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(uri);
        }
    }

    public void refreshToken(String code) {
        try {
            AuthorizationCodeCredentials credentials = spotifyApi.
                    authorizationCode(code).build().execute();
            System.out.println("These credentials expires in " + credentials.getExpiresIn());
            spotifyApi.setAccessToken(credentials.getAccessToken());
            spotifyApi.setRefreshToken(credentials.getRefreshToken());
        } catch (Exception e){
            OptionPanes.showError("ApiUtils - Refresh Token", e);
        }
    }

    public CurrentlyPlaying getCurrentSong() {
        try {
            return spotifyApi.getUsersCurrentlyPlayingTrack().build().execute();
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<String> autoQueue(Ranking ranking, Track current){
        Song song1 = ranking.getRandomElement();
        Song song2 = ranking.getRandomElement();

        Track[] tracks1;
        TrackSimplified t2;

        try {
            Recommendations r = getRecommendations(song1.getId(), song2.getId(), current.getId());
            t2 = r.getTracks()[new Random().nextInt(r.getTracks().length)];
            tracks1 = spotifyApi.getArtistsTopTracks(t2.getArtists()[0].getId(), CountryCode.AR)
                    .build().execute();
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }

        if (tracks1[0].getPopularity() < 70) {
            System.err.println("Bad Recommendation: " + CommonUtils.toString(tracks1[0]));
            System.err.println("Cause: popularity is " + tracks1[0].getPopularity());
            return null;
        }

        Track t1 = tracks1[new Random().nextInt(tracks1.length)];
        Song t3 = IOUtils.pickRandomSongFromLibrary();

        ArrayList<String> uris = new ArrayList<>();
        StringBuilder errorSb = new StringBuilder("Failed to queue: \n\n");

        if (IOUtils.existsArtist(t1.getArtists()[0].getName())){
            uris.add(t1.getUri());
            errorSb.append(CommonUtils.toString(t1)).append('\n');
            if (!t2.getName().equals(t1.getName())) {
                errorSb.append(CommonUtils.toString(t2));
                uris.add(t2.getUri());
            }
        } else {
            uris.add(t2.getUri());
            errorSb.append(CommonUtils.toString(t2)).append('\n');
            if (t3 != null){
                errorSb.append(t3.toStringWithArtist());
                uris.add("spotify:track:"+t3.getId());
            }
        }

        try {
            for (String uri : uris){
                spotifyApi.addItemToUsersPlaybackQueue(uri).build().execute();
                Thread.sleep(1000);
            }
        } catch (SpotifyWebApiException e){
            System.err.println(e.getMessage());
            System.err.println(errorSb.toString());
        } catch (Exception e){
            e.printStackTrace();
        }

        return uris;
    }

    public Track getTrackById(String id){
        try {
            return spotifyApi.getTrack(id).build().execute();
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private int findMostPopular(Track[] tracks){
        int max = 0, result = 0;
        for (int i=0; i<tracks.length; i++){
            Track track = tracks[i];
            if (track.getPopularity() > max){
                max = track.getPopularity();
                result = i;
            }
        }

        return result;
    }

    public Track[] getUntilMostPopular(String termKey, int min){
        Track[] result = null;
        min = Math.min(min, 49);

        try {
            Track[] tracks1 = spotifyApi.getUsersTopTracks().time_range(termKey)
                    .limit(min).build().execute().getItems();
            Track[] tracks2 = spotifyApi.getUsersTopTracks().time_range(termKey)
                    .limit(50).offset(min).build().execute().getItems();

            int mostPopularIndex2 = findMostPopular(tracks2);
            result = new Track[tracks1.length + mostPopularIndex2 + 1];
            System.arraycopy(tracks1, 0, result, 0, tracks1.length);
            System.arraycopy(tracks2, 0, result, tracks1.length, mostPopularIndex2 + 1);
        } catch (Exception e){
            Maintenance.writeErrorFile(e);
        }

        if (result == null) result = new Track[0];
        return result;
    }

    public Track[] getTopTracks(String termKey){

        try {
            return spotifyApi.getUsersTopTracks().time_range(termKey)
                    .limit(50).build().execute().getItems();
        } catch (Exception e){
            Maintenance.writeErrorFile(e);
            return new Track[0];
        }
    }

    public Recommendations getRecommendations(String... ids){
        try {
            StringBuilder sb = new StringBuilder(ids[0]);
            for (int i=1; i<ids.length; i++) sb.append(",").append(ids[i]);

            return spotifyApi.getRecommendations()
                    .seed_tracks(sb.toString()).build().execute();
        } catch (Exception e){
            Maintenance.writeErrorFile(e);
            return null;
        }
    }

    public boolean skipCurrentTrack(){
        try {
            spotifyApi.skipUsersPlaybackToNextTrack().build().execute();
            return true;
        } catch (SpotifyWebApiException e){
            System.err.println(e.getMessage());
        } catch (Exception e){
            Maintenance.writeErrorFile(e);
        }

        return false;
    }

    public boolean playThis(String trackId){
        try {
            spotifyApi.addItemToUsersPlaybackQueue("spotify:track:"+trackId).build().execute();
            spotifyApi.skipUsersPlaybackToNextTrack().build().execute();
            return true;
        } catch (SpotifyWebApiException e){
            System.err.println(e.getMessage());
        } catch (Exception e){
            Maintenance.writeErrorFile(e);
        }

         return false;
    }
}
