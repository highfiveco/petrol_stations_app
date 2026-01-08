package co.highfive.petrolstation.customers.dto;

import com.google.gson.annotations.SerializedName;
import co.highfive.petrolstation.auth.dto.SettingDto;

public class UpdateCustomerResponse {

    @SerializedName("status")
    public boolean status;

    @SerializedName("message")
    public String message;

    // عند الفشل أحياناً "data": "" (string)
    // فنجعله Object لتجنب JsonSyntaxException
    @SerializedName("data")
    public Object data;

    @SerializedName("setting")
    public SettingDto setting;

    /** helper للحصول على CustomerDto عند النجاح */
    public CustomerDto getCustomerOrNull(com.google.gson.Gson gson) {
        if (!status || data == null) return null;
        try {
            // data قد يكون LinkedTreeMap -> نحوله عبر gson
            String json = gson.toJson(data);
            return gson.fromJson(json, CustomerDto.class);
        } catch (Exception ignore) {
            return null;
        }
    }
}
