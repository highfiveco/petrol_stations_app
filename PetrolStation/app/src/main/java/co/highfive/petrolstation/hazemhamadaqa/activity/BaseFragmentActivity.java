package co.highfive.petrolstation.hazemhamadaqa.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import co.highfive.petrolstation.hazemhamadaqa.Http.HttpRequest.RequestAsyncTask;
import co.highfive.petrolstation.hazemhamadaqa.Http.HttpResponse.AsyncResponse;
import co.highfive.petrolstation.hazemhamadaqa.Http.HttpResponse.model.ResponseObject;
import co.highfive.petrolstation.hazemhamadaqa.app.AppConfig;
import co.highfive.petrolstation.hazemhamadaqa.app.AppController;
import co.highfive.petrolstation.hazemhamadaqa.app.Constant;
import co.highfive.petrolstation.hazemhamadaqa.fragment.BaseFragment;
import co.highfive.petrolstation.hazemhamadaqa.helper.SessionKeys;
import co.highfive.petrolstation.hazemhamadaqa.helper.SessionManager;
import co.highfive.petrolstation.listener.CheckInternetListener;

public class BaseFragmentActivity extends FragmentActivity implements Constant {

    private SessionManager sessionManager;
    GsonBuilder gsonBuilder;
    private Gson gson;
    Toolbar toolbar;
    private SessionKeys sessionKeys;

    public static BaseFragmentActivity baseActivity;

    KProgressHUD progressHUD;

    public String fontBold = "fonts/EncodeSansCondensed-Bold.ttf";
    public String fontMedium = "fonts/EncodeSansCondensed-Medium.ttf";
    public int spanCount=1;

    public AppConfig appConfig;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionKeys = new SessionKeys();
        sessionManager =  new SessionManager(getApplicationContext());
//        if(getSessionManager().getString(getSessionKeys().language_code) != null){
//            Locale locale = new Locale(getSessionManager().getString(getSessionKeys().language_code));
//            Locale.setDefault(locale);
//            Configuration config = new Configuration();
//            config.locale = locale;
//            getBaseContext().getResources().updateConfiguration(config,
//                    getBaseContext().getResources().getDisplayMetrics());
//        }

        baseActivity=this;


        appConfig = new AppConfig();
        gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();

        progressHUD= KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(AppController.localeManager.setLocale(newBase));
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



    public void setUpToolBar(String title_txt, boolean isBack,boolean isHaveFilter,boolean isHaveSearch){
//        TextView title=(TextView) findViewById(R.id.title);
//        TextView back=(TextView) findViewById(R.id.back);
//        TextView filter=(TextView) findViewById(R.id.filter);
//        TextView search=(TextView) findViewById(R.id.search);
//        title.setText(title_txt);
//
//        if(isBack){
//            back.setVisibility(View.VISIBLE);
//            back.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    finish();
//                }
//            });
//        }else{
//            back.setVisibility(View.GONE);
//        }
//
//        if(isHaveFilter){
//            filter.setVisibility(View.VISIBLE);
//        }else{
//            filter.setVisibility(View.GONE);
//        }
//
//        if(isHaveSearch){
//            search.setVisibility(View.VISIBLE);
//        }else{
//            search.setVisibility(View.GONE);
//        }
//
//        filter.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                moveToActivityWithResult(baseActivity, FilterActivity.class,null,false,AppConfig.filter);
//            }
//        });
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

    public void errorLogger(String tag,String message){
        if(isDevelopment)
            Log.e(tag,message);
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
        return gson;
    }

    public void toast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
    }

    public ArrayList convertArrayToArrayList(Object []  objects){
        return new ArrayList<>(Arrays.asList(objects));
    }


    public void moveToActivity(Context context, Class aClass, Bundle bundle, boolean isFinish){
        Intent intent= new Intent(context,aClass);
        if(bundle !=null){
            intent.putExtras(bundle);
        }
        startActivity(intent);
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

    public void openFragment(int container_body_res,boolean addToStack,BaseFragment selectedFragment){

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
        if(progressHUD !=null){
            progressHUD.show();
        }
    }

    public void hideProgressHUD(){
        if(progressHUD !=null && progressHUD.isShowing()){
            progressHUD.dismiss();
        }
    }

    public  String round(float x){
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

    public SessionKeys getSessionKeys() {
        return sessionKeys;
    }

    public void setSessionKeys(SessionKeys sessionKeys) {
        this.sessionKeys = sessionKeys;
    }
}
