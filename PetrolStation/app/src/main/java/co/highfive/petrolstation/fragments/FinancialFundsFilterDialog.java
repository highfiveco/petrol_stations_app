package co.highfive.petrolstation.fragments;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.List;

import co.highfive.petrolstation.databinding.DialogFinancialFundsFilterBinding;
import co.highfive.petrolstation.listener.FinancialFundFilter;
import co.highfive.petrolstation.models.Currency;
import android.view.Gravity;
import android.widget.PopupMenu;

public class FinancialFundsFilterDialog extends DialogFragment {

    private DialogFinancialFundsFilterBinding binding;
    private FinancialFundFilter listener;

    private String currency;   // selected currency id (or code)
    private String boxStatus;  // selected status id
    private String userId;     // selected user id
    private String fromDate;   // yyyy-MM-dd
    private String toDate;     // yyyy-MM-dd

    private List<Currency> currencies;
    private List<Currency> statusFunds;
    private List<Currency> users;
    private int viewUsers;

    public static FinancialFundsFilterDialog newInstance(
            String currency,
            String boxStatus,
            String userId,
            String fromDate,
            String toDate,
            List<Currency> statusFunds,
            List<Currency> currencies,
            List<Currency> users,
            int viewUsers
    ) {
        FinancialFundsFilterDialog d = new FinancialFundsFilterDialog();
        d.currency = currency;
        d.boxStatus = boxStatus;
        d.userId = userId;
        d.fromDate = fromDate;
        d.toDate = toDate;
        d.statusFunds = statusFunds;
        d.currencies = currencies;
        d.users = users;
        d.viewUsers = viewUsers;
        return d;
    }

