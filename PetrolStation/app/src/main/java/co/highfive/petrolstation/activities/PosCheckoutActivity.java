package co.highfive.petrolstation.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.customers_settings.dto.LookupDto;
import co.highfive.petrolstation.data.local.AppDatabase;
import co.highfive.petrolstation.data.local.DatabaseProvider;
import co.highfive.petrolstation.data.local.entities.OfflineCustomerEntity;
import co.highfive.petrolstation.databinding.ActivityPosCheckoutBinding;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.models.Setting;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;

public class PosCheckoutActivity extends BaseActivity {

    public static final String EXTRA_CUSTOMER_NAME  = "customer_name";
    public static final String EXTRA_CUSTOMER_PHONE = "customer_phone";
    public static final String EXTRA_ITEMS_JSON     = "items_json";
    public static final String EXTRA_GO_ADD_ITEMS   = "go_add_items";

    private ActivityPosCheckoutBinding binding;

    private final ArrayList<co.highfive.petrolstation.pos.dto.PosDraftItemDto> cartItems = new ArrayList<>();
    private CheckoutItemsAdapter itemsAdapter;

    private LinearLayout paymentContainer;
    private final List<LookupDto> paymentTypes = new ArrayList<>();
    private co.highfive.petrolstation.pos.data.PosInvoiceDbHelper posDb;
    private long invoiceId = -1;
    private int openedSwipePosition = RecyclerView.NO_POSITION;
    private float swipeClampWidthPx = 0f;
    public static final String EXTRA_PAYMENT_TYPES_JSON = "payment_types_json";

    public static final String EXTRA_CHECKOUT_DONE = "checkout_done";
    private int accountId = 0;
    private android.app.AlertDialog successDialog;

    private int customerId = 0;
    private String customerName = "";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        posDb = new co.highfive.petrolstation.pos.data.PosInvoiceDbHelper(this);

        binding = ActivityPosCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupUI(binding.getRoot());

        paymentContainer = binding.paymentContainer;

        initHeader();
        initCartList();
        initActions();

        // ✅ مهم: أولاً payment types
        readPaymentTypesFromIntent();

        // ✅ بعدها حمّل invoice (وفيها restore note + payments)
        readIntentAndBind();

