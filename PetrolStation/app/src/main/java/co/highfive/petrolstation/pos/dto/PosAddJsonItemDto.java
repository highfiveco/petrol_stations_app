package co.highfive.petrolstation.pos.dto;

import com.google.gson.annotations.SerializedName;

public class PosAddJsonItemDto {

    @SerializedName("item_id")
    public Integer item_id;

    @SerializedName("price")
    public Double price;

    @SerializedName("count")
    public Double count;
}
