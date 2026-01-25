package co.highfive.petrolstation.activities;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.adapters.CustomerAdapter;
import co.highfive.petrolstation.customers.dto.CustomerDto;
import co.highfive.petrolstation.customers.dto.CustomersData;
import co.highfive.petrolstation.data.local.AppDatabase;
import co.highfive.petrolstation.data.local.DatabaseProvider;
import co.highfive.petrolstation.data.local.entities.CustomerEntity;
import co.highfive.petrolstation.databinding.ActivityCustomersBinding;
import co.highfive.petrolstation.fragments.CustomerFilterDialog;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.listener.CustomerListener;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;

import android.os.Handler;
import android.os.Looper;

public class CustomersActivity extends BaseActivity {

    private ActivityCustomersBinding binding;
    private CustomerAdapter adapter;

    // Room (للاوفلاين فقط)
    private AppDatabase db;
    private ExecutorService dbExecutor;
    private Handler mainHandler;

    // paging
    private int currentPage = 1;
    private final int pageSize = 10;
    private boolean isLoading = false;
    private boolean hasMore = true;

    private final ArrayList<CustomerDto> customers = new ArrayList<>();

    // filters
    private String filterName = null;
    private String filterBalance = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_customers);
        setupUI(binding.mainLayout);

        db = DatabaseProvider.get(this);
        dbExecutor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        applyPermissions();
        initViews();
        initClicks();

        // ✅ لا تحميل عند فتح الصفحة
        adapter.setItems(customers);
    }

    private void initClicks() {
        binding.add.setOnClickListener(view ->
                moveToActivity(CustomersActivity.this, EditCustomerActivity.class, null, false)
        );
    }

    private void initViews() {
        adapter = new CustomerAdapter(new CustomerAdapter.CustomerItemListener() {
            @Override public void onCustomerClick(CustomerDto customer, int position) {
                Bundle bundle = buildCustomerBundle(customer);
                moveToActivity(CustomersActivity.this, CustomerActivity.class, bundle, false);
            }

            @Override public void onAddPaymentClick(CustomerDto customer, int position) {
                Bundle bundle = buildCustomerBundle(customer);
                moveToActivity(CustomersActivity.this, AddFinancialTransactionActivity.class, bundle, false);
            }

            @Override public void onViewClick(CustomerDto customer, int position) {
                Bundle bundle = buildCustomerBundle(customer);
                moveToActivity(CustomersActivity.this, CustomerActivity.class, bundle, false);
            }

            @Override public void onViewVehicles(CustomerDto customer, int position) {
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

        binding.search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideSoftKeyboard();

                    filterName = trimToNull(binding.search.getText() != null ? binding.search.getText().toString() : null);

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

        binding.icFilterWhite.setOnClickListener(v -> openCustomerFilterDialog());

        binding.recyclerCustomers.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy <= 0) return;
                if (!hasAnyFilter()) return;

                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visible = lm.getChildCount();
                int total = lm.getItemCount();
                int firstVisible = lm.findFirstVisibleItemPosition();

                if (!isLoading && hasMore && (visible + firstVisible) >= (total - 2)) {
                    loadPage(currentPage + 1, false);
                }
            }
        });

        binding.add.setVisibility(View.VISIBLE);
    }

    private void applyPermissions() {
        if (getAppData() == null) {
            hideAllActions();
            return;
        }

        int addCustomers = safeInt(getAppData().getAdd_customers());
        setVisible(binding.add, addCustomers == 1);
    }

    private void hideAllActions() {
        setVisible(binding.add, false);
    }

    private Bundle buildCustomerBundle(CustomerDto customerDto) {
        Bundle b = new Bundle();
        b.putString("id", safe("" + customerDto.id));
        b.putString("customer_id", safe("" + customerDto.id));
        b.putString("account_id", safe("" + customerDto.account_id));
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

        // ✅ ممنوع تحميل بدون فلتر
        if (!hasAnyFilter()) {
            clearResults();
            return;
        }

        isLoading = true;

        if (showDialog) showProgressHUD();
        if (page > 1) adapter.setLoading(true);

        if (connectionAvailable) {
            loadPageFromApi(page);
        } else {
            loadPageFromRoom(page);
        }
    }

    // =========================
    // ONLINE: API ONLY (no caching)
    // =========================
    private void loadPageFromApi(int page) {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));

        String n = trimToNull(filterName);
        String b = trimToNull(filterBalance);

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
                        endLoadingUi();
                        toast(error.message);
                    }

                    @Override
                    public void onUnauthorized(String rawJson) {
                        hideProgressHUD();
                        logout();
                    }

                    @Override
                    public void onNetworkError(String reason) {
                        endLoadingUi();
                        toast(R.string.no_internet);
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        endLoadingUi();
                        toast(getString(R.string.general_error));
                    }
                }
        );
    }

    // =========================
    // OFFLINE: ROOM ONLY
    // =========================
    private void loadPageFromRoom(int page) {
        int offset = (page - 1) * pageSize;

        final String name = trimToNull(filterName);
        final Double balanceVal = parseDoubleOrNull(trimToNull(filterBalance));

        dbExecutor.execute(() -> {
            List<CustomerEntity> rows;

            if (name != null && balanceVal == null) {
                rows = db.customerDao().searchByNamePaged("%" + name + "%", pageSize, offset);
            } else if (name == null && balanceVal != null) {
                rows = db.customerDao().searchByBalanceMinPaged(balanceVal, pageSize, offset);
            } else {
                // name + balance موجودين
                rows = db.customerDao().searchByNameAndBalanceMinPaged("%" + name + "%", balanceVal != null ? balanceVal : 0.0, pageSize, offset);
            }

            ArrayList<CustomerDto> mapped = new ArrayList<>();
            if (rows != null) {
                for (CustomerEntity e : rows) mapped.add(mapEntityToDto(e));
            }

            mainHandler.post(() -> {
                hideProgressHUD();
                binding.swipeRefreshLayout.setRefreshing(false);
                adapter.setLoading(false);
                isLoading = false;

                if (page == 1) customers.clear();

                if (!mapped.isEmpty()) {
                    customers.addAll(mapped);
                    adapter.setItems(customers);

                    currentPage = page;
                    hasMore = mapped.size() >= pageSize;
                } else {
                    hasMore = false;
                    adapter.setItems(customers);
                }
            });
        });
    }

    private void endLoadingUi() {
        hideProgressHUD();
        binding.swipeRefreshLayout.setRefreshing(false);
        adapter.setLoading(false);
        isLoading = false;
    }

    private CustomerDto mapEntityToDto(CustomerEntity e) {
        CustomerDto c = new CustomerDto();
        c.id = e.id;
        c.name = e.name;
        c.mobile = e.mobile;
        c.account_id = e.accountId;
        c.balance = e.balance;
        c.address = e.address;
        c.asseal_no = e.assealNo;

        c.type_customer = e.typeCustomer;
        c.customer_classify = e.customerClassify;
        c.status = e.status;
        c.customer_status = e.customerStatus;
        c.customer_classify_name = e.customerClassifyName;
        c.type_customer_name = e.typeCustomerName;
        c.campaign_name = e.campaignName;
        c.remaining_amount = e.remainingAmount;

        return c;
    }

    private static Double parseDoubleOrNull(String s) {
        if (s == null) return null;
        try { return Double.parseDouble(s); } catch (Exception e) { return null; }
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
