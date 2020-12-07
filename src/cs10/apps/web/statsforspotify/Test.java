package cs10.apps.web.statsforspotify;

import cs10.apps.web.statsforspotify.app.PersonalChartApp;
import cs10.apps.web.statsforspotify.utils.Maintenance;

public class Test {
    private static final boolean APPLY_FIXES = false;

    public static void main(String[] args) {
        Maintenance.clearPreviousReport();

        if (APPLY_FIXES) Maintenance.fixSongFiles();
        else new PersonalChartApp().init();
    }
}
