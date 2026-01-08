package co.highfive.petrolstation.fuelsale.dto;

import com.google.gson.annotations.SerializedName;

public class FuelPriceOfflinePaymentDto {

    @SerializedName("payment_type_id")
    public Integer payment_type_id;

    @SerializedName("amount")
    public Double amount;

    // يظهر أحياناً
    @SerializedName("mobile")
    public String mobile;
}
