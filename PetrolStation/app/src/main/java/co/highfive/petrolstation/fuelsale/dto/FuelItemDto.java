package co.highfive.petrolstation.fuelsale.dto;

import com.google.gson.annotations.SerializedName;

public class FuelItemDto {
    @SerializedName("id") public int id;
    @SerializedName("name") public String name;
    @SerializedName("negative_check") public Integer negative_check;
    @SerializedName("price") public Double price;
    @SerializedName("barcode") public String barcode;
    @SerializedName("icon") public String icon;
}
