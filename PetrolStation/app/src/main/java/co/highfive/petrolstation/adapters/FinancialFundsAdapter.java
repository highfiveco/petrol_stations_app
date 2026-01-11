package co.highfive.petrolstation.adapters;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.databinding.ItemFinancialFundBinding;
import co.highfive.petrolstation.fragments.CloseFinancialDialog;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.listener.SuccessListener;
import co.highfive.petrolstation.models.Currency;
import co.highfive.petrolstation.models.Fund;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.Endpoints;

public class FinancialFundsAdapter extends RecyclerView.Adapter<FinancialFundsAdapter.VH> {

    private final List<Fund> items;
    private final BaseActivity activity;
    private final SuccessListener refreshListener;
    ArrayList<Currency> fundTo;
    public FinancialFundsAdapter(
            BaseActivity activity,
            ArrayList<Currency> fundTo,
            List<Fund> items,
            SuccessListener refreshListener
    ) {
        this.fundTo = fundTo;
        this.activity = activity;
        this.items = items;
        this.refreshListener = refreshListener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFinancialFundBinding b = ItemFinancialFundBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        h.bind(items.get(position),fundTo);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class VH extends RecyclerView.ViewHolder {

        private final ItemFinancialFundBinding b;

        VH(ItemFinancialFundBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        void bind(Fund fund,ArrayList<Currency> fundTo1) {

            b.name.setText(fund.getName());
            b.currency.setText(fund.getName_currency());
            b.fundOpenDate.setText(fund.getOpen_date());
            b.fundCloseDate.setText(fund.getClose_date());

            try {
                float total = Float.parseFloat(fund.getDepit())
                        - Float.parseFloat(fund.getCredit());
                b.total.setText(activity.getString(R.string.total) + "\n" + total);
            } catch (Exception e) {
                b.total.setText("0");
            }

            b.totalRevenue.setText(activity.getString(R.string.total_revenue) + "\n" + fund.getDepit());
            b.totalExpenses.setText(activity.getString(R.string.total_expenses) + "\n" + fund.getCredit());
            b.receivedMoney.setText(activity.getString(R.string.received_money) + "\n" + fund.getAmount_close());

            boolean canClose = activity.getSessionManager()
                    .getInt(activity.getSessionKeys().close_fund) == 1;

            boolean canReport = activity.getSessionManager()
                    .getInt(activity.getSessionKeys().report_fund) == 1;

            b.closeLayout.setVisibility(canClose ? android.view.View.VISIBLE : android.view.View.GONE);
            b.reportLayout.setVisibility(canReport ? android.view.View.VISIBLE : android.view.View.GONE);

            if (fund.getClose_date() != null) {
                b.closeLayout.setAlpha(0.5f);
            }

            b.reportLayout.setOnClickListener(v -> openReport(fund));
            Log.e("fundTo2",""+fundTo1.size());
            b.closeLayout.setOnClickListener(v -> {
                if (fund.getClose_date() != null) {
                    activity.toast(R.string.fund_already_closed);
                    return;
                }
                requestFundTotalPayments(fund, fundTo1);
            });
        }



        private void openReport(Fund fund) {
            if (fund.getUrl() == null || fund.getUrl().trim().isEmpty()) {
                activity.toast(R.string.report_not_found);
                return;
            }

            try {
                Uri uri = Uri.parse(fund.getUrl());

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                Intent chooser = Intent.createChooser(intent, activity.getString(R.string.open_pdf));
                activity.startActivity(chooser);

            } catch (Exception e) {
                activity.toast(activity.getString(R.string.general_error));
            }
        }
    }
    public void setFunTo(ArrayList<Currency> fundTo){
        this.fundTo =  fundTo;
    }

    private void requestCloseFund(HashMap<String, String> params, CloseFinancialDialog dialog) {

        // Most projects: apiClient exists in BaseActivity
        // If not accessible, tell me how you access it and I will adjust.
        if (activity.apiClient == null) {
            activity.toast(R.string.general_error);
            return;
        }

        Type type = new TypeToken<BaseResponse<Object>>() {}.getType();

        activity.showProgressHUD();

        activity.apiClient.request(
                Constant.REQUEST_POST,
                Endpoints.FUND_CLOSE, // تأكد أنه موجود عندك
                params,
                null,
                type,
                0,
                new ApiCallback<Object>() {

                    @Override
                    public void onSuccess(Object data, String msg, String raw) {
                        activity.hideProgressHUD();

                        if (dialog != null) {
                            dialog.dismissAllowingStateLoss();
                        }

                        // msg is "تمت العملية بنجاح"
                        if (msg != null && !msg.trim().isEmpty()) {
                            activity.toast(msg);
                        } else {
                            activity.toast(R.string.success);
                        }

                        // Close dialog will be dismissed from inside dialog OR you can dismiss it there
                        refreshListener.success(true);
                    }

                    @Override
                    public void onError(co.highfive.petrolstation.network.ApiError error) {
                        activity.hideProgressHUD();
                        activity.toast(error.message);
                    }

                    @Override
                    public void onUnauthorized(String raw) {
                        activity.hideProgressHUD();
                        activity.logout();
                    }

                    @Override
                    public void onNetworkError(String reason) {
                        activity.hideProgressHUD();
                        activity.toast(R.string.no_internet);
                    }

                    @Override
                    public void onParseError(String raw, Exception e) {
                        activity.hideProgressHUD();
                        activity.toast(R.string.general_error);
                    }
                }
        );
    }

    private void requestFundTotalPayments(Fund fund, ArrayList<Currency> fundTo) {

        if (activity.apiClient == null) {
            activity.toast(R.string.general_error);
            return;
        }

        HashMap<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(fund.getId()));

        Type type = new TypeToken<BaseResponse<ArrayList<co.highfive.petrolstation.models.FundPaymentSummary>>>() {}.getType();

        activity.showProgressHUD();

        activity.apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.FUND_TOTAL_PAYMENTS,
                params,
                null,
                type,
                0,
                new ApiCallback<ArrayList<co.highfive.petrolstation.models.FundPaymentSummary>>() {

                    @Override
                    public void onSuccess(ArrayList<co.highfive.petrolstation.models.FundPaymentSummary> data, String msg, String raw) {
                        activity.hideProgressHUD();

                        ArrayList<co.highfive.petrolstation.models.FundPaymentSummary> list =
                                data == null ? new ArrayList<>() : data;

                        openCloseDialogAfterPaymentsLoaded(fund, fundTo, list);
                    }

                    @Override
                    public void onError(co.highfive.petrolstation.network.ApiError error) {
                        activity.hideProgressHUD();
                        activity.toast(error.message);
                    }

                    @Override
                    public void onUnauthorized(String raw) {
                        activity.hideProgressHUD();
                        activity.logout();
                    }

                    @Override
                    public void onNetworkError(String reason) {
                        activity.hideProgressHUD();
                        activity.toast(R.string.no_internet);
                    }

                    @Override
                    public void onParseError(String raw, Exception e) {
                        activity.hideProgressHUD();
                        activity.toast(R.string.general_error);
                    }
                }
        );
    }

    private void openCloseDialogAfterPaymentsLoaded(
            Fund fund,
            ArrayList<Currency> fundTo,
            ArrayList<co.highfive.petrolstation.models.FundPaymentSummary> payments
    ) {
        CloseFinancialDialog dialog = CloseFinancialDialog.newInstance(fund, fundTo, payments);
        dialog.setListener((f, params) -> requestCloseFund(params, dialog));
        dialog.show(activity.getSupportFragmentManager(), "CloseFinancialDialog");
    }


}
