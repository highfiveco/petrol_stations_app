package co.highfive.petrolstation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import co.highfive.petrolstation.customers.dto.InvoiceDto;
import co.highfive.petrolstation.databinding.CustomerInvoicesItemBinding;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;

public class CustomerInvoicesAdapter extends RecyclerView.Adapter<CustomerInvoicesAdapter.VH> {

    public interface Listener {
        void onView(InvoiceDto invoice);
        void onPrint(InvoiceDto invoice);
        void onSend(InvoiceDto invoice);
    }

    private final List<InvoiceDto> items;
    private final Listener listener;

    private boolean hideCustomerNameInItem = false;
    BaseActivity baseActivity;
    public CustomerInvoicesAdapter(BaseActivity baseActivity,List<InvoiceDto> items, Listener listener) {
        this.baseActivity = baseActivity;
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

//        h.binding.txtInvoiceNumber.setText(safe(inv.invoice_no));
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

        boolean isPending = inv.is_offline && (inv.sync_status == 0);
        boolean isFailed  = inv.is_offline && (inv.sync_status == 2);

        String no = safe(inv.invoice_no);
//        if (inv.is_offline) {
//            if (inv.sync_status == 0) no += "  •  Pending";
//            else if (inv.sync_status == 2) no += "  •  Failed";
//        }
        h.binding.txtInvoiceNumber.setText(no);

        // (اختياري) أخفي زر print للـ pending

        boolean canPrint = !inv.is_offline || (inv.id > 0 && inv.sync_status == 1);
        h.binding.printInvoice.setEnabled(canPrint);
        h.binding.printInvoice.setAlpha(canPrint ? 1f : 0.4f);

        if (inv.is_offline) {
            h.binding.statusLayout.setVisibility(View.VISIBLE);

            if (inv.sync_status == 0) {
                h.binding.txtStatus.setText("قيد الارسال");
            } else if (inv.sync_status == 2) {
                h.binding.txtStatus.setText("فشل الارسال");
            } else {
                h.binding.txtStatus.setText("اوف لاين");
            }
        } else {
            h.binding.statusLayout.setVisibility(View.GONE);
        }

        h.binding.buttonSend.setOnClickListener(v -> {
            if (listener != null) listener.onSend(inv);
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
