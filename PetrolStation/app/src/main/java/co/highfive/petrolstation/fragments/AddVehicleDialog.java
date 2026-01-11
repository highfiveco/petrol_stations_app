package co.highfive.petrolstation.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.adapters.DropDownAdapter;
import co.highfive.petrolstation.customers.dto.CustomerVehicleDto;
import co.highfive.petrolstation.customers.dto.SimpleSettingDto;
import co.highfive.petrolstation.customers.dto.VehicleSettingsResponseDto;
import co.highfive.petrolstation.databinding.AddVehiclesLayoutBinding;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.models.Currency;

/**
 * UI-only dialog for Add/Edit Vehicle.
 * - No API calls here.
 * - Activity passes settings and handles API.
 */
public class AddVehicleDialog extends DialogFragment {

    public interface Listener {
        void onSubmitAdd(@NonNull Map<String, String> payload);
        void onSubmitEdit(@NonNull Map<String, String> payload);
        void onDismissed();
    }

    private static final String ARG_CUSTOMER_ID = "customer_id";

    private Context context;
    private AddVehiclesLayoutBinding binding;

    private Listener listener;

    private String customerId = "";
    private CustomerVehicleDto editVehicle = null;
    private VehicleSettingsResponseDto settings = null;

    // Selected values
    private Integer selectedVehicleTypeId = null;
    private Integer selectedVehicleColorId = null;
    private String selectedModel = null;

    public AddVehicleDialog() {}

    public static AddVehicleDialog newInstance(
            @NonNull String customerId,
            @Nullable CustomerVehicleDto editVehicle,
            @NonNull VehicleSettingsResponseDto settings
    ) {
        AddVehicleDialog d = new AddVehicleDialog();
        Bundle b = new Bundle();
        b.putString(ARG_CUSTOMER_ID, customerId);
        d.setArguments(b);

        d.editVehicle = editVehicle;
        d.settings = settings;
        return d;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.context = null;
    }

