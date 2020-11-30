package cs10.apps.web.statsforspotify.utils;

import cs10.apps.web.statsforspotify.view.OptionPanes;

import java.io.*;
import java.util.Date;

public class Maintenance {
    private static final String LOGS_FILE = "logs.txt";

    public static void fixSongFiles(){
        File[] folders = new File("library").listFiles();
        if (folders != null) for (File artistFolder : folders){
            File[] files = artistFolder.listFiles();
            if (files != null) for (File songFile : files){
                fixSongFile(songFile);
            }
        }
    }

    private static void fixSongFile(File file){
        int lastCodeRead = 0;
        boolean fixed = false;
        StringBuilder sb = new StringBuilder();

        // READ
        try (BufferedReader br = new BufferedReader(new FileReader(file))){
            String line;

            // Skip header
            sb.append(br.readLine()).append('\n');

            while ((line = br.readLine()) != null){
                int codeRead = Integer.parseInt(line.split("--")[2]);
                if (codeRead != lastCodeRead){
                    lastCodeRead = codeRead;
                    sb.append(line).append('\n');
                } else fixed = true;
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
            else fw.write(e.getMessage());
            fw.write("\n\n");
            System.err.println("Error " + e.getMessage() + " written in " + LOGS_FILE);
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    public static void log(String str){
        try (FileWriter fw = new FileWriter(LOGS_FILE, true)){
            fw.write(str);
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
