package co.highfive.petrolstation.fuelsale.dto;

import com.google.gson.annotations.SerializedName;

import co.highfive.petrolstation.customers.dto.InvoiceDto;

public class FuelPriceAddResponse {

    @SerializedName("status")
    public boolean status;

    @SerializedName("message")
    public String message;

    // success: object invoice
    // fail: string message
    @SerializedName("data")
    public Object data;

    public InvoiceDto getInvoiceOrNull(com.google.gson.Gson gson) {
        if (!status || data == null) return null;
        try {
            if (data instanceof String) return null;
            String json = gson.toJson(data);
            return gson.fromJson(json, InvoiceDto.class);
        } catch (Exception ignore) {
            return null;
        }
    }

    public String getErrorFromDataOrMessage() {
        // في الفشل: data = "..."
        if (data instanceof String) {
            String s = ((String) data).trim();
            if (!s.isEmpty()) return s;
        }
        if (message != null && !message.trim().isEmpty()) return message;
        return "Unknown error";
    }
}
