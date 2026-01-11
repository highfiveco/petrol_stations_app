package co.highfive.petrolstation.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.customers.dto.InvoiceDetailDto;
import co.highfive.petrolstation.databinding.ItemInvoiceDetailRowBinding;

public class InvoiceDetailsItemsAdapter extends RecyclerView.Adapter<InvoiceDetailsItemsAdapter.VH> {

    private final ArrayList<InvoiceDetailDto> items = new ArrayList<>();

    public void setItems(List<InvoiceDetailDto> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemInvoiceDetailRowBinding b = ItemInvoiceDetailRowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        InvoiceDetailDto d = items.get(position);

        String name = "-";
        try {
            if (d != null && d.item != null && d.item.name != null && !d.item.name.trim().isEmpty()) {
                name = d.item.name.trim();
            }
        } catch (Exception ignored) {}

        double qty = 0;
        try { qty = d != null && d.count != null ? d.count : 0; } catch (Exception ignored) {}

        h.binding.tvPrice.setText(""+d.price);
        h.binding.tvQty.setText(name+"x" + formatNumber(qty));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemInvoiceDetailRowBinding binding;
        VH(ItemInvoiceDetailRowBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }

    private static String formatNumber(double v) {
        if (Math.abs(v - Math.round(v)) < 0.000001) return String.valueOf((long) Math.round(v));
        return String.valueOf(v);
    }
}
