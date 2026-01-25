package co.highfive.petrolstation.activities;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.common.reflect.TypeToken;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import co.highfive.petrolstation.BuildConfig;
import co.highfive.petrolstation.R;
import co.highfive.petrolstation.adapters.DropDownAdapter;

import co.highfive.petrolstation.financial.dto.FinancialAddMoveResponseDto;
import co.highfive.petrolstation.financial.dto.FinancialResponseDto;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.models.Account;
import co.highfive.petrolstation.models.Currency;
import co.highfive.petrolstation.models.Transactions;
import co.highfive.petrolstation.models.Reading;
import co.highfive.petrolstation.models.Setting;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.ApiError;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;

import co.highfive.petrolstation.databinding.ActivityAddFinancialTransactionBinding;

public class AddFinancialTransactionActivity extends BaseActivity {

    private ActivityAddFinancialTransactionBinding binding;

    AppCompatTextView name;
    AppCompatTextView phone;
    AppCompatTextView currency;
    AppCompatTextView account_type;

    RadioGroup radio_group;
    RadioButton add_transaction;
    RadioButton load_transaction;
    RadioButton discount;

    AppCompatTextView payment_method;
    AppCompatEditText check_number;
    AppCompatTextView bank;
    AppCompatTextView due_date;
    AppCompatEditText statement;
    AppCompatEditText amount;
    AppCompatTextView currency_edit_txt;
    AppCompatTextView date;
    AppCompatTextView income_type;
    AppCompatTextView transaction_type;
    AppCompatTextView transaction_month;

    LinearLayout payment_method_container;
    LinearLayout income_type_container;
    LinearLayout transaction_type_container;
    LinearLayout bank_date_container;
    LinearLayout check_container;
    LinearLayout statement_layout;
    LinearLayout date_layout;

    AppCompatEditText notes;

    int selectedChoice = 1;

    String payment_method_id =  null;
    String bank_id =  null;
    String currency_id =  null;
    String income_type_id =  null;
    String transaction_type_id =  null;
//    String transaction_month_id =  null;

    String customer_id =  null;
    String account_id =  null;

    ArrayList<Currency> payment_type = new ArrayList<Currency>();
    ArrayList<Currency> currencies = new ArrayList<Currency>();
    ArrayList<Currency> type_loaded = new ArrayList<Currency>();
    ArrayList<Currency> type_income = new ArrayList<Currency>();
    ArrayList<Currency> banks = new ArrayList<Currency>();

    int check_last_reading_sanad = 0;
    int view_date = 0;
    int view_statement = 0;
    int has_move = 0;
    int has_load = 0;
    int payment_type_default = -50;
    int disabled_type_move = -50;
    int has_discount = 0;
    int default_type_income =  -10;

    Account account ;

    private int record = 17;
    private String[] mStrings = new String[]{"CP437", "CP850", "CP860", "CP863", "CP865", "CP857", "CP737", "CP928", "Windows-1252", "CP866", "CP852", "CP858", "CP874", "Windows-775", "CP855", "CP862", "CP864", "GB18030", "BIG5", "KSC5601", "utf-8"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddFinancialTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI(binding.mainLayout);

        bindViewsFromBinding();
        bindClicksFromBinding();

        setUpViews();
    }

    private void bindViewsFromBinding() {

        name = binding.name;
        phone = binding.phone;
        currency = binding.currency;
        account_type = binding.accountType;

        radio_group = binding.radioGroup;
        add_transaction = binding.addTransaction;
        load_transaction = binding.loadTransaction;
        discount = binding.discount;

        payment_method = binding.paymentMethod;
        check_number = binding.checkNumber;
        bank = binding.bank;
        due_date = binding.dueDate;
        statement = binding.statement;
        amount = binding.amount;
        currency_edit_txt = binding.currencyEditTxt;
        date = binding.date;
        income_type = binding.incomeType;
        transaction_type = binding.transactionType;

        payment_method_container = binding.paymentMethodContainer;
        income_type_container = binding.incomeTypeContainer;
        transaction_type_container = binding.transactionTypeContainer;

        bank_date_container = binding.bankDateContainer;
        check_container = binding.checkContainer;

        statement_layout = binding.statementLayout;
        date_layout = binding.dateLayout;

        notes = binding.notes;
    }

