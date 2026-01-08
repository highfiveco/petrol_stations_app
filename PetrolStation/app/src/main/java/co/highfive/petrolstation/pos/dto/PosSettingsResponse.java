package co.highfive.petrolstation.pos.dto;

import com.google.gson.annotations.SerializedName;

public class PosSettingsResponse {

    @SerializedName("status")
    public boolean status;

    @SerializedName("message")
    public String message;

    // success: object
    // fail: "" أو null أحياناً
    @SerializedName("data")
    public Object data;

    public PosSettingsData getDataOrNull(com.google.gson.Gson gson) {
        if (!status || data == null) return null;
        try {
            if (data instanceof String) {
                String s = ((String) data).trim();
                if (s.isEmpty()) return null;
            }
            String json = gson.toJson(data);
            return gson.fromJson(json, PosSettingsData.class);
        } catch (Exception ignore) {
            return null;
        }
    }
}
