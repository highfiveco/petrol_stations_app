package co.highfive.petrolstation.vehicles.dto;

import com.google.gson.annotations.SerializedName;

public class VehicleSyncItemDto {

    @SerializedName("id")
    public Integer id; // موجود عندك في JSON (للتمييز تحديث/إضافة)

    @SerializedName("customer_id")
    public Integer customer_id;

    @SerializedName("vehicle_number")
    public String vehicle_number;

    @SerializedName("vehicle_type")
    public Integer vehicle_type;

    @SerializedName("vehicle_color")
    public Integer vehicle_color;

    @SerializedName("model")
    public String model;

    @SerializedName("license_expiry_date")
    public String license_expiry_date;

    @SerializedName("notes")
    public String notes;
}
