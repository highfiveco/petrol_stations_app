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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.adapters.CustomerInvoicesAdapter;
import co.highfive.petrolstation.customers.dto.InvoiceDto;
import co.highfive.petrolstation.customers.dto.InvoicesPaginationDto;
import co.highfive.petrolstation.data.local.AppDatabase;
import co.highfive.petrolstation.data.local.DatabaseProvider;
import co.highfive.petrolstation.data.local.entities.CustomerEntity;
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

    private CustomerInvoicesAdapter adapter;

    private final ArrayList<InvoiceDto> pendingOffline = new ArrayList<>();
    private final ArrayList<InvoiceDto> onlineItems = new ArrayList<>();
    private final ArrayList<InvoiceDto> cachedItems = new ArrayList<>();
    private final ArrayList<InvoiceDto> displayItems = new ArrayList<>();

    private boolean isLoading = false;
    private boolean hasNextPage = true;
    private int currentPage = 1;

    private final int pageSize = 10;
    private final int isFuelSale = 1;

    private boolean isCustomerMode = false;

    private String accountId = "";
    private String customerName = "";
    private String customerMobile = "";

    private boolean isOfflineCustomer = false;
    private long offlineCustomerLocalId = 0;

    private int customerIdInt = 0;

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

        fetchFuelSalesSmart(true, 1, true);
    }

    private void readExtras() {
        Bundle extras = getIntent() != null ? getIntent().getExtras() : null;
        if (extras == null) return;

        isOfflineCustomer = extras.getBoolean("is_offline", false);
        offlineCustomerLocalId = extras.getLong("offline_local_id", 0);

        String cidStr = safe(extras.getString("customer_id"));
        if (cidStr.trim().isEmpty()) cidStr = safe(extras.getString("id"));

        try { customerIdInt = Integer.parseInt(cidStr); } catch (Exception ignored) { customerIdInt = 0; }

        accountId = safe(extras.getString("account_id"));
        customerName = safe(extras.getString("name"));
        customerMobile = safe(extras.getString("mobile"));

        isCustomerMode = (customerIdInt != 0) || (accountId != null && !accountId.trim().isEmpty());
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
        adapter = new CustomerInvoicesAdapter(this, displayItems, new CustomerInvoicesAdapter.Listener() {
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

            @Override
            public void onSend(InvoiceDto invoice) {
                sendOneOfflineInvoice(invoice);
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
                    fetchFuelSalesSmart(false, currentPage + 1, false);
                }
            }
        });
    }

    private void initRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> fetchFuelSalesSmart(false, 1, true));
    }

    private void fetchFuelSalesSmart(boolean showDialog, int page, boolean isReset) {
        if (isLoading) return;

        if (connectionAvailable) {
            if (isReset) {
                loadPendingOffline(() -> {
                    rebuildDisplayListOnlineMode(true);
                    fetchFuelSalesOnline(showDialog, 1, true);
                });
            } else {
                fetchFuelSalesOnline(showDialog, page, false);
            }
        } else {
            if (isReset) {
                loadPendingOffline(() -> {
                    rebuildDisplayListOfflineMode();

                    if (customerIdInt < 0 || isOfflineCustomer) {
                        currentPage = 1;
                        hasNextPage = false;
                        if (isCustomerMode) binding.amount.setText("0");
                        return;
                    }

                    fetchFuelSalesOffline(showDialog, 1, true);
                });
            } else {
                fetchFuelSalesOffline(showDialog, page, false);
            }
        }
    }

    private void loadPendingOffline(@Nullable Runnable done) {

        errorLogger("loadPendingOffline","loadPendingOffline");
        dbExecutor.execute(() -> {
            List<OfflineInvoiceEntity> rows;

            Integer aid = null;
            try { if (accountId != null && !accountId.trim().isEmpty()) aid = Integer.parseInt(accountId); }
            catch (Exception ignored) {}

            Integer cid = customerIdInt;

            errorLogger("customerIdInt",""+customerIdInt);
            if (aid != null && aid > 0) {
                rows = db.offlineInvoiceDao().getPendingByAccountAndType(aid, isFuelSale);
            } else if (cid != null && cid != 0) {
                errorLogger("cid",""+cid);
                rows = db.offlineInvoiceDao().getPendingByCustomerAndType(cid, isFuelSale);
            } else {
                rows = db.offlineInvoiceDao().getPendingByType(isFuelSale);
            }

            ArrayList<InvoiceDto> mapped = new ArrayList<>();
            if (rows != null) {
                for (OfflineInvoiceEntity e : rows) mapped.add(mapOfflineEntityToDto(e));
            }

            mainHandler.post(() -> {
                pendingOffline.clear();
                pendingOffline.addAll(mapped);
                if (done != null) done.run();
            });
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

    private void fetchFuelSalesOnline(boolean showDialog, int page, boolean isReset) {
        if (isLoading) return;

        isLoading = true;
        if (showDialog) showProgressHUD();
        binding.swipeRefreshLayout.setRefreshing(!showDialog);

        Map<String, String> params;

        if (isCustomerMode) {
            if (accountId != null && !accountId.trim().isEmpty()) {
                params = ApiClient.mapOf(
                        "page", String.valueOf(page),
                        "account_id", accountId
                );
            } else {
                params = ApiClient.mapOf(
                        "page", String.valueOf(page),
                        "customer_id", String.valueOf(customerIdInt)
                );
            }
        } else {
            params = ApiClient.mapOf("page", String.valueOf(page));
        }

        Type type = new TypeToken<BaseResponse<InvoicesPaginationDto>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.CUSTOMERS_GETFUELSALES,
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

                        try {
                            if (data.data != null && !data.data.isEmpty() && data.data.get(0) != null && data.data.get(0).account != null) {
                                if (data.data.get(0).account.getBalance() != null) {
                                    binding.amount.setText("" + data.data.get(0).account.getBalance());
                                } else if (data.data.get(0).account.getCredit() != null && data.data.get(0).account.getDepit() != null) {
                                    double balance = data.data.get(0).account.getCredit() - data.data.get(0).account.getDepit();
                                    binding.amount.setText("" + balance);
                                }
                            }
                        } catch (Exception ignored) {}

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

                        rebuildDisplayListOnlineMode(isReset);
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
                        toast(getString(R.string.general_error));
                    }
                }
        );
    }

    private void fetchFuelSalesOffline(boolean showDialog, int page, boolean isReset) {
        if (isLoading) return;

        isLoading = true;
        if (showDialog) showProgressHUD();
        binding.swipeRefreshLayout.setRefreshing(!showDialog);

        final int offset = (page - 1) * pageSize;

        dbExecutor.execute(() -> {

            if (customerIdInt < 0 || isOfflineCustomer) {
                mainHandler.post(() -> {
                    isLoading = false;
                    if (showDialog) hideProgressHUD();
                    binding.swipeRefreshLayout.setRefreshing(false);

                    if (isReset) cachedItems.clear();
                    currentPage = 1;
                    hasNextPage = false;

                    rebuildDisplayListOfflineMode();
                    if (isCustomerMode) binding.amount.setText("0");
                });
                return;
            }

            int total = 0;
            ArrayList<InvoiceDto> mapped = new ArrayList<>();

            Integer cid = customerIdInt;

            if (cid != null && cid != 0) {

                List<co.highfive.petrolstation.data.local.dao.InvoiceWithCustomerLite> rows =
                        db.invoiceDao().getByCustomerAndTypePagedWithCustomer(cid, isFuelSale, pageSize, offset);

                total = db.invoiceDao().countByCustomerAndType(cid, isFuelSale);

                if (rows != null) {
                    for (co.highfive.petrolstation.data.local.dao.InvoiceWithCustomerLite r : rows) {
                        mapped.add(mapInvoiceRowToDto(r));
                    }
                }

                boolean hasMore = (offset + mapped.size()) < total;
                final boolean finalHasMore = hasMore;

                String headerText = "";
                if (isCustomerMode) {

                    Integer aidExtra = null;
                    try {
                        if (accountId != null && !accountId.trim().isEmpty()) {
                            aidExtra = Integer.parseInt(accountId);
                        }
                    } catch (Exception ignored) {}

                    List<InvoiceEntity> invoiceEntities = new ArrayList<>();
                    if (rows != null) {
                        for (co.highfive.petrolstation.data.local.dao.InvoiceWithCustomerLite r : rows) {
                            if (r != null && r.invoice != null) invoiceEntities.add(r.invoice);
                        }
                    }

                    OfflineAccountSummary acc = getOfflineAccountSummary(aidExtra);

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
                    } else {
                        cachedItems.addAll(mapped);
                    }

                    currentPage = page;
                    hasNextPage = finalHasMore;

                    rebuildDisplayListOfflineMode();

                    if (isCustomerMode) binding.amount.setText(finalHeaderText);
                });

                return;
            }

            List<co.highfive.petrolstation.data.local.dao.InvoiceWithCustomerLite> rowsAll =
                    db.invoiceDao().getByTypePagedWithCustomer(isFuelSale, pageSize, offset);

            total = db.invoiceDao().countByType(isFuelSale);

            if (rowsAll != null) {
                for (co.highfive.petrolstation.data.local.dao.InvoiceWithCustomerLite r : rowsAll) {
                    mapped.add(mapInvoiceRowToDto(r));
                }
            }

            boolean hasMore = (offset + mapped.size()) < total;
            final boolean finalHasMore = hasMore;

            mainHandler.post(() -> {
                isLoading = false;
                if (showDialog) hideProgressHUD();
                binding.swipeRefreshLayout.setRefreshing(false);

                if (isReset) {
                    cachedItems.clear();
                    cachedItems.addAll(mapped);
                } else {
                    cachedItems.addAll(mapped);
                }

                currentPage = page;
                hasNextPage = finalHasMore;

                rebuildDisplayListOfflineMode();
            });
        });
    }

    private InvoiceDto mapOfflineEntityToDto(OfflineInvoiceEntity e) {
        InvoiceDto dto = new InvoiceDto();

        dto.is_offline = true;
        dto.local_id = e.localId;
        dto.sync_status = e.syncStatus;
        dto.sync_error = e.syncError;

        dto.id = e.onlineId != null ? e.onlineId : 0;
        dto.account_id = e.accountId;

        dto.statement = e.statement;
        dto.invoice_no = (e.invoiceNo != null && !e.invoiceNo.trim().isEmpty())
                ? e.invoiceNo
                : ("OFF-" + e.localId);

        dto.total = e.total;
        dto.notes = e.notes;

        dto.date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
                .format(new java.util.Date(e.createdAtTs));

        if (dto.account == null) dto.account = new co.highfive.petrolstation.models.Account();
        dto.account.setAccount_name(e.customerName != null ? e.customerName : "");
        dto.account.setMobile(e.customerMobile != null ? e.customerMobile : "");

        return dto;
    }

    private InvoiceDto mapInvoiceRowToDto(co.highfive.petrolstation.data.local.dao.InvoiceWithCustomerLite r) {
        InvoiceEntity e = r.invoice;

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
        dto.is_fuel_sale = e.isFuelSale;

        if (dto.account == null) dto.account = new co.highfive.petrolstation.models.Account();
        dto.account.setAccount_name(r.customerName != null ? r.customerName : "");
        dto.account.setMobile(r.customerMobile != null ? r.customerMobile : "");

        return dto;
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

    private void sendOneOfflineInvoice(InvoiceDto inv) {
        if (inv == null || !inv.is_offline || inv.local_id <= 0) {
            toast("This invoice is not offline");
            return;
        }

        if (!connectionAvailable) {
            toast(getString(R.string.no_internet));
            return;
        }

        showProgressHUD();

        ExecutorService ex = Executors.newSingleThreadExecutor();
        Handler main = new Handler(Looper.getMainLooper());

        ex.execute(() -> repo.syncOneInvoiceJson(inv.local_id, new InvoiceLocalRepository.SingleSyncListener() {

            @Override
            public void onSuccess() {
                main.post(() -> {
                    hideProgressHUD();
                    toast(getString(R.string.done));
                    fetchFuelSalesSmart(false, 1, true);
                });
            }

            @Override
            public void onFailed(@NonNull String errorMsg) {
                main.post(() -> {
                    hideProgressHUD();
                    toast(errorMsg);
                    fetchFuelSalesSmart(false, 1, true);
                });
            }

            @Override
            public void onNetwork(@NonNull String reason) {
                main.post(() -> {
                    hideProgressHUD();
                    toast(getString(R.string.no_internet));
                    fetchFuelSalesSmart(false, 1, true);
                });
            }
        }));
    }

    private static class OfflineAccountSummary {
        String name = "";
        Double balance = null;
        Double credit = null;
        Double depit = null;
    }

    private OfflineAccountSummary getOfflineAccountSummary(Integer accountIdFromExtras) {
        OfflineAccountSummary s = new OfflineAccountSummary();

        Integer aid = null;
        try {
            if (accountIdFromExtras != null && accountIdFromExtras > 0) {
                aid = accountIdFromExtras;
            }
        } catch (Exception ignored) {}

        if (aid == null || aid <= 0) return s;

        try {
            CustomerEntity ce = db.customerDao().getByAccountId(aid);
            if (ce != null) {
                if (ce.name != null) s.name = ce.name;
                s.balance = ce.balance;
            }
        } catch (Exception ignored) {}

        return s;
    }
}
