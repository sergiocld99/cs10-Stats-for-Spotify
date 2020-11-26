package cs10.apps.web.statsforspotify.view;

import cs10.apps.web.statsforspotify.app.PersonalChartApp;

import javax.swing.*;

public class OptionPanes {

    public static void showCredentialsError(){
        JOptionPane.showMessageDialog(null,
                "Unable to get credentials", PersonalChartApp.APP_NAME, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * @return -1 if the user closed the window,
     * 0 if the user pressed "Yes",
     * 1 if the user pressed "No"
     */
    public static int askForPermission(){
        return JOptionPane.showConfirmDialog(null,
                "Hello there! Do you want to grant Spotify permissions to this app?",
                PersonalChartApp.APP_NAME, JOptionPane.YES_NO_OPTION);
    }

    public static void showPleaseLogin(){
        JOptionPane.showMessageDialog(null,
                "Welcome again! Please, log in to continue",
                PersonalChartApp.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showCanCloseBrowser(){
        JOptionPane.showMessageDialog(null,
                "Authentication successful, you can close the browser :)",
                PersonalChartApp.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showSavedSuccessfully(){
        JOptionPane.showMessageDialog(null,
                "The actual ranking was saved successfully",
                PersonalChartApp.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showSaveError(){
        JOptionPane.showMessageDialog(null,
                "Unable to save file",
                PersonalChartApp.APP_NAME, JOptionPane.WARNING_MESSAGE);
    }

    public static void showPlaybackStopped(){
        JOptionPane.showMessageDialog(null,
                "Playback Service has just stopped",
                PersonalChartApp.APP_NAME, JOptionPane.WARNING_MESSAGE);
    }

    public static void showPlaybackUpdated(){
        JOptionPane.showMessageDialog(null,
                "Playback Service has been updated",
                PersonalChartApp.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(String origin, Exception e){
        JOptionPane.showMessageDialog(null,
                "Exception caught in " + origin + ": " + e.getMessage(),
                PersonalChartApp.APP_NAME, JOptionPane.ERROR_MESSAGE);
    }

    public static void message(String str){
        JOptionPane.showMessageDialog(null, str,
                PersonalChartApp.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
    }
}
