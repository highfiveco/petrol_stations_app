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

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.databinding.DialogAddCustomerBinding;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;

public class AddCustomerDialog extends DialogFragment {

    public interface Listener {
        void onSubmit(@NonNull String name, @NonNull String mobile);
    }

    private DialogAddCustomerBinding binding;
    private BaseActivity baseActivity;
    private Listener listener;

    private String lastName = "";
    private String lastMobile = "";

    public AddCustomerDialog() {}

    public static AddCustomerDialog newInstance(String lastName, String lastMobile) {
        AddCustomerDialog d = new AddCustomerDialog();
        d.lastName = lastName != null ? lastName : "";
        d.lastMobile = lastMobile != null ? lastMobile : "";
        return d;
    }

    public void setListener(Listener l) {
        this.listener = l;
    }

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

        binding = DialogAddCustomerBinding.inflate(inflater, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        bindState();
        initClicks();

        return binding.getRoot();
    }

    private void bindState() {
        binding.etName.setText(lastName);
        binding.etMobile.setText(lastMobile);

        if (binding.etName.getText() != null) {
            binding.etName.setSelection(binding.etName.getText().length());
        }
    }

    private void initClicks() {
        binding.close.setOnClickListener(v -> dismissAllowingStateLoss());

        binding.btnSave.setOnClickListener(v -> {
            String name = safeTrim(binding.etName.getText());
            String mobile = safeTrim(binding.etMobile.getText());

            if (name.isEmpty()) {
                toastLocal(getString(R.string.enter_name));
                return;
            }
            if (mobile.isEmpty()) {
                toastLocal("أدخل رقم الجوال");
                return;
            }

            lastName = name;
            lastMobile = mobile;

            if (listener != null) listener.onSubmit(name, mobile);
            dismissAllowingStateLoss();
        });
    }

    public String getLastName() { return lastName; }
    public String getLastMobile() { return lastMobile; }

    private void toastLocal(String msg) {
        try { if (baseActivity != null) baseActivity.toast(msg); } catch (Exception ignored) {}
    }

    private static String safeTrim(CharSequence cs) {
        return cs == null ? "" : cs.toString().trim();
    }
}
