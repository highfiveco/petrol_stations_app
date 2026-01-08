package co.highfive.petrolstation.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.adapters.CustomerRemindersAdapter;
import co.highfive.petrolstation.customers.dto.CustomerRemindersResponse;
import co.highfive.petrolstation.databinding.ActivityCustomerRemindersBinding;
import co.highfive.petrolstation.fragments.AddReminderDialog;
import co.highfive.petrolstation.fragments.DeleteDialog;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.models.Account;
import co.highfive.petrolstation.models.Setting;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.ApiError;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;

import com.google.gson.reflect.TypeToken;

import co.highfive.petrolstation.models.Reminder; // عدّل الباكدج حسب مشروعك

public class CustomerRemindersActivity extends BaseActivity {

    private ActivityCustomerRemindersBinding binding;

    private String customerId = "";  // كان id
    private String accountId = "";   // كان account_id (إذا لزمك لاحقاً)

    private Account account;
    private Setting setting;

    private CustomerRemindersAdapter adapter;

    private int page = 1;
    private boolean isLoading = false;
    private boolean hasMore = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCustomerRemindersBinding.inflate(getLayoutInflater());
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

        customerId = safe(extras.getString("id"));
        accountId = safe(extras.getString("account_id"));
    }

    private void initRecycler() {
        adapter = new CustomerRemindersAdapter(
                this,
                new ArrayList<>(),
                new CustomerRemindersAdapter.Listener() {
                    @Override
                    public void onDeleteClicked(Reminder r) {
                        if (r == null) return;
                        confirmDeleteReminder(safe(r.getId()));
                    }
                }
        );

        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);

        binding.recycler.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@androidx.annotation.NonNull androidx.recyclerview.widget.RecyclerView rv, int dx, int dy) {
                if (dy <= 0) return;
                if (isLoading || !hasMore) return;

                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                if (lm == null) return;

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

        binding.icAddWhite.setOnClickListener(v -> openAddReminderDialog());
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
        if (customerId.trim().isEmpty()) {
            toast(getString(R.string.general_error));
            return;
        }

        isLoading = true;
        if (showDialog) showProgressHUD();

        Map<String, String> params = ApiClient.mapOf(
                "page", String.valueOf(targetPage),
                "id", customerId
        );

        Type type = new TypeToken<BaseResponse<CustomerRemindersResponse>>() {}.getType();

        apiClient.request(
                co.highfive.petrolstation.hazemhamadaqa.Http.Constant.REQUEST_GET,
                Endpoints.CUSTOMERS_GETREMINDERS, // TODO: endpoint
                params,
                null,
                type,
                0,
                new ApiCallback<CustomerRemindersResponse>() {
                    @Override
                    public void onSuccess(CustomerRemindersResponse data, String msg, String rawJson) {
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        isLoading = false;

                        if (data == null) {
                            toast(getString(R.string.general_error));
                            return;
                        }

                        account = data.customer;
                        setting = data.setting;

                        updateHeader(account);

                        // update app check (لو عندك)
                        if (setting != null && setting.getVersion_app() != null) {
                            // if (!setting.getVersion_app().equals(BuildConfig.VERSION_NAME)) { openUpdateAppDialog(...); }
                        }

                        List<Reminder> newItems = (data.reminders != null && data.reminders.data != null)
                                ? data.reminders.data
                                : new ArrayList<>();

                        if (newItems.isEmpty()) {
                            hasMore = false;
                        } else {
                            page = targetPage;
                            adapter.addAll(newItems);

                            // pagination based on last_page if available
                            if (data.reminders != null && data.reminders.last_page > 0) {
                                hasMore = page < data.reminders.last_page;
                            } else if (data.reminders != null) {
                                hasMore = data.reminders.next_page_url != null;
                            }
                        }
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

    private void updateHeader(Account account) {
        if (account == null) return;
        binding.name.setText(safe(account.getName()));
        binding.phone.setText(safe(account.getMobile()));
        binding.amount.setText(safe(""+account.getBalance()));
    }

    private void openAddReminderDialog() {
        AddReminderDialog  dialog = AddReminderDialog.newInstance();
        dialog.setCancelable(false);
        dialog.setListener((text, date) -> addReminderRequest(date, text,dialog));
        dialog.show(getSupportFragmentManager(), "AddReminderDialogNew");
    }

    private void confirmDeleteReminder(String reminderId) {
         DeleteDialog dialog =
                new  DeleteDialog();
        dialog.setCancelable(false);
        dialog.setSuccessListener(success -> {
            if (success) {
                dialog.dismiss();
                deleteReminderRequest(reminderId);
            }
        });
        dialog.show(getSupportFragmentManager(), "DeleteDialog");
    }

    private void deleteReminderRequest(String reminderId) {
        showProgressHUD();

        Map<String, String> params = ApiClient.mapOf("id", safe(reminderId));
        Type type = new TypeToken<BaseResponse<Object>>() {}.getType();

        apiClient.request(
                co.highfive.petrolstation.hazemhamadaqa.Http.Constant.REQUEST_POST,
                Endpoints.CUSTOMERS_DELETEREMINDERS, // TODO: endpoint
                params,
                null,
                type,
                0,
                new ApiCallback<Object>() {
                    @Override
                    public void onSuccess(Object data, String msg, String rawJson) {
                        hideProgressHUD();
                        toast(msg != null ? msg : getString(R.string.done));
                        refreshFirstPage(true);
                    }

                    @Override
                    public void onError(ApiError error) {
                        hideProgressHUD();
                        toast(error.message);
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

    private void addReminderRequest(String date, String text, AddReminderDialog dialog) {
        showProgressHUD();

        Map<String, String> params = ApiClient.mapOf(
                "id", safe(customerId),
                "date", safe(date),
                "text", safe(text)
        );

        Type type = new TypeToken<BaseResponse<Object>>() {}.getType();

        apiClient.request(
                co.highfive.petrolstation.hazemhamadaqa.Http.Constant.REQUEST_POST,
                Endpoints.CUSTOMERS_ADDREMINDERS, // TODO: endpoint
                params,
                null,
                type,
                0,
                new ApiCallback<Object>() {
                    @Override
                    public void onSuccess(Object data, String msg, String rawJson) {
                        hideProgressHUD();
                        toast(msg != null ? msg : getString(R.string.done));
                        dialog.dismiss();
                        refreshFirstPage(true);
                    }

                    @Override
                    public void onError(ApiError error) {
                        hideProgressHUD();
                        toast(error.message);
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
