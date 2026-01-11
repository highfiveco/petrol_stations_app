package co.highfive.petrolstation.activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.adapters.CustomerVehiclesAdapter;
import co.highfive.petrolstation.customers.dto.CustomerVehicleDto;
import co.highfive.petrolstation.customers.dto.CustomerVehiclesResponseDto;
import co.highfive.petrolstation.customers.dto.VehicleSettingsResponseDto;
import co.highfive.petrolstation.databinding.ActivityCustomerVehiclesBinding;
import co.highfive.petrolstation.fragments.AddVehicleDialog;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.ApiError;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;

public class CustomerVehiclesActivity extends BaseActivity {

    private ActivityCustomerVehiclesBinding binding;

    private String customerId = "";
    private String accountId = "";
    private String customerName = "";
    private String customerMobile = "";

    private final ArrayList<CustomerVehicleDto> items = new ArrayList<>();
    private CustomerVehiclesAdapter adapter;

    // permissions
    private boolean canView = true;
    private boolean canAdd = true;
    private boolean canEdit = true;
    private boolean canDelete = true;

    // cached settings
    private VehicleSettingsResponseDto cachedSettings = null;
    private AddVehicleDialog activeDialog = null;

    // session key for settings json
    private static final String SESSION_KEY_VEHICLE_SETTINGS_JSON = "SESSION_KEY_VEHICLE_SETTINGS_JSON";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCustomerVehiclesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI(binding.mainLayout);

        readExtras();
        initHeader();
        initRecycler();
        initRefresh();
        initClicks();