    private void bindClicksFromBinding() {

        binding.icHome.setOnClickListener(v -> ic_home());
        binding.icBack.setOnClickListener(v -> ic_back());

        binding.paymentMethodLayout.setOnClickListener(v -> payment_method_layout());
        binding.bankLayout.setOnClickListener(v -> bank_layout());
        binding.dueDateLayout.setOnClickListener(v -> due_date_layout());

        binding.currencyLayout.setOnClickListener(v -> currency_layout());

        binding.date.setOnClickListener(v -> date());

        binding.incomeTypeLayout.setOnClickListener(v -> income_type_layout());
        binding.transactionTypeLayout.setOnClickListener(v -> transaction_type_layout());

        binding.save.setOnClickListener(v -> save());

        binding.phone.setOnClickListener(v -> phone());
    }

    void ic_home(){
        moveToActivity(getApplicationContext(),MainActivity.class,null,false,true);
    }

    private void setUpViews() {

        radio_group.setOnCheckedChangeListener((group, checkedId) -> {

            if (checkedId == R.id.add_transaction) {
                selectedChoice = 1;
            } else if (checkedId == R.id.load_transaction) {
                selectedChoice = 2;
            } else if (checkedId == R.id.discount) {
                selectedChoice = 3;
            }

            hideShowForm();
        });
        if(isDevelopment){
            statement.setText("تست");
            amount.setText("50");
//            currency_id =  "1";
//            currency_edit_txt.setText("شيكل");
            date.setText("2022-12-19");
        }

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            customer_id = extras.getString("customer_id",null);
            account_id = extras.getString("account_id",null);
            if(connectionAvailable){
                getData(true);
            }else{ // get financial data
                getLocalData(true);
            }

        }else{
            finish();
            return;
        }
    }

    private void hideShowForm() {
        payment_method_container.setVisibility(View.GONE);
        income_type_container.setVisibility(View.GONE);
        transaction_type_container.setVisibility(View.GONE);

        bank_date_container.setVisibility(View.GONE);
        check_container.setVisibility(View.GONE);

        switch (selectedChoice){
            case 1:
                payment_method_container.setVisibility(View.VISIBLE);
                income_type_container.setVisibility(View.VISIBLE);

                if(payment_method_id != null  &&  payment_method_id.equals("2")){
                    bank_date_container.setVisibility(View.VISIBLE);
                    check_container.setVisibility(View.VISIBLE);
                }

                break;
            case 2:
                transaction_type_container.setVisibility(View.VISIBLE);
                break;
            case 3:
                break;
        }

    }

    void payment_method_layout(){
        selectPaymentMethod();
    }

    public void selectPaymentMethod() {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View popupView = Objects.requireNonNull(inflater).inflate(R.layout.drop_down_list, null);
        RecyclerView recyclerView = (RecyclerView) popupView.findViewById(R.id.recycler_view);
        int width = (int) (getWindowManager().getDefaultDisplay().getWidth() * 0.4);
        PopupWindow popupWindow = new PopupWindow(popupView, width, ViewGroup.LayoutParams.WRAP_CONTENT);

        DropDownAdapter adapter = new DropDownAdapter(payment_type, (view, position) -> {
            String title = payment_type.get(position).getName();

            payment_method_id = payment_type.get(position).getId();

            errorLogger("payment_method_id",""+payment_method_id);

            payment_method.setText(title);
            hideShowForm();
            popupWindow.dismiss();
        });
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(18f);
        }
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.showAsDropDown(payment_method);
    }

    void bank_layout(){
        selectBank();
    }

    public void selectBank() {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View popupView = Objects.requireNonNull(inflater).inflate(R.layout.drop_down_list, null);
        RecyclerView recyclerView = (RecyclerView) popupView.findViewById(R.id.recycler_view);
        int width = (int) (getWindowManager().getDefaultDisplay().getWidth() * 0.4);
        PopupWindow popupWindow = new PopupWindow(popupView, width, ViewGroup.LayoutParams.WRAP_CONTENT);

        DropDownAdapter adapter = new DropDownAdapter(banks, (view, position) -> {
            String title = banks.get(position).getName();

            bank_id = banks.get(position).getId();

            bank.setText(title);

            popupWindow.dismiss();
        });
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(18f);
        }
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.showAsDropDown(bank);
    }

    void due_date_layout(){
        showPicker(due_date);
    }


    public void showPicker(AppCompatTextView target) {
        Calendar cal = Calendar.getInstance();

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH); // 0-based
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    String formatted = String.format(
                            Locale.ENGLISH,
                            "%04d-%02d-%02d",
                            y, (m + 1), d
                    );
                    target.setText(formatted);
                },
                year, month, day
        );

        dialog.show();
    }

    void currency_layout(){
        selectCurrency();
    }

    public void selectCurrency() {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View popupView = Objects.requireNonNull(inflater).inflate(R.layout.drop_down_list, null);
        RecyclerView recyclerView = (RecyclerView) popupView.findViewById(R.id.recycler_view);
        int width = (int) (getWindowManager().getDefaultDisplay().getWidth() * 0.4);
        PopupWindow popupWindow = new PopupWindow(popupView, width, ViewGroup.LayoutParams.WRAP_CONTENT);

        DropDownAdapter adapter = new DropDownAdapter(currencies, (view, position) -> {
            String title = currencies.get(position).getName();

            currency_id = currencies.get(position).getId();

            currency_edit_txt.setText(title);

            popupWindow.dismiss();
        });
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(18f);
        }
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.showAsDropDown(currency_edit_txt);
    }

    void date(){
        showPicker(date);
    }

    void income_type_layout(){
        if(disabled_type_move == 1){
            selectIncomeType();
        }
    }

    public void selectIncomeType() {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View popupView = Objects.requireNonNull(inflater).inflate(R.layout.drop_down_list, null);
        RecyclerView recyclerView = (RecyclerView) popupView.findViewById(R.id.recycler_view);
        int width = (int) (getWindowManager().getDefaultDisplay().getWidth() * 0.4);
        PopupWindow popupWindow = new PopupWindow(popupView, width, ViewGroup.LayoutParams.WRAP_CONTENT);

        DropDownAdapter adapter = new DropDownAdapter(type_income, (view, position) -> {
            String title = type_income.get(position).getName();

            income_type_id = type_income.get(position).getId();

            income_type.setText(title);

            popupWindow.dismiss();
        });
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(18f);
        }
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.showAsDropDown(income_type);
    }

    void transaction_type_layout(){
        selectTransactionType();
    }

    public void selectTransactionType() {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View popupView = Objects.requireNonNull(inflater).inflate(R.layout.drop_down_list, null);
        RecyclerView recyclerView = (RecyclerView) popupView.findViewById(R.id.recycler_view);
        int width = (int) (getWindowManager().getDefaultDisplay().getWidth() * 0.4);
        PopupWindow popupWindow = new PopupWindow(popupView, width, ViewGroup.LayoutParams.WRAP_CONTENT);

        DropDownAdapter adapter = new DropDownAdapter(type_loaded, (view, position) -> {
            String title = type_loaded.get(position).getName();

            transaction_type_id = type_loaded.get(position).getId();

            transaction_type.setText(title);

            popupWindow.dismiss();
        });
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(18f);
        }
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.showAsDropDown(transaction_type);
    }



    void save(){
        String validate = validate();
        if(validate == null){
            switch (selectedChoice){
                case 1:
                    if(connectionAvailable){
                        addMove(true);
                    }else{
                        if(account != null){
                            addLocalMove(true);
                        }else{
                            toast(R.string.customer_not_found);
                        }
                    }
                    break;
                case 2:
                    if(connectionAvailable){
                        addLoad(true);
                    }else{
                        if(account != null){
                            addLocalLoad(true);
                        }else{
                            toast(R.string.customer_not_found);
                        }
                    }
                    break;
                case 3:
                    if(connectionAvailable){
                        addDiscount(true);
                    }else{
                        if(account != null){
                            addLocalDiscount(true);
                        }else{
                            toast(R.string.customer_not_found);
                        }
                    }
                    break;
            }
        }else{
            toast(validate);
        }
    }

    private String validate() {

        if(view_statement == 1){
            if(statement.getText().toString().trim().isEmpty()){
                return getString(R.string.enter_statement);
            }
        }

        if(amount.getText().toString().trim().isEmpty()){
            return getString(R.string.enter_amount);
        }
        if(currency_id == null){
            return getString(R.string.select_currency);
        }
        if(view_date == 1){
            if(date.getText().toString().trim().isEmpty()){
                return getString(R.string.enter_date);
            }
        }

        switch (selectedChoice){
            case 1:
                income_type_container.setVisibility(View.VISIBLE);

                if(payment_method_id == null){
                    return getString(R.string.enter_payment_method);
                }else{
                    if(payment_method_id.equals("2")){
                        if(check_number.getText().toString().trim().isEmpty()){
                            return getString(R.string.enter_check_number);
                        }
                        if(bank_id == null){
                            return getString(R.string.select_bank);
                        }
                        if(check_number.getText().toString().trim().isEmpty()){
                            return getString(R.string.enter_check_due_date);
                        }
                    }
                }

                if(income_type_id == null){
                    return getString(R.string.enter_income_type);
                }

                break;
            case 2:
                if(transaction_type_id == null){
                    return getString(R.string.enter_transaction_type);
                }
//                if(transaction_month_id == null){
//                    return getString(R.string.enter_transaction_month);
//                }

                break;
            case 3:
                break;
        }
        return null;
    }

    void ic_back(){
        finish();
    }

    private void getData(boolean showDialog) {

        if (account_id == null || String.valueOf(account_id).trim().isEmpty()) {
            toast(getString(co.highfive.petrolstation.R.string.general_error));
            return;
        }

        if (showDialog) showProgressHUD();

        Map<String, String> params = ApiClient.mapOf(
                "person_id", String.valueOf(account_id)
        );

        Type type = new TypeToken<BaseResponse<FinancialResponseDto>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.FINANCIAL,
                params,
                null,
                type,
                0,
                new ApiCallback<FinancialResponseDto>() {

                    @Override
                    public void onSuccess(FinancialResponseDto data, String msg, String rawJson) {
                        if (showDialog) hideProgressHUD();

                        if (data == null) {
                            toast(getString(co.highfive.petrolstation.R.string.general_error));
                            return;
                        }

                        // same old behavior
                        check_last_reading_sanad = data.check_last_reading_sanad;
                        has_move = data.has_move;
                        has_load = data.has_load;
                        has_discount = data.has_discount;
                        view_statement = data.view_statement;
                        view_date = data.view_date;
                        payment_type_default = data.payment_type_default;
                        disabled_type_move = data.disabled_type_move;
                        default_type_income = data.default_type_income;

                        getSessionManager().setInt(getSessionKeys().check_last_reading_sanad, check_last_reading_sanad);
                        getSessionManager().setInt(getSessionKeys().has_move, has_move);
                        getSessionManager().setInt(getSessionKeys().has_load, has_load);
                        getSessionManager().setInt(getSessionKeys().has_discount, has_discount);
                        getSessionManager().setInt(getSessionKeys().view_date, view_date);
                        getSessionManager().setInt(getSessionKeys().payment_type_default, payment_type_default);
                        getSessionManager().setInt(getSessionKeys().view_statement, view_statement);
                        getSessionManager().setInt(getSessionKeys().default_type_income, default_type_income);
                        getSessionManager().setInt(getSessionKeys().disabled_type_move, disabled_type_move);

                        account = data.account;

                        payment_type = (ArrayList<Currency>) data.payment_type;
                        currencies = (ArrayList<Currency>) data.currency;
                        type_loaded = (ArrayList<Currency>)  data.type_loaded ;
                        type_income =  (ArrayList<Currency>)  data.type_income ;
                        banks = (ArrayList<Currency>)  data.bank ;

                        if (data.setting != null) {
                            if (!data.setting.getVersion_app().equals(BuildConfig.VERSION_NAME)) {
                                openUpdateAppDialog(
                                        data.setting.getUpdate_title(),
                                        data.setting.getUpdate_description(),
                                        data.setting.getUrl_app()
                                );
                            }
                        }

                        setUpData();
                    }

                    @Override
                    public void onError(ApiError error) {
                        if (showDialog) hideProgressHUD();
                        toast(error != null ? error.message : getString(co.highfive.petrolstation.R.string.general_error));
                    }

                    @Override
                    public void onUnauthorized(String rawJson) {
                        if (showDialog) hideProgressHUD();
                        logout();
                    }

                    @Override
                    public void onNetworkError(String reason) {
                        if (showDialog) hideProgressHUD();
                        toast(co.highfive.petrolstation.R.string.no_internet);
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        if (showDialog) hideProgressHUD();
                        errorLogger("FinancialGetDataParseError", e.getMessage() == null ? "null" : e.getMessage());
                        toast(getString(co.highfive.petrolstation.R.string.general_error));
                    }
                }
        );
    }

    public void getLocalData(boolean isShowDialog){


    }

    void phone(){
        if(account != null && account.getMobile() !=null){
            call(account.getMobile());
        }
    }

    private void setUpData() {
        if(account != null){
            if(account.getAccount_name() != null){
                name.setText(account.getAccount_name());
            }else{
                name.setText("");
            }

            currency.setText(account.getName_currency());
            account_type.setText(account.getAccount_type());
            phone.setText(account.getMobile());
        }else{
            name.setText("");
            currency.setText("");
            account_type.setText("");
            phone.setText("");
        }

        if(view_statement == 1){
            statement_layout.setVisibility(View.VISIBLE);
        }else{
            statement_layout.setVisibility(View.GONE);
        }

        if(view_date == 1){
            date_layout.setVisibility(View.VISIBLE);
        }else{
            date_layout.setVisibility(View.GONE);
        }

        try{
            for (int i = 0; i < payment_type.size(); i++) {
                errorLogger("payment_method_id",""+payment_type.get(i).getId());

                if(payment_type.get(i).getId().equals(""+payment_type_default)){
                    errorLogger("payment_method_id","found");
                    payment_method.setText(payment_type.get(i).getName());
                    payment_method_id = payment_type.get(i).getId();
                    break;
                }
            }
        }catch (Exception e){
        }

        try{
            if(account != null && account.getAccount_currency() != null){
                for (int i = 0; i < currencies.size() ; i++) {
                    if(currencies.get(i).getId().equals(account.getAccount_currency())){
                        currency.setText(currencies.get(i).getName());
                        currency_edit_txt.setText(currencies.get(i).getName());
                        currency_id = currencies.get(i).getId();
                        break;
                    }
                }
            }
        }catch (Exception e){
        }

        try{
            for (int i = 0; i < type_income.size(); i++) {
                if(type_income.get(i).getId().equals(""+default_type_income)){
                    income_type_id = ""+default_type_income;
                    income_type.setText(type_income.get(i).getName());
                    break;
                }
            }
        }catch (Exception e){
        }

        if(has_move == 1){
            add_transaction.setVisibility(View.VISIBLE);
            selectedChoice =1 ;
            add_transaction.setChecked(true);
        }else{
            add_transaction.setVisibility(View.GONE);
        }

        if(has_load == 1){
            load_transaction.setVisibility(View.VISIBLE);

            if(has_move == 0){
                selectedChoice =2 ;
                load_transaction.setChecked(true);
            }
        }else{
            load_transaction.setVisibility(View.GONE);
        }

        if(has_discount == 1){
            discount.setVisibility(View.VISIBLE);

            if(has_move == 0 && has_load ==0 ){
                selectedChoice =3 ;
                discount.setChecked(true);
            }
        }else{
            discount.setVisibility(View.GONE);
        }

        hideShowForm();
    }


    private void addMove(boolean showDialog) {

        if (account == null || account.getId() == null) {
            toast(getString(co.highfive.petrolstation.R.string.general_error));
            return;
        }

        if (showDialog) showProgressHUD();

        // payload like vehicles
        Map<String, String> payload = ApiClient.mapOf(
                "account_id", String.valueOf(account.getId()),
                "payment_type", safe(payment_method_id),
                "date", safe(date.getText().toString()),
                "notes", safe(notes.getText().toString()),
                "amount", safe(amount.getText().toString()),
                "currency", safe(currency_id),
                "type_move", safe(income_type_id),
                "statement", safe(statement.getText().toString())
        );

        // cheque fields
        if ("2".equals(safe(payment_method_id))) {
            payload.put("cheque_no", safe(check_number.getText().toString() ));
            payload.put("due_date", safe(due_date.getText().toString()));
            payload.put("bank_id", safe(bank_id));
        }

        Type type = new TypeToken<BaseResponse<FinancialAddMoveResponseDto>>() {}.getType();

//        Type type = new TypeToken<FinancialAddMoveResponseDto>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_POST,
                Endpoints.FINANCIAL_ADDMOVE,
                payload,
                null,
                type,
                0,
                new ApiCallback<FinancialAddMoveResponseDto>() {

                    @Override
                    public void onSuccess(FinancialAddMoveResponseDto data, String msg, String rawJson) {
                        if (showDialog) hideProgressHUD();

                        if (msg != null && !msg.trim().isEmpty()) toast(msg);

                        if (data == null) {
                            toast(getString(co.highfive.petrolstation.R.string.general_error));
                            return;
                        }

                        if (data.setting != null) {
                            if (!data.setting.getVersion_app().equals(BuildConfig.VERSION_NAME)) {
                                openUpdateAppDialog(
                                        data.setting.getUpdate_title(),
                                        data.setting.getUpdate_description(),
                                        data.setting.getUrl_app()
                                );
                            }
                        }
                        finish();

                        // same behavior as old:
                        printMove(data.setting, data.move);
                    }

                    @Override
                    public void onError(ApiError error) {
                        if (showDialog) hideProgressHUD();
                        toast(error != null ? error.message : getString(co.highfive.petrolstation.R.string.general_error));
                    }

                    @Override
                    public void onUnauthorized(String rawJson) {
                        if (showDialog) hideProgressHUD();
                        logout();
                    }

                    @Override
                    public void onNetworkError(String reason) {
                        if (showDialog) hideProgressHUD();
                        toast(co.highfive.petrolstation.R.string.no_internet);
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        if (showDialog) hideProgressHUD();
                        errorLogger("FinancialAddMoveParseError", e.getMessage() == null ? "null" : e.getMessage());
                        toast(getString(co.highfive.petrolstation.R.string.general_error));
                    }
                }
        );
    }


    private void addLoad(boolean showDialog) {

        if (account == null || account.getId() == null) {
            toast(getString(co.highfive.petrolstation.R.string.general_error));
            return;
        }

        if (showDialog) showProgressHUD();

        Map<String, String> payload = ApiClient.mapOf(
                "statement", safe(statement.getText().toString()),
                "amount", safe(amount.getText().toString()),
                "currency", safe(currency_id),
                "account_id", String.valueOf(account.getId()),
                "notes", safe(notes.getText().toString()),
                "type_move", safe(transaction_type_id)
//                ,  "month_load", safe(transaction_month_id)
        );

        Type type = new TypeToken<BaseResponse<Object>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_POST,
                Endpoints.FINANCIAL_ADDLOAD,
                payload,
                null,
                type,
                0,
                new ApiCallback<Object>() {

                    @Override
                    public void onSuccess(Object data, String msg, String rawJson) {
                        if (showDialog) hideProgressHUD();
                        toast(msg != null && !msg.trim().isEmpty() ? msg : getString(co.highfive.petrolstation.R.string.done));
                        finish();
                    }

                    @Override
                    public void onError(ApiError error) {
                        if (showDialog) hideProgressHUD();
                        toast(error != null ? error.message : getString(co.highfive.petrolstation.R.string.general_error));
                    }

                    @Override
                    public void onUnauthorized(String rawJson) {
                        if (showDialog) hideProgressHUD();
                        logout();
                    }

                    @Override
                    public void onNetworkError(String reason) {
                        if (showDialog) hideProgressHUD();
                        toast(co.highfive.petrolstation.R.string.no_internet);
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        if (showDialog) hideProgressHUD();
                        errorLogger("FinancialAddLoadParseError", e.getMessage() == null ? "null" : e.getMessage());
                        toast(getString(co.highfive.petrolstation.R.string.general_error));
                    }
                }
        );
    }

    private void addDiscount(boolean showDialog) {

        if (account == null || account.getId() == null) {
            toast(getString(co.highfive.petrolstation.R.string.general_error));
            return;
        }

        if (showDialog) showProgressHUD();

        Map<String, String> payload = ApiClient.mapOf(
                "statement", safe(statement.getText().toString()),
                "amount", safe(amount.getText().toString()),
                "currency", safe(currency_id),
                "account_id", String.valueOf(account.getId()),
                "notes", safe(notes.getText().toString())
        );

        Type type = new TypeToken<BaseResponse<Object>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_POST,
                Endpoints.FINANCIAL_ADDDISCOUNT,
                payload,
                null,
                type,
                0,
                new ApiCallback<Object>() {

                    @Override
                    public void onSuccess(Object data, String msg, String rawJson) {
                        if (showDialog) hideProgressHUD();
                        toast(msg != null && !msg.trim().isEmpty() ? msg : getString(co.highfive.petrolstation.R.string.done));
                        finish();
                    }

                    @Override
                    public void onError(ApiError error) {
                        if (showDialog) hideProgressHUD();
                        toast(error != null ? error.message : getString(co.highfive.petrolstation.R.string.general_error));
                    }

                    @Override
                    public void onUnauthorized(String rawJson) {
                        if (showDialog) hideProgressHUD();
                        logout();
                    }

                    @Override
                    public void onNetworkError(String reason) {
                        if (showDialog) hideProgressHUD();
                        toast(co.highfive.petrolstation.R.string.no_internet);
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        if (showDialog) hideProgressHUD();
                        errorLogger("FinancialAddDiscountParseError", e.getMessage() == null ? "null" : e.getMessage());
                        toast(getString(co.highfive.petrolstation.R.string.general_error));
                    }
                }
        );
    }

    public void addLocalMove(boolean isShowDialog){


    }

    public void addLocalLoad(boolean isShowDialog){

    }

    public void addLocalDiscount(boolean isShowDialog){

    }


}
