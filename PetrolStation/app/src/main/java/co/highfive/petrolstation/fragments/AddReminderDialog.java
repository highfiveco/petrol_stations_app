package co.highfive.petrolstation.fragments;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Locale;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.databinding.AddReminderDialogLayoutBinding;

public class AddReminderDialog extends DialogFragment {

    public interface Listener {
        void onSave(String text, String date);
    }

    private AddReminderDialogLayoutBinding binding;
    private Listener listener;

    public static AddReminderDialog newInstance() {
        return new AddReminderDialog();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = AddReminderDialogLayoutBinding.inflate(inflater, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        binding.selectDate.setOnClickListener(v -> showNativeDatePicker());
        binding.close.setOnClickListener(v -> dismiss());

        binding.save.setOnClickListener(v -> {
            String text = safe(binding.text.getText() != null ? binding.text.getText().toString() : "");
            String date = safe(binding.date.getText() != null ? binding.date.getText().toString() : "");

            String err = validate(text, date);
            if (err != null) {
                if (getActivity() != null) {
                    android.widget.Toast.makeText(getActivity(), err, android.widget.Toast.LENGTH_LONG).show();
                }
                return;
            }

            if (listener != null) listener.onSave(text.trim(), date.trim());
        });

        return binding.getRoot();
    }

    private void showNativeDatePicker() {
        if (getActivity() == null) return;

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH); // 0-based
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                getActivity(),
                (view, y, m, d) -> {
                    // format: yyyy-MM-dd
                    String formatted = String.format(
                            Locale.ENGLISH,
                            "%04d-%02d-%02d",
                            y,
                            (m + 1),
                            d
                    );
                    binding.date.setText(formatted);
                },
                year, month, day
        );

        dialog.show();
    }

    private String validate(String text, String date) {
        if (text.trim().isEmpty()) return getString(R.string.enter_text);
        if (date.trim().isEmpty()) return getString(R.string.enter_date);
        return null;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
