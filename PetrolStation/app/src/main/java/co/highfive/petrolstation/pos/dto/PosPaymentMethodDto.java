package co.highfive.petrolstation.pos.dto;

import com.google.gson.annotations.SerializedName;

public class PosPaymentMethodDto {

    @SerializedName("payment_type_id")
    public Integer payment_type_id;

    @SerializedName("amount")
    public Double amount;

    // Optional (يظهر أحياناً)
    @SerializedName("mobile")
    public String mobile;
}
