package co.highfive.petrolstation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.customers_settings.dto.LookupDto;
import co.highfive.petrolstation.data.local.entities.CustomerEntity;
import co.highfive.petrolstation.databinding.ActivityPosBinding;
import co.highfive.petrolstation.fragments.AddCustomerDialog;
import co.highfive.petrolstation.fragments.SelectCustomerDialog;
import co.highfive.petrolstation.fuelsale.dto.FuelCustomerDto;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.ApiError;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;
import co.highfive.petrolstation.pos.dto.PosActiveInvoice;
import co.highfive.petrolstation.pos.dto.PosItemDto;
import co.highfive.petrolstation.pos.dto.PosItemsData;
import co.highfive.petrolstation.pos.dto.PosSettingsData;
import co.highfive.petrolstation.pos.ui.PosCategoriesAdapter;
import co.highfive.petrolstation.pos.ui.PosItemsAdapter;

public class PosActivity extends BaseActivity {

    private ActivityPosBinding binding;

    private PosCategoriesAdapter categoriesAdapter;
    private PosItemsAdapter itemsAdapter;

    private final List<LookupDto> categories = new ArrayList<>();
    private final List<PosItemDto> items = new ArrayList<>();

    private int selectedCategoryId = 0; // 0 = all
    private String currentSearch = "";

    private final Map<Integer, Integer> qtyMap = new HashMap<>();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    private String lastCustomerSearch = "";
    private FuelCustomerDto selectedCustomer = null;
    private ArrayList<FuelCustomerDto> lastCustomerResults = new ArrayList<>();
    private String lastAddCustomerName = "";
    private String lastAddCustomerMobile = "";
    private co.highfive.petrolstation.pos.data.PosInvoiceDbHelper posDb;
    public String itemsJson; // الآن يحتوي items_details_json

    private int itemsPage = 1;
    private int itemsLastPage = 1;
    private boolean itemsLoading = false;
    private boolean itemsHasMore = true;

    private int lastReqCategory = 0;
    private String lastReqSearch = "";
    private long editingInvoiceId = -1; // -1 يعني فاتورة جديدة

    private final Map<Integer, co.highfive.petrolstation.pos.dto.PosDraftItemDto> selectedItemsCache = new HashMap<>();

    List<LookupDto> paymentTypes= new ArrayList<>();

    private co.highfive.petrolstation.data.local.AppDatabase db;
    private java.util.concurrent.ExecutorService dbExecutor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final int OFFLINE_PAGE_SIZE = 20; // نفس per_page تقريباً

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupUI(findViewById(android.R.id.content));
        posDb = new co.highfive.petrolstation.pos.data.PosInvoiceDbHelper(this);

        db = co.highfive.petrolstation.data.local.DatabaseProvider.get(this);
        dbExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();

        setupHeader();
        setupLists();
        setupSearchAndActions();

