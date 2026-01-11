package co.highfive.petrolstation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import co.highfive.petrolstation.customers.dto.InvoiceDto;
import co.highfive.petrolstation.databinding.CustomerInvoicesItemBinding;

public class CustomerInvoicesAdapter extends RecyclerView.Adapter<CustomerInvoicesAdapter.VH> {

    public interface Listener {
        void onView(InvoiceDto invoice);
        void onPrint(InvoiceDto invoice);
    }

    private final List<InvoiceDto> items;
    private final Listener listener;

    private boolean hideCustomerNameInItem = false;

    public CustomerInvoicesAdapter(List<InvoiceDto> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setHideCustomerNameInItem(boolean hide) {
        this.hideCustomerNameInItem = hide;
        notifyDataSetChanged();
    }

    public void setItems(List<InvoiceDto> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    public void addItems(List<InvoiceDto> list) {
        if (list == null || list.isEmpty()) return;
        int start = items.size();
        items.addAll(list);
        notifyItemRangeInserted(start, list.size());
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CustomerInvoicesItemBinding b = CustomerInvoicesItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        InvoiceDto inv = items.get(position);

        h.binding.txtInvoiceNumber.setText(safe(inv.invoice_no));
        h.binding.txtInvoiceDate.setText(safe(inv.date));
        h.binding.txtInvoiceAmount.setText(formatNumber(inv.total));

        if (hideCustomerNameInItem) {
            h.binding.customerNameLayout.setVisibility(View.GONE);
        } else {
            h.binding.customerNameLayout.setVisibility(View.VISIBLE);
            String name = (inv.account != null) ? safe(inv.account.getAccount_name()) : "";
            h.binding.txtCustomerName.setText(name.isEmpty() ? "-" : name);
        }

        h.binding.viewInvoice.setOnClickListener(v -> {
            if (listener != null) listener.onView(inv);
        });

        h.binding.printInvoice.setOnClickListener(v -> {
            if (listener != null) listener.onPrint(inv);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final CustomerInvoicesItemBinding binding;
        VH(CustomerInvoicesItemBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private static String formatNumber(Double v) {
        if (v == null) return "";
        if (Math.abs(v - Math.round(v)) < 0.000001) return String.valueOf((long) Math.round(v));
        return String.valueOf(v);
    }
}
