package cs10.apps.web.statsforspotify.utils;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.*;
import cs10.apps.desktop.statsforspotify.model.Ranking;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.desktop.statsforspotify.model.Status;
import cs10.apps.web.statsforspotify.app.Private;
import cs10.apps.web.statsforspotify.model.TopTerms;
import org.apache.hc.core5.http.ParseException;
import retrofit2.Retrofit;
import retrofit2.Retrofit.Builder;
import retrofit2.converter.gson.GsonConverterFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class ApiUtils {
    private final SpotifyApi spotifyApi;
    private final boolean ready;

    // This URI should equal to the saved URI on the App Dashboard
    private static final URI redirectUri0 = SpotifyHttpManager.makeUri("http://example.com/callback/");
    private static final URI redirectUri = SpotifyHttpManager.makeUri("http://localhost:8080");


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
                .scope("user-top-read")
                .show_dialog(true)
                .build().execute();

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(uri);
        }
    }

    public void refreshToken(String code) throws Exception {
        AuthorizationCodeCredentials credentials = spotifyApi.authorizationCode(code).build().execute();
        spotifyApi.setAccessToken(credentials.getAccessToken());
        spotifyApi.setRefreshToken(credentials.getRefreshToken());
    }

    public User getUser() throws Exception {
        return spotifyApi.getCurrentUsersProfile().build().execute();
    }

    public Ranking getRanking(TopTerms term){
        Ranking ranking = new Ranking();

        try {
            Paging<Track> paging = spotifyApi.getUsersTopTracks()
                    .time_range(term.getKey()).limit(50).build().execute();

            Track[] tracks = paging.getItems();

            for (int i=0; i<paging.getTotal(); i++){
                Song song = new Song();
                song.setName(tracks[i].getName());
                song.setArtists(combineArtists(tracks[i].getArtists()));
                song.setRank(i+1);
                song.setStatus(Status.NEW);
                song.setImageUrl(tracks[i].getAlbum().getImages()[0].getUrl());
                song.setPopularity(tracks[i].getPopularity());
                ranking.add(song);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return ranking;
    }

    private String combineArtists(ArtistSimplified[] arr){
        StringBuilder sb = new StringBuilder(arr[0].getName());
        for (int i=1; i<arr.length; i++) sb.append(", ").append(arr[i].getName());
        return sb.toString();
    }
}
