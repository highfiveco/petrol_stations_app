package co.highfive.petrolstation.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.common.reflect.TypeToken;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.activities.FinanceActivity;
import co.highfive.petrolstation.adapters.DropDownAdapter;
import co.highfive.petrolstation.adapters.SelectCustomerAdapter;
import co.highfive.petrolstation.customers.dto.CustomerDto;
import co.highfive.petrolstation.customers.dto.CustomersData;
import co.highfive.petrolstation.databinding.AddFinanceAccountDialogLayoutBinding;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.listener.AddFinanceAccountListener;
import co.highfive.petrolstation.listener.SelectIntListener;
import co.highfive.petrolstation.models.Currency;
import co.highfive.petrolstation.models.Customer;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;
import co.highfive.petrolstation.settings.dto.CompanySettingData;

public class AddFinanceAccountDialog extends DialogFragment {

    private Context context;
    private BaseActivity baseActivity;

    private AddFinanceAccountListener addFinanceAccountListener;

    private AddFinanceAccountDialogLayoutBinding binding;

    private String type_id = null;
    private String account_type_id = null;
    private String select_user_id = null;
    private String currency_id = null;

    private final ArrayList<CustomerDto> customers = new ArrayList<>();

    private int customer_request_typePos = -1;
    private int confirmCustomer_request_type = -1;
    private int confirmCustomer_request_type_id = -1;

    private CompanySettingData appData;
    ApiClient apiClient;
    public AddFinanceAccountDialog(FinanceActivity financeActivity, CompanySettingData appData, ApiClient apiClient) {

        this.apiClient= apiClient;
        this.baseActivity = financeActivity;
        this.appData = appData;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.context = null;
    }

