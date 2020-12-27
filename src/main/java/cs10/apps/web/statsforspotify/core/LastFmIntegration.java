package cs10.apps.web.statsforspotify.core;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LastFmIntegration {

    public static int getPlayCount(String artistName, String trackName, String user, String key){
        if (user == null || user.isEmpty()) return -1;

        String baseUrl = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=";
        String url = baseUrl + key + "&artist=" + replaceBlank(artistName) +
                "&track=" + replaceBlank(trackName) +
                "&username=" + user + "&format=json";

        try {
            String source = readJsonFromUrl(url);
            JSONObject jsonObject = new JSONObject(source);
            JSONObject track = jsonObject.getJSONObject("track");
            System.out.println(track.getString("name"));
            System.out.println("Listeners: " + track.getInt("listeners"));
            System.out.println("Streams: " + track.getInt("playcount"));
            return track.getInt("userplaycount");
        } catch (Exception e){
            System.err.println("No data found for " + trackName);
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
            System.err.println("No data found for " + musicBrainzID);
        }

        return -1;
    }

    private static String replaceBlank(String str){
        return str.replace(" ", "+").toLowerCase();
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
