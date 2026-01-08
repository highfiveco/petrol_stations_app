package co.highfive.petrolstation.pos.dto;

import com.google.gson.annotations.SerializedName;

public class PosItemsResponse {

    @SerializedName("status")
    public boolean status;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public Object data;

    public PosItemsData getDataOrNull(com.google.gson.Gson gson) {
        if (!status || data == null) return null;
        try {
            if (data instanceof String) {
                String s = ((String) data).trim();
                if (s.isEmpty()) return null;
            }
            String json = gson.toJson(data);
            return gson.fromJson(json, PosItemsData.class);
        } catch (Exception ignore) {
            return null;
        }
    }
}
