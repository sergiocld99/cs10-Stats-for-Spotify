package cs10.apps.web.statsforspotify.utils;

import cs10.apps.desktop.statsforspotify.model.Ranking;
import cs10.apps.desktop.statsforspotify.model.Song;
import cs10.apps.desktop.statsforspotify.utils.OldIOUtils;
import cs10.apps.web.statsforspotify.model.TopTerms;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class IOUtils {
    private static final String CODES_FILE = "appcode";
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static void writeCode(String code){
        File file = new File(CODES_FILE);
        try {
            if (!file.exists() && file.createNewFile()){
                System.out.println(file.getAbsolutePath() + " has been just created");
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
              writer.println(code);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static String retrieveLastCode(){
        File file = new File(CODES_FILE);
        if (!file.exists()) return null;

        StringBuilder sb = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(file))){
            sb.append(br.readLine());
            return sb.toString();
        } catch (FileNotFoundException e){
            System.err.println(file.getAbsolutePath() + " not found");
        } catch (IOException e){
            e.printStackTrace();
        }

        return sb.toString();
    }

    public static boolean saveRanking(Ranking ranking, boolean replace){
        Calendar calendar = Calendar.getInstance();
        File directory = new File("ranking//" + ranking.getTitle().toLowerCase());
        if (!directory.exists() && directory.mkdirs()){
            System.out.println(directory.getAbsolutePath() + " has been just created");
        }

        File file = new File(directory.getAbsolutePath() + "//" +
                dateFormat.format(calendar.getTime()) + ".txt");

        String filepath = "logs//" + ranking.getTitle().toLowerCase() + "//";

        File dir1 = new File(filepath);
        if (!dir1.exists()) {
            if (dir1.mkdirs()){
                System.out.println("Directory created successfully");
            }
        }

        if (!replace & file.exists()) return false;
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))){
            for (Song s : ranking){
                writer.println(s.getRank() + "--" + s.getId() + "--" + s.getName() + "--" +
                        s.getArtists() + "--" + s.getPopularity());
                OldIOUtils.appendArtistLog(s, filepath);
            }
            return true;
        } catch (IOException e){
            e.printStackTrace();
            return false;
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
