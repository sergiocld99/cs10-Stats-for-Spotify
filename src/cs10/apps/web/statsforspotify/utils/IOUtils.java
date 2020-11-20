package cs10.apps.web.statsforspotify.utils;

import cs10.apps.desktop.statsforspotify.model.Ranking;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.web.statsforspotify.model.Artist;
import cs10.apps.web.statsforspotify.model.BigRanking;

import javax.swing.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class IOUtils {
    private static final String DATA_FILE = "appdata.ini";
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static boolean isFirstTime(){
        return ! new File(DATA_FILE).exists();
    }

    /**
     *
     * @return an array with compare [0] and last [1] codes
     */
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

    public static void saveLastRankingCode(long compareCode, long lastCode){
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
            pw.println("last="+lastCode);
        } catch (FileNotFoundException e){
            System.err.println("The file " + DATA_FILE + " doesn't exist!");
        } catch (IOException e){
            e.printStackTrace();
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
        float[] result = new float[10];

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

    public static int getTimesOnRanking(String artists, String id){
        String artist = artists.split(", ")[0].replace("/\\","");
        File file = new File("library//"+artist+"//"+id);
        int cant = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))){
            // skip header
            br.readLine();

            // read info
            while (br.readLine() != null) cant++;
        } catch (FileNotFoundException e) {
            System.err.println(file.getPath() + " doesn't exist!");
        } catch (IOException e){
            e.printStackTrace();
        }

        return cant;
    }

    public static int getRankingsAmount(){
        File[] files = new File("ranking").listFiles();
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
        File[] folders = new File("library").listFiles();
        if (folders == null) return null;

        Artist[] result = new Artist[folders.length];
        for (int i=0; i<result.length; i++){
            result[i] = new Artist();
            result[i].setName(folders[i].getName());
            result[i].setScore(getArtistScore(folders[i].getName()));
        }

        return result;
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

}
