package cs10.apps.desktop.statsforspotify.utils;

import cs10.apps.desktop.statsforspotify.model.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OldIOUtils {
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
            JOptionPane.showMessageDialog(null,
                    "This ranking and the one created at " +
                            dateFormat.format(new Date(file.lastModified())) + " are the same",
                    "IOUtils says...", JOptionPane.INFORMATION_MESSAGE
                    );
        }

        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(ranking.getTitle() + " - ");
        bufferedWriter.write(dateFormat.format(new Date(System.currentTimeMillis())) + "\n");
        bufferedWriter.write("biggest gain: " + ranking.getBiggestGain().toStringForRanking() + "\n");
        bufferedWriter.write("biggest loss: " + ranking.getBiggestLoss().toStringForRanking() + "\n\n");
        ranking.sortByDefault();

        for (Song s : ranking){
            bufferedWriter.write(s.toStringForRanking() + "\n");
            appendArtistLog(s, filepath);
        }

        bufferedWriter.close();
        fileWriter.close();
    }

    public static void appendArtistLog(Song song, String filepath) throws IOException {
        // Do not include invariant ranks
        if (song.getStatus() == Status.NOTHING) return;

        String[] artists = song.getArtists().split(", ");

        for (String a : artists){
            // FIX: Axwell /\ Ingrosso
            a = a.replace("/\\", "");

            File file = new File( filepath + a.trim() + ".txt");
            FileWriter fileWriter = new FileWriter(file, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write(dateFormat.format(new Date(song.getTimestamp())) + " - ");
            bufferedWriter.write(song.toStringWithoutArtists() + "\n");
            bufferedWriter.close();
            fileWriter.close();
        }
    }

    public static Library getArtistsFromLogs() throws IOException {
        Library library = new Library();

        File logsDir = new File("logs//");
        File[] rankFolders = logsDir.listFiles(File::isDirectory);
        if (rankFolders != null){
            for (int i=0; i<rankFolders.length; i++){
                File[] logs = rankFolders[i].listFiles();
                if (logs != null){
                    for (File log : logs) {
                        String name = log.getName().replace(".txt", "");
                        Artist a = library.findByName(name);
                        if (a == null) {
                            a = new Artist();
                            a.setName(name);
                            library.add(a);
                        }
                        updateArtist(log, a);
                        a.addScore(log.length(), i);
                    }
                }
            }
        }

        return library;
    }

    private static void updateArtist(File file, Artist artist) throws IOException {
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            String[] params = line.split(" - ");
            String rank = params[1].replace("#", "").trim();
            String name = params[2].trim();
            int parenthesisIndex = name.indexOf("(");
            try {
                if (parenthesisIndex > 0){
                    name = name.substring(0, parenthesisIndex).trim();
                }
                if (!artist.hasSong(name)) artist.addSong(name);
                artist.addTimeOn(Integer.parseInt(rank));
            } catch (StringIndexOutOfBoundsException e) {
                System.err.println("An error occurred with the song " + name +
                        " on file " + file.getAbsolutePath());
            } catch (NumberFormatException e){
                System.err.println("An error occurred with the rank " + rank +
                        " on file " + file.getAbsolutePath());
            }
        }
    }

    public static ImageIcon getImageIcon(Status status){
        return new ImageIcon(status.getPath());
    }
}
