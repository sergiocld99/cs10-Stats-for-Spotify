package cs10.apps.web.statsforspotify;

import cs10.apps.web.statsforspotify.utils.ApiUtils;
import cs10.apps.web.statsforspotify.utils.IOUtils;
import cs10.apps.web.statsforspotify.view.OptionPanes;
import cs10.apps.web.statsforspotify.view.StatsFrame;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Test {

    public static void main(String[] args) throws Exception {
        // Server
        ServerSocket serverSocket = new ServerSocket(8080);

        // Spotify Connection
        ApiUtils apiUtils = new ApiUtils();
        if (apiUtils.isReady()){

            // Ask and open browser
            if (IOUtils.isFirstTime()){
                int result = OptionPanes.askForPermission();
                if (result != 0) System.exit(2);
                apiUtils.openGrantPermissionPage();
            } else {
                OptionPanes.showPleaseLogin();
                apiUtils.openReconfirmPermissionPage();
            }

            // Await for code
            Socket socket = serverSocket.accept();

            // Read code (it's in the first line)
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = br.readLine();
            String code = line.split(" ")[1].replace("/?code=","");

            // Update
            apiUtils.refreshToken(code);
            OptionPanes.showCanCloseBrowser();
            socket.close();

            // Custom UI
            UIManager.put("nimbusOrange", Color.decode("#00c853"));
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // User Interface
            SwingUtilities.invokeLater(()->{
                try {
                    StatsFrame statsFrame = new StatsFrame(apiUtils);
                    statsFrame.init();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // Permission Re-update
            Runnable autoConfirm = () -> {
                try {
                    apiUtils.openReconfirmPermissionPage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };

            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleAtFixedRate(autoConfirm, 55, 55, TimeUnit.MINUTES);

            //new Thread(autoCloseable).start();
        } else {
            OptionPanes.showCredentialsError();
            System.exit(1);
        }
    }
}
