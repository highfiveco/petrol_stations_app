package co.highfive.petrolstation.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.adapters.CustomerInvoicesAdapter;
import co.highfive.petrolstation.customers.dto.InvoiceDto;
import co.highfive.petrolstation.customers.dto.InvoicesPaginationDto;
import co.highfive.petrolstation.data.local.entities.OfflineInvoiceEntity;
import co.highfive.petrolstation.databinding.ActivityCustomerInvoicesBinding;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.models.Setting;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.ApiError;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import co.highfive.petrolstation.data.local.AppDatabase;
import co.highfive.petrolstation.data.local.DatabaseProvider;
import co.highfive.petrolstation.data.local.entities.CustomerEntity;
import co.highfive.petrolstation.data.local.entities.InvoiceEntity;
public class CustomerInvoicesActivity extends BaseActivity {

    private ActivityCustomerInvoicesBinding binding;

    private String accountId = "";
    private String customerName = "";
    private String customerMobile = "";

    private final ArrayList<InvoiceDto> items = new ArrayList<>();
    private CustomerInvoicesAdapter adapter;

    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean hasNextPage = true;

    private boolean isCustomerMode = false;
    private String customerId = "";

    private AppDatabase db;
    private ExecutorService dbExecutor;
    private Handler mainHandler;

    private final int pageSize = 10; // لو بدك نفس pageSize تبع السيرفر

    private int invoiceType = 0; // 0=invoices, 1=fuel_invoices

    private final ArrayList<InvoiceDto> pendingOffline = new ArrayList<>();
    private final ArrayList<InvoiceDto> onlineItems = new ArrayList<>();   // لما تكون Online (مصدرها سيرفر)
    private final ArrayList<InvoiceDto> cachedItems = new ArrayList<>();   // لما تكون Offline (مصدرها DB invoices)
    private final ArrayList<InvoiceDto> displayItems = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCustomerInvoicesBinding.inflate(getLayoutInflater());
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

