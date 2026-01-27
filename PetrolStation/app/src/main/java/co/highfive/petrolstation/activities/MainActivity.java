package co.highfive.petrolstation.activities;

import android.os.Bundle;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.adapters.MainAdapter;
import co.highfive.petrolstation.customers.dto.InvoiceDetailDto;
import co.highfive.petrolstation.customers.dto.VehicleSettingsResponseDto;
import co.highfive.petrolstation.data.local.entities.AccountEntity;
import co.highfive.petrolstation.data.local.entities.CampaignEntity;
import co.highfive.petrolstation.data.local.entities.InvoiceEntity;
import co.highfive.petrolstation.data.local.entities.ItemEntity;
import co.highfive.petrolstation.data.local.entities.ItemsCacheEntity;
import co.highfive.petrolstation.data.local.entities.PosItemCacheEntity;
import co.highfive.petrolstation.data.local.entities.PumpEntity;
import co.highfive.petrolstation.databinding.ActivityMainBinding;
import co.highfive.petrolstation.fuelsale.dto.FuelPriceSettingsData;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.models.AppData;
import co.highfive.petrolstation.models.MainItemView;
import co.highfive.petrolstation.models.User;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.ApiError;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;
import co.highfive.petrolstation.pos.data.PosItemsResponseData;
import co.highfive.petrolstation.pos.dto.PosItemDto;
import co.highfive.petrolstation.pos.dto.PosSettingsData;
import co.highfive.petrolstation.settings.dto.CompanySettingData;

import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.FragmentManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import co.highfive.petrolstation.data.local.AppDatabase;
import co.highfive.petrolstation.data.local.DatabaseProvider;
import co.highfive.petrolstation.data.local.entities.CompanySettingCacheEntity;
import co.highfive.petrolstation.data.local.entities.GetSettingCacheEntity;
import co.highfive.petrolstation.data.local.entities.CustomerEntity;
import co.highfive.petrolstation.data.local.entities.CustomersMetaCacheEntity;
import co.highfive.petrolstation.data.local.entities.CustomerVehicleEntity;
import co.highfive.petrolstation.data.local.entities.FuelSaleEntity;
import co.highfive.petrolstation.data.local.entities.InvoiceDetailEntity;

import co.highfive.petrolstation.fragments.RefreshDataDialog;
import co.highfive.petrolstation.network.RefreshDataResponseData;