    private boolean isEditMode() {
        return editVehicle != null && editVehicle.id > 0;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = AddVehiclesLayoutBinding.inflate(inflater, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        customerId = getArguments() != null ? safe(getArguments().getString(ARG_CUSTOMER_ID)) : "";

        initUI();
        initClicks();

        if (isEditMode()) {
            prefillEditData();
        }

        return binding.getRoot();
    }

    private void initUI() {
        // Date picker on click
        binding.vehicleLicenseExpiryDate.setFocusable(false);
        binding.vehicleLicenseExpiryDate.setClickable(true);
        binding.vehicleLicenseExpiryDate.setOnClickListener(v -> openDatePicker());

        // Title text in header (optional if you want change title dynamically)
        // XML title is static "@string/add_vehicles"
        // If you have another TextView for title, set here.
    }

    private void initClicks() {
        binding.close.setOnClickListener(v -> dismissAllowingStateLoss());

        binding.vehicleTypeLayout.setOnClickListener(v -> openVehicleTypeDropdown());
        binding.typeLayout.setOnClickListener(v -> openVehicleColorDropdown());
        binding.vehicleModelLayout.setOnClickListener(v -> openModelDropdown());

        binding.save.setOnClickListener(v -> {
            String error = validate();
            if (error != null) {
                toastLocal(error);
                return;
            }

            Map<String, String> payload = buildPayload();

            if (listener != null) {
                if (isEditMode()) listener.onSubmitEdit(payload);
                else listener.onSubmitAdd(payload);
            }
        });
    }

    private void prefillEditData() {
        // Fill text inputs
        binding.vehicleNumber.setText(safe(editVehicle.vehicle_number));
        binding.vehicleLicenseExpiryDate.setText(safe(editVehicle.license_expiry_date));
        binding.notes.setText(safe(editVehicle.notes));

        // Set selections
        selectedVehicleTypeId = editVehicle.vehicle_type;
        selectedVehicleColorId = editVehicle.vehicle_color;
        selectedModel = safe(editVehicle.model);

        // Set dropdown displayed names
        String typeName = safe(editVehicle.vehicle_type_name);
        if (typeName.isEmpty() && settings != null && settings.vehicle_type != null) {
            typeName = resolveNameById((ArrayList<SimpleSettingDto>) settings.vehicle_type, selectedVehicleTypeId);
        }
        binding.vehicleType.setText(typeName);

        String colorName = safe(editVehicle.vehicle_color_name);
        if (colorName.isEmpty() && settings != null && settings.vehicle_color != null) {
            colorName = resolveNameById((ArrayList<SimpleSettingDto>) settings.vehicle_color, selectedVehicleColorId);
        }
        binding.vehicleColor.setText(colorName);

        // model is string in your API response, we show the string as-is
        binding.vehicleModel.setText(selectedModel);
    }

    private Map<String, String> buildPayload() {
        Map<String, String> m = new HashMap<>();

        // Required
        m.put("customer_id", safe(customerId));
        m.put("vehicle_number", safeTrim(binding.vehicleNumber.getText()));
        m.put("vehicle_type", String.valueOf(selectedVehicleTypeId != null ? selectedVehicleTypeId : 0));
        m.put("vehicle_color", String.valueOf(selectedVehicleColorId != null ? selectedVehicleColorId : 0));
        m.put("model", safe(selectedModel));
        m.put("license_expiry_date", safeTrim(binding.vehicleLicenseExpiryDate.getText()));
        m.put("notes", safeTrim(binding.notes.getText()));

        // Edit needs id
        if (isEditMode()) {
            m.put("id", String.valueOf(editVehicle.id));
        }

        return m;
    }
    private int[] parseYmd(String ymd) {
        try {
            // yyyy-MM-dd
            String[] parts = ymd.split("-");
            int y = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]); // 1-12
            int d = Integer.parseInt(parts[2]);
            return new int[]{y, m, d};
        } catch (Exception e) {
            return null;
        }
    }
    private String validate() {
        if (safeTrim(binding.vehicleNumber.getText()).isEmpty()) return getString(R.string.vehicle_number);

        if (selectedVehicleTypeId == null || selectedVehicleTypeId <= 0) return getString(R.string.vehicle_type);

        if (selectedVehicleColorId == null || selectedVehicleColorId <= 0) return getString(R.string.vehicle_color);

        if (selectedModel == null || selectedModel.trim().isEmpty()) return getString(R.string.vehicle_model);

        if (safeTrim(binding.vehicleLicenseExpiryDate.getText()).isEmpty()) return getString(R.string.vehicle_license_expiry_date);

        if (customerId == null || customerId.trim().isEmpty()) return getString(R.string.general_error);

        return null;
    }

    // =========================
    // Date Picker
    // =========================
    private void openDatePicker() {
        if (getActivity() == null) return;

        int year, month, day;

        // If edit mode and has a date, open picker on that date
        String current = safeTrim(binding.vehicleLicenseExpiryDate.getText());
        int[] ymd = parseYmd(current);

        if (ymd != null) {
            year = ymd[0];
            month = ymd[1] - 1; // Calendar month is 0-based
            day = ymd[2];
        } else {
            Calendar cal = Calendar.getInstance();
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH);
            day = cal.get(Calendar.DAY_OF_MONTH);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                getActivity(),
                (view, y, m, d) -> {
                    String mm = String.format(Locale.ENGLISH, "%02d", m + 1);
                    String dd = String.format(Locale.ENGLISH, "%02d", d);
                    binding.vehicleLicenseExpiryDate.setText(y + "-" + mm + "-" + dd);
                },
                year, month, day
        );
        dialog.show();
    }

    // =========================
    // Dropdowns
    // =========================
    private void openVehicleTypeDropdown() {
        if (settings == null || settings.vehicle_type == null || settings.vehicle_type.isEmpty()) {
            toastLocal(getString(R.string.general_error));
            return;
        }

        ArrayList<Currency> list = toCurrencyList((ArrayList<SimpleSettingDto>) settings.vehicle_type);

        showDropDown(binding.vehicleType, list, (id, name) -> {
            selectedVehicleTypeId = id;
            binding.vehicleType.setText(name);
        });
    }

    private void openVehicleColorDropdown() {
        if (settings == null || settings.vehicle_color == null || settings.vehicle_color.isEmpty()) {
            toastLocal(getString(R.string.general_error));
            return;
        }

        ArrayList<Currency> list = toCurrencyList((ArrayList<SimpleSettingDto>) settings.vehicle_color);

        showDropDown(binding.vehicleColor, list, (id, name) -> {
            selectedVehicleColorId = id;
            binding.vehicleColor.setText(name);
        });
    }

    private void openModelDropdown() {
        // As you said: model list may not come now, but assume it exists later.
        // If not returned, we fallback to vehicle_type as a model list (your assumption).
        ArrayList<SimpleSettingDto> src = null;

        if (settings != null && settings.model != null && !settings.model.isEmpty()) {
            src = new ArrayList<>(settings.model);
        } else if (settings != null && settings.vehicle_type != null && !settings.vehicle_type.isEmpty()) {
            src = new ArrayList<>(settings.vehicle_type);
        }

        if (src == null || src.isEmpty()) {
            toastLocal(getString(R.string.general_error));
            return;
        }

        ArrayList<Currency> list = toCurrencyList(src);

        showDropDown(binding.vehicleModel, list, (id, name) -> {
            selectedModel = name; // model is String
            binding.vehicleModel.setText(name);
        });
    }

    private interface OnDropSelected {
        void onSelected(int id, String name);
    }

    private void showDropDown(View anchor, ArrayList<Currency> arrayList, OnDropSelected cb) {
        if (getActivity() == null) return;

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View popupView = Objects.requireNonNull(inflater).inflate(R.layout.drop_down_list, null);

        RecyclerView recyclerView = popupView.findViewById(R.id.recycler_view);
        int width = (int) (getActivity().getWindowManager().getDefaultDisplay().getWidth() * 0.55);
        PopupWindow popupWindow = new PopupWindow(popupView, width, ViewGroup.LayoutParams.WRAP_CONTENT);

        DropDownAdapter adapter = new DropDownAdapter(arrayList, (view, position) -> {
            Currency c = arrayList.get(position);
            int id = parseIntSafe(c.getId());
            String name = safe(c.getName());
            if (cb != null) cb.onSelected(id, name);
            popupWindow.dismiss();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) popupWindow.setElevation(18f);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.showAsDropDown(anchor);
    }

    private ArrayList<Currency> toCurrencyList(ArrayList<SimpleSettingDto> src) {
        ArrayList<Currency> out = new ArrayList<>();
        for (SimpleSettingDto s : src) {
            if (s == null) continue;
            out.add(new Currency(String.valueOf(s.id), safe(s.name)));
        }
        return out;
    }

    private String resolveNameById(ArrayList<SimpleSettingDto> src, Integer id) {
        if (id == null) return "";
        for (SimpleSettingDto s : src) {
            if (s != null && s.id == id) return safe(s.name);
        }
        return "";
    }

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    private void toastLocal(String msg) {
        try {
            if (getActivity() instanceof BaseActivity) {
                ((BaseActivity) getActivity()).toast(msg);
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listener != null) listener.onDismissed();
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        try {
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(this, tag);
            ft.commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            Log.e("AddVehicleDialog", "Exception", e);
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    private static String safe(String s) { return s == null ? "" : s; }
    private static String safeTrim(CharSequence cs) { return cs == null ? "" : cs.toString().trim(); }
}
