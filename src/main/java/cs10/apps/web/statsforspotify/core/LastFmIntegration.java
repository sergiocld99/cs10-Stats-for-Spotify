package cs10.apps.web.statsforspotify.core;

import com.wrapper.spotify.model_objects.specification.Track;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.web.statsforspotify.app.Private;
import cs10.apps.web.statsforspotify.model.BigRanking;
import cs10.apps.web.statsforspotify.model.Fanaticism;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LastFmIntegration {

    public static List<Fanaticism> analyzeFanaticism(BigRanking ranking, String user){
        List<Fanaticism> results = new ArrayList<>();

        for (Song s : ranking){
            double topFan = analyzeFanaticism(s,user);
            if (topFan > 0.5) results.add(new Fanaticism(s,topFan));
        }

        return results;
    }

    public static double analyzeFanaticism(Song song, String user){
        String baseUrl = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=";
        String url = baseUrl + Private.LAST_FM_API_KEY + "&artist=" +
                normalize(song.getArtists().split(", ")[0]) +
                "&track=" + normalize(song.getName()) +
                "&username=" + user + "&format=json";

        try {
            String source = readJsonFromUrl(url);
            JSONObject jsonObject = new JSONObject(source);
            JSONObject track = jsonObject.getJSONObject("track");
            double fansMinutes = track.getInt("playcount") / Math.sqrt(track.getInt("listeners"));
            double myMinutes = 3 * track.getInt("userplaycount");
            double percentage = myMinutes * 100 / fansMinutes;
            if (percentage > 1) return Math.log(percentage) / Math.log(100);
        } catch (Exception e){
            System.err.println("No data found for " + song.getName());
        }

        return -1;
    }

    public static int getPlayCount(Track nowPlayingTrack, String user){
        if (user == null || user.isEmpty()) return -1;

        String baseUrl = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=";
        String url = baseUrl + Private.LAST_FM_API_KEY + "&artist=" +
                normalize(nowPlayingTrack.getArtists()[0].getName()) +
                "&track=" + normalize(nowPlayingTrack.getName()) +
                "&username=" + user + "&format=json";

        try {
            String source = readJsonFromUrl(url);
            JSONObject jsonObject = new JSONObject(source);
            JSONObject track = jsonObject.getJSONObject("track");
            return track.getInt("userplaycount");
        } catch (Exception e){
            System.err.println("No data found for " + nowPlayingTrack.getName());
        }

        return -1;
    }

    private static String normalize(String str){
        return str.replace(" ", "+")
                .replace("&","%26")
                .replace("é","%E9").toLowerCase();
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