    public void setListener(FinancialFundFilter listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle s) {
        binding = DialogFinancialFundsFilterBinding.inflate(inflater, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        if (viewUsers != 1) {
            binding.userLayoutContainer.setVisibility(View.GONE);
        }

        bindInitialValues();
        bindClicks();

        return binding.getRoot();
    }

    private void bindInitialValues() {
        // Prefill texts based on passed IDs + lists
        binding.currency.setText(getSelectedNameById(currencies, currency));
        binding.boxStatus.setText(getSelectedNameById(statusFunds, boxStatus));

        if (viewUsers == 1) {
            binding.user.setText(getSelectedNameById(users, userId));
        }

        binding.fromDate.setText(nullToEmpty(fromDate));
        binding.toDate.setText(nullToEmpty(toDate));
    }

    private void bindClicks() {
        binding.close.setOnClickListener(v -> dismiss());
        binding.clearData.setOnClickListener(v -> clear());
        binding.search.setOnClickListener(v -> apply());

        // Currency dropdown
        binding.currencyLayout.setOnClickListener(v ->
                showPopupMenu(binding.currencyLayout, currencies, currency, (id, label) -> {
                    currency = id;
                    binding.currency.setText(label);
                })
        );

        // Box Status dropdown
        binding.boxStatusLayout.setOnClickListener(v ->
                showPopupMenu(binding.boxStatusLayout, statusFunds, boxStatus, (id, label) -> {
                    boxStatus = id;
                    binding.boxStatus.setText(label);
                })
        );

        // Users dropdown (only if allowed)
        if (viewUsers == 1) {
            binding.userLayout.setOnClickListener(v ->
                    showPopupMenu(binding.userLayout, users, userId, (id, label) -> {
                        userId = id;
                        binding.user.setText(label);
                    })
            );
        }

        // Date pickers
        binding.fromDate.setOnClickListener(v ->
                openDatePicker(binding.fromDate.getText().toString(), date -> {
                    fromDate = date;
                    binding.fromDate.setText(date);
                })
        );

        binding.toDate.setOnClickListener(v ->
                openDatePicker(binding.toDate.getText().toString(), date -> {
                    toDate = date;
                    binding.toDate.setText(date);
                })
        );
    }

    private void clear() {
        currency = null;
        boxStatus = null;
        userId = null;
        fromDate = null;
        toDate = null;

        binding.currency.setText("");
        binding.boxStatus.setText("");
        binding.user.setText("");
        binding.fromDate.setText("");
        binding.toDate.setText("");
    }

    private void apply() {
        if (listener != null) {
            listener.filter(
                    currency,
                    boxStatus,
                    userId,
                    binding.fromDate.getText().toString(),
                    binding.toDate.getText().toString()
            );
        }
        dismiss();
    }

    // ----------------------------
    // Helpers: Select Dialog
    // ----------------------------

    private interface OnSelectId {
        void onSelect(String id);
    }

    private void openSelectDialog(String title, List<Currency> items, String selectedId, OnSelectId cb) {
        if (items == null || items.isEmpty()) return;

        String[] labels = new String[items.size()];
        int selectedIndex = -1;

        for (int i = 0; i < items.size(); i++) {
            Currency c = items.get(i);
            labels[i] = getNameSafe(c);

            if (!TextUtils.isEmpty(selectedId) && selectedId.equals(getIdSafe(c))) {
                selectedIndex = i;
            }
        }

        final int[] tempIndex = { selectedIndex };

        AlertDialog.Builder b = new AlertDialog.Builder(requireContext());
        b.setTitle(title);
        b.setSingleChoiceItems(labels, selectedIndex, (dialog, which) -> tempIndex[0] = which);
        b.setPositiveButton("اختيار", (dialog, which) -> {
            int i = tempIndex[0];
            if (i >= 0 && i < items.size()) {
                cb.onSelect(getIdSafe(items.get(i)));
            }
            dialog.dismiss();
        });
        b.setNegativeButton("إلغاء", (dialog, which) -> dialog.dismiss());
        b.show();
    }

    private String getSelectedNameById(List<Currency> list, String id) {
        if (TextUtils.isEmpty(id) || list == null) return "";
        for (Currency c : list) {
            if (id.equals(getIdSafe(c))) {
                return getNameSafe(c);
            }
        }
        return "";
    }

    // ----------------------------
    // Helpers: Date Picker
    // ----------------------------

    private interface OnDateSelected {
        void onDate(String yyyyMMdd);
    }

    private void openDatePicker(String current, OnDateSelected cb) {
        Calendar cal = Calendar.getInstance();

        // if current is yyyy-MM-dd try to parse it
        if (!TextUtils.isEmpty(current) && current.contains("-")) {
            String[] parts = current.split("-");
            if (parts.length == 3) {
                try {
                    int y = Integer.parseInt(parts[0]);
                    int m = Integer.parseInt(parts[1]) - 1;
                    int d = Integer.parseInt(parts[2]);
                    cal.set(Calendar.YEAR, y);
                    cal.set(Calendar.MONTH, m);
                    cal.set(Calendar.DAY_OF_MONTH, d);
                } catch (Exception ignore) {}
            }
        }

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dp = new DatePickerDialog(
                requireContext(),
                (view, y, m, d) -> cb.onDate(formatYMD(y, m + 1, d)),
                year, month, day
        );

        dp.show();
    }

    private String formatYMD(int y, int m, int d) {
        return pad4(y) + "-" + pad2(m) + "-" + pad2(d);
    }

    private String pad2(int v) {
        return (v < 10 ? "0" : "") + v;
    }

    private String pad4(int v) {
        String s = String.valueOf(v);
        if (s.length() >= 4) return s;
        return ("0000" + s).substring(s.length());
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    // -------------------------------------------------
    // IMPORTANT: adjust these two methods to your model
    // -------------------------------------------------
    private String getIdSafe(Currency c) {
        if (c == null) return "";
        // عدّل حسب حقول Currency عندك:
        // مثال: return String.valueOf(c.id);
        // أو: return c.getId();
        try {
            return String.valueOf(c.getId());
        } catch (Exception e) {
            try {
                return String.valueOf(c.getId());
            } catch (Exception ignore) {}
        }
        return "";
    }

    private String getNameSafe(Currency c) {
        if (c == null) return "";
        // عدّل حسب حقول Currency عندك:
        // مثال: return c.getName();
        // أو: return c.title;
        try {
            String v = c.getName();
            return v == null ? "" : v;
        } catch (Exception e) {
            try {
                String v = c.getName();
                return v == null ? "" : v;
            } catch (Exception ignore) {}
        }
        return "";
    }

    private interface OnSelectItem {
        void onSelect(String id, String label);
    }

    private void showPopupMenu(View anchor, List<Currency> items, String selectedId, OnSelectItem cb) {
        if (items == null || items.isEmpty() || cb == null) return;

        PopupMenu pm = new PopupMenu(requireContext(), anchor);

        // Optional: force menu to appear under anchor (some devices place it nicely by default)
        try {
            pm.setGravity(Gravity.START);
        } catch (Exception ignore) {}

        int selectedIndex = -1;

        for (int i = 0; i < items.size(); i++) {
            Currency c = items.get(i);
            String id = getIdSafe(c);
            String name = getNameSafe(c);

            pm.getMenu().add(0, i, i, name);

            if (selectedId != null && selectedId.equals(id)) {
                selectedIndex = i;
            }
        }

        // Optional: mark selected item (not all OEMs show checkmark)
        if (selectedIndex >= 0) {
            try {
                pm.getMenu().getItem(selectedIndex).setCheckable(true);
                pm.getMenu().getItem(selectedIndex).setChecked(true);
            } catch (Exception ignore) {}
        }

        pm.setOnMenuItemClickListener(item -> {
            int index = item.getItemId();
            if (index >= 0 && index < items.size()) {
                Currency chosen = items.get(index);
                cb.onSelect(getIdSafe(chosen), getNameSafe(chosen));
                return true;
            }
            return false;
        });

        pm.show();
    }
}
