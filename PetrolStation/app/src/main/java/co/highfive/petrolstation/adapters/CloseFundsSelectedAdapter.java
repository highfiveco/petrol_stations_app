package co.highfive.petrolstation.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import co.highfive.petrolstation.databinding.ItemCloseFundAmountBinding;
import co.highfive.petrolstation.models.SelectedCloseFundItem;

public class CloseFundsSelectedAdapter extends RecyclerView.Adapter<CloseFundsSelectedAdapter.Holder> {

    public interface OnDelete {
        void onDelete(SelectedCloseFundItem item);
    }

    private final ArrayList<SelectedCloseFundItem> items;
    private final OnDelete onDelete;

    public CloseFundsSelectedAdapter(ArrayList<SelectedCloseFundItem> items, OnDelete onDelete) {
        this.items = items;
        this.onDelete = onDelete;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCloseFundAmountBinding b = ItemCloseFundAmountBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new Holder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        SelectedCloseFundItem item = items.get(position);

        h.binding.txtFundName.setText(item.fundName);

        // Avoid duplicated watchers
        if (h.watcher != null) {
            h.binding.edtAmount.removeTextChangedListener(h.watcher);
        }

        h.binding.edtAmount.setText(item.amount == null ? "" : item.amount);

        h.watcher = new SimpleTextWatcher(s -> item.amount = s);
        h.binding.edtAmount.addTextChangedListener(h.watcher);

        h.binding.btnDelete.setOnClickListener(v -> {
            if (onDelete != null) onDelete.onDelete(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        ItemCloseFundAmountBinding binding;
        TextWatcher watcher;

        Holder(ItemCloseFundAmountBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }

    // Small watcher helper
    static class SimpleTextWatcher implements TextWatcher {
        interface OnChanged { void onChanged(String s); }
        private final OnChanged cb;

        SimpleTextWatcher(OnChanged cb) { this.cb = cb; }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (cb != null) cb.onChanged(s == null ? "" : s.toString().trim());
        }
        @Override public void afterTextChanged(Editable s) {}
    }
}
