package co.highfive.petrolstation.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.adapters.SelectVehicleAdapter;
import co.highfive.petrolstation.customers.dto.CustomerVehicleDto;
import co.highfive.petrolstation.databinding.DialogSelectVehicleBinding;
import co.highfive.petrolstation.fuelsale.dto.FuelCustomerVehiclesResponseDto;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.ApiError;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;

public class SelectVehicleDialog extends DialogFragment {

    public interface Listener {
        void onSelected(@NonNull CustomerVehicleDto vehicle);
    }

    private DialogSelectVehicleBinding binding;
    private BaseActivity baseActivity;
    private Listener listener;

    private int customerId = 0;

    // persist
    private int selectedVehicleId = 0;
    private ArrayList<CustomerVehicleDto> lastVehicles = new ArrayList<>();

    private SelectVehicleAdapter adapter;

    public SelectVehicleDialog() {}

    public static SelectVehicleDialog newInstance(int customerId,
                                                  ArrayList<CustomerVehicleDto> lastVehicles,
                                                  int selectedVehicleId) {
        SelectVehicleDialog d = new SelectVehicleDialog();
        d.customerId = customerId;
        d.lastVehicles = lastVehicles != null ? lastVehicles : new ArrayList<>();
        d.selectedVehicleId = selectedVehicleId;
        return d;
    }

    public void setListener(Listener l) { this.listener = l; }

    public ArrayList<CustomerVehicleDto> getLastVehicles() { return lastVehicles; }
    public int getSelectedVehicleId() { return selectedVehicleId; }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof BaseActivity) baseActivity = (BaseActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = DialogSelectVehicleBinding.inflate(inflater, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        initRecycler();
        initClicks();

        // لو في بيانات قديمة اعرضها فوراً، وبعدها اعمل refresh
        renderVehicles(lastVehicles);

        // دايمًا نعمل request عشان تكون آخر بيانات (تقدر تغيرها لاحقًا)
        fetchVehicles();

        return binding.getRoot();
    }

    private void initRecycler() {
        adapter = new SelectVehicleAdapter(v -> selectedVehicleId = v.id);

        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.setAdapter(adapter);

        adapter.setSelectedId(selectedVehicleId);
    }

    private void initClicks() {
        binding.close.setOnClickListener(v -> dismissAllowingStateLoss());

        binding.confirm.setOnClickListener(v -> {
            CustomerVehicleDto sel = adapter.getSelected();
            if (sel == null) {
                toastLocal(getString(R.string.general_error));
                return;
            }
            selectedVehicleId = sel.id;

            if (listener != null) listener.onSelected(sel);
            dismissAllowingStateLoss();
        });
    }

    private void fetchVehicles() {

        if (baseActivity == null) return;
        baseActivity.showProgressHUD();

        if (customerId <= 0) {
            toastLocal(getString(R.string.general_error));
            return;
        }

        baseActivity.showProgressHUD();

        Type type = new TypeToken<BaseResponse<FuelCustomerVehiclesResponseDto>>() {}.getType();

        ApiClient.ApiParams params = new ApiClient.ApiParams().add("customer_id", String.valueOf(customerId));

        baseActivity.apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.CUSTOMER_VEHICLES, // لازم يكون هذا معرف عندك
                params,
                null,
                type,
                0,
                new ApiCallback<FuelCustomerVehiclesResponseDto>() {

                    @Override
                    public void onSuccess(FuelCustomerVehiclesResponseDto data, String msg, String rawJson) {
                        baseActivity.hideProgressHUD();

                        List<CustomerVehicleDto> vehicles = (data != null && data.vehicles != null)
                                ? data.vehicles : new ArrayList<>();

                        lastVehicles.clear();
                        lastVehicles.addAll(vehicles);

                        renderVehicles(lastVehicles);

                        adapter.setSelectedId(selectedVehicleId); // keep selection if exists
                    }

                    @Override
                    public void onError(ApiError error) {
                        baseActivity.hideProgressHUD();
                        toastLocal(error != null ? error.message : getString(R.string.general_error));
                    }

                    @Override
                    public void onUnauthorized(String rawJson) {
                        baseActivity.hideProgressHUD();
                        baseActivity.logout();
                    }

                    @Override
                    public void onNetworkError(String reason) {
                        baseActivity.hideProgressHUD();
                        toastLocal(getString(R.string.no_internet));
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        baseActivity.hideProgressHUD();
                        toastLocal(getString(R.string.general_error));
                    }
                }
        );
    }

    private void renderVehicles(List<CustomerVehicleDto> vehicles) {
        boolean empty = vehicles == null || vehicles.isEmpty();
        binding.empty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.recycler.setVisibility(empty ? View.GONE : View.VISIBLE);

        adapter.setItems(vehicles);
        adapter.setSelectedId(selectedVehicleId);
    }

    private void toastLocal(String msg) {
        try { if (baseActivity != null) baseActivity.toast(msg); } catch (Exception ignored) {}
    }
}