    public void setAddFinanceAccountListener(AddFinanceAccountListener addFinanceAccountListener) {
        this.addFinanceAccountListener = addFinanceAccountListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = AddFinanceAccountDialogLayoutBinding.inflate(inflater, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        View root = binding.getRoot();

        baseActivity.setupUIToHideKeyboard(root);

        // ✅ خلي الفوكس افتراضيًا على mainLayout (مش على EditText)
        binding.mainLayout.requestFocus();

        baseActivity.hideKeyboardOnScrollUniversal(root, binding.scrollView);
        binding.scrollView.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_MOVE
                    || event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                if (baseActivity != null) {
                    baseActivity.forceHideKeyboardAndClearFocus(binding.getRoot());
                }
            }
            return false; // مهم: لا تمنع السكرول
        });

        initClicks();
        initCustomerSearch();

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(
                    android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                            | android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
            );
        }


        return binding.getRoot();
    }

    private void initClicks() {

        binding.close.setOnClickListener(v -> dismiss());

        binding.save.setOnClickListener(v -> {
            String validate = validate();
            if (validate == null) {
                if (addFinanceAccountListener != null) {
                    addFinanceAccountListener.addFinanceAccount(
                            type_id,
                            account_type_id,
                            String.valueOf(confirmCustomer_request_type_id),
                            select_user_id,
                            safeTrim(binding.accountName.getText()),
                            currency_id,
                            safeTrim(binding.notes.getText()),
                            safeTrim(binding.mobile.getText())
                    );
                }
            } else {
                if (baseActivity != null) {
                    baseActivity.toast(validate);
                }
            }
        });
        binding.mainLayout.setOnClickListener(v ->
                baseActivity.forceHideKeyboardAndClearFocus(binding.getRoot())
        );
        binding.typeLayout.setOnClickListener(v ->{
            baseActivity.forceHideKeyboardAndClearFocus(binding.getRoot());
            selectTypeLayout();
        });
        binding.accountTypeLayout.setOnClickListener(v -> {
            baseActivity.forceHideKeyboardAndClearFocus(binding.getRoot());
            selectAccountTypeLayout();
        });

        binding.selectUserLayout.setOnClickListener(v -> {
            baseActivity.forceHideKeyboardAndClearFocus(binding.getRoot());
            selectUserLayout();
        });

        binding.currencyLayout.setOnClickListener(v -> {
            baseActivity.forceHideKeyboardAndClearFocus(binding.getRoot());
            selectCurrencyLayout();
        });
    }

    private void initCustomerSearch() {
        binding.search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (baseActivity != null) baseActivity.hideSoftKeyboard();
                if (!safeTrim(binding.search.getText()).isEmpty()) {
                    getCustomers(true);
                }
                return true;
            }
            return false;
        });
    }

    private String validate() {

        if (account_type_id == null) {
            return getString(R.string.select_account_type);
        }

        if (currency_id == null) {
            return getString(R.string.select_currency);
        }

        if (type_id == null) {
            return getString(R.string.type);
        }

        if (type_id.equals("1")) { // customer
            if (confirmCustomer_request_type_id == -1) {
                return getString(R.string.select_customer);
            }
        } else if (type_id.equals("3")) { // user
            if (select_user_id == null) {
                return getString(R.string.select_user);
            }
        }

        if (type_id.equals("2")) { // private
            if (safeTrim(binding.accountName.getText()).isEmpty()) {
                return getString(R.string.enter_account_name);
            }
            if (safeTrim(binding.mobile.getText()).isEmpty()) {
                return "أدخل رقم الجوال";
            }
        }

        return null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        try {
            super.onActivityCreated(savedInstanceState);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        try {
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(this, tag);
            ft.commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            Log.e("ABSDIALOGFRAG", "Exception", e);
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(
                    android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                            | android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
            );
        }
    }

    // =========================
    // Dropdown: Type
    // =========================
    private void selectTypeLayout() {

        ArrayList<Currency> arrayList = new ArrayList<>();
        if (appData != null && appData.type != null && appData.type.size() > 0) {
            for (int i = 0; i < appData.type.size(); i++) {
                arrayList.add(new Currency(""+appData.type.get(i).id, appData.type.get(i).name));
            }
        }

        LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View popupView = Objects.requireNonNull(inflater).inflate(R.layout.drop_down_list, null);

        RecyclerView recyclerView = popupView.findViewById(R.id.recycler_view);
        int width = (int) (requireActivity().getWindowManager().getDefaultDisplay().getWidth() * 0.4);
        PopupWindow popupWindow = new PopupWindow(popupView, width, ViewGroup.LayoutParams.WRAP_CONTENT);

        DropDownAdapter adapter = new DropDownAdapter(arrayList, (view, position) -> {
            String title = arrayList.get(position).getName();
            type_id = arrayList.get(position).getId();

            toggleContainersByType(type_id);

            binding.type.setText(title);
            popupWindow.dismiss();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity().getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) popupWindow.setElevation(18f);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.showAsDropDown(binding.type);
    }

    private void toggleContainersByType(String typeId) {

        if (typeId == null) return;

        if (typeId.equals("1")) { // customer
            binding.customerContainer.setVisibility(View.VISIBLE);
            binding.userContainer.setVisibility(View.GONE);
            binding.accountNameContainer.setVisibility(View.GONE);

        } else if (typeId.equals("2")) { // private (خاص)
            binding.customerContainer.setVisibility(View.GONE);
            binding.userContainer.setVisibility(View.GONE);
            binding.accountNameContainer.setVisibility(View.VISIBLE);

            // ✅ mobile يظهر فقط هنا
            binding.mobileLabel.setVisibility(View.VISIBLE);
            binding.mobile.setVisibility(View.VISIBLE);

        } else if (typeId.equals("3")) { // user
            binding.accountNameContainer.setVisibility(View.GONE);
            binding.customerContainer.setVisibility(View.GONE);
            binding.userContainer.setVisibility(View.VISIBLE);

        } else {
            binding.customerContainer.setVisibility(View.GONE);
            binding.userContainer.setVisibility(View.GONE);
            binding.accountNameContainer.setVisibility(View.VISIBLE);

            // الافتراضي: نخفيه إلا لو بدك غير هيك
            binding.mobileLabel.setVisibility(View.GONE);
            binding.mobile.setVisibility(View.GONE);
        }

        // ✅ تنظيف حقل الجوال عند تغيير النوع
        if (!"2".equals(typeId)) {
            binding.mobile.setText("");
        }
    }

    // =========================
    // Dropdown: Account Type
    // =========================
    private void selectAccountTypeLayout() {

        ArrayList<Currency> arrayList = new ArrayList<>();
        if (appData != null && appData.account_type != null && appData.account_type.size() > 0) {
            for (int i = 0; i < appData.account_type.size(); i++) {
                arrayList.add(new Currency(""+appData.account_type.get(i).id, appData.account_type.get(i).name));
            }
        }

        LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View popupView = Objects.requireNonNull(inflater).inflate(R.layout.drop_down_list, null);

        RecyclerView recyclerView = popupView.findViewById(R.id.recycler_view);
        int width = (int) (requireActivity().getWindowManager().getDefaultDisplay().getWidth() * 0.4);
        PopupWindow popupWindow = new PopupWindow(popupView, width, ViewGroup.LayoutParams.WRAP_CONTENT);

        DropDownAdapter adapter = new DropDownAdapter(arrayList, (view, position) -> {
            String title = arrayList.get(position).getName();
            account_type_id = arrayList.get(position).getId();
            binding.accountType.setText(title);
            popupWindow.dismiss();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity().getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) popupWindow.setElevation(18f);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.showAsDropDown(binding.accountType);
    }

    // =========================
    // Dropdown: User
    // =========================
    private void selectUserLayout() {

        ArrayList<Currency> arrayList = new ArrayList<>();
        if (appData != null && appData.users != null && appData.users.size() > 0) {
            for (int i = 0; i < appData.users.size(); i++) {
                arrayList.add(new Currency(""+appData.users.get(i).id, appData.users.get(i).name));
            }
        }

        LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View popupView = Objects.requireNonNull(inflater).inflate(R.layout.drop_down_list, null);

        RecyclerView recyclerView = popupView.findViewById(R.id.recycler_view);
        int width = (int) (requireActivity().getWindowManager().getDefaultDisplay().getWidth() * 0.4);
        PopupWindow popupWindow = new PopupWindow(popupView, width, ViewGroup.LayoutParams.WRAP_CONTENT);

        DropDownAdapter adapter = new DropDownAdapter(arrayList, (view, position) -> {
            String title = arrayList.get(position).getName();
            select_user_id = arrayList.get(position).getId();
            binding.selectUser.setText(title);
            popupWindow.dismiss();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity().getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) popupWindow.setElevation(18f);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.showAsDropDown(binding.selectUser);
    }

    // =========================
    // Dropdown: Currency
    // =========================
    private void selectCurrencyLayout() {

        ArrayList<Currency> arrayList = new ArrayList<>();
        if (appData != null && appData.currency != null && appData.currency.size() > 0) {
            for (int i = 0; i < appData.currency.size(); i++) {
                arrayList.add(new Currency(""+appData.currency.get(i).id, appData.currency.get(i).name));
            }
        }

        LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View popupView = Objects.requireNonNull(inflater).inflate(R.layout.drop_down_list, null);

        RecyclerView recyclerView = popupView.findViewById(R.id.recycler_view);
        int width = (int) (requireActivity().getWindowManager().getDefaultDisplay().getWidth() * 0.4);
        PopupWindow popupWindow = new PopupWindow(popupView, width, ViewGroup.LayoutParams.WRAP_CONTENT);

        DropDownAdapter adapter = new DropDownAdapter(arrayList, (view, position) -> {
            String title = arrayList.get(position).getName();
            currency_id = arrayList.get(position).getId();
            binding.currency.setText(title);
            popupWindow.dismiss();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity().getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) popupWindow.setElevation(18f);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.showAsDropDown(binding.currency);
    }

    // =========================
    // Customers API + BottomSheet
    // =========================
    private void getCustomers(boolean isShowDialog) {

        if (baseActivity == null) return;

        customers.clear();

        if (isShowDialog) baseActivity.showProgressHUD();

        // page=1 + optional name
        Map<String, String> params = new HashMap<>();
        params.put("page", "1");

        String q = safeTrim(binding.search.getText());
        if (q != null && !q.trim().isEmpty()) {
            params.put("name", q.trim());
        }

        Type type = new TypeToken<BaseResponse<CustomersData>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.CUSTOMERS,
                params,
                null,
                type,
                0,
                new ApiCallback<CustomersData>() {

                    @Override
                    public void onSuccess(CustomersData data, String message, String rawJson) {
                        if (isShowDialog) baseActivity.hideProgressHUD();

                        List<CustomerDto> list = (data == null) ? null : data.customers;

                        if (list != null && !list.isEmpty()) {
                            customers.clear();
                            customers.addAll(list);
                            openCustomerDialog(customers);
                        } else {
                            // زي القديم: لو ما في نتائج/فشل status
                            // إذا بتحب تظهر message من السيرفر:
                            if (message != null && !message.trim().isEmpty()) {
                                baseActivity.toast(message);
                            }
                        }
                    }

                    @Override
                    public void onError(co.highfive.petrolstation.network.ApiError error) {
                        if (isShowDialog) baseActivity.hideProgressHUD();
                        baseActivity.toast(error != null && error.message != null
                                ? error.message
                                : baseActivity.getString(R.string.general_error));
                    }

                    @Override
                    public void onUnauthorized(String rawJson) {
                        if (isShowDialog) baseActivity.hideProgressHUD();
                        baseActivity.logout();
                    }

                    @Override
                    public void onNetworkError(String reason) {
                        if (isShowDialog) baseActivity.hideProgressHUD();
                        baseActivity.toast(R.string.no_internet);
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        if (isShowDialog) baseActivity.hideProgressHUD();
                        baseActivity.toast(baseActivity.getString(R.string.general_error));
                    }
                }
        );
    }


    private void openCustomerDialog(ArrayList<CustomerDto> customers) {
        if (baseActivity == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(baseActivity);
        View view = LayoutInflater.from(baseActivity).inflate(R.layout.select_customer_layout_dialog, null);

        RecyclerView recycle_customer = view.findViewById(R.id.recycle_customer);
        View back = view.findViewById(R.id.back);
        View confirm = view.findViewById(R.id.confirm);

        SelectCustomerAdapter adapter = new SelectCustomerAdapter(customers, confirmCustomer_request_type, new SelectIntListener() {
            @Override
            public void selectInt(int number) {
                customer_request_typePos = number;
            }
        });

        recycle_customer.setHasFixedSize(true);
        recycle_customer.setLayoutManager(new LinearLayoutManager(baseActivity));
        recycle_customer.setAdapter(adapter);

        back.setOnClickListener(v -> dialog.dismiss());

        confirm.setOnClickListener(v -> {
            dialog.dismiss();

            if (customer_request_typePos < 0 || customer_request_typePos >= customers.size()) return;

            binding.search.setText(String.valueOf(customers.get(customer_request_typePos).name));

            confirmCustomer_request_type = customer_request_typePos;
            try {
                confirmCustomer_request_type_id =  customers.get(customer_request_typePos).id;
            } catch (Exception ignored) {
                confirmCustomer_request_type_id = -1;
            }
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private static String safeTrim(CharSequence cs) {
        return cs == null ? "" : cs.toString().trim();
    }
}
