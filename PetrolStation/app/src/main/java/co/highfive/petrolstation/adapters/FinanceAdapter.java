package co.highfive.petrolstation.adapters;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.activities.AddFinancialTransactionActivity;
import co.highfive.petrolstation.activities.CustomerFinancialAccountActivity;
import co.highfive.petrolstation.databinding.FinanceViewLayoutBinding;
import co.highfive.petrolstation.fragments.EditNameDialog;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.listener.EditNameListener;
import co.highfive.petrolstation.models.Account;

public class FinanceAdapter extends RecyclerView.Adapter<FinanceAdapter.VH> {

    public interface IntProvider { int get(); }

    private final List<Account> items;
    private final IntProvider printPermission;
    private final IntProvider updatePermission;
    private final IntProvider viewPermission;
    private final BaseActivity baseActivity;
    private final FinanceActions actions;
    EditNameDialog editNameDialog;
    public interface FinanceActions {
        void onEditNameRequested(Account account, int position, String newName, Runnable onDoneUiUpdate,EditNameDialog editNameDialog);
    }

    public FinanceAdapter(List<Account> items,
                          IntProvider printPermission,
                          IntProvider updatePermission,
                          IntProvider viewPermission,
                          BaseActivity baseActivity,
                          FinanceActions actions) {
        this.items = items;
        this.printPermission = printPermission;
        this.updatePermission = updatePermission;
        this.viewPermission = viewPermission;
        this.baseActivity = baseActivity;
        this.actions = actions;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FinanceViewLayoutBinding b = FinanceViewLayoutBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Account a = items.get(position);

        h.binding.name.setText(s(a.getAccount_name()));
        h.binding.phone.setText(s(a.getMobile()));
        h.binding.currency.setText(s(a.getName_currency()));
        h.binding.accountType.setText(s(a.getAccount_type()));

        h.binding.phone.setOnClickListener(v -> {
            if (a.getMobile() != null) baseActivity.call(a.getMobile());
        });

        h.binding.addCustomerPayment.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putString("account_id", s(a.getId()));
            baseActivity.moveToActivity(baseActivity, AddFinancialTransactionActivity.class, b, false);
        });

        h.binding.editNameLayout.setOnClickListener(v -> {
            if (updatePermission.get() != 1) {
                baseActivity.toast(baseActivity.getString(R.string.no_permissno));
                return;
            }

            openEditNameDialog(a, nameVal -> {
                if (actions == null) return;

                int pos = h.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                actions.onEditNameRequested(a, pos, nameVal, () -> {
                    // UI update after success (only)
                    h.binding.name.setText(nameVal);
                },editNameDialog);
            });
        });

        h.binding.financeAccounts.setOnClickListener(v -> {
            if (viewPermission.get() != 1) {
                baseActivity.toast(baseActivity.getString(R.string.no_permissno));
                return;
            }
            Bundle b = new Bundle();
            b.putString("account_id", s(a.getId()));
            baseActivity.moveToActivity(baseActivity, CustomerFinancialAccountActivity.class, b, false);
        });

        h.binding.icExcelFile.setOnClickListener(v -> openFileIfAllowed(a.getExcel_url()));
        h.binding.icPdfFile.setOnClickListener(v -> openFileIfAllowed(a.getPdf_url()));
    }

    private void openFileIfAllowed(String url) {
        if (printPermission.get() != 1) {
            baseActivity.toast(baseActivity.getString(R.string.no_permissno));
            return;
        }
        try {
            String token = baseActivity.getSessionManager().getString(baseActivity.getSessionKeys().token);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url + "?Authorization=" + token));
            Bundle headers = new Bundle();
            headers.putString("Authorization", token);
            intent.putExtra(Browser.EXTRA_HEADERS, headers);
            baseActivity.startActivity(intent);
        } catch (Exception ignored) {}
    }

    private void openEditNameDialog(Account account, EditNameListener listener) {
        editNameDialog = new EditNameDialog(account.getAccount_name());
        editNameDialog.setCancelable(false);
        editNameDialog.setEditNameListener(listener);
        editNameDialog.show(baseActivity.getSupportFragmentManager(), "EditNameDialog");
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final FinanceViewLayoutBinding binding;
        VH(FinanceViewLayoutBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }

    private static String s(Object o) { return o == null ? "" : String.valueOf(o); }
    public void updateNameById(String id, String newName) {
        if (id == null) return;

        for (int i = 0; i < items.size(); i++) {
            Account a = items.get(i);
            if (a != null && id.equals(a.getId())) {
                a.setAccount_name(newName);
                notifyItemChanged(i);
                return;
            }
        }
        notifyDataSetChanged();
    }



}
