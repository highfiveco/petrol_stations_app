package co.highfive.petrolstation.pos.dto;

import com.google.gson.annotations.SerializedName;

public class PosDraftItemDto {

    @SerializedName("item_id")
    public int itemId;

    @SerializedName("name")
    public String name;

    @SerializedName("price")
    public double price;

    @SerializedName("qty")
    public int qty;
}
