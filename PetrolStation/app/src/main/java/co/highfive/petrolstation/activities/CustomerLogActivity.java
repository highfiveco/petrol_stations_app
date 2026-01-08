package co.highfive.petrolstation.activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.adapters.CustomerLogAdapter;
import co.highfive.petrolstation.customers.dto.CustomerLogResponse;
import co.highfive.petrolstation.databinding.ActivityCustomerLogBinding;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.models.Account;
import co.highfive.petrolstation.models.CustomerLog;
import co.highfive.petrolstation.models.Setting;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.ApiError;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;

import com.google.gson.reflect.TypeToken;

public class CustomerLogActivity extends BaseActivity {

    private ActivityCustomerLogBinding binding;

    private String id = "";

    private Account account;
    private Setting setting;

    private CustomerLogAdapter adapter;

    private int page = 1;
    private boolean isLoading = false;
    private boolean hasMore = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCustomerLogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI(binding.mainLayout);

        readExtras();
        initRecycler();
        initRefresh();
        initClicks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshFirstPage(true);
    }

    private void readExtras() {
        Bundle extras = getIntent() != null ? getIntent().getExtras() : null;
        if (extras == null) return;
        id = safe(extras.getString("id"));
    }

    private void initRecycler() {
        adapter = new CustomerLogAdapter(new ArrayList<>());
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);

        binding.recycler.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@androidx.annotation.NonNull androidx.recyclerview.widget.RecyclerView rv, int dx, int dy) {
                if (dy <= 0) return;
                if (isLoading || !hasMore) return;

                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                if (lm == null) return;

                int lastVisible = lm.findLastVisibleItemPosition();
                if (lastVisible >= adapter.getItemCount() - 3) {
                    loadNextPage();
                }
            }
        });
    }

    private void initRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> refreshFirstPage(false));
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
    }

    private void refreshFirstPage(boolean showDialog) {
        page = 1;
        hasMore = true;
        adapter.clear();
        fetchPage(page, showDialog);
    }

    private void loadNextPage() {
        if (!hasMore) return;
        fetchPage(page + 1, false);
    }

    private void fetchPage(int targetPage, boolean showDialog) {
        if (id.trim().isEmpty()) {
            toast(getString(R.string.general_error));
            return;
        }

        isLoading = true;
        if (showDialog) showProgressHUD();

        Map<String, String> params = ApiClient.mapOf(
                "page", String.valueOf(targetPage),
                "id", id
        );

        Type type = new TypeToken<BaseResponse<CustomerLogResponse>>() {}.getType();

        apiClient.request(
                co.highfive.petrolstation.hazemhamadaqa.Http.Constant.REQUEST_GET,
                Endpoints.CUSTOMERS_LOG, // TODO endpoint عندكم
                params,
                null,
                type,
                0,
                new ApiCallback<CustomerLogResponse>() {
                    @Override
                    public void onSuccess(CustomerLogResponse data, String msg, String rawJson) {
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        isLoading = false;

                        if (data == null) {
                            toast(getString(R.string.general_error));
                            return;
                        }

                        account = data.customer;
                        setting = data.setting;

                        updateHeader(account);

                        List<CustomerLog> newItems = (data.logs != null && data.logs.data != null)
                                ? data.logs.data
                                : new ArrayList<>();

                        // pagination: اعتمد next_page_url / current_page last_page
                        boolean noNext = (data.logs == null) || (data.logs.next_page_url == null) || data.logs.next_page_url.trim().isEmpty();
                        if (newItems.isEmpty() || noNext) {
                            // إذا الصفحة الحالية فيها بيانات بس noNext => آخر صفحة
                            // إذا فاضية => أكيد انتهى
                            hasMore = !newItems.isEmpty() && data.logs != null && data.logs.current_page < data.logs.last_page;
                        } else {
                            hasMore = true;
                        }

                        if (!newItems.isEmpty()) {
                            page = targetPage;
                            adapter.addAll(newItems);
                        }

                        // update app (إذا موجود)
                        if (setting != null && setting.getVersion_app() != null) {
                            // if (!setting.getVersion_app().equals(BuildConfig.VERSION_NAME)) { ... }
                        }
                    }

                    @Override
                    public void onError(ApiError error) {
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        isLoading = false;
                        toast(error.message);
                    }

                    @Override
                    public void onUnauthorized(String rawJson) {
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        isLoading = false;
                        logout();
                    }

                    @Override
                    public void onNetworkError(String reason) {
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        isLoading = false;
                        toast(R.string.no_internet);
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        isLoading = false;
                        toast(getString(R.string.general_error));
                    }
                }
        );
    }

    private void updateHeader(Account account) {
        if (account == null) return;
        binding.name.setText(safe(account.getName()));      // أو account_name حسب موديلك
        binding.phone.setText(safe(account.getMobile()));
        binding.amount.setText(safe(""+account.getBalance()));
    }

}
