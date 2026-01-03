package co.highfive.petrolstation.hazemhamadaqa.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.util.ArrayList;
import java.util.Arrays;

import co.highfive.petrolstation.activities.SplashActivity;
import co.highfive.petrolstation.hazemhamadaqa.app.AppConfig;
import co.highfive.petrolstation.hazemhamadaqa.app.Constant;
import co.highfive.petrolstation.hazemhamadaqa.helper.SessionKeys;
import co.highfive.petrolstation.hazemhamadaqa.helper.SessionManager;

/**
 * Created by Eng. Hazem Hamadaqa on 7/8/2017.
 */

public class BaseFragment extends Fragment implements Constant {
    private SessionManager sessionManager;
    private SessionKeys sessionKeys;
    GsonBuilder gsonBuilder;
    private Gson gson;



    public int spanCount=1;

    public String fontBold = "fonts/EncodeSansCondensed-Bold.ttf";
    public String fontMedium = "fonts/EncodeSansCondensed-Medium.ttf";

    private AppConfig appConfig;
    KProgressHUD progressHUD;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager= new SessionManager(getActivity().getApplicationContext());
        sessionKeys = new SessionKeys();

        gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
        appConfig = new AppConfig();

        progressHUD= KProgressHUD.create(getActivity())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);
    }

    public AppConfig getAppConfig() {
        return appConfig;
    }
    public static String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }
    public void setAppConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }
    public Spanned getHtmlText(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return  Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return  Html.fromHtml(text);
        }
    }

    public void showProgressHUD(){
        try{
            if(progressHUD !=null){
                progressHUD.show();
            }
        }catch (Exception e){

        }

    }

    public  boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            return !TextUtils.isEmpty(locationProviders);
        }
    }
    public void logout(){
//        getSessionManager().setBoolean(getSessionKeys().is_qr_scanned,false);
        getSessionManager().setBoolean(getSessionKeys().isLogin,false);
        getSessionManager().setString(getSessionKeys().userJson,null);
//        getSessionManager().setString(getSessionKeys().code,null);
        getSessionManager().setString(getSessionKeys().token,null);
        try{
            moveToActivity(getActivity(), SplashActivity.class, null,false, true);
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

    public void errorLogger(String tag,String message){
        if(isDevelopment){
            Log.e(tag,message);
        }
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public String capitalizeFirstLetter(String query){
        String retVal="";
        if(query.length() >0){
            retVal=query.substring(0, 1).toUpperCase() + query.substring(1);
        }
        return retVal;
    }

    public void freeMemory(){
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
    }

    public void toast(String message) {
        try{
            Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }catch (Exception e){

        }

    }

    public void toast(int message) {
        toast(getString(message));
    }

    public void openFragment(int container_body_res,boolean addToStack,BaseFragment selectedFragment){

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(container_body_res, selectedFragment);
        if(addToStack){
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();

    }

    public void hideSoftKeyboard() {
        if(getActivity().getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }


    public void moveToActivityWithResult(Context context, Class aClass, Bundle bundle, boolean isFinish,int requestCode){
        Intent intent= new Intent(context,aClass);
        if(bundle !=null){
            intent.putExtras(bundle);
        }
        startActivityForResult(intent,requestCode);
        if(isFinish){
            getActivity().finish();
        }
    }

    public void moveToActivity(Context context, Class aClass, Bundle bundle, boolean isFinish){
        Intent intent= new Intent(context,aClass);
        if(bundle !=null){
            intent.putExtras(bundle);
        }
        getActivity().startActivity(intent);
        if(isFinish){
            getActivity().finish();
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
            getActivity().finish();
        }
    }

    public ArrayList convertArrayToArrayList(Object []  objects){
        return new ArrayList<>(Arrays.asList(objects));
    }

    public Gson getGson() {
        return gson;
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    public  void closeFragment(){
        try {
            getActivity().getSupportFragmentManager().popBackStack();
        } catch (IllegalStateException ignored) {
            // There's no way to avoid getting this if saveInstanceState has already been called.
        }
    }

    public SessionKeys getSessionKeys() {
        return sessionKeys;
    }

    public void setSessionKeys(SessionKeys sessionKeys) {
        this.sessionKeys = sessionKeys;
    }
}
