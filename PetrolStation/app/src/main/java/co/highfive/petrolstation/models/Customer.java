package co.highfive.petrolstation.models;

import java.util.ArrayList;

public class Customer {
    private String id;
    private String account_id;
    private String name;
    private String mobile;
    private String customer_classify;
    private String customer_classify_name;
    private String status;
    private String customer_status;
    private String balance;
    private String accumulated_account;
    private String cumulative;
    private String remain_monthly;
    private String month;
    private String pdf_url;
    private String excel_url;
    private String total_price;
    private String curnt_value;
    private String last_value;
    private String diff;
    private String kilo_price;
    private String app_type;
    private String min_amount;
    private String deleted;
    private String add_user_id;
    private String insert_date;
    private String price;
    private String address;
    private String subscription_fees;
    private String subscription_fees_date;
    private String name_conductor;
    private String first_area;
    private String second_area;
    private String customer_order;
    private String company_id;
    private String ampere_count;
    private String app_type_2;
    private String switch_no;
    private String is_constant_pay;
    private String other_information;
    private String financial_safe;
    private String user_update;
    private String user_update_date;
    private String browser;
    private String created_at;
    private String updated_at;
    private String subscription_fee_check;
    private String send_sms;
    private String baqi;
    private String read;
    private String tablon_no;
    private String service_customer_check;
    private String breakers_id;
    private String prev_read;
    private String now_price;
    private String customer_id;
    private String collector_id;
    private String type_customer;
    private String type_customer_name;
    private String accounting_type;
    private String accounting_type_name;
    private String added_from;
    private String app_type_name;
    private String app_type_2_name;
    private String first_area_name;
    private String second_area_name;
    private String currency_name;
    private String aesseal_no;
    private String tablon_name;
    private String breakers_name;
    private String collector_name;
    private String check_pay;
    private String account_currency;
    private String name_currency;
    private ArrayList<CustomerReading> readings;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCustomer_status() {
        return customer_status;
    }

