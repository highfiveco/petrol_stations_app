package co.highfive.petrolstation.settings.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CompanySettingData {

    @SerializedName("setting")
    public CompanySettingDto setting;

    // flags / permissions
    @SerializedName("sms") public Integer sms;
    @SerializedName("view_log") public Integer view_log;
    @SerializedName("view_financial_move") public Integer view_financial_move;
    @SerializedName("update_customers") public Integer update_customers;
    @SerializedName("add_customers") public Integer add_customers;
    @SerializedName("view_reminders") public Integer view_reminders;
    @SerializedName("add_reminders") public Integer add_reminders;
    @SerializedName("delete_reminders") public Integer delete_reminders;
    @SerializedName("update_mobile") public Integer update_mobile;
    @SerializedName("show_json_app") public Integer show_json_app;
    @SerializedName("fuel_sales") public Integer fuel_sales;
    @SerializedName("pos") public Integer pos;
    @SerializedName("delete_invoices") public Integer delete_invoices;
    @SerializedName("view_invoices") public Integer view_invoices;

    @SerializedName("default_type_income") public Integer default_type_income;

    @SerializedName("has_move") public Integer has_move;
    @SerializedName("has_load") public Integer has_load;
    @SerializedName("has_discount") public Integer has_discount;

    @SerializedName("payment_type_default") public Integer payment_type_default;

    @SerializedName("view_statement") public Integer view_statement;
    @SerializedName("view_date") public Integer view_date;
    @SerializedName("disabled_type_move") public Integer disabled_type_move;

    @SerializedName("customer_invoices") public Integer customer_invoices;
    @SerializedName("customer_fuel_sales") public Integer customer_fuel_sales;
    @SerializedName("co/highfive/petrolstation/financial") public Integer financial;
    @SerializedName("customers") public Integer customers;
    @SerializedName("fund_financial") public Integer fund_financial;

    @SerializedName("achievement") public Integer achievement;

    // objects / lists
    @SerializedName("about_app") public AboutAppDto about_app;

    @SerializedName("status_fund") public List<IdNameDto> status_fund;
    @SerializedName("currency") public List<IdNameDto> currency;
    @SerializedName("fund_to") public List<IdNameDto> fund_to;
    @SerializedName("users") public List<IdNameDto> users;
    @SerializedName("type_loaded") public List<IdNameDto> type_loaded;
    @SerializedName("type_income") public List<IdNameDto> type_income;
    @SerializedName("payment_type") public List<IdNameDto> payment_type;
    @SerializedName("bank") public List<IdNameDto> bank;
    @SerializedName("type") public List<IdNameDto> type;
    @SerializedName("account_type") public List<IdNameDto> account_type;

    @SerializedName("customer_status") public List<CustomerStatusDto> customer_status;

    // misc
    @SerializedName("user_sanad") public Integer user_sanad;
    @SerializedName("user_company_code") public String user_company_code;
}
