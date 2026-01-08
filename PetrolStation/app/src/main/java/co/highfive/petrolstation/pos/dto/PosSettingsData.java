package co.highfive.petrolstation.pos.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import co.highfive.petrolstation.auth.dto.SettingDto;
import co.highfive.petrolstation.customers_settings.dto.LookupDto;

public class PosSettingsData {

    @SerializedName("category")
    public List<LookupDto> category;

    @SerializedName("payment_type")
    public List<LookupDto> payment_type;

    @SerializedName("setting")
    public SettingDto setting;
}
