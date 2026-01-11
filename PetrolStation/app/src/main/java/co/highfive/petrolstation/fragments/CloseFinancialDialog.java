package co.highfive.petrolstation.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.adapters.CloseFundsSelectedAdapter;
import co.highfive.petrolstation.adapters.MultiSelectDropDownAdapter;
import co.highfive.petrolstation.databinding.DialogCloseFinancialBinding;
import co.highfive.petrolstation.listener.CloseFinancialListener;
import co.highfive.petrolstation.models.Currency;
import co.highfive.petrolstation.models.Fund;
import co.highfive.petrolstation.models.SelectedCloseFundItem;

public class CloseFinancialDialog extends DialogFragment {

    private static final String ARG_FUND = "arg_fund";
    private static final String ARG_FUND_TO = "arg_fund_to";
    private static final String ARG_PAYMENTS = "arg_payments";
    private ArrayList<co.highfive.petrolstation.models.FundPaymentSummary> paymentsSummary;
    private DialogCloseFinancialBinding binding;

    private Fund fund;
    private ArrayList<Currency> fundTo;

    private CloseFinancialListener listener;

    // Multi selection state
    private final HashSet<String> selectedFundToIds = new HashSet<>();
    private final ArrayList<SelectedCloseFundItem> selectedItems = new ArrayList<>();

    private CloseFundsSelectedAdapter selectedAdapter;

    public CloseFinancialDialog() { }

    public static CloseFinancialDialog newInstance(
            Fund fund,
            ArrayList<Currency> fundTo,
            ArrayList<co.highfive.petrolstation.models.FundPaymentSummary> paymentsSummary
    ) {
        CloseFinancialDialog dialog = new CloseFinancialDialog();
        Bundle b = new Bundle();
        b.putSerializable(ARG_FUND, (Serializable) fund);
        b.putSerializable(ARG_FUND_TO, (Serializable) fundTo);
        b.putSerializable(ARG_PAYMENTS, paymentsSummary);
        dialog.setArguments(b);
        dialog.setCancelable(false);
        return dialog;
    }

    public void setListener(CloseFinancialListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            try { fund = (Fund) args.getSerializable(ARG_FUND); } catch (Exception ignored) {}
            try { fundTo = (ArrayList<Currency>) args.getSerializable(ARG_FUND_TO); } catch (Exception ignored) {}
        }
        if (fundTo == null) fundTo = new ArrayList<>();

        try { paymentsSummary = (ArrayList<co.highfive.petrolstation.models.FundPaymentSummary>) args.getSerializable(ARG_PAYMENTS); }
        catch (Exception ignored) {}

        if (paymentsSummary == null) paymentsSummary = new ArrayList<>();

        Log.e("fundTo3",""+fundTo.size());
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = DialogCloseFinancialBinding.inflate(inflater, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        initViews();
        return binding.getRoot();
    }

    private void initViews() {
        co.highfive.petrolstation.adapters.FundPaymentsSummaryAdapter payAdapter =
                new co.highfive.petrolstation.adapters.FundPaymentsSummaryAdapter(paymentsSummary);

        androidx.recyclerview.widget.LinearLayoutManager lm =
                new androidx.recyclerview.widget.LinearLayoutManager(requireContext(),
                        androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
                        false);

        binding.recyclerPaymentSummary.setLayoutManager(lm);
        binding.recyclerPaymentSummary.setAdapter(payAdapter);

        binding.close.setOnClickListener(v -> dismissAllowingStateLoss());
        binding.hide.setOnClickListener(v -> dismissAllowingStateLoss());

        // Selected list
        selectedAdapter = new CloseFundsSelectedAdapter(selectedItems, item -> {
            // remove from list
            removeSelectedItem(item.fundId);
            // also remove from multi-select state
            selectedFundToIds.remove(item.fundId);
            updateBoxStatusSummaryText();
            selectedAdapter.notifyDataSetChanged();
        });

        binding.recyclerSelectedFunds.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerSelectedFunds.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerSelectedFunds.setAdapter(selectedAdapter);

        // Multi select dropdown
        binding.boxStatusLayout.setOnClickListener(v -> openMultiFundToDropdown());

        binding.save.setOnClickListener(v -> {
            String error = validate();
            if (error != null) {
                toast(error);
                return;
            }

            // Build request params exactly like postman
            HashMap<String, String> params = buildCloseParams();

            if (listener != null) {
                // Option A: pass params (recommended)
                listener.closeMulti(fund, params);
            }
        });
    }

