package cs10.apps.web.statsforspotify;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import cs10.apps.web.statsforspotify.utils.ApiUtils;
import cs10.apps.web.statsforspotify.view.OptionPanes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Test {
    private static final String LIAM_PAYNE_ID = "5pUo3fmmHT8bhCyHE52hA6";

    public static void main(String[] args) throws Exception {
        // Server
        ServerSocket serverSocket = new ServerSocket(8080);
        //serverSocket.accept();

        // Spotify Connection
        ApiUtils apiUtils = new ApiUtils();
        if (apiUtils.isReady()){
            int result;

            result = OptionPanes.askForPermission();
            if (result != 0) System.exit(2);

            apiUtils.openGrantPermissionPage();
            Socket socket = serverSocket.accept();

            // Read code (it's in the first line)
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = br.readLine();
            String code = line.split(" ")[1].replace("/?code=","");
            apiUtils.refreshToken(code);
            System.out.println(apiUtils.getUser());

        } else {
            OptionPanes.showCredentialsError();
            System.exit(1);
        }
    }
}
