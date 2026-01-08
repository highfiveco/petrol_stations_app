package co.highfive.petrolstation.hazemhamadaqa.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.LocaleList;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import co.highfive.petrolstation.BuildConfig;
import co.highfive.petrolstation.R;
import co.highfive.petrolstation.activities.CustomerFinancialAccountActivity;
import co.highfive.petrolstation.activities.ImageViewerActivity;
import co.highfive.petrolstation.activities.MainActivity;
import co.highfive.petrolstation.activities.SplashActivity;
import co.highfive.petrolstation.fragments.UpdateAppDialog;
import co.highfive.petrolstation.hazemhamadaqa.Http.HttpRequest.RequestAsyncTask;
import co.highfive.petrolstation.hazemhamadaqa.Http.HttpResponse.AsyncResponse;
import co.highfive.petrolstation.hazemhamadaqa.Http.HttpResponse.model.ResponseObject;
import co.highfive.petrolstation.hazemhamadaqa.app.AppConfig;
import co.highfive.petrolstation.hazemhamadaqa.app.AppController;
import co.highfive.petrolstation.hazemhamadaqa.app.Constant;
import co.highfive.petrolstation.hazemhamadaqa.fragment.BaseFragment;
import co.highfive.petrolstation.hazemhamadaqa.helper.SessionKeys;
import co.highfive.petrolstation.hazemhamadaqa.helper.SessionManager;
import co.highfive.petrolstation.hazemhamadaqa.util.NetworkUtil;
import co.highfive.petrolstation.helper.CheckInternetConnection;
import co.highfive.petrolstation.helper.ConnectionChangeListener;
import co.highfive.petrolstation.listener.CheckInternetListener;
import co.highfive.petrolstation.listener.SuccessListener;
import co.highfive.petrolstation.listener.UploadListener;
import co.highfive.petrolstation.models.AppData;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.utils.BluetoothUtil;
import co.highfive.petrolstation.utils.ESCUtil;
import co.highfive.petrolstation.utils.SunmiPrintHelper;


/**
 * Created by Eng. Hazem Hamadaqa on 3/27/2017.
 */

public class BaseActivity extends AppCompatActivity implements Constant {

    private SessionManager sessionManager;
    private SessionKeys sessionKeys;
    GsonBuilder gsonBuilder;
    private Gson gson;
    Toolbar toolbar;

    public static BaseActivity baseActivity;

    KProgressHUD progressHUD;
    public int spanCount=1;

    public AppConfig appConfig;

    public boolean connectionAvailable = true;
    CheckInternetConnection checkInternetConnection;
    protected ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        sessionManager =  new SessionManager(getApplicationContext());
        sessionKeys = new SessionKeys();
        
        baseActivity=this;
        initApiClient();
        appConfig = new AppConfig();
        setBadge(getApplicationContext(),0);
        gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();

        progressHUD= KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);

        if(NetworkUtil.isConnected(this)){
            /// Connection is Available
            connectionAvailable = true;
            errorLogger("connectionAvailable","true");
        }else{
            /// Connection is not Available
            connectionAvailable = false;
            errorLogger("connectionAvailable","false");
            openNoInternet();
        }

        initPrinterStyle();

    }
    private void initApiClient() {
        apiClient = new ApiClient(
                getApplicationContext(),
                getGson(),
                new ApiClient.HeaderProvider() {
                    @Override public String getToken() {
                        return getSessionManager().getString(getSessionKeys().token);
                    }
                    @Override public String getLang() {
                        String lang = getSessionManager().getString(getSessionKeys().language_code);
                        return (lang == null || lang.trim().isEmpty()) ? "ar" : lang;
                    }
                    @Override public boolean isLoggedIn() {
                        return getSessionManager().getBoolean(getSessionKeys().isLogin);
                    }
                },
                () -> runOnUiThread(BaseActivity.this::logout)
        );
    }

    private void initPrinterStyle() {
        try{
            if(BluetoothUtil.isBlueToothPrinter){
                BluetoothUtil.sendData(ESCUtil.init_printer());
            }else{
                SunmiPrintHelper.getInstance().initPrinter();
            }
        }catch (Exception e){
            errorLogger("initPrinterStyle",""+e.getMessage());
        }
    }
    public static String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }
    @Override
    protected void onStart() {
        super.onStart();
        checkInternetConnection = new CheckInternetConnection(getApplicationContext());
        checkInternetConnection.addConnectionChangeListener(new ConnectionChangeListener() {
            @Override
            public void onConnectionChanged(boolean isConnectionAvailable) {
                if(connectionAvailable && !isConnectionAvailable) {
//                    Toast.makeText(StoreBuildingActivity.this, "Internet connection unavailable!", Toast.LENGTH_SHORT).show();
                    connectionAvailable = false;
                    openNoInternet();
                }
                else if(!connectionAvailable && isConnectionAvailable) {
//                    Toast.makeText(StoreBuildingActivity.this, "Internet connection is back again.", Toast.LENGTH_SHORT).show();
                    connectionAvailable = true;
                }
            }
        });
    }

    private void openNoInternet() {
//        moveToActivity(this, NoInternetActivity.class,null,false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        checkInternetConnection.removeConnectionChangeListener();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(AppController.localeManager.setLocale(base));
        Log.d("attachBaseContext", "attachBaseContext");
    }

    public void backHome() {
        moveToActivity(BaseActivity.this, MainActivity.class,null,false,true);
    }



    @Override
    public Resources getResources() {
        return  getResources(super.getResources());
    }

