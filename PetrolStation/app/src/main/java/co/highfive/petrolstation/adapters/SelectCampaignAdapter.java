package co.highfive.petrolstation.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import co.highfive.petrolstation.databinding.ItemSelectCampaignBinding;
import co.highfive.petrolstation.fuelsale.dto.FuelCampaignDto;

public class SelectCampaignAdapter extends RecyclerView.Adapter<SelectCampaignAdapter.VH> {

    public interface Listener {
        void onClick(FuelCampaignDto campaign);
    }

    private final ArrayList<FuelCampaignDto> items;
    private int selectedId;
    private final Listener listener;

    public SelectCampaignAdapter(ArrayList<FuelCampaignDto> items, int selectedId, Listener listener) {
        this.items = items != null ? items : new ArrayList<>();
        this.selectedId = selectedId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSelectCampaignBinding b = ItemSelectCampaignBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        FuelCampaignDto c = items.get(position);

        String name = (c != null && c.name != null) ? c.name : "";
        h.b.title.setText(name);

        boolean isSelected = (c != null && c.id == selectedId);
        h.b.ivCheck.setVisibility(isSelected ? android.view.View.VISIBLE : android.view.View.GONE);

        h.b.getRoot().setOnClickListener(v -> {
            if (c == null) return;
            selectedId = c.id;
            notifyDataSetChanged();
            if (listener != null) listener.onClick(c);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ItemSelectCampaignBinding b;
        VH(ItemSelectCampaignBinding b) { super(b.getRoot()); this.b = b; }
    }
}
