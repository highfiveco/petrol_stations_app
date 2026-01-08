package co.highfive.petrolstation.customers.dialogs;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import java.util.Calendar;
import java.util.Locale;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.listener.AddReminderListener;

public class AddReminderDialog extends DialogFragment {

    private AddReminderListener addReminderListener;

    private AppCompatEditText text;
    private AppCompatTextView date;

    public AddReminderDialog() {
    }

    public void setAddReminderListener(AddReminderListener addReminderListener) {
        this.addReminderListener = addReminderListener;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View customView = inflater.inflate(R.layout.add_reminder_dialog_layout, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        text = customView.findViewById(R.id.text);
        date = customView.findViewById(R.id.date);

        AppCompatImageView close = customView.findViewById(R.id.close);
        AppCompatTextView save = customView.findViewById(R.id.save);
        RelativeLayout selectDate = customView.findViewById(R.id.select_date);

        selectDate.setOnClickListener(v -> showPicker(date));

        close.setOnClickListener(v -> dismissAllowingStateLoss());

        save.setOnClickListener(v -> {
            String validate = validate();
            if (validate == null) {
                if (addReminderListener != null) {
                    addReminderListener.addReminder(
                            text.getText() != null ? text.getText().toString().trim() : "",
                            date.getText() != null ? date.getText().toString().trim() : ""
                    );
                }
            } else {
                Toast.makeText(requireContext(), validate, Toast.LENGTH_LONG).show();
            }
        });

        return customView;
    }

    private String validate() {
        String t = (text.getText() == null) ? "" : text.getText().toString().trim();
        String d = (date.getText() == null) ? "" : date.getText().toString().trim();

        if (t.isEmpty()) return getString(R.string.enter_text);
        if (d.isEmpty()) return getString(R.string.enter_date);
        return null;
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        try {
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(this, tag);
            ft.commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            Log.e("AddReminderDialog", "show error", e);
        }
    }

    public void showPicker(AppCompatTextView target) {
        Calendar c = Calendar.getInstance();

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);


        year+=20;

        android.app.DatePickerDialog dlg = new android.app.DatePickerDialog(
                requireContext(),
                R.style.AppDatePickerDialog,
                (view, y, m, d) -> {
                    String dateStr = String.format(
                            Locale.ENGLISH,
                            "%04d-%02d-%02d",
                            y, (m + 1), d
                    );
                    target.setText(dateStr);
                },
                year, month, day
        );

        // نفس منطقك: max = اليوم (أو عدله حسب احتياجك)
//        dlg.getDatePicker().setMaxDate(System.currentTimeMillis());
        dlg.show();
    }

}
