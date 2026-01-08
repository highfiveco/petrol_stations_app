package co.highfive.petrolstation.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.listener.CustomerListener;

public class CustomerFilterDialog extends DialogFragment {

    private static final String ARG_NAME = "arg_name";
    private static final String ARG_BALANCE = "arg_balance";

    private CustomerListener listener;

    public static CustomerFilterDialog newInstance(String name, String balance) {
        CustomerFilterDialog d = new CustomerFilterDialog();
        Bundle b = new Bundle();
        b.putString(ARG_NAME, name);
        b.putString(ARG_BALANCE, balance);
        d.setArguments(b);
        return d;
    }

    public void setListener(CustomerListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View v = inflater.inflate(R.layout.dialog_customer_filter, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        AppCompatImageView close = v.findViewById(R.id.close);
        AppCompatTextView save = v.findViewById(R.id.save);
        AppCompatTextView cancel = v.findViewById(R.id.cancel_filter);

        AppCompatEditText nameEt = v.findViewById(R.id.name);
        AppCompatEditText balanceEt = v.findViewById(R.id.balance);

        String initName = getArguments() != null ? getArguments().getString(ARG_NAME, "") : "";
        String initBalance = getArguments() != null ? getArguments().getString(ARG_BALANCE, "") : "";

        if (initName != null && !initName.trim().isEmpty()) nameEt.setText(initName);
        if (initBalance != null && !initBalance.trim().isEmpty()) balanceEt.setText(initBalance);

        close.setOnClickListener(view -> dismiss());

        save.setOnClickListener(view -> {
            String nameVal = nameEt.getText() == null ? "" : nameEt.getText().toString().trim();
            String balanceVal = balanceEt.getText() == null ? "" : balanceEt.getText().toString().trim();

            if (listener != null) listener.onApplyFilter(nameVal, balanceVal);
            dismiss();
        });

        cancel.setOnClickListener(view -> {
            nameEt.setText("");
            balanceEt.setText("");

            if (listener != null) listener.onClearFilter();
            dismiss();
        });

        return v;
    }
}
