package co.highfive.petrolstation.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import co.highfive.petrolstation.BuildConfig;
import co.highfive.petrolstation.R;
import co.highfive.petrolstation.adapters.DropDownAdapter;
import co.highfive.petrolstation.adapters.FinanceAdapter;
import co.highfive.petrolstation.customers.dto.CompanySettingResponse;
import co.highfive.petrolstation.customers.dto.GetAccountsResponse;
import co.highfive.petrolstation.databinding.ActivityFinanceBinding;
import co.highfive.petrolstation.fragments.AddFinanceAccountDialog;
import co.highfive.petrolstation.fragments.EditNameDialog;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.listener.AddFinanceAccountListener;
import co.highfive.petrolstation.models.Account;
import co.highfive.petrolstation.models.AppData;
import co.highfive.petrolstation.models.Currency;
import co.highfive.petrolstation.models.Setting;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.ApiError;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;
import co.highfive.petrolstation.settings.dto.CompanySettingData;

public class FinanceActivity extends BaseActivity {

    private ActivityFinanceBinding binding;

    private AddFinanceAccountDialog addFinanceAccountDialog;

    private CompanySettingData appData;
    private final ArrayList<Account> accounts = new ArrayList<>();
    private FinanceAdapter adapter;

    private String accountTypeId = null;

    private int add_financial = 0;
    private int view_financial_move = 0;
    private int update_account = 0;
    private int print_financial_move = 0;

    private int page = 1;
    private boolean isLoading = false;
    private boolean hasMore = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFinanceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI(binding.mainLayout);

