package co.highfive.petrolstation.pos.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PosAddJsonInvoiceDto {

    @SerializedName("date")
    public String date;

    @SerializedName("account_id")
    public Integer account_id;

    @SerializedName("discount")
    public Double discount;

    @SerializedName("notes")
    public String notes;

    @SerializedName("items")
    public List<PosAddJsonItemDto> items;

    @SerializedName("payment_methods")
    public List<PosPaymentMethodDto> payment_methods;
}
