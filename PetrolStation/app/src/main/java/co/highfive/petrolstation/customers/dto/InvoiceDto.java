package co.highfive.petrolstation.customers.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class InvoiceDto {
    @SerializedName("id") public int id;
    @SerializedName("date") public String date;
    @SerializedName("statement") public String statement;
    @SerializedName("account_id") public Integer account_id;
    @SerializedName("store_id") public Integer store_id;
    @SerializedName("discount") public Double discount;
    @SerializedName("total") public Double total;
    @SerializedName("invoice_no") public String invoice_no;
    @SerializedName("notes") public String notes;
    @SerializedName("campaign_id") public Integer campaign_id;
    @SerializedName("pump_id") public Integer pump_id;
    @SerializedName("customer_vehicle_id") public Integer customer_vehicle_id;
    @SerializedName("is_fuel_sale") public Integer is_fuel_sale;
    @SerializedName("created_at") public String created_at;

    @SerializedName("details") public List<InvoiceDetailDto> details;

    // رح نخزنهم JSON في Room (لكن DTO لازم يفهمهم)
    @SerializedName("account") public Object account;
    @SerializedName("pump") public Object pump;
    @SerializedName("customer_vehicle") public Object customer_vehicle;
    @SerializedName("campaign") public Object campaign;

    @SerializedName("pay_amount")
    public Double pay_amount;
}
