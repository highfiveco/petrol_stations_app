package co.highfive.petrolstation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.databinding.ItemSelectCustomerBinding;
import co.highfive.petrolstation.fuelsale.dto.FuelCustomerDto;

public class SelectSearchCustomerAdapter extends RecyclerView.Adapter<SelectSearchCustomerAdapter.VH> {

    public interface Listener {
        void onClick(FuelCustomerDto dto);
    }

    private List<FuelCustomerDto> list = new ArrayList<>();
    private int selectedId = 0;
    private final Listener listener;

    public SelectSearchCustomerAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<FuelCustomerDto> items) {
        list = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setSelectedId(int id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    public FuelCustomerDto getSelected() {
        if (list == null) return null;
        for (FuelCustomerDto c : list) {
            if (c != null && c.id == selectedId) return c;
        }
        return null;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemSelectCustomerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        FuelCustomerDto c = list.get(position);

        String name = (c != null && c.name != null) ? c.name : "";
        String campaign = (c != null && c.campaign_name != null) ? c.campaign_name : "";
        String remaining = (c != null) ? String.valueOf(c.remaining_amount) : "";

        h.binding.tvName.setText(name);
        h.binding.tvCampaign.setText(campaign);
        h.binding.tvRemaining.setText("المتبقي: " + remaining);

        boolean isSel = c != null && c.id == selectedId;

        // ✅ show/hide check icon
        h.binding.icSelected.setVisibility(isSel ? View.VISIBLE : View.GONE);

        // Optional stronger highlight
        h.binding.root.setCardElevation(isSel ? 6f : 2f);
        h.binding.root.setAlpha(isSel ? 1f : 0.92f);

        h.binding.getRoot().setOnClickListener(v -> {
            if (c == null) return;
            selectedId = c.id;
            notifyDataSetChanged();
            if (listener != null) listener.onClick(c);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        ItemSelectCustomerBinding binding;

        VH(ItemSelectCustomerBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }
}
