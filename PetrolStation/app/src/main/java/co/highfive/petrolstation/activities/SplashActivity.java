package co.highfive.petrolstation.activities;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


import co.highfive.petrolstation.R;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.Http.HttpRequest.RequestAsyncTask;
import co.highfive.petrolstation.hazemhamadaqa.Http.HttpResponse.AsyncResponse;
import co.highfive.petrolstation.hazemhamadaqa.Http.HttpResponse.model.ResponseObject;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.hazemhamadaqa.app.AppConfig;
import co.highfive.petrolstation.models.AppData;
import co.highfive.petrolstation.models.Setting;

public class SplashActivity extends BaseActivity {


    Handler handler;
    Runnable r;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

    }

    private void setUpViews() {
//        Glide.with(getApplicationContext()).load(R.drawable.splsh_bg).into(splash_bg);
//        Glide.with(getApplicationContext()).load(R.drawable.splsh_logo).into(logo);

        if(connectionAvailable){
            if(getSessionManager().getBoolean(getSessionKeys().isLogin)){
                updateUserSanad();
            }else{
                getData(true);
            }

        }else{
            moveToNext();
        }
    }

    public void getData(boolean isShowDialog){

        if(isShowDialog){
            showProgressHUD();
        }

        ArrayList<String> keys =  new ArrayList<>();
        ArrayList<String> values =  new ArrayList<>();

        ArrayList<String> headerKeys =  new ArrayList<>();
        ArrayList<String> headerValues =  new ArrayList<>();


        headerKeys.add("lang");
        headerValues.add(""+getSessionManager().getString(getSessionKeys().language_code));
        if(getSessionManager().getBoolean(getSessionKeys().isLogin)){
            headerKeys.add("Authorization");
            headerValues.add(""+getSessionManager().getString(getSessionKeys().token));

            AppData appData  = getGson().fromJson( getSessionManager().getString(getSessionKeys().app_data), AppData.class);

            if(appData != null && appData.getUser_sanad() >0){
                keys.add("user_sanad");
                values.add(""+appData.getUser_sanad() );
            }
        }
        try {

            new RequestAsyncTask(0, getApplicationContext(), AppConfig.getSetting, Constant.REQUEST_GET, keys, values, headerKeys, headerValues, new AsyncResponse() {
                @Override
                public void processFinish(ResponseObject responseObject) {
                    hideProgressHUD();
                    if(responseObject.getResponseCode() == 200){
                        try {
                            JSONObject jsonObject = new JSONObject(responseObject.getResponseText());
                            if(jsonObject.getBoolean("status")){

                                getSessionManager().setString(getSessionKeys().app_data,jsonObject.getString("data"));
                                moveToNext();

                            }else{
                                toast(jsonObject.getString("message"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            errorLogger("JSONException",""+e.getMessage());
                            toast(getString(R.string.general_error));
                        }catch (Exception e){
                            errorLogger("Exception",""+e.getMessage());
                            toast(getString(R.string.general_error));
                        }

                    }else if(responseObject.getResponseCode() == 401){
                        logout();
                    }else{
//                        errorLogger("Exception",""+e.getMessage());
                        toast(getString(R.string.general_error));
                    }
                }

                @Override
                public void processerror(String output) {
                    hideProgressHUD();
                    errorLogger("processerror",""+output);
                    if(output.equals("no_internet")){
                        toast(R.string.no_internet);
                    }else{
                        toast(getString(R.string.general_error));
                    }
                }
            }).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            hideProgressHUD();
            errorLogger("UnsupportedEncodin",""+e.getMessage());
            toast(getString(R.string.general_error));
        }catch (Exception e){
            hideProgressHUD();
            errorLogger("Exception",""+e.getMessage());
            toast(getString(R.string.general_error));
        }
    }

    private void moveToNext() {
        handler = new Handler();
        r = new Runnable() {
            public void run() {
                if(getSessionManager().getBoolean(getSessionKeys().isLogin)){
                    if(connectionAvailable){
                        getCompanySetting(true);
                    }else{
                        moveToActivity(SplashActivity.this,MainActivity.class,getIntent().getExtras(),true);
                    }
                }else{
                    moveToActivity(SplashActivity.this,SignInActivity.class,getIntent().getExtras(),true);
                }
            }
        };
        handler.postDelayed(r,1000);
    }

    public void getCompanySetting(boolean isShowDialog){

        if(isShowDialog){
            showProgressHUD();
        }

        ArrayList<String> keys =  new ArrayList<>();
        ArrayList<String> values =  new ArrayList<>();

        ArrayList<String> headerKeys =  new ArrayList<>();
        ArrayList<String> headerValues =  new ArrayList<>();

        keys.add("code");
        values.add("147153");

        AppData appData  = getGson().fromJson( getSessionManager().getString(getSessionKeys().app_data), AppData.class);

        if(appData != null && appData.getUser_sanad() >0){
            keys.add("user_sanad");
            values.add(""+appData.getUser_sanad() );
        }


        headerKeys.add("Authorization");
        headerValues.add(""+getSessionManager().getString(getSessionKeys().token));

        try {

            new RequestAsyncTask(0, getApplicationContext(), AppConfig.getCompanySetting, Constant.REQUEST_GET, keys, values, headerKeys, headerValues, new AsyncResponse() {
                @Override
                public void processFinish(ResponseObject responseObject) {
                    hideProgressHUD();
                    if(responseObject.getResponseCode() == 200){
                        try {
                            JSONObject jsonObject = new JSONObject(responseObject.getResponseText());
                            if(jsonObject.getBoolean("status")){

                                getSessionManager().setString(getSessionKeys().app_data,jsonObject.getJSONObject("data").toString());

                                Setting setting = getGson().fromJson(jsonObject.getJSONObject("data").getJSONObject("setting").toString(),Setting.class);

                                if(setting.getImage() != null){
                                    errorLogger("getImage","is not null");

                                    if(getSessionManager().getString(getSessionKeys().downloadImage)!= null && setting.getImage().contains(getSessionManager().getString(getSessionKeys().downloadImage))){
                                        errorLogger("getImage","already downloaded");
                                        moveToActivity(SplashActivity.this,MainActivity.class,getIntent().getExtras(),true);
                                    }else{ // download Image and then go to main activity
                                        try{
                                            String[] split = setting.getImage().split("/");
                                            errorLogger("split",""+split.length);
                                            if(split.length >0){
                                                String fileName = split[split.length -1];

                                                errorLogger("fileName",""+fileName);
                                                String[] split1 = fileName.split("\\.");
                                                errorLogger("split1",""+split1.length);
                                                errorLogger("split1[0]",""+split1[0]);

                                                downloadImageNew( fileName,setting.getImage(),split1[1]);
                                                getSessionManager().setString(getSessionKeys().downloadImage, fileName);
                                                moveToActivity(SplashActivity.this,MainActivity.class,getIntent().getExtras(),true);
                                            }else{
                                                moveToActivity(SplashActivity.this,MainActivity.class,getIntent().getExtras(),true);
                                            }
                                        }catch (Exception e){
                                            errorLogger("Exception",""+e.getMessage());
                                            moveToActivity(SplashActivity.this,MainActivity.class,getIntent().getExtras(),true);
                                        }
                                    }
                                }else{
                                    errorLogger("getImage","is null");
                                    moveToActivity(SplashActivity.this,MainActivity.class,getIntent().getExtras(),true);
                                }




                            }else{
                                toast(jsonObject.getString("message"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            errorLogger("JSONException",""+e.getMessage());
                            toast(getString(R.string.general_error));
                        }catch (Exception e){
                            errorLogger("Exception",""+e.getMessage());
                            toast(getString(R.string.general_error));
                        }

                    }else if(responseObject.getResponseCode() == 401){
                        logout();
                    }else{
//                        errorLogger("Exception",""+e.getMessage());
                        toast(getString(R.string.general_error));
                    }
                }

                @Override
                public void processerror(String output) {
                    hideProgressHUD();
                    errorLogger("processerror",""+output);
                    if(output.equals("no_internet")){
                        toast(R.string.no_internet);
                    }else{
                        toast(getString(R.string.general_error));
                    }
                }
            }).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            hideProgressHUD();
            errorLogger("UnsupportedEncodin",""+e.getMessage());
            toast(getString(R.string.general_error));
        }catch (Exception e){
            hideProgressHUD();
            errorLogger("Exception",""+e.getMessage());
            toast(getString(R.string.general_error));
        }
    }

    private void downloadImageNew(String filename, String downloadUrlOfImage,String type){
        errorLogger("downloadImageNew","downloadImageNew");
        errorLogger("filename",""+filename);
        errorLogger("type",""+type);

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(path, "logo.png");

        try{
            if(file.exists()){
                file.delete();
            }
        }catch (Exception e){

        }


        try{
            DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Uri downloadUri = Uri.parse(downloadUrlOfImage);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(filename)
                    .setMimeType("image/"+type) // Your file type. You can use this code to download other file types also.
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES,  "logo."+type);
            dm.enqueue(request);
            errorLogger("downloadImageNew","Image download started.");
        }catch (Exception e){
            errorLogger("downloadImageNew","Image download failed.");
        }
    }

    private void updateUserSanad() {

        try{
            AppData appData = getGson().fromJson(getSessionManager().getString(getSessionKeys().app_data), AppData.class);

            if(appData != null && appData.getUser_sanad() >0){
                ArrayList<String> keys = new ArrayList<>();
                ArrayList<String> values = new ArrayList<>();

                ArrayList<String> headerKeys = new ArrayList<>();
                ArrayList<String> headerValues = new ArrayList<>();


                headerKeys.add("Authorization");
                headerValues.add(getSessionManager().getString(getSessionKeys().token));

                keys.add("user_sanad");
                values.add(""+appData.getUser_sanad());

                keys.add("user_company_code");
                values.add(""+appData.getUser_company_code());


                try {
                    new RequestAsyncTask(0, getApplicationContext(), AppConfig.updateUserSanad,
                            Constant.REQUEST_POST, keys, values, headerKeys, headerValues, new AsyncResponse() {
                        @Override
                        public void processFinish(ResponseObject responseObject) {
                            try {
                                if (responseObject.getResponseCode() == 200) {
                                    getData(true);
                                } else if (responseObject.getResponseCode() == 401) {
                                    runOnUiThread(() -> logout());
                                }
                            } catch (Exception e) {
                                getData(true);
                            } finally {
                                getData(true);
                            }
                        }

                        @Override
                        public void processerror(String output) {
                            getData(true);
                        }
                    }).execute();
                } catch (UnsupportedEncodingException e) {
                    getData(true);

                }
                catch (Exception e) {
                    getData(true);
                }
            }else{
                getData(true);
            }

        }catch (Exception e){
            getData(true);
        }
    }

}