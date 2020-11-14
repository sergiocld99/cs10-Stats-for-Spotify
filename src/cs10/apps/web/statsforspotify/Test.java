package cs10.apps.web.statsforspotify;

import cs10.apps.web.statsforspotify.utils.ApiUtils;
import cs10.apps.web.statsforspotify.view.OptionPanes;
import cs10.apps.web.statsforspotify.view.StatsFrame;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Test {
    private static final String LIAM_PAYNE_ID = "5pUo3fmmHT8bhCyHE52hA6";

    public static void main(String[] args) throws Exception {
        // Server
        ServerSocket serverSocket = new ServerSocket(8080);

        // Spotify Connection
        ApiUtils apiUtils = new ApiUtils();
        if (apiUtils.isReady()){
            int result = OptionPanes.askForPermission();
            if (result != 0) System.exit(2);

            // Open browser
            apiUtils.openGrantPermissionPage();

            // Await for code
            Socket socket = serverSocket.accept();

            // Read code (it's in the first line)
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = br.readLine();
            String code = line.split(" ")[1].replace("/?code=","");

            // Update
            apiUtils.refreshToken(code);

            // Request something
            StatsFrame statsFrame = new StatsFrame(apiUtils);
            statsFrame.init();
        } else {
            OptionPanes.showCredentialsError();
            System.exit(1);
        }
    }
}
