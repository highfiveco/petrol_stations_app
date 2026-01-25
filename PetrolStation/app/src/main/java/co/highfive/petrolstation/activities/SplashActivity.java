package co.highfive.petrolstation.activities;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Map;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.models.AppData;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;
import co.highfive.petrolstation.settings.dto.CompanySettingData;
import co.highfive.petrolstation.settings.dto.GetSettingData;

public class SplashActivity extends BaseActivity {

    private AppCompatImageView splash_bg;
    private AppCompatImageView logo;

    private ApiClient apiClient;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splash_bg = findViewById(R.id.splash_bg);
        logo = findViewById(R.id.logo);

        initApiClient();
        setUpViews();
        startFlow();
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
                () -> runOnUiThread(this::logout)
        );
    }

    private void setUpViews() {
        Glide.with(getApplicationContext()).load(R.drawable.splsh_bg).into(splash_bg);
        Glide.with(getApplicationContext()).load(R.drawable.splsh_logo).into(logo);
    }

    private void startFlow() {

        // لو مافي نت: بس قرر وين تروح (حسب سياستكم)
        if (!connectionAvailable) {
            goNext();
            return;
        }

        // لو مش مسجل دخول: مباشرة للوجن
        if (!getSessionManager().getBoolean(getSessionKeys().isLogin)) {
            goToLogin();
            return;
        }

        // مسجل دخول: كمل Calls
        int userSanad = getUserSanadFromSession();
        errorLogger("userSanad",""+userSanad);
//        if (userSanad <= 0) {
//            // ما عنا user_sanad مخزن صح
//            goToLogin();
//            return;
//        }

        if(connectionAvailable){
            updateUserSanadThenLoadSettings(userSanad);
        }else{
            goNext();
        }

    }


    private void updateUserSanadThenLoadSettings(int userSanad) {

        showProgressHUD();

        Map<String, String> params = ApiClient.mapOf(
                "user_sanad", String.valueOf(userSanad)
        );

        Type type = new TypeToken<BaseResponse<Object>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_POST,
                Endpoints.UPDATEUSERSANAD,
                params,
                null,
                type,
                0,
                new ApiCallback<Object>() {
                    @Override public void onSuccess(Object data, String message, String rawJson) {
                        // حتى لو نجاح، كمل
                        loadGetSetting(userSanad);
                    }

                    @Override public void onError(co.highfive.petrolstation.network.ApiError error) {
                        // ما نوقف السبلاش بسببها
                        loadGetSetting(userSanad);
                    }

                    @Override public void onUnauthorized(String rawJson) {
                        hideProgressHUD();
                        logout();
                    }

                    @Override public void onNetworkError(String reason) {
                        hideProgressHUD();
                        goNext();
                    }

                    @Override public void onParseError(String rawJson, Exception e) {
                        loadGetSetting(userSanad);
                    }
                }
        );
    }

    private void loadGetSetting(int userSanad) {

        Map<String, String> params = ApiClient.mapOf(
                "user_sanad", String.valueOf(userSanad)
        );

        Type type = new TypeToken<BaseResponse<GetSettingData>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.GET_SETTING,
                params,
                null,
                type,
                0,
                new ApiCallback<GetSettingData>() {
                    @Override public void onSuccess(GetSettingData data, String message, String rawJson) {
                        // إذا بدك تخزّن rawJson أو data — هون مكانها
                        loadCompanySetting(userSanad);
                    }

                    @Override public void onError(co.highfive.petrolstation.network.ApiError error) {
                        // كمل حسب سياستكم
                        loadCompanySetting(userSanad);
                    }

                    @Override public void onUnauthorized(String rawJson) {
                        hideProgressHUD();
                        logout();
                    }

                    @Override public void onNetworkError(String reason) {
                        hideProgressHUD();
                        goNext();
                    }

                    @Override public void onParseError(String rawJson, Exception e) {
                        loadCompanySetting(userSanad);
                    }
                }
        );
    }

    private void loadCompanySetting(int userSanad) {

        // code (اختياري) حسب السيرفر — موجود في الـ Postman بس disabled
        String code = getCompanyCodeFromSession();

        Map<String, String> params = (code != null && !code.trim().isEmpty())
                ? ApiClient.mapOf("user_sanad", String.valueOf(userSanad), "code", code)
                : ApiClient.mapOf("user_sanad", String.valueOf(userSanad));

        Type type = new TypeToken<BaseResponse<CompanySettingData>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.GET_COMPANY_SETTING,
                params,
                null,
                type,
                0,
                new ApiCallback<CompanySettingData>() {
                    @Override public void onSuccess(CompanySettingData data, String message, String rawJson) {
                        hideProgressHUD();

                        // مهم: خزّن data كـ app_data بصيغة JSON حتى يكمل الكود القديم
                        if (data != null) {
                            String dataJson = getGson().toJson(data);
                            getSessionManager().setString(getSessionKeys().app_data, dataJson);


                            if(data.setting.image != null){
                                errorLogger("getImage","is not null");

                                if(getSessionManager().getString(getSessionKeys().downloadImage)!= null && data.setting.image.contains(getSessionManager().getString(getSessionKeys().downloadImage))){
                                    errorLogger("getImage","already downloaded");
//                                    moveToActivity(SplashActivity.this,MainActivity.class,getIntent().getExtras(),true);
                                }else{ // download Image and then go to main activity
                                    try{
                                        String[] split = data.setting.image.split("/");
                                        String fileName = split[split.length - 1];
                                        String[] parts = fileName.split("\\.");
                                        String ext = (parts.length > 1) ? parts[parts.length - 1] : "png";

                                        downloadCompanyLogo(data.setting.image, ext);
                                    }catch (Exception e){
                                        errorLogger("Exception",""+e.getMessage());
//                                        moveToActivity(SplashActivity.this,MainActivity.class,getIntent().getExtras(),true);
                                    }
                                }
                            }else{
                                errorLogger("getImage","is null");
                                moveToActivity(SplashActivity.this,MainActivity.class,getIntent().getExtras(),true);
                            }


                        }

                        goNext();
                    }

                    @Override public void onError(co.highfive.petrolstation.network.ApiError error) {
                        hideProgressHUD();
                        goNext();
                    }

                    @Override public void onUnauthorized(String rawJson) {
                        hideProgressHUD();
                        logout();
                    }

                    @Override public void onNetworkError(String reason) {
                        hideProgressHUD();
                        goNext();
                    }

                    @Override public void onParseError(String rawJson, Exception e) {
                        hideProgressHUD();
                        goNext();
                    }
                }
        );
    }

    private void goNext() {
        handler.postDelayed(() -> {
            if (getSessionManager().getBoolean(getSessionKeys().isLogin)) {
                moveToActivity(SplashActivity.this, MainActivity.class, null, true);
            } else {
                goToLogin();
            }
        }, 700);
    }

    private void goToLogin() {
        handler.postDelayed(() ->
                        moveToActivity(SplashActivity.this, SignInActivity.class, null, true)
                , 400);
    }

    private File getCompanyLogoFile(String ext) {
        if (ext == null || ext.trim().isEmpty()) ext = "png";
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (dir != null && !dir.exists()) dir.mkdirs();
        return new File(dir, "company_logo." + ext.toLowerCase());
    }

    private void downloadCompanyLogo(String downloadUrl, String ext) {
        try {
            if (downloadUrl == null || downloadUrl.trim().isEmpty()) return;

            if (ext == null || ext.trim().isEmpty()) ext = "png";
            ext = ext.toLowerCase();

            DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Uri downloadUri = Uri.parse(downloadUrl);

            String fileName = "company_logo." + ext;

            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle("company_logo")
                    .setMimeType("image/" + ext)
                    // ✅ لا تستخدم HIDDEN على أجهزة كثيرة
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    // ✅ داخل مجلد التطبيق (لا يحتاج Permission)
                    .setDestinationInExternalFilesDir(this, Environment.DIRECTORY_PICTURES, fileName);

            long id = dm.enqueue(request);

            // خزّن المسار الكامل للقراءة لاحقًا
            File out = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
            getSessionManager().setString(getSessionKeys().downloadImage, out.getAbsolutePath());

            errorLogger("downloadCompanyLogo", "started id=" + id + " -> " + out.getAbsolutePath());
        } catch (Exception e) {
            errorLogger("downloadCompanyLogo", "failed: " + (e.getMessage() == null ? "null" : e.getMessage()));
        }
    }


}
