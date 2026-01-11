package co.highfive.petrolstation.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.adapters.CustomerInvoicesAdapter;
import co.highfive.petrolstation.customers.dto.InvoiceDto;
import co.highfive.petrolstation.customers.dto.InvoicesPaginationDto;
import co.highfive.petrolstation.databinding.ActivityCustomerInvoicesBinding;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.models.Setting;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.ApiError;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;

public class CustomerInvoicesActivity extends BaseActivity {

    private ActivityCustomerInvoicesBinding binding;

    private String accountId = "";
    private String customerName = "";
    private String customerMobile = "";

    private final ArrayList<InvoiceDto> items = new ArrayList<>();
    private CustomerInvoicesAdapter adapter;

    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean hasNextPage = true;

    private boolean isCustomerMode = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCustomerInvoicesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI(binding.mainLayout);

        readExtras();
        initHeader();
        initRecycler();
        initRefresh();
        initClicks();

        fetchInvoices(true, 1, true);
    }

    private void readExtras() {
        Bundle extras = getIntent() != null ? getIntent().getExtras() : null;
        if (extras == null) {
            isCustomerMode = false;
            return;
        }

        accountId = safe(extras.getString("account_id"));
        customerName = safe(extras.getString("name"));
        customerMobile = safe(extras.getString("mobile"));

        isCustomerMode = !accountId.trim().isEmpty();
    }

    private void initHeader() {
        if (isCustomerMode) {
            binding.headerInfoLayout.setVisibility(View.VISIBLE);
            binding.name.setText(customerName);
            binding.phone.setText(customerMobile);


            binding.phone.setOnClickListener(v -> {
                if (!customerMobile.trim().isEmpty()) call(customerMobile);
            });
        } else {
            binding.headerInfoLayout.setVisibility(View.GONE);
        }
    }

    private void initClicks() {
        binding.icHome.setOnClickListener(v ->
                moveToActivity(getApplicationContext(), MainActivity.class, null, false, true)
        );
        binding.icBack.setOnClickListener(v -> finish());
    }

    private void initRecycler() {
        adapter = new CustomerInvoicesAdapter(items, new CustomerInvoicesAdapter.Listener() {
            @Override
            public void onView(InvoiceDto invoice) {
                openInvoiceDetails(invoice);
            }

            @Override
            public void onPrint(InvoiceDto invoice) {
                Setting setting = null;

                if (getAppData() != null) {
                    setting = getAppData().getSetting(); // عدّل حسب مشروعك
                }

                if (setting == null) {
                    toast(getString(R.string.general_error));
                    return;
                }

                printInvoice(setting, invoice);
            }
        });

        adapter.setHideCustomerNameInItem(isCustomerMode);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        binding.recycler.setLayoutManager(lm);
        binding.recycler.setAdapter(adapter);

        binding.recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@androidx.annotation.NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);

                if (dy <= 0) return;
                if (isLoading || !hasNextPage) return;

                int visibleCount = lm.getChildCount();
                int totalCount = lm.getItemCount();
                int firstVisible = lm.findFirstVisibleItemPosition();

                if ((visibleCount + firstVisible) >= (totalCount - 3)) {
                    fetchInvoices(false, currentPage + 1, false);
                }
            }
        });
    }

    private void initRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> fetchInvoices(false, 1, true));
    }

    // ============================================================
    // API
    // ============================================================
    private void fetchInvoices(boolean showDialog, int page, boolean isReset) {
        if (isLoading) return;

        isLoading = true;
        if (showDialog) showProgressHUD();
        binding.swipeRefreshLayout.setRefreshing(!showDialog);

        Map<String, String> params;

        if (isCustomerMode) {
            params = ApiClient.mapOf(
                    "page", String.valueOf(page),
                    "account_id", accountId
            );
        } else {
            params = ApiClient.mapOf(
                    "page", String.valueOf(page)
            );
        }

        Type type = new TypeToken<BaseResponse<InvoicesPaginationDto>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.CUSTOMERS_GETINVOICES, // لازم يكون معرف عندكم
                params,
                null,
                type,
                0,
                new ApiCallback<InvoicesPaginationDto>() {

                    @Override
                    public void onSuccess(InvoicesPaginationDto data, String msg, String rawJson) {
                        isLoading = false;
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);

                        if (data == null) {
                            toast(getString(R.string.general_error));
                            return;
                        }

                        if(data.data.size() >0){
                            if(data.data.get(0).account.getBalance() != null){
                                binding.amount.setText(""+data.data.get(0).account.getBalance());
                            }else if(data.data.get(0).account.getCredit() != null && data.data.get(0).account.getDepit() != null){
                                double balance  = data.data.get(0).account.getCredit() - data.data.get(0).account.getDepit();
                                binding.amount.setText(""+balance);
                            }
                        }


                        List<InvoiceDto> list = (data.data != null) ? data.data : new ArrayList<>();

                        if (isReset) {
                            items.clear();
                            adapter.setItems(list);
                            currentPage = (data.current_page != null) ? data.current_page : 1;
                        } else {
                            adapter.addItems(list);
                            currentPage = (data.current_page != null) ? data.current_page : page;
                        }

                        hasNextPage = data.next_page_url != null && !data.next_page_url.trim().isEmpty();
                    }

                    @Override
                    public void onError(ApiError error) {
                        isLoading = false;
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        toast(error != null ? error.message : getString(R.string.general_error));
                    }

                    @Override
                    public void onUnauthorized(String rawJson) {
                        isLoading = false;
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        logout();
                    }

                    @Override
                    public void onNetworkError(String reason) {
                        isLoading = false;
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        toast(R.string.no_internet);
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        isLoading = false;
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        errorLogger("CustomerInvoicesParseError", e.getMessage() == null ? "null" : e.getMessage());
                        toast(getString(R.string.general_error));
                    }
                }
        );
    }

    private void openInvoiceDetails(InvoiceDto inv) {
        if (inv == null) return;

        Bundle b = new Bundle();
        b.putInt("invoice_id", inv.id);
        b.putString("invoice_no", inv.invoice_no);
        b.putString("date", inv.date);
        b.putString("statement", inv.statement);
        b.putString("total", inv.total != null ? String.valueOf(inv.total) : "");
        b.putString("notes", inv.notes);

        // If you want later
         b.putString("raw_invoice_json", getGson().toJson(inv));

        moveToActivity(CustomerInvoicesActivity.this, InvoiceDetailsActivity.class, b, false);
    }
}
