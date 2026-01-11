package co.highfive.petrolstation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import co.highfive.petrolstation.customers.dto.CustomerVehicleDto;
import co.highfive.petrolstation.databinding.CustomerVehiclesItemBinding;

public class CustomerVehiclesAdapter extends RecyclerView.Adapter<CustomerVehiclesAdapter.VH> {

    public interface Listener {
        void onEdit(CustomerVehicleDto v);
    }

    private final List<CustomerVehicleDto> items;
    private final Listener listener;

    private boolean canEdit = true;

    public CustomerVehiclesAdapter(List<CustomerVehicleDto> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
        notifyDataSetChanged();
    }

    public void setItems(List<CustomerVehicleDto> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CustomerVehiclesItemBinding b = CustomerVehiclesItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        CustomerVehicleDto v = items.get(position);

        h.binding.vehiclesNumber.setText(safe(v.vehicle_number));
        h.binding.vehiclesType.setText(safe(v.vehicle_type_name)); // في XML id اسمه date بس هو نوع المركبة
        h.binding.color.setText(safe(v.vehicle_color_name)); // في XML id اسمه count بس هو اللون
        h.binding.model.setText(safe(v.model));              // في XML id اسمه total بس هو الموديل
        h.binding.licenseExpiryDate.setText(safe(v.license_expiry_date));
        // license expiry date - الـ TextView ما له id بالـ XML
        // نخليها كما هي (static label only) لحد ما تضيف id للتاريخ بالـ XML.
        // إذا بدك نعرضها لازم تعطيني id للـ TextView الأخير.

        h.binding.editLayout.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        h.binding.editLayout.setOnClickListener(vv -> {
            if (listener != null) listener.onEdit(v);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final CustomerVehiclesItemBinding binding;
        VH(CustomerVehiclesItemBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
