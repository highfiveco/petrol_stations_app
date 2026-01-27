package co.highfive.petrolstation.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.adapters.FuelItemsAdapter;
import co.highfive.petrolstation.adapters.PumpsAdapter;
import co.highfive.petrolstation.customers.dto.CustomerVehicleDto;
import co.highfive.petrolstation.customers.dto.VehicleSettingsResponseDto;
import co.highfive.petrolstation.customers_settings.dto.LookupDto;
import co.highfive.petrolstation.data.local.AppDatabase;
import co.highfive.petrolstation.data.local.DatabaseProvider;
import co.highfive.petrolstation.data.local.entities.CustomerEntity;
import co.highfive.petrolstation.data.local.entities.CustomerVehicleEntity;
import co.highfive.petrolstation.data.local.entities.OfflineCustomerEntity;
import co.highfive.petrolstation.data.local.repo.InvoiceLocalRepository;
import co.highfive.petrolstation.databinding.ActivityFuelSaleBinding;
import co.highfive.petrolstation.fragments.AddCustomerDialog;
import co.highfive.petrolstation.fragments.AddVehicleDialog;
import co.highfive.petrolstation.fragments.SelectCustomerDialog;
import co.highfive.petrolstation.fragments.SelectVehicleDialog;
import co.highfive.petrolstation.fuelsale.dto.FuelCampaignDto;
import co.highfive.petrolstation.fuelsale.dto.FuelCampaignItemDto;
import co.highfive.petrolstation.fuelsale.dto.FuelCustomerDto;
import co.highfive.petrolstation.fuelsale.dto.FuelItemDto;
import co.highfive.petrolstation.fuelsale.dto.FuelPriceAddRequest;
import co.highfive.petrolstation.fuelsale.dto.FuelPriceSettingsData;
import co.highfive.petrolstation.fuelsale.dto.PumpDto;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.models.Setting;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;
import co.highfive.petrolstation.vehicles.dto.CustomerVehiclesSettingsData;

public class FuelSaleActivity extends BaseActivity {

    private ActivityFuelSaleBinding binding;

    private FuelPriceSettingsData settingsData;

    private final List<FuelItemDto> items = new ArrayList<>();
    private final List<PumpDto> pumps = new ArrayList<>();
    private final List<FuelCampaignDto> campaigns = new ArrayList<>();
    private final List<LookupDto> paymentTypes = new ArrayList<>();

    private FuelItemsAdapter itemsAdapter;
    private PumpsAdapter pumpsAdapter;

    private FuelItemDto selectedItem;
    private PumpDto selectedPump;

    private LinearLayout paymentContainer; // binding.paymentContainer

    private String lastCustomerSearch = "";
    private ArrayList<FuelCustomerDto> lastCustomerResults = new ArrayList<>();
    private FuelCustomerDto selectedCustomer = null;

    private String lastAddCustomerName = "";
    private String lastAddCustomerMobile = "";

    private ArrayList<CustomerVehicleDto> lastVehicleResults = new ArrayList<>();
    private CustomerVehicleDto selectedVehicle = null;

    private int lastVehiclesCustomerId = 0;

    private co.highfive.petrolstation.customers.dto.VehicleSettingsResponseDto cachedVehicleSettings = null;
    private AddVehicleDialog activeVehicleDialog = null;

    private ArrayList<FuelCampaignDto> filteredCampaigns = new ArrayList<>();
    private FuelCampaignDto selectedCampaign = null;
    private static final String SESSION_KEY_FUEL_SALE_DRAFTS = "SESSION_KEY_FUEL_SALE_DRAFTS";
    private Integer pendingApplyDraftId = null;

    public Integer customerId; // NEW
    private Integer activeDraftLocalId = null; // NEW

    private boolean isUpdatingCalc = false;

    private enum LastEditSource { NONE, QTY, TOTAL }
    private LastEditSource lastEditSource = LastEditSource.NONE;
    private double selectedUnitPrice = 0;
    private android.app.AlertDialog successDialog;

    private AppDatabase db;

    private ExecutorService dbExecutor;
    private Handler mainHandler;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFuelSaleBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        db = DatabaseProvider.get(this);
        dbExecutor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        paymentContainer = binding.paymentContainer;

        setupUI(binding.getRoot());
        initHeader();
        initRecyclerViews();
        initActions();