        fetchInvoicesSmart(true, 1, true);
    }

    private void fetchInvoicesSmart(boolean showDialog, int page, boolean isReset) {

        int isFuelSale = (invoiceType == 1) ? 1 : 0;

        if (connectionAvailable) {
            // ✅ Online mode: pending + online(server)
            if (isReset) {

                loadPendingOffline(isFuelSale, () -> {
                    // ✅ اعرض pending مباشرة (اختياري لكنه ممتاز UX)
                    rebuildDisplayListOnlineMode(true);

                    // ✅ الآن ابدأ تحميل السيرفر
                    fetchInvoicesOnline(showDialog, 1, true);
                });
            } else {
                // pagination للـ online فقط
                fetchInvoicesOnline(showDialog, page, false);
            }
        } else {
            // ✅ Offline mode: pending + cached(invoices table)
            if (isReset) {

                loadPendingOffline(isFuelSale, () -> {
                    // ✅ اعرض pending مباشرة
                    rebuildDisplayListOfflineMode();

                    // ✅ الآن ابدأ تحميل الكاش
                    fetchInvoicesOffline(showDialog, 1, true);
                });
            } else {
                fetchInvoicesOffline(showDialog, page, false);
            }
        }
    }



    private void readExtras() {
        Bundle extras = getIntent() != null ? getIntent().getExtras() : null;
        if (extras == null) {
            isCustomerMode = false;
            return;
        }

        invoiceType = extras.getInt("invoice_type", 0); // default invoices
        customerId = safe(extras.getString("id"));
        if (customerId.trim().isEmpty()) {
            customerId = safe(extras.getString("customer_id"));
        }
        accountId = safe(extras.getString("account_id"));
        customerName = safe(extras.getString("name"));
        customerMobile = safe(extras.getString("mobile"));

        isCustomerMode = !accountId.trim().isEmpty();
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

                if (getAppData() != null) {
                    setting = getAppData().getSetting(); // عدّل حسب مشروعك
                }

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
            public void onScrolled(@androidx.annotation.NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);

                if (dy <= 0) return;
                if (isLoading || !hasNextPage) return;

                int visibleCount = lm.getChildCount();
                int totalCount = lm.getItemCount();
                int firstVisible = lm.findFirstVisibleItemPosition();

                if ((visibleCount + firstVisible) >= (totalCount - 3)) {
                    fetchInvoicesSmart(false, currentPage + 1, false);
                }
            }
        });
    }

    private void rebuildDisplayListOnlineMode(boolean isResetOnline) {
        displayItems.clear();
        displayItems.addAll(pendingOffline);
        displayItems.addAll(onlineItems);
        adapter.notifyDataSetChanged();
    }

    private void rebuildDisplayListOfflineMode() {
        displayItems.clear();
        displayItems.addAll(pendingOffline);
        displayItems.addAll(cachedItems);
        adapter.notifyDataSetChanged();
    }

    private void loadPendingOffline(int isFuelSale, @Nullable Runnable done) {

        dbExecutor.execute(() -> {

            List<OfflineInvoiceEntity> rows;

            Integer aid = null;
            try { if (accountId != null && !accountId.trim().isEmpty()) aid = Integer.parseInt(accountId); }
            catch (Exception ignored) {}

            Integer cid = null;
            try { if (customerId != null && !customerId.trim().isEmpty()) cid = Integer.parseInt(customerId); }
            catch (Exception ignored) {}

            if (aid != null && aid > 0) {
                rows = db.offlineInvoiceDao().getPendingByAccountAndType(aid, isFuelSale);
            } else if (cid != null && cid > 0) {
                rows = db.offlineInvoiceDao().getPendingByCustomerAndType(cid, isFuelSale);
            } else {
                rows = db.offlineInvoiceDao().getPendingByType(isFuelSale);
            }

            ArrayList<InvoiceDto> mapped = new ArrayList<>();
            if (rows != null) {
                for (OfflineInvoiceEntity e : rows) {
                    mapped.add(mapOfflineEntityToDto(e));
                }
            }

            mainHandler.post(() -> {
                pendingOffline.clear();
                pendingOffline.addAll(mapped);
                if (done != null) done.run();
            });
        });
    }

    private InvoiceDto mapOfflineEntityToDto(OfflineInvoiceEntity e) {
        InvoiceDto dto = new InvoiceDto();

        dto.is_offline = true;
        dto.local_id = e.localId;
        dto.sync_status = e.syncStatus;
        dto.sync_error = e.syncError;

        // عشان list و UI
        dto.id = e.onlineId != null ? e.onlineId : 0;
//        dto.customer_id = e.customerId; // إذا عندك بالحقل، أو تجاهل
        dto.account_id = e.accountId;

        dto.statement = e.statement;
        dto.invoice_no = (e.invoiceNo != null && !e.invoiceNo.trim().isEmpty())
                ? e.invoiceNo
                : ("OFF-" + e.localId);

        dto.total = e.total;
        dto.notes = e.notes;

        // date display (بدك شكل لطيف)
        dto.date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
                .format(new java.util.Date(e.createdAtTs));

        return dto;
    }

    private void initRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> fetchInvoicesSmart(false, 1, true));
    }

    // ============================================================
    // API
    // ============================================================
    private void fetchInvoicesOnline(boolean showDialog, int page, boolean isReset) {
        if (isLoading) return;

        isLoading = true;
        if (showDialog) showProgressHUD();
        binding.swipeRefreshLayout.setRefreshing(!showDialog);

        Map<String, String> params;

        if (isCustomerMode) {
            params = ApiClient.mapOf(
                    "page", String.valueOf(page),
                    "account_id", accountId
            );
        } else {
            params = ApiClient.mapOf(
                    "page", String.valueOf(page)
            );
        }

        Type type = new TypeToken<BaseResponse<InvoicesPaginationDto>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.CUSTOMERS_GETINVOICES, // لازم يكون معرف عندكم
                params,
                null,
                type,
                0,
                new ApiCallback<InvoicesPaginationDto>() {

                    @Override
                    public void onSuccess(InvoicesPaginationDto data, String msg, String rawJson) {
                        isLoading = false;
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);

                        if (data == null) {
                            toast(getString(R.string.general_error));
                            return;
                        }

                        if(data.data.size() >0){
                            if(data.data.get(0).account.getBalance() != null){
                                binding.amount.setText(""+data.data.get(0).account.getBalance());
                            }else if(data.data.get(0).account.getCredit() != null && data.data.get(0).account.getDepit() != null){
                                double balance  = data.data.get(0).account.getCredit() - data.data.get(0).account.getDepit();
                                binding.amount.setText(""+balance);
                            }
                        }


                        List<InvoiceDto> list = (data.data != null) ? data.data : new ArrayList<>();

                        if (isReset) {
                            onlineItems.clear();
                            onlineItems.addAll(list);
                            currentPage = (data.current_page != null) ? data.current_page : 1;
                        } else {
                            onlineItems.addAll(list);
                            currentPage = (data.current_page != null) ? data.current_page : page;
                        }

                        hasNextPage = data.next_page_url != null && !data.next_page_url.trim().isEmpty();

                        // ✅ rebuild العرض: pending + online
                        rebuildDisplayListOnlineMode(isReset);

                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);

//                        if (isReset) {
//                            items.clear();
//                            adapter.setItems(list);
//                            currentPage = (data.current_page != null) ? data.current_page : 1;
//                        } else {
//                            adapter.addItems(list);
//                            currentPage = (data.current_page != null) ? data.current_page : page;
//                        }

//                        hasNextPage = data.next_page_url != null && !data.next_page_url.trim().isEmpty();
                    }

                    @Override
                    public void onError(ApiError error) {
                        isLoading = false;
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        toast(error != null ? error.message : getString(R.string.general_error));
                    }

                    @Override
                    public void onUnauthorized(String rawJson) {
                        isLoading = false;
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        logout();
                    }

                    @Override
                    public void onNetworkError(String reason) {
                        isLoading = false;
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        toast(R.string.no_internet);
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        isLoading = false;
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        errorLogger("CustomerInvoicesParseError", e.getMessage() == null ? "null" : e.getMessage());
                        toast(getString(R.string.general_error));
                    }
                }
        );
    }

    private void fetchInvoicesOffline(boolean showDialog, int page, boolean isReset) {
        if (isLoading) return;

        isLoading = true;
        if (showDialog) showProgressHUD();
        binding.swipeRefreshLayout.setRefreshing(!showDialog);

        final int offset = (page - 1) * pageSize;
        int isFuelSale = (invoiceType == 1) ? 1 : 0;


        dbExecutor.execute(() -> {

            List<InvoiceEntity> rows;
            int total;

            Integer cid = null;
            try {
                if (customerId != null && !customerId.trim().isEmpty()) {
                    cid = Integer.parseInt(customerId);
                }
            } catch (Exception ignored) {}

            // ✅ الحالة 1: عندنا customerId => فلترة حسب العميل
            if (cid != null && cid > 0) {
                rows = db.invoiceDao().getByCustomerAndTypePaged(cid, isFuelSale, pageSize, offset);
                total = db.invoiceDao().countByCustomerAndType(cid, isFuelSale);
            }
            // ✅ الحالة 2: ما عندنا customerId => جيب الكل مع pagination
            else {
                rows = db.invoiceDao().getByTypePaged(isFuelSale, pageSize, offset);
                total = db.invoiceDao().countByType(isFuelSale);
            }

            ArrayList<InvoiceDto> mapped = new ArrayList<>();
            if (rows != null) {
                for (InvoiceEntity e : rows) {
                    mapped.add(mapInvoiceEntityToDto(e));
                }
            }

            boolean hasMore = (offset + mapped.size()) < total;

            // ✅ balance: بس لو customer mode
            String headerText = "";
            if (isCustomerMode) {
                Integer aidExtra = null;
                try {
                    if (accountId != null && !accountId.trim().isEmpty()) {
                        aidExtra = Integer.parseInt(accountId);
                    }
                } catch (Exception ignored) {}

                OfflineAccountSummary acc = getOfflineAccountSummary(aidExtra, rows);

                Double headerBalance = null;
                try {
                    if (acc.balance != null) headerBalance = acc.balance;
                    else if (acc.credit != null && acc.depit != null) headerBalance = acc.credit - acc.depit;
                } catch (Exception ignored) {}

                headerText = (headerBalance != null) ? String.valueOf(headerBalance) : "0";
            }

            final String finalHeaderText = headerText;

            mainHandler.post(() -> {
                isLoading = false;
                if (showDialog) hideProgressHUD();
                binding.swipeRefreshLayout.setRefreshing(false);

                if (isReset) {
                    cachedItems.clear();
                    cachedItems.addAll(mapped);
                    currentPage = page;
                } else {
                    cachedItems.addAll(mapped);
                    currentPage = page;
                }

                hasNextPage = hasMore;

                // ✅ rebuild العرض: pending + cached
                rebuildDisplayListOfflineMode();

                if (isCustomerMode) binding.amount.setText(finalHeaderText);
            });
        });
    }

    private InvoiceDto mapInvoiceEntityToDto(InvoiceEntity e) {
        InvoiceDto dto = new InvoiceDto();
        dto.id = e.id;
        dto.date = e.date;
        dto.statement = e.statement;
        dto.account_id = e.accountId;
        dto.store_id = e.storeId;
        dto.discount = e.discount;
        dto.total = e.total;
        dto.invoice_no = e.invoiceNo;
        dto.notes = e.notes;
        dto.campaign_id = e.campaignId;
        dto.pump_id = e.pumpId;
        dto.customer_vehicle_id = e.customerVehicleId;
        dto.created_at = e.createdAt;
        return dto;
    }


    private void openInvoiceDetails(InvoiceDto inv) {
        if (inv == null) return;

        Bundle b = new Bundle();

        // ✅ OFFLINE (Pending/Failed/Offline local)
        if (inv.is_offline) {

            // Flag for details screen
            b.putBoolean("is_offline", true);

            // Local PK (must be used by details screen)
            b.putLong("local_id", inv.local_id);

            // Optional: pass sync info
            b.putInt("sync_status", inv.sync_status);
            b.putString("sync_error", inv.sync_error);

            // Snapshot fields for header / quick display
            b.putString("invoice_no", inv.invoice_no);
            b.putString("date", inv.date);
            b.putString("statement", inv.statement);
            b.putString("total", inv.total != null ? String.valueOf(inv.total) : "");
            b.putString("notes", inv.notes);

            // Optional: if you keep request json in dto (recommended)
//            if (inv.request_json != null && !inv.request_json.trim().isEmpty()) {
//                b.putString("request_json", inv.request_json);
//            }

            moveToActivity(CustomerInvoicesActivity.this, InvoiceDetailsActivity.class, b, false);
            return;
        }

        // ✅ ONLINE
        b.putBoolean("is_offline", false);

        b.putInt("invoice_id", inv.id);
        b.putString("invoice_no", inv.invoice_no);
        b.putString("date", inv.date);
        b.putString("statement", inv.statement);
        b.putString("total", inv.total != null ? String.valueOf(inv.total) : "");
        b.putString("notes", inv.notes);

        // If online and details already included, pass raw json
        if (connectionAvailable && inv.details != null && !inv.details.isEmpty()) {
            b.putString("raw_invoice_json", getGson().toJson(inv));
        }

        moveToActivity(CustomerInvoicesActivity.this, InvoiceDetailsActivity.class, b, false);
    }

    private static class OfflineAccountSummary {
        String name = "";
        Double balance = null;
        Double credit = null;
        Double depit = null;
    }

    private OfflineAccountSummary getOfflineAccountSummary(Integer accountIdFromExtras, List<InvoiceEntity> rows) {
        OfflineAccountSummary s = new OfflineAccountSummary();

        Integer aid = null;

        // 1) الأفضل: من extras
        try {
            if (accountIdFromExtras != null && accountIdFromExtras > 0) {
                aid = accountIdFromExtras;
            }
        } catch (Exception ignored) {}

        // 2) fallback: من أول فاتورة
        if (aid == null || aid <= 0) {
            try {
                if (rows != null && !rows.isEmpty() && rows.get(0) != null) {
                    aid = rows.get(0).accountId;
                }
            } catch (Exception ignored) {}
        }

        if (aid == null || aid <= 0) return s;

        // 3) اقرأ من AccountEntity
        try {
            co.highfive.petrolstation.data.local.entities.AccountEntity ae =
                    db.accountDao().getById(aid);

            if (ae != null) {
                if (ae.name != null) s.name = ae.name;

                // ✅ اربط الحقول حسب AccountEntity عندك
                // s.balance = ae.balance;
                // s.credit  = ae.credit;
                // s.depit   = ae.depit;
            }
        } catch (Exception ignored) {}

        // 4) fallback: اقرأ من CustomerEntity حسب accountId
        try {
            CustomerEntity ce = db.customerDao().getByAccountId(aid);
            if (ce != null) {
                if (s.name.trim().isEmpty() && ce.name != null) s.name = ce.name;

                // ✅ اربط الحقول حسب CustomerEntity عندك
                 if (s.balance == null) s.balance = ce.balance;
                // if (s.credit == null)  s.credit  = ce.credit;
                // if (s.depit == null)   s.depit   = ce.depit;
            }
        } catch (Exception ignored) {}

        return s;
    }


}
