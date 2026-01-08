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
import co.highfive.petrolstation.databinding.ActivityMainBinding;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.models.AppData;
import co.highfive.petrolstation.models.MainItemView;
import co.highfive.petrolstation.models.User;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;
import co.highfive.petrolstation.settings.dto.CompanySettingData;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;
    private MainAdapter adapter;
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setupUI(binding.getRoot());

        initApiClient();
        initHeader();
        initRecycler();
        initRefresh();
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
        adapter = new MainAdapter(new ArrayList<>(), this);
        binding.loadMoreView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.loadMoreView.setAdapter(adapter);

        AppData appData = getGson().fromJson(getSessionManager().getString(getSessionKeys().app_data), AppData.class);


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
