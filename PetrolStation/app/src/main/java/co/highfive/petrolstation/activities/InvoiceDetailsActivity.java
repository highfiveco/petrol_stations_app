package co.highfive.petrolstation.activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.adapters.InvoiceDetailsItemsAdapter;
import co.highfive.petrolstation.customers.dto.InvoiceDto;
import co.highfive.petrolstation.databinding.ActivityInvoiceDetailsBinding;
import co.highfive.petrolstation.databinding.RowInvoiceSummaryBinding;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.models.Setting;

public class InvoiceDetailsActivity extends BaseActivity {

    private ActivityInvoiceDetailsBinding binding;

    private InvoiceDto invoice;
    private InvoiceDetailsItemsAdapter itemsAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityInvoiceDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI(binding.root);

        readInvoiceFromExtras();
        initHeader();
        initRecycler();
        bindData();
        initClicks();
    }

    private void readInvoiceFromExtras() {
        String raw = "";
        try {
            Bundle b = getIntent() != null ? getIntent().getExtras() : null;
            if (b != null) raw = safe(b.getString("raw_invoice_json"));
        } catch (Exception ignored) {}

        if (raw.trim().isEmpty()) {
            toast(getString(R.string.general_error));
            finish();
            return;
        }

        try {
            Type type = new TypeToken<InvoiceDto>() {}.getType();
            invoice = getGson().fromJson(raw, type);
        } catch (Exception e) {
            errorLogger("InvoiceDetailsParse", e.getMessage() == null ? "null" : e.getMessage());
            toast(getString(R.string.general_error));
            finish();
        }
    }

    private void initHeader() {
    }

    private void initRecycler() {
        itemsAdapter = new InvoiceDetailsItemsAdapter();
        binding.recyclerItems.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerItems.setAdapter(itemsAdapter);
    }

    private void bindData() {
        if (invoice == null) return;

        // Customer info
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

//        double remaining = total - discount - paid;
//        if (remaining < 0) remaining = 0; // optional safety

        binding.tvTotalBig.setText(formatNumber(total));

        setSummaryRow(binding.rowDiscount, getString(R.string.discount), formatNumber(discount));
        setSummaryRow(binding.rowPaid, getString(R.string.paid_amount), formatNumber(paid));
//        setSummaryRow(binding.rowAmountToPay, getString(R.string.invoice_total), formatNumber(total)); // أو خليها amount_to_pay إذا بتحب
        setSummaryRow(binding.rowRemaining, getString(R.string.invoice_remaining_amount), formatNumber(remain));
    }

    private void initClicks() {
        binding.icHome.setOnClickListener(v ->
                moveToActivity(getApplicationContext(), MainActivity.class, null, false, true)
        );
        binding.icBack.setOnClickListener(v -> finish());


        binding.btnPrint.setOnClickListener(v -> {
            if (invoice == null) return;

            Setting setting = null;
            try {
                // غيّر المفتاح إذا عندك مختلف
                setting = getAppData().getSetting();
            } catch (Exception ignored) {}

            if (setting == null) {
                toast(getString(R.string.general_error));
                return;
            }

            // ✅ دالة الطباعة اللي عملناها قبل
            printInvoice(setting, invoice);
        });

        binding.tvPhone.setOnClickListener(v -> {
            String phone = safe(binding.tvPhone.getText() != null ? binding.tvPhone.getText().toString() : "");
            if (!phone.trim().isEmpty()) call(phone);
        });

        binding.btnPrint.setOnClickListener(v -> {
            if (invoice == null) return;

            Setting setting = null;
            try {
                // نفس اللي تستخدمه بباقي الشاشات
                setting = getAppData().getSetting();
            } catch (Exception ignored) {}

            if (setting == null) {
                toast(getString(R.string.general_error));
                return;
            }

            // ✅ نفّذ الطباعة
            printInvoice(setting, invoice);
        });

    }

    // Helper: set row label/value by order from includes
    private void setSummaryRow(RowInvoiceSummaryBinding row, String label, String value) {
        if (row == null) return;
        row.label.setText(label);
        row.val.setText(value);
    }

}
