package co.highfive.petrolstation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import co.highfive.petrolstation.databinding.CustomerFinancialAccountViewLayoutBinding;
import co.highfive.petrolstation.models.Setting;
import co.highfive.petrolstation.models.Transactions;

public class CustomerFinancialAccountAdapter extends RecyclerView.Adapter<CustomerFinancialAccountAdapter.VH> {

    public interface Listener {
        void onDeleteClicked(Transactions tx);
        void onPrintClicked(Transactions tx);
        void onAddReturnClicked(Transactions tx);
    }

    private final List<Transactions> items;
    private final Listener listener;

    private int deleteFinancial = 0;
    private String accountId = "";
    private Setting setting;

    public CustomerFinancialAccountAdapter(List<Transactions> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setDeleteFinancial(int v) { this.deleteFinancial = v; }
    public void setAccountId(String v) { this.accountId = v == null ? "" : v; }
    public void setSetting(Setting v) { this.setting = v; }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Transactions> more) {
        int start = items.size();
        items.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CustomerFinancialAccountViewLayoutBinding b =
                CustomerFinancialAccountViewLayoutBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                );
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Transactions tx = items.get(position);

        h.binding.statement.setText(safe(tx.getType_statement()));
        h.binding.transactionDate.setText(safe(tx.getCreated_at()));
        h.binding.amount.setText(tx.getAmount() == null ? "" : String.valueOf(tx.getAmount()));
        h.binding.currency.setText(safe(tx.getCurrency_name()));

        h.binding.deleteLayout.setVisibility(deleteFinancial == 1 ? View.VISIBLE : View.GONE);

        boolean canPrint = "1".equals(safe(tx.getIs_print())) || "true".equalsIgnoreCase(safe(tx.getIs_print()));
        h.binding.printLayout.setVisibility(canPrint ? View.VISIBLE : View.GONE);

        h.binding.deleteLayout.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClicked(tx);
        });

        h.binding.printLayout.setOnClickListener(v -> {
            if (listener != null) listener.onPrintClicked(tx);
        });

        h.binding.addReturnLayout.setOnClickListener(v -> {
            if (listener != null) listener.onAddReturnClicked(tx);
        });

        boolean showPrintContainer = (deleteFinancial == 1) || canPrint;
        h.binding.printContainer.setVisibility(showPrintContainer ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final CustomerFinancialAccountViewLayoutBinding binding;
        VH(CustomerFinancialAccountViewLayoutBinding b) {
            super(b.getRoot());
            this.binding = b;
        }
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
