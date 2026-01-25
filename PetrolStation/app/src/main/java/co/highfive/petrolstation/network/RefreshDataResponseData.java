package co.highfive.petrolstation.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import co.highfive.petrolstation.auth.dto.SettingDto;
import co.highfive.petrolstation.customers.dto.CustomerDto;

public class RefreshDataResponseData {

    @SerializedName("customers")
    private List<CustomerDto> customers;

    @SerializedName("sms")
    private Integer sms;

    @SerializedName("view_log")
    private Integer viewLog;

    @SerializedName("view_financial_move")
    private Integer viewFinancialMove;

    @SerializedName("update_customers")
    private Integer updateCustomers;

    @SerializedName("add_customers")
    private Integer addCustomers;

    @SerializedName("view_reminders")
    private Integer viewReminders;

    @SerializedName("add_reminders")
    private Integer addReminders;

    @SerializedName("delete_reminders")
    private Integer deleteReminders;

    @SerializedName("update_mobile")
    private Integer updateMobile;

    @SerializedName("view_customer_vehicles")
    private Integer viewCustomerVehicles;

    @SerializedName("add_customer_vehicles")
    private Integer addCustomerVehicles;

    @SerializedName("edit_customer_vehicles")
    private Integer editCustomerVehicles;

    @SerializedName("setting")
    private SettingDto setting;

    public List<CustomerDto> getCustomers() { return customers; }
    public SettingDto getSetting() { return setting; }

    public Integer getSms() { return sms; }
    public Integer getViewLog() { return viewLog; }
    public Integer getViewFinancialMove() { return viewFinancialMove; }
    public Integer getUpdateCustomers() { return updateCustomers; }
    public Integer getAddCustomers() { return addCustomers; }
    public Integer getViewReminders() { return viewReminders; }
    public Integer getAddReminders() { return addReminders; }
    public Integer getDeleteReminders() { return deleteReminders; }
    public Integer getUpdateMobile() { return updateMobile; }
    public Integer getViewCustomerVehicles() { return viewCustomerVehicles; }

    public Integer getAddCustomerVehicles() {
        return addCustomerVehicles;
    }

    public Integer getEditCustomerVehicles() {
        return editCustomerVehicles;
    }
}
