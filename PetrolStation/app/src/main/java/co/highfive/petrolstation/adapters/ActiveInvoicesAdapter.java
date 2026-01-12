package co.highfive.petrolstation.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import co.highfive.petrolstation.databinding.ItemActiveInvoiceBinding;
import co.highfive.petrolstation.fuelsale.dto.FuelSaleDraft;

public class ActiveInvoicesAdapter extends RecyclerView.Adapter<ActiveInvoicesAdapter.VH> {

    public interface Listener {
        void onClick(FuelSaleDraft draft);
    }

    private final List<FuelSaleDraft> items;
    private final Listener listener;

    public ActiveInvoicesAdapter(List<FuelSaleDraft> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemActiveInvoiceBinding b = ItemActiveInvoiceBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new VH(b);
    }
    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        FuelSaleDraft d = items.get(position);

        String name = d != null && d.customer_name != null ? d.customer_name : "";
        String mobile = d != null && d.customer_mobile != null ? d.customer_mobile : "";
        String total = d != null && d.total_text != null ? d.total_text : "";

        h.b.tvName.setText(name);
        h.b.tvMobile.setText(mobile);
        h.b.tvTotal.setText(total);

        h.b.card.setOnClickListener(v -> {
            if (listener != null) listener.onClick(d);
        });

        h.b.btnOpen.setOnClickListener(v -> {
            if (listener != null) listener.onClick(d);
        });
    }


    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        ItemActiveInvoiceBinding b;
        VH(ItemActiveInvoiceBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
