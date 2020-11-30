package cs10.apps.web.statsforspotify.utils;

import cs10.apps.web.statsforspotify.view.OptionPanes;

import java.io.*;
import java.util.Date;

public class Maintenance {
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

    public static void writeErrorFile(Exception e){
        File file = new File("logs.txt");
        try (PrintWriter pw = new PrintWriter(file)){
            pw.println(new Date(System.currentTimeMillis()).toString());
            e.printStackTrace(pw);
            System.err.println("Error written in " + file.getPath());
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    public static void clearPreviousReport(){
        File file = new File("logs.txt");
        if (file.exists() && file.delete()){
            System.out.println("Previous Report cleared");
        }
    }
}
