package co.highfive.petrolstation.vehicles.dto;

import com.google.gson.annotations.SerializedName;

public class ProcessedVehicleDto {

    @SerializedName("id")
    public int id;

    @SerializedName("customer_id")
    public Integer customer_id;

    @SerializedName("vehicle_number")
    public String vehicle_number;

    @SerializedName("vehicle_type_name")
    public String vehicle_type_name;

    @SerializedName("vehicle_color_name")
    public String vehicle_color_name;

    @SerializedName("model")
    public String model;

    @SerializedName("license_expiry_date")
    public String license_expiry_date;

    @SerializedName("operation")
    public String operation; // "added" / "updated" أو نص
}
