package co.highfive.petrolstation.fuelsale.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import co.highfive.petrolstation.auth.dto.SettingDto;
import co.highfive.petrolstation.customers_settings.dto.LookupDto;

public class FuelPriceSettingsData {

    @SerializedName("payment_type")
    public List<LookupDto> payment_type;

    @SerializedName("pumps")
    public List<PumpDto> pumps;

    @SerializedName("campaigns")
    public List<FuelCampaignDto> campaigns;

    @SerializedName("items")
    public List<FuelItemDto> items;

    @SerializedName("setting")
    public SettingDto setting;
}
