package co.highfive.petrolstation.customers.dto;

import com.google.gson.annotations.SerializedName;

public class InvoiceDetailDto {
    @SerializedName("id") public int id;
    @SerializedName("item_id") public Integer item_id;
    @SerializedName("update_cost_price") public Integer update_cost_price;
    @SerializedName("count") public Double count;
    @SerializedName("price") public Double price;
    @SerializedName("invoice_id") public Integer invoice_id;

    // نخزن item JSON في Room
    @SerializedName("item") public Object item;
}
