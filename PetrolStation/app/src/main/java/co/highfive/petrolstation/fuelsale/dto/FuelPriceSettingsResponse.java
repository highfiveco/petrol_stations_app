package co.highfive.petrolstation.fuelsale.dto;

import com.google.gson.annotations.SerializedName;

public class FuelPriceSettingsResponse {

    @SerializedName("status")
    public boolean status;

    @SerializedName("message")
    public String message;

    // success: object
    // fail: "" أو null
    @SerializedName("data")
    public Object data;

    public FuelPriceSettingsData getDataOrNull(com.google.gson.Gson gson) {
        if (!status || data == null) return null;
        try {
            if (data instanceof String) {
                String s = ((String) data).trim();
                if (s.isEmpty()) return null;
            }
            String json = gson.toJson(data);
            return gson.fromJson(json, FuelPriceSettingsData.class);
        } catch (Exception ignore) {
            return null;
        }
    }
}
