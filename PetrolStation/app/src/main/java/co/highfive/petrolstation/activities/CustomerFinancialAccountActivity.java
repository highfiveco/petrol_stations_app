package co.highfive.petrolstation.activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.adapters.CustomerFinancialAccountAdapter;
import co.highfive.petrolstation.customers.dto.CustomerFinancialAccountResponse;
import co.highfive.petrolstation.databinding.ActivityCustomerFinancialAccountBinding;
import co.highfive.petrolstation.fragments.DeleteDialog;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.models.Account;
import co.highfive.petrolstation.models.Setting;
import co.highfive.petrolstation.models.Transactions;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.ApiError;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;

import com.google.gson.reflect.TypeToken;

public class CustomerFinancialAccountActivity extends BaseActivity {

    private ActivityCustomerFinancialAccountBinding binding;

    private String accountId = "";
    private int deleteFinancial = 0;

    private Setting setting;
    private Account account;

    private CustomerFinancialAccountAdapter adapter;

    private int page = 1;
    private boolean isLoading = false;
    private boolean hasMore = true;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCustomerFinancialAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI(binding.mainLayout);


        readExtras();
        initRecycler();
        initRefresh();
        initClicks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshFirstPage(true);
    }

    private void readExtras() {
        Bundle extras = getIntent() != null ? getIntent().getExtras() : null;
        if (extras == null) return;
        accountId = safe(extras.getString("account_id"));
    }

    private void initRecycler() {
        adapter = new CustomerFinancialAccountAdapter(
                new ArrayList<>(),
                new CustomerFinancialAccountAdapter.Listener() {
                    @Override
                    public void onDeleteClicked(Transactions tx) {
                        if (tx == null) return;
                        if (deleteFinancial != 1) return;
                        confirmDeleteMove(tx.getId());
                    }

                    @Override
                    public void onPrintClicked(Transactions tx) {
                        if (tx == null) return;
                        if (!"1".equals(safe(tx.getIs_print()))) return;
                        printMoveRequest(tx.getId());
                    }

                    @Override
                    public void onAddReturnClicked(Transactions tx) {
                        if (tx == null) return;
                        Bundle b = new Bundle();
                        b.putString("account_id", accountId);
                        b.putString("parent_id", safe(tx.getId()));
                        moveToActivity(getApplicationContext(), AddReturnActivity.class, b, false, false);
                    }
                }
        );

        LinearLayoutManager lm = new LinearLayoutManager(this);
        binding.recycler.setLayoutManager(lm);
        binding.recycler.setAdapter(adapter);

        binding.recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@androidx.annotation.NonNull RecyclerView rv, int dx, int dy) {
                if (dy <= 0) return;
                if (isLoading || !hasMore) return;

                int lastVisible = lm.findLastVisibleItemPosition();
                if (lastVisible >= adapter.getItemCount() - 3) {
                    loadNextPage();
                }
            }
        });
    }

    private void initRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> refreshFirstPage(false));
    }

    private void initClicks() {
        binding.icHome.setOnClickListener(v ->
                moveToActivity(getApplicationContext(), MainActivity.class, null, false, true)
        );
        binding.icBack.setOnClickListener(v -> finish());

        binding.icAddWhite.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putString("account_id", accountId);
            moveToActivity(this, AddFinancialTransactionActivity.class, b, false);
        });

        binding.phone.setOnClickListener(v -> {
            if (account != null && !safe(account.getMobile()).trim().isEmpty()) {
                call(account.getMobile());
            }
        });
    }

    private void refreshFirstPage(boolean showDialog) {
        page = 1;
        hasMore = true;
        adapter.clear();
        fetchPage(page, showDialog);
    }

    private void loadNextPage() {
        if (!hasMore) return;
        fetchPage(page + 1, false);
    }

    private void fetchPage(int targetPage, boolean showDialog) {
        if (accountId.trim().isEmpty()) {
            toast(getString(R.string.general_error));
            return;
        }

        isLoading = true;
        if (showDialog) showProgressHUD();

        Map<String, String> params = ApiClient.mapOf(
                "page", String.valueOf(targetPage),
                "account_id", accountId
        );

        Type type = new TypeToken<BaseResponse<CustomerFinancialAccountResponse>>() {}.getType();

        apiClient.request(
                co.highfive.petrolstation.hazemhamadaqa.Http.Constant.REQUEST_GET,
                Endpoints.CUSTOMERS_VIEWFINANCIALMOVE,
                params,
                null,
                type,
                0,
                new ApiCallback<CustomerFinancialAccountResponse>() {
                    @Override
                    public void onSuccess(CustomerFinancialAccountResponse data, String msg, String rawJson) {
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        isLoading = false;

                        if (data == null) {
                            toast(getString(R.string.general_error));
                            return;
                        }

                        setting = data.setting;
                        account = data.account;
                        deleteFinancial = data.delete_financial;

                        updateHeader(account);

                        List<Transactions> newItems =
                                (data.transactions != null && data.transactions.data != null)
                                        ? data.transactions.data
                                        : new ArrayList<>();

                        // ✅ pagination الأفضل: اعتمد next_page_url أو last_page
                        if (data.transactions == null || data.transactions.next_page_url == null) {
                            hasMore = false;
                        }

                        if (!newItems.isEmpty()) {
                            page = targetPage;
                            adapter.setDeleteFinancial(deleteFinancial);
                            adapter.setAccountId(accountId);
                            adapter.setSetting(setting);
                            adapter.addAll(newItems);
                        }

                        // زر + (حسب صلاحية السيرفر)
                        binding.icAddWhite.setVisibility(deleteFinancial == 1 ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onError(ApiError error) {
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        isLoading = false;
                        toast(error.message);
                    }

                    @Override
                    public void onUnauthorized(String rawJson) {
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        isLoading = false;
                        logout();
                    }

                    @Override
                    public void onNetworkError(String reason) {
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        isLoading = false;
                        toast(R.string.no_internet);
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        isLoading = false;
                        toast(getString(R.string.general_error));
                    }
                }
        );
    }

    private void updateHeader(Account a) {
        if (a == null) return;
        binding.name.setText(safe(a.getAccount_name()));
        binding.phone.setText(safe(a.getMobile()));
        binding.amount.setText(safe(""+a.getBalance()));
    }

    private void confirmDeleteMove(String moveId) {
        DeleteDialog dialog =new DeleteDialog();
        dialog.setCancelable(false);
        dialog.setSuccessListener(success -> {
            if (success) {
                dialog.dismiss();
                financialDeleteMove(moveId);
            }
        });
        dialog.show(getSupportFragmentManager(), "DeleteDialog");
    }


    private void printMoveRequest(String moveId) {
        // انت جاهز تحطه لاحقاً حسب endpoint تبع printMove عندك
        toast(getString(R.string.general_error));
    }

    private void financialDeleteMove(String moveId) {
        showProgressHUD();

        Map<String, String> params = ApiClient.mapOf(
                "id", safe(moveId)
        );

        Type type = new TypeToken<BaseResponse<Object>>() {}.getType();

        apiClient.request(
                co.highfive.petrolstation.hazemhamadaqa.Http.Constant.REQUEST_POST,
                Endpoints.FINANCIAL_DELETEMOVE,
                params,
                null,
                type,
                0,
                new ApiCallback<Object>() {
                    @Override
                    public void onSuccess(Object data, String msg, String rawJson) {
                        hideProgressHUD();

                        // بالقديم: toast(message) ثم getData(true)
                        toast(msg != null && !msg.trim().isEmpty() ? msg : getString(R.string.done));
                        refreshFirstPage(true);
                    }

                    @Override
                    public void onError(ApiError error) {
                        hideProgressHUD();
                        toast(error != null && error.message != null
                                ? error.message
                                : getString(R.string.general_error));
                    }

                    @Override
                    public void onUnauthorized(String rawJson) {
                        hideProgressHUD();
                        logout();
                    }

                    @Override
                    public void onNetworkError(String reason) {
                        hideProgressHUD();
                        toast(R.string.no_internet);
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        hideProgressHUD();
                        toast(getString(R.string.general_error));
                    }
                }
        );
    }



}
