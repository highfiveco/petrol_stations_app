package co.highfive.petrolstation.vehicles.dto;

import com.google.gson.annotations.SerializedName;
import co.highfive.petrolstation.auth.dto.SettingDto;
import co.highfive.petrolstation.customers.dto.CustomerVehicleDto;

public class AddCustomerVehicleResponse {

    @SerializedName("status")
    public boolean status;

    @SerializedName("message")
    public String message;

    // success: object
    // fail: "" أو null
    @SerializedName("data")
    public Object data;

    @SerializedName("setting")
    public SettingDto setting;

    public CustomerVehicleDto getVehicleOrNull(com.google.gson.Gson gson) {
        if (!status || data == null) return null;
        try {
            if (data instanceof String) {
                String s = ((String) data).trim();
                if (s.isEmpty()) return null;
            }
            String json = gson.toJson(data);
            return gson.fromJson(json, CustomerVehicleDto.class);
        } catch (Exception ignore) {
            return null;
        }
    }
}
