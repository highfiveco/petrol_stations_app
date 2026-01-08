package co.highfive.petrolstation.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.databinding.CustomerLogViewLayoutBinding;
import co.highfive.petrolstation.models.CustomerLog;

public class CustomerLogAdapter extends RecyclerView.Adapter<CustomerLogAdapter.VH> {

    private final List<CustomerLog> items;

    public CustomerLogAdapter(List<CustomerLog> items) {
        this.items = items;
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<CustomerLog> more) {
        int start = items.size();
        items.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CustomerLogViewLayoutBinding b = CustomerLogViewLayoutBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        CustomerLog log = items.get(position);
        if (log == null) return;

        h.binding.feild.setText(safe(log.getCol_name()));
        h.binding.previousValue.setText(
                h.binding.getRoot().getContext().getString(R.string.previous_value) + "\n" + safe(log.getOld_value())
        );
        h.binding.newValue.setText(
                h.binding.getRoot().getContext().getString(R.string.new_value) + "\n" + safe(log.getNew_value())
        );
        h.binding.user.setText(safe(log.getFull_name()));
        h.binding.date.setText(safe(log.getInsert_date()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final CustomerLogViewLayoutBinding binding;
        VH(CustomerLogViewLayoutBinding b) {
            super(b.getRoot());
            this.binding = b;
        }
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
