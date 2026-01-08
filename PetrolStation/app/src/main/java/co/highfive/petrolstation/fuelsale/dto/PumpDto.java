package co.highfive.petrolstation.fuelsale.dto;

import com.google.gson.annotations.SerializedName;

public class PumpDto {
    @SerializedName("id") public int id;
    @SerializedName("name") public String name;
    @SerializedName("icon") public String icon;
}