        initRecycler();
        initClicks();
        initSearch();
        initRefresh();
        int userSanad = getUserSanadFromSession();
        getCompanySetting(true,userSanad);
        fetchFirstPage(true);
    }

    private void initRecycler() {
        adapter = new FinanceAdapter(
                accounts,
                () -> print_financial_move,
                () -> update_account,
                () -> view_financial_move,
                this,
                (account, position, newName, onDoneUiUpdate,editNameDialog) -> updateAccount(account, position, newName, onDoneUiUpdate,editNameDialog)
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

    private void updateAccount(Account account, int position, String newName, Runnable onDoneUiUpdate, EditNameDialog editNameDialog) {

        if (account == null || safe(account.getId()).trim().isEmpty()) {
            toast(getString(R.string.general_error));
            return;
        }

        showProgressHUD();

        Map<String, String> params = ApiClient.mapOf(
                "id", safe(account.getId()),
                "name", safe(newName).trim()
        );

        Type type = new TypeToken<BaseResponse<Object>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_POST,
                Endpoints.FINANCIAL_UPDATEACCOUNT, // ✅ عدّلها لاسم endpoint الصحيح عندكم
                params,
                null,
                type,
                0,
                new ApiCallback<Object>() {

                    @Override
                    public void onSuccess(Object data, String msg, String rawJson) {
                        hideProgressHUD();

                        try {
                            String targetId = safe(account.getId());

                            int index = -1;
                            for (int i = 0; i < accounts.size(); i++) {
                                Account a = accounts.get(i);
                                if (a != null && safe(a.getId()).equals(targetId)) {
                                    index = i;
                                    break;
                                }
                            }

                            if (index >= 0) {
                                accounts.get(index).setAccount_name(newName);
                                adapter.notifyItemChanged(index);
                            } else {
                                // احتياط: لو ما لقيناه (نادر) اعمل refresh
                                adapter.notifyDataSetChanged();
                            }

                            if (editNameDialog != null && editNameDialog.isVisible()) {
                                editNameDialog.dismiss();
                            }
                            adapter.updateNameById(safe(account.getId()), newName);

                        } catch (Exception ignored) {}

                        toast(msg == null || msg.trim().isEmpty() ? getString(R.string.done) : msg);
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


    private void initClicks() {
        binding.icHome.setOnClickListener(v ->
                moveToActivity(getApplicationContext(), MainActivity.class, null, false, true)
        );
        binding.icBack.setOnClickListener(v -> finish());

        binding.accountTypeLayout.setOnClickListener(v -> selectAccountType());

        binding.icAddWhite.setOnClickListener(v -> {
            if (add_financial != 1) {
                toast(getString(R.string.no_permissno));
                return;
            }
            try {
                openAddFinanceAccountDialog(appData, (type_id, account_type_id, select_customer_id, select_user_id, account_name, currency_id, notes, mobile) ->
                        addAccount(type_id, account_type_id, select_customer_id, select_user_id, account_name, currency_id, notes, mobile)
                );
            } catch (Exception e) {
                errorLogger("Exception", String.valueOf(e.getMessage()));
            }
        });
    }

    private void initSearch() {
        binding.search.setOnEditorActionListener((TextView v, int actionId, android.view.KeyEvent event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                hideSoftKeyboard();
                fetchFirstPage(true);
                return true;
            }
            return false;
        });
    }

    private void initRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> fetchFirstPage(false));
    }

    private void fetchFirstPage(boolean showDialog) {
        page = 1;
        hasMore = true;
        accounts.clear();
        adapter.notifyDataSetChanged();
        getData(page, showDialog);
    }

    private void loadNextPage() {
        if (!hasMore) return;
        getData(page + 1, false);
    }

    // =============================
    // Dropdown account type (كما هو)
    // =============================
    public void selectAccountType() {

        errorLogger("getAccount_type_size",""+appData.account_type.size());
        ArrayList<Currency> arrayList = new ArrayList<>();
        if (appData != null && appData.account_type != null && appData.account_type.size() > 0) {
            for (int i = 0; i < appData.account_type.size(); i++) {
                arrayList.add(new Currency(""+appData.account_type.get(i).id, appData.account_type.get(i).name));
            }
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View popupView = Objects.requireNonNull(inflater).inflate(R.layout.drop_down_list, null);

        androidx.recyclerview.widget.RecyclerView recyclerView = popupView.findViewById(R.id.recycler_view);
        int width = (int) (getWindowManager().getDefaultDisplay().getWidth() * 0.4);

        PopupWindow popupWindow = new PopupWindow(popupView, width, ViewGroup.LayoutParams.WRAP_CONTENT);

        DropDownAdapter dd = new DropDownAdapter(arrayList, (view, position) -> {
            String title = arrayList.get(position).getName();
            accountTypeId = arrayList.get(position).getId();
            binding.accountType.setText(title);

            fetchFirstPage(true);
            popupWindow.dismiss();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(dd);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) popupWindow.setElevation(18f);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.showAsDropDown(binding.accountType);
    }

    // =============================
    // API: Get Accounts (Paging)
    // =============================
    private void getData(int targetPage, boolean showDialog) {
        isLoading = true;
        if (showDialog) showProgressHUD();

        Map<String, String> params = ApiClient.mapOf(
                "page", String.valueOf(targetPage)
        );

        String q = safe(binding.search.getText() == null ? "" : binding.search.getText().toString()).trim();
        if (!q.isEmpty()) params.put("name", q);
        if (accountTypeId != null) params.put("account_type", accountTypeId);

        Type type = new TypeToken<BaseResponse<GetAccountsResponse>>() {}.getType();

        apiClient.request(
                co.highfive.petrolstation.hazemhamadaqa.Http.Constant.REQUEST_GET,
                Endpoints.FINANCIAL_getAccounts,
                params,
                null,
                type,
                0,
                new ApiCallback<GetAccountsResponse>() {
                    @Override
                    public void onSuccess(GetAccountsResponse data, String msg, String rawJson) {
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        isLoading = false;

                        if (data == null) {
                            toast(getString(R.string.general_error));
                            return;
                        }

                        add_financial = data.add_financial;
                        view_financial_move = data.view_financial_move;
                        update_account = data.update_account;
                        print_financial_move = data.print_financial_move;

                        binding.icAddWhite.setVisibility(add_financial == 1 ? View.VISIBLE : View.GONE);

                        // update setting check
                        Setting setting = data.setting;
                        if (setting != null && setting.getVersion_app() != null) {
                            if (!setting.getVersion_app().equals(BuildConfig.VERSION_NAME)) {
                                openUpdateAppDialog(setting.getUpdate_title(), setting.getUpdate_description(), setting.getUrl_app());
                            }
                        }

                        List<Account> newItems = (data.accounts != null && data.accounts.data != null)
                                ? data.accounts.data
                                : new ArrayList<>();

                        boolean noNext = (data.accounts == null) || (data.accounts.next_page_url == null) || data.accounts.next_page_url.trim().isEmpty();
                        if (newItems.isEmpty()) {
                            hasMore = false;
                        } else {
                            page = targetPage;
                            hasMore = !noNext;
                            int start = accounts.size();
                            accounts.addAll(newItems);
                            adapter.notifyItemRangeInserted(start, newItems.size());
                        }
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
                    public void onError(ApiError error) {
                        if (showDialog) hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        isLoading = false;
                        toast(error.message);
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

    // =============================
    // API: Add Account
    // =============================
    private void addAccount(String type_id,
                            String account_type_id,
                            String select_customer_id,
                            String select_user_id,
                            String account_name,
                            String currency_id,
                            String notes,
                            String mobile) {

        showProgressHUD();

        Map<String, String> params = ApiClient.mapOf(
                "type", safe(type_id)
        );

        if (account_name != null && !account_name.trim().isEmpty()) params.put("name", account_name.trim());
        if (account_type_id != null) params.put("account_type", account_type_id);
        if (currency_id != null) params.put("account_currency", currency_id);
        if (select_user_id != null) params.put("user_id", select_user_id);
        if (select_customer_id != null) params.put("person_id", select_customer_id);
        if (notes != null && !notes.trim().isEmpty()) params.put("notes", notes.trim());
        if ("2".equals(type_id)) {
            if (mobile != null && !mobile.trim().isEmpty()) {
                params.put("mobile", mobile.trim()); // ✅ اسم المتغير في الريكوست
            }
        }
        Type type = new TypeToken<BaseResponse<Object>>() {}.getType();

        apiClient.request(
                co.highfive.petrolstation.hazemhamadaqa.Http.Constant.REQUEST_POST,
                Endpoints.FINANCIAL_ADDACCOUNT,
                params,
                null,
                type,
                0,
                new ApiCallback<Object>() {
                    @Override
                    public void onSuccess(Object data, String msg, String rawJson) {
                        hideProgressHUD();
                        try {
                            if (addFinanceAccountDialog != null) addFinanceAccountDialog.dismiss();
                        } catch (Exception ignored) {}

                        toast(msg == null ? getString(R.string.done) : msg);
                        fetchFirstPage(true);
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
                    public void onError(ApiError error) {
                        hideProgressHUD();
                        toast(error.message);
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        hideProgressHUD();
                        toast(getString(R.string.general_error));
                    }
                }
        );
    }

    // =============================
    // API: Company Setting
    // =============================

    private void getCompanySetting(boolean showDialog,int userSanad) {
        if (showDialog) showProgressHUD();
        // code (اختياري) حسب السيرفر — موجود في الـ Postman بس disabled
        String code = getCompanyCodeFromSession();

        Map<String, String> params = (code != null && !code.trim().isEmpty())
                ? ApiClient.mapOf("user_sanad", String.valueOf(userSanad), "code", code)
                : ApiClient.mapOf("user_sanad", String.valueOf(userSanad));

        Type type = new TypeToken<BaseResponse<CompanySettingData>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.GET_COMPANY_SETTING,
                params,
                null,
                type,
                0,
                new ApiCallback<CompanySettingData>() {
                    @Override public void onSuccess(CompanySettingData data, String message, String rawJson) {
                        hideProgressHUD();

                        if (data != null) {
                            String dataJson = getGson().toJson(data);
                            getSessionManager().setString(getSessionKeys().app_data, dataJson);
                            appData = data;

                        }


                    }

                    @Override public void onError(co.highfive.petrolstation.network.ApiError error) {
                        if (showDialog) hideProgressHUD();
                        toast(error.message);
                    }

                    @Override public void onUnauthorized(String rawJson) {
                        if (showDialog) hideProgressHUD();
                        logout();
                    }

                    @Override public void onNetworkError(String reason) {
                        if (showDialog) hideProgressHUD();
                        toast(R.string.no_internet);
                    }

                    @Override public void onParseError(String rawJson, Exception e) {
                        if (showDialog) hideProgressHUD();
                        toast(getString(R.string.general_error));
                    }
                }
        );
    }

    // =============================
    // Dialog opening (كما القديم)
    // =============================
    private void openAddFinanceAccountDialog(CompanySettingData appData, AddFinanceAccountListener listener) {
        addFinanceAccountDialog = new AddFinanceAccountDialog(this, appData,apiClient);
        addFinanceAccountDialog.setCancelable(false);
        addFinanceAccountDialog.setAddFinanceAccountListener(listener);
        addFinanceAccountDialog.show(getSupportFragmentManager(), "AddFinanceAccountDialog");
    }


}
