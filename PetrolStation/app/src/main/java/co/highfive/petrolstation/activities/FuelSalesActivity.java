package co.highfive.petrolstation.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.adapters.CustomerInvoicesAdapter;
import co.highfive.petrolstation.customers.dto.InvoiceDto;
import co.highfive.petrolstation.customers.dto.InvoicesPaginationDto;
import co.highfive.petrolstation.data.local.AppDatabase;
import co.highfive.petrolstation.data.local.DatabaseProvider;
import co.highfive.petrolstation.data.local.dao.UnifiedInvoiceRow;
import co.highfive.petrolstation.data.local.entities.InvoiceEntity;
import co.highfive.petrolstation.data.local.entities.OfflineInvoiceEntity;
import co.highfive.petrolstation.data.local.repo.InvoiceLocalRepository;
import co.highfive.petrolstation.databinding.ActivityCustomerInvoicesBinding;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.models.Setting;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.ApiError;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;

public class FuelSalesActivity extends BaseActivity {

    private ActivityCustomerInvoicesBinding binding;

    private AppDatabase db;
    private ExecutorService dbExecutor;
    private Handler mainHandler;

    private InvoiceLocalRepository repo;

    private final ArrayList<InvoiceDto> displayItems = new ArrayList<>();
    private CustomerInvoicesAdapter adapter;

    private boolean isCustomerMode = false;
    private String accountId = "";
    private String customerId = "";
    private String customerName = "";
    private String customerMobile = "";

    private boolean isLoading = false;
    private boolean hasNextPage = true;
    private int currentPage = 1;

    private final int pageSize = 10;
    private final int isFuelSale = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCustomerInvoicesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI(binding.mainLayout);

        db = DatabaseProvider.get(this);
        dbExecutor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        repo = new InvoiceLocalRepository(this, db, getGson(), apiClient);

        readExtras();
        initHeader();
        initClicks();
        initRecycler();
        initRefresh();