        // load vehicles directly on open
        fetchVehicles(true);
    }

    private void readExtras() {
        Bundle extras = getIntent() != null ? getIntent().getExtras() : null;
        if (extras == null) return;

        customerId = safe(extras.getString("id"));
        accountId = safe(extras.getString("account_id"));
        customerName = safe(extras.getString("name"));
        customerMobile = safe(extras.getString("mobile"));
    }

    private void initHeader() {
        binding.name.setText(customerName);
        binding.phone.setText(customerMobile);
        binding.amount.setText("");

        binding.phone.setOnClickListener(v -> {
            if (!customerMobile.trim().isEmpty()) call(customerMobile);
        });

        // hide add button until permissions loaded
        binding.icAddWhite.setVisibility(View.GONE);
    }

    private void initClicks() {
        binding.icHome.setOnClickListener(v ->
                moveToActivity(getApplicationContext(), MainActivity.class, null, false, true)
        );
        binding.icBack.setOnClickListener(v -> finish());

        binding.icAddWhite.setOnClickListener(v -> {
            if (!canAdd) return;

            // fetch settings then open add dialog
            fetchVehicleSettingsThenOpenDialog(null);
        });
    }

    private void initRecycler() {
        adapter = new CustomerVehiclesAdapter(items, v -> {
            if (!canEdit) return;

            // fetch settings then open edit dialog with data
            fetchVehicleSettingsThenOpenDialog(v);
        });

        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);
    }

    private void initRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> fetchVehicles(false));
    }

    // ============================================================
    // Vehicles List API
    // ============================================================
    private void fetchVehicles(boolean showDialog) {
        if (customerId.trim().isEmpty()) {
            toast(getString(R.string.general_error));
            return;
        }

        if (showDialog) showProgressHUD();
        binding.swipeRefreshLayout.setRefreshing(!showDialog);

        Map<String, String> params = ApiClient.mapOf(
                "customer_id", customerId
        );

        Type type = new TypeToken<BaseResponse<CustomerVehiclesResponseDto>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.CUSTOMER_VEHICLES,
                params,
                null,
                type,
                0,
                new ApiCallback<CustomerVehiclesResponseDto>() {
                    @Override
                    public void onSuccess(CustomerVehiclesResponseDto data, String msg, String rawJson) {
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);

                        if (data == null) {
                            toast(getString(R.string.general_error));
                            return;
                        }

                        canView = data.view_customer_vehicles == 1;
                        canAdd = data.add_customer_vehicles == 1;
                        canEdit = data.edit_customer_vehicles == 1;
                        canDelete = data.delete_customer_vehicles == 1;

                        binding.icAddWhite.setVisibility(canAdd ? View.VISIBLE : View.GONE);
                        adapter.setCanEdit(canEdit);

                        if (data.customer != null) {
                            String n = safe(data.customer.name);
                            String m = safe(data.customer.mobile);

                            if (!n.isEmpty()) binding.name.setText(n);
                            if (!m.isEmpty()) binding.phone.setText(m);

                            try {
                                binding.amount.setText(formatNumber(data.customer.balance));
                            } catch (Exception ignored) {
                                binding.amount.setText("");
                            }
                        }

                        if (!canView) {
                            adapter.setItems(new ArrayList<>());
                            toast(getString(R.string.general_error));
                            return;
                        }

                        List<CustomerVehicleDto> list = (data.vehicles != null) ? data.vehicles : new ArrayList<>();
                        adapter.setItems(list);
                    }

                    @Override
                    public void onError(ApiError error) {
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        toast(error != null ? error.message : getString(R.string.general_error));
                    }

                    @Override
                    public void onUnauthorized(String rawJson) {
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        logout();
                    }

                    @Override
                    public void onNetworkError(String reason) {
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        toast(R.string.no_internet);
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        errorLogger("CustomerVehiclesParseError", e.getMessage() == null ? "null" : e.getMessage());
                        toast(getString(R.string.general_error));
                    }
                }
        );
    }

    // ============================================================
    // Settings -> session fallback -> open dialog
    // editVehicle == null => add mode
    // ============================================================
    private void fetchVehicleSettingsThenOpenDialog(@Nullable CustomerVehicleDto editVehicle) {

        // 1) if already cached in memory, open immediately
        if (cachedSettings != null) {
            openAddEditVehicleDialog(editVehicle, cachedSettings);
            return;
        }

        // 2) try session first (fast)
        VehicleSettingsResponseDto fromSession = readVehicleSettingsFromSession();
        if (fromSession != null) {
            cachedSettings = fromSession;
            // still try API to refresh if internet is available,
            // but open dialog now based on session
            openAddEditVehicleDialog(editVehicle, cachedSettings);

            // refresh silently from api
            fetchVehicleSettingsFromApi(false, null);
            return;
        }

        // 3) no session => must call API (show loading)
        fetchVehicleSettingsFromApi(true, () -> {
            if (cachedSettings != null) {
                openAddEditVehicleDialog(editVehicle, cachedSettings);
            } else {
                toast(getString(R.string.general_error));
            }
        });
    }

    // ============================================================
    // Settings API
    // ============================================================
    private void fetchVehicleSettingsFromApi(boolean showDialog, @Nullable Runnable onReady) {

        if (showDialog) showProgressHUD();

        Type type = new TypeToken<BaseResponse<VehicleSettingsResponseDto>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.CUSTOMER_VEHICLES_SETTINGS,
                (Map<String, String>) null,
                null,
                type,
                0,
                new ApiCallback<VehicleSettingsResponseDto>() {

                    @Override
                    public void onSuccess(VehicleSettingsResponseDto data, String msg, String rawJson) {
                        if (showDialog) hideProgressHUD();

                        if (rawJson != null && !rawJson.trim().isEmpty()) {
                            try { getSessionManager().setString(SESSION_KEY_VEHICLE_SETTINGS_JSON, rawJson); }
                            catch (Exception ignored) {}
                        }

                        if (data != null) {
                            cachedSettings = data;
                        } else {
                            cachedSettings = readVehicleSettingsFromSession();
                        }

                        if (onReady != null) onReady.run();
                    }

                    @Override
                    public void onError(ApiError error) {
                        if (showDialog) hideProgressHUD();

                        cachedSettings = readVehicleSettingsFromSession();
                        if (cachedSettings == null) {
                            toast(error != null ? error.message : getString(R.string.general_error));
                        }

                        if (onReady != null) onReady.run();
                    }

                    @Override
                    public void onUnauthorized(String rawJson) {
                        if (showDialog) hideProgressHUD();
                        logout();
                    }

                    @Override
                    public void onNetworkError(String reason) {
                        if (showDialog) hideProgressHUD();

                        cachedSettings = readVehicleSettingsFromSession();
                        if (cachedSettings == null) toast(R.string.no_internet);

                        if (onReady != null) onReady.run();
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        if (showDialog) hideProgressHUD();

                        cachedSettings = readVehicleSettingsFromSession();
                        if (cachedSettings == null) {
                            errorLogger("VehicleSettingsParseError", e.getMessage() == null ? "null" : e.getMessage());
                            toast(getString(R.string.general_error));
                        }

                        if (onReady != null) onReady.run();
                    }
                }
        );
    }

    private VehicleSettingsResponseDto readVehicleSettingsFromSession() {
        try {
            String raw = getSessionManager().getString(SESSION_KEY_VEHICLE_SETTINGS_JSON);
            if (raw == null || raw.trim().isEmpty()) return null;

            // session contains BaseResponse<VehicleSettingsResponseDto>
            Type type = new TypeToken<BaseResponse<VehicleSettingsResponseDto>>() {}.getType();
            BaseResponse<VehicleSettingsResponseDto> base = getGson().fromJson(raw, type);
            if (base != null && base.status) return base.data;

        } catch (Exception ignored) {}
        return null;
    }

    // ============================================================
    // Dialog open (UI only) + Activity handles requests later
    // ============================================================
    private void openAddEditVehicleDialog(@Nullable CustomerVehicleDto editVehicle,
                                          @NonNull VehicleSettingsResponseDto settings) {

        activeDialog = AddVehicleDialog.newInstance(
                safe(customerId),
                editVehicle,          // null => add mode
                settings              // contains vehicle_type, vehicle_color, model (assumed)
        );

        activeDialog.setCancelable(false);

        activeDialog.setListener(new AddVehicleDialog.Listener() {
            @Override
            public void onSubmitAdd(Map<String, String> payload) {
                // Activity handles add request
                submitAddVehicle(payload);
            }

            @Override
            public void onSubmitEdit(Map<String, String> payload) {
                // Activity handles update request
                submitUpdateVehicle(payload);
            }

            @Override
            public void onDismissed() {

            }
        });

        activeDialog.show(getSupportFragmentManager(), "AddVehicleDialog");
    }

    // ============================================================
    // Add / Update requests are in Activity (as you requested)
    // ============================================================
    private void submitAddVehicle(Map<String, String> payload) {
        if (payload == null) {
            toast(getString(R.string.general_error));
            return;
        }

        showProgressHUD();

        Type type = new TypeToken<BaseResponse<CustomerVehicleDto>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_POST,
                Endpoints.CUSTOMER_VEHICLES_ADD,
                payload,
                null,
                type,
                0,
                new ApiCallback<CustomerVehicleDto>() {
                    @Override
                    public void onSuccess(CustomerVehicleDto data, String msg, String rawJson) {
                        hideProgressHUD();
                        if (activeDialog != null) activeDialog.dismissAllowingStateLoss();
                        toast(msg != null && !msg.trim().isEmpty() ? msg : getString(R.string.done));
                        fetchVehicles(true); // refresh list
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
                        errorLogger("AddVehicleParseError", e.getMessage() == null ? "null" : e.getMessage());
                        toast(getString(R.string.general_error));
                    }
                }
        );
    }

    private void submitUpdateVehicle(Map<String, String> payload) {
        if (payload == null) {
            toast(getString(R.string.general_error));
            return;
        }

        showProgressHUD();

        Type type = new TypeToken<BaseResponse<CustomerVehicleDto>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_POST,
                Endpoints.CUSTOMER_VEHICLES_UPDATE,
                payload,
                null,
                type,
                0,
                new ApiCallback<CustomerVehicleDto>() {
                    @Override
                    public void onSuccess(CustomerVehicleDto data, String msg, String rawJson) {
                        hideProgressHUD();
                        if (activeDialog != null) activeDialog.dismissAllowingStateLoss();
                        toast(msg != null && !msg.trim().isEmpty() ? msg : getString(R.string.done));
                        fetchVehicles(true);
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
                        errorLogger("UpdateVehicleParseError", e.getMessage() == null ? "null" : e.getMessage());
                        toast(getString(R.string.general_error));
                    }
                }
        );
    }

    // ============================================================
    // Utils
    // ============================================================



}
