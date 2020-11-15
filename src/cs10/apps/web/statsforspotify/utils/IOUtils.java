package cs10.apps.web.statsforspotify.utils;

import com.wrapper.spotify.model_objects.specification.Track;
import cs10.apps.desktop.statsforspotify.model.Ranking;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.web.statsforspotify.model.BigRanking;
import cs10.apps.web.statsforspotify.model.TopTerms;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class IOUtils {
    private static final String DATA_FILE = "appdata";
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static boolean isFirstTime(){
        return ! new File(DATA_FILE).exists();
    }

    public static long[] readLastRankingCode(){
        long[] result = new long[2];

        try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE))){
            result[0] = Long.parseLong(br.readLine().split("=")[1]);
            result[1] = Long.parseLong(br.readLine().split("=")[1]);
        } catch (FileNotFoundException e){
            System.err.println("The file " + DATA_FILE + " doesn't exist!");
        } catch (NumberFormatException e){
            System.err.println("The ranking code is not a number");
        } catch (ArrayIndexOutOfBoundsException e){
            System.err.println("The file " + DATA_FILE + " has an invalid format");
        } catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }

    public static void saveLastRankingCode(long compareCode, long actualCode){
        File file = new File(DATA_FILE);
        if (!file.exists()){
            try {
                System.out.println(DATA_FILE + " created: " + file.createNewFile());
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_FILE))){
            pw.println("compare="+compareCode);
            pw.println("actual="+actualCode);
        } catch (FileNotFoundException e){
            System.err.println("The file " + DATA_FILE + " doesn't exist!");
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void makeLibraryFiles(Ranking ranking){
        for (Song s : ranking){
            String[] artists = s.getArtists().split(", ");
            for (String a : artists) makeArtistFiles(a, s, ranking.getCode());
        }
    }

    private static void makeArtistFiles(String artist, Song song, long code){
        artist = artist.replace("/\\","");
        boolean header = false;

        File directory = new File("library//"+artist);
        if (!directory.exists()){
            System.out.println(directory.getPath() + " created: " + directory.mkdirs());
        }

        File songFile = new File(directory.getAbsolutePath() + "//" + song.getId());
        if (!songFile.exists()){
            try {
                header = songFile.createNewFile();
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(songFile, true))){
            if (header) pw.println(song.getName());
            pw.println(song.getRank()+"--"+song.getPopularity()+"--"+code);
        } catch (FileNotFoundException e){
            System.err.println(songFile.getPath() + " doesn't exist!");
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static BigRanking loadPreviousRanking(){
        BigRanking ranking = new BigRanking();
        long code = readLastRankingCode()[0];
        if (code == 0) return ranking;

        try (BufferedReader br = new BufferedReader(new FileReader("ranking//" + code))){
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
            e.printStackTrace();
        } catch (NumberFormatException e){
            System.err.println("The rank column has an invalid format");
        } catch (ArrayIndexOutOfBoundsException e){
            System.err.println("The rows of Previous Ranking have an invalid format");
        }

        return ranking;
    }

    public static float[] getScores(String artist){
        artist = artist.replace("/\\","");
        float[] result = new float[5];

        File file = new File("library//" + artist);
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
                if (rank > 50) continue;
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
            e.printStackTrace();
        }
    }

    public static void addFailedRecommendation(Song song, Track track){
        File file = new File("failed_recommendations.txt");
        if (!file.exists()){
            try {
                System.out.println(file.getPath() + " created: " + file.createNewFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))){
            pw.print(dateFormat.format(new Date(System.currentTimeMillis())));
            pw.print(" -- " + song.getRank() + " -- " + song.getName() + " vs " + track.getName());
        } catch (FileNotFoundException e){
            System.err.println(file.getPath() + " doesn't exist!");
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    // -------------------------------- VERSION 2 --------------------------------------------
    public static void saveRanking(Ranking ranking, boolean replace){
        File directory = new File("ranking//");

        if (!directory.exists() && directory.mkdirs())
            System.out.println(directory.getAbsolutePath() + " has been just created");

        File file = new File(directory.getPath() + "//" + ranking.getCode());

        if (!replace & file.exists()) return;
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))){
            writer.println(dateFormat.format(new Date(System.currentTimeMillis())));
            for (Song s : ranking) writer.println(s.getRank() + "--" + s.getId());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static Ranking getLastSavedRanking(TopTerms term){
        File directory = new File("ranking//" + term.getDescription().toLowerCase());
        File[] rankings = directory.listFiles();

        if (rankings != null && rankings.length > 1) {
            File lastRanking = rankings[rankings.length-2];
            return getRanking(lastRanking);
        }

        return null;
    }

    public static Ranking getRanking(File file){
        Ranking ranking = new Ranking();

        try (BufferedReader br = new BufferedReader(new FileReader(file))){
            String line;

            while ((line = br.readLine()) != null){
                String[] params = line.split("--");
                Song song = new Song();
                song.setRank(Integer.parseInt(params[0]));
                song.setId(params[1]);
                song.setName(params[2]);
                song.setArtists(params[3]);
                song.setPopularity(Integer.parseInt(params[4]));
                ranking.add(song);
            }

        } catch (FileNotFoundException e){
            System.err.println(file.getAbsolutePath() + " not found");
        } catch (IOException e){
            e.printStackTrace();
        } catch (NumberFormatException e){
            System.out.println("Rank or popularity is not a number in " + file.getName());
        } catch (ArrayIndexOutOfBoundsException e){
            System.out.println("Not enough parameters in " + file.getName());
        }

        return ranking;
    }
}
