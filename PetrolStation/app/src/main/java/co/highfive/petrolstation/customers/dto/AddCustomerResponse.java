package co.highfive.petrolstation.customers.dto;

import com.google.gson.annotations.SerializedName;

import co.highfive.petrolstation.auth.dto.SettingDto;

public class AddCustomerResponse {

    @SerializedName("status")
    public boolean status;

    @SerializedName("message")
    public String message;

    // نفس شكل customer object اللي رجع في "data"
    @SerializedName("data")
    public CustomerDto data;

    // ملاحظة: setting هنا ليست داخل data
    @SerializedName("setting")
    public SettingDto setting;
}
