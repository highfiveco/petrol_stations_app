package co.highfive.petrolstation.pos.dto;

import com.google.gson.annotations.SerializedName;

public class PosItemDto {
    @SerializedName("id")
    public int id;

    @SerializedName("name")
    public String name;

    @SerializedName("category")
    public int category;

    @SerializedName("negative_check")
    public int negative_check;

    @SerializedName("price")
    public double price;

    @SerializedName("barcode")
    public String barcode;

    @SerializedName("icon")
    public String icon;
}
