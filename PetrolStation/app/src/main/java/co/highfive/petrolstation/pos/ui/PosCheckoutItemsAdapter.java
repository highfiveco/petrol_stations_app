package co.highfive.petrolstation.pos.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.databinding.RowPosCheckoutItemBinding;
import co.highfive.petrolstation.pos.dto.PosDraftItemDto;

public class PosCheckoutItemsAdapter extends RecyclerView.Adapter<PosCheckoutItemsAdapter.VH> {

    public interface Listener {
        void onDelete(PosDraftItemDto item, int position);
    }

    private final List<PosDraftItemDto> list = new ArrayList<>();
    private final Listener listener;

    public PosCheckoutItemsAdapter(Listener listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        PosDraftItemDto it = list.get(position);
        return it != null ? it.itemId : super.getItemId(position);
    }

    public void submitList(List<PosDraftItemDto> data) {
        list.clear();
        if (data != null) list.addAll(data);
        notifyDataSetChanged();
    }

    public PosDraftItemDto getItem(int position) {
        if (position < 0 || position >= list.size()) return null;
        return list.get(position);
    }

    public void removeAt(int position) {
        if (position < 0 || position >= list.size()) return;
        list.remove(position);
        notifyItemRemoved(position);
    }

    public List<PosDraftItemDto> getData() {
        return new ArrayList<>(list);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowPosCheckoutItemBinding b = RowPosCheckoutItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PosDraftItemDto item = list.get(position);
        if (item == null) return;

        h.b.txtName.setText(item.name == null ? "" : item.name);

        double price = item.price;
        int qty = item.qty;

        h.b.txtPrice.setText("$" + trimZeros(price));
        h.b.txtQty.setText("x" + qty);

        double lineTotal = price * qty;
        h.b.txtLineTotal.setText("$" + trimZeros(lineTotal));
    }

    private String trimZeros(double v) {
        if (v == (long) v) return String.valueOf((long) v);
        return String.valueOf(v);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        RowPosCheckoutItemBinding b;
        VH(RowPosCheckoutItemBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
