package co.highfive.petrolstation.vehicles.dto;

import com.google.gson.annotations.SerializedName;
import co.highfive.petrolstation.auth.dto.SettingDto;

public class VehicleSyncResponse {

    @SerializedName("status")
    public boolean status;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public VehicleSyncResponseData data;

    @SerializedName("setting")
    public SettingDto setting;
}
