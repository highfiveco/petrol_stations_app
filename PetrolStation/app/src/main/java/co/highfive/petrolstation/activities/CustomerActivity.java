package co.highfive.petrolstation.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.customers.dialogs.SendSmsDialog;
import co.highfive.petrolstation.customers.dialogs.UpdatePhoneDialog;
import co.highfive.petrolstation.databinding.ActivityCustomerBinding;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;

import co.highfive.petrolstation.customers.dialogs.AddReminderDialog;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.listener.AddReminderListener;
import co.highfive.petrolstation.listener.SendSmsListener;
import co.highfive.petrolstation.listener.UpdatePhoneListener;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.ApiError;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;

public class CustomerActivity extends BaseActivity {


    private ActivityCustomerBinding binding;

    private String customerId;
    private String accountId;
    private String customerName;
    private String customerMobile;

    private AddReminderDialog addReminderDialog;
    private SendSmsDialog sendSmsDialog;
    private UpdatePhoneDialog updatePhoneDialog;

    private boolean isOfflineCustomer = false;
    private long offlineCustomerLocalId = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCustomerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        readExtras();
        initHeader();
        applyPermissions();
        initClicks();
    }

    private void openAddReminderDialog(AddReminderListener listener) {
        addReminderDialog = new AddReminderDialog();
        addReminderDialog.setCancelable(false);
        addReminderDialog.setAddReminderListener(listener);
        addReminderDialog.show(getSupportFragmentManager(), "AddReminderDialog");
    }


    private void addReminders(String date, String text) {
        showProgressHUD();

        Map<String, String> params = ApiClient.mapOf(
                "id", customerId,
                "date", date,
                "text", text
        );

        Type type = new TypeToken<BaseResponse<Object>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_POST,
                Endpoints.CUSTOMERS_ADDREMINDERS,
                params,
                null,
                type,
                0,
                new ApiCallback<Object>() {
                    @Override
                    public void onSuccess(Object data, String message, String rawJson) {
                        hideProgressHUD();
                        if (addReminderDialog != null) addReminderDialog.dismissAllowingStateLoss();
                        toast(message != null && !message.trim().isEmpty() ? message : getString(R.string.saved_local));
                    }

                    @Override
                    public void onError(co.highfive.petrolstation.network.ApiError error) {
                        hideProgressHUD();
                        toast(error.message);
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
                        toast(getString(R.string.general_error));
                    }
                }
        );
    }



    private void readExtras() {
        Bundle b = getIntent() != null ? getIntent().getExtras() : null;
        if (b == null) return;

        isOfflineCustomer = b.getBoolean("is_offline_customer", false);
        offlineCustomerLocalId = b.getLong("offline_customer_local_id", 0);

        customerId = safe(b.getString("id"));
        if (customerId.trim().isEmpty()) customerId = safe(b.getString("customer_id"));

        accountId = safe(b.getString("account_id"));
        customerName = safe(b.getString("name"));
        customerMobile = safe(b.getString("mobile"));
    }

    private void initHeader() {
        binding.name.setText(customerName);
        binding.phone.setText(customerMobile);

        binding.icBack.setOnClickListener(v -> finish());
        binding.icHome.setOnClickListener(v ->
                moveToActivity(getApplicationContext(), MainActivity.class, null, false, true)
        );

        // call on phone click (مثل القديم)
        binding.phone.setOnClickListener(v -> {
            if (!customerMobile.trim().isEmpty()) {
                call(customerMobile);
            }
        });
    }

    private void applyPermissions() {
        // الصلاحيات الآن عندكم غالباً ضمن app_data
        // ملاحظة: انت سابقاً كنت تستخدم appData.getUpdate_customers() ... إلخ
        // هنا بنقرأها من السيشن بنفس الطريقة اللي تعملها بباقي الشاشات.
        // إذا getAppData() عندك جاهزة في BaseActivity استخدمها، وإلا اقرأها من session.
        // سأستخدم getAppData() لأنها موجودة عندك بكذا مكان.

        if (getAppData() == null) {
            hideAllActions();
            return;
        }

        int updateCustomers = safeInt(getAppData().getUpdate_customers());
        int viewFinancialMove = safeInt(getAppData().getView_financial_move());
        int viewReminders = safeInt(getAppData().getView_reminders());
        int viewLog = safeInt(getAppData().getView_log());
        int sms = safeInt(getAppData().getSms());
        int addReminders = safeInt(getAppData().getAdd_reminders());
        int updateMobile = safeInt(getAppData().getUpdate_mobile());
        int invoices = safeInt(getAppData().getCustomer_invoices());
        int fuel_sales = safeInt(getAppData().getFuel_sales());
        int view_customer_vehicles = safeInt(getAppData().getView_customer_vehicles());

        setVisible(binding.editDataLayout, updateCustomers == 1);
        setVisible(binding.moneyAccountLayout, viewFinancialMove == 1);

        // زر إضافة حركة مالية (بالقديم كان ظاهر دائماً؟ كان مربوط بالـ financial_move أيضاً)
        setVisible(binding.addMoneyTransactionLayout, viewFinancialMove == 1);


        setVisible(binding.fuelSalesLayout, fuel_sales ==1);
//        setVisible(binding.fuelSalesLayout, true);

        setVisible(binding.ticketsLayout, viewReminders == 1);
        setVisible(binding.logsLayout, viewLog == 1);

        setVisible(binding.sendSmsLayout, sms == 1);
        setVisible(binding.addReminderLayout, addReminders == 1);
        setVisible(binding.updatePhoneLayout, updateMobile == 1);

        // invoices + vehicles: حسب permissions اللي عندك بالـ app_data (ذكرت view_customer_vehicles)
//        int viewCustomerVehicles = safeInt(getAppData().getView_customer_vehicles());
        setVisible(binding.vehiclesLayout, view_customer_vehicles == 1);

        // invoices permission مش ظاهر عندك بالـ JSON المرجعي كحقل صريح
        // فحالياً نخليه GONE. إذا عندك flag لاحقاً (مثلاً view_invoices) ابعته ونربطه.
        setVisible(binding.invoicesLayout, invoices==1);
    }

    private void initClicks() {

        binding.editDataLayout.setOnClickListener(v -> {
            Bundle bundle = buildCustomerBundle();
            moveToActivityWithResult(CustomerActivity.this, EditCustomerActivity.class, bundle, false, 202);
        });

        binding.addMoneyTransactionLayout.setOnClickListener(v -> {
            Bundle bundle = buildCustomerBundle();
            moveToActivity(CustomerActivity.this, AddFinancialTransactionActivity.class, bundle, false);
        });

        binding.moneyAccountLayout.setOnClickListener(v -> {
            Bundle bundle = buildCustomerBundle();
            moveToActivity(CustomerActivity.this, CustomerFinancialAccountActivity.class, bundle, false);
        });

        binding.ticketsLayout.setOnClickListener(v -> {
            Bundle bundle = buildCustomerBundle();
            moveToActivity(CustomerActivity.this, CustomerRemindersActivity.class, bundle, false);
        });

        binding.logsLayout.setOnClickListener(v -> {
            Bundle bundle = buildCustomerBundle();
            moveToActivity(CustomerActivity.this, CustomerLogActivity.class, bundle, false);
        });

        binding.sendSmsLayout.setOnClickListener(v -> openSendSmsDialog());

        binding.addReminderLayout.setOnClickListener(v -> {
            openAddReminderDialog(new AddReminderListener() {
                @Override
                public void addReminder(String text, String date) {
                    addReminders(date, text);
                }
            });
        });

        binding.updatePhoneLayout.setOnClickListener(v -> {
            openUpdatePhoneDialog(phone -> {
                if (connectionAvailable) {
                    updatePhoneRequest(true, phone);
                } else {
                    toast(R.string.no_internet); // حسب طلبك تجاهل updateLocalPhoneRequest
                }
            });
        });

        binding.invoicesLayout.setOnClickListener(v -> {
            Bundle bundle = buildCustomerBundle();
            bundle.putInt("invoice_type", 0); // 0 invoices
            moveToActivity(CustomerActivity.this, CustomerInvoicesActivity.class, bundle, false);
        });

        binding.vehiclesLayout.setOnClickListener(v -> {
            Bundle bundle = buildCustomerBundle();
            moveToActivity(CustomerActivity.this, CustomerVehiclesActivity.class, bundle, false);
        });

        binding.fuelSalesLayout.setOnClickListener(v -> {
            Bundle bundle = buildCustomerBundle();
            bundle.putInt("invoice_type", 1); // 1 fuel_invoices
            moveToActivity(CustomerActivity.this, FuelSalesActivity.class, bundle, false);
        });
    }
    private void openUpdatePhoneDialog(UpdatePhoneListener listener) {
        updatePhoneDialog = UpdatePhoneDialog.newInstance(customerName, customerMobile);
        updatePhoneDialog.setCancelable(false);
        updatePhoneDialog.setListener(listener);
        updatePhoneDialog.show(getSupportFragmentManager(), "UpdatePhoneDialog");
    }

    private void updatePhoneRequest(boolean showDialog, String phoneVal) {

        if (customerId == null || customerId.trim().isEmpty()) {
            toast(getString(R.string.general_error));
            return;
        }

        if (showDialog) showProgressHUD();

        Map<String, String> params = ApiClient.mapOf(
                "id", customerId,
                "mobile", phoneVal
        );

        Type type = new TypeToken<BaseResponse<Object>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_POST,
                Endpoints.CUSTOMERS_UPDATE_MOBILE,
                params,
                null,
                type,
                0,
                new ApiCallback<Object>() {

                    @Override
                    public void onSuccess(Object data, String message, String rawJson) {
                        if (showDialog) hideProgressHUD();

                        // ✅ update local vars + UI
                        customerMobile = phoneVal;
                        binding.phone.setText(phoneVal);

                        // ✅ close dialog
                        if (updatePhoneDialog != null) {
                            updatePhoneDialog.dismissAllowingStateLoss();
                        }

                        toast(message != null && !message.trim().isEmpty()
                                ? message
                                : getString(R.string.done));
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

    private void openSendSmsDialog() {
        sendSmsDialog = new SendSmsDialog();
        sendSmsDialog.setCancelable(false);

        sendSmsDialog.setListener(new SendSmsListener() {
            @Override
            public void onSend(String message) {
                sendOneSms(true, customerId, message);
            }

            @Override
            public void onPrefillRequested() {

            }
        });

        sendSmsDialog.show(getSupportFragmentManager(), "SendSmsDialog");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 202 && resultCode == Activity.RESULT_OK && data != null) {
            String newName = data.getStringExtra("name");
            String newPhone = data.getStringExtra("phone");

            if (newName != null) {
                customerName = newName;
                binding.name.setText(newName);
            }
            if (newPhone != null) {
                customerMobile = newPhone;
                binding.phone.setText(newPhone);
            }
        }
    }

    private void sendOneSms(boolean showDialog, String customerId, String message) {
        if (showDialog) showProgressHUD();

        java.util.Map<String, String> params = ApiClient.mapOf(
                "id", customerId,
                "text", message
        );

        java.lang.reflect.Type type =
                new com.google.gson.reflect.TypeToken<BaseResponse<Object>>() {}.getType();

        apiClient.request(
                co.highfive.petrolstation.hazemhamadaqa.Http.Constant.REQUEST_POST,
                Endpoints.CUSTOMERS_SENDONESMS,
                params,
                null,
                type,
                0,
                new ApiCallback<Object>() {
                    @Override
                    public void onSuccess(Object data, String msg, String rawJson) {
                        hideProgressHUD();
                        toast(msg != null && !msg.trim().isEmpty() ? msg : getString(R.string.done));
                        if (sendSmsDialog != null) sendSmsDialog.dismiss();
                    }

                    @Override
                    public void onError(co.highfive.petrolstation.network.ApiError error) {
                        hideProgressHUD();
                        toast(error.message);
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
                        toast(getString(R.string.general_error));
                    }
                }
        );
    }


    private Bundle buildCustomerBundle() {
        Bundle b = new Bundle();
        b.putString("id", safe(customerId));
        b.putString("customer_id", safe(customerId));
        b.putString("account_id", safe(accountId));
        b.putString("name", safe(customerName));
        b.putString("mobile", safe(customerMobile));

        // ✅ offline markers
        b.putBoolean("is_offline_customer", isOfflineCustomer);
        b.putLong("offline_customer_local_id", offlineCustomerLocalId);

        return b;
    }

    private void hideAllActions() {
        setVisible(binding.editDataLayout, false);
        setVisible(binding.addMoneyTransactionLayout, false);
        setVisible(binding.moneyAccountLayout, false);
        setVisible(binding.fuelSalesLayout, false);
        setVisible(binding.ticketsLayout, false);
        setVisible(binding.logsLayout, false);
        setVisible(binding.sendSmsLayout, false);
        setVisible(binding.addReminderLayout, false);
        setVisible(binding.updatePhoneLayout, false);
        setVisible(binding.invoicesLayout, false);
        setVisible(binding.vehiclesLayout, false);
    }


}
