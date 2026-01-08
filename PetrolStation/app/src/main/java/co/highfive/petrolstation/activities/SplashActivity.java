package co.highfive.petrolstation.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.google.gson.reflect.TypeToken;

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

        updateUserSanadThenLoadSettings(userSanad);
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
}
