package co.highfive.petrolstation.fuelsale.dto;

import com.google.gson.annotations.SerializedName;

public class PaymentMethodDto {
    @SerializedName("payment_type_id")
    public int payment_type_id;

    @SerializedName("amount")
    public double amount;

    public PaymentMethodDto(int payment_type_id, double amount) {
        this.payment_type_id = payment_type_id;
        this.amount = amount;
    }
}
