package co.highfive.petrolstation.activities;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.adapters.CustomerAdapter;
import co.highfive.petrolstation.customers.dto.CustomerDto;
import co.highfive.petrolstation.customers.dto.CustomersData;
import co.highfive.petrolstation.databinding.ActivityCustomersBinding;
import co.highfive.petrolstation.fragments.CustomerFilterDialog;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.listener.CustomerListener;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;

public class CustomersActivity extends BaseActivity {

    private ActivityCustomersBinding binding;
    private CustomerAdapter adapter;
    private ApiClient apiClient;

    // paging
    private int currentPage = 1;
    private final int pageSize = 10;
    private boolean isLoading = false;
    private boolean hasMore = true;

    private final ArrayList<CustomerDto> customers = new ArrayList<>();

    // filters: name + balance
    private String filterName = null;
    private String filterBalance = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_customers);
        setupUI(binding.mainLayout);

        initApiClient();
        initViews();

        // ✅ لا تعمل request عند فتح الصفحة
        adapter.setItems(customers);
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

    private void initViews() {
        adapter = new CustomerAdapter(new CustomerAdapter.CustomerItemListener() {
            @Override public void onCustomerClick(CustomerDto customer, int position) {
                Bundle bundle = buildCustomerBundle(customer);
                baseActivity.moveToActivity(baseActivity, CustomerActivity.class,bundle,false);
            }
            @Override public void onAddPaymentClick(CustomerDto customer, int position) {
                Bundle bundle = buildCustomerBundle(customer);
                baseActivity.moveToActivity(baseActivity, AddFinancialTransactionActivity.class,bundle,false);
            }
            @Override public void onViewClick(CustomerDto customer, int position) {
                Bundle bundle = buildCustomerBundle(customer);
                baseActivity.moveToActivity(baseActivity, CustomerActivity.class,bundle,false);
            }

            @Override
            public void onViewVehicles(CustomerDto customer, int position) {
                Bundle bundle = buildCustomerBundle(customer);
                moveToActivity(CustomersActivity.this, CustomerVehiclesActivity.class, bundle, false);
            }
        });

        binding.recyclerCustomers.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerCustomers.setAdapter(adapter);

        binding.swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.blue);
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            // ✅ Refresh فقط إذا فيه فلتر
            if (hasAnyFilter()) {
                loadPage(1, false);
            } else {
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });

        binding.icBack.setOnClickListener(v -> finish());
        binding.icHome.setOnClickListener(v ->
                moveToActivity(getApplicationContext(), MainActivity.class, null, false, true)
        );

        // Enter على search (name فقط)
        binding.search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideSoftKeyboard();

                    // name من search
                    filterName = trimToNull(binding.search.getText() != null ? binding.search.getText().toString() : null);

                    // ✅ لا تعمل request لو ما في name ولا balance
                    if (hasAnyFilter()) {
                        loadPage(1, true);
                    } else {
                        clearResults();
                    }
                    return true;
                }
                return false;
            }
        });

        // ✅ زر الفلتر: فتح الديالوج وربطه بالاكشن
        binding.icFilterWhite.setOnClickListener(v -> openCustomerFilterDialog());

        // pagination
        binding.recyclerCustomers.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy <= 0) return;
                if (!hasAnyFilter()) return; // ✅ لا pagination بدون فلتر

                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visible = lm.getChildCount();
                int total = lm.getItemCount();
                int firstVisible = lm.findFirstVisibleItemPosition();

                if (!isLoading && hasMore && (visible + firstVisible) >= (total - 2)) {
                    loadPage(currentPage + 1, false);
                }
            }
        });
    }
    private Bundle buildCustomerBundle(CustomerDto customerDto) {
        Bundle b = new Bundle();
        b.putString("id", safe(""+customerDto.id));
        b.putString("customer_id", safe(""+customerDto.id)); // بعض الشاشات تستخدم customer_id
        b.putString("account_id", safe(""+customerDto.account_id));
        b.putString("name", safe(customerDto.name));
        b.putString("mobile", safe(customerDto.mobile));
        return b;
    }
    private void openCustomerFilterDialog() {
        CustomerFilterDialog d = CustomerFilterDialog.newInstance(filterName, filterBalance);
        d.setCancelable(false);

        d.setListener(new CustomerListener() {
            @Override
            public void onApplyFilter(String nameVal, String balanceVal) {
                setFilters(nameVal, balanceVal);
            }

            @Override
            public void onClearFilter() {
                // ✅ يمسح الفلاتر + يمسح search + يمسح النتائج بدون request
                filterName = null;
                filterBalance = null;
                binding.search.setText("");
                clearResults();
            }
        });

        d.show(getSupportFragmentManager(), "CustomerFilterDialog");
    }

    private void setFilters(String nameVal, String balanceVal) {
        filterName = trimToNull(nameVal);
        filterBalance = trimToNull(balanceVal);

        // sync search input مع name (لو بدك)
        binding.search.setText(filterName == null ? "" : filterName);

        if (hasAnyFilter()) {
            loadPage(1, true);
        } else {
            clearResults();
        }
    }

    private boolean hasAnyFilter() {
        return trimToNull(filterName) != null || trimToNull(filterBalance) != null;
    }

    private void clearResults() {
        customers.clear();
        adapter.setItems(customers);
        hasMore = false;
        currentPage = 1;
        binding.swipeRefreshLayout.setRefreshing(false);
    }

    private void loadPage(int page, boolean showDialog) {
        if (isLoading) return;

        // ✅ حماية: لا request بدون فلتر
        if (!hasAnyFilter()) {
            clearResults();
            return;
        }

        isLoading = true;

        if (showDialog) showProgressHUD();
        if (page > 1) adapter.setLoading(true);

        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));

        String n = trimToNull(filterName);
        String b = trimToNull(filterBalance);

        // ✅ لا ترسل balance لو null (ولا name لو null)
        if (n != null) params.put("name", n);
        if (b != null) params.put("balance", b);

        Type type = new TypeToken<BaseResponse<CustomersData>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.CUSTOMERS,
                params,
                null,
                type,
                0,
                new ApiCallback<CustomersData>() {
                    @Override
                    public void onSuccess(CustomersData data, String message, String rawJson) {
                        hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        adapter.setLoading(false);
                        isLoading = false;

                        if (page == 1) customers.clear();

                        List<CustomerDto> list = (data == null) ? null : data.customers;

                        if (list != null && !list.isEmpty()) {
                            customers.addAll(list);
                            adapter.setItems(customers);

                            currentPage = page;
                            hasMore = list.size() >= pageSize;
                        } else {
                            hasMore = false;
                            adapter.setItems(customers);
                        }
                    }

                    @Override
                    public void onError(co.highfive.petrolstation.network.ApiError error) {
                        hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        adapter.setLoading(false);
                        isLoading = false;
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
                        binding.swipeRefreshLayout.setRefreshing(false);
                        adapter.setLoading(false);
                        isLoading = false;
                        toast(R.string.no_internet);
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        adapter.setLoading(false);
                        isLoading = false;
                        toast(getString(R.string.general_error));
                    }
                }
        );
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