// مهم: DTOs
import co.highfive.petrolstation.customers.dto.CustomerDto;
import co.highfive.petrolstation.customers.dto.CustomerVehicleDto;
import co.highfive.petrolstation.customers.dto.InvoiceDto;
import co.highfive.petrolstation.vehicles.dto.CustomerVehiclesSettingsData;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;
    private MainAdapter adapter;

    private AppDatabase db;
    private java.util.concurrent.ExecutorService dbExecutor;
    private android.os.Handler mainHandler;

    private RefreshDataDialog refreshDialog;
    private boolean isRefreshingNow = false;

    private static final int CUSTOMERS_PER_PAGE = 50;

    private int currentCustomersPage = 0;
    private static final int ITEMS_PER_PAGE = 20; // حسب JSON per_page
    private int currentItemsPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setupUI(binding.getRoot());

        db = DatabaseProvider.get(this);
        dbExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();
        mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Boolean> future = executor.submit(() -> {
                errorLogger("customerDao_size",""+db.customerDao().getAll().size());

                return true;
            });
        } catch (Exception e) {
            // تعامل مع الخطأ حسب حاجتك
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }

        initHeader();
        initRecycler();
        initRefresh();
    }


    private void initHeader() {
        User user = getGson().fromJson(
                getSessionManager().getString(getSessionKeys().userJson),
                User.class
        );

        if (user != null) {
            binding.name.setText(user.getName());
            binding.phone.setText(user.getMobile());
            binding.companyName.setText(user.getCompany_name());
        }

        binding.icNotification.setOnClickListener(v -> moveToActivity(this, NotificationsActivity.class, null, false));
        binding.icHome.setOnClickListener(v -> toast("Home clicked"));
        binding.icBack.setOnClickListener(v -> logout());
    }

    private void initRecycler() {
        adapter = new MainAdapter(new ArrayList<>(), this, () -> refreshOfflineData());
        binding.loadMoreView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.loadMoreView.setAdapter(adapter);

        AppData appData = getGson().fromJson(getSessionManager().getString(getSessionKeys().app_data), AppData.class);

        errorLogger("appData",""+appData.toString());

        ArrayList<MainItemView> list = new ArrayList<>();
        if(appData.getCustomers() == 1){
            list.add(new MainItemView(1, R.string.customers, R.drawable.rounded_customer_bg, R.drawable.ic_customers_white));
        }
        if(appData.getFinancial() == 1){
            list.add(new MainItemView(2, R.string.finance, R.drawable.rounded_finance_bg, R.drawable.ic_finance_white));
        }
        if(appData.getFund_financial() == 1){
            list.add(new MainItemView(3, R.string.finance_funds, R.drawable.rounded_fincancial_funds_bg, R.drawable.ic_fincancial_funds_homw_white));
        }
//        list.add(new MainItemView(4, R.string.monthly_readings, R.drawable.rounded_monthly_reading_bg, R.drawable.ic_monthly_reading_white));
//        list.add(new MainItemView(5, R.string.expelled_readings, R.drawable.rounded_expelled_bg, R.drawable.ic_expelled_white));
        list.add(new MainItemView(6, R.string.notifications, R.drawable.rounded_monthly_reading_bg, R.drawable.ic_notififcation_white_home));
        if(appData.getPos() == 1){
            list.add(new MainItemView(7, R.string.pos, R.drawable.rounded_notifications_bg, R.drawable.ic_pos));
        }
        if(appData.getFuel_sales() == 1){
            list.add(new MainItemView(8, R.string.fule_sale, R.drawable.rounded_customer_bg, R.drawable.ic_fule_sale));
        }
        if(appData.getView_invoices() == 1){
            list.add(new MainItemView(9, R.string.invoices, R.drawable.rounded_monthly_reading_bg, R.drawable.ic_invoices));
        }

//        list.add(new MainItemView(11, R.string.maintenances, R.drawable.rounded_maintenance_bg, R.drawable.ic_maintenances_white));
//        list.add(new MainItemView(12, R.string.collection, R.drawable.rounded_collection_bg, R.drawable.ic_collection_white));
        list.add(new MainItemView(13, R.string.refresh_data, R.drawable.rounded_finance_bg, R.drawable.ic_refresh));
        list.add(new MainItemView(14, R.string.sync_data, R.drawable.rounded_fincancial_funds_bg, R.drawable.ic_sync));
        list.add(new MainItemView(10, R.string.about, R.drawable.rounded_about_bg, R.drawable.ic_about_white));

        adapter.updateData(list);
    }

    private void refreshOfflineData() {
        if (isRefreshingNow) return;

        if (!connectionAvailable) {
            toast("No internet connection");
            return;
        }

        isRefreshingNow = true;

        refreshDialog = new RefreshDataDialog();
        refreshDialog.setCancelable(false);
        refreshDialog.show(getSupportFragmentManager(), "refresh_data_dialog");

        // أول خطوة: GET Setting
        requestGetSettingThenCompanyThenCustomers();
    }

    private void requestGetSettingThenCompanyThenCustomers() {

        refreshDialog.showUpdateSetting();

        // الأفضل: قبل الريكوست اعمل loader، وبعدها result.
        // حسب ديلوجك: عندك changeUpdateSetting(status) بدون show loader.
        // إذا بدك loader قبل الطلب: أضف method showUpdateSetting() في الديلوج (زي الباقي).
        // حالياً نكمل بدون.

        Type type = new TypeToken<BaseResponse<co.highfive.petrolstation.settings.dto.GetSettingData>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.GET_SETTING,
                (ApiClient.ApiParams) null,
                null,
                type,
                0,
                new ApiCallback<co.highfive.petrolstation.settings.dto.GetSettingData>() {

                    @Override
                    public void onSuccess(co.highfive.petrolstation.settings.dto.GetSettingData data, String message, String rawJson) {

                        getSessionManager().setString(getSessionKeys().get_setting_json, rawJson);


                        dbExecutor.execute(() -> {
                            GetSettingCacheEntity e = new GetSettingCacheEntity();
                            e.id = 1;
                            e.json = rawJson; // نخزن الريسبونس كما هو
                            e.updatedAt = System.currentTimeMillis();
                            db.getSettingCacheDao().upsert(e);

                            mainHandler.post(() -> {
                                refreshDialog.changeUpdateSetting(true);
                                requestCompanySetting();
                            });
                        });
                    }

                    @Override public void onError(ApiError error) {
                        mainHandler.post(() -> {
                            if (refreshDialog != null && refreshDialog.isAdded()) {
                                refreshDialog.changeUpdateCompanySetting(false);
                            }
                            finishRefresh();
                        });
                    }

                    @Override public void onUnauthorized(String rawJson) {
                        logout();
                    }

                    @Override public void onNetworkError(String reason) {
                        mainHandler.post(() -> {
                            if (refreshDialog != null && refreshDialog.isAdded()) {
                                refreshDialog.changeUpdateCompanySetting(false);
                            }
                            finishRefresh();
                        });
                    }

                    @Override public void onParseError(String rawJson, Exception e) {
                        mainHandler.post(() -> {
                            if (refreshDialog != null && refreshDialog.isAdded()) {
                                refreshDialog.changeUpdateCompanySetting(false);
                            }
                            finishRefresh();
                        });
                    }
                }
        );
    }

    private void requestCompanySetting() {

        refreshDialog.showUpdateCompanySetting();

        int userSanad = getUserSanadFromSession();
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

                    @Override
                    public void onSuccess(CompanySettingData data, String message, String rawJson) {

                        getSessionManager().setString(getSessionKeys().company_setting_json, rawJson);

                        dbExecutor.execute(() -> {
                            CompanySettingCacheEntity e = new CompanySettingCacheEntity();
                            e.id = 1;
                            e.json = rawJson;
                            e.updatedAt = System.currentTimeMillis();
                            db.companySettingCacheDao().upsert(e);

                            mainHandler.post(() -> {
//                                refreshDialog.changeUpdateCompanySetting(true);
                                requestPosSettings();
                            });
                        });
                    }

                    @Override public void onError(co.highfive.petrolstation.network.ApiError error) {
                        refreshDialog.changeUpdateCompanySetting(false);
                        finishRefresh();
                    }

                    @Override public void onUnauthorized(String rawJson) {
                        logout();
                    }

                    @Override public void onNetworkError(String reason) {
                        refreshDialog.changeUpdateCompanySetting(false);
                        finishRefresh();
                    }

                    @Override public void onParseError(String rawJson, Exception e) {
                        refreshDialog.changeUpdateCompanySetting(false);
                        finishRefresh();
                    }
                }
        );
    }

    private void requestPosSettings() {
        // إذا عندك UI بالديالوج، أضف:
        // refreshDialog.showUpdatePosSettings();

        Type type = new TypeToken<BaseResponse<PosSettingsData>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                "/api/pos/settings",
                (Map<String, String>) null,
                null,
                type,
                0,
                new ApiCallback<PosSettingsData>() {

                    @Override
                    public void onSuccess(PosSettingsData data, String message, String rawJson) {

                        // خزّن في Session (الكاش الأساسي المطلوب)
                        getSessionManager().setString(getSessionKeys().pos_settings_json, rawJson);

                        // إذا بدك كمان Room Cache مثل الباقي لاحقاً (اختياري) ممكن تضيف DAO خاص

                        mainHandler.post(() -> {
                            // إذا عندك ديالوج status:
                            // refreshDialog.changeUpdatePosSettings(true);

                            requestFuelPriceSettings();
                        });
                    }

                    @Override public void onError(ApiError error) {
                        // refreshDialog.changeUpdatePosSettings(false);
                        finishRefresh();
                    }

                    @Override public void onUnauthorized(String rawJson) { logout(); }

                    @Override public void onNetworkError(String reason) {
                        // refreshDialog.changeUpdatePosSettings(false);
                        finishRefresh();
                    }

                    @Override public void onParseError(String rawJson, Exception e) {
                        // refreshDialog.changeUpdatePosSettings(false);
                        finishRefresh();
                    }
                }
        );
    }

    private void requestFuelPriceSettings() {
        // refreshDialog.showUpdateFuelSettings();

        Type type = new TypeToken<BaseResponse<FuelPriceSettingsData>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.FUEL_PRICE_SETTINGS, // أو "/api/fuel-price/settings" حسب Endpoints عندك
                (ApiClient.ApiParams) null,
                null,
                type,
                0,
                new ApiCallback<FuelPriceSettingsData>() {

                    @Override
                    public void onSuccess(FuelPriceSettingsData data, String message, String rawJson) {

                        // خزّن في Session
                        getSessionManager().setString(getSessionKeys().fuel_price_settings_json, rawJson);

                        mainHandler.post(() -> {
//                            refreshDialog.changeUpdateCompanySetting(true);

                            requestCustomerVehiclesSettings();
                        });
                    }

                    @Override public void onError(ApiError error) {
                        // refreshDialog.changeUpdateFuelSettings(false);
                        finishRefresh();
                    }

                    @Override public void onUnauthorized(String rawJson) { logout(); }

                    @Override public void onNetworkError(String reason) {
                        // refreshDialog.changeUpdateFuelSettings(false);
                        finishRefresh();
                    }

                    @Override public void onParseError(String rawJson, Exception e) {
                        // refreshDialog.changeUpdateFuelSettings(false);
                        finishRefresh();
                    }
                }
        );
    }

    private void requestCustomerVehiclesSettings() {
        // refreshDialog.showUpdateFuelSettings();

        Type type = new TypeToken<BaseResponse<VehicleSettingsResponseDto>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.CUSTOMER_VEHICLES_SETTINGS,
                (ApiClient.ApiParams) null,
                null,
                type,
                0,
                new ApiCallback<VehicleSettingsResponseDto>() {

                    @Override
                    public void onSuccess(VehicleSettingsResponseDto data, String message, String rawJson) {

                        errorLogger("rawJson",""+rawJson);
                        errorLogger("data_model",""+data.model.size());
                        errorLogger("data_vehicle_color",""+data.vehicle_color.size());
                        // خزّن في Session
                        getSessionManager().setString(getSessionKeys().customer_vehicles_settings_json, rawJson);

                        mainHandler.post(() -> {
                            refreshDialog.changeUpdateCompanySetting(true);

                            requestCustomersOfflineAllPages();
                        });
                    }

                    @Override public void onError(ApiError error) {
                        // refreshDialog.changeUpdateFuelSettings(false);
                        finishRefresh();
                    }

                    @Override public void onUnauthorized(String rawJson) { logout(); }

                    @Override public void onNetworkError(String reason) {
                        // refreshDialog.changeUpdateFuelSettings(false);
                        finishRefresh();
                    }

                    @Override public void onParseError(String rawJson, Exception e) {
                        // refreshDialog.changeUpdateFuelSettings(false);
                        finishRefresh();
                    }
                }
        );
    }

    private void requestCustomersOfflineAllPages() {
        if (refreshDialog != null && refreshDialog.isAdded()) {
            refreshDialog.showUpdateCustomers();
        }

        // إذا بدك تمسح القديم قبل التحديث:
        // dbExecutor.execute(() -> {
        //     db.invoiceDetailDao().deleteAll();
        //     db.invoiceDao().deleteAll();
        //     db.customerVehicleDao().deleteAll();
        //     db.customerDao().deleteAll();
        // });

        fetchCustomersOfflinePage(1);
    }

    private void fetchCustomersOfflinePage(int page) {
        if (page <= currentCustomersPage) return;
        currentCustomersPage = page;

        ApiClient.ApiParams params = new ApiClient.ApiParams()
                .add("per_page", CUSTOMERS_PER_PAGE)
                .add("page", page);

        Type type = new TypeToken<BaseResponse<RefreshDataResponseData>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.CUSTOMERS_OFFLINE,
                params,
                null,
                type,
                0,
                new ApiCallback<RefreshDataResponseData>() {

                    @Override
                    public void onSuccess(RefreshDataResponseData data, String message, String rawJson) {

                        if (data == null || data.getCustomers() == null || data.getCustomers().isEmpty()) {
                            mainHandler.post(() -> {
                                if (refreshDialog != null && refreshDialog.isAdded()) refreshDialog.changeCustomers(true);

                                // ✅ بعد ما خلصت صفحات الزبائن -> ابدأ بالـ Items
                                requestItemsOfflineAllPages();
                            });
                            return;
                        }

                        dbExecutor.execute(() -> {
                            try {
                                saveCustomersPageToRoom(data);

                                mainHandler.post(() -> fetchCustomersOfflinePage(page + 1));

                            } catch (Exception ex) {
                                mainHandler.post(() -> {
                                    refreshDialog.changeCustomers(false);
                                    finishRefresh();
                                });
                            }
                        });
                    }

                    @Override public void onError(co.highfive.petrolstation.network.ApiError error) {
                        refreshDialog.changeCustomers(false);
                        finishRefresh();
                    }

                    @Override public void onUnauthorized(String rawJson) {
                        logout();
                    }

                    @Override public void onNetworkError(String reason) {
                        refreshDialog.changeCustomers(false);
                        finishRefresh();
                    }

                    @Override public void onParseError(String rawJson, Exception e) {
                        refreshDialog.changeCustomers(false);
                        finishRefresh();
                    }
                }
        );
    }
    private void requestItemsOfflineAllPages() {
        currentItemsPage = 0;

        if (refreshDialog != null && refreshDialog.isAdded()) {
            refreshDialog.showUpdateItems();
        }

        fetchItemsOfflinePage(1);
    }

    private void fetchItemsOfflinePage(int page) {
        if (page <= currentItemsPage) return;
        currentItemsPage = page;

        ApiClient.ApiParams params = new ApiClient.ApiParams()
                .add("page", page); // فقط pagination - بدون category/name

        Type type = new TypeToken<BaseResponse<PosItemsResponseData>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                "/api/pos/items",
                params,
                null,
                type,
                0,
                new ApiCallback<PosItemsResponseData>() {

                    @Override
                    public void onSuccess(PosItemsResponseData data, String message, String rawJson) {

                        if (data == null || data.items == null || data.items.data == null || data.items.data.isEmpty()) {
                            mainHandler.post(() -> {
                                if (refreshDialog != null && refreshDialog.isAdded()) refreshDialog.changeUpdateItems(true);
                                finishRefresh();
                            });
                            return;
                        }

                        dbExecutor.execute(() -> {
                            try {
                                saveItemsPageToRoom(data);

                                int lastPage = data.items.last_page;
                                if (page < lastPage) {
                                    mainHandler.post(() -> fetchItemsOfflinePage(page + 1));
                                } else {
                                    mainHandler.post(() -> {
                                        if (refreshDialog != null && refreshDialog.isAdded()) refreshDialog.changeUpdateItems(true);
                                        finishRefresh();
                                    });
                                }

                            } catch (Exception e) {
                                mainHandler.post(() -> {
                                    if (refreshDialog != null && refreshDialog.isAdded()) refreshDialog.changeUpdateItems(false);
                                    finishRefresh();
                                });
                            }
                        });
                    }

                    @Override public void onError(ApiError error) {
                        if (refreshDialog != null && refreshDialog.isAdded()) refreshDialog.changeUpdateItems(false);
                        finishRefresh();
                    }

                    @Override public void onUnauthorized(String rawJson) { logout(); }

                    @Override public void onNetworkError(String reason) {
                        if (refreshDialog != null && refreshDialog.isAdded()) refreshDialog.changeUpdateItems(false);
                        finishRefresh();
                    }

                    @Override public void onParseError(String rawJson, Exception e) {
                        if (refreshDialog != null && refreshDialog.isAdded()) refreshDialog.changeUpdateItems(false);
                        finishRefresh();
                    }
                }
        );
    }

    private void saveItemsPageToRoom(PosItemsResponseData data) {

        ArrayList<ItemsCacheEntity> cacheItems = new ArrayList<>();
        long now = System.currentTimeMillis();

        for (PosItemDto it : data.items.data) {
            ItemsCacheEntity e = new ItemsCacheEntity();
            e.id = it.id;
            e.name = it.name;
            e.category = it.category;
            e.negativeCheck = it.negative_check;
            e.price = it.price;
            e.barcode = it.barcode;
            e.icon = it.icon;
//            e.updatedAt = now;
            cacheItems.add(e);
        }

        if (!cacheItems.isEmpty()) {
            db.itemsCacheDao().upsertAll(cacheItems);
        }
    }


    private void saveCustomersPageToRoom(RefreshDataResponseData data) {

        ArrayList<CustomerEntity> customers = new ArrayList<>();
        ArrayList<CustomerVehicleEntity> vehicles = new ArrayList<>();

        ArrayList<InvoiceEntity> invoices = new ArrayList<>();
        ArrayList<InvoiceDetailEntity> details = new ArrayList<>();

        ArrayList<ItemEntity> items = new ArrayList<>();
        ArrayList<AccountEntity> accounts = new ArrayList<>();
        ArrayList<PumpEntity> pumps = new ArrayList<>();
        ArrayList<CampaignEntity> campaigns = new ArrayList<>();

        long now = System.currentTimeMillis();

        for (CustomerDto c : data.getCustomers()) {

            CustomerEntity ce = new CustomerEntity();
            ce.id = c.id;
            ce.typeCustomer = c.type_customer;
            ce.customerClassify = c.customer_classify;
            ce.name = c.name;
            ce.accountId = c.account_id;
            ce.mobile = c.mobile;
            ce.status = c.status;
            ce.customerStatus = c.customer_status;
            ce.customerClassifyName = c.customer_classify_name;
            ce.typeCustomerName = c.type_customer_name;
            ce.balance = c.balance;
            ce.campaignName = c.campaign_name;
            ce.remainingAmount = c.remaining_amount;
            ce.address = c.address;
            ce.assealNo = c.asseal_no;

            customers.add(ce);

            if (c.vehicles != null) {
                for (CustomerVehicleDto v : c.vehicles) {
                    CustomerVehicleEntity ve = new CustomerVehicleEntity();
                    ve.id = v.id;
                    ve.customerId = (v.customer_id != null ? v.customer_id : c.id);
                    ve.vehicleNumber = v.vehicle_number;
                    ve.vehicleType = v.vehicle_type;
                    ve.vehicleColor = v.vehicle_color;
                    ve.model = v.model;
                    ve.licenseExpiryDate = v.license_expiry_date;
                    ve.notes = v.notes;
                    ve.createdAt = v.created_at;
                    ve.vehicleTypeName = v.vehicle_type_name;
                    ve.vehicleColorName = v.vehicle_color_name;
                    ve.accountId = v.account_id;
                    vehicles.add(ve);
                }
            }

            collectInvoicesOfCustomer(c.id, 0, c.invoices, invoices, details, items, accounts, pumps, campaigns, now);
            collectInvoicesOfCustomer(c.id, 1, c.fuel_invoices, invoices, details, items, accounts, pumps, campaigns, now);
        }

        db.customerDao().upsertAll(customers);

        if (!vehicles.isEmpty()) db.customerVehicleDao().upsertAll(vehicles);
        if (!accounts.isEmpty()) db.accountDao().upsertAll(accounts);
        if (!pumps.isEmpty()) db.pumpDao().upsertAll(pumps);
        if (!campaigns.isEmpty()) db.campaignDao().upsertAll(campaigns);
        if (!items.isEmpty()) db.itemDao().upsertAll(items);

        if (!invoices.isEmpty()) db.invoiceDao().upsertAll(invoices);
        if (!details.isEmpty()) db.invoiceDetailDao().upsertAll(details);

        CustomersMetaCacheEntity meta = new CustomersMetaCacheEntity();
        meta.id = 1;
        meta.sms = data.getSms();
        meta.viewLog = data.getViewLog();
        meta.viewFinancialMove = data.getViewFinancialMove();
        meta.updateCustomers = data.getUpdateCustomers();
        meta.addCustomers = data.getAddCustomers();
        meta.viewReminders = data.getViewReminders();
        meta.addReminders = data.getAddReminders();
        meta.deleteReminders = data.getDeleteReminders();
        meta.updateMobile = data.getUpdateMobile();
        meta.viewCustomerVehicles = data.getViewCustomerVehicles();
        meta.addCustomerVehicles = data.getAddCustomerVehicles();
        meta.editCustomerVehicles = data.getEditCustomerVehicles();
        meta.settingJson = (data.getSetting() != null) ? getGson().toJson(data.getSetting()) : null;
        meta.updatedAt = System.currentTimeMillis();

        db.customersMetaDao().upsert(meta);
    }


    private void collectInvoicesOfCustomer(
            int customerId,
            int isFuelSale,
            List<InvoiceDto> source,
            ArrayList<InvoiceEntity> invoices,
            ArrayList<InvoiceDetailEntity> details,
            ArrayList<ItemEntity> items,
            ArrayList<AccountEntity> accounts,
            ArrayList<PumpEntity> pumps,
            ArrayList<CampaignEntity> campaigns,
            long now
    ) {
        if (source == null) return;

        for (InvoiceDto inv : source) {

            // ===== account object -> accounts table
            if (inv.account != null) {
                try {
                    // افتراض: Account فيه id + name (إذا اسم الحقل مختلف خبرني)
                    AccountEntity ae = new AccountEntity();
                    ae.id = Integer.parseInt(inv.account.getId());
                    ae.name = inv.account.getName();
                    accounts.add(ae);
                } catch (Exception ignore) {}
            }

            // ===== pump object -> pumps table
            if (inv.getPump() != null) {
                PumpEntity pe = new PumpEntity();
                pe.id = inv.getPump().id;
                pe.name = inv.getPump().name;
                pumps.add(pe);
            }

            // ===== campaign object -> campaigns table
            if (inv.getCampaign() != null) {
                CampaignEntity ce = new CampaignEntity();
                ce.id = inv.getCampaign().id;
                ce.name = inv.getCampaign().name;
                campaigns.add(ce);
            }

            InvoiceEntity ie = new InvoiceEntity();
            ie.id = inv.id;
            ie.customerId = customerId;
            ie.date = inv.date;
            ie.statement = inv.statement;
            ie.accountId = inv.account_id;
            ie.storeId = inv.store_id;
            ie.discount = inv.discount;
            ie.total = inv.total;
            ie.invoiceNo = inv.invoice_no;
            ie.notes = inv.notes;
            ie.campaignId = inv.campaign_id;
            ie.pumpId = inv.pump_id;
            ie.customerVehicleId = inv.customer_vehicle_id;
            ie.isFuelSale = (isFuelSale == 1) ? 1 : 0;
            ie.createdAt = inv.created_at;
            ie.updatedAt = now;

            invoices.add(ie);

            if (inv.details != null) {
                for (InvoiceDetailDto d : inv.details) {

                    // item object -> items table
                    if (d.item != null) {
                        ItemEntity it = new ItemEntity();
                        it.id = d.item.id;
                        it.categoryId = 0; // غير موجود بالريسبونس
                        it.name = d.item.name;
                        it.negativeCheck = d.item.negative_check;
                        it.price = d.item.price;
                        it.barcode = d.item.barcode;
                        it.icon = d.item.icon;
                        items.add(it);
                    }

                    InvoiceDetailEntity de = new InvoiceDetailEntity();
                    de.id = d.id;
                    de.invoiceId = inv.id;
                    de.itemId = d.item_id;
                    de.updateCostPrice = d.update_cost_price;
                    de.count = d.count;
                    de.price = d.price;

                    details.add(de);
                }
            }
        }
    }




    private void finishRefresh() {
        currentCustomersPage = 0;


        isRefreshingNow = false;

        if (refreshDialog != null) {
            refreshDialog.activateCloseButton();
        }

        // هنا تقدر تعمل reload للشاشة من DB بدل Session
        mainHandler.post(() -> initRecycler());
    }



    private void initRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            if(connectionAvailable){
                int userSanad = getUserSanadFromSession();
                loadCompanySetting(userSanad,binding.swipeRefreshLayout);
            }else{
                try{
                    binding.swipeRefreshLayout.setRefreshing(false);
                }catch (Exception e){

                }
            }

        });
    }

    private void loadCompanySetting(int userSanad, SwipeRefreshLayout swipeRefreshLayout) {

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
                            errorLogger("data",""+data.toString());
                            String dataJson = getGson().toJson(data);
                            getSessionManager().setString(getSessionKeys().app_data, dataJson);
                        }

                        swipeRefreshLayout.setRefreshing(false);
                        initRecycler();
                    }

                    @Override public void onError(co.highfive.petrolstation.network.ApiError error) {
                        hideProgressHUD();
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override public void onUnauthorized(String rawJson) {
                        hideProgressHUD();
                        logout();
                    }

                    @Override public void onNetworkError(String reason) {
                        hideProgressHUD();
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override public void onParseError(String rawJson, Exception e) {
                        hideProgressHUD();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );
    }

}
