package co.highfive.petrolstation.fuelsale.dto;

import com.google.gson.annotations.SerializedName;
import co.highfive.petrolstation.auth.dto.SettingDto;

public class FuelPriceAddJsonResponse {

    @SerializedName("status")
    public boolean status;

    @SerializedName("message")
    public String message;

    // عادة object، لكن نخليها آمنة
    @SerializedName("data")
    public Object data;

    @SerializedName("setting")
    public SettingDto setting;

    public FuelPriceAddJsonResponseData getDataOrNull(com.google.gson.Gson gson) {
        if (!status || data == null) return null;
        try {
            if (data instanceof String) {
                String s = ((String) data).trim();
                if (s.isEmpty()) return null;
            }
            String json = gson.toJson(data);
            return gson.fromJson(json, FuelPriceAddJsonResponseData.class);
        } catch (Exception ignore) {
            return null;
        }
    }
}
