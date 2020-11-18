package cs10.apps.web.statsforspotify.utils;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.specification.User;
import cs10.apps.desktop.statsforspotify.model.Ranking;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.desktop.statsforspotify.model.Status;
import cs10.apps.web.statsforspotify.app.Private;
import cs10.apps.web.statsforspotify.model.TopTerms;
import org.apache.hc.core5.http.ParseException;

import javax.swing.*;
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
            System.out.println("Attempting to connect with " + code);
            AuthorizationCodeCredentials credentials = spotifyApi.authorizationCode(code).build().execute();
            System.out.println("These credentials expires in " + credentials.getExpiresIn());
            spotifyApi.setAccessToken(credentials.getAccessToken());
            spotifyApi.setRefreshToken(credentials.getRefreshToken());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public User getUser() throws Exception {
        return spotifyApi.getCurrentUsersProfile().build().execute();
    }

    public Paging<Track> getPaging(TopTerms term){
        try {
            return spotifyApi.getUsersTopTracks().time_range(term.getKey())
                    .limit(50).build().execute();
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
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
                song.setArtists(CommonUtils.combineArtists(tracks[i].getArtists()));
                song.setRank(i+1);
                song.setTimestamp(System.currentTimeMillis());
                song.setStatus(Status.NEW);
                song.setImageUrl(tracks[i].getAlbum().getImages()[0].getUrl());
                song.setId(tracks[i].getId());
                song.setPopularity(tracks[i].getPopularity());
                ranking.add(song);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return ranking;
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

    public void printTrackInfo(String id){
        try {
            Track track = spotifyApi.getTrack(id).build().execute();
            JOptionPane.showMessageDialog(null,
                    track.getName() + " by " + track.getArtists()[0].getName(),
                    "Song that left the chart", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e){
            e.printStackTrace();
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
        min = Math.min(min, 50);

        try {
            Track[] tracks1 = spotifyApi.getUsersTopTracks().time_range(termKey)
                    .limit(min).build().execute().getItems();
            Track[] tracks2 = spotifyApi.getUsersTopTracks().time_range(termKey)
                    .limit(49).offset(min).build().execute().getItems();

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

    public Track[] getUntilPosition(String termKey, int position){

        try {
            return spotifyApi.getUsersTopTracks().time_range(termKey)
                    .limit(Math.min(position, 50)).build().execute().getItems();
        } catch (Exception e){
            e.printStackTrace();
            return new Track[0];
        }
    }
}
