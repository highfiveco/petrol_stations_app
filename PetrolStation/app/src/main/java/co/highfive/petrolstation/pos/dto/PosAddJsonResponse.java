package co.highfive.petrolstation.pos.dto;

import com.google.gson.annotations.SerializedName;

import co.highfive.petrolstation.auth.dto.SettingDto;

public class PosAddJsonResponse {

    @SerializedName("status")
    public boolean status;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public PosAddJsonDataDto data;

    @SerializedName("setting")
    public SettingDto setting;
}
