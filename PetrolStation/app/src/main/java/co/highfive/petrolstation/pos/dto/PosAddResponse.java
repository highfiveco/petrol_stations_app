package co.highfive.petrolstation.pos.dto;

import com.google.gson.annotations.SerializedName;
import com.google.gson.Gson;

import co.highfive.petrolstation.customers.dto.InvoiceDto;

public class PosAddResponse {

    @SerializedName("status")
    public boolean status;

    // في هذا الريكوست ما في message حسب الأمثلة
    @SerializedName("message")
    public String message;

    // success: Invoice object
    // fail: String
    @SerializedName("data")
    public Object data;

    public String getErrorMessage() {
        if (status) return null;
        if (data == null) return (message != null && !message.trim().isEmpty()) ? message : "Unknown error";
        if (data instanceof String) return (String) data;
        return (message != null && !message.trim().isEmpty()) ? message : "Unknown error";
    }

    public InvoiceDto getInvoiceOrNull(Gson gson) {
        if (!status || data == null) return null;
        try {
            // إذا رجع String بالغلط
            if (data instanceof String) {
                String s = ((String) data).trim();
                if (s.isEmpty()) return null;
            }
            String json = gson.toJson(data);
            return gson.fromJson(json, InvoiceDto.class);
        } catch (Exception ignore) {
            return null;
        }
    }
}
