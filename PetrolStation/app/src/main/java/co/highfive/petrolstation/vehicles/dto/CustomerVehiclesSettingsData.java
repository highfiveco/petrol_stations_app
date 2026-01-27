package co.highfive.petrolstation.vehicles.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import co.highfive.petrolstation.auth.dto.SettingDto;
import co.highfive.petrolstation.customers_settings.dto.LookupDto;

public class CustomerVehiclesSettingsData {

    @SerializedName("vehicle_type")
    public List<LookupDto> vehicle_type;

    @SerializedName("vehicle_color")
    public List<LookupDto> vehicle_color;

    @SerializedName("model")
    public List<LookupDto> model;

    @SerializedName("setting")
    public SettingDto setting;
}