        updateTotal();
    }


    private void readPaymentTypesFromIntent() {
        try {
            String json = getIntent().getStringExtra(EXTRA_PAYMENT_TYPES_JSON);
            if (json == null || json.trim().isEmpty()) return;

            Type type = new TypeToken<ArrayList<LookupDto>>(){}.getType();
            ArrayList<LookupDto> list = getGson().fromJson(json, type);

            if (list != null && !list.isEmpty()) {
                setPaymentTypes(list); // عندك الدالة جاهزة
            }
        } catch (Exception ignored) {}
    }


    /* ================= HEADER ================= */

    private void initHeader() {
        binding.icBack.setOnClickListener(v -> {
            persistCheckoutNow();
            Intent i = new Intent();
            i.putExtra("pos_invoice_id", invoiceId);
            setResult(RESULT_OK, i);
            finish();
        });
        binding.icHome.setOnClickListener(v -> finish());
        binding.icCart.setVisibility(View.GONE); // هذه شاشة سلة، ما تحتاج كرت
        binding.icAddNewInvoice.setVisibility(View.GONE);
    }

    /* ================= ACTIONS ================= */
    private android.text.TextWatcher simpleWatcher(Runnable r) {
        return new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { if (r != null) r.run(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        };
    }
    private void initActions() {

        binding.etNotes.addTextChangedListener(simpleWatcher(() -> persistCheckoutNow()));


        // إضافة أصناف جديدة => رجوع للـ POS
        binding.btnAddItems.setOnClickListener(v -> {
            persistCheckoutNow();
            Intent i = new Intent();
            i.putExtra(EXTRA_GO_ADD_ITEMS, true);
            i.putExtra("pos_invoice_id", invoiceId);
            setResult(RESULT_OK, i);
            finish();
        });

        // إضافة طريقة دفع
        binding.btnAddPayment.setOnClickListener(v -> addPaymentRow());

        // حفظ (لاحقًا ريكوست)
        binding.btnSave.setOnClickListener(v -> submitPosSale());



    }

    /* ================= INTENT DATA ================= */

    private void readIntentAndBind() {

         invoiceId = getIntent().getLongExtra("pos_invoice_id", -1);
        if (invoiceId <= 0) { finish(); return; }

        co.highfive.petrolstation.pos.dto.PosActiveInvoice inv = posDb.getInvoice(invoiceId);
        if (inv == null) { finish(); return; }

        binding.customerName.setText(inv.customerName != null ? inv.customerName : "");
        binding.customerPhone.setText(inv.customerMobile != null ? inv.customerMobile : "");
        this.accountId = inv.accountId;
        if (this.accountId <= 0) {
            this.accountId = getIntent().getIntExtra("account_id", 0);
            if (this.accountId > 0) {
                posDb.updateInvoiceAccountId(invoiceId, this.accountId); // ثبّتها
            }
        }
        this.customerId = inv.customerId;           // ✅ لازم يكون موجود بجدول pos invoice
        this.customerName = inv.customerName != null ? inv.customerName : "";
        // ✅ note
        binding.etNotes.setText(inv.note != null ? inv.note : "");

        // ✅ payments (لو موجود)
        restorePaymentsFromJson(inv.paymentsJson);

        cartItems.clear();
        cartItems.addAll(parseItemsJson(inv.itemsJson));
        itemsAdapter.notifyDataSetChanged();

        // payment types لازم تيجي من API/Settings - حالياً خلي افتراضي
//        if (paymentTypes.isEmpty()) {
//            LookupDto def = new LookupDto();
//            def.id = 1;
//            def.name = "نقدي";
//            paymentTypes.add(def);
//        }
    }
    private void restorePaymentsFromJson(String json) {
        paymentContainer.removeAllViews();

        if (json == null || json.trim().isEmpty()) {
            addPaymentRow();
            return;
        }

        try {
            Type t = new TypeToken<ArrayList<co.highfive.petrolstation.fuelsale.dto.PaymentMethodDto>>(){}.getType();
            ArrayList<co.highfive.petrolstation.fuelsale.dto.PaymentMethodDto> list = getGson().fromJson(json, t);

            if (list == null || list.isEmpty()) {
                addPaymentRow();
                return;
            }

            for (co.highfive.petrolstation.fuelsale.dto.PaymentMethodDto pm : list) {
                addPaymentRowWithValue(pm.payment_type_id, pm.amount);
            }

        } catch (Exception e) {
            addPaymentRow();
        }
    }

    private void addPaymentRowWithValue(int paymentTypeId, double amount) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View row = inflater.inflate(R.layout.item_payment_method, paymentContainer, false);

        Spinner spPayment = row.findViewById(R.id.sp_payment_type);
        EditText etAmount = row.findViewById(R.id.et_payment_amount);
        ImageView btnRemove = row.findViewById(R.id.btn_remove_payment);

        List<String> names = new ArrayList<>();
        for (LookupDto l : paymentTypes) names.add(l != null ? l.name : "");

        ArrayAdapter<String> adapter = buildPaymentSpinnerAdapter(names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPayment.setAdapter(adapter);

        int idx = findPaymentTypeIndex(paymentTypeId);
        if (idx >= 0) spPayment.setSelection(idx);

        if (amount > 0) etAmount.setText(String.valueOf(amount));

        btnRemove.setOnClickListener(v -> {
            paymentContainer.removeView(row);
            persistCheckoutNow(); // ✅ احفظ بعد الحذف
        });

        // ✅ احفظ عند أي تغيير
        etAmount.addTextChangedListener(simpleWatcher(() -> persistCheckoutNow()));
        spPayment.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) { persistCheckoutNow(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        paymentContainer.addView(row);
    }


    private int findPaymentTypeIndex(int id) {
        for (int i = 0; i < paymentTypes.size(); i++) {
            LookupDto l = paymentTypes.get(i);
            if (l != null && l.id == id) return i;
        }
        return -1;
    }

    private void persistCheckoutNow() {
        try {
            if (invoiceId <= 0) return;

            String note = binding.etNotes.getText() != null ? binding.etNotes.getText().toString().trim() : "";
            String paymentsJson = getGson().toJson(collectPaymentMethodsFromUI());

            posDb.updateInvoiceCheckout(invoiceId, note, paymentsJson);
        } catch (Exception ignore) {}
    }


    private List<co.highfive.petrolstation.pos.dto.PosDraftItemDto> parseItemsJson(String json) {
        try {
            Type type = new TypeToken<ArrayList<co.highfive.petrolstation.pos.dto.PosDraftItemDto>>() {}.getType();
            ArrayList<co.highfive.petrolstation.pos.dto.PosDraftItemDto> list = getGson().fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /* ================= CART LIST ================= */

    private void initCartList() {
        binding.rvItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvItems.setNestedScrollingEnabled(false);

        itemsAdapter = new CheckoutItemsAdapter(cartItems, new CheckoutItemsAdapter.Listener() {
            @Override
            public void onCartChanged() {
                updateTotal();
                persistCartItemsNow();
            }

            @Override
            public void onDeleteClicked(int position) {
                if (position >= 0 && position < cartItems.size()) {
                    cartItems.remove(position);
                    itemsAdapter.notifyItemRemoved(position);
                    updateTotal();
                    persistCartItemsNow();

                }
            }
        });

        binding.rvItems.setAdapter(itemsAdapter);

        attachSwipeToReveal(binding.rvItems);
        swipeClampWidthPx = dpToPx(72);

    }

    private void attachSwipeToReveal(RecyclerView rv) {

        final float clampWidth = dpToPx(72);
        final float openThreshold = dpToPx(10); // ✅ سحبة بسيطة تفتح
        ItemTouchHelper.SimpleCallback cb = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // لا تعمل swiped اصلاً
                itemsAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 2f;
            }

            @Override
            public void onChildDraw(@NonNull android.graphics.Canvas c,
                                    @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {

                if (!(viewHolder instanceof CheckoutItemsAdapter.VH)) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    return;
                }

                CheckoutItemsAdapter.VH h = (CheckoutItemsAdapter.VH) viewHolder;
                int pos = viewHolder.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                // عندك زر delete بالخلف على اليمين (end) => لازم نسحب لليسار (dX negative)
                float newDX = dX;
                if (newDX < 0) newDX = 0;
                if (newDX > clampWidth) newDX = clampWidth;

                // ✅ إذا تحرك أكثر من 10dp خليه يروح مباشرة للآخر أثناء السحب
                if (newDX > openThreshold) newDX = clampWidth;

                h.fgContent.setTranslationX(newDX);
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);

                if (!(viewHolder instanceof CheckoutItemsAdapter.VH)) return;

                CheckoutItemsAdapter.VH h = (CheckoutItemsAdapter.VH) viewHolder;
                int pos = viewHolder.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                float tx = h.fgContent.getTranslationX();

                // إذا المستخدم سحب لمسافة كافية => افتح وخزّن position
                if (tx >= openThreshold) {

                    if (openedSwipePosition != RecyclerView.NO_POSITION && openedSwipePosition != pos) {
                        int old = openedSwipePosition;
                        openedSwipePosition = RecyclerView.NO_POSITION;
                        itemsAdapter.notifyItemChanged(old);
                    }

                    openedSwipePosition = pos;
                    h.fgContent.setTranslationX(clampWidth); // ثبّت مفتوح
                } else {
                    if (openedSwipePosition == pos) openedSwipePosition = RecyclerView.NO_POSITION;
                    h.fgContent.setTranslationX(0f);
                }
            }
        };

        new ItemTouchHelper(cb).attachToRecyclerView(rv);

        // سكّر السوايب إذا المستخدم لمس خارج / سكرول
        rv.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull android.view.MotionEvent e) {
                if (e.getAction() == android.view.MotionEvent.ACTION_DOWN && openedSwipePosition != RecyclerView.NO_POSITION) {

                    View child = rv.findChildViewUnder(e.getX(), e.getY());
                    if (child == null) return false;

                    int touchedPos = rv.getChildAdapterPosition(child);
                    if (touchedPos == RecyclerView.NO_POSITION) return false;

                    // إذا لمسنا عنصر ثاني غير المفتوح => سكّر المفتوح
                    if (touchedPos != openedSwipePosition) {
                        int old = openedSwipePosition;
                        openedSwipePosition = RecyclerView.NO_POSITION;
                        itemsAdapter.notifyItemChanged(old);
                    }
                }
                return false;
            }
        });

    }

    @Override
    public void onBackPressed() {
        persistCheckoutNow();
        Intent i = new Intent();
        i.putExtra("pos_invoice_id", invoiceId);
        setResult(RESULT_OK, i);
        super.onBackPressed();
    }
    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }


    private void updateTotal() {
        double total = 0;
        for (co.highfive.petrolstation.pos.dto.PosDraftItemDto it : cartItems) {
            total += (it.price * it.qty);
        }
        binding.txtTotal.setText(formatMoney(total));
    }

    private String formatMoney(double v) {
        // عدّل العملة حسب مشروعك
        if (v == (long) v) return "" + (long) v;
        return "" + v;
    }

    /* ================= PAYMENT TYPES ================= */

    // استدعِ هذه لاحقاً بعد ما تجيب طرق الدفع من settings API
    public void setPaymentTypes(@NonNull List<LookupDto> types) {
        paymentTypes.clear();
        paymentTypes.addAll(types);
        // إعادة بناء الصفوف الحالية بنفس الطريقة
//        paymentContainer.removeAllViews();
//        addPaymentRow();
    }

    private void addPaymentRow() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View row = inflater.inflate(R.layout.item_payment_method, paymentContainer, false);

        Spinner spPayment = row.findViewById(R.id.sp_payment_type);
        EditText etAmount = row.findViewById(R.id.et_payment_amount);
        ImageView btnRemove = row.findViewById(R.id.btn_remove_payment);

        List<String> names = new ArrayList<>();
        for (LookupDto l : paymentTypes) names.add(l != null ? l.name : "");

        ArrayAdapter<String> adapter = buildPaymentSpinnerAdapter(names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPayment.setAdapter(adapter);

        btnRemove.setOnClickListener(v -> {
            paymentContainer.removeView(row);
            persistCheckoutNow();
        });

        etAmount.addTextChangedListener(simpleWatcher(() -> persistCheckoutNow()));
        spPayment.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) { persistCheckoutNow(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        paymentContainer.addView(row);

        persistCheckoutNow(); // optional
    }



    private ArrayAdapter<String> buildPaymentSpinnerAdapter(List<String> names) {
        Typeface tf = ResourcesCompat.getFont(this, R.font.din_regular);

        return new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, names) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                try {
                    TextView tv = (TextView) v;
                    tv.setTypeface(tf);
                    tv.setTextSize(14);
                    tv.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                } catch (Exception ignored) {}
                return v;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                try {
                    TextView tv = (TextView) v;
                    tv.setTypeface(tf);
                    tv.setTextSize(14);
                    tv.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                    tv.setPadding(24, 24, 24, 24);
                } catch (Exception ignored) {}
                return v;
            }
        };
    }

    // جاهزة لمرحلة الريكوست
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

    /* ================= MODELS ================= */

    // نفس مفاتيح JSON اللي عندك: itemId/name/price/qty
    public static class CheckoutItem {
        public int itemId;
        public String name;
        public double price;
        public int qty;
    }

    public static class PaymentMethodDto {
        public int payment_type_id;
        public double amount;

        public PaymentMethodDto(int id, double amt) {
            this.payment_type_id = id;
            this.amount = amt;
        }
    }

    /* ================= ADAPTER (SIMPLE) ================= */

    // Adapter بسيط بدون Layout إضافي (عشان يشتغل فوراً)
    private class CheckoutItemsAdapter extends RecyclerView.Adapter<CheckoutItemsAdapter.VH> {

        interface Listener {
            void onCartChanged();
            void onDeleteClicked(int position);
        }

        private final List<co.highfive.petrolstation.pos.dto.PosDraftItemDto> data;
        private final Listener listener;

        CheckoutItemsAdapter(List<co.highfive.petrolstation.pos.dto.PosDraftItemDto> data, Listener listener) {
            this.data = data;
            this.listener = listener;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_checkout_cart, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {

            co.highfive.petrolstation.pos.dto.PosDraftItemDto it = data.get(position);

            h.txtName.setText(it.name != null ? it.name : "");
            h.txtUnitPrice.setText("" + formatMoney(it.price));

            h.etQty.setText(String.valueOf(Math.max(1, it.qty)));
            h.txtLineTotal.setText("" + formatMoney(it.price * Math.max(1, it.qty)));

            // Reset swipe state on bind (مهم جداً)
            if (openedSwipePosition == position) {
                h.fgContent.setTranslationX(swipeClampWidthPx);
            } else {
                h.fgContent.setTranslationX(0f);
            }


            // Delete button (يظهر بعد السوايب)
            h.btnDelete.setOnClickListener(v -> {
                int p = h.getAdapterPosition();
                if (p != RecyclerView.NO_POSITION) {
                    if (listener != null) listener.onDeleteClicked(p);
                }
            });

            h.btnPlus.setOnClickListener(v -> {
                int p = h.getAdapterPosition();
                if (p == RecyclerView.NO_POSITION) return;

                co.highfive.petrolstation.pos.dto.PosDraftItemDto item = data.get(p);
                item.qty = Math.max(1, item.qty) + 1;

                notifyItemChanged(p);
                if (listener != null) listener.onCartChanged();
                persistCartItemsNow();
            });

            h.btnMinus.setOnClickListener(v -> {
                int p = h.getAdapterPosition();
                if (p == RecyclerView.NO_POSITION) return;

                co.highfive.petrolstation.pos.dto.PosDraftItemDto item = data.get(p);
                int newQty = Math.max(1, item.qty) - 1;
                item.qty = Math.max(1, newQty);

                notifyItemChanged(p);
                if (listener != null) listener.onCartChanged();
                persistCartItemsNow();
            });

            // EditText qty: افتح الكيبورد مباشرة
            h.etQty.setOnClickListener(v -> {
                showKeyboard(h.etQty);
                h.etQty.post(() -> h.etQty.selectAll());
            });

            // عند تغيير الرقم يدويًا
            h.etQty.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    h.etQty.post(() -> h.etQty.selectAll());
                    return;
                }

                int p = h.getAdapterPosition();
                if (p == RecyclerView.NO_POSITION) return;

                int q = parseIntSafe(h.etQty.getText());
                if (q <= 0) q = 1;

                co.highfive.petrolstation.pos.dto.PosDraftItemDto item = data.get(p);
                item.qty = q;

                notifyItemChanged(p);
                if (listener != null) listener.onCartChanged();
            });


            // Optional: لو ضغط Done من الكيبورد
            h.etQty.setOnEditorActionListener((v, actionId, event) -> {
                int p = h.getAdapterPosition();
                if (p == RecyclerView.NO_POSITION) return false;

                int q = parseIntSafe(h.etQty.getText());
                if (q <= 0) q = 1;

                co.highfive.petrolstation.pos.dto.PosDraftItemDto item = data.get(p);
                item.qty = q;

                hideSoftKeyboard();
                h.etQty.clearFocus();

                notifyItemChanged(p);
                if (listener != null) listener.onCartChanged();
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return data == null ? 0 : data.size();
        }

        class VH extends RecyclerView.ViewHolder {

            View fgContent;
            ImageView btnDelete, btnPlus, btnMinus;
            TextView txtName, txtUnitPrice, txtLineTotal;
            EditText etQty;

            VH(@NonNull View itemView) {
                super(itemView);
                fgContent = itemView.findViewById(R.id.fgContent);

                btnDelete = itemView.findViewById(R.id.btnDelete);
                btnPlus = itemView.findViewById(R.id.btnPlus);
                btnMinus = itemView.findViewById(R.id.btnMinus);

                txtName = itemView.findViewById(R.id.txtName);
                txtUnitPrice = itemView.findViewById(R.id.txtUnitPrice);
                txtLineTotal = itemView.findViewById(R.id.txtLineTotal);

                etQty = itemView.findViewById(R.id.etQty);
            }
        }
    }

    private int parseIntSafe(CharSequence cs) {
        try {
            String s = cs == null ? "" : cs.toString().trim();
            if (s.isEmpty()) return 0;
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private void showKeyboard(View v) {
        try {
            v.requestFocus();
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(v, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        } catch (Exception ignore) {}

    }

    private void persistCartItemsNow() {
        try {
            if (invoiceId <= 0) return;

            // حوّل cartItems لنفس JSON
            String json = getGson().toJson(cartItems);

            // لازم يكون عندك updateInvoiceItemsJson في PosInvoiceDbHelper
            // إذا مش موجود، أعطيك كودها تحت
            posDb.updateInvoiceItemsJson(invoiceId, json);
        } catch (Exception ignore) {}
    }

    private void submitPosSale() {

        if (invoiceId <= 0) { toast("فاتورة غير صالحة"); return; }

        if (cartItems == null || cartItems.isEmpty()) {
            toast("أضف أصناف أولاً");
            return;
        }

        List<co.highfive.petrolstation.fuelsale.dto.PaymentMethodDto> pms = collectPaymentMethodsFromUI();
        if (pms == null || pms.isEmpty()) {
            toast("أضف طريقة دفع واحدة على الأقل");
            return;
        }

//        int accountId = this.accountId;
//        if (accountId <= 0) {
//            toast("حدد الحساب (account) أولاً");
//            return;
//        }

        String notes = binding.etNotes.getText() != null ? binding.etNotes.getText().toString().trim() : "";

        // ✅✅✅ NEW: Offline => Save locally like FuelSale
        if (!connectionAvailable) {
            saveOfflinePosSale(accountId, notes, pms);
            return;
        }

        ApiClient.ApiParams params = buildPosAddParams(accountId, notes, pms);

        showProgressHUD();

        Type type = new TypeToken<BaseResponse<co.highfive.petrolstation.customers.dto.InvoiceDto>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_POST,
                Endpoints.POS_ADD,
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
                        if (getAppData() != null) setting = getAppData().getSetting();
                        if (setting == null) {
                            toast(getString(R.string.general_error));
                            return;
                        }
                        printInvoice(setting, data);
                    }

                    @Override public void onError(co.highfive.petrolstation.network.ApiError error) {
                        hideProgressHUD();
                        toast(error != null ? error.message : getString(R.string.general_error));
                    }
                    @Override public void onUnauthorized(String rawJson) { hideProgressHUD(); logout(); }
                    @Override public void onNetworkError(String reason) { hideProgressHUD(); toast(R.string.no_internet); }
                    @Override public void onParseError(String rawJson, Exception e) { hideProgressHUD(); toast(getString(R.string.general_error)); }
                }
        );
    }

    private void saveOfflinePosSale(int accountId,
                                    @NonNull String notes,
                                    @NonNull List<co.highfive.petrolstation.fuelsale.dto.PaymentMethodDto> pms) {

        try {
            if (invoiceId <= 0) {
                toast("فاتورة غير صالحة");
                return;
            }

            // احسب المجموع
            double total = 0;
            for (co.highfive.petrolstation.pos.dto.PosDraftItemDto it : cartItems) {
                if (it == null) continue;
                total += (it.price * it.qty);
            }

            // نص ثابت للنوع (زي statement بالمحروقات)
            String statement = "فاتورة POS";
            String invoiceNoPlaceholder = "POS-OFF-" + System.currentTimeMillis();

            // ✅ جيب بيانات الفاتورة من DB (اسم الزبون.. الخ)
            co.highfive.petrolstation.pos.dto.PosActiveInvoice inv = posDb.getInvoice(invoiceId);

            String customerName = inv != null && inv.customerName != null ? inv.customerName : "";
            int customerId = inv != null ? inv.customerId : 0;
            String customerMobile = inv != null && inv.customerMobile != null ? inv.customerMobile : "";

            // ✅ ابني Payload بسيط واضح
            co.highfive.petrolstation.pos.dto.PosOfflineSaleRequest req =
                    new co.highfive.petrolstation.pos.dto.PosOfflineSaleRequest();

            req.accountId = accountId;
            req.customerId = customerId;
            req.offlineCustomerLocalId = (customerId < 0) ? Math.abs((long) customerId) : 0;
            req.customerName = customerName;
            req.notes = notes;
            req.items = new ArrayList<>(cartItems);
            req.paymentMethods = new ArrayList<>(pms);

            // ✅ نفس الريبو اللي بالمحروقات
            co.highfive.petrolstation.data.local.AppDatabase db =
                    co.highfive.petrolstation.data.local.DatabaseProvider.get(this);

            co.highfive.petrolstation.data.local.repo.InvoiceLocalRepository repo =
                    new co.highfive.petrolstation.data.local.repo.InvoiceLocalRepository(
                            this,
                            db,
                            getGson(),
                            apiClient
                    );

            java.util.concurrent.ExecutorService ex = java.util.concurrent.Executors.newSingleThreadExecutor();
            android.os.Handler main = new android.os.Handler(android.os.Looper.getMainLooper());

            showProgressHUD();

            final Double  allTotal = total;
            ex.execute(() -> {
                try {
                    repo.savePosSaleOffline(req, statement, allTotal, invoiceNoPlaceholder);

                    // ✅ بعد ما خزّنا بالـ queue: احذف الفاتورة من active invoices
                    try { posDb.deleteInvoice(invoiceId); } catch (Exception ignored) {}

                    main.post(() -> {
                        hideProgressHUD();
                        toast("تم حفظ فاتورة الـ POS محليًا وسيتم إرسالها عند توفر الإنترنت");

                        // ✅ رجّع ل PosActivity وكأنه Done (عشان يعمل reset ويحدث badge)
                        Intent i = new Intent();
                        i.putExtra("pos_invoice_id", invoiceId);
                        i.putExtra(EXTRA_CHECKOUT_DONE, true);
                        setResult(RESULT_OK, i);
                        finish();
                    });

                } catch (Exception e) {
                    main.post(() -> {
                        hideProgressHUD();
                        toast(getString(R.string.general_error));
                    });
                }
            });

        } catch (Exception e) {
            toast(getString(R.string.general_error));
        }
    }



    @NonNull
    private ApiClient.ApiParams buildPosAddParams(int accountId,
                                                  @NonNull String notes,
                                                  @NonNull List<co.highfive.petrolstation.fuelsale.dto.PaymentMethodDto> pms) {

        ApiClient.ApiParams p = new ApiClient.ApiParams();

        // ✅ arrays
        for (co.highfive.petrolstation.pos.dto.PosDraftItemDto it : cartItems) {
            if (it == null) continue;

            int itemId = it.itemId; // تأكد اسم الحقل عندك (ممكن it.id)
            double price = it.price;
            double qty = it.qty;

            if (itemId <= 0) continue;
            if (qty <= 0) qty = 1;

            p.add("item_id[]", itemId);
            p.add("price[]", price);
            p.add("count[]", qty);
        }

        // ✅ account_id
        p.add("account_id", accountId);
        p.add("customer_id", customerId);
        // ✅ notes
        p.add("notes", notes != null ? notes : "");

        // ✅ payment_methods json string
        p.add("payment_methods", getGson().toJson(pms));

        return p;
    }


    private void showSuccessDialog(String apiMessage) {

        if (isFinishing()) return;
        if (successDialog != null && successDialog.isShowing()) return;

        View v = LayoutInflater.from(this).inflate(R.layout.dialog_success, null, false);

        TextView txtMessage = v.findViewById(R.id.txtMessage);
        TextView btnOk = v.findViewById(R.id.btnOk);

        String msg = (apiMessage != null && !apiMessage.trim().isEmpty())
                ? apiMessage
                : getString(R.string.done);

        txtMessage.setText(msg);

        android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(this);
        b.setView(v);
        b.setCancelable(false);

        successDialog = b.create();
        if (successDialog.getWindow() != null) {
            successDialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        successDialog.show();

        btnOk.setOnClickListener(view -> {
            try { if (successDialog != null) successDialog.dismiss(); } catch (Exception ignored) {}

            try { posDb.deleteInvoice(invoiceId); } catch (Exception ignored) {}

            Intent i = new Intent();
            i.putExtra("pos_invoice_id", invoiceId);
            i.putExtra(EXTRA_CHECKOUT_DONE, true);
            setResult(RESULT_OK, i);
            finish();
        });
    }
    private OfflineCustomerEntity ensureOfflineCustomer(@NonNull String name, @NonNull String mobile) {
        AppDatabase db = DatabaseProvider.get(this);
        String norm = normalizeMobile(mobile);

        OfflineCustomerEntity existing = db.offlineCustomerDao().getByMobileNormalized(norm);
        long now = System.currentTimeMillis();

        if (existing != null) {
            // optional: حدث الاسم إذا كان فاضي
            if ((existing.name == null || existing.name.trim().isEmpty()) && name != null) {
                existing.name = name.trim();
                existing.updatedAtTs = now;
                db.offlineCustomerDao().update(existing);
            }
            return existing;
        }

        OfflineCustomerEntity e = new OfflineCustomerEntity();
        e.name = name != null ? name.trim() : "";
        e.mobile = mobile != null ? mobile.trim() : "";
        e.mobileNormalized = norm;
        e.syncStatus = 0;
        e.createdAtTs = now;
        e.updatedAtTs = now;

        long localId = db.offlineCustomerDao().insert(e);
        e.localId = localId;
        return e;
    }

    private String normalizeMobile(String m) {
        if (m == null) return "";
        return m.replace(" ", "").replace("-", "").trim();
    }

}
