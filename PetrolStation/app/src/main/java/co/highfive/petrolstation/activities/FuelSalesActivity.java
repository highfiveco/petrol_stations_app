package co.highfive.petrolstation.activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.adapters.FuelSalesAdapter;
import co.highfive.petrolstation.customers.dto.FuelSaleDetailDto;
import co.highfive.petrolstation.customers.dto.FuelSaleDto;
import co.highfive.petrolstation.customers.dto.FuelSalesPagingDto;
import co.highfive.petrolstation.customers.dto.GetFuelSalesResponse;
import co.highfive.petrolstation.customers.dto.PagedResponse;
import co.highfive.petrolstation.databinding.ActivityFuelSalesBinding;
import co.highfive.petrolstation.fragments.DeleteDialog;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.models.Setting;
import co.highfive.petrolstation.models.TableItem;
import co.highfive.petrolstation.models.User;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.ApiError;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;
import co.highfive.petrolstation.utils.SunmiPrintHelper;

public class FuelSalesActivity extends BaseActivity {

    private ActivityFuelSalesBinding binding;

    private String customerId = "";
    private String accountId = "";
    private String customerName = "";
    private String customerMobile = "";

    private final ArrayList<FuelSaleDto> items = new ArrayList<>();
    private FuelSalesAdapter adapter;

    private int page = 1;
    private boolean isLoading = false;
    private boolean hasMore = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFuelSalesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI(binding.mainLayout);

        readExtras();
        initHeader();
        initRecycler();
        initRefresh();
        initClicks();

