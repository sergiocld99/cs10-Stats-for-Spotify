package cs10.apps.web.statsforspotify.utils;

import com.wrapper.spotify.model_objects.specification.Track;
import cs10.apps.desktop.statsforspotify.model.Ranking;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.web.statsforspotify.app.AppOptions;
import cs10.apps.web.statsforspotify.model.Artist;
import cs10.apps.web.statsforspotify.model.BigRanking;
import cs10.apps.web.statsforspotify.model.SimpleRanking;

import javax.swing.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class IOUtils {
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final String APP_DATA_FILE = "appdata.bin";
    private static final String LIBRARY_FOLDER = "library";
    private static final String RANKING_FOLDER = "ranking";

    public static boolean isFirstTime(){
        return ! new File(RANKING_FOLDER).exists();
    }

    // ------------------------------ RANKING CODES ----------------------------------

    /**
     *
     * @return an array with compare [0] and last [1] codes
     */
    public static long[] getSavedRankingCodes(String userId){
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

    public static void updateRankingCodes(long compareCode, long lastCode, String userId){
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

    // --------------------------------- LIBRARY -------------------------------------

    public static void updateLibrary(Ranking ranking, JProgressBar progressBar){
        int total = ranking.size(), i = 0;

        for (Song s : ranking){
            progressBar.setValue((i++) * 100 / total);
            String[] artists = s.getArtists().split(", ");
            for (String a : artists) updateSongFile(a, s, ranking.getCode());
        }
    }

    // ----------------------------------- SONG FILE ----------------------------------

    private static void updateSongFile(String artist, Song song, long code){
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

    public static int getTimesOnRanking(String artists, String id){
        String artist = artists.split(", ")[0].replace("/\\","");
        File file = new File(LIBRARY_FOLDER+"//"+artist+"//"+id);
        int cant = 0;

        if (file.exists()) try (BufferedReader br = new BufferedReader(new FileReader(file))){
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

    // ----------------------------- RANKING --------------------------------------

    public static int getRankingsAmount(){
        File[] files = new File(RANKING_FOLDER).listFiles();
        if (files == null || files.length == 0) return 1;
        else return files.length;
    }

    private static String getRankingDate(String code){
        String filename = RANKING_FOLDER+"//"+ code;
        String date = null;

        try (BufferedReader br = new BufferedReader(new FileReader(filename))){
            date = br.readLine();
        } catch (IOException e){
            Maintenance.writeErrorFile(e, false);
        }

        return date;
    }

    public static BigRanking getRanking(String code, boolean detailed) {
        String filename = RANKING_FOLDER+"//"+ code;
        BigRanking ranking = new BigRanking();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))){
            // header
            ranking.setDate(br.readLine());
            ranking.setCode(Long.parseLong(code));

            // read ranking
            String line;
            while ((line = br.readLine()) != null){
                String[] params = line.split("--");
                Song song = new Song();
                song.setRank(Integer.parseInt(params[0]));
                song.setId(params[1]);
                if (detailed) retrieveData(params[1], code, song);
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

    public static BigRanking getLastRanking(String userId){
        BigRanking ranking = new BigRanking();
        long code = getSavedRankingCodes(userId)[0];
        if (code == 0) return ranking;
        else return getRanking(String.valueOf(code), false);
    }

    public static SimpleRanking[] getAvailableRankings(){
        File[] rankFiles = new File(RANKING_FOLDER).listFiles();
        if (rankFiles != null){
            SimpleRanking[] result = new SimpleRanking[rankFiles.length];
            for (int i=0; i<rankFiles.length; i++) {
                result[i] = new SimpleRanking(rankFiles[i].getName());
                result[i].setDate(getRankingDate(rankFiles[i].getName()));
            }

            return result;
        }

        return new SimpleRanking[0];
    }

    public static void save(Ranking ranking, boolean replace){
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

    // ------------------------------- SEARCH ---------------------------------

    public static void retrieveData(String trackId, String rankingCode, Song song){
        File[] artistFolders = new File(LIBRARY_FOLDER).listFiles();

        if (artistFolders != null) for (File folder : artistFolders){
            File[] tracksF = folder.listFiles();
            if (tracksF != null) for (File t : tracksF){
                if (t.getName().equals(trackId)){
                    song.setId(t.getName());
                    song.setArtists(folder.getName());
                    retrieveData(t, rankingCode, song);
                    return;
                }
            }
        }
    }

    // -------------------------------- ARTIST ------------------------------------

    public static boolean existsArtist(String name){
        return new File(LIBRARY_FOLDER+"//"+name).exists();
    }

    public static float[] getDetailedArtistScores(String artist){
        artist = artist.replace("/\\","");
        float[] result = new float[10];

        File file = new File(LIBRARY_FOLDER+"//"+artist);
        File[] songsFiles = file.listFiles();
        if (songsFiles != null) {
            for (File f : songsFiles)
                getSongScore(f, result);
        }

        return result;
    }

    public static float getArtistScore(String artist){
        float[] scores = IOUtils.getDetailedArtistScores(artist);
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

    // ------------------------------- READ SONG FILE --------------------------------

    public static void retrieveData(File songFile, String rankingCode, Song song){

        try (BufferedReader br = new BufferedReader(new FileReader(songFile))){
            song.setName(br.readLine());
            String line;

            while ((line = br.readLine()) != null){
                String[] params = line.split("--");
                if (params[2].equals(rankingCode))
                    song.setPopularity(Integer.parseInt(params[1]));
            }

        } catch (IOException e){
            Maintenance.writeErrorFile(e, false);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e){
            System.err.println("Invalid format: " + songFile.getPath());
        }
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

    private static void getSongScore(File file, float[] scores){
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

    // --------------------------------- RANDOM ----------------------------------

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

    // ------------------------------------- APP OPTIONS ----------------------------------

    public static AppOptions loadAppOptions(){
        AppOptions appOptions = new AppOptions();

        File file = new File(APP_DATA_FILE);
        if (!file.exists()) try {
            if (file.createNewFile()) System.out.println("Created: " + APP_DATA_FILE);
            return appOptions;
        } catch (IOException e){
            Maintenance.writeErrorFile(e, false);
            return appOptions;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            appOptions = (AppOptions) ois.readObject();
        } catch (IOException | ClassNotFoundException e){
            Maintenance.writeErrorFile(e, true);
        }

        return appOptions;
    }

    public static void save(AppOptions options){
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(APP_DATA_FILE)
        )){
            oos.writeObject(options);
        } catch (IOException e){
            Maintenance.writeErrorFile(e, false);
        }
    }

}
