package co.highfive.petrolstation.pos.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.databinding.RowPosActiveInvoiceBinding;
import co.highfive.petrolstation.pos.dto.PosActiveInvoice;
import co.highfive.petrolstation.pos.dto.PosDraftItemDto;

public class SimplePosInvoicesAdapter extends RecyclerView.Adapter<SimplePosInvoicesAdapter.VH> {

    public interface Listener {
        void onInvoiceClicked(long invoiceId);
    }

    private final List<PosActiveInvoice> list = new ArrayList<>();
    private final Listener listener;

    private final Gson gson = new Gson();
    private final DecimalFormat moneyFmt = new DecimalFormat("0.##"); // إذا بدك كسور

    public SimplePosInvoicesAdapter(List<PosActiveInvoice> data, Listener listener) {
        if (data != null) list.addAll(data);
        this.listener = listener;
        setHasStableIds(true);
    }

    public void submitList(List<PosActiveInvoice> data) {
        list.clear();
        if (data != null) list.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        PosActiveInvoice inv = list.get(position);
        return inv != null ? inv.id : super.getItemId(position);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowPosActiveInvoiceBinding b = RowPosActiveInvoiceBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PosActiveInvoice inv = list.get(position);
        if (inv == null) return;

        // ===== Customer name =====
        String customer = inv.customerName != null ? inv.customerName.trim() : "";
        if (customer.isEmpty()) customer = "-";
        h.b.tvName.setText("الزبون: " + customer);

        // ===== Mobile (اختياري) =====
        // إذا PosActiveInvoice فيه customerMobile استخدمه، غير هيك اخفِه
        String mobile = "";
        try {
            // لو عندك inv.customerMobile
            // mobile = inv.customerMobile != null ? inv.customerMobile.trim() : "";
            // إذا ما عندك الحقل، اتركه فاضي وسيتم إخفاء التكست
        } catch (Exception ignore) {}

        if (mobile.isEmpty()) {
            h.b.tvMobile.setText("");
            h.b.tvMobile.setVisibility(android.view.View.GONE);
        } else {
            h.b.tvMobile.setVisibility(android.view.View.VISIBLE);
            h.b.tvMobile.setText(mobile);
        }

        // ===== Total =====
        double total = calcTotalFromJson(inv.itemsJson);
        h.b.tvTotal.setText(((int) total) + " شيكل");
        // إذا بدك بدون كسور:
        // h.b.tvTotal.setText(((int) total) + " شيكل");


        // ===== Click =====
        h.b.getRoot().setOnClickListener(v -> {
            if (listener != null) listener.onInvoiceClicked(inv.id);
        });

        // زر الفتح (المربع البنفسجي)
        h.b.btnOpen.setOnClickListener(v -> {
            if (listener != null) listener.onInvoiceClicked(inv.id);
        });
    }

    private double calcTotalFromJson(String itemsJson) {
        if (itemsJson == null || itemsJson.trim().isEmpty()) return 0;

        double total = 0;

        try {
            com.google.gson.JsonArray arr = com.google.gson.JsonParser
                    .parseString(itemsJson)
                    .getAsJsonArray();

            for (int i = 0; i < arr.size(); i++) {
                com.google.gson.JsonObject o = arr.get(i).getAsJsonObject();

                int qty = getIntAny(o, "qty", "quantity", "q");
                double price = getDoubleAny(o, "price", "unitPrice", "unit_price", "item_price");

                if (qty > 0 && price > 0) {
                    total += (qty * price);
                }
            }
        } catch (Exception ignore) {}

        return total;
    }

    private int getIntAny(com.google.gson.JsonObject o, String... keys) {
        for (String k : keys) {
            if (o.has(k) && !o.get(k).isJsonNull()) {
                try { return o.get(k).getAsInt(); }
                catch (Exception e) {
                    try { return Integer.parseInt(o.get(k).getAsString()); }
                    catch (Exception ignore) {}
                }
            }
        }
        return 0;
    }

    private double getDoubleAny(com.google.gson.JsonObject o, String... keys) {
        for (String k : keys) {
            if (o.has(k) && !o.get(k).isJsonNull()) {
                try { return o.get(k).getAsDouble(); }
                catch (Exception e) {
                    try { return Double.parseDouble(o.get(k).getAsString()); }
                    catch (Exception ignore) {}
                }
            }
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        RowPosActiveInvoiceBinding b;
        VH(RowPosActiveInvoiceBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
