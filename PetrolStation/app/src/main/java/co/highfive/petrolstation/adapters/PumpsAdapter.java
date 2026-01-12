package co.highfive.petrolstation.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import co.highfive.petrolstation.databinding.ItemPumpBinding;
import co.highfive.petrolstation.fuelsale.dto.PumpDto;

public class PumpsAdapter extends RecyclerView.Adapter<PumpsAdapter.VH> {

    public interface OnPumpSelect {
        void onSelect(PumpDto pump);
    }

    private List<PumpDto> list;
    private final OnPumpSelect listener;

    private int selectedId = 0;

    public PumpsAdapter(List<PumpDto> list, OnPumpSelect listener) {
        this.list = list;
        this.listener = listener;
    }

    public void updateData(List<PumpDto> data) {
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
    public PumpDto getSelected() {
        return selectedId > 0 ? findById(selectedId) : null;
    }

    private int indexOfId(int id) {
        if (list == null || id <= 0) return -1;
        for (int i = 0; i < list.size(); i++) {
            PumpDto it = list.get(i);
            if (it != null && it.id == id) return i;
        }
        return -1;
    }

    @Nullable
    private PumpDto findById(int id) {
        if (list == null) return null;
        for (PumpDto it : list) {
            if (it != null && it.id == id) return it;
        }
        return null;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemPumpBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int p) {
        PumpDto pump = list.get(p);

        h.binding.name.setText(pump != null && pump.name != null ? pump.name : "");

        // icon
        try {
            if (pump != null && pump.icon != null && !pump.icon.trim().isEmpty()) {
                com.bumptech.glide.Glide.with(h.binding.getRoot().getContext())
                        .load(pump.icon)
                        .into(h.binding.icon);
            } else {
                h.binding.icon.setImageDrawable(null);
            }
        } catch (Exception ignored) {}

        boolean isSelected = (pump != null && pump.id == selectedId);
        h.binding.getRoot().setSelected(isSelected);

        h.binding.getRoot().setOnClickListener(v -> {
            if (pump == null) return;

            int oldPos = indexOfId(selectedId);
            selectedId = pump.id;

            if (oldPos >= 0) notifyItemChanged(oldPos);
            notifyItemChanged(p);

            if (listener != null) listener.onSelect(pump);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        ItemPumpBinding binding;
        VH(ItemPumpBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }
}
