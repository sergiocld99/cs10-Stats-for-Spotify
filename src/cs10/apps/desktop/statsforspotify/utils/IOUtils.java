package cs10.apps.desktop.statsforspotify.utils;

import cs10.apps.desktop.statsforspotify.model.Ranking;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.desktop.statsforspotify.model.Status;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IOUtils {
    private static final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    public static Ranking parseSongs(String filename) throws IOException {
        StringBuilder sb = readFile(filename);
        String line;

        line = sb.toString();
        Document document = Jsoup.parse(line);
        Element target = document.selectFirst("body").selectFirst("tbody");
        Ranking ranking = new Ranking();

        // TITLE
        ranking.setTitle(document.selectFirst("h2").text());

        for (Element row : target.select("tr")){
            Elements elements = row.getAllElements();
            if (elements.size() < 10) continue;
            else if (elements.size() > 10){
                elements.remove(0);
            }

            try {
                Song song = new Song();
                song.setInfoStatus(elements.get(1).attr("title"));
                song.setRank(Integer.parseInt(elements.get(3).text()));
                song.setName(elements.get(8).text());
                song.setArtists(elements.get(9).text());
                song.setTimestamp(System.currentTimeMillis());
                song.validate();
                ranking.add(song);
            } catch (NumberFormatException e){
                System.err.println("Unable to parse song #" + (ranking.size()+1));
            }

        }

        return ranking;
    }

    private static StringBuilder readFile(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()){
            System.err.println("file not found");
        }

        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = bufferedReader.readLine()) != null){
            sb.append(line);
        }

        bufferedReader.close();
        fileReader.close();
        return sb;
    }

    public static void saveFile(Ranking ranking) throws IOException {
        String filteredTitle = ranking.getTitle().substring(
                ranking.getTitle().indexOf("(")+1,ranking.getTitle().indexOf(")"));
        String filepath = "logs//" + filteredTitle + "//";

        File dir1 = new File(filepath);
        if (!dir1.exists()) {
            if (dir1.mkdirs()){
                System.out.println("Directory created successfully");
            }
        }

        File file = new File("logs//" + ranking.getCode() + ".txt");
        if (file.exists()){
            System.err.println("File already exists, skipping writing");
            return;
        }

        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(ranking.getTitle() + " - ");
        bufferedWriter.write(dateFormat.format(new Date(System.currentTimeMillis())) + "\n");
        bufferedWriter.write("biggest gain: " + ranking.getBiggestGain() + "\n");
        bufferedWriter.write("biggest loss: " + ranking.getBiggestLoss() + "\n\n");
        ranking.sortByDefault();

        for (Song s : ranking){
            bufferedWriter.write(s.toString() + "\n");
            appendArtistLog(s, filepath);
        }

        bufferedWriter.close();
        fileWriter.close();
    }

    private static void appendArtistLog(Song song, String filepath) throws IOException {
        // Do not include invariant ranks
        if (song.getChange() == 0) return;

        String[] artists = song.getArtists().split(",");

        for (String a : artists){
            // FIX: Axwell /\ Ingrosso
            a = a.replace("/", "").replace("\\", "");

            File file = new File( filepath + a.trim().toLowerCase() + ".txt");
            FileWriter fileWriter = new FileWriter(file, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write(dateFormat.format(new Date(song.getTimestamp())) + " - ");
            bufferedWriter.write(song.toStringWithoutArtists() + "\n");
            bufferedWriter.close();
            fileWriter.close();
        }
    }

    public static ImageIcon getImageIcon(Status status){
        return new ImageIcon(status.getPath());
    }
}
