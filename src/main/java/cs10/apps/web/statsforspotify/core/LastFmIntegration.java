package cs10.apps.web.statsforspotify.core;

import cs10.apps.web.statsforspotify.app.Private;
import de.umass.lastfm.Track;
import de.umass.lastfm.User;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class LastFmIntegration {

    public static void main(String[] args) {
        String key = Private.LAST_FM_API_KEY;
        String user = "cs10_sergiocarp";

        Track track = getNowPlayingTrack(user, key);
        int count = getPlayCount(track.getArtist(), track.getName(), user, key);
        if (count <= 0) count = getPlayCount(track.getId(), user, key);
        System.out.println(track.getName() + ": " + count);
    }

    public static Track getNowPlayingTrack(String user, String key){
        Collection<Track> tracks = User.getRecentTracks(user, 1, 1, key).getPageResults();
        return tracks.iterator().next();
    }

    public static int getPlayCount(String artistName, String trackName, String user, String key){
        String baseUrl = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=";
        String url = baseUrl + key + "&artist=" + replaceBlank(artistName) +
                "&track=" + replaceBlank(trackName) +
                "&username=" + user + "&format=json";

        try {
            String source = readJsonFromUrl(url);
            JSONObject jsonObject = new JSONObject(source);
            return jsonObject.getJSONObject("track").getInt("userplaycount");
        } catch (Exception e){
            e.printStackTrace();
        }

        return -1;
    }

    public static int getPlayCount(String musicBrainzID, String user, String key){
        String baseUrl = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=";
        String url = baseUrl + key + "&mbid=" + musicBrainzID +
                "&username=" + user + "&format=json";

        try {
            String source = readJsonFromUrl(url);
            JSONObject jsonObject = new JSONObject(source);
            return jsonObject.getJSONObject("track").getInt("userplaycount");
        } catch (Exception e){
            e.printStackTrace();
        }

        return -1;
    }

    private static String replaceBlank(String str){
        return str.replace(" ", "%20%").toLowerCase();
    }

    public static String readJsonFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            return readAll(rd);
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}
