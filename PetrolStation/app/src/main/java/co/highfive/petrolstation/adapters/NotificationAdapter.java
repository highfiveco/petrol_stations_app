package co.highfive.petrolstation.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.activities.CustomerActivity;
import co.highfive.petrolstation.databinding.LoadMoreViewBinding;
import co.highfive.petrolstation.databinding.NotificationViewLayoutBinding;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.models.Notification;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 1;
    private static final int TYPE_LOADING = 2;

    private final BaseActivity activity;
    private final ArrayList<Notification> items = new ArrayList<>();
    private boolean showLoading = false;

    public NotificationAdapter(BaseActivity activity) {
        this.activity = activity;
    }

    public void setItems(List<Notification> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void addItems(List<Notification> more) {
        if (more == null || more.isEmpty()) return;
        int start = items.size();
        items.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }

    public void setLoading(boolean loading) {
        if (this.showLoading == loading) return;
        this.showLoading = loading;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (showLoading && position == getItemCount() - 1) return TYPE_LOADING;
        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return items.size() + (showLoading ? 1 : 0);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_LOADING) {
            LoadMoreViewBinding b = LoadMoreViewBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new LoadingVH(b);
        }

        NotificationViewLayoutBinding b = NotificationViewLayoutBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ItemVH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemVH) {
            Notification n = items.get(position);
            ((ItemVH) holder).bind(n);
        }
    }

    class ItemVH extends RecyclerView.ViewHolder {
        private final NotificationViewLayoutBinding binding;

        ItemVH(NotificationViewLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Notification notification) {
            binding.title.setText(notification.getTitle());
            binding.date.setText(notification.getCreated_at());

            binding.rootItem.setOnClickListener(v -> {
                String type = notification.getType_notification();
                if ("1".equals(type)) {
                    Bundle b = new Bundle();
                    b.putString("id", notification.getCustomer_id());
                    b.putString("name", notification.getCustomer_name());
                    b.putString("mobile", notification.getCustomer_mobile());
                    b.putString("account_id", notification.getCustomer_account_id());
                    activity.moveToActivity(activity, CustomerActivity.class, b, false);

                }
            });
        }
    }

    static class LoadingVH extends RecyclerView.ViewHolder {
        LoadingVH(LoadMoreViewBinding binding) {
            super(binding.getRoot());
        }
    }
}
