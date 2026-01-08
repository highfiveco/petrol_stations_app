package co.highfive.petrolstation.customers.dto;

import com.google.gson.annotations.SerializedName;

public class UpdateMobileJsonItemDto {

    @SerializedName("customer_id")
    public String customer_id; // جاي كنص

    @SerializedName("phone")
    public String phone;
}
