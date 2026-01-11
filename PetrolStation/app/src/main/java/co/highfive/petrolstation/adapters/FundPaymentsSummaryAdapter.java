package co.highfive.petrolstation.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import co.highfive.petrolstation.databinding.ItemFundPaymentSummaryBinding;
import co.highfive.petrolstation.models.FundPaymentSummary;

public class FundPaymentsSummaryAdapter extends RecyclerView.Adapter<FundPaymentsSummaryAdapter.Holder> {

    private final ArrayList<FundPaymentSummary> items;

    public FundPaymentsSummaryAdapter(ArrayList<FundPaymentSummary> items) {
        this.items = items == null ? new ArrayList<>() : items;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFundPaymentSummaryBinding b = ItemFundPaymentSummaryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new Holder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        FundPaymentSummary item = items.get(position);
        h.binding.txtPayment.setText(item.payment == null ? "" : item.payment);
        h.binding.txtAmount.setText(item.net_balance == null ? "0" : item.net_balance);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        ItemFundPaymentSummaryBinding binding;
        Holder(ItemFundPaymentSummaryBinding b) { super(b.getRoot()); binding = b; }
    }
}
