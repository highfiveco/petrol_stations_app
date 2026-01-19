package co.highfive.petrolstation.pos.dto;

import com.google.gson.annotations.SerializedName;

public class PosItemsData {

    @SerializedName("setting")
    public PosSettingDto setting;

    @SerializedName("items")
    public PosItemsPage items;
}
