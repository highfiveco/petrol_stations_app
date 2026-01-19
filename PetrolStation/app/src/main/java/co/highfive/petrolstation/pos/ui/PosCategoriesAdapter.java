package co.highfive.petrolstation.pos.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.customers_settings.dto.LookupDto;
import co.highfive.petrolstation.databinding.RowPosCategoryBinding;

public class PosCategoriesAdapter extends RecyclerView.Adapter<PosCategoriesAdapter.VH> {

    public interface Listener {
        void onCategoryClicked(@NonNull LookupDto category);
    }

    private final Listener listener;
    private final List<LookupDto> list = new ArrayList<>();
    private int selectedId = 0;

    public PosCategoriesAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<LookupDto> data) {
        list.clear();
        if (data != null) list.addAll(data);
        notifyDataSetChanged();
    }

    public void setSelectedId(int id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowPosCategoryBinding b = RowPosCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        LookupDto c = list.get(position);
        boolean isSelected = c.id == selectedId;

        h.b.txt.setText(c.name == null ? "" : c.name);
        h.b.getRoot().setSelected(isSelected);

        h.b.getRoot().setOnClickListener(v -> {
            if (listener != null) listener.onCategoryClicked(c);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        RowPosCategoryBinding b;
        VH(RowPosCategoryBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
