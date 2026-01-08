package co.highfive.petrolstation.customers.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import co.highfive.petrolstation.auth.dto.SettingDto;

public class CustomersData {
    @SerializedName("customers") public List<CustomerDto> customers;

    // flags/permissions أحياناً بترجع في customers و offline
    @SerializedName("sms") public Integer sms;
    @SerializedName("view_log") public Integer view_log;
    @SerializedName("view_financial_move") public Integer view_financial_move;
    @SerializedName("update_customers") public Integer update_customers;
    @SerializedName("add_customers") public Integer add_customers;
    @SerializedName("view_reminders") public Integer view_reminders;
    @SerializedName("add_reminders") public Integer add_reminders;
    @SerializedName("delete_reminders") public Integer delete_reminders;
    @SerializedName("update_mobile") public Integer update_mobile;
    @SerializedName("view_customer_vehicles") public Integer view_customer_vehicles;

    @SerializedName("setting") public SettingDto setting;
}