    public void setCustomer_status(String customer_status) {
        this.customer_status = customer_status;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getCurnt_value() {
        return curnt_value;
    }

    public void setCurnt_value(String curnt_value) {
        this.curnt_value = curnt_value;
    }

    public String getLast_value() {
        return last_value;
    }

    public void setLast_value(String last_value) {
        this.last_value = last_value;
    }

    public String getDiff() {
        return diff;
    }

    public void setDiff(String diff) {
        this.diff = diff;
    }

    public String getKilo_price() {
        return kilo_price;
    }

    public void setKilo_price(String kilo_price) {
        this.kilo_price = kilo_price;
    }

    public String getAccount_id() {
        return account_id;
    }

    public void setAccount_id(String account_id) {
        this.account_id = account_id;
    }



    public String getApp_type() {
        return app_type;
    }

    public void setApp_type(String app_type) {
        this.app_type = app_type;
    }

    public String getMin_amount() {
        return min_amount;
    }

    public void setMin_amount(String min_amount) {
        this.min_amount = min_amount;
    }

    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

    public String getAdd_user_id() {
        return add_user_id;
    }

    public void setAdd_user_id(String add_user_id) {
        this.add_user_id = add_user_id;
    }

    public String getInsert_date() {
        return insert_date;
    }

    public void setInsert_date(String insert_date) {
        this.insert_date = insert_date;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSubscription_fees() {
        return subscription_fees;
    }

    public void setSubscription_fees(String subscription_fees) {
        this.subscription_fees = subscription_fees;
    }

    public String getSubscription_fees_date() {
        return subscription_fees_date;
    }

    public void setSubscription_fees_date(String subscription_fees_date) {
        this.subscription_fees_date = subscription_fees_date;
    }

    public String getName_conductor() {
        return name_conductor;
    }

    public void setName_conductor(String name_conductor) {
        this.name_conductor = name_conductor;
    }

    public String getFirst_area() {
        return first_area;
    }

    public void setFirst_area(String first_area) {
        this.first_area = first_area;
    }

    public String getSecond_area() {
        return second_area;
    }

    public void setSecond_area(String second_area) {
        this.second_area = second_area;
    }

    public String getCustomer_order() {
        return customer_order;
    }

    public void setCustomer_order(String customer_order) {
        this.customer_order = customer_order;
    }

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }

    public String getAmpere_count() {
        return ampere_count;
    }

    public void setAmpere_count(String ampere_count) {
        this.ampere_count = ampere_count;
    }

    public String getApp_type_2() {
        return app_type_2;
    }

    public void setApp_type_2(String app_type_2) {
        this.app_type_2 = app_type_2;
    }

    public String getSwitch_no() {
        return switch_no;
    }

    public void setSwitch_no(String switch_no) {
        this.switch_no = switch_no;
    }

    public String getIs_constant_pay() {
        return is_constant_pay;
    }

    public void setIs_constant_pay(String is_constant_pay) {
        this.is_constant_pay = is_constant_pay;
    }

    public String getOther_information() {
        return other_information;
    }

    public void setOther_information(String other_information) {
        this.other_information = other_information;
    }

    public String getFinancial_safe() {
        return financial_safe;
    }

    public void setFinancial_safe(String financial_safe) {
        this.financial_safe = financial_safe;
    }

    public String getUser_update() {
        return user_update;
    }

    public void setUser_update(String user_update) {
        this.user_update = user_update;
    }

    public String getUser_update_date() {
        return user_update_date;
    }

    public void setUser_update_date(String user_update_date) {
        this.user_update_date = user_update_date;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getSubscription_fee_check() {
        return subscription_fee_check;
    }

    public void setSubscription_fee_check(String subscription_fee_check) {
        this.subscription_fee_check = subscription_fee_check;
    }

    public String getSend_sms() {
        return send_sms;
    }

    public void setSend_sms(String send_sms) {
        this.send_sms = send_sms;
    }

    public String getBaqi() {
        return baqi;
    }

    public void setBaqi(String baqi) {
        this.baqi = baqi;
    }

    public String getRead() {
        return read;
    }

    public void setRead(String read) {
        this.read = read;
    }

    public String getTablon_no() {
        return tablon_no;
    }

    public void setTablon_no(String tablon_no) {
        this.tablon_no = tablon_no;
    }

    public String getService_customer_check() {
        return service_customer_check;
    }

    public void setService_customer_check(String service_customer_check) {
        this.service_customer_check = service_customer_check;
    }

    public String getBreakers_id() {
        return breakers_id;
    }

    public void setBreakers_id(String breakers_id) {
        this.breakers_id = breakers_id;
    }

    public String getPrev_read() {
        return prev_read;
    }

    public void setPrev_read(String prev_read) {
        this.prev_read = prev_read;
    }

    public String getNow_price() {
        return now_price;
    }

    public void setNow_price(String now_price) {
        this.now_price = now_price;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }

    public String getCollector_id() {
        return collector_id;
    }

    public void setCollector_id(String collector_id) {
        this.collector_id = collector_id;
    }

    public String getType_customer() {
        return type_customer;
    }

    public void setType_customer(String type_customer) {
        this.type_customer = type_customer;
    }

    public String getAccounting_type() {
        return accounting_type;
    }

    public void setAccounting_type(String accounting_type) {
        this.accounting_type = accounting_type;
    }

    public String getAdded_from() {
        return added_from;
    }

    public void setAdded_from(String added_from) {
        this.added_from = added_from;
    }

    public String getApp_type_name() {
        return app_type_name;
    }

    public void setApp_type_name(String app_type_name) {
        this.app_type_name = app_type_name;
    }

    public String getFirst_area_name() {
        return first_area_name;
    }

    public void setFirst_area_name(String first_area_name) {
        this.first_area_name = first_area_name;
    }

    public String getSecond_area_name() {
        return second_area_name;
    }

    public void setSecond_area_name(String second_area_name) {
        this.second_area_name = second_area_name;
    }

    public String getCurrency_name() {
        return currency_name;
    }

    public void setCurrency_name(String currency_name) {
        this.currency_name = currency_name;
    }

    public String getApp_type_2_name() {
        return app_type_2_name;
    }

    public void setApp_type_2_name(String app_type_2_name) {
        this.app_type_2_name = app_type_2_name;
    }

    public String getAesseal_no() {
        return aesseal_no;
    }

    public void setAesseal_no(String aesseal_no) {
        this.aesseal_no = aesseal_no;
    }

    public String getTablon_name() {
        return tablon_name;
    }

    public void setTablon_name(String tablon_name) {
        this.tablon_name = tablon_name;
    }

    public String getBreakers_name() {
        return breakers_name;
    }

    public void setBreakers_name(String breakers_name) {
        this.breakers_name = breakers_name;
    }

    public String getType_customer_name() {
        return type_customer_name;
    }

    public void setType_customer_name(String type_customer_name) {
        this.type_customer_name = type_customer_name;
    }

    public String getCollector_name() {
        return collector_name;
    }

    public void setCollector_name(String collector_name) {
        this.collector_name = collector_name;
    }

    public String getAccounting_type_name() {
        return accounting_type_name;
    }

    public void setAccounting_type_name(String accounting_type_name) {
        this.accounting_type_name = accounting_type_name;
    }

    public String getAccumulated_account() {
        return accumulated_account;
    }

    public void setAccumulated_account(String accumulated_account) {
        this.accumulated_account = accumulated_account;
    }

    public String getTotal_price() {
        return total_price;
    }

    public void setTotal_price(String total_price) {
        this.total_price = total_price;
    }

    public String getCumulative() {
        return cumulative;
    }

    public void setCumulative(String cumulative) {
        this.cumulative = cumulative;
    }

    public String getRemain_monthly() {
        return remain_monthly;
    }

    public void setRemain_monthly(String remain_monthly) {
        this.remain_monthly = remain_monthly;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getExcel_url() {
        return excel_url;
    }

    public void setExcel_url(String excel_url) {
        this.excel_url = excel_url;
    }

    public String getPdf_url() {
        return pdf_url;
    }

    public void setPdf_url(String pdf_url) {
        this.pdf_url = pdf_url;
    }

    public String getCheck_pay() {
        return check_pay;
    }

    public void setCheck_pay(String check_pay) {
        this.check_pay = check_pay;
    }

    public ArrayList<CustomerReading> getReadings() {
        return readings;
    }

    public void setReadings(ArrayList<CustomerReading> readings) {
        this.readings = readings;
    }

    public String getAccount_currency() {
        return account_currency;
    }

    public void setAccount_currency(String account_currency) {
        this.account_currency = account_currency;
    }

    public String getName_currency() {
        return name_currency;
    }

    public void setName_currency(String name_currency) {
        this.name_currency = name_currency;
    }

    public String getCustomer_classify() {
        return customer_classify;
    }

    public void setCustomer_classify(String customer_classify) {
        this.customer_classify = customer_classify;
    }

    public String getCustomer_classify_name() {
        return customer_classify_name;
    }

    public void setCustomer_classify_name(String customer_classify_name) {
        this.customer_classify_name = customer_classify_name;
    }
}
