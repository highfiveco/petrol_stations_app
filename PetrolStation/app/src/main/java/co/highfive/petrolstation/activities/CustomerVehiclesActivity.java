package co.highfive.petrolstation.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.adapters.CustomerVehiclesAdapter;
import co.highfive.petrolstation.customers.dto.CustomerVehicleDto;
import co.highfive.petrolstation.customers.dto.CustomerVehiclesResponseDto;
import co.highfive.petrolstation.customers.dto.VehicleSettingsResponseDto;
import co.highfive.petrolstation.data.local.AppDatabase;
import co.highfive.petrolstation.data.local.DatabaseProvider;
import co.highfive.petrolstation.data.local.entities.CustomerEntity;
import co.highfive.petrolstation.data.local.entities.CustomerVehicleEntity;
import co.highfive.petrolstation.data.local.entities.CustomersMetaCacheEntity;
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

    private boolean canView = true;
    private boolean canAdd = true;
    private boolean canEdit = true;

    private VehicleSettingsResponseDto cachedSettings = null;
    private AddVehicleDialog activeDialog = null;

    private AppDatabase db;
    private ExecutorService dbExecutor;
    private Handler mainHandler;

    private static final String SESSION_KEY_VEHICLE_SETTINGS_JSON = "SESSION_KEY_VEHICLE_SETTINGS_JSON";
    private VehicleSettingsResponseDto cachedVehicleSettings = null;

    private boolean isOfflineCustomer = false;
    private long offlineCustomerLocalId = 0;
    private int customerIdInt = 0;

    private CustomerVehicleDto lastEditVehicleRef = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCustomerVehiclesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI(binding.mainLayout);

        db = DatabaseProvider.get(this);
        dbExecutor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        readExtras();
        initHeader();
        initRecycler();
        initRefresh();
        initClicks();

        fetchVehiclesSmart(true);
    }

    private void fetchVehiclesSmart(boolean showDialog) {
        if (connectionAvailable) {
            fetchVehiclesOnline(showDialog);
        } else {
            fetchVehiclesOffline(showDialog);
        }
    }

    private void readExtras() {
        Bundle extras = getIntent() != null ? getIntent().getExtras() : null;
        if (extras == null) return;

        isOfflineCustomer = extras.getBoolean("is_offline", false);
        String tempCustomerId = extras.getString("offline_local_id", null);
        if (tempCustomerId != null) {
            try { offlineCustomerLocalId = Long.parseLong(tempCustomerId); } catch (Exception ignored) {}
        }

        String cidStr = safe(extras.getString("customer_id"));
        if (cidStr.trim().isEmpty()) cidStr = safe(extras.getString("id"));
        try { customerIdInt = Integer.parseInt(cidStr); } catch (Exception ignored) { customerIdInt = 0; }

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

        binding.icAddWhite.setVisibility(View.GONE);
    }

    private void initClicks() {
        binding.icHome.setOnClickListener(v ->
                moveToActivity(getApplicationContext(), MainActivity.class, null, false, true)
        );
        binding.icBack.setOnClickListener(v -> finish());

        binding.icAddWhite.setOnClickListener(v -> {
            if (!canAdd) return;
            fetchVehicleSettingsThenOpenDialog(null);
        });
    }

    private void initRecycler() {
        adapter = new CustomerVehiclesAdapter(items, v -> {
            if (!canEdit) return;
            fetchVehicleSettingsThenOpenDialog(v);
        });

        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);
    }

    private void initRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> fetchVehiclesSmart(false));
    }

    private void fetchVehiclesOnline(boolean showDialog) {
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

                        binding.icAddWhite.setVisibility(canAdd ? View.VISIBLE : View.GONE);
                        adapter.setCanEdit(canEdit);

                        if (data.customer != null) {
                            String n = safe(data.customer.name);
                            String m = safe(data.customer.mobile);

                            if (!n.isEmpty()) binding.name.setText(n);
                            if (!m.isEmpty()) binding.phone.setText(m);

                            try { binding.amount.setText(formatNumber(data.customer.balance)); }
                            catch (Exception ignored) { binding.amount.setText(""); }
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

    private void fetchVehiclesOffline(boolean showDialog) {

        if (isOfflineCustomer) {
            if (offlineCustomerLocalId <= 0) {
                toast(getString(R.string.general_error));
                return;
            }
        } else {
            if (customerId.trim().isEmpty()) {
                toast(getString(R.string.general_error));
                return;
            }
        }

        if (showDialog) showProgressHUD();
        binding.swipeRefreshLayout.setRefreshing(!showDialog);

        final int cid;
        if (!isOfflineCustomer) {
            try { cid = Integer.parseInt(customerId); }
            catch (Exception e) {
                if (showDialog) hideProgressHUD();
                binding.swipeRefreshLayout.setRefreshing(false);
                toast(getString(R.string.general_error));
                return;
            }
        } else {
            cid = 0;
        }

        dbExecutor.execute(() -> {

            CustomersMetaCacheEntity meta = db.customersMetaDao().getOne();

            CustomerEntity customer = null;
            if (!isOfflineCustomer) {
                customer = db.customerDao().getById(cid);
            }
            final CustomerEntity finalCustomer = customer;

            ArrayList<CustomerVehicleDto> finalList = new ArrayList<>();

            if (!isOfflineCustomer) {
                List<CustomerVehicleEntity> onlineRows = db.customerVehicleDao().getByCustomer(cid);
                if (onlineRows != null) {
                    for (CustomerVehicleEntity ve : onlineRows) {
                        finalList.add(mapVehicleEntityToDto(ve));
                    }
                }

                List<co.highfive.petrolstation.data.local.entities.OfflineCustomerVehicleEntity> addedOffline =
                        db.offlineCustomerVehicleDao().getByCustomer(cid);

                if (addedOffline != null) {
                    for (co.highfive.petrolstation.data.local.entities.OfflineCustomerVehicleEntity oe : addedOffline) {
                        finalList.add(mapOfflineVehicleEntityToDto(oe));
                    }
                }

                List<co.highfive.petrolstation.data.local.entities.OfflineCustomerVehicleEditEntity> edits =
                        db.offlineCustomerVehicleEditDao().getByCustomer(cid);

                if (edits != null && !edits.isEmpty()) {
                    applyEditsOverlay(finalList, edits);
                }

            } else {
                List<co.highfive.petrolstation.data.local.entities.OfflineCustomerVehicleEntity> addedOffline =
                        db.offlineCustomerVehicleDao().getByOfflineCustomerLocalId(offlineCustomerLocalId);

                if (addedOffline != null) {
                    for (co.highfive.petrolstation.data.local.entities.OfflineCustomerVehicleEntity oe : addedOffline) {
                        finalList.add(mapOfflineVehicleEntityToDto(oe));
                    }
                }

                List<co.highfive.petrolstation.data.local.entities.OfflineCustomerVehicleEditEntity> edits =
                        db.offlineCustomerVehicleEditDao().getByOfflineCustomerLocalId(offlineCustomerLocalId);

                if (edits != null && !edits.isEmpty()) {
                    applyEditsOverlay(finalList, edits);
                }
            }

            mainHandler.post(() -> {
                if (showDialog) hideProgressHUD();
                binding.swipeRefreshLayout.setRefreshing(false);

                applyVehiclePermissionsFromMeta(meta);

                if (finalCustomer != null) {
                    String n = safe(finalCustomer.name);
                    String m = safe(finalCustomer.mobile);

                    if (!n.isEmpty()) binding.name.setText(n);
                    if (!m.isEmpty()) binding.phone.setText(m);

                    try { binding.amount.setText(formatNumber(finalCustomer.balance)); }
                    catch (Exception ignored) { binding.amount.setText(""); }
                }

                if (!canView) {
                    adapter.setItems(new ArrayList<>());
                    toast(getString(R.string.general_error));
                    return;
                }

                adapter.setItems(finalList);
            });
        });
    }

    private CustomerVehicleDto mapVehicleEntityToDto(CustomerVehicleEntity e) {
        CustomerVehicleDto v = new CustomerVehicleDto();
        v.id = e.id;
        v.customer_id = e.customerId;
        v.vehicle_number = e.vehicleNumber;
        v.vehicle_type = e.vehicleType;
        v.vehicle_color = e.vehicleColor;
        v.model = e.model;
        v.license_expiry_date = e.licenseExpiryDate;
        v.notes = e.notes;
        v.created_at = e.createdAt;
        v.vehicle_type_name = e.vehicleTypeName;
        v.vehicle_color_name = e.vehicleColorName;
        v.account_id = e.accountId;
        return v;
    }

    private CustomerVehicleDto mapOfflineVehicleEntityToDto(co.highfive.petrolstation.data.local.entities.OfflineCustomerVehicleEntity e) {
        CustomerVehicleDto v = new CustomerVehicleDto();
        v.id = (int) (-e.localId);
        v.customer_id = e.customerId;
        v.customer_local_id = e.offlineCustomerLocalId;
        v.vehicle_number = e.vehicleNumber;
        v.vehicle_type = e.vehicleType;
        v.vehicle_color = e.vehicleColor;
        v.model = e.model;
        v.license_expiry_date = e.licenseExpiryDate;
        v.notes = e.notes;
        v.vehicle_type_name = e.vehicleTypeName;
        v.vehicle_color_name = e.vehicleColorName;
        v.account_id = e.accountId != null ? e.accountId : 0;
        return v;
    }

    private void applyEditsOverlay(@NonNull ArrayList<CustomerVehicleDto> baseList,
                                   @NonNull List<co.highfive.petrolstation.data.local.entities.OfflineCustomerVehicleEditEntity> edits) {
        for (co.highfive.petrolstation.data.local.entities.OfflineCustomerVehicleEditEntity ed : edits) {
            if (ed == null) continue;
            for (int i = 0; i < baseList.size(); i++) {
                CustomerVehicleDto v = baseList.get(i);
                if (v == null) continue;

                boolean match = false;

                if (ed.targetOnlineVehicleId != null && ed.targetOnlineVehicleId > 0) {
                    match = (v.id == ed.targetOnlineVehicleId);
                } else if (ed.targetLocalVehicleId != null && ed.targetLocalVehicleId > 0) {
                    int localAsNegativeId = (int) (-ed.targetLocalVehicleId.longValue());
                    match = (v.id == localAsNegativeId);
                }

                if (!match) continue;

                if (ed.vehicleNumber != null) v.vehicle_number = ed.vehicleNumber;
                if (ed.model != null) v.model = ed.model;
                if (ed.licenseExpiryDate != null) v.license_expiry_date = ed.licenseExpiryDate;
                if (ed.notes != null) v.notes = ed.notes;

                if (ed.vehicleType != null) v.vehicle_type = ed.vehicleType;
                if (ed.vehicleColor != null) v.vehicle_color = ed.vehicleColor;

                if (ed.vehicleTypeName != null) v.vehicle_type_name = ed.vehicleTypeName;
                if (ed.vehicleColorName != null) v.vehicle_color_name = ed.vehicleColorName;

                baseList.set(i, v);
                break;
            }
        }
    }

    private void fetchVehicleSettingsThenOpenDialog(@Nullable CustomerVehicleDto editVehicle) {

        lastEditVehicleRef = editVehicle;

        if (cachedSettings != null) {
            cachedVehicleSettings = cachedSettings;
            openAddEditVehicleDialog(editVehicle, cachedSettings);
            return;
        }

        VehicleSettingsResponseDto fromSession = readVehicleSettingsFromSession();
        if (fromSession != null) {
            cachedSettings = fromSession;
            cachedVehicleSettings = fromSession;
            openAddEditVehicleDialog(editVehicle, cachedSettings);
            fetchVehicleSettingsFromApi(false, null);
            return;
        }

        fetchVehicleSettingsFromApi(true, () -> {
            if (cachedSettings != null) {
                cachedVehicleSettings = cachedSettings;
                openAddEditVehicleDialog(editVehicle, cachedSettings);
            } else {
                toast(getString(R.string.general_error));
            }
        });
    }

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
                            cachedVehicleSettings = data;
                        } else {
                            cachedSettings = readVehicleSettingsFromSession();
                            cachedVehicleSettings = cachedSettings;
                        }

                        if (onReady != null) onReady.run();
                    }

                    @Override
                    public void onError(ApiError error) {
                        if (showDialog) hideProgressHUD();

                        cachedSettings = readVehicleSettingsFromSession();
                        cachedVehicleSettings = cachedSettings;

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
                        cachedVehicleSettings = cachedSettings;

                        if (cachedSettings == null) toast(R.string.no_internet);

                        if (onReady != null) onReady.run();
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        if (showDialog) hideProgressHUD();

                        cachedSettings = readVehicleSettingsFromSession();
                        cachedVehicleSettings = cachedSettings;

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

            Type type = new TypeToken<BaseResponse<VehicleSettingsResponseDto>>() {}.getType();
            BaseResponse<VehicleSettingsResponseDto> base = getGson().fromJson(raw, type);
            if (base != null && base.status) return base.data;

        } catch (Exception ignored) {}
        return null;
    }

    private void openAddEditVehicleDialog(@Nullable CustomerVehicleDto editVehicle,
                                          @NonNull VehicleSettingsResponseDto settings) {

        activeDialog = AddVehicleDialog.newInstance(
                safe(customerId),
                editVehicle,
                settings
        );

        activeDialog.setCancelable(false);

        activeDialog.setListener(new AddVehicleDialog.Listener() {
            @Override
            public void onSubmitAdd(Map<String, String> payload) {
                submitAddVehicle(payload);
            }

            @Override
            public void onSubmitEdit(Map<String, String> payload) {
                submitUpdateVehicle(payload);
            }

            @Override
            public void onDismissed() {
            }
        });

        activeDialog.show(getSupportFragmentManager(), "AddVehicleDialog");
    }

    private void submitAddVehicle(Map<String, String> payload) {
        if (payload == null) {
            toast(getString(R.string.general_error));
            return;
        }

        if (!connectionAvailable) {
            saveVehicleOffline(payload);
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
                        fetchVehiclesSmart(true);
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

    private void saveVehicleOffline(@NonNull Map<String, String> payload) {

        showProgressHUD();

        dbExecutor.execute(() -> {
            try {
                long now = System.currentTimeMillis();

                co.highfive.petrolstation.data.local.entities.OfflineCustomerVehicleEntity e =
                        new co.highfive.petrolstation.data.local.entities.OfflineCustomerVehicleEntity();

                if (isOfflineCustomer) {
                    e.offlineCustomerLocalId = offlineCustomerLocalId;
                    e.customerId = 0;
                    e.accountId = null;
                } else {
                    e.customerId = Integer.parseInt(customerId);
                    try { e.accountId = Integer.parseInt(accountId); } catch (Exception ignored) { e.accountId = null; }
                }

                e.vehicleNumber = safe(payload.get("vehicle_number"));
                e.model = safe(payload.get("model"));
                e.licenseExpiryDate = safe(payload.get("license_expiry_date"));
                e.notes = safe(payload.get("notes"));

                try { e.vehicleType = Integer.parseInt(payload.get("vehicle_type")); } catch (Exception ignored) { e.vehicleType = null; }
                try { e.vehicleColor = Integer.parseInt(payload.get("vehicle_color")); } catch (Exception ignored) { e.vehicleColor = null; }

                try {
                    if (cachedVehicleSettings != null) {
                        if (e.vehicleType != null) {
                            e.vehicleTypeName = resolveSimpleSettingName(cachedVehicleSettings.vehicle_type, e.vehicleType);
                        }
                        if (e.vehicleColor != null) {
                            e.vehicleColorName = resolveSimpleSettingName(cachedVehicleSettings.vehicle_color, e.vehicleColor);
                        }
                    }
                } catch (Exception ignored) {}

                e.syncStatus = 0;
                e.syncError = null;
                e.createdAtTs = now;
                e.updatedAtTs = now;

                e.requestJson = null;

                long localId = db.offlineCustomerVehicleDao().insert(e);
                e.localId = localId;
                e.requestJson = buildOfflineVehicleRequestJson(e, payload);
                db.offlineCustomerVehicleDao().update(e);

                mainHandler.post(() -> {
                    hideProgressHUD();
                    if (activeDialog != null) {
                        activeDialog.dismissAllowingStateLoss();
                        activeDialog = null;
                    }
                    toast("تم حفظ المركبة (أوفلاين)");
                    fetchVehiclesOffline(false);
                });

            } catch (Exception ex) {
                mainHandler.post(() -> {
                    hideProgressHUD();
                    toast(getString(R.string.general_error));
                });
            }
        });
    }

    private void submitUpdateVehicle(Map<String, String> payload) {
        if (payload == null) {
            toast(getString(R.string.general_error));
            return;
        }

        if (!connectionAvailable) {
            saveVehicleEditOffline(payload, lastEditVehicleRef);
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
                        fetchVehiclesSmart(true);
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

    private void saveVehicleEditOffline(@NonNull Map<String, String> payload, @Nullable CustomerVehicleDto editedRef) {

        if (editedRef == null) {
            toast(getString(R.string.general_error));
            return;
        }

        showProgressHUD();

        dbExecutor.execute(() -> {
            try {
                long now = System.currentTimeMillis();

                co.highfive.petrolstation.data.local.entities.OfflineCustomerVehicleEditEntity ed =
                        new co.highfive.petrolstation.data.local.entities.OfflineCustomerVehicleEditEntity();

                if (isOfflineCustomer) {
                    ed.offlineCustomerLocalId = offlineCustomerLocalId;
                    ed.customerId = 0;
                } else {
                    try { ed.customerId = Integer.parseInt(customerId); } catch (Exception ignored) { ed.customerId = 0; }
                    ed.offlineCustomerLocalId = 0;
                }

                Integer onlineId = null;
                Long localId = null;

                if (editedRef.id > 0) {
                    onlineId = editedRef.id;
                } else if (editedRef.id < 0) {
                    localId = (long) (-editedRef.id);
                }

                ed.targetOnlineVehicleId = onlineId;
                ed.targetLocalVehicleId = localId;

                ed.vehicleNumber = safe(payload.get("vehicle_number"));
                ed.model = safe(payload.get("model"));
                ed.licenseExpiryDate = safe(payload.get("license_expiry_date"));
                ed.notes = safe(payload.get("notes"));

                try { ed.vehicleType = Integer.parseInt(payload.get("vehicle_type")); } catch (Exception ignored) { ed.vehicleType = null; }
                try { ed.vehicleColor = Integer.parseInt(payload.get("vehicle_color")); } catch (Exception ignored) { ed.vehicleColor = null; }

                try {
                    if (cachedVehicleSettings != null) {
                        if (ed.vehicleType != null) {
                            ed.vehicleTypeName = resolveSimpleSettingName(cachedVehicleSettings.vehicle_type, ed.vehicleType);
                        }
                        if (ed.vehicleColor != null) {
                            ed.vehicleColorName = resolveSimpleSettingName(cachedVehicleSettings.vehicle_color, ed.vehicleColor);
                        }
                    }
                } catch (Exception ignored) {}

                ed.syncStatus = 0;
                ed.syncError = null;
                ed.createdAtTs = now;
                ed.updatedAtTs = now;

                db.offlineCustomerVehicleEditDao().upsertByTarget(ed);

                mainHandler.post(() -> {
                    hideProgressHUD();
                    if (activeDialog != null) {
                        activeDialog.dismissAllowingStateLoss();
                        activeDialog = null;
                    }
                    toast("تم تعديل المركبة (أوفلاين)");
                    fetchVehiclesOffline(false);
                });

            } catch (Exception ex) {
                mainHandler.post(() -> {
                    hideProgressHUD();
                    toast(getString(R.string.general_error));
                });
            }
        });
    }

    private String buildOfflineVehicleRequestJson(
            co.highfive.petrolstation.data.local.entities.OfflineCustomerVehicleEntity e,
            Map<String, String> payload
    ) {
        try {
            Map<String, Object> root = new HashMap<>();
            root.put("local_id", e.localId);
            root.put("customer_id", e.customerId);
            root.put("offline_customer_local_id", e.offlineCustomerLocalId);
            root.put("payload", payload);
            return getGson().toJson(root);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String resolveSimpleSettingName(List<co.highfive.petrolstation.customers.dto.SimpleSettingDto> list, Integer id) {
        if (list == null || id == null) return "";
        for (co.highfive.petrolstation.customers.dto.SimpleSettingDto s : list) {
            if (s != null && s.id == id) return s.name != null ? s.name : "";
        }
        return "";
    }

    private void applyVehiclePermissionsFromMeta(@Nullable CustomersMetaCacheEntity meta) {
        if (meta == null) {
            canView = true;
            canAdd = false;
            canEdit = false;
        } else {
            canView = safeInt(meta.viewCustomerVehicles) == 1;
            canAdd = safeInt(meta.addCustomerVehicles) == 1;
            canEdit = safeInt(meta.editCustomerVehicles) == 1;
        }

        binding.icAddWhite.setVisibility(canAdd ? View.VISIBLE : View.GONE);
        if (adapter != null) adapter.setCanEdit(canEdit);
    }
}