        loadPosSettings();
    }

    private void setupHeader() {
        binding.icBack.setOnClickListener(v -> finish());
        binding.icHome.setOnClickListener(v -> finish());

        refreshActiveInvoicesBadge();


        binding.icAddNewInvoice.setOnClickListener(v -> openSaveDraftDialog());


        binding.icCart.setOnClickListener(v -> {
            startActivityForResult(new Intent(this, PosInvoiceDetailsActivity.class), 2001);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 3001 && resultCode == RESULT_OK && data != null) {

            boolean done = data.getBooleanExtra(PosCheckoutActivity.EXTRA_CHECKOUT_DONE, false);

            if (done) {
                // ✅ تم الحفظ على السيرفر من checkout
                resetForNewInvoice();          // يمسح العميل + selections + qtyMap + editingInvoiceId
                refreshActiveInvoicesBadge();  // يحدث البادج من DB
                return;
            }

            // ✅ مش done يعني رجع من checkout تعديل فقط (Back / Add Items)
            long invoiceId = data.getLongExtra("pos_invoice_id", -1);
            if (invoiceId > 0) {
                loadInvoiceIntoScreen(invoiceId);
            }
            return;
        }
        if (requestCode == 2001 && resultCode == RESULT_OK && data != null) {
            long invoiceId = data.getLongExtra("pos_invoice_id", -1);
            if (invoiceId > 0) loadInvoiceIntoScreen(invoiceId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshActiveInvoicesBadge();
    }
    private void cacheItemIfNeeded(int itemId) {
        if (selectedItemsCache.containsKey(itemId)) return;

        for (PosItemDto it : items) {
            if (it != null && it.id == itemId) {
                co.highfive.petrolstation.pos.dto.PosDraftItemDto d = new co.highfive.petrolstation.pos.dto.PosDraftItemDto();
                d.itemId = it.id;
                d.name = it.name;
                d.price = it.price;
                d.qty = 0; // رح نعبّيها لاحقًا من qtyMap
                selectedItemsCache.put(itemId, d);
                return;
            }
        }
    }


    private String buildInvoiceItemsJson() {

        List<co.highfive.petrolstation.pos.dto.PosDraftItemDto> out = new ArrayList<>();

        if (qtyMap == null || qtyMap.isEmpty()) return "[]";

        for (Map.Entry<Integer, Integer> e : qtyMap.entrySet()) {
            int itemId = e.getKey();
            int qty = e.getValue() == null ? 0 : e.getValue();
            if (qty <= 0) continue;

            co.highfive.petrolstation.pos.dto.PosDraftItemDto cached = selectedItemsCache.get(itemId);

            // لو مش موجود بالكاش (مثلاً فاتورة قديمة رجعناها) حاول نلاقيه من items الحالية
            if (cached == null) {
                cacheItemIfNeeded(itemId);
                cached = selectedItemsCache.get(itemId);
            }

            // لو لسه null: على الأقل احفظ id + qty وما تخسر العنصر
            co.highfive.petrolstation.pos.dto.PosDraftItemDto d = new co.highfive.petrolstation.pos.dto.PosDraftItemDto();
            d.itemId = itemId;
            d.qty = qty;

            if (cached != null) {
                d.name = cached.name;
                d.price = cached.price;
            } else {
                d.name = "";   // أو "Item " + itemId
                d.price = 0;   // إذا السعر مش معروف
            }

            out.add(d);
        }

        return getGson().toJson(out);
    }

    private void fillQtyMapFromItemsJson(String itemsJson) {
        qtyMap.clear();

        if (itemsJson == null || itemsJson.trim().isEmpty()) return;

        try {
            com.google.gson.JsonArray arr = com.google.gson.JsonParser
                    .parseString(itemsJson)
                    .getAsJsonArray();

            for (int i = 0; i < arr.size(); i++) {
                com.google.gson.JsonObject o = arr.get(i).getAsJsonObject();

                int itemId = getIntAny(o, "itemId", "item_id", "id");
                int qty = getIntAny(o, "qty", "quantity", "q");

                if (itemId > 0 && qty > 0) {
                    qtyMap.put(itemId, qty);
                }
            }
        } catch (Exception ignore) {
            qtyMap.clear();
        }
    }

    private int getIntAny(com.google.gson.JsonObject o, String... keys) {
        for (String k : keys) {
            if (o.has(k) && !o.get(k).isJsonNull()) {
                try {
                    return o.get(k).getAsInt();
                } catch (Exception e) {
                    try {
                        return Integer.parseInt(o.get(k).getAsString());
                    } catch (Exception ignore) {}
                }
            }
        }
        return 0;
    }



    private void loadInvoiceIntoScreen(long invoiceId) {

        co.highfive.petrolstation.pos.dto.PosActiveInvoice inv = posDb.getInvoice(invoiceId);
        if (inv == null) {
            toast(getString(R.string.general_error));
            return;
        }

        // ✅ مهم: احفظ إننا الآن بنعدل على فاتورة موجودة
        editingInvoiceId = invoiceId;

        FuelCustomerDto c = new FuelCustomerDto();
        c.id = inv.customerId;
        c.name = inv.customerName;
        c.account_id = inv.accountId;

        if (inv.customerId < 0) {
            c.is_offline = true;
            c.local_id = Math.abs((long) inv.customerId);
        }

        selectedCustomer = c;
        binding.typeCustomer.setText(inv.customerName != null ? inv.customerName : "");
        lastCustomerSearch = inv.customerName != null ? inv.customerName : "";
        lastCustomerResults = new ArrayList<>(); // نتركها فاضية، لأننا رح نجيبها بالبحث لما يضغط

        fillQtyMapFromItemsJson(inv.itemsJson);
        selectedItemsCache.clear();

        try {
            com.google.gson.JsonArray arr = com.google.gson.JsonParser.parseString(inv.itemsJson).getAsJsonArray();
            for (int i = 0; i < arr.size(); i++) {
                com.google.gson.JsonObject o = arr.get(i).getAsJsonObject();

                int itemId = getIntAny(o, "itemId", "item_id", "id");
                int qty = getIntAny(o, "qty", "quantity", "q");

                String name = o.has("name") && !o.get("name").isJsonNull() ? o.get("name").getAsString() : "";
                double price = 0;
                try {
                    if (o.has("price") && !o.get("price").isJsonNull()) price = o.get("price").getAsDouble();
                } catch (Exception ignore) {}

                if (itemId > 0 && qty > 0) {
                    co.highfive.petrolstation.pos.dto.PosDraftItemDto d = new co.highfive.petrolstation.pos.dto.PosDraftItemDto();
                    d.itemId = itemId;
                    d.name = name;
                    d.price = price;
                    d.qty = qty;
                    selectedItemsCache.put(itemId, d);
                }
            }
        } catch (Exception ignored) {}

        if (itemsAdapter != null) itemsAdapter.setSelectionFromQtyMap(qtyMap);
//        if (itemsAdapter != null) itemsAdapter.notifyDataSetChanged();

        selectedCategoryId = 0;
        currentSearch = "";
        binding.search.setText("");
        if (categoriesAdapter != null) categoriesAdapter.setSelectedId(0);

        loadItems(0, "");
    }




    private void openSaveDraftDialog() {

        boolean hasItems = qtyMap != null && !qtyMap.isEmpty();
        boolean hasCustomer = isValidSelectedCustomer();

        // 1) إذا ما في Items أصلاً => ابدأ جديد مباشرة
        if (!hasItems) {
            resetForNewInvoice();
            return;
        }

        // 2) في Items لكن ما في Customer => ممنوع حفظ Draft
        if (!hasCustomer) {
            toast("اختر الزبون أولاً");
            return;
        }

        // 3) في Items + Customer => افتح Dialog مثل FuelSale
        co.highfive.petrolstation.fragments.ConfirmSaveFuelInvoiceDialog d =
                co.highfive.petrolstation.fragments.ConfirmSaveFuelInvoiceDialog.newInstance();

        d.setCancelable(false);

        d.setListener(new co.highfive.petrolstation.fragments.ConfirmSaveFuelInvoiceDialog.Listener() {
            @Override
            public void onSaveAndNew() {
                long id = saveActiveInvoiceDraft();
                if (id <= 0) {
                    toast(getString(R.string.general_error));
                    return;
                }
                resetForNewInvoice();
            }

            @Override
            public void onCancelCurrentAndNew() {
                resetForNewInvoice();
            }
        });

        d.show(getSupportFragmentManager(), "ConfirmSaveFuelInvoiceDialog");
    }

    private void resetForNewInvoice() {

        editingInvoiceId = -1;

        selectedCustomer = null;
        binding.typeCustomer.setText("");

        qtyMap.clear();
        selectedItemsCache.clear(); // ✅ مهم جدًا

        if (itemsAdapter != null) {
            itemsAdapter.clearSelection(); // ✅ حتى ما يضل الشكل سلكتد
            itemsAdapter.notifyDataSetChanged();
        }

        selectedCategoryId = 0;
        currentSearch = "";
        binding.search.setText("");

        if (categoriesAdapter != null) categoriesAdapter.setSelectedId(0);

        loadItems(0, "");
    }



    private void setupLists() {

        categoriesAdapter = new PosCategoriesAdapter(category -> {
            selectedCategoryId = category.id;
            categoriesAdapter.setSelectedId(selectedCategoryId);
            loadItems(selectedCategoryId, currentSearch);
        });

        binding.rvCategories.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        binding.rvCategories.setAdapter(categoriesAdapter);

        itemsAdapter = new PosItemsAdapter(new PosItemsAdapter.Listener() {

            @Override
            public void onSelectItem(int itemId) {
                cacheItemIfNeeded(itemId);
                if (!qtyMap.containsKey(itemId) || qtyMap.get(itemId) == null || qtyMap.get(itemId) <= 0) {
                    qtyMap.put(itemId, 1);
                }
            }


            @Override
            public void onUnselectItem(int itemId) {
                qtyMap.remove(itemId);
                selectedItemsCache.remove(itemId); // اختياري
            }


            @Override
            public void onPlusClicked(int itemId) {
                cacheItemIfNeeded(itemId);
                increase(itemId);
            }

            @Override
            public void onMinusClicked(int itemId) {
                cacheItemIfNeeded(itemId);
                decrease(itemId);

                // لو الكمية صارت 0 احذف من الكاش (اختياري)
                Integer q = qtyMap.get(itemId);
                if (q == null || q <= 0) selectedItemsCache.remove(itemId);
            }

            @Override
            public void onQtyChanged(int itemId, int qty) {
                cacheItemIfNeeded(itemId);
                qtyMap.put(itemId, qty);
            }

            @Override
            public int getQty(int itemId) {
                Integer v = qtyMap.get(itemId);
                return v == null ? 0 : v;
            }
        });



        binding.rvItems.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvItems.setAdapter(itemsAdapter);

        GridLayoutManager lm = (GridLayoutManager) binding.rvItems.getLayoutManager();

        binding.rvItems.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull androidx.recyclerview.widget.RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);

                if (dy <= 0) return;
                if (itemsLoading || !itemsHasMore) return;

                androidx.recyclerview.widget.RecyclerView.LayoutManager mgr = rv.getLayoutManager();
                if (!(mgr instanceof GridLayoutManager)) return;

                GridLayoutManager glm = (GridLayoutManager) mgr;

                int lastVisible = glm.findLastVisibleItemPosition();
                int total = glm.getItemCount();

                // لما نكون قريبين من آخر عنصرين
                if (lastVisible >= total - 3) {
                    loadItemsNextPage();
                }
            }
        });

        binding.nsMain.setOnScrollChangeListener(new androidx.core.widget.NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(androidx.core.widget.NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY <= oldScrollY) return; // بس نزول
                if (itemsLoading || !itemsHasMore) return;

                View child = v.getChildAt(0);
                if (child == null) return;

                int diff = (child.getBottom() - (v.getHeight() + v.getScrollY()));
                if (diff <= 200) { // قريب من النهاية
                    loadItemsNextPage();
                }
            }
        });

    }

    private void refreshActiveInvoicesBadge() {
        try {
            int count = posDb.listInvoices().size();
            updateBadge(count);
        } catch (Exception e) {
            updateBadge(0);
        }
    }

    private void setupSearchAndActions() {

//        binding.search.addTextChangedListener(new android.text.TextWatcher() {
//            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override public void afterTextChanged(android.text.Editable s) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                currentSearch = safeTrim(s);
//
//                if (searchRunnable != null) handler.removeCallbacks(searchRunnable);
//                searchRunnable = () -> loadItems(selectedCategoryId, currentSearch);
//                handler.postDelayed(searchRunnable, 400);
//            }
//        });

        binding.search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                currentSearch = safeTrim(binding.search.getText());
                hideSoftKeyboard();
                loadItems(selectedCategoryId, currentSearch); // category + name معاً
                return true;
            }
            return false;
        });


        binding.selectCustomer.setOnClickListener(v -> openSelectCustomerDialogSmart());
        binding.addCustomer.setOnClickListener(v -> openAddCustomerDialog());

        binding.btnNext.setOnClickListener(v -> {

            if (qtyMap.isEmpty()) {
                toast("اختر صنف واحد على الأقل");
                return;
            }

            if (!isValidSelectedCustomer()) {
                toast("اختر الزبون أولاً");
                return;
            }

            long invoiceId = saveActiveInvoiceDraft(); // ✅ يحفظ/يحدث ويرجع ID

            if (invoiceId <= 0) {
                toast(getString(R.string.general_error));
                return;
            }

            Intent i = new Intent(this, PosCheckoutActivity.class);
            i.putExtra("pos_invoice_id", invoiceId); // ✅ فقط ID
            i.putExtra(PosCheckoutActivity.EXTRA_PAYMENT_TYPES_JSON, getGson().toJson(paymentTypes));
            startActivityForResult(i, 3001);
        });

    }
    private void openSelectCustomerDialogSmart() {

        boolean fromActiveInvoice = editingInvoiceId > 0;

        boolean hasCustomer = selectedCustomer != null
                && isValidSelectedCustomer()
                && selectedCustomer.name != null
                && !selectedCustomer.name.trim().isEmpty();

        if (fromActiveInvoice && hasCustomer) {
            fetchCustomersThenOpenDialog(selectedCustomer.name.trim(), selectedCustomer.id);
            return;
        }

        openSelectCustomerDialog();
    }


    private void fetchCustomersThenOpenDialog(@NonNull String name, int selectedId) {

        showProgressHUD();

        // عدّل النوع حسب الريسبونس الحقيقي عندك
        Type type = new TypeToken<BaseResponse<ArrayList<FuelCustomerDto>>>() {}.getType();

        Map<String, String> params = new HashMap<>();
        params.put("name", name); // إذا API عندك key مختلف (q مثلا) عدّله

        apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.CUSTOMERS_SELECT,
                params,
                null,
                type,
                0,
                new ApiCallback<ArrayList<FuelCustomerDto>>() {

                    @Override
                    public void onSuccess(ArrayList<FuelCustomerDto> data, String message, String rawJson) {
                        hideProgressHUD();

                        // ✅ حضّر الديلوج بالنتائج + search text + selected id
                        lastCustomerSearch = name;
                        lastCustomerResults = (data != null) ? data : new ArrayList<>();

                        SelectCustomerDialog d = SelectCustomerDialog.newInstance(
                                lastCustomerSearch,
                                lastCustomerResults,
                                selectedId
                        );

                        d.setCancelable(false);

                        d.setListener(customer -> {
                            applySelectedCustomer(customer);
                            lastCustomerSearch = d.getLastSearch();
                            lastCustomerResults = d.getLastResults();
                        });

                        d.show(getSupportFragmentManager(), "SelectCustomerDialog");
                    }

                    @Override
                    public void onError(co.highfive.petrolstation.network.ApiError error) {
                        hideProgressHUD();

                        // fallback: افتح الديلوج عادي بدل ما يطلع فاضي
                        openSelectCustomerDialog();
                    }

                    @Override public void onUnauthorized(String rawJson) { hideProgressHUD(); logout(); }
                    @Override public void onNetworkError(String reason) { hideProgressHUD(); toast(R.string.no_internet); }
                    @Override public void onParseError(String rawJson, Exception e) { hideProgressHUD(); openSelectCustomerDialog(); }
                }
        );
    }


    private void openAddCustomerDialog() {
        AddCustomerDialog d = AddCustomerDialog.newInstance(lastAddCustomerName, lastAddCustomerMobile);
        d.setCancelable(false);

        d.setListener((name, mobile) -> {
            // خزّن آخر مدخلات (لو سكر وفتح يرجع نفسهم)
            lastAddCustomerName = name;
            lastAddCustomerMobile = mobile;

            submitAddCustomer(name, mobile);
        });

        d.show(getSupportFragmentManager(), "AddCustomerDialog");
    }

    private void submitAddCustomer(String name, String mobile) {

        String n = name == null ? "" : name.trim();
        String m = mobile == null ? "" : mobile.trim();

        if (n.isEmpty()) {
            toast(getString(R.string.enter_name));
            return;
        }
        if (m.isEmpty()) {
            toast("أدخل رقم الجوال");
            return;
        }

        // ✅ لو Offline: خزّن في Room offline_customers بدل API
        if (!connectionAvailable) {

            String mobileNorm = normalizeMobile(m);

            showProgressHUD();

            dbExecutor.execute(() -> {

                try {
                    // 1) افحص customers الأساسيين (اللي من MainActivity)
                    CustomerEntity existsMain = null;
                    try {
                        existsMain = db.customerDao().getByMobileExact(m);
                        if (existsMain == null && mobileNorm != null && !mobileNorm.isEmpty()) {
                            existsMain = db.customerDao().getByMobileNormalizedLoose(mobileNorm);
                        }
                    } catch (Exception ignored) {}

                    // 2) افحص داخل offline_customers نفسه (منع تكرار أوفلاين)
                    co.highfive.petrolstation.data.local.entities.OfflineCustomerEntity existsOffline = null;
                    try {
                        existsOffline = db.offlineCustomerDao().getByMobileNormalized(mobileNorm);
                    } catch (Exception ignored) {}

                    final boolean hasDuplicateMain = (existsMain != null);
                    final boolean hasDuplicateOffline = (existsOffline != null);

                    mainHandler.post(() -> {
                        hideProgressHUD();

                        // إذا موجود بالفعل في offline_customers → امنع (أفضل)
                        if (hasDuplicateOffline) {
                            toast("هذا الرقم تم إضافته مسبقًا (أوفلاين).");
                            return;
                        }

                        if (hasDuplicateMain) {
                            showConfirmDuplicateMobileDialog(m, () -> insertOfflineCustomerAndSelect(n, m));
                        } else {
                            insertOfflineCustomerAndSelect(n, m);
                        }
                    });

                } catch (Exception e) {
                    mainHandler.post(() -> {
                        hideProgressHUD();
                        toast(getString(R.string.general_error));
                    });
                }
            });

            return;
        }

        // ✅ Online: نفس كودك الحالي
        showProgressHUD();

        String endpoint = Endpoints.CUSTOMERS_ADD;

        java.util.Map<String, String> params = ApiClient.mapOf(
                "name", n,
                "mobile", m
        );

        Type type = new TypeToken<BaseResponse<FuelCustomerDto>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_POST,
                endpoint,
                params,
                null,
                type,
                0,
                new ApiCallback<FuelCustomerDto>() {

                    @Override
                    public void onSuccess(FuelCustomerDto data, String message, String rawJson) {
                        hideProgressHUD();

                        toast(message != null && !message.trim().isEmpty()
                                ? message
                                : getString(R.string.done));

                        if (data == null || data.id <= 0) {
                            toast(getString(R.string.general_error));
                            return;
                        }

                        applySelectedCustomer(data);

                        lastCustomerSearch = data.name != null ? data.name : "";
                        lastCustomerResults = new ArrayList<>();
                        lastCustomerResults.add(data);
                    }

                    @Override
                    public void onError(co.highfive.petrolstation.network.ApiError error) {
                        hideProgressHUD();
                        toast(error != null ? error.message : getString(R.string.general_error));
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
                        toast(getString(R.string.general_error));
                    }
                }
        );
    }

    private void insertOfflineCustomerAndSelect(String name, String mobile) {

        showProgressHUD();

        dbExecutor.execute(() -> {
            try {
                long now = System.currentTimeMillis();

                co.highfive.petrolstation.data.local.entities.OfflineCustomerEntity e =
                        new co.highfive.petrolstation.data.local.entities.OfflineCustomerEntity();

                e.name = name;
                e.mobile = mobile;
                e.mobileNormalized = normalizeMobile(mobile);
                e.syncStatus = 0; // pending
                e.syncError = null;
                e.createdAtTs = now;
                e.updatedAtTs = now;

                // payload للمستقبل (sync)
                // بنخزن object بسيط: name/mobile/local_id لاحقاً بعد insert
                e.requestJson = null;

                long localId = db.offlineCustomerDao().insert(e);

                // بعد insert نحدّث requestJson لأنه صار عندنا localId
                e.localId = localId;
                e.requestJson = buildOfflineCustomerRequestJson(e);
                db.offlineCustomerDao().update(e);

                FuelCustomerDto dto = new FuelCustomerDto();
                int fakeId = (localId > Integer.MAX_VALUE) ? -1 : (int) (-localId);
                dto.id = fakeId;
                dto.name = name;
                dto.mobile = mobile;

                // ✅ نضيف flags (لازم نضيفها للـ DTO إذا مش موجودة)
                dto.is_offline = true;
                dto.local_id = localId;

                mainHandler.post(() -> {
                    hideProgressHUD();

                    toast("تم حفظ الزبون (أوفلاين)");

                    applySelectedCustomer(dto);

                    lastCustomerSearch = name;
                    lastCustomerResults = new ArrayList<>();
                    lastCustomerResults.add(dto);
                });

            } catch (Exception ex) {
                mainHandler.post(() -> {
                    hideProgressHUD();
                    toast(getString(R.string.general_error));
                });
            }
        });
    }
    private boolean isValidSelectedCustomer() {
        if (selectedCustomer == null) return false;
        if (selectedCustomer.id > 0) return true; // online
        return selectedCustomer.is_offline && selectedCustomer.local_id > 0 && selectedCustomer.id < 0;
    }

    private int getCustomerKeyForPosDb(FuelCustomerDto c) {
        // هذا هو الرقم اللي نخزنه في posDb
        if (c == null) return 0;
        return c.id; // online موجب / offline سالب
    }



    private String buildOfflineCustomerRequestJson(co.highfive.petrolstation.data.local.entities.OfflineCustomerEntity e) {
        try {
            java.util.Map<String, Object> customer = new java.util.HashMap<>();
            customer.put("local_id", e.localId);
            customer.put("name", e.name);
            customer.put("mobile", e.mobile);
            // اختياري:
            if (e.address != null) customer.put("address", e.address);
            if (e.notes != null) customer.put("notes", e.notes);

            java.util.Map<String, Object> root = new java.util.HashMap<>();
            root.put("customer", customer);

            return getGson().toJson(root);
        } catch (Exception ignored) {
            return null;
        }
    }


    private void loadPosSettings() {

        // ✅ إذا ما في نت: حاول من الكاش بالسيشن
        if (!connectionAvailable) {
            String cached = getSessionManager().getString(getSessionKeys().pos_settings_json);
            if (cached != null && !cached.trim().isEmpty()) {
                try {
                    java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<
                            co.highfive.petrolstation.network.BaseResponse<co.highfive.petrolstation.pos.dto.PosSettingsData>
                            >() {}.getType();

                    co.highfive.petrolstation.network.BaseResponse<co.highfive.petrolstation.pos.dto.PosSettingsData> resp =
                            getGson().fromJson(cached, type);

                    co.highfive.petrolstation.pos.dto.PosSettingsData data = resp != null ? resp.data : null;

                    buildCategories(data == null ? null : data.category);
                    categoriesAdapter.submitList(categories);

                    paymentTypes.clear();
                    if (data != null && data.payment_type != null) paymentTypes.addAll(data.payment_type);

                    selectedCategoryId = 0;
                    categoriesAdapter.setSelectedId(0);

                    // ✅ أوفلاين: حمّل من الروم
                    loadItems(0, "");
                    return;

                } catch (Exception e) {
                    toast(getString(R.string.no_internet));
                    return;
                }
            }

            toast(getString(R.string.no_internet));
            return;
        }

        // ✅ Online: نفس كودك الحالي
        showProgressHUD();

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
                    public void onSuccess(PosSettingsData data, String message, String raw) {
                        hideProgressHUD();

                        // ✅ خزّن للإوفلاين
                        getSessionManager().setString(getSessionKeys().pos_settings_json, raw);

                        buildCategories(data == null ? null : data.category);
                        categoriesAdapter.submitList(categories);

                        paymentTypes.clear();
                        if (data != null && data.payment_type != null) paymentTypes.addAll(data.payment_type);

                        selectedCategoryId = 0;
                        categoriesAdapter.setSelectedId(0);

                        loadItems(0, "");
                    }

                    @Override public void onError(ApiError error) {
                        hideProgressHUD();
                        toast(error != null ? safe(error.message) : getString(R.string.general_error));
                    }

                    @Override public void onUnauthorized(String rawJson) { hideProgressHUD(); logout(); }
                    @Override public void onNetworkError(String output) { hideProgressHUD(); toast(getString(R.string.general_error)); }
                    @Override public void onParseError(String raw, Exception e) { hideProgressHUD(); toast(getString(R.string.general_error)); }
                }
        );
    }


    private void loadItems(int categoryId, String name) {
        // reset pagination state
        itemsPage = 1;
        itemsLastPage = 1;
        itemsHasMore = true;
        itemsLoading = false;

        lastReqCategory = categoryId;
        lastReqSearch = name != null ? name : "";

        // clear current list
        items.clear();
        if (itemsAdapter != null) itemsAdapter.submitList(new ArrayList<>(items));

        // load first page
        loadItemsPage(categoryId, name, 1);
    }

    private void loadItemsNextPage() {
        if (!itemsHasMore || itemsLoading) return;
        int next = itemsPage + 1;
        if (next > itemsLastPage) {
            itemsHasMore = false;
            return;
        }
        loadItemsPage(lastReqCategory, lastReqSearch, next);
    }

    private void loadItemsPage(int categoryId, String name, int page) {

        // ✅ Offline -> Room
        if (!connectionAvailable) {
            loadItemsPageFromRoom(categoryId, name, page);
            return;
        }

        // ✅ Online -> Server (نفس الحالي) + Cache to Room
        itemsLoading = true;
        showProgressHUD();

        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));

        if (categoryId > 0) params.put("category", String.valueOf(categoryId));
        if (name != null && !name.trim().isEmpty()) params.put("name", name.trim());

        Type type = new TypeToken<BaseResponse<PosItemsData>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                "/api/pos/items",
                params,
                null,
                type,
                0,
                new ApiCallback<PosItemsData>() {

                    @Override
                    public void onSuccess(PosItemsData data, String message, String raw) {
                        hideProgressHUD();
                        itemsLoading = false;

                        if (data != null && data.items != null) {
                            itemsPage = data.items.currentPage;
                            itemsLastPage = data.items.lastPage;
                            itemsHasMore = data.items.currentPage < data.items.lastPage;
                        } else {
                            itemsHasMore = false;
                        }

                        List<PosItemDto> newItems = (data != null && data.items != null) ? data.items.data : null;
                        if (newItems != null && !newItems.isEmpty()) {
                            items.addAll(newItems);
                        }

                        itemsAdapter.submitList(new ArrayList<>(items));

                        // ✅ Cache items to Room (حتى لو فلتر)
                        if (newItems != null && !newItems.isEmpty()) {
                            cacheServerItemsToRoom(newItems);
                        }
                    }

                    @Override
                    public void onError(ApiError error) {
                        hideProgressHUD();
                        itemsLoading = false;
                        toast(error != null ? safe(error.message) : getString(R.string.general_error));
                    }

                    @Override public void onUnauthorized(String rawJson) { hideProgressHUD(); itemsLoading = false; logout(); }
                    @Override public void onNetworkError(String output) { hideProgressHUD(); itemsLoading = false; toast(getString(R.string.general_error)); }
                    @Override public void onParseError(String raw, Exception e) { hideProgressHUD(); itemsLoading = false; toast(getString(R.string.general_error)); }
                }
        );
    }

    private void loadItemsPageFromRoom(int categoryId, String name, int page) {

        showProgressHUD();
        itemsLoading = true;

        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * OFFLINE_PAGE_SIZE;

        String q = (name == null) ? "" : name.trim();

        dbExecutor.execute(() -> {
            List<co.highfive.petrolstation.data.local.entities.ItemsCacheEntity> rows =
                    db.itemsCacheDao().getPagedFiltered(categoryId, q, OFFLINE_PAGE_SIZE, offset);

            ArrayList<PosItemDto> mapped = new ArrayList<>();

            if (rows != null) {
                for (co.highfive.petrolstation.data.local.entities.ItemsCacheEntity e : rows) {
                    PosItemDto d = new PosItemDto();
                    d.id = e.id;
                    d.name = e.name;
                    d.negative_check = e.negativeCheck;
                    d.price = e.price;
                    d.barcode = e.barcode;
                    d.icon = e.icon;

                    // ✅ مهم: إذا PosItemDto عندك اسم الحقل مختلف عدّله
                    d.category = e.category;

                    mapped.add(d);
                }
            }

            mainHandler.post(() -> {
                itemsLoading = false;


                // pagination offline: إذا رجع 20 عنصر يعني غالباً في صفحة بعدها
                itemsPage = safePage;
                itemsHasMore = mapped.size() == OFFLINE_PAGE_SIZE;
                itemsLastPage = itemsHasMore ? (safePage + 1) : safePage;

                items.addAll(mapped);
                itemsAdapter.submitList(new ArrayList<>(items));
                hideProgressHUD();
            });
        });
    }

    private void cacheServerItemsToRoom(List<PosItemDto> newItems) {
        dbExecutor.execute(() -> {
            try {
                ArrayList<co.highfive.petrolstation.data.local.entities.ItemsCacheEntity> out = new ArrayList<>();

                for (PosItemDto it : newItems) {
                    co.highfive.petrolstation.data.local.entities.ItemsCacheEntity e =
                            new co.highfive.petrolstation.data.local.entities.ItemsCacheEntity();

                    e.id = it.id;
                    e.name = it.name != null ? it.name : "";
                    e.negativeCheck = it.negative_check;
                    e.price = it.price;
                    e.barcode = it.barcode;
                    e.icon = it.icon;

                    // ✅ مهم: نفس اسم الحقل في PosItemDto
                    e.category = it.category;

                    out.add(e);
                }

                if (!out.isEmpty()) db.itemsCacheDao().upsertAll(out);

            } catch (Exception ignore) {}
        });
    }



    private void buildCategories(List<LookupDto> apiCategories) {
        categories.clear();

        LookupDto all = new LookupDto();
        all.id = 0;
        all.name = getString(R.string.all); // تأكد عندك string all
        categories.add(all);

        if (apiCategories != null) categories.addAll(apiCategories);
    }

    private void increase(int itemId) {
        int q = qtyMap.containsKey(itemId) ? qtyMap.get(itemId) : 0;
        q++;
        qtyMap.put(itemId, q);
        itemsAdapter.notifyQtyChanged(itemId);
    }

    private void decrease(int itemId) {
        int q = qtyMap.containsKey(itemId) ? qtyMap.get(itemId) : 0;
        q--;
        if (q <= 0) qtyMap.remove(itemId);
        else qtyMap.put(itemId, q);
        itemsAdapter.notifyQtyChanged(itemId);
    }

    private long saveActiveInvoiceDraft() {

        if (!isValidSelectedCustomer()) return -1;
        if (qtyMap == null || qtyMap.isEmpty()) return -1;

        int accountId = selectedCustomer != null ? selectedCustomer.account_id : 0;


        try {
            String itemsDetailsJson = buildInvoiceItemsJson();

            long resultId;
            int customerKey = getCustomerKeyForPosDb(selectedCustomer);
            String mobile = (selectedCustomer.mobile != null) ? selectedCustomer.mobile : "";

            if (mobile.trim().isEmpty() && editingInvoiceId > 0) {
                PosActiveInvoice inv = posDb.getInvoice(editingInvoiceId);
                if (inv != null && inv.customerMobile != null) mobile = inv.customerMobile;
            }

            if (editingInvoiceId > 0) {


                // ✅ تعديل على نفس الفاتورة
                boolean updated = posDb.updateInvoice(editingInvoiceId, customerKey, selectedCustomer.name, mobile, accountId, itemsDetailsJson);


                resultId = updated ? editingInvoiceId : -1;
            } else {
                // ✅ إنشاء فاتورة جديدة
                resultId = posDb.insertInvoice(customerKey, selectedCustomer.name, mobile, accountId, itemsDetailsJson);


                // ✅ لو عملنا Insert، خزّن id كـ editing (اختياري)
                if (resultId > 0) editingInvoiceId = resultId;
            }

            // ✅ البادج يعكس العدد الحقيقي من DB
            updateBadge(posDb.listInvoices().size());

            return resultId;

        } catch (Exception e) {
            return -1;
        }
    }


    private int getActiveDraftsCount() {
        // كبداية: خليه 1 اذا في مسودة محفوظة
        try {
            String json = getSessionManager().getString("POS_ACTIVE_QTY_MAP");
            return (json != null && !json.trim().isEmpty()) ? 1 : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private void updateBadge(int count) {
        if (count > 0) {
            binding.badgeCount.setText(String.valueOf(count));
            binding.badgeCount.setVisibility(View.VISIBLE);
        } else {
            binding.badgeCount.setVisibility(View.GONE);
        }
    }
    private void openSelectCustomerDialog() {
        SelectCustomerDialog d = SelectCustomerDialog.newInstance(
                lastCustomerSearch,
                lastCustomerResults,
                selectedCustomer != null ? selectedCustomer.id : 0
        );

        d.setCancelable(false);

        d.setListener(customer -> {

            // ✅ هذا أهم سطر: يعيد ضبط المركبات + يحدّث UI
            applySelectedCustomer(customer);

            // cache dialog state
            lastCustomerSearch = d.getLastSearch();
            lastCustomerResults = d.getLastResults();
        });

        d.show(getSupportFragmentManager(), "SelectCustomerDialog");
    }


    private void applySelectedCustomer(FuelCustomerDto customer) {

        if (customer == null) return;

        // ✅ اسمح للأوفلاين
        if (customer.id == 0 && !(customer.is_offline && customer.local_id > 0)) return;

        selectedCustomer = customer;
        binding.typeCustomer.setText(customer.name != null ? customer.name : "");

        if (editingInvoiceId > 0) {
            try {
                int customerKey = getCustomerKeyForPosDb(customer);


                String mobile = selectedCustomer.mobile != null ? selectedCustomer.mobile : "";

                posDb.updateInvoice(editingInvoiceId, customerKey, selectedCustomer.name, mobile, customer.account_id, buildInvoiceItemsJson());

            } catch (Exception ignore) {}
        }
    }

    private String normalizeMobile(String mobile) {
        if (mobile == null) return "";
        String m = mobile.trim();
        m = m.replace(" ", "").replace("-", "").replace("+", "");
        // لو بدك تشيل 00
        if (m.startsWith("00")) m = m.substring(2);
        return m;
    }

    private void showConfirmDuplicateMobileDialog(
            String mobile,
            Runnable onProceed
    ) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("تنبيه")
                .setMessage("رقم الجوال موجود مسبقًا (" + mobile + "). هل أنت متأكد أنك تريد إنشاء زبون جديد بنفس الرقم؟")
                .setPositiveButton("نعم، أضف", (d, w) -> {
                    d.dismiss();
                    if (onProceed != null) onProceed.run();
                })
                .setNegativeButton("إلغاء", (d, w) -> d.dismiss())
                .show();
    }


}
