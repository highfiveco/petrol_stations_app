package co.highfive.petrolstation.activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.adapters.InvoiceDetailsItemsAdapter;
import co.highfive.petrolstation.catalog.dto.ItemDto;
import co.highfive.petrolstation.customers.dto.InvoiceDetailDto;
import co.highfive.petrolstation.customers.dto.InvoiceDto;
import co.highfive.petrolstation.data.local.AppDatabase;
import co.highfive.petrolstation.data.local.DatabaseProvider;
import co.highfive.petrolstation.data.local.entities.InvoiceDetailEntity;
import co.highfive.petrolstation.data.local.entities.InvoiceEntity;
import co.highfive.petrolstation.data.local.entities.ItemEntity;
import co.highfive.petrolstation.data.local.relations.InvoiceDetailWithItem;
import co.highfive.petrolstation.data.local.relations.InvoiceWithDetails;
import co.highfive.petrolstation.databinding.ActivityInvoiceDetailsBinding;
import co.highfive.petrolstation.databinding.RowInvoiceSummaryBinding;
import co.highfive.petrolstation.fuelsale.dto.FuelItemDto;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.models.Setting;
import co.highfive.petrolstation.data.local.entities.OfflineInvoiceEntity;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
public class InvoiceDetailsActivity extends BaseActivity {

    private ActivityInvoiceDetailsBinding binding;

    private InvoiceDto invoice;
    private InvoiceDetailsItemsAdapter itemsAdapter;

    private AppDatabase db;
    private ExecutorService dbExecutor;
    private android.os.Handler mainHandler;

    private int invoiceId = 0;
    private boolean isOffline = false;
    private long localId = 0L;
    private String requestJsonExtra = "";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityInvoiceDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupUI(binding.root);

        db = DatabaseProvider.get(this);
        dbExecutor = Executors.newSingleThreadExecutor();
        mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

        initRecycler();
        initClicks();

