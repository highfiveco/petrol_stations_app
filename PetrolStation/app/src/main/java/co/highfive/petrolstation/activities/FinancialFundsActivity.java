package co.highfive.petrolstation.activities;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.highfive.petrolstation.BuildConfig;
import co.highfive.petrolstation.R;
import co.highfive.petrolstation.adapters.FinancialFundsAdapter;
import co.highfive.petrolstation.databinding.ActivityFinancialFundsBinding;
import co.highfive.petrolstation.financial.dto.FinancialFundsResponse;
import co.highfive.petrolstation.fragments.FinancialFundsFilterDialog;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.listener.FinancialFundFilter;
import co.highfive.petrolstation.models.Currency;
import co.highfive.petrolstation.models.Fund;
import co.highfive.petrolstation.models.Setting;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;

public class FinancialFundsActivity extends BaseActivity {

    private ActivityFinancialFundsBinding binding;
    private FinancialFundsAdapter adapter;

    private final ArrayList<Fund> funds = new ArrayList<>();

    // filters
    private String currency;
    private String boxStatus;
    private String userId;
    private String dateFrom;
    private String dateTo;

    private ArrayList<Currency> currencies = new ArrayList<>();
    private ArrayList<Currency> statusFunds = new ArrayList<>();
    private ArrayList<Currency> users = new ArrayList<>();
    private ArrayList<Currency> fundTo = new ArrayList<>();

    // pagination
    private int page = 1;
    private boolean isLoading = false;
    private boolean hasMore = true;

    private int closeFund = 0;
    private int reportFund = 0;
    private int viewUsers = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFinancialFundsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupUI(binding.mainLayout);

        initViews();
    }


    private void initViews() {

//        errorLogger("funds",""+funds.size());
        errorLogger("fundTo5",""+fundTo.size());
        adapter = new FinancialFundsAdapter(
                this,
                fundTo,
                funds,
                success -> refreshFirstPage(true)
        );

        binding.recyclerCustomers.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerCustomers.setAdapter(adapter);

        binding.icBack.setOnClickListener(v -> finish());
        binding.icHome.setOnClickListener(v ->
                moveToActivity(this, MainActivity.class, null, false, true)
        );

        binding.icFilterWhite.setOnClickListener(v -> openFilterDialog());

        binding.swipeRefreshLayout.setOnRefreshListener(() -> refreshFirstPage(false));


        binding.recyclerCustomers.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView rv, int dx, int dy) {
                if (dy <= 0 || isLoading || !hasMore) return;

                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                if (lm != null && lm.findLastVisibleItemPosition() >= adapter.getItemCount() - 3) {
                    loadPage(page + 1, false);
                }
            }
        });

        refreshFirstPage(true);

    }

    private void openFilterDialog() {
        FinancialFundsFilterDialog dialog =
                FinancialFundsFilterDialog.newInstance(
                        currency, boxStatus, userId, dateFrom, dateTo,
                        statusFunds, currencies, users, viewUsers
                );

        dialog.setListener((c, s, u, from, to) -> {
            currency = c;
            boxStatus = s;
            userId = u;
            dateFrom = from;
            dateTo = to;
            refreshFirstPage(true);
        });

        dialog.show(getSupportFragmentManager(), "FinancialFundsFilterDialog");
    }

    private void refreshFirstPage(boolean showDialog) {
        page = 1;
        hasMore = true;
        funds.clear();
        adapter.notifyDataSetChanged();
        loadPage(page, showDialog);
    }

    private boolean hasAnyFilter() {
        return currency != null || boxStatus != null || userId != null || dateFrom != null || dateTo != null;
    }

    private void loadPage(int targetPage, boolean showDialog) {

        if (isLoading) return;

        isLoading = true;
        if (showDialog) showProgressHUD();

        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(targetPage));
        if (currency != null) params.put("currency", currency);
        if (boxStatus != null) params.put("status", boxStatus);
        if (userId != null) params.put("user_id", userId);
        if (dateFrom != null) params.put("from_date", dateFrom);
        if (dateTo != null) params.put("to_date", dateTo);

        Type type = new TypeToken<BaseResponse<FinancialFundsResponse>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.FUND,
                params,
                null,
                type,
                0,
                new ApiCallback<FinancialFundsResponse>() {
                    @Override
                    public void onSuccess(FinancialFundsResponse data, String msg, String raw) {

                        hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        isLoading = false;

                        if (data == null) return;

                        closeFund = data.close_fund;
                        reportFund = data.report_fund;
                        viewUsers = data.view_users;

                        getSessionManager().setInt(getSessionKeys().close_fund, closeFund);
                        getSessionManager().setInt(getSessionKeys().report_fund, reportFund);
                        getSessionManager().setInt(getSessionKeys().view_users, viewUsers);

                        currencies = (ArrayList<Currency>) data.currency;
                        statusFunds = (ArrayList<Currency>) data.status_fund;
                        users = (ArrayList<Currency>) data.users;
                        fundTo = (ArrayList<Currency>) data.fund_to;
                        errorLogger("fundTo6",""+fundTo.size());
                        Setting setting = data.setting;
                        if (setting != null && !setting.getVersion_app().equals(BuildConfig.VERSION_NAME)) {
                            openUpdateAppDialog(
                                    setting.getUpdate_title(),
                                    setting.getUpdate_description(),
                                    setting.getUrl_app()
                            );
                        }

                        List<Fund> list = data.fund.data;
                        adapter.setFunTo(fundTo);
                        if (list != null && !list.isEmpty()) {
                            funds.addAll(list);
                            adapter.notifyDataSetChanged();
                            page = targetPage;
                            hasMore = list.size() >= 10;
                        } else {
                            hasMore = false;
                        }
                    }

                    @Override public void onError(co.highfive.petrolstation.network.ApiError error) {
                        finishLoad();
                        toast(error.message);
                    }

                    @Override public void onUnauthorized(String raw) {
                        finishLoad();
                        logout();
                    }

                    @Override public void onNetworkError(String reason) {
                        finishLoad();
                        toast(R.string.no_internet);
                    }

                    @Override public void onParseError(String raw, Exception e) {
                        finishLoad();
                        toast(R.string.general_error);
                    }

                    private void finishLoad() {
                        hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        isLoading = false;
                    }
                }
        );
    }


}
