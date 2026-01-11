package co.highfive.petrolstation.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.adapters.DropDownAdapter;
import co.highfive.petrolstation.databinding.ActivityEditCustomerBinding;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.models.Currency;
import co.highfive.petrolstation.models.Customer;
import co.highfive.petrolstation.models.CustomerStatus;
import co.highfive.petrolstation.models.CustomersSettingData;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.ApiError;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;

public class EditCustomerActivity extends BaseActivity {

    private ActivityEditCustomerBinding binding;

    private String customerId;
    private Customer customer;

    private String statusId = null;
    private String typeCustomerId = null;
    private String customerClassifyId = null;

    private ArrayList<CustomerStatus> settingsCustomerStatus = new ArrayList<>();
    private ArrayList<CustomerStatus> settingsTypeCustomer = new ArrayList<>();
    private ArrayList<CustomerStatus> settingsCustomerClassify = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEditCustomerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupUI(findViewById(android.R.id.content));

        readExtras();
        initHeader();
        initClicks();

        fetchCustomersSetting(true);
    }

    private boolean isAddMode() {
        return customerId == null || customerId.trim().isEmpty();
    }

    private void readExtras() {
        Bundle b = getIntent() != null ? getIntent().getExtras() : null;
        if (b == null) return;
        customerId = safe(b.getString("id"));
    }

    private void initHeader() {
        binding.icBack.setOnClickListener(v -> finish());
        binding.icHome.setOnClickListener(v ->
                moveToActivity(getApplicationContext(), MainActivity.class, null, false, true)
        );

        binding.title.setText(isAddMode() ? R.string.add_customer : R.string.edit_data);
    }

    private void initClicks() {

        binding.statusLayout.setOnClickListener(v -> selectStatus());
        binding.typeCustomerLayout.setOnClickListener(v -> selectTypeCustomer());

        if (binding.customerClassifyLayout != null) {
            binding.customerClassifyLayout.setOnClickListener(v -> selectCustomerClassify());
        }

        binding.phone.setOnClickListener(v -> {
            String phone = safeTrim(binding.phone.getText());
            if (!phone.isEmpty()) call(phone);
        });

        binding.save.setOnClickListener(v -> {
            String validate = validateForm();
            if (validate != null) {
                toast(validate);
                return;
            }
            submitCustomer(true);
        });
    }

    private String validateForm() {
        if (safeTrim(binding.name.getText()).isEmpty()) {
            return getString(R.string.enter_name);
        }
        return null;
    }

    private void setUpDataToUi() {
        if (customer == null) return;

        binding.name.setText(safe(customer.getName()));
        binding.phone.setText(safe(customer.getMobile()));
        binding.address.setText(safe(customer.getAddress()));
        binding.originalNumber.setText(safe(customer.getAesseal_no()));

        statusId = safe(customer.getStatus());
        binding.statusVal.setText(safe(customer.getCustomer_status()));

        typeCustomerId = safe(customer.getType_customer());
        binding.typeCustomer.setText(safe(customer.getType_customer_name()));

        // لازم يكون عندك داخل Customer:
        // getCustomer_classify() + getCustomer_classify_name()
        if (binding.customerClassify != null) {
            customerClassifyId = safe(customer.getCustomer_classify());
            binding.customerClassify.setText(safe(customer.getCustomer_classify_name()));
        }
    }

    private void fetchCustomer(boolean showDialog) {
        if (isAddMode()) return;

        if (showDialog) showProgressHUD();

        Map<String, String> params = ApiClient.mapOf("id", customerId);

        String url = Endpoints.CUSTOMERS_EDIT;
        Type type = new TypeToken<BaseResponse<Customer>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                url,
                params,
                null,
                type,
                0,
                new ApiCallback<Customer>() {
                    @Override
                    public void onSuccess(Customer data, String message, String rawJson) {
                        if (showDialog) hideProgressHUD();
                        customer = data;
                        setUpDataToUi();
                    }

                    @Override
                    public void onError(ApiError error) {
                        if (showDialog) hideProgressHUD();
                        toast(error != null && error.message != null ? error.message : getString(R.string.general_error));
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

    private void submitCustomer(boolean showDialog) {
        if (showDialog) showProgressHUD();

        Map<String, String> params = ApiClient.mapOf(
                "name", safeTrim(binding.name.getText()),
                "mobile", safeTrim(binding.phone.getText()),
                "address", safeTrim(binding.address.getText()),
                "aesseal_no_check", safeTrim(binding.originalNumber.getText())
        );

        if (statusId != null && !statusId.trim().isEmpty()) {
            params.put("status", statusId);
        }
        if (typeCustomerId != null && !typeCustomerId.trim().isEmpty()) {
            params.put("type_customer", typeCustomerId);
        }
        if (customerClassifyId != null && !customerClassifyId.trim().isEmpty()) {
            params.put("customer_classify", customerClassifyId);
        }

        String endpoint;
        if (isAddMode()) {
            endpoint = Endpoints.CUSTOMERS_ADD;
        } else {
            endpoint = Endpoints.CUSTOMERS_UPDATE;
            params.put("id", customerId);
        }

        Type type = new TypeToken<BaseResponse<Object>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_POST,
                endpoint,
                params,
                null,
                type,
                0,
                new ApiCallback<Object>() {
                    @Override
                    public void onSuccess(Object data, String message, String rawJson) {
                        if (showDialog) hideProgressHUD();

                        toast(message != null && !message.trim().isEmpty()
                                ? message
                                : getString(R.string.done));

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("name", safeTrim(binding.name.getText()));
                        resultIntent.putExtra("phone", safeTrim(binding.phone.getText()));
                        setResult(Activity.RESULT_OK, resultIntent);

                        finish();
                    }

                    @Override
                    public void onError(ApiError error) {
                        if (showDialog) hideProgressHUD();
                        toast(error != null && error.message != null ? error.message : getString(R.string.general_error));
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

    // -------------------------
    // Settings Request
    // -------------------------

    private void fetchCustomersSetting(boolean showDialog) {
        if (showDialog) showProgressHUD();

        String url = Endpoints.GETCUSTOMERSSETTING;

        Type type = new TypeToken<BaseResponse<CustomersSettingData>>() {}.getType();

        ApiClient.ApiParams params = new ApiClient.ApiParams(); // فاضي (GET بدون باراميتر)

        apiClient.request(
                Constant.REQUEST_GET,
                url,
                params,
                null,
                type,
                0,
                new ApiCallback<CustomersSettingData>() {
                    @Override
                    public void onSuccess(CustomersSettingData data, String message, String rawJson) {
                        if (showDialog) hideProgressHUD();

                        settingsCustomerStatus.clear();
                        settingsTypeCustomer.clear();
                        settingsCustomerClassify.clear();

                        if (data != null) {
                            if (data.getCustomer_status() != null) settingsCustomerStatus.addAll(data.getCustomer_status());
                            if (data.getType_customer() != null) settingsTypeCustomer.addAll(data.getType_customer());
                            if (data.getCustomer_classify() != null) settingsCustomerClassify.addAll(data.getCustomer_classify());
                        }

                        if (!isAddMode()) {
                            fetchCustomer(true);
                        } else {
                            binding.title.setText(R.string.add_customer);
                        }
                    }

                    @Override
                    public void onError(ApiError error) {
                        if (showDialog) hideProgressHUD();
                        toast(error != null && error.message != null ? error.message : getString(R.string.general_error));
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

    // -------------------------
    // Dropdowns (PopupWindow)
    // -------------------------

    private void selectStatus() {
        ArrayList<Currency> list = new ArrayList<>();

        if (settingsCustomerStatus != null && !settingsCustomerStatus.isEmpty()) {
            for (int i = 0; i < settingsCustomerStatus.size(); i++) {
                list.add(new Currency(
                        safe(settingsCustomerStatus.get(i).getId()),
                        safe(settingsCustomerStatus.get(i).getName())
                ));
            }
        }

        if (list.isEmpty()) {
            toast(getString(R.string.general_error));
            return;
        }

        showDropDown(binding.statusVal, list, (selectedId, selectedName) -> {
            statusId = selectedId;
            binding.statusVal.setText(selectedName);
        });
    }

    private void selectTypeCustomer() {
        ArrayList<Currency> list = new ArrayList<>();

        if (settingsTypeCustomer != null && !settingsTypeCustomer.isEmpty()) {
            for (int i = 0; i < settingsTypeCustomer.size(); i++) {
                list.add(new Currency(
                        safe(settingsTypeCustomer.get(i).getId()),
                        safe(settingsTypeCustomer.get(i).getName())
                ));
            }
        }

        if (list.isEmpty()) {
            toast(getString(R.string.general_error));
            return;
        }

        showDropDown(binding.typeCustomer, list, (selectedId, selectedName) -> {
            typeCustomerId = selectedId;
            binding.typeCustomer.setText(selectedName);
        });
    }

    private void selectCustomerClassify() {
        if (binding.customerClassify == null) return;

        ArrayList<Currency> list = new ArrayList<>();

        if (settingsCustomerClassify != null && !settingsCustomerClassify.isEmpty()) {
            for (int i = 0; i < settingsCustomerClassify.size(); i++) {
                list.add(new Currency(
                        safe(settingsCustomerClassify.get(i).getId()),
                        safe(settingsCustomerClassify.get(i).getName())
                ));
            }
        }

        if (list.isEmpty()) {
            toast(getString(R.string.general_error));
            return;
        }

        showDropDown(binding.customerClassify, list, (selectedId, selectedName) -> {
            customerClassifyId = selectedId;
            binding.customerClassify.setText(selectedName);
        });
    }

    private interface DropDownSelectListener {
        void onSelected(String id, String name);
    }

    private void showDropDown(View anchor, ArrayList<Currency> list, DropDownSelectListener listener) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View popupView = Objects.requireNonNull(inflater).inflate(R.layout.drop_down_list, null);

        RecyclerView recyclerView = popupView.findViewById(R.id.recycler_view);

        int width = (int) (getWindowManager().getDefaultDisplay().getWidth() * 0.6);
        PopupWindow popupWindow = new PopupWindow(popupView, width, ViewGroup.LayoutParams.WRAP_CONTENT);

        DropDownAdapter adapter = new DropDownAdapter(list, (view, position) -> {
            Currency c = list.get(position);
            popupWindow.dismiss();
            if (listener != null) listener.onSelected(c.getId(), c.getName());
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(18f);
        }
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.showAsDropDown(anchor);
    }
}
