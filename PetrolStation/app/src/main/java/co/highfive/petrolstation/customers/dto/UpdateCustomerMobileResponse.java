package co.highfive.petrolstation.customers.dto;

import com.google.gson.annotations.SerializedName;

import co.highfive.petrolstation.auth.dto.SettingDto;

public class UpdateCustomerMobileResponse {

    @SerializedName("status")
    public boolean status;

    @SerializedName("message")
    public String message;

    // success: object
    // fail: null (وبعض الأحيان ممكن تكون "" لو السيرفر ما التزم)
    @SerializedName("data")
    public Object data;

    @SerializedName("setting")
    public SettingDto setting;

    public CustomerDto getCustomerOrNull(com.google.gson.Gson gson) {
        if (!status || data == null) return null;
        try {
            // إذا رجعها "" بالغلط، نحمي نفسنا
            if (data instanceof String) {
                String s = ((String) data).trim();
                if (s.isEmpty()) return null;
            }
            String json = gson.toJson(data);
            return gson.fromJson(json, CustomerDto.class);
        } catch (Exception ignore) {
            return null;
        }
    }
}
