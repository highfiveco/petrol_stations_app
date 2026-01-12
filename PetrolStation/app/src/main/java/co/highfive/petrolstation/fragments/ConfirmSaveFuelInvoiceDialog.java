package co.highfive.petrolstation.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import co.highfive.petrolstation.databinding.DialogConfirmSaveFuelInvoiceBinding;

public class ConfirmSaveFuelInvoiceDialog extends DialogFragment {

    public interface Listener {
        void onSaveAndNew();
        void onCancelCurrentAndNew();
    }

    private DialogConfirmSaveFuelInvoiceBinding binding;
    private Listener listener;

    public static ConfirmSaveFuelInvoiceDialog newInstance() {
        return new ConfirmSaveFuelInvoiceDialog();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = DialogConfirmSaveFuelInvoiceBinding.inflate(inflater, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        binding.btnSave.setOnClickListener(v -> {
            dismissAllowingStateLoss();
            if (listener != null) listener.onSaveAndNew();
        });

        binding.btnCancelInvoice.setOnClickListener(v -> {
            dismissAllowingStateLoss();
            if (listener != null) listener.onCancelCurrentAndNew();
        });

        binding.close.setOnClickListener(v -> dismissAllowingStateLoss());

        return binding.getRoot();
    }
}
