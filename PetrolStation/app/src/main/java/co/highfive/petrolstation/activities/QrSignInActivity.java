package co.highfive.petrolstation.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.lang.reflect.Type;
import java.util.Map;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.models.AppData;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.ApiError;
import co.highfive.petrolstation.network.ApiParams;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;
import co.highfive.petrolstation.auth.dto.LoginData;
import com.google.gson.reflect.TypeToken;

public class QrSignInActivity extends BaseActivity {

    private static final int PERMISSION_REQUEST_CODE = 200;
    private ApiClient apiClient;
    private String fcmToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_sign_in);
        setupUI(findViewById(R.id.main_layout));

        initApiClient();
        setUpViews();

        findViewById(R.id.ic_back).setOnClickListener(v -> finish());
        findViewById(R.id.scan_qr_txt).setOnClickListener(v ->
                moveToActivityWithResult(this, ScanQrCodeActivity.class, getIntent().getExtras(), false, 10)
        );
    }

    private void initApiClient() {
        apiClient = new ApiClient(
                getApplicationContext(),
                getGson(),
                new ApiClient.HeaderProvider() {
                    @Override
                    public String getToken() {
                        return getSessionManager().getString(getSessionKeys().token);
                    }

                    @Override
                    public String getLang() {
                        String lang = getSessionManager().getString(getSessionKeys().language_code);
                        return (lang == null || lang.trim().isEmpty()) ? "ar" : lang;
                    }

                    @Override
                    public boolean isLoggedIn() {
                        return getSessionManager().getBoolean(getSessionKeys().isLogin);
                    }
                },
                () -> runOnUiThread(QrSignInActivity.this::logout)
        );
    }

    private void setUpViews() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            errorLogger("QrSignInActivity", "Fetching FCM token failed: " + task.getException());
                            return;
                        }
                        fcmToken = task.getResult();
                        getSessionManager().setString(getSessionKeys().notification_token, fcmToken);
                    }
                });

        if (!checkPermission()) {
            requestPermission();
        }
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                    showMessageOKCancel(getString(R.string.allow_permission), (dialog, which) -> requestPermission());
                }
            }
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(QrSignInActivity.this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok), okListener)
                .setNegativeButton(getString(R.string.cancel), null)
                .create()
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == Activity.RESULT_OK) {
            String result = data.getStringExtra("result");
            if (result != null && !result.isEmpty()) {
                getSessionManager().setString(getSessionKeys().code, result);
                performQrLogin(result);
            }
        }
    }

    private void performQrLogin(String qrKey) {
        showProgressHUD();

        Map<String, String> params = new ApiParams()
                .add("key", qrKey)
                .add("platform", "android")
                .add("fcm_token", getSessionManager().getString(getSessionKeys().notification_token))
                .build();

        Type type = new TypeToken<BaseResponse<LoginData>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_POST,
                Endpoints.AUTH_QR,
                params,
                null,
                type,
                0,
                new ApiCallback<LoginData>() {
                    @Override
                    public void onSuccess(LoginData data, String message, String rawJson) {
                        hideProgressHUD();
                        if (data != null) {
                            saveLoginData(data, qrKey);
                            moveToNext();
                        } else {
                            toast(R.string.general_error);
                        }
                    }

                    @Override
                    public void onError(ApiError error) {
                        hideProgressHUD();
                        toast(error != null ? error.message : getString(R.string.general_error));
                    }

                    @Override
                    public void onUnauthorized(String rawJson) {
                        hideProgressHUD();
                        logout();
                    }

                    @Override
                    public void onNetworkError(String reason) {
                        hideProgressHUD();
                        toast(R.string.no_internet);
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        hideProgressHUD();
                        toast(R.string.general_error);
                    }
                }
        );
    }

    private void saveLoginData(LoginData data, String qrKey) {
        getSessionManager().setBoolean(getSessionKeys().isLogin, true);
        getSessionManager().setString(getSessionKeys().token, data.token);
        getSessionManager().setString(getSessionKeys().userJson, getGson().toJson(data.user));
        getSessionManager().setString(getSessionKeys().code, qrKey);

        // لحفظ بيانات AppData بنفس تنسيق المشروع
        AppData appData = new AppData();
        if (data.user != null) {
            appData.setUser_sanad(data.user.id);
        }
        appData.setUser_company_code(qrKey);
        getSessionManager().setString(getSessionKeys().app_data, getGson().toJson(appData));
    }

    private void moveToNext() {
        moveToActivity(this, SplashActivity.class, getIntent().getExtras(), true);
    }
}
