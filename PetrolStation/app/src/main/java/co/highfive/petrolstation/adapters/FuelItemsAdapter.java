package co.highfive.petrolstation.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import co.highfive.petrolstation.databinding.ItemFuelBinding;
import co.highfive.petrolstation.fuelsale.dto.FuelItemDto;

public class FuelItemsAdapter extends RecyclerView.Adapter<FuelItemsAdapter.VH> {

    public interface OnItemSelect {
        void onSelect(FuelItemDto item);
    }

    private List<FuelItemDto> list;
    private final OnItemSelect listener;

    private int selectedId = 0;

    public FuelItemsAdapter(List<FuelItemDto> list, OnItemSelect listener) {
        this.list = list;
        this.listener = listener;
    }

    public void updateData(List<FuelItemDto> data) {
        this.list = data;

        // keep selectedId only if still exists
        if (selectedId > 0 && findById(selectedId) == null) {
            selectedId = 0;
        }

        notifyDataSetChanged();
    }

    public void setSelectedId(int id) {
        if (id <= 0) {
            clearSelection();
            return;
        }

        if (findById(id) == null) {
            clearSelection();
            return;
        }

        if (selectedId == id) return;

        int oldPos = indexOfId(selectedId);
        selectedId = id;
        int newPos = indexOfId(selectedId);

        if (oldPos >= 0) notifyItemChanged(oldPos);
        if (newPos >= 0) notifyItemChanged(newPos);
    }

    public void clearSelection() {
        int oldPos = indexOfId(selectedId);
        selectedId = 0;
        if (oldPos >= 0) notifyItemChanged(oldPos);
    }

    public int getSelectedId() {
        return selectedId;
    }

    @Nullable
    public FuelItemDto getSelected() {
        return selectedId > 0 ? findById(selectedId) : null;
    }

    private int indexOfId(int id) {
        if (list == null || id <= 0) return -1;
        for (int i = 0; i < list.size(); i++) {
            FuelItemDto it = list.get(i);
            if (it != null && it.id == id) return i;
        }
        return -1;
    }

    @Nullable
    private FuelItemDto findById(int id) {
        if (list == null) return null;
        for (FuelItemDto it : list) {
            if (it != null && it.id == id) return it;
        }
        return null;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemFuelBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int p) {
        FuelItemDto item = list.get(p);

        h.binding.name.setText(item != null && item.name != null ? item.name : "");
        h.binding.price.setText(item != null && item.price != null ? String.valueOf(item.price) : "");

        // icon
        try {
            if (item != null && item.icon != null && !item.icon.trim().isEmpty()) {
                com.bumptech.glide.Glide.with(h.binding.getRoot().getContext())
                        .load(item.icon)
                        .into(h.binding.icon);
            } else {
                h.binding.icon.setImageDrawable(null);
            }
        } catch (Exception ignored) {}

        boolean isSelected = (item != null && item.id == selectedId);
        h.binding.getRoot().setSelected(isSelected);

        h.binding.getRoot().setOnClickListener(v -> {
            if (item == null) return;

            int oldPos = indexOfId(selectedId);
            selectedId = item.id;

            if (oldPos >= 0) notifyItemChanged(oldPos);
            notifyItemChanged(p);

            if (listener != null) listener.onSelect(item);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        ItemFuelBinding binding;
        VH(ItemFuelBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }
}
