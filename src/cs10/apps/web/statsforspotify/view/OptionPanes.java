package cs10.apps.web.statsforspotify.view;

import cs10.apps.web.statsforspotify.app.Constants;

import javax.swing.*;

public class OptionPanes {

    public static void showCredentialsError(){
        JOptionPane.showMessageDialog(null,
                "Unable to get credentials", Constants.APP_NAME, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * @return -1 if the user closed the window,
     * 0 if the user pressed "Yes",
     * 1 if the user pressed "No"
     */
    public static int askForPermission(){
        return JOptionPane.showConfirmDialog(null,
                "Hello there! Do you want to grant Spotify permissions to this app?",
                Constants.APP_NAME, JOptionPane.YES_NO_OPTION);
    }

    /**
     * @return -1 if the user closed the window,
     * 0 if the user pressed the button
     */
    public static int confirmPermissionGrant(){
        return JOptionPane.showConfirmDialog(null,
                "Please, press the following button when you grant the permission",
                Constants.APP_NAME, JOptionPane.DEFAULT_OPTION);
    }
}
