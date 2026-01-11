package co.highfive.petrolstation.customers.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import co.highfive.petrolstation.auth.dto.SettingDto;

public class VehicleSettingsResponseDto {

    @SerializedName("vehicle_type")
    public List<SimpleSettingDto> vehicle_type;

    @SerializedName("vehicle_color")
    public List<SimpleSettingDto> vehicle_color;

    // حسب اتفاقنا: الموديل حيكون بنفس vehicle_type
    // فإذا صار يرجع من API مستقبلاً
    @SerializedName("model")
    public List<SimpleSettingDto> model;

    @SerializedName("setting")
    public SettingDto setting;
}
