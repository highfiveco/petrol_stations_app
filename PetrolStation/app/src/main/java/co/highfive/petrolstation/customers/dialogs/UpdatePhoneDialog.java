package co.highfive.petrolstation.customers.dialogs;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.listener.UpdatePhoneListener;

public class UpdatePhoneDialog extends DialogFragment {

    private UpdatePhoneListener listener;

    private String nameVal = "";
    private String phoneVal = "";

    public UpdatePhoneDialog() {}

    public static UpdatePhoneDialog newInstance(String name, String phone) {
        UpdatePhoneDialog d = new UpdatePhoneDialog();
        Bundle b = new Bundle();
        b.putString("name", name);
        b.putString("phone", phone);
        d.setArguments(b);
        return d;
    }

    public void setListener(UpdatePhoneListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            nameVal = safe(args.getString("name"));
            phoneVal = safe(args.getString("phone"));
        }
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.update_phone_dialog_layout, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        AppCompatTextView tvName = v.findViewById(R.id.name);
        AppCompatEditText etPhone = v.findViewById(R.id.phone);
        AppCompatImageView btnClose = v.findViewById(R.id.close);
        AppCompatTextView btnSave = v.findViewById(R.id.save);

        tvName.setText(nameVal);
        etPhone.setText(phoneVal);

        btnClose.setOnClickListener(view -> dismissAllowingStateLoss());

        btnSave.setOnClickListener(view -> {
            String phone = safe(etPhone.getText() != null ? etPhone.getText().toString() : "").trim();

            String validate = validate(phone);
            if (validate != null) {
                Toast.makeText(requireContext(), validate, Toast.LENGTH_LONG).show();
                return;
            }

            if (listener != null) {
                listener.setPhone(phone);
            }
        });

        return v;
    }

    private String validate(String phone) {
        if (phone.isEmpty()) return getString(R.string.enter_phone);
        return null;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(this, tag);
            ft.commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            Log.e("UpdatePhoneDialog", "show error", e);
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