//    @Suppress("DEPRECATION")
    public Resources  getResources(Resources resources){
        sessionManager = new SessionManager(getApplicationContext());
        sessionKeys = new SessionKeys();
        if(getSessionManager().getString(getSessionKeys().language_code) == null){
            String language_code = Locale.getDefault().getLanguage();
            if(language_code.equals("ar")){
                getSessionManager().setString(getSessionKeys().language_code,"ar");
            }else{
                getSessionManager().setString(getSessionKeys().language_code,"en");
            }
        }


        try{
            Locale locale;
            try{

                if(getSessionManager() != null && getSessionKeys() != null && getSessionManager().getString(getSessionKeys().language_code) != null){
                    locale = new Locale(getSessionManager().getString(getSessionKeys().language_code));
                }else{
                    locale = new Locale("ar");
                }

                Locale.setDefault(locale);
            }catch (Exception  e){
                locale = new Locale("ar");
            }

            if(resources != null){
                Configuration config = resources.getConfiguration();
                if(config != null){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                        config.setLocale(locale);

                        Set<Locale> set = new LinkedHashSet<>();
                        // bring the target locale to the front of the list
                        set.add(locale);

                        LocaleList all = LocaleList.getDefault();
                        for (int i = 0; i < all.size(); i++) {
                            // append other locales supported by the user
                            set.add(all.get(i));
                        }

                        Locale[] locales = set.toArray(new Locale[0]);
                        config.setLocales(new LocaleList(locales));

                        return resources;
                    } else {
                        config.locale = locale;
                        DisplayMetrics metrics = resources.getDisplayMetrics();
                        return new Resources(this.getAssets(), metrics, config);
                    }
                }else{
                    return resources;
                }
            }
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        try{
            DisplayMetrics metrics = resources.getDisplayMetrics();
            return new Resources(this.getAssets(), metrics, null);
        }catch (Exception e){
            return resources;
        }

    }



    public void setUpToolBar(int title_txt, boolean isBack,boolean isMenu){
        if(title_txt != -1){
            setUpToolBar(getString(title_txt),isBack,isMenu);
        }else {
            setUpToolBar(null,isBack,isMenu);
        }

    }

    public void setUpToolBar(String title_txt, boolean isBack,boolean isMenu){

//        LinearLayout ic_back=(LinearLayout) findViewById(R.id.ic_back);
//        AppCompatTextView title=(AppCompatTextView) findViewById(R.id.title);
//        LinearLayout hamburgar_layout=(LinearLayout) findViewById(R.id.hamburgar_layout);
//
//        if(isMenu){
//            hamburgar_layout.setVisibility(View.VISIBLE);
//
//        }else{
//            hamburgar_layout.setVisibility(View.GONE);
//        }
//
//        if(isBack){
//            ic_back.setVisibility(View.VISIBLE);
//            ic_back.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    finish();
//                }
//            });
//        }else{
//            ic_back.setVisibility(View.GONE);
//        }
//
//        if(title_txt != null){
//            title.setText(title_txt);
//        }else{
//            title.setVisibility(View.GONE);
//        }

    }

    public void errorLogger(String tag, String message) {
        if (isDevelopment) {
            int maxLogSize = 4000; // الحد الأقصى لحجم السجل في أندرويد
            for (int i = 0; i <= message.length() / maxLogSize; i++) {
                int start = i * maxLogSize;
                int end = (i + 1) * maxLogSize;
                end = end > message.length() ? message.length() : end;
                Log.e(tag + " [Part " + (i + 1) + "]", message.substring(start, end));
            }
        }
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void showSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.showSoftInput(view, 0);
    }

    public Gson getGson() {

        if(gson == null){
            gsonBuilder = new GsonBuilder();
            gson = gsonBuilder.create();
        }

        return gson;
    }

    public void toast(String message){
        try{
            Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
        }catch (Exception e){

        }
    }

    public void toast(int message){
        try{
            Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
        }catch (Exception e){

        }

    }

    public ArrayList convertArrayToArrayList(Object []  objects){
        try{
            return new ArrayList<>(Arrays.asList(objects));
        }catch (Exception e){
//            FirebaseCrashlytics.getInstance().recordException(e);

            return  new ArrayList();
        }
    }


    public void moveToActivity(Context context,Class aClass,Bundle bundle,boolean isFinish){
        Intent intent= new Intent(context,aClass);
        if(bundle !=null){
            intent.putExtras(bundle);
        }
        startActivity(intent);
        if(isFinish){
            finish();
        }
    }

    public void moveToActivity(Context context,Class aClass,Bundle bundle,boolean isFinish,boolean clearStack){
        Intent intent= new Intent(context,aClass);
        if(bundle !=null){
            intent.putExtras(bundle);
        }
        if(clearStack){
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        startActivity(intent);
        if(isFinish){
            finish();
        }
    }

    public void moveToActivityWithResult(Context context, Class aClass, Bundle bundle, boolean isFinish,int requestCode){
        Intent intent= new Intent(context,aClass);
        if(bundle !=null){
            intent.putExtras(bundle);
        }
        startActivityForResult(intent,requestCode);
        if(isFinish){
            finish();
        }
    }

    public AppConfig getAppConfig() {
        return appConfig;
    }

    public void setAppConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }


    public void openFragmentOrBackToIt(int container_body_res,boolean addToStack,BaseFragment selectedFragment){
        String backStateName = selectedFragment.getClass().getName();

        FragmentManager fragmentManager = getSupportFragmentManager();
        boolean fragmentPopped = fragmentManager.popBackStackImmediate (backStateName, 0);

        if (!fragmentPopped) { //fragment not in back stack, create it.
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(container_body_res, selectedFragment);
            if(addToStack){
                fragmentTransaction.addToBackStack(null);
            }
            fragmentTransaction.commit();
        }


    }

    public void openFragment(int container_body_res, boolean addToStack, BaseFragment selectedFragment){

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(container_body_res, selectedFragment);
        if(addToStack){
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();

    }

    public static void closeFragment(){
        try {
        baseActivity.getSupportFragmentManager().popBackStack();
        } catch (IllegalStateException ignored) {
            FirebaseCrashlytics.getInstance().recordException(ignored);

            // There's no way to avoid getting this if saveInstanceState has already been called.
        }
    }

    public void openFragmentWithAnimations(int container_body_res, boolean addToStack, BaseFragment selectedFragment, int enter_res, int exit_res){

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(enter_res,exit_res);
        fragmentTransaction.replace(container_body_res, selectedFragment);
        if(addToStack){
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();

    }

    public void showProgressHUD(){
        try{
            if(progressHUD !=null){
                progressHUD.show();
            }
        }catch (Exception e){

        }

    }

    public void hideProgressHUD(){
        try{
            if(progressHUD !=null && progressHUD.isShowing()){
                progressHUD.dismiss();
            }
        }catch (Exception e){

        }

    }

    public  String round(float x){
        return ""+Math.round(x*100.0)/100.0;
    }

    public  String round(double x){
        return ""+Math.round(x*100.0)/100.0;
    }

    public String capitalizeFirstLetter(String query){
        String retVal="";
        if(query.length() >0){
            retVal=query.substring(0, 1).toUpperCase() + query.substring(1);
        }
        return retVal;
    }

    public void freeMemory(){
        try{
            System.runFinalization();
            Runtime.getRuntime().gc();
            System.gc();
        }catch (Exception e){

        }

    }

    public void checkInternetStatus(final CheckInternetListener checkInternetListener){
        try {
            new RequestAsyncTask(1000, getApplicationContext(), AppConfig.testUrl, co.highfive.petrolstation.hazemhamadaqa.Http.Constant.REQUEST_GET, null, null, null, null, new AsyncResponse() {
                @Override
                public void processFinish(ResponseObject responseObject) {
                    if(responseObject.getResponseCode()== 200){
                        checkInternetListener.done(true);
                    }else{
                        checkInternetListener.done(false);
                    }
                }

                @Override
                public void processerror(String output) {
                    checkInternetListener.done(false);
                }
            }).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            checkInternetListener.done(false);
        }catch (Exception e ){
            checkInternetListener.done(false);
        }
    }

    public void setupUI(View view) {

        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard();
                    return false;
                }

            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupUI(innerView);
            }
        }
    }
    public Date parseDateTime(String dateTime){

        try{
            SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

            Date date = from.parse(dateTime);
            return date;
        }catch (Exception e){
            return  null;
        }
    }

    public String formatDateTime(String dateTime){
        String ret_date = "";

        try{
            SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

            Date date = from.parse(dateTime);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd   hh:mm a",Locale.ENGLISH);
            ret_date = sdf.format(date);

            ret_date = ret_date.replace("PM",getString(R.string.pm));
            ret_date = ret_date.replace("AM",getString(R.string.am));

        }catch (Exception e){

        }


        return  ret_date;
    }

    public String formatDate(String dateTime){
        String ret_date = "";

        try{
            SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

            Date date = from.parse(dateTime);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH);
            ret_date = sdf.format(date);
        }catch (Exception e){

        }


        return  ret_date;
    }



    public String formatTime(String dateTime,String format){
        String ret_date = "";
        if(format == null){
            format = "yyyy-MM-dd HH:mm:ss";
        }
        try{
            SimpleDateFormat from = new SimpleDateFormat(format, Locale.ENGLISH);

            Date date = from.parse(dateTime);

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a",Locale.ENGLISH);


            ret_date = sdf.format(date);

            ret_date = ret_date.replace("PM",getString(R.string.pm));
            ret_date = ret_date.replace("AM",getString(R.string.am));

        }catch (Exception e){

        }
        return  ret_date;
    }

    public String formatDateTime(String dateTime,String fromDateFormat,String toDateFormat){
        String ret_date = "";

        if(fromDateFormat == null){
            fromDateFormat = "yyyy-MM-dd HH:mm:ss";
        }

        if(toDateFormat == null){
            toDateFormat = "yyyy-MM-dd hh:mm a";
        }

        try{
            SimpleDateFormat from = new SimpleDateFormat(fromDateFormat, Locale.ENGLISH);
            Date date = from.parse(dateTime);

            SimpleDateFormat sdf = new SimpleDateFormat(toDateFormat,Locale.ENGLISH);
            ret_date = sdf.format(date);

        }catch (Exception e){

        }

        return  ret_date;
    }

    public  String convertTimeZones(final String fromTimeZoneString,
                                    final String toTimeZoneString, final String fromDateTime,String returnFormat) {

        errorLogger("fromTimeZoneString",fromTimeZoneString);
        errorLogger("toTimeZoneString",toTimeZoneString);


        final DateTimeZone fromTimeZone = DateTimeZone.forID(fromTimeZoneString);
        final DateTimeZone toTimeZone = DateTimeZone.forID(toTimeZoneString);
        final DateTime dateTime = new DateTime(fromDateTime, fromTimeZone);

        if(returnFormat == null){
            returnFormat = "yyyy-MM-dd H:mm:ss";
        }
        final DateTimeFormatter outputFormatter
                = DateTimeFormat.forPattern(returnFormat).withZone(toTimeZone);
        return outputFormatter.print(dateTime);
    }



    public  String convertTimeZones(final String fromTimeZoneString,
                                          final String toTimeZoneString, final String fromDateTime) {

        try{
            errorLogger("fromTimeZoneString",fromTimeZoneString);
            errorLogger("toTimeZoneString",toTimeZoneString);


            final DateTimeZone fromTimeZone = DateTimeZone.forID(fromTimeZoneString);
            final DateTimeZone toTimeZone = DateTimeZone.forID(toTimeZoneString);
            final DateTime dateTime = new DateTime(fromDateTime, fromTimeZone);

            final DateTimeFormatter outputFormatter
                    = DateTimeFormat.forPattern("yyyy-MM-dd H:mm:ss").withZone(toTimeZone);

            return outputFormatter.print(dateTime);
        }catch (Exception e){
            return null;
        }

    }

    public String convertDateFromLocalToZone(String dateTimeFormat,String toTimeZone, String dateStr){
        String formattedDate = null;

        if(dateTimeFormat == null){
            dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
        }

        errorLogger("getDefault",""+TimeZone.getDefault().getID());
        errorLogger("getDefault",""+TimeZone.getDefault().getDisplayName(Locale.ENGLISH));
        try {
            SimpleDateFormat df = new SimpleDateFormat(dateTimeFormat, Locale.ENGLISH);
            df.setTimeZone(TimeZone.getDefault());
//            df.setTimeZone(TimeZone.getTimeZone(fromTimeZone));

            Date date = df.parse(dateStr);
            df.setTimeZone(TimeZone.getTimeZone(toTimeZone));
            formattedDate = df.format(date);
        }catch (Exception e){

        }
        return formattedDate;
    }

    public String convertDateFromZoneToLocal(String fromTimeZone, String dateStr){
        String formattedDate = null;
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            df.setTimeZone(TimeZone.getTimeZone(fromTimeZone));

//            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = df.parse(dateStr);
            df.setTimeZone(TimeZone.getDefault());
            formattedDate = df.format(date);
        }catch (Exception e){

        }
        return formattedDate;
    }

    public String convertDateFromToZone(String fromTimeZone,String toTimeZone, String dateStr){
        String formattedDate = null;
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            df.setTimeZone(TimeZone.getTimeZone(fromTimeZone));

//            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = df.parse(dateStr);
            df.setTimeZone(TimeZone.getTimeZone(toTimeZone));
            formattedDate = df.format(date);
        }catch (Exception e){

        }
        return formattedDate;
    }


    public Spanned getHtmlText(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return  Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return  Html.fromHtml(text);
        }
    }

    public static void setBadge(Context context, int count) {
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", count);
        intent.putExtra("badge_count_package_name", context.getPackageName());
        intent.putExtra("badge_count_class_name", launcherClassName);
        context.sendBroadcast(intent);
    }

    public static String getLauncherClassName(Context context) {

        PackageManager pm = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(context.getPackageName())) {
                String className = resolveInfo.activityInfo.name;
                return className;
            }
        }
        return null;
    }

    public String convertTimeMs(int millis){
        // New date object from millis
        Date date = new Date(millis);
        SimpleDateFormat formatter= new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formatted = formatter.format(date);
        return  formatted;
    }

    public void logout(){
        getSessionManager().setBoolean(getSessionKeys().isLogin,false);
        getSessionManager().setString(getSessionKeys().userJson,null);
        getSessionManager().setString(getSessionKeys().token,null);
        moveToActivity(this, SplashActivity.class, null,false, true);
    }

    public SessionKeys getSessionKeys() {
        return sessionKeys;
    }

    public void setSessionKeys(SessionKeys sessionKeys) {
        this.sessionKeys = sessionKeys;
    }

    public  boolean isImageFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("image");
    }

    public  boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }

    public void uploadFile(String fileName, ArrayList<File> files , ArrayList<String> keys, ArrayList<String> values, ArrayList<String> headerKeys, ArrayList<String> headerValues, UploadListener uploadListener){

//        ArrayList<File> files = new ArrayList<>();
//
//        files.add(file);

        try {
            new RequestAsyncTask(false, getApplicationContext(), AppConfig.testUrl,fileName,files, keys, values, headerKeys, headerValues, new AsyncResponse() {
                @Override
                public void processFinish(ResponseObject responseObject) {
                    hideProgressHUD();
                    if(responseObject.getResponseCode() == 200){
                        try {
                            JSONObject jsonObject = new JSONObject(responseObject.getResponseText());
                            if(jsonObject.getBoolean("status")){

                                uploadListener.success(jsonObject.getString("data"));
                            }else{
                                uploadListener.fail(jsonObject.getString("message"));
//                                toast(jsonObject.getString("message"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            errorLogger("Exception",""+e.getMessage());
                            uploadListener.fail(getString(R.string.general_error));
                        }catch (Exception e){
                            errorLogger("Exception",""+e.getMessage());
                            uploadListener.fail(getString(R.string.general_error));
                        }
                    }else{
                        hideProgressHUD();
                        errorLogger("getResponseCode",""+responseObject.getResponseCode());
                        uploadListener.fail(getString(R.string.general_error));
                    }
                }

                @Override
                public void processerror(String output) {
                    hideProgressHUD();
                    errorLogger("processerror","processerror:"+output);
                    uploadListener.fail(getString(R.string.general_error));
                }
            }).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            hideProgressHUD();
            errorLogger("UnsupportedEnco",""+e.getMessage());
            uploadListener.fail(getString(R.string.general_error));
        }catch (Exception e){
            hideProgressHUD();
            errorLogger("Exception",""+e.getMessage());
            uploadListener.fail(getString(R.string.general_error));
        }
    }


    public void call(String mobile) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);

            intent.setData(Uri.parse("tel:" + mobile));
            startActivity(intent);
        }catch (Exception e){
            errorLogger("Exception",""+e.getMessage());
        }

    }

    public void openURl(String url){
        try{
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }catch (Exception e){

        }

    }

    public int coverPixelToDP (int dps) {
        try{
            final float scale = this.getResources().getDisplayMetrics().density;
            return (int) (dps * scale);
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
            return dps ;
        }

    }

    public void shareString(String shared_txt, String shared_SUBJECT){
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, ""+shared_SUBJECT);
            String shareMessage= "\n"+shared_txt+"\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, ""+getString(R.string.choose)));
        } catch(Exception e) {
            //e.toString();
        }
    }

    public void shareApp(String shared_txt){
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, ""+getString(R.string.app_name_ar));
            String shareMessage= "\n"+shared_txt+"\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, ""+getString(R.string.choose)));
        } catch(Exception e) {
            //e.toString();
        }
    }

    public long getTimeDiffrence(String adantime,String currentTime){
        if(adantime != null){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
            try {
                String[] adantime_split = adantime.split(":");
                if(adantime_split.length  == 2){
                    adantime += ":00";
                }

                String[] currentTime_split = currentTime.split(":");
                if(currentTime_split.length  == 2){
                    currentTime += ":00";
                }

                Date date1 = simpleDateFormat.parse(currentTime);
                Date date2 = simpleDateFormat.parse(adantime);

                Log.e("date1",""+date1.toString());
                Log.e("date2",""+date2.toString());
                long difference = date2.getTime() - date1.getTime();
                return difference;
            } catch (ParseException e) {
                e.printStackTrace();
                Log.e("ParseException22222",""+e.getMessage());
                FirebaseCrashlytics.getInstance().recordException(e);
                return 0;
            }catch (Exception e){
                return 0;
            }
        }else{
            return 0;
        }
    }

    public boolean isTowDateEqual(Calendar date1 , Calendar date2 ){

        if( date1.get(Calendar.DAY_OF_MONTH) ==  date2.get(Calendar.DAY_OF_MONTH) &&  date1.get(Calendar.MONTH) ==  date2.get(Calendar.MONTH) &&  date1.get(Calendar.YEAR) ==  date2.get(Calendar.YEAR)){
            return true;
        }

        return false;
    }


    public float getDistance(double lat1,double lon1,double lat2,double lon2){
        Location loc1 = new Location("");
        loc1.setLatitude(lat1);
        loc1.setLongitude(lon1);

        Location loc2 = new Location("");
        loc2.setLatitude(lat2);
        loc2.setLongitude(lon2);

        return loc1.distanceTo(loc2);
    }

    public  void copyFile(File src, File dst) throws IOException {
        FileInputStream var2 = new FileInputStream(src);
        FileOutputStream var3 = new FileOutputStream(dst);
        byte[] var4 = new byte[1024];

        int var5;
        while((var5 = var2.read(var4)) > 0) {
            var3.write(var4, 0, var5);
        }

        var2.close();
        var3.close();
    }

    public boolean createDirIfNotExist(String path) {
        boolean ret = true;

        baseActivity.errorLogger("getAbsolutePath",""+Environment.getDownloadCacheDirectory().getAbsolutePath()+path);
        baseActivity.errorLogger("getFilesDir",""+getFilesDir().getAbsolutePath());

        File file = new File(Environment.getDownloadCacheDirectory().getAbsolutePath()+path);

        if (!file.exists()) {
            try{
                file.mkdirs();
            }catch (Exception e){

            }
            if (!file.mkdirs()) {
                Log.e("createDirIfNotExist :: ", "Problem creating folder");
                ret = false;
            }
        }else{
            baseActivity.errorLogger("file",""+file.getAbsolutePath());
        }
        return ret;
    }
    public  int getDaysDifference(Date fromDate,Date toDate)
    {
        if(fromDate==null||toDate==null)
            return 0;

        return (int)( (toDate.getTime() - fromDate.getTime()) / (1000 * 60 * 60 * 24));
    }

    public static String getDeviceId(Context context) {

        String deviceId;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            deviceId = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        } else {
            final TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (mTelephony.getDeviceId() != null) {
                try{
                    deviceId = mTelephony.getDeviceId();
                }catch (Exception e){
                    deviceId = Settings.Secure.getString(
                            context.getContentResolver(),
                            Settings.Secure.ANDROID_ID);
                }

            } else {
                deviceId = Settings.Secure.getString(
                        context.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
            }
        }

        return deviceId;
    }
    public void playBeepTone() {
        try {
            ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void playSunmiPrinterBeep() {
        try {
            // الطريقة الرسمية لطابعات Sunmi
            Class<?> clazz = Class.forName("com.sunmi.peripheral.printer.SunmiPrinterService");
            Method method = clazz.getMethod("sound");
            method.invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
            // استخدام بديل إذا فشلت الطريقة الرسمية
            playBeepTone();
        }
    }

    public void vibrate() {
        try {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null && v.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    v.vibrate(400);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openUpdateAppDialog(String  update_title, String update_description,String url){
        UpdateAppDialog updateAppDialog = new UpdateAppDialog(this,update_title,update_description);
        updateAppDialog.setCancelable(false);
        updateAppDialog.setSuccessListener(new SuccessListener() {
            @Override
            public void success(boolean success) {
                openURl(url);
            }
        });
        updateAppDialog.show(baseActivity.getSupportFragmentManager(),"OpenReadingDialog");
    }
    public void openImage(BaseActivity baseActivity,String image_path,View view) {

        try{
            Intent intent = new Intent(baseActivity, ImageViewerActivity.class);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(baseActivity, view, "image");

            intent.putExtra("image_path",image_path);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                startActivity(intent, options.toBundle());
            }else{
                startActivity(intent);
            }
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
        }

    }

    public void setClipboard( String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("", text);
        clipboard.setPrimaryClip(clip);
    }

    public String formatJson(String text){

        StringBuilder json = new StringBuilder();
        String indentString = "";

        for (int i = 0; i < text.length(); i++) {
            char letter = text.charAt(i);
            switch (letter) {
                case '{':
                case '[':
                    json.append("\n" + indentString + letter + "\n");
                    indentString = indentString + "\t";
                    json.append(indentString);
                    break;
                case '}':
                case ']':
                    indentString = indentString.replaceFirst("\t", "");
                    json.append("\n" + indentString + letter);
                    break;
                case ',':
                    json.append(letter + "\n" + indentString);
                    break;

                default:
                    json.append(letter);
                    break;
            }
        }

        return json.toString();
    }

    public float calcDiff(String previous_readings_val,String current_reading_val){
        float diff  = 0 ;
        float current_reading  = 0 ;
        float previous_readings  = 0 ;
        if(!previous_readings_val.trim().isEmpty() && !current_reading_val.trim().isEmpty()) {
            try{
                current_reading = Float.parseFloat(current_reading_val.trim());
            } catch (Exception e) {

            }
            try{
                previous_readings = Float.parseFloat(previous_readings_val.trim());
            } catch (Exception e) {

            }
            diff  =current_reading  - previous_readings ;
        }

        return diff;

    }


    public double calculateTotalPrice(double currentValue,
                                      double lastValue,
                                      int checkCurrentRead,
                                      double kiloPrice,
                                      double diff,
                                      int minDay,
                                      String subscriptionDateStr,  // التاريخ كـ "yyyy-MM-dd"
                                      double minAmount,
                                      int minimumWithdrawalZero) {

        double totalPrice = 0;

        // الحالات التي يكون فيها السعر صفرًا
        if ((currentValue < lastValue && checkCurrentRead == 1)
                || Double.isNaN(currentValue)
                || (currentValue - lastValue == 0 && minimumWithdrawalZero == 0)) {
            return 0;
        }

        // الحساب الأساسي
        totalPrice = diff * kiloPrice;

        errorLogger("totalPrice",""+totalPrice);
        errorLogger("minDay",""+minDay);
        errorLogger("minAmount",""+minAmount);
        errorLogger("subscriptionDate",""+subscriptionDateStr);

        // إذا كان minDay != 0، نحتاج إلى مقارنة الفرق في الأيام
        if (minDay != 0) {
            try {

                if(subscriptionDateStr == null){
                    errorLogger("test","test1");
                    if (totalPrice < minAmount) {
                        errorLogger("test","test2");
                        // إذا لم يتجاوز الحد الأدنى وكان السعر أقل من minAmount، نستخدم minAmount
                        totalPrice = minAmount;
                    }else{
                        errorLogger("test","test3");
                    }
                }else{
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date subscriptionDate = sdf.parse(subscriptionDateStr);
                    Date today = new Date(); // تاريخ اليوم

                    errorLogger("subscriptionDate",""+subscriptionDate.toString());
                    // حساب الفرق بين اليوم وتاريخ الاشتراك بالأيام
                    long daysBetween = calculateDaysBetween(subscriptionDate, today);


                    errorLogger("today",""+today.toString());

                    errorLogger("daysBetween","daysBetween:"+daysBetween);


                    if (daysBetween >= minDay) {
                        // إذا تجاوز الحد الأدنى للأيام، نستخدم السعر المحسوب
                        totalPrice = diff * kiloPrice;
                    } else if (totalPrice < minAmount) {
                        // إذا لم يتجاوز الحد الأدنى وكان السعر أقل من minAmount، نستخدم minAmount
                        totalPrice = minAmount;
                    }
                }

            } catch (ParseException e) {
                e.printStackTrace();
                errorLogger("ParseException",""+e.getMessage());
                return 0; // أو يمكنك إرجاع minAmount كقيمة افتراضية
            }
        } else if (totalPrice < minAmount) {
            // إذا كان minDay = 0 ولكن السعر أقل من minAmount
            totalPrice = minAmount;
        }

        errorLogger("final",""+totalPrice);
        return Double.parseDouble(round(totalPrice));
    }

    private long calculateDaysBetween(Date startDate, Date endDate) {
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        endCal.set(Calendar.HOUR_OF_DAY, 0);
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.SECOND, 0);
        endCal.set(Calendar.MILLISECOND, 0);

        long millisecondsDiff = endCal.getTimeInMillis() - startCal.getTimeInMillis();
        return millisecondsDiff / (24 * 60 * 60 * 1000); // التحويل من مللي ثانية إلى أيام
    }
    public AppData getAppData(){
        return  getGson().fromJson(getSessionManager().getString(getSessionKeys().app_data), AppData.class);
    }

    public int getUserSanadFromSession() {
        try {
            String raw = getSessionManager().getString(getSessionKeys().app_data);
            if (raw == null || raw.trim().isEmpty()) return 0;

            AppData appData = getGson().fromJson(raw, AppData.class);
            return appData != null ? appData.getUser_sanad() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public String getCompanyCodeFromSession() {
        try {
            String raw = getSessionManager().getString(getSessionKeys().app_data);
            if (raw == null || raw.trim().isEmpty()) return null;

            AppData appData = getGson().fromJson(raw, AppData.class);
            return appData != null ? appData.getUser_company_code() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static void setVisible(View v, boolean visible) {
        if (v == null) return;
        v.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public static int safeInt(Integer v) {
        return v == null ? 0 : v;
    }

    public static String safe(String s) {
        return s == null ? "" : s;
    }
    public double safeDouble(Object o) {
        try {
            if (o == null) return 0.0;
            if (o instanceof Number) return ((Number) o).doubleValue();
            return Double.parseDouble(String.valueOf(o));
        } catch (Exception e) {
            return 0.0;
        }
    }
}

