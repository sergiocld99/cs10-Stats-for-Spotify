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

    public static void showPleaseLogin(){
        JOptionPane.showMessageDialog(null,
                "Welcome again! Please, log in to continue",
                Constants.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showCanCloseBrowser(){
        JOptionPane.showMessageDialog(null,
                "Authentication successful, you can close the browser :)",
                Constants.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showSavedSuccessfully(){
        JOptionPane.showMessageDialog(null,
                "The actual ranking was saved successfully",
                Constants.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showSaveError(){
        JOptionPane.showMessageDialog(null,
                "Unable to save file",
                Constants.APP_NAME, JOptionPane.WARNING_MESSAGE);
    }

    public static void showPlaybackStopped(){
        JOptionPane.showMessageDialog(null,
                "Playback Service has just stopped",
                Constants.APP_NAME, JOptionPane.WARNING_MESSAGE);
    }
}
