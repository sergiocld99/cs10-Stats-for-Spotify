package cs10.apps.web.statsforspotify.utils;

import com.google.gson.JsonArray;
import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.enums.AlbumType;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.model_objects.miscellaneous.Device;
import com.wrapper.spotify.model_objects.specification.*;
import cs10.apps.desktop.statsforspotify.model.Ranking;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.web.statsforspotify.app.Private;
import cs10.apps.web.statsforspotify.view.OptionPanes;
import cs10.apps.web.statsforspotify.view.histogram.DailyMixesFrame;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ApiUtils {
    private final SpotifyApi spotifyApi;
    private final Random random;
    private ArrayList<Track> missedTracks;
    private final Set<String> enqueuedUris = new HashSet<>();
    private final boolean ready;

    // This URI should equal to the saved URI on the App Dashboard
    private static final URI redirectUri = SpotifyHttpManager.makeUri("http://localhost:8080");
    private static final String SCOPE = "user-top-read " +
            "user-read-currently-playing user-read-playback-state user-read-recently-played " +
            "user-modify-playback-state playlist-read-private";


    public ApiUtils(){
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(Private.CLIENT_ID)
                .setClientSecret(Private.CLIENT_SECRET_ID)
                .setRedirectUri(redirectUri)
                .build();

        this.ready = authenticate();
        this.random = new Random();
    }

    private boolean authenticate(){
        try {
            ClientCredentials credentials = spotifyApi.clientCredentials().build().execute();
            spotifyApi.setAccessToken(credentials.getAccessToken());
            return true;
        } catch (SpotifyWebApiException e){
            Maintenance.writeErrorFile(e, false);
        } catch (Exception e){
            Maintenance.writeErrorFile(e, true);
        }

        return false;
    }

    public boolean isReady() {
        return ready;
    }

    public void openGrantPermissionPage() throws IOException {
        URI uri = spotifyApi.authorizationCodeUri()
                .scope(SCOPE)
                .build().execute();

        if (Desktop.isDesktopSupported()
                && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
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
            Maintenance.writeErrorFile(e, true);
        }
    }

    public CurrentlyPlaying getCurrentSong() {
        try {
            return spotifyApi.getUsersCurrentlyPlayingTrack().build().execute();
        } catch (Exception e){
            Maintenance.writeErrorFile(e, true);
            return null;
        }
    }

    private Track findPopularTrackOfArtist(String artistId){
        try {
            Track[] tracks = spotifyApi.getArtistsTopTracks(artistId, CountryCode.AR).build().execute();
            if (tracks.length > 0) return tracks[random.nextInt(tracks.length)];
        } catch (Exception e){
            Maintenance.writeErrorFile(e, true);
        }

        return null;
    }

    private void queue(String uri){
        try {
            if (enqueuedUris.contains(uri)) return;
            spotifyApi.addItemToUsersPlaybackQueue(uri).build().execute();
            enqueuedUris.add(uri);
        } catch (Exception e){
            Maintenance.writeErrorFile(e, true);
        }
    }

    public ArrayList<String> autoQueue(Ranking ranking, Track current){
        Song song1 = ranking.getRandomElement();
        Song song2 = ranking.getRandomElement();

        Track[] tracks1;
        TrackSimplified t2;

        if (missedTracks != null && missedTracks.size() > 1){
            try {
                for (int i=0; i<2; i++){
                    Track mt = missedTracks.remove(0);
                    if (missedTracks.size() % 5 == 0){
                        Track pt = findPopularTrackOfArtist(mt.getArtists()[0].getId());
                        if (pt != null) queue(pt.getUri());
                    } else queue(mt.getUri());
                }
                System.out.println(missedTracks.size() + " missed tracks left");
            } catch (Exception e){
                Maintenance.writeErrorFile(e, true);
            }

            // do not continue
            return null;
        }

        try {
            Recommendations r = getRecommendations(song1.getId(), song2.getId(),
                    (current == null) ? ranking.getRandomElement().getId() : current.getId());
            t2 = r.getTracks()[random.nextInt(r.getTracks().length)];
            tracks1 = spotifyApi.getArtistsTopTracks(t2.getArtists()[0].getId(), CountryCode.AR)
                    .build().execute();
            System.out.println(Arrays.toString(tracks1));
        } catch (Exception e){
            Maintenance.writeErrorFile(e, true);
            return null;
        }

        if (tracks1[0].getPopularity() < 70) {
            Maintenance.log("AU || Bad Recommendation: " + CommonUtils.toString(tracks1[0]));
            Maintenance.log("AU || Cause: popularity is " + tracks1[0].getPopularity());
            return null;
        }

        Track t1 = tracks1[random.nextInt(tracks1.length)];
        Song t3 = IOUtils.pickRandomSongFromLibrary();

        ArrayList<String> uris = new ArrayList<>();
        StringBuilder errorSb = new StringBuilder("Failed to queue: \n\n");

        if (IOUtils.existsArtist(t1.getArtists()[0].getName())){
            Maintenance.log("AU || Added from Artist Top Tracks: " + CommonUtils.toString(t1));
            uris.add(t1.getUri());
            errorSb.append(CommonUtils.toString(t1)).append('\n');
            if (!t2.getName().equals(t1.getName())) {
                Maintenance.log("AU || Added from Recommendations: " + CommonUtils.toString(t2));
                errorSb.append(CommonUtils.toString(t2));
                uris.add(t2.getUri());
            }
        } else {
            Maintenance.log("AU || Added from Recommendations: " + CommonUtils.toString(t2));
            uris.add(t2.getUri());
            errorSb.append(CommonUtils.toString(t2)).append('\n');
            if (t3 != null){
                Maintenance.log("AU || Added from Library: " + t3.toStringWithArtist());
                errorSb.append(t3.toStringWithArtist());
                uris.add("spotify:track:"+t3.getId());
            }
        }

        try {
            for (String uri : uris){
                queue(uri);
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (InterruptedException e){
            Maintenance.writeErrorFile(e, false);
        }

        return uris;
    }

    public Track getTrackById(String id){
        try {
            return spotifyApi.getTrack(id).build().execute();
        } catch (Exception e){
            Maintenance.writeErrorFile(e, true);
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
            missedTracks = new ArrayList<>();
            missedTracks.addAll(Arrays.asList(tracks2).subList(mostPopularIndex2 + 1, tracks2.length));
            Collections.shuffle(missedTracks);
        } catch (Exception e){
            Maintenance.writeErrorFile(e, true);
        }

        if (result == null) result = new Track[0];
        return result;
    }

    public void addToMissedTracks(Collection<Track> tracks){
        System.out.println(tracks.size() + " tracks added to Missed List");
        missedTracks.addAll(tracks);
        Collections.shuffle(missedTracks);
    }

    public Track[] getTopTracks(String termKey){

        try {
            return spotifyApi.getUsersTopTracks().time_range(termKey)
                    .limit(50).build().execute().getItems();
        } catch (Exception e){
            Maintenance.writeErrorFile(e, true);
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
            Maintenance.writeErrorFile(e, true);
            return null;
        }
    }

    public boolean skipCurrentTrack(){
        try {
            spotifyApi.skipUsersPlaybackToNextTrack().build().execute();
            return true;
        } catch (SpotifyWebApiException e){
            Maintenance.writeErrorFile(e, false);
        } catch (Exception e){
            Maintenance.writeErrorFile(e, true);
        }

        return false;
    }

    public boolean playThis(String trackId, boolean immediately){
        try {
            Device[] devices = spotifyApi.getUsersAvailableDevices().build().execute();
            if (devices == null || devices.length == 0) return false;

            JsonArray array = new JsonArray();
            array.add("spotify:track:"+trackId);

            if (immediately){
                Device active = null;

                for (Device device : devices){
                    if (device.getIs_active()){
                        active = device;
                        break;
                    }
                }

                if (active == null) active = devices[0];
                spotifyApi.startResumeUsersPlayback().device_id(active.getId())
                        .uris(array).build().execute();
                return true;
            }

            queue("spotify:track:"+trackId);
            return true;
        } catch (SpotifyWebApiException e){
            Maintenance.writeErrorFile(e, false);
        } catch (Exception e){
            Maintenance.writeErrorFile(e, true);
        }

         return false;
    }

    public User getUser() throws Exception {
        return spotifyApi.getCurrentUsersProfile().build().execute();
    }

    public PlayHistory[] getRecentTracks(){
        try {
            // This returns only the last 30 played tracks
            return spotifyApi.getCurrentUsersRecentlyPlayedTracks()
                    .limit(50).build().execute().getItems();
        } catch (Exception e){
            Maintenance.writeErrorFile(e, true);
            return new PlayHistory[0];
        }
    }

    public Collection<Track> findDailyMix(int number, int minPopularity){
        Collection<Track> result = new HashSet<>();
        if (number < 1 || number > 6) number = random.nextInt(6) + 1;

        try {
            PlaylistSimplified[] ps = spotifyApi.getListOfCurrentUsersPlaylists()
                    .limit(49).build().execute().getItems();

            for (PlaylistSimplified p : ps){
                if (p.getName().equals("Daily Mix " + number)){
                    System.out.println(p.getName() + " was selected for Missed Tracks");
                    Playlist playlist = spotifyApi.getPlaylist(p.getId()).build().execute();
                    for (PlaylistTrack pt : playlist.getTracks().getItems()){
                        Track track = (Track) pt.getTrack();
                        if (track.getPopularity() > minPopularity)
                            result.add(track);
                    }
                    break;
                }
            }
        } catch (Exception e){
            Maintenance.writeErrorFile(e, true);
        }

        return result;
    }

    public List<Playlist> getDailyMixes(){
        List<Playlist> result = new ArrayList<>(6);

        try {
            PlaylistSimplified[] ps = spotifyApi.getListOfCurrentUsersPlaylists()
                    .limit(49).build().execute().getItems();

            for (PlaylistSimplified p : ps){
                if (p.getName().startsWith("Daily Mix")){
                    Playlist playlist = spotifyApi.getPlaylist(p.getId()).build().execute();
                    result.add(playlist);
                    if (result.size() == 6) break;
                }
            }
        } catch (Exception e){
            Maintenance.writeErrorFile(e, true);
        }

        return result;
    }

    public void analyzeDailyMixes(){
        int dailyMixes = 6, count = 0;
        int[] tracks = new int[dailyMixes];
        int[] sizes = new int[dailyMixes];
        int[] artists = new int[dailyMixes];
        int[] times = new int[dailyMixes];

        try {
            PlaylistSimplified[] ps = spotifyApi.getListOfCurrentUsersPlaylists()
                    .limit(49).build().execute().getItems();
            for (PlaylistSimplified p : ps){
                if (p.getName().startsWith("Daily Mix")){
                    int popularitySum = 0;
                    sizes[count] = p.getTracks().getTotal();
                    Playlist playlist = spotifyApi.getPlaylist(p.getId()).build().execute();
                    for (PlaylistTrack pt : playlist.getTracks().getItems()){
                        Track t = (Track) pt.getTrack();
                        popularitySum += t.getPopularity();
                        int ts = IOUtils.getTimesOnRanking(t.getArtists()[0].getName(), t.getId());
                        if (ts > 0) {tracks[count]++; times[count] += ts;}
                        if (IOUtils.existsArtist(t.getArtists()[0].getName()))
                            artists[count]++;
                    }
                    System.out.println(p.getName() + " average popularity: " +
                            popularitySum / p.getTracks().getTotal());
                    if (++count == dailyMixes) break;
                }
            }
        } catch (Exception e){
            Maintenance.writeErrorFile(e, true);
        }

        for (int i=0; i<dailyMixes; i++){
            System.out.println("DAILY MIX " + (i+1));
            System.out.println(tracks[i] + "/" + sizes[i] + " tracks are already in your library");
            int artistsPercentage = sizes[i] > 0 ? artists[i] * 100 / sizes[i] : 0;
            System.out.println(artistsPercentage + "% artists that you already know");
        }

        new DailyMixesFrame(times).init();
    }

    public void enqueueTwoTracksOfTheSameAlbum(Track track){
        int actualNumber = track.getTrackNumber() - 1;  // 1 -> 0
        if (track.getAlbum().getAlbumType() == AlbumType.SINGLE){
            System.err.println("This track is a single - Unable to enqueue album");
            return;
        }

        try {
            Album album = spotifyApi.getAlbum(track.getAlbum().getId()).build().execute();
            int max = album.getTracks().getItems().length;
            if (max < 6) {
                System.err.println("This album has less than 3 songs. That's not enough");
                return;
            }

            int n1, n2;

            do {
                n1 = random.nextInt(max);
                n2 = random.nextInt(max);
            } while (n1 == actualNumber || n2 == actualNumber || n1 == n2);

            playThis(album.getTracks().getItems()[n1].getId(), false);
            playThis(album.getTracks().getItems()[n2].getId(), false);
            System.out.println("Two tracks from " + album.getName() + " enqueued!");
        } catch (Exception e) {
            Maintenance.writeErrorFile(e, true);
            e.printStackTrace();
        }
    }
}
