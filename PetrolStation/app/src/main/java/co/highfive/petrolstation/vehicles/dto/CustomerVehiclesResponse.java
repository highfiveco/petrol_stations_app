package co.highfive.petrolstation.vehicles.dto;

import com.google.gson.annotations.SerializedName;

public class CustomerVehiclesResponse {

    @SerializedName("status")
    public boolean status;

    @SerializedName("message")
    public String message;

    // success: object
    // fail: "" أو null
    @SerializedName("data")
    public Object data;

    public CustomerVehiclesData getDataOrNull(com.google.gson.Gson gson) {
        if (!status || data == null) return null;

        try {
            if (data instanceof String) {
                String s = ((String) data).trim();
                if (s.isEmpty()) return null;
            }
            String json = gson.toJson(data);
            return gson.fromJson(json, CustomerVehiclesData.class);
        } catch (Exception ignore) {
            return null;
        }
    }
}