        refreshFirstPage(true);
    }


    private void readExtras() {
        Bundle extras = getIntent() != null ? getIntent().getExtras() : null;
        if (extras == null) return;

        customerId = safe(extras.getString("id"));
        accountId = safe(extras.getString("account_id"));
        customerName = safe(extras.getString("name"));
        customerMobile = safe(extras.getString("mobile"));
    }

    private void initHeader() {
        binding.name.setText(customerName);
        binding.phone.setText(customerMobile);

        // amount إذا ما عندك قيمة جاهزة من extras خليها فاضية
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
    }

    private void initRecycler() {
        adapter = new FuelSalesAdapter(items, new FuelSalesAdapter.Listener() {
            @Override
            public void onPrint(FuelSaleDto sale) {
                Setting setting = null;

                if (getAppData() != null) {
                    setting = getAppData().getSetting(); // عدّل حسب مشروعك
                }

                if (setting == null) {
                    toast(getString(R.string.general_error));
                    return;
                }

                printFuelSale(setting, sale);
            }

            @Override
            public void onDelete(FuelSaleDto sale) {
                // ما عندنا endpoint delete للفويل سيلز في Endpoints اللي أرسلتها
                // فبعرض Dialog بس وبعدين toast. لما تعطيني endpoint بنركّب request.
                confirmDelete(sale);
            }

            @Override
            public void onOpen(FuelSaleDto sale) {
                // لو بدك شاشة تفاصيل: FuelSaleDetailsActivity لاحقاً
            }
        });

        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);

        binding.recycler.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull androidx.recyclerview.widget.RecyclerView rv, int dx, int dy) {
                if (dy <= 0) return;
                if (isLoading || !hasMore) return;

                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                if (lm == null) return;

                int lastVisible = lm.findLastVisibleItemPosition();
                if (lastVisible >= adapter.getItemCount() - 3) loadNextPage();
            }
        });
    }


    public void printFuelSale(Setting setting, FuelSaleDto sale) {
        try {
            if (setting == null || sale == null) return;

            int no_of_copy = 1;
            try {
                if (setting.getNo_print_copies() != null) {
                    no_of_copy = Integer.parseInt(setting.getNo_print_copies());
                }
            } catch (Exception ignored) {}

            // user from session
            User user = null;
            try {
                user = getGson().fromJson(
                        getSessionManager().getString(getSessionKeys().userJson),
                        User.class
                );
            } catch (Exception ignored) {}

            java.text.SimpleDateFormat sdf =
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.ENGLISH);
            String printedAt = sdf.format(new java.util.Date());
            String printedBy = (user != null && user.getName() != null && !user.getName().trim().isEmpty())
                    ? user.getName().trim()
                    : "-";

            for (int copy = 0; copy < no_of_copy; copy++) {

                // =========================
                // Header (Station Name + Address)
                // =========================
                SunmiPrintHelper.getInstance().changeFontBold();
                SunmiPrintHelper.getInstance().setAlign(1);

                String stationName = safe(setting.getName());
                String stationAddress = safe(setting.getAddress());

                if (!stationName.isEmpty()) {
                    SunmiPrintHelper.getInstance().printTable(new String[]{stationName}, new int[]{1}, new int[]{1});
                }
                if (!stationAddress.isEmpty()) {
                    SunmiPrintHelper.getInstance().printTable(new String[]{stationAddress}, new int[]{1}, new int[]{1});
                }

                SunmiPrintHelper.getInstance().cancelFontBold();

                // dashed separator
                printDashedLine();

                // =========================
                // Invoice Basic Info (Customer / Invoice No / Date)
                // =========================
                SunmiPrintHelper.getInstance().setAlign(2);
                SunmiPrintHelper.getInstance().changeFontBold();

                String customerName = (sale.account != null) ? safe(sale.account.getAccount_name()) : "";
                String invoiceNo = safe(sale.invoice_no);
                String invoiceDate = safe(sale.date);

                SunmiPrintHelper.getInstance().printTable(
                        new String[]{"الاسم : " + customerName},
                        new int[]{1},
                        new int[]{2}
                );

                SunmiPrintHelper.getInstance().printTable(
                        new String[]{"رقم الفاتورة : " + invoiceNo},
                        new int[]{1},
                        new int[]{2}
                );

                SunmiPrintHelper.getInstance().printTable(
                        new String[]{"التاريخ : " + invoiceDate},
                        new int[]{1},
                        new int[]{2}
                );

                SunmiPrintHelper.getInstance().cancelFontBold();

                // dashed separator
                printDashedLine();

                // =========================
                // Items Table Header (الإجمالي | السعر | الكمية | الصنف)
                // =========================
                java.util.LinkedList<TableItem> itemsHeader = new java.util.LinkedList<>();
                itemsHeader.add(new TableItem(
                        new String[]{"الإجمالي", "السعر", "الكمية", "الصنف"},
                        new int[]{2, 2, 2, 4},
                        new int[]{1, 1, 1, 2}
                ));

                SunmiPrintHelper.getInstance().changeFontBold();
                for (TableItem t : itemsHeader) {
                    SunmiPrintHelper.getInstance().printTable(t.getText(), t.getWidth(), t.getAlign());
                }
                SunmiPrintHelper.getInstance().cancelFontBold();

                printLine();

                // =========================
                // Items Rows
                // =========================
                if (sale.details != null && !sale.details.isEmpty()) {
                    for (int i = 0; i < sale.details.size(); i++) {
                        FuelSaleDetailDto d = sale.details.get(i);
                        if (d == null) continue;

                        String itemName = (d.item != null) ? safe(d.item.name) : "-";
                        double qty = d.count;
                        double price = d.price;
                        double lineTotal = qty * price;

                        java.util.LinkedList<TableItem> row = new java.util.LinkedList<>();
                        row.add(new TableItem(
                                new String[]{
                                        formatNumber(lineTotal),
                                        formatNumber(price),
                                        formatNumber(qty),
                                        itemName
                                },
                                new int[]{2, 2, 2, 4},
                                new int[]{1, 1, 1, 2}
                        ));

                        for (TableItem t : row) {
                            SunmiPrintHelper.getInstance().printTable(t.getText(), t.getWidth(), t.getAlign());
                        }
                    }
                } else {
                    SunmiPrintHelper.getInstance().printTable(
                            new String[]{"لا توجد أصناف"},
                            new int[]{1},
                            new int[]{1}
                    );
                }

                // dashed separator
                printDashedLine();

                // =========================
                // Total (number far left)
                // =========================
                double total = sale.total;

                SunmiPrintHelper.getInstance().changeFontBold();

                java.util.LinkedList<TableItem> totalRow = new java.util.LinkedList<>();
                totalRow.add(new TableItem(
                        new String[]{formatNumber(total), "الإجمالي"},
                        new int[]{3, 3},
                        new int[]{0, 2}
                ));

                for (TableItem t : totalRow) {
                    SunmiPrintHelper.getInstance().printTable(t.getText(), t.getWidth(), t.getAlign());
                }

                SunmiPrintHelper.getInstance().cancelFontBold();

                // dashed separator
                printDashedLine();

                // =========================
                // Printed info table (SMALLER)
                // =========================

                // صغر العنوان (التاريخ/طبع بواسطة)
                SunmiPrintHelper.getInstance().changeFontSize(18);

                java.util.LinkedList<TableItem> printedHeader = new java.util.LinkedList<>();
                printedHeader.add(new TableItem(
                        new String[]{"التاريخ", "طبع بواسطة"},
                        new int[]{3, 3},
                        new int[]{1, 1}
                ));
                for (TableItem t : printedHeader) {
                    SunmiPrintHelper.getInstance().printTable(t.getText(), t.getWidth(), t.getAlign());
                }

                // صغر البيانات كمان (printedAt/printedBy)
                SunmiPrintHelper.getInstance().changeFontSize(18);
                SunmiPrintHelper.getInstance().changeFontBold();

                java.util.LinkedList<TableItem> printedRow = new java.util.LinkedList<>();
                printedRow.add(new TableItem(
                        new String[]{printedAt, printedBy},
                        new int[]{3, 3},
                        new int[]{1, 1}
                ));

                for (TableItem t : printedRow) {
                    SunmiPrintHelper.getInstance().printTable(t.getText(), t.getWidth(), t.getAlign());
                }

                SunmiPrintHelper.getInstance().cancelFontBold();

                // رجّع حجم الخط الطبيعي لباقي الفاتورة
                SunmiPrintHelper.getInstance().changeFontSize(24);

                // =========================
                // Footer
                // =========================
                SunmiPrintHelper.getInstance().setAlign(1);
                SunmiPrintHelper.getInstance().printTable(
                        new String[]{"نسخة مرخصة من هاي فايف"},
                        new int[]{1},
                        new int[]{1}
                );

                SunmiPrintHelper.getInstance().printTable(new String[]{""}, new int[]{1}, new int[]{0});
                SunmiPrintHelper.getInstance().printTable(new String[]{""}, new int[]{1}, new int[]{0});
                SunmiPrintHelper.getInstance().printTable(new String[]{""}, new int[]{1}, new int[]{0});

                if (copy < no_of_copy - 1) {
                    try { Thread.sleep(2500); } catch (InterruptedException ignored) {}
                }
            }

        } catch (Exception e) {
            errorLogger("printFuelSale", e.getMessage() == null ? "null" : e.getMessage());
        }
    }



    private void initRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> refreshFirstPage(false));
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
        if (accountId == null || accountId.trim().isEmpty()) {
            toast(getString(R.string.general_error));
            return;
        }

        isLoading = true;
        if (showDialog) showProgressHUD();

        Map<String, String> params = ApiClient.mapOf(
                "page", String.valueOf(targetPage),
                "account_id", accountId
        );

        Type type = new TypeToken<BaseResponse<FuelSalesPagingDto>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.CUSTOMERS_GETFUELSALES,
                params,
                null,
                type,
                0,
                new ApiCallback<FuelSalesPagingDto>() {
                    @Override
                    public void onSuccess(FuelSalesPagingDto data, String msg, String rawJson) {
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        isLoading = false;

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

                        List<FuelSaleDto> newItems = (data.data != null) ? data.data : new ArrayList<>();

                        if (newItems.isEmpty()) {
                            hasMore = false;
                        } else {
                            page = targetPage;
                            boolean noNext = (data.next_page_url == null || data.next_page_url.trim().isEmpty());
                            hasMore = !noNext;

                            adapter.addAll(newItems);
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
                        errorLogger("FuelSalesParseError", e.getMessage() == null ? "null" : e.getMessage());
                        toast(getString(R.string.general_error));
                    }
                }
        );
    }


    private void confirmDelete(FuelSaleDto sale) {
        if (sale == null) return;

        DeleteDialog dialog = new DeleteDialog();
        dialog.setCancelable(false);

        dialog.setSuccessListener(success -> {
            if (success) {
                // ✅ لما المستخدم يوافق: نفذ الحذف + اخفاء الديلوج بعد النجاح
                deleteInvoiceRequest(safe(String.valueOf(sale.id)), dialog);
            } else {
                // ✅ لو كبس لا
                dialog.dismiss();
            }
        });

        dialog.show(getSupportFragmentManager(), "DeleteDialogFuelSale");
    }

    private void deleteInvoiceRequest(String invoiceId, DeleteDialog dialog) {
        showProgressHUD();

        Map<String, String> params = ApiClient.mapOf(
                "id", safe(invoiceId)
        );

        Type type = new TypeToken<BaseResponse<Object>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_POST,
                Endpoints.CUSTOMERS_DELETEINVOICE, // ✅ /api/customers/deleteInvoice
                params,
                null,
                type,
                0,
                new ApiCallback<Object>() {
                    @Override
                    public void onSuccess(Object data, String msg, String rawJson) {
                        hideProgressHUD();

                        // ✅ اخفاء ديلوج التأكيد بعد نجاح الحذف
                        if (dialog != null) dialog.dismissAllowingStateLoss();

                        toast(msg != null && !msg.trim().isEmpty() ? msg : getString(R.string.done));

                        // ✅ اعادة تحميل البيانات من اول صفحة
                        refreshFirstPage(true);
                    }

                    @Override
                    public void onError(ApiError error) {
                        hideProgressHUD();
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
                        toast(R.string.no_internet);
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        hideProgressHUD();
                        errorLogger("DeleteInvoiceParseError", e.getMessage() == null ? "null" : e.getMessage());
                        toast(getString(R.string.general_error));
                    }
                }
        );
    }
}
