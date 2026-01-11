package co.highfive.petrolstation.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;

import co.highfive.petrolstation.databinding.ItemMultiSelectDropdownBinding;
import co.highfive.petrolstation.models.Currency;

public class MultiSelectDropDownAdapter extends RecyclerView.Adapter<MultiSelectDropDownAdapter.Holder> {

    public interface OnToggle {
        void onToggle(Currency item, boolean isSelected);
    }

    private final ArrayList<Currency> items;
    private final HashSet<String> selectedIds;
    private final OnToggle onToggle;

    public MultiSelectDropDownAdapter(ArrayList<Currency> items, HashSet<String> selectedIds, OnToggle onToggle) {
        this.items = items == null ? new ArrayList<>() : items;
        this.selectedIds = selectedIds == null ? new HashSet<>() : selectedIds;
        this.onToggle = onToggle;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMultiSelectDropdownBinding b = ItemMultiSelectDropdownBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new Holder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Currency item = items.get(position);
        String id = item.getId();

        h.binding.title.setText(item.getName());

        boolean checked = selectedIds.contains(id);

        // Important: detach listener before setting checked to avoid triggering it while binding
        h.binding.checkbox.setOnCheckedChangeListener(null);
        h.binding.checkbox.setChecked(checked);

        // When user changes checkbox state (by clicking checkbox OR programmatically via toggle())
        h.binding.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) selectedIds.add(id);
            else selectedIds.remove(id);

            if (onToggle != null) onToggle.onToggle(item, isChecked);
        });

        // Root click toggles checkbox
        h.binding.getRoot().setOnClickListener(v -> h.binding.checkbox.toggle());

        // Optional: title click toggles too (if you want)
        h.binding.title.setOnClickListener(v -> h.binding.checkbox.toggle());
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        ItemMultiSelectDropdownBinding binding;
        Holder(ItemMultiSelectDropdownBinding b) { super(b.getRoot()); binding = b; }
    }
}
