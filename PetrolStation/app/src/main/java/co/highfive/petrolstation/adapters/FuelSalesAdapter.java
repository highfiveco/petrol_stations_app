package co.highfive.petrolstation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import co.highfive.petrolstation.databinding.FuelSaleItemBinding;
import co.highfive.petrolstation.customers.dto.FuelSaleDto;
import co.highfive.petrolstation.customers.dto.FuelSaleDetailDto;

public class FuelSalesAdapter extends RecyclerView.Adapter<FuelSalesAdapter.VH> {

    public interface Listener {
        void onPrint(FuelSaleDto sale);
        void onDelete(FuelSaleDto sale);
        void onOpen(FuelSaleDto sale);
    }

    private final List<FuelSaleDto> items;
    private final Listener listener;

    public FuelSalesAdapter(List<FuelSaleDto> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<FuelSaleDto> more) {
        int start = items.size();
        items.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FuelSaleItemBinding b = FuelSaleItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        FuelSaleDto s = items.get(position);

        h.binding.itemName.setText(safe(s.statement));

        String campaignName = "-";
        if (s.campaign != null && s.campaign.name != null && !s.campaign.name.trim().isEmpty()) {
            campaignName = s.campaign.name;
        }
        h.binding.campaignName.setText(campaignName);

        h.binding.date.setText(safe(s.date));
        h.binding.total.setText(String.valueOf(s.total));
        h.binding.price.setText(String.valueOf(s.details.get(0).price));

        // count: مجموع counts داخل details
        h.binding.count.setText(String.valueOf(s.details.get(0).count));

        h.binding.printLayout.setOnClickListener(v -> {
            if (listener != null) listener.onPrint(s);
        });

        h.binding.deleteLayout.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(s);
        });

        // لو بدك فتح تفاصيل عند الضغط على الكارد
//        h.binding.getRoot().setOnClickListener(v -> {
//            if (listener != null) listener.onOpen(s);
//        });
    }



    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final FuelSaleItemBinding binding;
        VH(FuelSaleItemBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
