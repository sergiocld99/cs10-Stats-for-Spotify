package cs10.apps.web.statsforspotify.utils;

import com.wrapper.spotify.model_objects.specification.Track;
import cs10.apps.desktop.statsforspotify.model.Ranking;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.web.statsforspotify.model.Artist;
import cs10.apps.web.statsforspotify.model.BigRanking;
import cs10.apps.web.statsforspotify.view.OptionPanes;

import javax.swing.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class IOUtils {
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final String LIBRARY_FOLDER = "library";
    private static final String RANKING_FOLDER = "ranking";

    public static boolean isFirstTime(){
        return ! new File(RANKING_FOLDER).exists();
    }

    /**
     *
     * @return an array with compare [0] and last [1] codes
     */
    public static long[] readLastRankingCode(String userId){
        long[] result = new long[2];

        try (BufferedReader br = new BufferedReader(new FileReader(userId))){
            result[0] = Long.parseLong(br.readLine().split("=")[1]);
            result[1] = Long.parseLong(br.readLine().split("=")[1]);
        } catch (FileNotFoundException e){
            System.err.println("The file " + userId + " doesn't exist!");
        } catch (NumberFormatException e){
            System.err.println("The ranking code is not a number");
        } catch (ArrayIndexOutOfBoundsException e){
            System.err.println("The file " + userId + " has an invalid format");
        } catch (Exception e){
            Maintenance.writeErrorFile(e, true);
        }

        return result;
    }

    public static void saveLastRankingCode(long compareCode, long lastCode, String userId){
        File file = new File(userId);
        if (!file.exists()){
            try {
                System.out.println(userId + " created: " + file.createNewFile());
            } catch (IOException e){
                Maintenance.writeErrorFile(e, false);
            }
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))){
            pw.println("compare="+compareCode);
            pw.println("last="+lastCode);
        } catch (FileNotFoundException e){
            System.err.println("The file " + userId + " doesn't exist!");
        } catch (IOException e){
            Maintenance.writeErrorFile(e, false);
        }
    }

    public static void makeLibraryFiles(Ranking ranking, JProgressBar progressBar){
        int total = ranking.size(), i = 0;

        for (Song s : ranking){
            progressBar.setValue((i++) * 100 / total);
            String[] artists = s.getArtists().split(", ");
            for (String a : artists) makeArtistFiles(a, s, ranking.getCode());
        }
    }

    private static void makeArtistFiles(String artist, Song song, long code){
        artist = artist.replace("/\\","");
        boolean header = false;

        File directory = new File(LIBRARY_FOLDER+"//"+artist);
        if (!directory.exists()){
            System.out.println(directory.getPath() + " created: " + directory.mkdirs());
        }

        File songFile = new File(directory.getAbsolutePath() + "//" + song.getId());
        if (!songFile.exists()){
            try {
                header = songFile.createNewFile();
            } catch (IOException e){
                Maintenance.writeErrorFile(e, false);
            }
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(songFile, true))){
            if (header) pw.println(song.getName());
            pw.println(song.getRank()+"--"+song.getPopularity()+"--"+code);
        } catch (FileNotFoundException e){
            System.err.println(songFile.getPath() + " doesn't exist!");
        } catch (IOException e){
            Maintenance.writeErrorFile(e, false);
        }
    }

    public static BigRanking loadPreviousRanking(String userId){
        BigRanking ranking = new BigRanking();
        long code = readLastRankingCode(userId)[0];
        if (code == 0) return ranking;

        String filename = RANKING_FOLDER+"//"+ code;
        try (BufferedReader br = new BufferedReader(new FileReader(filename))){
            // skip header
            br.readLine();

            // read ranking
            String line;
            while ((line = br.readLine()) != null){
                String[] params = line.split("--");
                Song song = new Song();
                song.setRank(Integer.parseInt(params[0]));
                song.setId(params[1]);
                ranking.add(song);
            }
        } catch (FileNotFoundException e){
            System.err.println("The previous ranking " + code + " doesn't exist!");
        } catch (IOException e){
            Maintenance.writeErrorFile(e, false);
        } catch (NumberFormatException e){
            System.err.println("The rank column has an invalid format");
        } catch (ArrayIndexOutOfBoundsException e){
            System.err.println("The rows of Previous Ranking have an invalid format");
        }

        return ranking;
    }

    public static float[] getScores(String artist){
        artist = artist.replace("/\\","");
        float[] result = new float[10];

        File file = new File(LIBRARY_FOLDER+"//"+artist);
        File[] songsFiles = file.listFiles();
        if (songsFiles != null) {
            for (File f : songsFiles)
                readSongInfo(f, result);
        }

        return result;
    }

    private static void readSongInfo(File file, float[] scores){
        try (BufferedReader br = new BufferedReader(new FileReader(file))){
            // skip header
            br.readLine();

            // read info
            String line;
            while ((line = br.readLine()) != null){
                String[] params = line.split("--");
                int rank = Integer.parseInt(params[0]);
                int popularity = Integer.parseInt(params[1]);
                scores[(rank-1) / 10] += popularity / 100f;
            }
        } catch (FileNotFoundException e) {
            System.err.println(file.getPath() + " doesn't exist!");
        } catch (NumberFormatException e) {
            System.err.println("Columns aren't numbers!");
        } catch (ArrayIndexOutOfBoundsException e){
            System.err.println("Rows in " + file.getPath() + " don't have enough params");
        } catch (Exception e){
            Maintenance.writeErrorFile(e, true);
        }
    }

    public static int getTimesOnRanking(String artists, String id){
        String artist = artists.split(", ")[0].replace("/\\","");
        File file = new File(LIBRARY_FOLDER+"//"+artist+"//"+id);
        int cant = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))){
            // skip header
            br.readLine();

            // read info
            while (br.readLine() != null) cant++;
        } catch (FileNotFoundException e) {
            System.err.println(file.getPath() + " doesn't exist!");
        } catch (IOException e){
            Maintenance.writeErrorFile(e, false);
        }

        return cant;
    }

    public static int getRankingsAmount(){
        File[] files = new File(RANKING_FOLDER).listFiles();
        if (files == null || files.length == 0) return 1;
        else return files.length;
    }

    public static float getArtistScore(String artist){
        float[] scores = IOUtils.getScores(artist);
        float score = 0;

        for (int i=1; i<=scores.length; i++){
            score += scores[10-i] * i;
        }

        return score * 3 / IOUtils.getRankingsAmount();
    }

    public static Artist[] getAllArtistsScore(){
        File[] folders = new File(LIBRARY_FOLDER).listFiles();
        if (folders == null) return null;

        Artist[] result = new Artist[folders.length];
        for (int i=0; i<result.length; i++){
            result[i] = new Artist();
            result[i].setName(folders[i].getName());
            result[i].setScore(getArtistScore(folders[i].getName()));
        }

        return result;
    }

    public static int getFirstPopularity(Track track){
        String artist = track.getArtists()[0].getName();
        String trackID = track.getId();
        File file = new File(LIBRARY_FOLDER+"//"+artist+"//"+trackID);
        if (!file.exists()) return 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))){
            // skip header
            br.readLine();

            // read first line
            String line = br.readLine();
            return Integer.parseInt(line.split("--")[1]);
        } catch (IOException e){
            Maintenance.writeErrorFile(e, false);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e){
            System.err.println(file.getPath() + ": invalid format");
        }

        return 0;
    }

    public static boolean existsArtist(String name){
        File file = new File(LIBRARY_FOLDER+"//"+name);
        System.out.println("Checking existence of " + file.getPath());
        return file.exists();
    }

    public static Song pickRandomSongFromLibrary(){
        File[] artistFolders = new File(LIBRARY_FOLDER+"//").listFiles();
        if (artistFolders == null) return null;

        Random random = new Random();
        File pickedArtist = artistFolders[random.nextInt(artistFolders.length)];
        File[] songFiles = pickedArtist.listFiles();
        if (songFiles == null) return null;

        File pickedSong = songFiles[random.nextInt(songFiles.length)];
        Song song = new Song();
        song.setId(pickedSong.getName());
        song.setArtists(pickedArtist.getName());

        try (BufferedReader br = new BufferedReader(new FileReader(pickedSong))){
            song.setName(br.readLine());
        } catch (IOException e){
            Maintenance.writeErrorFile(e, false);
        }

        return song;
    }

    // -------------------------------- VERSION 2 --------------------------------------------
    public static void saveRanking(Ranking ranking, boolean replace){
        File directory = new File(RANKING_FOLDER);

        if (!directory.exists() && directory.mkdirs())
            System.out.println(directory.getAbsolutePath() + " has been just created");

        File file = new File(RANKING_FOLDER + "//" + ranking.getCode());

        if (!replace & file.exists()) return;
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))){
            writer.println(dateFormat.format(new Date(System.currentTimeMillis())));
            for (Song s : ranking) writer.println(s.getRank() + "--" + s.getId());
        } catch (IOException e){
            Maintenance.writeErrorFile(e, false);
        }
    }

}
