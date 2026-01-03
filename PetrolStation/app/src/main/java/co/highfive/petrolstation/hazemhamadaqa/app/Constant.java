package co.highfive.petrolstation.hazemhamadaqa.app;

import co.highfive.petrolstation.BuildConfig;

/**
 * Created by Eng. Hazem Hamadaqa on 7/7/2017.
 */

public interface Constant {
    boolean isDevelopment= BuildConfig.DEBUG;

    String RES_PREFIX = "android.resource://co.highfive.petrolstation/";

    boolean isCashing= false;
    int ALARM_ID = 1010;
    String ACTION_WEZARA_START = "ACTION_WEZARA_START";
    String ACTION_HAJ_START = "ACTION_HAJ_START";
    String ACTION_ZAKA_START = "ACTION_ZAKA_START";
    String ACTION_SOUND_START = "ACTION_SOUND_START";
    String ACTION_SOUND_STOP = "ACTION_SOUND_STOP";
    String ACTION_NEW_NOTIFICATION = "ACTION_NEW_NOTIFICATION";
    String REFRESH_NOTIFICATION = "REFRESH_NOTIFICATION";
    String countryDialCode = "+972"; //966
    String ACTION_Department_close_other = "ACTION_Department_close_other";
    String ACTION_SOUND_SELECR = "ACTION_SOUND_SELECR";
}
