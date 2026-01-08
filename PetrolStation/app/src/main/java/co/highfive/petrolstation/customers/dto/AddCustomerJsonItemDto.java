package co.highfive.petrolstation.customers.dto;

import com.google.gson.annotations.SerializedName;

public class AddCustomerJsonItemDto {

    @SerializedName("name")
    public String name;

    @SerializedName("mobile")
    public String mobile;

    @SerializedName("status")
    public Integer status;

    @SerializedName("type_customer")
    public Integer type_customer;

    @SerializedName("customer_classify")
    public Integer customer_classify;

    @SerializedName("asseal_no")
    public Integer asseal_no;

    @SerializedName("address")
    public String address;
}
