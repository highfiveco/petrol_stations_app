package co.highfive.petrolstation.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import co.highfive.petrolstation.databinding.CustomerTicketViewLayoutBinding;
import co.highfive.petrolstation.databinding.CustomerTicketViewLayoutBinding;
import co.highfive.petrolstation.models.Reminder; // عدّل حسب مشروعك

public class CustomerRemindersAdapter extends RecyclerView.Adapter<CustomerRemindersAdapter.VH> {

    public interface Listener {
        void onDeleteClicked(Reminder r);
    }

    private final android.content.Context context;
    private final List<Reminder> items;
    private final Listener listener;

    public CustomerRemindersAdapter(android.content.Context context, List<Reminder> items, Listener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Reminder> more) {
        int start = items.size();
        items.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CustomerTicketViewLayoutBinding b = CustomerTicketViewLayoutBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Reminder r = items.get(position);

        h.binding.text.setText(safe(r.getText()));
        h.binding.date.setText(safe(r.getDate()));
        h.binding.createdAt.setText(safe(r.getCreated_at()));
        h.binding.user.setText(safe(r.getName()));

        h.binding.deleteLayout.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClicked(r);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final CustomerTicketViewLayoutBinding binding;
        VH(CustomerTicketViewLayoutBinding b) {
            super(b.getRoot());
            this.binding = b;
        }
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
