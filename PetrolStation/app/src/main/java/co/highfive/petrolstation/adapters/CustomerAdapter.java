package co.highfive.petrolstation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.customers.dto.CustomerDto;

public class CustomerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface CustomerItemListener {
        void onCustomerClick(CustomerDto customer, int position);
        void onAddPaymentClick(CustomerDto customer, int position);
        void onViewClick(CustomerDto customer, int position);
        void onViewVehicles(CustomerDto customer, int position);
    }

    private static final int VT_ITEM = 1;
    private static final int VT_LOADING = 2;

    private final ArrayList<CustomerDto> items = new ArrayList<>();
    private final CustomerItemListener listener;
    private boolean showLoading = false;

    public CustomerAdapter(CustomerItemListener listener) {
        this.listener = listener;
    }

    // =========================
    // Adapter core
    // =========================
    @Override
    public int getItemViewType(int position) {
        if (showLoading && position == items.size()) return VT_LOADING;
        return VT_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());

        if (viewType == VT_LOADING) {
            // اعمل layout بسيط فيه ProgressBar (مثلاً: R.layout.row_loading)
            View v = inf.inflate(R.layout.customer_item, parent, false);
            return new LoadingVH(v);
        }

        View v = inf.inflate(R.layout.customer_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof LoadingVH) {
            return;
        }

        VH h = (VH) holder;
        CustomerDto c = items.get(position);

        h.name.setText(safe(c.name));
        h.phone.setText(safe(c.mobile));
        h.status.setText(safe(c.customer_status));

        h.balance.setText(c.balance == null ? "" : String.valueOf(c.balance));
//        h.vehicles.setText(c.vehicles == null ? "" : String.valueOf(c.vehicles.size()));

        h.campaign.setText(safe(c.campaign_name));
        h.remainingAmount.setText(c.remaining_amount == null ? "" : String.valueOf(c.remaining_amount));

        h.root.setOnClickListener(v -> {
            if (listener != null) listener.onCustomerClick(c, position);
        });

        h.addCustomerPayment.setOnClickListener(v -> {
            if (listener != null) listener.onAddPaymentClick(c, position);
        });

        h.viewLayout.setOnClickListener(v -> {
            if (listener != null) listener.onViewClick(c, position);
        });

        h.vehicles_layout.setOnClickListener(v -> {
            if (listener != null) listener.onViewVehicles(c, position);
        });
    }

    @Override
    public int getItemCount() {
        return items.size() + (showLoading ? 1 : 0);
    }

    // =========================
    // Public helpers
    // =========================
    public void setItems(List<CustomerDto> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    public void addItems(List<CustomerDto> data) {
        if (data == null || data.isEmpty()) return;
        int start = items.size();
        items.addAll(data);
        notifyItemRangeInserted(start, data.size());
    }

    public void setLoading(boolean loading) {
        if (this.showLoading == loading) return;

        this.showLoading = loading;
        if (loading) {
            notifyItemInserted(items.size());
        } else {
            notifyItemRemoved(items.size());
        }
    }

    public CustomerDto getItem(int position) {
        if (position < 0 || position >= items.size()) return null;
        return items.get(position);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    // =========================
    // ViewHolders
    // =========================
    static class VH extends RecyclerView.ViewHolder {
        View root;

        AppCompatTextView name;
        AppCompatTextView phone;
        AppCompatTextView status;

        AppCompatTextView balance;
        AppCompatTextView vehicles;
        LinearLayout vehicles_layout;
        AppCompatTextView campaign;
        AppCompatTextView remainingAmount;

        View addCustomerPayment;
        View viewLayout;

        VH(@NonNull View itemView) {
            super(itemView);
            root = itemView;

            name = itemView.findViewById(R.id.name);
            phone = itemView.findViewById(R.id.phone);
            status = itemView.findViewById(R.id.status);

            balance = itemView.findViewById(R.id.balance);
            vehicles_layout = itemView.findViewById(R.id.vehicles_layout);
            vehicles = itemView.findViewById(R.id.vehicles);
            campaign = itemView.findViewById(R.id.campaign);
            remainingAmount = itemView.findViewById(R.id.remaining_amount);

            addCustomerPayment = itemView.findViewById(R.id.add_customer_payment);
            viewLayout = itemView.findViewById(R.id.view_layout);
        }
    }

    static class LoadingVH extends RecyclerView.ViewHolder {
        LoadingVH(@NonNull View itemView) {
            super(itemView);
        }
    }
}
