package co.highfive.petrolstation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.customers.dto.CustomerVehicleDto;
import co.highfive.petrolstation.databinding.ItemSelectVehicleBinding;

public class SelectVehicleAdapter extends RecyclerView.Adapter<SelectVehicleAdapter.VH> {

    public interface Listener {
        void onClick(CustomerVehicleDto dto);
    }

    private List<CustomerVehicleDto> list = new ArrayList<>();
    private int selectedId = 0;
    private final Listener listener;

    public SelectVehicleAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<CustomerVehicleDto> items) {
        list = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setSelectedId(int id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    public CustomerVehicleDto getSelected() {
        if (list == null) return null;
        for (CustomerVehicleDto v : list) {
            if (v != null && v.id == selectedId) return v;
        }
        return null;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemSelectVehicleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        CustomerVehicleDto v = list.get(position);

        String typeName = v != null ? safe(v.vehicle_type_name) : "";
        String model = v != null ? safe(v.model) : "";
        String colorName = v != null ? safe(v.vehicle_color_name) : "";
        String number = v != null ? safe(v.vehicle_number) : "";

        String vehicleName = typeName;
        if (!model.isEmpty()) {
            vehicleName = vehicleName.isEmpty() ? model : (vehicleName + " " + model);
        }

        h.binding.tvLine1.setText("اسم المركبة: " + safe(vehicleName));
        h.binding.tvLine2.setText("اللون: " + safe(colorName));
        h.binding.tvLine3.setText("رقم المركبة: " + safe(number));

        boolean isSel = v != null && v.id == selectedId;

        // ✅ فقط عند السلكت يظهر
        h.binding.ivSelected.setVisibility(isSel ? View.VISIBLE : View.GONE);

        h.binding.getRoot().setAlpha(isSel ? 1f : 0.9f);
        h.binding.getRoot().setScaleX(isSel ? 1.0f : 0.995f);
        h.binding.getRoot().setScaleY(isSel ? 1.0f : 0.995f);

        h.binding.getRoot().setOnClickListener(view -> {
            if (v == null) return;
            selectedId = v.id;
            notifyDataSetChanged();
            if (listener != null) listener.onClick(v);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        ItemSelectVehicleBinding binding;
        VH(ItemSelectVehicleBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
