package co.highfive.petrolstation.customers.dto;

import com.google.gson.annotations.SerializedName;

import co.highfive.petrolstation.fuelsale.dto.FuelItemDto;

public class FuelSaleDetailDto {
    @SerializedName("id") public int id;
    @SerializedName("item_id") public int item_id;

    @SerializedName("update_cost_price") public double update_cost_price;
    @SerializedName("count") public double count;
    @SerializedName("price") public double price;

    @SerializedName("invoice_id") public int invoice_id;

    @SerializedName("item") public FuelItemDto item; // انت رافع FuelItemDto
}
