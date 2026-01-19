package co.highfive.petrolstation.activities;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Calendar;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.auth.AuthService;
import co.highfive.petrolstation.auth.dto.LoginData;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.models.AppData;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;

public class SignInActivity extends BaseActivity {

    private AppCompatEditText etUsername, etPassword, etCompanyCode;
    private AppCompatImageView icShowPassword;
    private AppCompatTextView btnSignIn;

    private boolean passwordVisible = false;
    private String fcmToken = "";

    private ApiClient apiClient;
    private AuthService authService;
    AppCompatTextView year;
    Calendar calendar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        etUsername = findViewById(R.id.username);
        etPassword = findViewById(R.id.password);
        etCompanyCode = findViewById(R.id.company_code);
        icShowPassword = findViewById(R.id.ic_show_password);
        btnSignIn = findViewById(R.id.sign_in);
        year = findViewById(R.id.year);

        calendar = Calendar.getInstance();
        year.setText(""+calendar.get(Calendar.YEAR));

        initApiClient();
        authService = new AuthService(apiClient);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        fcmToken = task.getResult();
                        getSessionManager().setString(getSessionKeys().notification_token, fcmToken);
                    }
                });

        icShowPassword.setOnClickListener(v -> togglePassword());
        btnSignIn.setOnClickListener(v -> doLogin());

        findViewById(R.id.scan_qr).setOnClickListener(v ->
                moveToActivity(this, QrSignInActivity.class, getIntent().getExtras(), false)
        );

        findViewById(R.id.footer).setOnClickListener(v ->
                openURl("https://www.high-five.co/")
        );

        if(isDevelopment){
            etUsername.setText("admin");
            etPassword.setText("1");
//            company_code.setText("8796");
            etCompanyCode.setText("0001");

//            username.setText("hft");
//            password.setText("1");
//            company_code.setText("8981");
//            company_code.setText("8796");
        }
    }

    private void initApiClient() {
        apiClient = new ApiClient(
                getApplicationContext(),
                getGson(),
                new ApiClient.HeaderProvider() {
                    @Override public String getToken() { return getSessionManager().getString(getSessionKeys().token); }
                    @Override public String getLang() {
                        String lang = getSessionManager().getString(getSessionKeys().language_code);
                        return (lang == null || lang.trim().isEmpty()) ? "ar" : lang;
                    }
                    @Override public boolean isLoggedIn() { return getSessionManager().getBoolean(getSessionKeys().isLogin); }
                },
                () -> runOnUiThread(SignInActivity.this::logout)
        );
    }

    private void togglePassword() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        etPassword.setSelection(etPassword.getText() != null ? etPassword.getText().length() : 0);
    }

    private void doLogin() {
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String code = etCompanyCode.getText() != null ? etCompanyCode.getText().toString().trim() : "";

        if (username.isEmpty()) { toast(R.string.username); return; }
        if (password.isEmpty()) { toast(R.string.password); return; }
        if (code.isEmpty()) { toast(R.string.company_code); return; }

        showProgressHUD();

        authService.login(
                code,
                username,
                password,
                "android",
                fcmToken,
                new ApiCallback<LoginData>() {
                    @Override
                    public void onSuccess(LoginData data, String message, String rawJson) {
                        hideProgressHUD();

                        // خزّن session
                        if (data != null) {
                            getSessionManager().setBoolean(getSessionKeys().isLogin, true);
                            getSessionManager().setString(getSessionKeys().token, data.token);
                            getSessionManager().setString(getSessionKeys().username, username);
                            getSessionManager().setString(getSessionKeys().code, code);
                            getSessionManager().setString(getSessionKeys().userJson, getGson().toJson(data.user));

                            // مهم للتوافق مع الكود القديم: نخزّن user_sanad + user_company_code داخل app_data
                            AppData appData = new AppData();
                            if (data.user != null) appData.setUser_sanad(data.user.id);
                            appData.setUser_company_code(code);
                            getSessionManager().setString(getSessionKeys().app_data, getGson().toJson(appData));
                        }

                        // بعد اللوجن رجّعنا للسبلاش عشان تعمل getSetting/getCompanySetting
                        moveToActivity(SignInActivity.this, SplashActivity.class, null, true);
                    }

                    @Override
                    public void onError(co.highfive.petrolstation.network.ApiError error) {
                        hideProgressHUD();
                        toast(error != null ? error.message : getString(R.string.general_error));
                    }

                    @Override
                    public void onUnauthorized(String rawJson) {
                        hideProgressHUD();
                        toast(getString(R.string.general_error));
                    }

                    @Override
                    public void onNetworkError(String reason) {
                        hideProgressHUD();
                        toast(R.string.no_internet);
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        hideProgressHUD();
                        toast(getString(R.string.general_error));
                    }
                }
        );
    }
}
