package cs10.apps.web.statsforspotify.utils;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.model_objects.specification.Recommendations;
import com.wrapper.spotify.model_objects.specification.Track;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.web.statsforspotify.app.Private;
import org.apache.hc.core5.http.ParseException;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

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
            e.printStackTrace();
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

    public boolean addToQueue(Song song){
        try {
            spotifyApi.addItemToUsersPlaybackQueue("spotify:track:"+song.getId()).build().execute();
            return true;
        } catch (SpotifyWebApiException e){
            System.err.println("You don't have Premium :(");
        } catch (Exception e){
            e.printStackTrace();
        }

        return false;
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

        System.out.println("The most popular track is " + tracks[result].getName());
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
            e.printStackTrace();
        }

        if (result == null) result = new Track[0];
        return result;
    }

    public Track[] getTopTracks(String termKey){

        try {
            return spotifyApi.getUsersTopTracks().time_range(termKey)
                    .limit(50).build().execute().getItems();
        } catch (Exception e){
            e.printStackTrace();
            return new Track[0];
        }
    }

    public Recommendations getRecommendations(String id){
        try {
            return spotifyApi.getRecommendations().seed_tracks(id).build().execute();
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