    private String validate() {
        if (binding == null) return getString(R.string.general_error);

        if (selectedItems.isEmpty()) {
            return getString(R.string.select_box_transfere);
        }

        for (SelectedCloseFundItem item : selectedItems) {
            if (item == null) continue;
            if (TextUtils.isEmpty(item.fundId)) return getString(R.string.select_box_transfere);

            String amount = item.amount == null ? "" : item.amount.trim();
            if (amount.isEmpty()) return getString(R.string.enter_recevied_money);
        }

        return null;
    }

    private HashMap<String, String> buildCloseParams() {
        HashMap<String, String> params = new HashMap<>();

        // API requires id
        if (fund != null) {
            // حسب الموديل عندك: fund.getId() أو fund.id
            try { params.put("id", String.valueOf(fund.getId())); } catch (Exception e) {
                try { params.put("id", String.valueOf(fund.getId())); } catch (Exception ignored) {}
            }
        }

        params.put("notes", binding.notes.getText() == null ? "" : binding.notes.getText().toString().trim());

        // Build fund_to[i] and amount_close[i]
        for (int i = 0; i < selectedItems.size(); i++) {
            int idx = i + 1;
            SelectedCloseFundItem item = selectedItems.get(i);

            params.put("fund_to[" + idx + "]", item.fundId);
            params.put("amount_close[" + idx + "]", item.amount == null ? "" : item.amount.trim());
        }

        return params;
    }

    @SuppressLint("InflateParams")
    private void openMultiFundToDropdown() {
        if (getActivity() == null) return;

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = Objects.requireNonNull(inflater).inflate(R.layout.drop_down_list_multi, null);

        RecyclerView recyclerView = popupView.findViewById(R.id.recycler_view);

        int width = (int) (getActivity().getWindowManager().getDefaultDisplay().getWidth() * 0.75);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                width,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        Log.e("fundTo4",""+fundTo.size());
        MultiSelectDropDownAdapter adapter = new MultiSelectDropDownAdapter(
                fundTo,
                selectedFundToIds,
                (item, isSelected) -> {
                    if (isSelected) {
                        addSelectedItem(item);
                    } else {
                        removeSelectedItem(item.getId());
                    }
                    updateBoxStatusSummaryText();
                    selectedAdapter.notifyDataSetChanged();
                }
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(18f);
        }
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        popupWindow.showAsDropDown(binding.boxStatus);
    }

    private void addSelectedItem(Currency currency) {
        if (currency == null) return;
        String id = currency.getId();
        if (TextUtils.isEmpty(id)) return;

        // avoid duplicates
        for (SelectedCloseFundItem it : selectedItems) {
            if (it != null && id.equals(it.fundId)) return;
        }
        selectedItems.add(new SelectedCloseFundItem(id, currency.getName()));
    }

    private void removeSelectedItem(String fundId) {
        if (TextUtils.isEmpty(fundId)) return;

        for (int i = selectedItems.size() - 1; i >= 0; i--) {
            SelectedCloseFundItem it = selectedItems.get(i);
            if (it != null && fundId.equals(it.fundId)) {
                selectedItems.remove(i);
            }
        }
    }

    private void updateBoxStatusSummaryText() {
        if (binding == null) return;

        if (selectedItems.isEmpty()) {
            binding.boxStatus.setText(getString(R.string.select_box_transfere));
            return;
        }

        // Show short summary like: "صندوقين" أو "3 صناديق"
        int c = selectedItems.size();
        if (c == 1) binding.boxStatus.setText(selectedItems.get(0).fundName);
        else binding.boxStatus.setText(c + " " + getString(R.string.boxes_selected)); // add boxes_selected in strings
    }

    private void toast(String msg) {
        if (getContext() == null) return;
        try {
            android.widget.Toast.makeText(getContext(), msg, android.widget.Toast.LENGTH_LONG).show();
        } catch (Exception ignored) {}
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
