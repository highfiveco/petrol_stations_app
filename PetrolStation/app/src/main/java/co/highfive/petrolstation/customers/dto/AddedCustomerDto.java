package co.highfive.petrolstation.customers.dto;

import com.google.gson.annotations.SerializedName;

public class AddedCustomerDto {

    @SerializedName("id")
    public int id;

    @SerializedName("name")
    public String name;

    @SerializedName("account_id")
    public Integer account_id;

    @SerializedName("mobile")
    public String mobile;

    @SerializedName("account_no")
    public String account_no;
}
