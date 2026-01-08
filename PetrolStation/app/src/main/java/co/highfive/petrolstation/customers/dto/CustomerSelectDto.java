package co.highfive.petrolstation.customers.dto;

import com.google.gson.annotations.SerializedName;

public class CustomerSelectDto {

    @SerializedName("id")
    public int id;

    @SerializedName("name")
    public String name;

    @SerializedName("account_id")
    public Integer account_id;

    @SerializedName("campaign_name")
    public String campaign_name;

    @SerializedName("remaining_amount")
    public Double remaining_amount;
}