        // ✅ Load data (online json if exists, else Room)
        loadInvoiceThenBind();
    }

    private void initRecycler() {
        itemsAdapter = new InvoiceDetailsItemsAdapter();
        binding.recyclerItems.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerItems.setAdapter(itemsAdapter);
    }

    private void loadInvoiceThenBind() {
        Bundle b = getIntent() != null ? getIntent().getExtras() : null;

        String raw = "";
        if (b != null) {
            raw = safe(b.getString("raw_invoice_json"));

            // ✅ NEW: offline flags
            isOffline = b.getBoolean("is_offline", false);
            localId = b.getLong("local_id", 0L);
            requestJsonExtra = safe(b.getString("request_json"));

            try {
                invoiceId = b.getInt("invoice_id", 0);
            } catch (Exception ignored) {}
        }

        // =========================================================
        // ✅ 1) OFFLINE DETAILS (Pending/Failed)
        // =========================================================
        if (isOffline) {
            if (localId <= 0) {
                toast(getString(R.string.general_error));
                finish();
                return;
            }

            showProgressHUD();

            dbExecutor.execute(() -> {
                OfflineInvoiceEntity off = null;
                try {
                    off = db.offlineInvoiceDao().getByLocalId(localId);
                } catch (Exception e) {
                    errorLogger("InvoiceDetailsOfflineRoom", e.getMessage() == null ? "null" : e.getMessage());
                }

                InvoiceDto dto = null;
                try {
                    dto = mapOfflineInvoiceToDto(off, requestJsonExtra);
                } catch (Exception e) {
                    errorLogger("InvoiceDetailsOfflineMap", e.getMessage() == null ? "null" : e.getMessage());
                }

                InvoiceDto finalDto = dto;

                mainHandler.post(() -> {
                    hideProgressHUD();

                    if (finalDto == null) {
                        toast(getString(R.string.general_error));
                        finish();
                        return;
                    }

                    invoice = finalDto;
                    bindData();
                });
            });

            return;
        }

        // =========================================================
        // ✅ 2) ONLINE RAW JSON (Fast path)
        // =========================================================
        if (!raw.trim().isEmpty()) {
            try {
                Type type = new TypeToken<InvoiceDto>() {}.getType();
                invoice = getGson().fromJson(raw, type);
                bindData();
                return;
            } catch (Exception e) {
                errorLogger("InvoiceDetailsParse", e.getMessage() == null ? "null" : e.getMessage());
            }
        }

        // =========================================================
        // ✅ 3) ONLINE/ROOM fallback by invoice_id
        // =========================================================
        if (invoiceId <= 0) {
            toast(getString(R.string.general_error));
            finish();
            return;
        }

        showProgressHUD();

        dbExecutor.execute(() -> {
            InvoiceWithDetails row = null;
            try {
                row = db.invoiceDao().getInvoiceWithDetails(invoiceId);
            } catch (Exception e) {
                errorLogger("InvoiceDetailsRoom", e.getMessage() == null ? "null" : e.getMessage());
            }

            InvoiceDto dto = (row != null) ? mapInvoiceWithDetailsToDto(row) : null;

            mainHandler.post(() -> {
                hideProgressHUD();

                if (dto == null) {
                    toast(getString(R.string.general_error));
                    finish();
                    return;
                }

                invoice = dto;
                bindData();
            });
        });
    }

    @Nullable
    private InvoiceDto mapOfflineInvoiceToDto(@Nullable OfflineInvoiceEntity off, @NonNull String requestJsonFromIntent) {
        if (off == null) return null;

        InvoiceDto dto = new InvoiceDto();

        // flags
        dto.is_offline = true;
        dto.local_id = off.localId;
        dto.sync_status = off.syncStatus;
        dto.sync_error = off.syncError;

        // header snapshot
        dto.id = (off.onlineId != null) ? off.onlineId : 0;
        dto.account_id = off.accountId;
        dto.statement = safe(off.statement);
        dto.invoice_no = (off.invoiceNo != null && !off.invoiceNo.trim().isEmpty()) ? off.invoiceNo : ("OFF-" + off.localId);
        dto.total = off.total;
        dto.notes = off.notes;

        // date from createdAtTs
        dto.date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
                .format(new java.util.Date(off.createdAtTs));

        // paid/remain (offline guess)
        dto.pay_amount = 0.0;
        dto.discount = 0.0;
        dto.remain = safeDouble(off.total);

        // =====================================================
        // details from requestJson
        // =====================================================
        String json = !requestJsonFromIntent.trim().isEmpty() ? requestJsonFromIntent : safe(off.requestJson);
//        dto.request_json = json;

        dto.details = buildDetailsFromOfflineRequestJson(json);

        return dto;
    }

    @NonNull
    private ArrayList<InvoiceDetailDto> buildDetailsFromOfflineRequestJson(@NonNull String json) {
        ArrayList<InvoiceDetailDto> out = new ArrayList<>();
        if (json.trim().isEmpty()) return out;

        try {
            JsonObject root = getGson().fromJson(json, JsonObject.class);
            if (root == null) return out;

            JsonArray arr = null;

            // جرّب أسماء شائعة
            if (root.has("items") && root.get("items").isJsonArray()) arr = root.getAsJsonArray("items");
            else if (root.has("details") && root.get("details").isJsonArray()) arr = root.getAsJsonArray("details");
            else if (root.has("invoice_items") && root.get("invoice_items").isJsonArray()) arr = root.getAsJsonArray("invoice_items");
            else if (root.has("fuel_items") && root.get("fuel_items").isJsonArray()) arr = root.getAsJsonArray("fuel_items");

            if (arr == null) return out;

            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject o = el.getAsJsonObject();

                InvoiceDetailDto d = new InvoiceDetailDto();

                // qty/count
                double qty = 1;
                if (o.has("qty")) qty = safeDoubleJson(o.get("qty"));
                else if (o.has("count")) qty = safeDoubleJson(o.get("count"));

                // price
                double price = 0;
                if (o.has("price")) price = safeDoubleJson(o.get("price"));

                // item id
                int itemId = 0;
                if (o.has("item_id")) itemId = safeIntJson(o.get("item_id"));
                else if (o.has("id")) itemId = safeIntJson(o.get("id"));

                d.item_id = itemId;
                d.count = qty;
                d.price = price;

                // item object (إذا واجهتك مشاكل بالـ FuelItemDto عدّل النوع حسب مشروعك)
                FuelItemDto it = new FuelItemDto();
                it.id = itemId;

                if (o.has("name") && !o.get("name").isJsonNull()) it.name = o.get("name").getAsString();
                d.item = it;

                out.add(d);
            }

        } catch (Exception e) {
            errorLogger("OfflineRequestJsonParse", e.getMessage() == null ? "null" : e.getMessage());
        }

        return out;
    }

    private double safeDoubleJson(JsonElement el) {
        try {
            if (el == null || el.isJsonNull()) return 0;
            if (el.getAsJsonPrimitive().isNumber()) return el.getAsDouble();
            return Double.parseDouble(el.getAsString());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private int safeIntJson(JsonElement el) {
        try {
            if (el == null || el.isJsonNull()) return 0;
            if (el.getAsJsonPrimitive().isNumber()) return el.getAsInt();
            return Integer.parseInt(el.getAsString());
        } catch (Exception ignored) {
            return 0;
        }
    }



    private void bindData() {
        if (invoice == null) return;

        // Customer info (قد لا يكون متوفر offline حسب الداتا)
        String name = "";
        String phone = "";
        try {
            if (invoice.account != null) {
                name = safe(invoice.account.getAccount_name());
                phone = safe(invoice.account.getMobile());
            }
        } catch (Exception ignored) {}

        if (!name.isEmpty() || !phone.isEmpty()) {
            binding.customerRow.setVisibility(View.VISIBLE);
            binding.tvName.setText(name);
            binding.tvPhone.setText(phone);
        } else {
            binding.customerRow.setVisibility(View.GONE);
        }

        // Items
        if (invoice.details != null) itemsAdapter.setItems(invoice.details);

        double total = safeDouble(invoice.total);
        double discount = safeDouble(invoice.discount);
        double paid = safeDouble(invoice.pay_amount);
        double remain = safeDouble(invoice.remain);

        binding.tvTotalBig.setText(formatNumber(total));

        setSummaryRow(binding.rowDiscount, getString(R.string.discount), formatNumber(discount));
        setSummaryRow(binding.rowPaid, getString(R.string.paid_amount), formatNumber(paid));
        setSummaryRow(binding.rowRemaining, getString(R.string.invoice_remaining_amount), formatNumber(remain));
    }

    private void initClicks() {
        binding.icHome.setOnClickListener(v ->
                moveToActivity(getApplicationContext(), MainActivity.class, null, false, true)
        );
        binding.icBack.setOnClickListener(v -> finish());

        binding.tvPhone.setOnClickListener(v -> {
            String phone = safe(binding.tvPhone.getText() != null ? binding.tvPhone.getText().toString() : "");
            if (!phone.trim().isEmpty()) call(phone);
        });

        binding.btnPrint.setOnClickListener(v -> {
            if (invoice == null) return;

            // ✅ لا تطبع Pending (لسه ما انعملها sync)
            if (invoice.is_offline && invoice.sync_status == 0) {
                toast("الفاتورة لسه Pending، لازم تتزامن أولاً.");
                return;
            }

            Setting setting = null;
            try { setting = getAppData().getSetting(); } catch (Exception ignored) {}

            if (setting == null) {
                toast(getString(R.string.general_error));
                return;
            }

            printInvoice(setting, invoice);
        });

    }

    private void setSummaryRow(RowInvoiceSummaryBinding row, String label, String value) {
        if (row == null) return;
        row.label.setText(label);
        row.val.setText(value);
    }

    // ============================================================
    // Mapping Room -> DTO
    // ============================================================
    @NonNull
    private InvoiceDto mapInvoiceWithDetailsToDto(@NonNull InvoiceWithDetails row) {

        InvoiceEntity e = row.invoice;

        InvoiceDto dto = new InvoiceDto();
        dto.id = e.id;
        dto.date = e.date;
        dto.statement = e.statement;
        dto.account_id = (e.accountId != null) ?   e.accountId  : null;
        dto.store_id = (e.storeId != null) ?  e.storeId  : null;

        dto.discount = e.discount;
        dto.total = e.total;

        dto.invoice_no = e.invoiceNo;
        dto.notes = e.notes;

        dto.campaign_id = (e.campaignId != null) ? e.campaignId : null;
        dto.pump_id = (e.pumpId != null) ?e.pumpId  : null;
        dto.customer_vehicle_id = (e.customerVehicleId != null) ?  e.customerVehicleId  : null;

        dto.created_at = e.createdAt;

        // offline fallback
        dto.pay_amount = 0.0;
        dto.remain = safeDouble(e.total) - safeDouble(e.discount);

        // details (Relation: InvoiceDetailWithItem)
        ArrayList<InvoiceDetailDto> details = new ArrayList<>();

        if (row.details != null) {
            for (InvoiceDetailWithItem rel : row.details) {

                // حسب تصميمك غالباً:
                // rel.detail  -> InvoiceDetailEntity (Embedded)
                // rel.item    -> ItemEntity (Relation)
                InvoiceDetailEntity d = rel.detail;

                InvoiceDetailDto dd = new InvoiceDetailDto();
                dd.id = d.id;
                dd.invoice_id = d.invoiceId;
                dd.item_id = d.itemId;
                dd.update_cost_price = d.updateCostPrice;
                dd.count = d.count;
                dd.price = d.price;

                // item object داخل dto (إذا موجود عندك)
                // عدّل أسماء الحقول حسب DTO عندك
                if (rel.item != null) {
                    // مثال إذا InvoiceDetailDto عندك field اسمه item من نوع ItemDto:
                     dd.item = new FuelItemDto();
                     dd.item.id = rel.item.id;
                     dd.item.name = rel.item.name;
                     dd.item.price = rel.item.price;
                     dd.item.barcode = rel.item.barcode;
                     dd.item.icon = rel.item.icon;
                     dd.item.negative_check = rel.item.negativeCheck;

                    // إذا DTO ما فيه item object -> تجاهل أو خزّن name بمكان مناسب
                }

                details.add(dd);
            }
        }

        dto.details = details;
        return dto;
    }


}
