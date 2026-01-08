package co.highfive.petrolstation.fuelsale.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FuelPriceOfflineInvoiceRequestDto {

    @SerializedName("date")
    public String date; // "YYYY-MM-DD"

    @SerializedName("account_id")
    public Integer account_id;

    @SerializedName("customer_vehicle_id")
    public Integer customer_vehicle_id;

    @SerializedName("pump_id")
    public Integer pump_id;

    @SerializedName("campaign_id")
    public Integer campaign_id; // ممكن null

    @SerializedName("discount")
    public Double discount;

    @SerializedName("notes")
    public String notes;

    @SerializedName("items")
    public List<FuelPriceOfflineItemDto> items;

    @SerializedName("payment_methods")
    public List<FuelPriceOfflinePaymentDto> payment_methods;
}