        // أول تحميل:
        refreshSmart(true);
    }

    private void readExtras() {
        Bundle extras = getIntent() != null ? getIntent().getExtras() : null;
        if (extras == null) return;

        customerId = safe(extras.getString("customer_id"));
        if (customerId.trim().isEmpty()) customerId = safe(extras.getString("id"));

        accountId = safe(extras.getString("account_id"));
        customerName = safe(extras.getString("name"));
        customerMobile = safe(extras.getString("mobile"));

        isCustomerMode = !accountId.trim().isEmpty() || !customerId.trim().isEmpty();
    }

    private void initHeader() {
        if (isCustomerMode) {
            binding.headerInfoLayout.setVisibility(View.VISIBLE);
            binding.name.setText(customerName);
            binding.phone.setText(customerMobile);

            binding.phone.setOnClickListener(v -> {
                if (!customerMobile.trim().isEmpty()) call(customerMobile);
            });
        } else {
            binding.headerInfoLayout.setVisibility(View.GONE);
        }
    }

    private void initClicks() {
        binding.icHome.setOnClickListener(v ->
                moveToActivity(getApplicationContext(), MainActivity.class, null, false, true)
        );
        binding.icBack.setOnClickListener(v -> finish());
    }

    private void initRecycler() {
        adapter = new CustomerInvoicesAdapter(displayItems, new CustomerInvoicesAdapter.Listener() {
            @Override
            public void onView(InvoiceDto invoice) {
                openInvoiceDetails(invoice);
            }

            @Override
            public void onPrint(InvoiceDto invoice) {
                Setting setting = null;
                if (getAppData() != null) setting = getAppData().getSetting();

                if (setting == null) {
                    toast(getString(R.string.general_error));
                    return;
                }
                printInvoice(setting, invoice);
            }
        });

        adapter.setHideCustomerNameInItem(isCustomerMode);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        binding.recycler.setLayoutManager(lm);
        binding.recycler.setAdapter(adapter);

        binding.recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                if (dy <= 0) return;
                if (isLoading || !hasNextPage) return;

                int visibleCount = lm.getChildCount();
                int totalCount = lm.getItemCount();
                int firstVisible = lm.findFirstVisibleItemPosition();

                if ((visibleCount + firstVisible) >= (totalCount - 3)) {
                    loadPageSmart(false, currentPage + 1, false);
                }
            }
        });
    }

    private void initRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> refreshSmart(false));
    }

    private void refreshSmart(boolean showDialog) {
        loadPageSmart(showDialog, 1, true);
    }

    private void loadPageSmart(boolean showDialog, int page, boolean isReset) {
        if (isLoading) return;

        Integer cid = safeInt(customerId);
        if (cid == null || cid <= 0) {
            toast("لا يوجد customer_id");
            return;
        }

        isLoading = true;

        if (showDialog) showProgressHUD();
        binding.swipeRefreshLayout.setRefreshing(!showDialog);

        if (connectionAvailable) {
            // 1) جرّب تعمل sync للـ offline pending أولاً
            syncPendingThenLoad(cid, page, isReset, showDialog);
        } else {
            // Offline: load unified directly
            loadUnifiedFromDb(cid, page, isReset, showDialog);
        }
    }

    private void syncPendingThenLoad(int customerIdInt, int page, boolean isReset, boolean showDialog) {

        // Sync only pending fuel invoices (limit مثلا 20)
        dbExecutor.execute(() -> {
            try {
                repo.syncPendingFuelInvoices(20, (success, failed, skipped) -> {
                    // بعد ما يخلص sync (حتى لو فشل/توقف)، اعمل fetch من السيرفر لتحديث invoices cache (اختياري)
                    mainHandler.post(() -> fetchFuelInvoicesOnlineAndCache(customerIdInt, page, isReset, showDialog));
                });
            } catch (Exception e) {
                // حتى لو sync ضرب، كمل load طبيعي
                mainHandler.post(() -> fetchFuelInvoicesOnlineAndCache(customerIdInt, page, isReset, showDialog));
            }
        });
    }

    /**
     * ✅ ONLINE:
     * - جيب من السيرفر (حسب API عندك)
     * - اعمل upsert للـ invoices table (isFuelSale=1)
     * - بعدين اعرض unified feed (Online from DB + Offline pending)
     */
    private void fetchFuelInvoicesOnlineAndCache(int customerIdInt, int page, boolean isReset, boolean showDialog) {

        // IMPORTANT:
        // عدّل endpoint والparams حسب API عندك.
        // خيار 1: نفس CUSTOMERS_GETINVOICES مع باراميتر is_fuel_sale=1
        // خيار 2: Endpoint خاص لفواتير المحروقات

        Map<String, String> params;

        // لو عندك account_id (customer mode) مثل CustomerInvoicesActivity
        if (accountId != null && !accountId.trim().isEmpty()) {
            params = ApiClient.mapOf(
                    "page", String.valueOf(page),
                    "account_id", accountId,
                    "is_fuel_sale", "1" // <-- عدّلها حسب السيرفر
            );
        } else {
            params = ApiClient.mapOf(
                    "page", String.valueOf(page),
                    "customer_id", String.valueOf(customerIdInt),
                    "is_fuel_sale", "1" // <-- عدّلها حسب السيرفر
            );
        }

        Type type = new TypeToken<BaseResponse<InvoicesPaginationDto>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.CUSTOMERS_GETFUELSALES, // <-- عدّلها إذا عندك Endpoint fuel خاص
                params,
                null,
                type,
                0,
                new ApiCallback<InvoicesPaginationDto>() {

                    @Override
                    public void onSuccess(InvoicesPaginationDto data, String msg, String rawJson) {

                        // خزّن invoices في DB ثم اعرض unified
                        List<InvoiceDto> serverList = (data != null && data.data != null) ? data.data : new ArrayList<>();

                        dbExecutor.execute(() -> {
                            try {
                                List<InvoiceEntity> entities = mapServerInvoicesToEntities(serverList, customerIdInt);
                                if (entities != null && !entities.isEmpty()) {
                                    db.invoiceDao().upsertAll(entities);
                                }
                            } catch (Exception ignored) {}

                            mainHandler.post(() -> {
                                // balance header (اختياري) مثل كودك
                                try {
                                    if (data != null && data.data != null && !data.data.isEmpty() && data.data.get(0) != null && data.data.get(0).account != null) {
                                        if (data.data.get(0).account.getBalance() != null) {
                                            binding.amount.setText("" + data.data.get(0).account.getBalance());
                                        } else if (data.data.get(0).account.getCredit() != null && data.data.get(0).account.getDepit() != null) {
                                            double balance = data.data.get(0).account.getCredit() - data.data.get(0).account.getDepit();
                                            binding.amount.setText("" + balance);
                                        }
                                    }
                                } catch (Exception ignored2) {}

                                // الآن اعرض unified من DB (وبالتالي تشمل OFFLINE pending)
                                loadUnifiedFromDb(customerIdInt, page, isReset, showDialog);

                                // pagination flags من السيرفر:
                                if (data != null) {
                                    currentPage = data.current_page != null ? data.current_page : page;
                                    hasNextPage = data.next_page_url != null && !data.next_page_url.trim().isEmpty();
                                } else {
                                    currentPage = page;
                                    // fallback من DB counts لاحقاً
                                }
                            });
                        });
                    }

                    @Override
                    public void onError(ApiError error) {
                        // fallback: حتى لو السيرفر فشل، اعرض unified من DB
                        loadUnifiedFromDb(customerIdInt, page, isReset, showDialog);
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
                        // fallback: unified from DB
                        loadUnifiedFromDb(customerIdInt, page, isReset, showDialog);
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        // fallback: unified from DB
                        loadUnifiedFromDb(customerIdInt, page, isReset, showDialog);
                    }
                }
        );
    }

    /**
     * ✅ DB Unified Feed:
     * - UNION invoices + offline_invoices
     */
    private void loadUnifiedFromDb(int customerIdInt, int page, boolean isReset, boolean showDialog) {

        final int offset = (page - 1) * pageSize;

        dbExecutor.execute(() -> {
            List<UnifiedInvoiceRow> rows = new ArrayList<>();
            int total = 0;

            try {
                rows = repo.getUnifiedInvoicesByCustomerPaged(customerIdInt, isFuelSale, pageSize, offset);
                total = repo.countUnifiedByCustomer(customerIdInt, isFuelSale);
            } catch (Exception ignored) {}

            ArrayList<InvoiceDto> mapped = new ArrayList<>();
            if (rows != null) {
                for (UnifiedInvoiceRow r : rows) {
                    mapped.add(mapUnifiedRowToInvoiceDto(r));
                }
            }

            boolean hasMore = (offset + mapped.size()) < total;

            mainHandler.post(() -> {
                if (showDialog) hideProgressHUD();
                binding.swipeRefreshLayout.setRefreshing(false);

                isLoading = false;

                if (isReset) {
                    displayItems.clear();
                    displayItems.addAll(mapped);
                } else {
                    displayItems.addAll(mapped);
                }
                adapter.notifyDataSetChanged();

                currentPage = page;
                // لو online وقرأنا hasNextPage من السيرفر خلّيه، otherwise من DB
                if (!connectionAvailable) {
                    hasNextPage = hasMore;
                } else {
                    // لو السيرفر ما رجّع next_page_url (أو انت بدك تحكم من DB)
                    // يمكنك تختار:
                    // hasNextPage = hasMore;
                }
            });
        });
    }

    // =========================
    // Mapping
    // =========================

    private InvoiceDto mapUnifiedRowToInvoiceDto(UnifiedInvoiceRow r) {
        InvoiceDto dto = new InvoiceDto();

        if (r == null) return dto;

        boolean isOffline = "OFFLINE".equalsIgnoreCase(r.source);

        dto.is_offline = isOffline;

        if (isOffline) {
            dto.local_id = (r.localId != null) ? r.localId : 0;
            dto.sync_status = (r.syncStatus != null) ? r.syncStatus : 0;
            dto.id = (r.onlineId != null) ? r.onlineId : 0;

            dto.invoice_no = (r.invoiceNo != null && !r.invoiceNo.trim().isEmpty())
                    ? r.invoiceNo
                    : ("OFF-" + dto.local_id);

            // تاريخ display من sortTs
            dto.date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date(r.sortTs));
        } else {
            dto.id = (r.onlineId != null) ? r.onlineId : 0;
            dto.invoice_no = (r.invoiceNo != null) ? r.invoiceNo : "";
            // تاريخ display غير موجود في row، ممكن نتركه فاضي أو نستخدم sortTs
            dto.date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date(r.sortTs));
        }

        dto.statement = r.statement;
        dto.total = r.total;

        return dto;
    }

    private List<InvoiceEntity> mapServerInvoicesToEntities(List<InvoiceDto> list, int customerIdInt) {
        ArrayList<InvoiceEntity> out = new ArrayList<>();
        if (list == null) return out;

        for (InvoiceDto d : list) {
            if (d == null) continue;

            InvoiceEntity e = new InvoiceEntity();
            e.id = d.id;

            // مهم: customerId
            // إن كان السيرفر يرجع customer_id استخدمه، وإلا استخدم customerIdInt القادم من extras
            try {
                // لو عندك d.customer_id
                // e.customerId = d.customer_id;
                e.customerId = customerIdInt;
            } catch (Exception ignored) {
                e.customerId = customerIdInt;
            }

            e.date = d.date;
            e.statement = d.statement;

            e.accountId = d.account_id;
            e.storeId = d.store_id;

            e.discount = d.discount;
            e.total = d.total;

            e.invoiceNo = d.invoice_no;
            e.notes = d.notes;

            e.campaignId = d.campaign_id;
            e.pumpId = d.pump_id;
            e.customerVehicleId = d.customer_vehicle_id;

            e.isFuelSale = 1;

            e.createdAt = d.created_at;

            // createdAtTs
            e.createdAtTs = guessCreatedAtTs(d);
            e.updatedAt = System.currentTimeMillis();

            out.add(e);
        }

        return out;
    }

    private long guessCreatedAtTs(InvoiceDto d) {
        // أفضل شيء: لو عندك server created_at بصيغة ثابتة
        // جرّب parsing، وإلا fallback على الآن
        try {
            if (d == null) return System.currentTimeMillis();

            String s = null;
            try {
                s = d.created_at; // موجود في mapInvoiceEntityToDto عندك
            } catch (Exception ignored) {}

            if (s == null || s.trim().isEmpty()) {
                // fallback: حاول date
                s = d.date;
            }
            if (s == null || s.trim().isEmpty()) return System.currentTimeMillis();

            // عدّل الفورمات حسب اللي عندك من السيرفر
            // أمثلة شائعة:
            // 2026-01-25 12:30:10
            // 2026-01-25T12:30:10.000Z
            SimpleDateFormat f1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            try {
                Date dt = f1.parse(s);
                if (dt != null) return dt.getTime();
            } catch (ParseException ignored1) {}

            SimpleDateFormat f2 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            try {
                Date dt = f2.parse(s);
                if (dt != null) return dt.getTime();
            } catch (ParseException ignored2) {}

            return System.currentTimeMillis();
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }

    private void openInvoiceDetails(InvoiceDto inv) {
        if (inv == null) return;

        Bundle b = new Bundle();

        if (inv.is_offline) {
            b.putBoolean("is_offline", true);
            b.putLong("local_id", inv.local_id);
            b.putInt("sync_status", inv.sync_status);
            b.putString("sync_error", inv.sync_error);

            b.putString("invoice_no", inv.invoice_no);
            b.putString("date", inv.date);
            b.putString("statement", inv.statement);
            b.putString("total", inv.total != null ? String.valueOf(inv.total) : "");
            b.putString("notes", inv.notes);

            moveToActivity(FuelSalesActivity.this, InvoiceDetailsActivity.class, b, false);
            return;
        }

        b.putBoolean("is_offline", false);
        b.putInt("invoice_id", inv.id);
        b.putString("invoice_no", inv.invoice_no);
        b.putString("date", inv.date);
        b.putString("statement", inv.statement);
        b.putString("total", inv.total != null ? String.valueOf(inv.total) : "");
        b.putString("notes", inv.notes);

        moveToActivity(FuelSalesActivity.this, InvoiceDetailsActivity.class, b, false);
    }

    private Integer safeInt(String s) {
        try {
            if (s == null) return null;
            s = s.trim();
            if (s.isEmpty()) return null;
            return Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }


}
