package co.highfive.petrolstation.hazemhamadaqa.app;

/**
 * Created by FOR Eng. Hazem Hamadaqa on 9/29/2016.
 */

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.StrictMode;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.google.firebase.FirebaseApp;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.Locale;

import co.highfive.petrolstation.hazemhamadaqa.helper.SessionManager;
import co.highfive.petrolstation.language.LocaleManager;
import co.highfive.petrolstation.language.Utility;
import co.highfive.petrolstation.utils.SunmiPrintHelper;


public class AppController extends MultiDexApplication {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    public static final String TAG = AppController.class.getSimpleName();
    private static AppController mInstance;
    SessionManager sessionManager;
//    private LocalizationApplicationDelegate localizationDelegate = new  LocalizationApplicationDelegate();
    public static LocaleManager localeManager;
    String realm_name= "palwakf_thedone.realm";

    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
        FirebaseApp.initializeApp(this);
        mInstance = this;
        sessionManager = new SessionManager(getApplicationContext());

        SunmiPrintHelper.getInstance().initSunmiPrinterService(this);
        try{
            Utility.bypassHiddenApiRestrictions();
        }catch (Exception e){
//            FirebaseCrashlytics.getInstance().recordException(e);
        }

        try{
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }catch (Exception e){

        }





        /**
         * just for cache Application's Context, and ':filedownloader' progress will NOT be launched
         * by below code, so please do not worry about performance.
         * @see FileDownloader#init(Context)
         */


    }


    public static synchronized AppController getInstance() {
        return mInstance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        localeManager = new LocaleManager(base);
        super.attachBaseContext(localeManager.setLocale(base));
        MultiDex.install(this);
        Log.d(TAG, "attachBaseContext");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        localeManager.setLocale(this);
        Log.e(TAG, "onConfigurationChanged: " + newConfig.locale.getLanguage());
    }

//    @Override
//    public Context getApplicationContext() {
//        return AppController.localeManager.setLocale(super.getApplicationContext());
//    }

    public void refreshLocale(Context context , Boolean force) {
        String language ;
        language = "ar";

        Locale locale ;
        if(language.equals("ar")){
            locale = new Locale("ar");
        }else{
            locale = Resources.getSystem().getConfiguration().locale;
        }

        updateLocale(context, locale);

        Context appContext = context.getApplicationContext();
        if (context != appContext) {
            updateLocale(appContext, locale);
        }
    }

    private void updateLocale(Context context, Locale locale) {
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        config.setLayoutDirection(config.locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}