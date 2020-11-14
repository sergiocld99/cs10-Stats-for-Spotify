package cs10.apps.web.statsforspotify.utils;

import java.io.*;

public class IOUtils {
    private static final String CODES_FILE = "codes.txt";

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
            System.err.println("Unable to read file " + file.getAbsolutePath());
        } catch (IOException e){
            e.printStackTrace();
        }

        return sb.toString();
    }
}
