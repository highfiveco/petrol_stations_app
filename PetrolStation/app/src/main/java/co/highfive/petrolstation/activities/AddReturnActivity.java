package co.highfive.petrolstation.activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import java.lang.reflect.Type;
import java.util.Map;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.customers.dto.CustomerFinancialAccountResponse;
import co.highfive.petrolstation.databinding.ActivityAddReturnBinding;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.models.Account;
import co.highfive.petrolstation.models.Setting;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.ApiError;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;

import com.google.gson.reflect.TypeToken;

public class AddReturnActivity extends BaseActivity {

    private ActivityAddReturnBinding binding;

    private String accountId = "";
    private String parentId = "";

    private Account account;
    private Setting setting;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddReturnBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI(binding.mainLayout);

        readExtras();
        initClicks();

        // load account header data
        fetchAccountHeader(true);
    }

    private void readExtras() {
        Bundle extras = getIntent() != null ? getIntent().getExtras() : null;
        if (extras == null) return;
        accountId = safe(extras.getString("account_id"));
        parentId = safe(extras.getString("parent_id"));
    }

    private void initClicks() {
        binding.icHome.setOnClickListener(v ->
                moveToActivity(getApplicationContext(), MainActivity.class, null, false, true)
        );

        binding.icBack.setOnClickListener(v -> finish());

        binding.phone.setOnClickListener(v -> {
            if (account != null && !safe(account.getMobile()).trim().isEmpty()) {
                call(account.getMobile());
            }
        });

        binding.save.setOnClickListener(v -> {
            String validate = validateForm();
            if (validate == null) {
                sendReturn(true);
            } else {
                toast(validate);
            }
        });
    }

    private String validateForm() {
        String statement = safe(binding.statement.getText() != null ? binding.statement.getText().toString() : "").trim();
        String amount = safe(binding.amountFourm.getText() != null ? binding.amountFourm.getText().toString() : "").trim();

        if (statement.isEmpty()) return getString(R.string.enter_statement);
        if (amount.isEmpty()) return getString(R.string.enter_amount);
        return null;
    }

    /**
     * بدل getData القديمة: بنجيب بيانات الحساب من نفس endpoint viewFinancialMove
     * لأنه بيرجع account + setting
     */
    private void fetchAccountHeader(boolean showDialog) {
        if (accountId.trim().isEmpty()) {
            toast(getString(R.string.general_error));
            return;
        }

        if (showDialog) showProgressHUD();

        Map<String, String> params = ApiClient.mapOf(
                "page", "1",
                "account_id", accountId
        );

        Type type = new TypeToken<BaseResponse<CustomerFinancialAccountResponse>>() {}.getType();

        apiClient.request(
                co.highfive.petrolstation.hazemhamadaqa.Http.Constant.REQUEST_GET,
                Endpoints.CUSTOMERS_VIEWFINANCIALMOVE,
                params,
                null,
                type,
                0,
                new ApiCallback<CustomerFinancialAccountResponse>() {
                    @Override
                    public void onSuccess(CustomerFinancialAccountResponse data, String msg, String rawJson) {
                        if (showDialog) hideProgressHUD();

                        if (data == null || data.account == null) {
                            toast(getString(R.string.general_error));
                            return;
                        }

                        setting = data.setting;
                        account = data.account;

                        bindHeader();
                    }

                    @Override
                    public void onError(ApiError error) {
                        if (showDialog) hideProgressHUD();
                        toast(error != null && error.message != null
                                ? error.message
                                : getString(R.string.general_error));
                    }

                    @Override
                    public void onUnauthorized(String rawJson) {
                        if (showDialog) hideProgressHUD();
                        logout();
                    }

                    @Override
                    public void onNetworkError(String reason) {
                        if (showDialog) hideProgressHUD();
                        toast(R.string.no_internet);
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        if (showDialog) hideProgressHUD();
                        toast(getString(R.string.general_error));
                    }
                }
        );
    }

    private void bindHeader() {
        if (account == null) return;

        binding.name.setText(safe(account.getAccount_name()));
        binding.phone.setText(safe(account.getMobile()));
        binding.amount.setText(safe(""+account.getBalance()));
        binding.accountType.setText(safe(account.getAccount_type()));
    }

    /**
     * بدل sendData القديمة: POST addReturn
     */
    private void sendReturn(boolean showDialog) {
        if (accountId.trim().isEmpty() || parentId.trim().isEmpty()) {
            toast(getString(R.string.general_error));
            return;
        }

        String statement = safe(binding.statement.getText() != null ? binding.statement.getText().toString() : "").trim();
        String amount = safe(binding.amountFourm.getText() != null ? binding.amountFourm.getText().toString() : "").trim();
        String notes = safe(binding.text.getText() != null ? binding.text.getText().toString() : "").trim();

        if (showDialog) showProgressHUD();

        Map<String, String> params = ApiClient.mapOf(
                "account_id", accountId,
                "amount", amount,
                "parent_id", parentId,
                "statement", statement,
                "notes", notes
        );

        Type type = new TypeToken<BaseResponse<Object>>() {}.getType();

        apiClient.request(
                co.highfive.petrolstation.hazemhamadaqa.Http.Constant.REQUEST_POST,
                Endpoints.FINANCIAL_ADDRETURN, // لازم يكون معرف عندك
                params,
                null,
                type,
                0,
                new ApiCallback<Object>() {
                    @Override
                    public void onSuccess(Object data, String msg, String rawJson) {
                        if (showDialog) hideProgressHUD();
                        toast(msg != null && !msg.trim().isEmpty() ? msg : getString(R.string.done));
                        finish();
                    }

                    @Override
                    public void onError(ApiError error) {
                        if (showDialog) hideProgressHUD();
                        toast(error != null && error.message != null
                                ? error.message
                                : getString(R.string.general_error));
                    }

                    @Override
                    public void onUnauthorized(String rawJson) {
                        if (showDialog) hideProgressHUD();
                        logout();
                    }

                    @Override
                    public void onNetworkError(String reason) {
                        if (showDialog) hideProgressHUD();
                        toast(R.string.no_internet);
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        if (showDialog) hideProgressHUD();
                        toast(getString(R.string.general_error));
                    }
                }
        );
    }


}
