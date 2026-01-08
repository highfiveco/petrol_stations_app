package co.highfive.petrolstation.customers.dialogs;

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
import co.highfive.petrolstation.listener.SendSmsListener;
import co.highfive.petrolstation.databinding.SendSmsDialogLayoutBinding;

public class SendSmsDialog extends DialogFragment {

    private SendSmsDialogLayoutBinding binding;
    private SendSmsListener listener;

    public SendSmsDialog() {}

    public void setListener(SendSmsListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = SendSmsDialogLayoutBinding.inflate(inflater, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        binding.back.setOnClickListener(v -> dismiss());
        binding.close.setOnClickListener(v -> dismiss());


        binding.save.setOnClickListener(v -> {
            String msg = binding.message.getText() != null ? binding.message.getText().toString().trim() : "";
            if (msg.isEmpty()) {
                binding.message.setError(getString(R.string.enter_message));
                return;
            }
            if (listener != null) listener.onSend(msg);
        });

        return binding.getRoot();
    }

    public void setMessageText(String text) {
        if (binding == null) return;
        binding.message.setText(text == null ? "" : text);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
