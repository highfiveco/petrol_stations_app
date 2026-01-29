package co.highfive.petrolstation.activities;

import android.os.Bundle;
import android.util.Log;
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
import co.highfive.petrolstation.data.local.entities.OfflineCustomerEntity;
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

    private Bundle buildCustomerBundle(CustomerDto c) {
        Bundle b = new Bundle();

        b.putString("id", safe("" + c.id));
        b.putString("customer_id", safe("" + c.id));

        if (c.id < 0) {
            long localId = -1L * c.id;
            b.putString("offline_local_id", String.valueOf(localId));
            b.putBoolean("is_offline", true);
        } else {
            b.putBoolean("is_offline", false);
        }

        b.putString("account_id", safe("" + c.account_id));
        b.putString("name", safe(c.name));
        b.putString("mobile", safe(c.mobile));

        if (b == null) {
            Log.e("BUNDLE", "Bundle is null");
        } else {
            for (String key : b.keySet()) {
                Object value = b.get(key);
                Log.e("BUNDLE", key + " = " + String.valueOf(value)
                        + " (" + (value != null ? value.getClass().getSimpleName() : "null") + ")");
            }
        }
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
        final int offset = (page - 1) * pageSize;

        final String name = trimToNull(filterName);
        final Double balanceVal = parseDoubleOrNull(trimToNull(filterBalance));

        dbExecutor.execute(() -> {

            // =============== OFFLINE PART (NO PAGINATION) ===============
            ArrayList<CustomerDto> offlineMapped = new ArrayList<>();

            // نجيب offline فقط في الصفحة الأولى (عشان ما يتكرروا بكل صفحة)
            if (page == 1) {
                try {
                    List<OfflineCustomerEntity> offlineRows;

                    if (name != null) {
                        // عندك search(q) جاهزة
                        offlineRows = db.offlineCustomerDao().search(name);
                    } else {
                        // لو ما في name فلتر، ما نعرض offline (لأن شاشتك ممنوع تحميل بدون فلتر)
                        offlineRows = new ArrayList<>();
                    }

                    if (offlineRows != null) {
                        for (OfflineCustomerEntity e : offlineRows) {
                            offlineMapped.add(mapOfflineToCustomerDto(e));
                        }
                    }
                } catch (Exception ignored) {}
            }

            // =============== ONLINE CACHED PART (WITH PAGINATION) ===============
            int onlineLimit = pageSize;
            int onlineOffset = offset;

            // إذا page=1 وفي offline موجود → قلل limit من online
            if (page == 1 && !offlineMapped.isEmpty()) {
                onlineLimit = Math.max(0, pageSize - offlineMapped.size());
                onlineOffset = 0;
            } else if (page > 1) {
                // الصفحة الأولى أخذت من customers عدد أقل بسبب offline
                // يعني خصم = (pageSize - onlineLimitInPage1)
                int onlineUsedInPage1 = Math.max(0, pageSize - (page == 1 ? 0 : offlineMapped.size())); // مش مستخدم هون
                // الأسهل: احسب كم customer انعرض في الصفحة الأولى فعليًا:
                int page1OnlineShown = pageSize;
                if (name != null) {
                    // لو في offline على الصفحة الأولى، page1OnlineShown تقل
                    // بس offlineMapped موجود فقط في page==1, هون page>1 لذلك لازم نعيد حسابها:
                    // نقدر ببساطة نفترض أن page1OnlineShown = max(0, pageSize - offlineCountFirstPage)
                    // عشان هيك نحتاج offlineCountFirstPage. نحسبه سريعًا بدون mapping كامل:
                    int offlineCountFirstPage = 0;
                    try {
                        if (name != null) {
                            List<OfflineCustomerEntity> tmp = db.offlineCustomerDao().search(name);
                            offlineCountFirstPage = (tmp != null) ? tmp.size() : 0;
                        }
                    } catch (Exception ignored) {}
                    page1OnlineShown = Math.max(0, pageSize - offlineCountFirstPage);
                }

                // الآن offset للـ customers يبدأ بعد page1OnlineShown
                onlineOffset = page1OnlineShown + (page - 2) * pageSize;
                onlineLimit = pageSize;
            }

            List<CustomerEntity> rowsOnline = new ArrayList<>();
            try {
                if (onlineLimit > 0) {
                    if (name != null && balanceVal == null) {
                        rowsOnline = db.customerDao().searchByNamePaged("%" + name + "%", onlineLimit, onlineOffset);
                    } else if (name == null && balanceVal != null) {
                        rowsOnline = db.customerDao().searchByBalanceMinPaged(balanceVal, onlineLimit, onlineOffset);
                    } else {
                        rowsOnline = db.customerDao().searchByNameAndBalanceMinPaged("%" + name + "%", balanceVal != null ? balanceVal : 0.0, onlineLimit, onlineOffset);
                    }
                }
            } catch (Exception ignored) {}

            ArrayList<CustomerDto> finalPage = new ArrayList<>();

            if (page == 1 && !offlineMapped.isEmpty()) finalPage.addAll(offlineMapped);

            if (rowsOnline != null) {
                for (CustomerEntity e : rowsOnline) finalPage.add(mapEntityToDto(e));
            }

            mainHandler.post(() -> {
                hideProgressHUD();
                binding.swipeRefreshLayout.setRefreshing(false);
                adapter.setLoading(false);
                isLoading = false;

                if (page == 1) customers.clear();

                if (!finalPage.isEmpty()) {
                    customers.addAll(finalPage);
                    adapter.setItems(customers);

                    currentPage = page;
                    hasMore = finalPage.size() >= pageSize;
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

    private CustomerDto mapOfflineToCustomerDto(OfflineCustomerEntity e) {
        CustomerDto c = new CustomerDto();

        c.id = (int) (-e.localId);          // ✅ سالب => أوفلاين
        c.name = e.name;
        c.mobile = e.mobile;

        c.account_id = 0;                   // لا يوجد
        c.balance = 0.0;
        c.remaining_amount = 0.0;
        c.address = e.address;
        c.asseal_no = null;

        c.type_customer = null;
        c.customer_classify = null;
        c.status = 1;

        c.customer_status = "OFFLINE";
        c.type_customer_name = "";
        c.customer_classify_name = "";
        c.campaign_name = "";

        return c;
    }


}
