package cs10.apps.web.statsforspotify.utils;

import com.wrapper.spotify.model_objects.specification.Track;
import cs10.apps.web.statsforspotify.view.OptionPanes;

import java.io.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Maintenance {
    private static final String LOGS_FILE = "logs.txt";

    /**
     * @param apiUtils an authorized instance of spotify api utils
     * @return size of duplicated ids set
     */
    public static int removeRedundantFiles(ApiUtils apiUtils){
        Set<String> duplicates = findDuplicatedIds();
        removeRedundantFiles(apiUtils, duplicates);
        int sum = duplicates.size();

        if (sum > 0) {
            removeEmptyFoldersInLibrary();
            sum += removeRedundantFiles(apiUtils);
        }

        return sum;
    }

    public static void removeEmptyFoldersInLibrary(){
        File[] folders = new File("library").listFiles();
        if (folders != null) for (File artistFolder : folders){
            File[] files = artistFolder.listFiles();
            if (files != null && files.length == 0 && artistFolder.delete())
                System.out.println(artistFolder.getName() + " folder has been deleted");
        }
    }

    public static Set<String> findDuplicatedIds(){
        Set<String> duplicates = new HashSet<>();
        Set<String> ids = new HashSet<>();
        File[] folders = new File("library").listFiles();
        if (folders != null) for (File artistFolder : folders){
            File[] files = artistFolder.listFiles();
            if (files != null) for (File songFile : files){
                String songId = songFile.getName();
                if (ids.contains(songId)) duplicates.add(songId);
                else ids.add(songId);
            }
        }

        return duplicates;
    }

    private static void removeRedundantFiles(ApiUtils apiUtils, Set<String> ids){
        for (String songId : ids) try {
            Track t = apiUtils.getTrackByID(songId);
            System.out.println("Removing redundant files for " + CommonUtils.toString(t));
             if (t.getArtists().length == 1) throw new RuntimeException(CommonUtils.toString(t) + " is not a collaboration");
             for (int i=1; i<t.getArtists().length; i++) removeFile(t.getArtists()[i].getName(), t.getId());
        } catch (Exception e){
            System.err.println("Unable to analyze " + songId + ". Error: " + e.getMessage());
        }
    }

    public static void removeFile(String artistName, String songId){
        File file = new File("library\\" + artistName + "\\" + songId);
        if (!file.exists()) throw new RuntimeException(file.getAbsolutePath() + " doesn't exist!");
        else if (file.delete()) System.out.println(file + " deleted successfully");
    }

    public static void fixSongFiles(int sinceRankingCode){
        File[] folders = new File("library").listFiles();
        if (folders != null) for (File artistFolder : folders){
            File[] files = artistFolder.listFiles();
            if (files != null) for (File songFile : files){
                fixSongFile(songFile, sinceRankingCode);
            }
        }
    }

    private static void fixSongFile(File file, int sinceRankingCode){
        boolean fixed = false;
        StringBuilder sb = new StringBuilder();

        // READ
        try (BufferedReader br = new BufferedReader(new FileReader(file))){
            String line;

            // Skip header
            sb.append(br.readLine()).append('\n');

            while ((line = br.readLine()) != null){
                int codeRead = Integer.parseInt(line.split("--")[2]);
                if (codeRead != sinceRankingCode){
                    sb.append(line).append('\n');
                } else {
                    fixed = true;
                    break;
                }
            }

        } catch (IOException e){
            OptionPanes.showError("IOUtils - Fix Song File (Read)", e);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e){
            System.err.println(file.getAbsolutePath() + ": invalid format");
        }

        // WRITE
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))){
            bw.write(sb.toString());
        } catch (IOException e){
            OptionPanes.showError("IOUtils - Fix Song File (Write)", e);
        }

        if (fixed) System.out.println(file.getPath() + " has been fixed");
    }

    public static void writeErrorFile(Exception e, boolean detailed){
        try (FileWriter fw = new FileWriter(LOGS_FILE, true)){
            fw.write(new Date(System.currentTimeMillis()).toString());
            fw.write('\n');
            if (detailed) e.printStackTrace(new PrintWriter(fw));
            else if (e.getMessage() != null) fw.write(e.getMessage());
            fw.write("\n\n");
            System.err.println("Error " + e.getMessage() + " written in " + LOGS_FILE);
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    public static void log(String str){
        try (FileWriter fw = new FileWriter(LOGS_FILE, true)){
            fw.write(str);
            fw.write('\n');
            System.out.println("Log: " + str);
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    public static void clearPreviousReport(){
        File file = new File(LOGS_FILE);
        if (file.exists() && file.delete()){
            System.out.println("Previous Report cleared");
        }
    }
}
