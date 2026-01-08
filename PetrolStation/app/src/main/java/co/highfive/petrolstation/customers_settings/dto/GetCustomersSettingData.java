package co.highfive.petrolstation.customers_settings.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GetCustomersSettingData {

    @SerializedName("setting")
    public CustomersSettingDto setting;

    @SerializedName("customer_status")
    public List<CustomerStatusDto> customer_status;

    @SerializedName("customer_classify")
    public List<LookupDto> customer_classify;

    @SerializedName("type_customer")
    public List<LookupDto> type_customer;

    @SerializedName("users")
    public List<UserLiteDto> users;
}