        loadFuelSaleSettingsSmart();
        attachCalcWatchers();
        attachSelectAllOnFocus();
    }

    private void loadFuelSaleSettingsSmart() {
        if (connectionAvailable) {
            loadFuelSaleSettings(); // online
            return;
        }

        // offline -> load from session
        FuelPriceSettingsData cached = getFuelSettingsFromSession();
        if (cached == null) {
            toast(getString(R.string.no_internet));
            // خيار: Disable UI
            return;
        }

        applyFuelSettingsToUI(cached);
    }

    private void applyFuelSettingsToUI(@NonNull FuelPriceSettingsData data) {

        settingsData = data;

        items.clear();
        pumps.clear();
        campaigns.clear();
        paymentTypes.clear();

        if (data.items != null) items.addAll(data.items);
        if (data.pumps != null) pumps.addAll(data.pumps);
        if (data.campaigns != null) campaigns.addAll(data.campaigns);
        if (data.payment_type != null) paymentTypes.addAll(data.payment_type);

        itemsAdapter.updateData(items);
        pumpsAdapter.updateData(pumps);

        hideCampaign();

        paymentContainer.removeAllViews();
        addPaymentRow();

        if (pendingApplyDraftId != null) {
            int id = pendingApplyDraftId;
            pendingApplyDraftId = null;
            applyDraftById(id);
        }
    }



    private int getDraftCount() {
        ArrayList<co.highfive.petrolstation.fuelsale.dto.FuelSaleDraft> list = readDraftsFromSession();
        return list != null ? list.size() : 0;
    }

    private void attachSelectAllOnFocus() {

        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (!hasFocus) return;

            if (v instanceof EditText) {
                EditText et = (EditText) v;
                if (et.getText() != null && et.getText().length() > 0) {
                    et.post(et::selectAll); // مهم post عشان ينجح بعد فتح الكيبورد
                }
            }
        };

        View.OnClickListener clickListener = v -> {
            if (v instanceof EditText) {
                EditText et = (EditText) v;
                if (et.getText() != null && et.getText().length() > 0) {
                    et.post(et::selectAll);
                }
            }
        };

        // Qty
        binding.etQuantity.setOnFocusChangeListener(focusListener);
        binding.etQuantity.setOnClickListener(clickListener);

        // Total (Amount)
        binding.etAmount.setOnFocusChangeListener(focusListener);
        binding.etAmount.setOnClickListener(clickListener);
    }


    private void attachCalcWatchers() {

        // Qty watcher -> update total
        binding.etQuantity.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (isUpdatingCalc) return;
                lastEditSource = LastEditSource.QTY;
                recalcTotalFromQty();
            }
        });

        // Total watcher (etAmount) -> update qty
        binding.etAmount.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (isUpdatingCalc) return;
                lastEditSource = LastEditSource.TOTAL;
                recalcQtyFromTotal();
            }
        });
    }

    private void recalcTotalFromQty() {
        if (selectedUnitPrice <= 0) return;

        double qty = parseDoubleSafe(binding.etQuantity.getText());
        if (qty < 0) qty = 0;

        double total = selectedUnitPrice * qty;

        isUpdatingCalc = true;
        setEditTextDouble(binding.etAmount, total);
        isUpdatingCalc = false;
    }

    private void recalcQtyFromTotal() {
        if (selectedUnitPrice <= 0) return;

        double total = parseDoubleSafe(binding.etAmount.getText());
        if (total < 0) total = 0;

        double qty = total / selectedUnitPrice;

        isUpdatingCalc = true;
        setEditTextDouble(binding.etQuantity, qty);
        isUpdatingCalc = false;
    }


    private void setEditTextDouble(EditText et, double v) {
        if (et == null) return;

        // صيغة نظيفة: إذا رقم صحيح لا تعرض .0
        String text;
        if (v == (long) v) text = String.valueOf((long) v);
        else text = String.valueOf(round2(v)); // خيار: خليها 2 decimal

        // لتقليل إعادة إطلاق watcher بسبب نفس النص
        String current = et.getText() != null ? et.getText().toString() : "";
        if (!current.equals(text)) {
            et.setText(text);
            et.setSelection(text.length());
        }
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }


    private void updateDraftBadge() {
        int count = getDraftCount();

        // لو بدك تخفي البادج لما يكون 0:
        if (count <= 0) {
            binding.badgeCount.setVisibility(View.GONE);
        } else {
            binding.badgeCount.setVisibility(View.VISIBLE);
            binding.badgeCount.setText(String.valueOf(count));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDraftBadge();
    }

    /* ================= HEADER ================= */

    private void initHeader() {
        binding.icBack.setOnClickListener(v -> finish());
        binding.icHome.setOnClickListener(v ->
                moveToActivity(this, MainActivity.class, null, true)
        );
    }

    /* ================= INIT ================= */

    private void initRecyclerViews() {

        // Fuel Items
        binding.rvItems.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        binding.rvItems.setNestedScrollingEnabled(false);

        itemsAdapter = new FuelItemsAdapter(items, item -> {
            selectedItem = item;

            // خزّن سعر الوحدة
            selectedUnitPrice = item != null && item.price != null ? item.price : 0;

            if (selectedUnitPrice <= 0) {
                toast("هذا الصنف بدون سعر");
                return;
            }

            isUpdatingCalc = true;

            // default qty = 1 إذا فاضي أو 0
            double qty = parseDoubleSafe(binding.etQuantity.getText());
            if (qty <= 0) {
                binding.etQuantity.setText("1");
                qty = 1;
            }

            // الإجمالي = سعر الوحدة * الكمية
            double total = selectedUnitPrice * qty;
            setEditTextDouble(binding.etAmount, total);

            isUpdatingCalc = false;

            filterCampaignsByItem(item.id);
        });


        binding.rvItems.setAdapter(itemsAdapter);

        // Pumps
        binding.rvPumps.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        binding.rvPumps.setNestedScrollingEnabled(false);

        pumpsAdapter = new PumpsAdapter(pumps, pump -> selectedPump = pump);
        binding.rvPumps.setAdapter(pumpsAdapter);
    }

    private void initActions() {

        binding.btnSave.setOnClickListener(v -> submitFuelSale());

        binding.btnAddPayment.setOnClickListener(v -> addPaymentRow());
        binding.selectCustomer.setOnClickListener(v -> openSelectCustomerDialog());
        binding.addCustomer.setOnClickListener(v -> openAddCustomerDialog());
        binding.selectVehicle.setOnClickListener(v -> openSelectVehicleDialog());
        binding.addVehicle.setOnClickListener(v -> openAddVehicleDialog());
        binding.campaignContainer.setOnClickListener(v -> openSelectCampaignDialog());
        binding.icAddNewInvoice.setOnClickListener(v -> {
            if (!isValidSelectedCustomerForFuelSale()) {
                toast("اختار الزبون أولاً");
                return;
            }
            openSaveDraftDialog();
        });
        binding.icCart.setOnClickListener(v -> {
            android.content.Intent i = new android.content.Intent(this, ActiveInvoicesActivity.class);
            activeInvoicesLauncher.launch(i);
        });
        binding.btnCancel.setOnClickListener(v -> {
            activeDraftLocalId = null;
            finish();
        });


    }


    private void submitFuelSale() {

        // ===== Validations =====
        if (selectedItem == null || selectedItem.id <= 0) {
            toast("اختار صنف أولاً");
            return;
        }

        if (selectedPump == null || selectedPump.id <= 0) {
            toast("اختار مضخة أولاً");
            return;
        }

        double price = parseDoubleSafe(binding.etAmount.getText());
        double qty   = parseDoubleSafe(binding.etQuantity.getText());

        if (price <= 0) { toast("أدخل السعر"); return; }
        if (qty <= 0)   { toast("أدخل الكمية"); return; }

        List<co.highfive.petrolstation.fuelsale.dto.PaymentMethodDto> pms = collectPaymentMethodsFromUI();
//        if (pms == null || pms.isEmpty()) {
//            toast("أضف طريقة دفع واحدة على الأقل");
//            return;
//        }

        // ===== Build request from UI =====
        FuelPriceAddRequest req = buildCurrentRequestFromUI();
        req.paymentMethods = pms;

        // ✅ هنا مكانه
        if (!connectionAvailable) {
            saveOfflineActiveInvoice(req);
            return;
        }

        ApiClient.ApiParams params = buildSaveFuelSaleParams(req);

        showProgressHUD();

        // ✅ غيّر Object إلى DTO الحقيقي إذا موجود عندك
        Type type = new TypeToken<BaseResponse<co.highfive.petrolstation.customers.dto.InvoiceDto>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_POST,
                Endpoints.FUEL_PRICE_ADD, // ✅ حط endpoint الحفظ الصحيح
                params,
                null,
                type,
                0,
                new ApiCallback<co.highfive.petrolstation.customers.dto.InvoiceDto>() {

                    @Override
                    public void onSuccess(co.highfive.petrolstation.customers.dto.InvoiceDto data, String msg, String rawJson) {
                        hideProgressHUD();

                        showSuccessDialog(msg);

                        Setting setting = null;

                        if (getAppData() != null) {
                            setting = getAppData().getSetting(); // عدّل حسب مشروعك
                        }

                        if (setting == null) {
                            toast(getString(R.string.general_error));
                            return;
                        }
                        printInvoice(setting,data);

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

    private void saveOfflineActiveInvoice(FuelPriceAddRequest req) {

        if (!isValidSelectedCustomerForFuelSale()) {
            toast("اختار الزبون أولاً");
            return;
        }

        String statement = "فاتورة محروقات";
        double total = parseDoubleSafe(binding.etAmount.getText());
        String invoiceNoPlaceholder = "OFF-" + System.currentTimeMillis();

        InvoiceLocalRepository repo = new InvoiceLocalRepository(
                this, db, getGson(), apiClient
        );

        FuelCustomerDto customer = selectedCustomer;
        CustomerVehicleDto vehicle = selectedVehicle;

        dbExecutor.execute(() -> {
            repo.saveFuelSaleOffline(req, customer, vehicle, statement, total, invoiceNoPlaceholder);

            mainHandler.post(() -> {
                toast("تم حفظ الفاتورة محليًا وسيتم إرسالها عند توفر الإنترنت");
                resetForNewInvoice();
            });
        });
    }



    @NonNull
    private ApiClient.ApiParams buildSaveFuelSaleParams(@NonNull FuelPriceAddRequest r) {

        ApiClient.ApiParams p = new ApiClient.ApiParams();

        // ===== Arrays: item_id[] / price[] / count[] =====
        if (r.itemIds != null) for (Integer id : r.itemIds) p.add("item_id[]", id != null ? id : 0);
        if (r.prices  != null) for (Double  pr : r.prices)  p.add("price[]",   pr != null ? pr : 0);
        if (r.counts  != null) for (Double  c  : r.counts)  p.add("count[]",   c  != null ? c  : 0);

        // ===== REQUIRED: customer_id =====
//        if (r.customerId != null) p.add("customer_id", r.customerId);

        // (Optional) account_id إذا عندكم مستخدم
        if (r.accountId != null) p.add("account_id", r.accountId);

        if (r.customerVehicleId != null && r.customerVehicleId > 0) {
            p.add("customer_vehicle_id", r.customerVehicleId);
        }
        if (r.pumpId != null)            p.add("pump_id", r.pumpId);
        if (r.campaignId != null)        p.add("campaign_id", r.campaignId);

        if (r.notes != null)             p.add("notes", r.notes);

        // ===== payment_methods as JSON string =====
        if (r.paymentMethods != null) {
            String json = getGson().toJson(r.paymentMethods);
            p.add("payment_methods", json);
        } else {
            p.add("payment_methods", "[]");
        }

        return p;
    }



    private void showSuccessDialogAndGoHome(String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("نجاح")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("تم", (d, w) -> {
                    d.dismiss();
                    moveToActivity(FuelSaleActivity.this, MainActivity.class, null, true);
                    // أو finish() لو بدك يرجع ع الرئيسية حسب الـ back stack عندك
                })
                .show();
    }


    private void openSaveDraftDialog() {
        co.highfive.petrolstation.fragments.ConfirmSaveFuelInvoiceDialog d =
                co.highfive.petrolstation.fragments.ConfirmSaveFuelInvoiceDialog.newInstance();

        d.setCancelable(false);

        d.setListener(new co.highfive.petrolstation.fragments.ConfirmSaveFuelInvoiceDialog.Listener() {
            @Override
            public void onSaveAndNew() {
                saveCurrentInvoiceAsDraft();
                resetForNewInvoice();
            }

            @Override
            public void onCancelCurrentAndNew() {
                resetForNewInvoice();
            }
        });

        d.show(getSupportFragmentManager(), "ConfirmSaveFuelInvoiceDialog");
    }

    @NonNull
    private FuelPriceAddRequest buildCurrentRequestFromUI() {

        FuelPriceAddRequest r = new FuelPriceAddRequest();

        r.accountId = selectedCustomer != null ? selectedCustomer.account_id : null;

        // item_id[] , price[] , count[]
        r.itemIds = new ArrayList<>();
        r.prices  = new ArrayList<>();
        r.counts  = new ArrayList<>();
        r.customerId = (selectedCustomer != null  ) ? selectedCustomer.id : null;

        if (selectedItem != null && selectedItem.id > 0) {
            r.itemIds.add(selectedItem.id);
        } else {
            r.itemIds.add(0);
        }

        double total = parseDoubleSafe(binding.etAmount.getText());
        double qty = parseDoubleSafe(binding.etQuantity.getText());
        if (qty <= 0) qty = 1;

        double unitPrice = (selectedUnitPrice > 0) ? selectedUnitPrice : (total / qty);

        r.prices.add(unitPrice);
        r.counts.add(qty);

        // عدّل حسب حقل الكمية عندك
//        double count = 1;
//        try { count = parseDoubleSafe(binding.etQuantity.getText()); } catch (Exception ignored) {}
//        r.counts.add(count);

        // account_id
        // إذا عندك selectedCustomer.account_id استخدمه
//        r.accountId = selectedCustomer != null ? selectedCustomer.account_id : null;

        // customer_vehicle_id
        r.customerVehicleId = (selectedVehicle != null && selectedVehicle.id > 0) ? selectedVehicle.id : null;

        // pump_id
        r.pumpId = selectedPump != null ? selectedPump.id : null;

        // campaign_id
        r.campaignId = selectedCampaign != null ? selectedCampaign.id : null;

        // payment_methods
        r.paymentMethods = collectPaymentMethodsFromUI();

        // notes
        try { r.notes = safeTrim(binding.etNotes.getText()); }
        catch (Exception e) { r.notes = ""; }

        return r;
    }

    private List<co.highfive.petrolstation.fuelsale.dto.PaymentMethodDto> collectPaymentMethodsFromUI() {

        ArrayList<co.highfive.petrolstation.fuelsale.dto.PaymentMethodDto> out = new ArrayList<>();

        if (paymentContainer == null) return out;

        for (int i = 0; i < paymentContainer.getChildCount(); i++) {
            View row = paymentContainer.getChildAt(i);
            if (row == null) continue;

            Spinner sp = row.findViewById(R.id.sp_payment_type);
            EditText et = row.findViewById(R.id.et_payment_amount);

            int pos = sp != null ? sp.getSelectedItemPosition() : -1;
            if (pos < 0 || pos >= paymentTypes.size()) continue;

            LookupDto type = paymentTypes.get(pos);
            if (type == null) continue;

            double amount = parseDoubleSafe(et != null ? et.getText() : null);
            if (amount <= 0) continue;

            out.add(new co.highfive.petrolstation.fuelsale.dto.PaymentMethodDto(type.id, amount));
        }

        return out;
    }

    private double parseDoubleSafe(CharSequence cs) {
        try {
            String s = cs == null ? "" : cs.toString().trim();
            if (s.isEmpty()) return 0;
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private double getSelectedPrice() {
        return parseDoubleSafe(binding.etAmount.getText());
    }

    private double getSelectedCount() {
        try {
            return parseDoubleSafe(binding.etQuantity.getText());
        } catch (Exception e) {
            return 1;
        }
    }

    private String buildTotalText(double total) {
        // غيّر العملة حسب مشروعك (شيكل/دينار/...)
        // إذا عندك currency من settingsData استخدمها هنا
        if (total == (long) total) {
            return ((long) total) + " شيكل";
        }
        return total + " شيكل";
    }


    private void saveCurrentInvoiceAsDraft() {

        if (!isValidSelectedCustomerForFuelSale()) {
            toast("اختار الزبون أولاً");
            return;
        }

        FuelPriceAddRequest req = buildCurrentRequestFromUI();

        co.highfive.petrolstation.fuelsale.dto.FuelSaleDraft draft =
                new co.highfive.petrolstation.fuelsale.dto.FuelSaleDraft();

        draft.saved_at = System.currentTimeMillis();
        draft.request = req;

        draft.customer_name = selectedCustomer != null && selectedCustomer.name != null
                ? selectedCustomer.name : "";
        draft.customer_id = (selectedCustomer != null && selectedCustomer.id > 0) ? selectedCustomer.id : 0;

        draft.vehicle_text = selectedVehicle != null ? buildVehicleDisplayText(selectedVehicle) : "";

        double price = getSelectedPrice();
        double count = getSelectedCount();
        double total = price * count;
        draft.total_text = buildTotalText(total);

        ArrayList<co.highfive.petrolstation.fuelsale.dto.FuelSaleDraft> list = readDraftsFromSession();

        // ✅ إذا داخل من فاتورة نشطة: عدّل نفس الفاتورة بدل إضافة جديدة
        if (hasActiveDraftSelected()) {

            int id = activeDraftLocalId != null ? activeDraftLocalId : 0;
            if (id <= 0) {
                toast(getString(R.string.general_error));
                return;
            }

            draft.local_id = id;

            boolean updated = false;
            for (int i = 0; i < list.size(); i++) {
                co.highfive.petrolstation.fuelsale.dto.FuelSaleDraft d = list.get(i);
                if (d != null && d.local_id == id) {
                    list.set(i, draft);
                    updated = true;
                    break;
                }
            }

            // احتياط: لو مش موجودة لأي سبب، أضفها
            if (!updated) {
                list.add(draft);
            }

            try {
                String json = getGson().toJson(list);
                getSessionManager().setString(SESSION_KEY_FUEL_SALE_DRAFTS, json);
                toast("تم تحديث الفاتورة المؤقتة #" + draft.local_id);
            } catch (Exception e) {
                toast(getString(R.string.general_error));
            }

            updateDraftBadge();
            return;
        }

        // ✅ غير هيك: إنشاء مسودة جديدة (New Draft)
        int nextId = 1;
        for (co.highfive.petrolstation.fuelsale.dto.FuelSaleDraft d : list) {
            if (d != null && d.local_id >= nextId) nextId = d.local_id + 1;
        }
        draft.local_id = nextId;

        list.add(draft);

        try {
            String json = getGson().toJson(list);
            getSessionManager().setString(SESSION_KEY_FUEL_SALE_DRAFTS, json);
            toast("تم حفظ الفاتورة كمسودة #" + draft.local_id);
        } catch (Exception e) {
            toast(getString(R.string.general_error));
        }

        updateDraftBadge();
    }


    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> activeInvoicesLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) return;

                int draftId = result.getData().getIntExtra(ActiveInvoicesActivity.EXTRA_SELECTED_DRAFT_ID, 0);
                if (draftId <= 0) return;

                // هنا لاحقاً سنعمل applyDraftById(draftId) لتعبئة الواجهة
                activeDraftLocalId = draftId; // ✅ NEW

                applyDraftById(draftId);

            });

    private void removeDraftById(int localId) {
        ArrayList<co.highfive.petrolstation.fuelsale.dto.FuelSaleDraft> list = readDraftsFromSession();
        if (list == null || list.isEmpty()) return;

        for (int i = 0; i < list.size(); i++) {
            co.highfive.petrolstation.fuelsale.dto.FuelSaleDraft d = list.get(i);
            if (d != null && d.local_id == localId) {
                list.remove(i);
                break;
            }
        }

        try {
            String json = getGson().toJson(list);
            getSessionManager().setString(SESSION_KEY_FUEL_SALE_DRAFTS, json);
        } catch (Exception ignored) {}

        updateDraftBadge();
    }

    private boolean hasActiveDraftSelected() {
        return activeDraftLocalId != null && activeDraftLocalId > 0;
    }


    @Nullable
    private co.highfive.petrolstation.fuelsale.dto.FuelSaleDraft findDraftById(int localId) {
        ArrayList<co.highfive.petrolstation.fuelsale.dto.FuelSaleDraft> list = readDraftsFromSession();
        if (list == null) return null;

        for (co.highfive.petrolstation.fuelsale.dto.FuelSaleDraft d : list) {
            if (d != null && d.local_id == localId) return d;
        }
        return null;
    }

    private void applyDraftById(int localId) {
        co.highfive.petrolstation.fuelsale.dto.FuelSaleDraft draft = findDraftById(localId);
        if (draft == null || draft.request == null) {
            toast(getString(R.string.general_error));
            return;
        }

        // Ensure settings loaded (items/pumps/campaigns/paymentTypes)
        if (settingsData == null || items.isEmpty() || pumps.isEmpty()) {
            // إذا الإعدادات لسه ما اجت، خزّن ID وطبّق بعد ما يخلص loadFuelSaleSettings
            pendingApplyDraftId = localId;
            loadFuelSaleSettingsSmart();
            return;
        }

        pendingApplyDraftId = null;

        FuelPriceAddRequest r = draft.request;

        // 1) Customer UI (we only have name/mobile text)
        // Restore minimal customer object so save request sends IDs
        selectedCustomer = new FuelCustomerDto();
        selectedCustomer.account_id = r.accountId; // ممكن تكون null
        selectedCustomer.name = draft.customer_name;
//        selectedCustomer.mobile = draft.customer_mobile;


        // 2) Vehicle UI (text)
        selectedVehicle = null;
        if (r.customerVehicleId != null && r.customerVehicleId > 0) {
            selectedVehicle = new CustomerVehicleDto();
            selectedVehicle.id = r.customerVehicleId;
            binding.vehicle.setText(draft.vehicle_text != null ? draft.vehicle_text : getString(R.string.select_vehicle));
        } else {
            binding.vehicle.setText(getString(R.string.select_vehicle));
        }

        // 3) Notes
        try { binding.etNotes.setText(r.notes != null ? r.notes : ""); } catch (Exception ignored) {}

        // 4) Amount + Quantity
        double price = 0;
        double count = 1;

        if (r.prices != null && !r.prices.isEmpty()) price = r.prices.get(0);
        if (r.counts != null && !r.counts.isEmpty()) count = r.counts.get(0);

        try { binding.etAmount.setText(price == (long) price ? String.valueOf((long) price) : String.valueOf(price)); } catch (Exception ignored) {}
        try { binding.etQuantity.setText(count == (long) count ? String.valueOf((long) count) : String.valueOf(count)); } catch (Exception ignored) {}

        // 5) Select Item
        selectedItem = null;
        int itemId = 0;
        if (r.itemIds != null && !r.itemIds.isEmpty()) itemId = r.itemIds.get(0);

        if (itemId > 0) {
            FuelItemDto item = findItemById(itemId);
            if (item != null) {
                selectedItem = item;
                // مهم لتجهيز الحملات
                filterCampaignsByItem(item.id);

                // إذا عندك دعم selection في adapter (إضافي)
                try { itemsAdapter.setSelectedId(item.id); } catch (Exception ignored) {}
                try { itemsAdapter.notifyDataSetChanged(); } catch (Exception ignored) {}
            } else {
                filterCampaignsByItem(0);
            }
        } else {
            filterCampaignsByItem(0);
        }

        // 6) Select Pump
        selectedPump = null;
        if (r.pumpId != null && r.pumpId > 0) {
            PumpDto p = findPumpById(r.pumpId);
            if (p != null) {
                selectedPump = p;
                try { pumpsAdapter.setSelectedId(p.id); } catch (Exception ignored) {}
                try { pumpsAdapter.notifyDataSetChanged(); } catch (Exception ignored) {}
            }
        }

        // 7) Select Campaign
        selectedCampaign = null;
        if (r.campaignId != null && r.campaignId > 0 && filteredCampaigns != null) {
            for (FuelCampaignDto c : filteredCampaigns) {
                if (c != null && c.id == r.campaignId) {
                    selectedCampaign = c;
                    break;
                }
            }
        }
        if (selectedCampaign != null) {
            binding.campaignContainer.setVisibility(View.VISIBLE);
            binding.campaign.setText(selectedCampaign.name != null ? selectedCampaign.name : "");
        } else {
            // لو في حملات للصنف
            if (filteredCampaigns != null && !filteredCampaigns.isEmpty()) {
                binding.campaignContainer.setVisibility(View.VISIBLE);
                binding.campaign.setText(getString(R.string.select_campaign));
            } else {
                hideCampaign();
            }
        }

        // 8) Payments
        paymentContainer.removeAllViews();
        if (r.paymentMethods != null && !r.paymentMethods.isEmpty()) {
            for (co.highfive.petrolstation.fuelsale.dto.PaymentMethodDto pm : r.paymentMethods) {
                addPaymentRowWithValue(pm);
            }
        } else {
            addPaymentRow();
        }

        selectedCustomer = null;

        if (draft.customer_id > 0) {
            FuelCustomerDto c = new FuelCustomerDto();
            c.id = draft.customer_id;
            c.name = draft.customer_name;
            c.account_id = r.accountId; // إذا موجود
            selectedCustomer = c;

            binding.typeCustomer.setText(c.name != null ? c.name : "");
        } else {
            binding.typeCustomer.setText(getString(R.string.select_customer));
        }

        toast("تم تحميل الفاتورة المؤقتة #" + localId);


    }
    @Nullable
    private FuelItemDto findItemById(int id) {
        for (FuelItemDto it : items) {
            if (it != null && it.id == id) return it;
        }
        return null;
    }

    @Nullable
    private PumpDto findPumpById(int id) {
        for (PumpDto p : pumps) {
            if (p != null && p.id == id) return p;
        }
        return null;
    }


    private void addPaymentRowWithValue(@NonNull co.highfive.petrolstation.fuelsale.dto.PaymentMethodDto pm) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View row = inflater.inflate(R.layout.item_payment_method, paymentContainer, false);

        Spinner spPayment = row.findViewById(R.id.sp_payment_type);
        EditText etAmount = row.findViewById(R.id.et_payment_amount);
        ImageView btnRemove = row.findViewById(R.id.btn_remove_payment);

        List<String> names = new ArrayList<>();
        for (LookupDto l : paymentTypes) {
            names.add(l.name);
        }

        ArrayAdapter<String> adapter = buildPaymentSpinnerAdapter(names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPayment.setAdapter(adapter);

        // set selected payment type by id
        int selectedIndex = 0;
        for (int i = 0; i < paymentTypes.size(); i++) {
            LookupDto t = paymentTypes.get(i);
            if (t != null && t.id == pm.payment_type_id) {
                selectedIndex = i;
                break;
            }
        }
        try { spPayment.setSelection(selectedIndex); } catch (Exception ignored) {}

        // set amount
        try {
            double a = pm.amount;
            etAmount.setText(a == (long) a ? String.valueOf((long) a) : String.valueOf(a));
        } catch (Exception ignored) {}

        btnRemove.setOnClickListener(v -> paymentContainer.removeView(row));
        paymentContainer.addView(row);
    }

    private ArrayList<co.highfive.petrolstation.fuelsale.dto.FuelSaleDraft> readDraftsFromSession() {
        try {
            String raw = getSessionManager().getString(SESSION_KEY_FUEL_SALE_DRAFTS);
            if (raw == null || raw.trim().isEmpty()) return new ArrayList<>();

            Type type = new TypeToken<ArrayList<co.highfive.petrolstation.fuelsale.dto.FuelSaleDraft>>() {}.getType();
            ArrayList<co.highfive.petrolstation.fuelsale.dto.FuelSaleDraft> list = getGson().fromJson(raw, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void resetForNewInvoice() {

        selectedUnitPrice = 0;
        isUpdatingCalc = false;
        lastEditSource = LastEditSource.NONE;

        activeDraftLocalId = null;

        selectedItem = null;
        selectedPump = null;
        if (itemsAdapter != null) itemsAdapter.clearSelection();
        if (pumpsAdapter != null) pumpsAdapter.clearSelection();

        // Customer + Vehicle
        selectedCustomer = null;
        lastCustomerSearch = "";
        lastCustomerResults = new ArrayList<>();
        binding.typeCustomer.setText(getString(R.string.select_customer));

        selectedVehicle = null;
        lastVehicleResults = new ArrayList<>();
        lastVehiclesCustomerId = 0;
        binding.vehicle.setText(getString(R.string.select_vehicle));

        // Item + Pump + Campaign
        selectedItem = null;
        selectedPump = null;

        selectedCampaign = null;
        filteredCampaigns.clear();
        binding.campaign.setText(getString(R.string.select_campaign));
        hideCampaign();

        // Inputs
        try { binding.etAmount.setText(""); } catch (Exception ignored) {}
        try { binding.etQuantity.setText(""); } catch (Exception ignored) {}
        try { binding.etNotes.setText(""); } catch (Exception ignored) {}

        // Payments
        paymentContainer.removeAllViews();
        addPaymentRow();

        // إذا عندك في adapters selection state، اعمل reset:
        // itemsAdapter.clearSelection();
        // pumpsAdapter.clearSelection();
    }


    private void openAddVehicleDialog() {

        if (!isValidSelectedCustomerForFuelSale()) {
            toast("اختار زبون أولاً");
            return;
        }

        if (!connectionAvailable) {
            if (cachedVehicleSettings == null) {

                cachedVehicleSettings = getCustomerVehiclesSettingsFromSession();
                if (cachedVehicleSettings == null) {
                    toast(getString(R.string.general_error));
                    return;
                }
            }
            openAddEditVehicleDialog(null, cachedVehicleSettings);
            return;
        }

        fetchVehicleSettingsThenOpenDialog(null);
    }

    private void fetchVehicleSettingsThenOpenDialog(@Nullable CustomerVehicleDto editVehicle) {

        // 1) already cached in memory
        if (cachedVehicleSettings != null) {
            openAddEditVehicleDialog(editVehicle, cachedVehicleSettings);
            return;
        }

        // 2) must call API
        fetchVehicleSettingsFromApi(true, () -> {
            if (cachedVehicleSettings != null) {
                openAddEditVehicleDialog(editVehicle, cachedVehicleSettings);
            } else {
                toast(getString(R.string.general_error));
            }
        });
    }

    private void fetchVehicleSettingsFromApi(boolean showDialog, @Nullable Runnable onReady) {

        if (showDialog) showProgressHUD();

        Type type = new TypeToken<BaseResponse<co.highfive.petrolstation.customers.dto.VehicleSettingsResponseDto>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.CUSTOMER_VEHICLES_SETTINGS,
                (ApiClient.ApiParams) null,
                null,
                type,
                0,
                new ApiCallback<co.highfive.petrolstation.customers.dto.VehicleSettingsResponseDto>() {

                    @Override
                    public void onSuccess(co.highfive.petrolstation.customers.dto.VehicleSettingsResponseDto data, String msg, String rawJson) {
                        if (showDialog) hideProgressHUD();
                        cachedVehicleSettings = data;
                        if (onReady != null) onReady.run();
                    }

                    @Override
                    public void onError(co.highfive.petrolstation.network.ApiError error) {
                        if (showDialog) hideProgressHUD();
                        toast(error != null ? error.message : getString(R.string.general_error));
                        if (onReady != null) onReady.run();
                    }

                    @Override
                    public void onUnauthorized(String rawJson) {
                        if (showDialog) hideProgressHUD();
                        logout();
                    }

                    @Override
                    public void onNetworkError(String reason) {
                        if (showDialog) hideProgressHUD();
                        toast(R.string.no_internet);
                        if (onReady != null) onReady.run();
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        if (showDialog) hideProgressHUD();
                        toast(getString(R.string.general_error));
                        if (onReady != null) onReady.run();
                    }
                }
        );
    }

    private void openAddEditVehicleDialog(@Nullable CustomerVehicleDto editVehicle,
                                          @NonNull co.highfive.petrolstation.customers.dto.VehicleSettingsResponseDto settings) {

        if(settings != null ){
            errorLogger("settings","not null");
            if(settings.vehicle_type != null ){
                errorLogger("settings.vehicle_type",""+settings.vehicle_type.size());
            }
        }else{
            errorLogger("settings","not");
        }
        activeVehicleDialog = AddVehicleDialog.newInstance(
                String.valueOf(selectedCustomer.id), // ✅ customerId
                editVehicle,                          // null => add
                settings
        );

        activeVehicleDialog.setCancelable(false);

        activeVehicleDialog.setListener(new AddVehicleDialog.Listener() {
            @Override
            public void onSubmitAdd(@NonNull java.util.Map<String, String> payload) {
                submitAddVehicle(payload);
            }

            @Override
            public void onSubmitEdit(@NonNull java.util.Map<String, String> payload) {
                // مش مطلوب هسا
            }

            @Override
            public void onDismissed() {}
        });

        activeVehicleDialog.show(getSupportFragmentManager(), "AddVehicleDialog");
    }

    private void submitAddVehicle(java.util.Map<String, String> payload) {

        if (payload == null) {
            toast(getString(R.string.general_error));
            return;
        }

        // ✅ OFFLINE
        if (!connectionAvailable) {
            saveVehicleOfflineAndSelect(payload);
            return;
        }


        showProgressHUD();

        Type type = new TypeToken<BaseResponse<CustomerVehicleDto>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_POST,
                Endpoints.CUSTOMER_VEHICLES_ADD,
                payload,
                null,
                type,
                0,
                new ApiCallback<CustomerVehicleDto>() {

                    @Override
                    public void onSuccess(CustomerVehicleDto data, String msg, String rawJson) {
                        hideProgressHUD();

                        if (activeVehicleDialog != null) {
                            activeVehicleDialog.dismissAllowingStateLoss();
                            activeVehicleDialog = null;
                        }

                        toast(msg != null && !msg.trim().isEmpty() ? msg : getString(R.string.done));

                        if (data == null || data.id <= 0) {
                            toast(getString(R.string.general_error));
                            return;
                        }

                        // ✅ 1) خليها Selected مباشرة
                        selectedVehicle = data;

                        // ✅ 2) اعرضها بالـ UI (اسم + لون + رقم)
                        binding.vehicle.setText(buildVehicleDisplayText(data));

                        // ✅ 3) حدّث كاش المركبات (وخليها أول عنصر)
                        if (lastVehicleResults == null) lastVehicleResults = new ArrayList<>();
                        removeVehicleIfExists(lastVehicleResults, data.id);
                        lastVehicleResults.add(0, data);
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

    private void saveVehicleOfflineAndSelect(@NonNull java.util.Map<String, String> payload) {

        if (!isValidSelectedCustomerForFuelSale()) {
            toast("اختار زبون أولاً");
            return;
        }

        showProgressHUD();

        dbExecutor.execute(() -> {
            try {
                long now = System.currentTimeMillis();

                co.highfive.petrolstation.data.local.entities.OfflineCustomerVehicleEntity e =
                        new co.highfive.petrolstation.data.local.entities.OfflineCustomerVehicleEntity();

                e.customerId = safeInt(payload.get("customer_id"));
                if (e.customerId <= 0 && selectedCustomer != null) {
                    e.customerId = selectedCustomer.id;
                }

                e.vehicleNumber = safe(payload.get("vehicle_number"));
                e.model = safe(payload.get("model"));
                e.licenseExpiryDate = safe(payload.get("license_expiry_date"));
                e.notes = safe(payload.get("notes"));

                e.vehicleType = safeInt(payload.get("vehicle_type"));
                e.vehicleColor = safeInt(payload.get("vehicle_color"));

                // أسماء العرض لو موجودة (اختياري)
//                e.vehicleTypeName = safe(payload.get("vehicle_type_name"));
//                e.vehicleColorName = safe(payload.get("vehicle_color_name"));

                try {
                    if (cachedVehicleSettings != null) {
                        e.vehicleTypeName = resolveSimpleSettingName(
                                cachedVehicleSettings.vehicle_type, e.vehicleType
                        );
                        e.vehicleColorName = resolveSimpleSettingName(
                                cachedVehicleSettings.vehicle_color, e.vehicleColor
                        );
                    }
                } catch (Exception ignored) {}

                e.syncStatus = 0;
                e.syncError = null;
                e.createdAtTs = now;
                e.updatedAtTs = now;

                e.requestJson = null;

                long localId = db.offlineCustomerVehicleDao().insert(e);
//                e.accountId = (selectedCustomer != null && selectedCustomer.account_id != null)
//                        ? selectedCustomer.account_id
//                        : 0;
                e.localId = localId;
                e.requestJson = buildOfflineVehicleRequestJson(e, payload);
                db.offlineCustomerVehicleDao().update(e);

                // ✅ ارجعها كـ DTO "وهمية" مثل offline customer (id سالب)
                CustomerVehicleDto dto = new CustomerVehicleDto();
                dto.id = (int) (-localId); // مهم: ID سالب للتمييز
                dto.customer_id = e.customerId;
//                dto.account_id = e.accountId;
                e.accountId = selectedCustomer != null ? selectedCustomer.account_id : 0;

                dto.vehicle_number = e.vehicleNumber;
                dto.vehicle_type = e.vehicleType;
                dto.vehicle_color = e.vehicleColor;
                dto.model = e.model;
                dto.license_expiry_date = e.licenseExpiryDate;
                dto.notes = e.notes;

                try {
                    if (cachedVehicleSettings != null) {
                        dto.vehicle_type_name = resolveSimpleSettingName(
                                cachedVehicleSettings.vehicle_type, dto.vehicle_type
                        );
                        dto.vehicle_color_name = resolveSimpleSettingName(
                                cachedVehicleSettings.vehicle_color, dto.vehicle_color
                        );
                    }
                } catch (Exception ignored) {}

                mainHandler.post(() -> {
                    hideProgressHUD();

                    if (activeVehicleDialog != null) {
                        activeVehicleDialog.dismissAllowingStateLoss();
                        activeVehicleDialog = null;
                    }

                    toast("تم حفظ المركبة (أوفلاين)");

                    selectedVehicle = dto;
                    binding.vehicle.setText(buildVehicleDisplayText(dto));

                    if (lastVehicleResults == null) lastVehicleResults = new ArrayList<>();
                    removeVehicleIfExists(lastVehicleResults, dto.id);
                    lastVehicleResults.add(0, dto);
                    lastVehiclesCustomerId = selectedCustomer.id;
                });

            } catch (Exception ex) {
                mainHandler.post(() -> {
                    hideProgressHUD();
                    toast(getString(R.string.general_error));
                });
            }
        });
    }

    private String resolveSimpleSettingName(java.util.List<co.highfive.petrolstation.customers.dto.SimpleSettingDto> list, Integer id) {
        if (list == null || id == null) return "";
        for (co.highfive.petrolstation.customers.dto.SimpleSettingDto s : list) {
            if (s != null && s.id == id) return s.name != null ? s.name : "";
        }
        return "";
    }



    private int safeInt(String s) {
        try {
            if (s == null) return 0;
            s = s.trim();
            if (s.isEmpty()) return 0;
            return Integer.parseInt(s);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private String buildOfflineVehicleRequestJson(
            co.highfive.petrolstation.data.local.entities.OfflineCustomerVehicleEntity e,
            java.util.Map<String, String> payload
    ) {
        try {
            java.util.Map<String, Object> root = new java.util.HashMap<>();
            root.put("local_id", e.localId);
            root.put("customer_id", e.customerId);

            // خزّن payload كما هو (أفضل لأنه هو نفس اللي كنت سترسله للـ API)
            root.put("payload", payload);

            return getGson().toJson(root);
        } catch (Exception ignored) {
            return null;
        }
    }


    private void removeVehicleIfExists(ArrayList<CustomerVehicleDto> list, int id) {
        if (list == null) return;
        for (int i = 0; i < list.size(); i++) {
            CustomerVehicleDto v = list.get(i);
            if (v != null && v.id == id) {
                list.remove(i);
                return;
            }
        }
    }

    private void openSelectVehicleDialog() {

        if (!isValidSelectedCustomerForFuelSale()) {
            toast("اختار زبون أولاً");
            return;
        }

        // ✅ OFFLINE: load vehicles from ROOM
        // ✅ OFFLINE: load vehicles from ROOM (online table + offline table)
        if (!connectionAvailable) {

            showProgressHUD();

//            int customerId = selectedCustomer.id > 0 ? selectedCustomer.id : 0;
            int customerId = selectedCustomer.id; // خليه كما هو (موجب أو سالب)

            dbExecutor.execute(() -> {

                List<CustomerVehicleEntity> rowsOnline = new ArrayList<>();
                List<co.highfive.petrolstation.data.local.entities.OfflineCustomerVehicleEntity> rowsOffline = new ArrayList<>();

                try {
                    rowsOnline = db.customerVehicleDao().getByCustomerId(customerId);
                } catch (Exception e) {
                    errorLogger("OfflineVehiclesOnline", e.getMessage() == null ? "null" : e.getMessage());
                }

                try {
                    rowsOffline = db.offlineCustomerVehicleDao().getPendingByCustomer(customerId);
                } catch (Exception e) {
                    errorLogger("OfflineVehiclesOffline", e.getMessage() == null ? "null" : e.getMessage());
                }

                ArrayList<CustomerVehicleDto> dtoList = new ArrayList<>();

                // 1) online vehicles
                dtoList.addAll(mapVehiclesEntitiesToDto(rowsOnline));

                // 2) offline vehicles (id سالب)
                dtoList.addAll(mapOfflineVehiclesEntitiesToDto(rowsOffline));

                errorLogger("dtoList",""+dtoList.size());
                mainHandler.post(() -> {
                    hideProgressHUD();

                    lastVehiclesCustomerId = customerId;
                    lastVehicleResults = dtoList;

                    errorLogger("lastVehiclesCustomerId",""+lastVehiclesCustomerId);
                    openVehicleDialogWithCache(dtoList);
                });
            });

            return;
        }

        // ✅ ONLINE: use cache (same logic you already have)
        ArrayList<CustomerVehicleDto> cache =
                (lastVehiclesCustomerId == selectedCustomer.id) ? lastVehicleResults : new ArrayList<>();

        openVehicleDialogWithCache(cache);
    }

    @NonNull
    private ArrayList<CustomerVehicleDto> mapOfflineVehiclesEntitiesToDto(
            @NonNull List<co.highfive.petrolstation.data.local.entities.OfflineCustomerVehicleEntity> rows
    ) {
        ArrayList<CustomerVehicleDto> out = new ArrayList<>();

        for (co.highfive.petrolstation.data.local.entities.OfflineCustomerVehicleEntity e : rows) {
            if (e == null) continue;

            CustomerVehicleDto d = new CustomerVehicleDto();
            d.id = (int) (-e.localId); // سالب
            d.customer_id = e.customerId;
            d.account_id = e.accountId;

            d.vehicle_number = e.vehicleNumber;
            d.vehicle_type = e.vehicleType;
            d.vehicle_color = e.vehicleColor;
            d.model = e.model;
            d.license_expiry_date = e.licenseExpiryDate;
            d.notes = e.notes;

            d.vehicle_type_name = e.vehicleTypeName;
            d.vehicle_color_name = e.vehicleColorName;

            out.add(d);
        }

        return out;
    }



    private void openVehicleDialogWithCache(ArrayList<CustomerVehicleDto> cache) {

        SelectVehicleDialog d = SelectVehicleDialog.newInstance(
                selectedCustomer.id,
                cache,
                selectedVehicle != null ? selectedVehicle.id : 0
        );

        d.setCancelable(false);

        d.setListener(vehicle -> {
            selectedVehicle = vehicle;
            binding.vehicle.setText(buildVehicleDisplayText(vehicle));

            lastVehiclesCustomerId = selectedCustomer.id;
            lastVehicleResults = d.getLastVehicles();
        });

        d.show(getSupportFragmentManager(), "SelectVehicleDialog");
    }

    private String buildVehicleDisplayText(CustomerVehicleDto v) {
        if (v == null) return "";

        String typeName = v.vehicle_type_name != null ? v.vehicle_type_name.trim() : "";
        String model = v.model != null ? v.model.trim() : "";
        String color = v.vehicle_color_name != null ? v.vehicle_color_name.trim() : "";
        String number = v.vehicle_number != null ? v.vehicle_number.trim() : "";

        String name = typeName;
        if (!model.isEmpty()) {
            name = name.isEmpty() ? model : (name + " " + model);
        }

        // مثال: "سكودا 2020 - أبيض - 43431"
        String out = "";
        if (!name.isEmpty()) out = name;
        if (!color.isEmpty()) out = out.isEmpty() ? color : (out + " - " + color);
        if (!number.isEmpty()) out = out.isEmpty() ? number : (out + " - " + number);

        return out;
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

        if (!connectionAvailable) {
            saveCustomerOfflineAndSelect(name, mobile);
            return;
        }

        showProgressHUD();

        String endpoint = Endpoints.CUSTOMERS_ADD;

        java.util.Map<String, String> params = ApiClient.mapOf(
                "name", name,
                "mobile", mobile,
                "status",1
        );

        // إذا الـ API عندكم يتوقع حقول إضافية، ضيفها هنا حسب الحاجة:
        // params.put("address", "");
        // params.put("aesseal_no_check", "");

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

                        // ✅ سلكتد مباشرة من نفس الريسبونس
                        applySelectedCustomer(data);

                        // ✅ حضّر حالة الديلوج (آخر بحث + النتائج) عشان إذا فتح SelectCustomerDialog يرجع واضح
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

    private void saveCustomerOfflineAndSelect(String name, String mobile) {
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
    }
    private boolean isValidSelectedCustomerForFuelSale() {
        if (selectedCustomer == null) return false;
        if (selectedCustomer.id > 0) return true; // online
        return selectedCustomer.is_offline && selectedCustomer.local_id > 0 && selectedCustomer.id < 0; // offline
    }

    private void insertOfflineCustomerAndSelect(String name, String mobile) {

        errorLogger("name",""+name);
        errorLogger("mobile",""+mobile);
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
                    errorLogger(" dto.mobile",""+ dto.mobile);
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


    private String normalizeMobile(String mobile) {
        if (mobile == null) return "";
        String m = mobile.trim();
        m = m.replace(" ", "").replace("-", "").replace("+", "");
        // لو بدك تشيل 00
        if (m.startsWith("00")) m = m.substring(2);
        return m;
    }

    private void applySelectedCustomer(FuelCustomerDto customer) {

        selectedCampaign = null;
        filteredCampaigns.clear();
        binding.campaign.setText(getString(R.string.select_campaign));
        hideCampaign();

        if (customer == null) return;

        boolean validOnline  = customer.id > 0;
        boolean validOffline = customer.is_offline && customer.local_id > 0 && customer.id < 0;

        if (!validOnline && !validOffline) return;

        boolean customerChanged = (selectedCustomer == null || selectedCustomer.id != customer.id);

        selectedCustomer = customer;
        binding.typeCustomer.setText(customer.name != null ? customer.name : "");

        if (customerChanged) {
            selectedVehicle = null;
            lastVehicleResults = new ArrayList<>();
            lastVehiclesCustomerId = 0;
            binding.vehicle.setText(getString(R.string.select_vehicle));
        }
    }




    private void openSelectCustomerDialog() {

        // ✅ إذا في زبون محدد، حطه في النتائج الافتراضية
        ArrayList<FuelCustomerDto> results = lastCustomerResults != null ? lastCustomerResults : new ArrayList<>();

        if (selectedCustomer != null && selectedCustomer.id > 0) {
            boolean exists = false;
            for (FuelCustomerDto c : results) {
                if (c != null && c.id == selectedCustomer.id) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                results = new ArrayList<>(results);
                results.add(0, selectedCustomer);
            }
            lastCustomerSearch = selectedCustomer.name != null ? selectedCustomer.name : lastCustomerSearch;
            lastCustomerResults = results;
        }

        SelectCustomerDialog d = SelectCustomerDialog.newInstance(
                lastCustomerSearch,
                lastCustomerResults,
                selectedCustomer != null ? selectedCustomer.id : 0
        );

        d.setCancelable(false);

        d.setListener(customer -> {
            applySelectedCustomer(customer);
            lastCustomerSearch = d.getLastSearch();
            lastCustomerResults = d.getLastResults();
        });

        d.show(getSupportFragmentManager(), "SelectCustomerDialog");
    }


    /* ================= API ================= */

    private void loadFuelSaleSettings() {

        showProgressHUD();

        Type type = new TypeToken<BaseResponse<FuelPriceSettingsData>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.FUEL_PRICE_SETTINGS,
                (ApiClient.ApiParams) null,
                null,
                type,
                0,
                new ApiCallback<FuelPriceSettingsData>() {

                    @Override
                    public void onSuccess(FuelPriceSettingsData data, String message, String rawJson) {
                        hideProgressHUD();

                        if (data == null) return;

//                        settingsData = data;
//
//                        items.clear();
//                        pumps.clear();
//                        campaigns.clear();
//                        paymentTypes.clear();
//
//                        if (data.items != null) items.addAll(data.items);
//                        if (data.pumps != null) pumps.addAll(data.pumps);
//                        if (data.campaigns != null) campaigns.addAll(data.campaigns);
//                        if (data.payment_type != null) paymentTypes.addAll(data.payment_type);
//
//                        itemsAdapter.updateData(items);
//                        pumpsAdapter.updateData(pumps);
//
//                        hideCampaign();
//
//                        paymentContainer.removeAllViews();
//                        addPaymentRow();
//                        if (pendingApplyDraftId != null) {
//                            int id = pendingApplyDraftId;
//                            pendingApplyDraftId = null;
//                            applyDraftById(id);
//                        }
                        applyFuelSettingsToUI(data);
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

    /* ================= CAMPAIGNS ================= */

    private void hideCampaign() {
        binding.campaignContainer.setVisibility(View.GONE);
    }


    /* ================= PAYMENT METHODS ================= */


    private void addPaymentRow() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View row = inflater.inflate(R.layout.item_payment_method, paymentContainer, false);

        Spinner spPayment = row.findViewById(R.id.sp_payment_type);
        EditText etAmount = row.findViewById(R.id.et_payment_amount);
        ImageView btnRemove = row.findViewById(R.id.btn_remove_payment);

        // ✅ هون مكان الكود تبعك بالضبط: تجهيز قائمة أسماء طرق الدفع وربطها بالـ Spinner
        List<String> names = new ArrayList<>();
        for (LookupDto l : paymentTypes) {
            names.add(l.name);
        }

        ArrayAdapter<String> adapter =  buildPaymentSpinnerAdapter(names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPayment.setAdapter(adapter);

        // حذف الصف
        btnRemove.setOnClickListener(v -> paymentContainer.removeView(row));

        paymentContainer.addView(row);
    }

    private ArrayAdapter<String> buildPaymentSpinnerAdapter(List<String> names) {
        android.graphics.Typeface tf = androidx.core.content.res.ResourcesCompat.getFont(this, R.font.din_regular);

        return new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, names) {

            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                try {
                    android.widget.TextView tv = (android.widget.TextView) v;
                    tv.setTypeface(tf);
                    tv.setTextSize(14);
                    tv.setGravity(android.view.Gravity.RIGHT | android.view.Gravity.CENTER_VERTICAL);
                } catch (Exception ignored) {}
                return v;
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                try {
                    android.widget.TextView tv = (android.widget.TextView) v;
                    tv.setTypeface(tf);
                    tv.setTextSize(14);
                    tv.setGravity(android.view.Gravity.RIGHT | android.view.Gravity.CENTER_VERTICAL);
                    tv.setPadding(24, 24, 24, 24);
                } catch (Exception ignored) {}
                return v;
            }
        };
    }

    private void openSelectCampaignDialog() {

        if (selectedItem == null || selectedItem.id <= 0) {
            toast("اختار صنف أولاً");
            return;
        }

        if (filteredCampaigns == null || filteredCampaigns.isEmpty()) {
            toast("لا يوجد حملات لهذا الصنف");
            return;
        }

        int selectedId = (selectedCampaign != null) ? selectedCampaign.id : 0;

        co.highfive.petrolstation.fragments.SelectCampaignDialog d =
                co.highfive.petrolstation.fragments.SelectCampaignDialog.newInstance(filteredCampaigns, selectedId);

        d.setCancelable(false);

        d.setListener(campaign -> {
            selectedCampaign = campaign;

            // اعرض اسم الحملة
            binding.campaign.setText(campaign != null && campaign.name != null ? campaign.name : "");
        });

        d.show(getSupportFragmentManager(), "SelectCampaignDialog");
    }

    private void filterCampaignsByItem(int itemId) {

        filteredCampaigns.clear();
        selectedCampaign = null;

        // نص placeholder للحملة (غيّره حسب TextView عندك)
        binding.campaign.setText(getString(R.string.select_campaign));

        if (campaigns.isEmpty()) {
            hideCampaign();
            return;
        }

        for (FuelCampaignDto c : campaigns) {
            if (c == null || c.items == null) continue;

            for (FuelCampaignItemDto ci : c.items) {
                if (ci != null && ci.item_id != null && ci.item_id == itemId) {
                    filteredCampaigns.add(c);
                    break;
                }
            }
        }

        if (filteredCampaigns.isEmpty()) {
            hideCampaign();
        } else {
            binding.campaignContainer.setVisibility(View.VISIBLE);
        }
    }

    private void showSuccessDialog(String apiMessage) {

        if (isFinishing()) return;
        if (successDialog != null && successDialog.isShowing()) return;

        View v = LayoutInflater.from(this).inflate(R.layout.dialog_success, null, false);

        android.widget.TextView txtMessage = v.findViewById(R.id.txtMessage);
        android.widget.TextView btnOk = v.findViewById(R.id.btnOk);

        String msg = (apiMessage != null && !apiMessage.trim().isEmpty())
                ? apiMessage
                : getString(R.string.done);

        txtMessage.setText(msg);

        android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(this);
        b.setView(v);
        b.setCancelable(false);

        successDialog = b.create();
        if (successDialog.getWindow() != null) {
            successDialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
            );
        }
        successDialog.show();

        btnOk.setOnClickListener(view -> {
            try { if (successDialog != null) successDialog.dismiss(); } catch (Exception ignored) {}

            // ✅ لو كانت فاتورة من المسودات، احذفها بعد الحفظ
            if (hasActiveDraftSelected()) {
                try { removeDraftById(activeDraftLocalId); } catch (Exception ignored) {}
                activeDraftLocalId = null;
            }

            // ✅ فرّغ كل شيء وخلي المستخدم يكمل بنفس الصفحة
            resetForNewInvoice();
        });
    }

    @NonNull
    private ArrayList<CustomerVehicleDto> mapVehiclesEntitiesToDto(@NonNull List<CustomerVehicleEntity> rows) {
        ArrayList<CustomerVehicleDto> out = new ArrayList<>();
        for (CustomerVehicleEntity e : rows) {
            if (e == null) continue;

            CustomerVehicleDto d = new CustomerVehicleDto();
            d.id = e.id;
            d.customer_id = e.customerId;
            d.vehicle_number = e.vehicleNumber;
            d.vehicle_type = e.vehicleType;
            d.vehicle_color = e.vehicleColor;
            d.model = e.model;
            d.license_expiry_date = e.licenseExpiryDate;
            d.notes = e.notes;
            d.created_at = e.createdAt;

            d.vehicle_type_name = e.vehicleTypeName;
            d.vehicle_color_name = e.vehicleColorName;

            d.account_id = e.accountId;

            out.add(d);
        }
        return out;
    }


}
