package co.highfive.petrolstation.customers.dto;

import com.google.gson.annotations.SerializedName;

public class CustomerVehicleDto {
    @SerializedName("id") public int id;
    @SerializedName("customer_id") public Integer customer_id;
    @SerializedName("vehicle_number") public String vehicle_number;
    @SerializedName("vehicle_type") public Integer vehicle_type;
    @SerializedName("vehicle_color") public Integer vehicle_color;
    @SerializedName("model") public String model;
    @SerializedName("license_expiry_date") public String license_expiry_date;
    @SerializedName("notes") public String notes;
    @SerializedName("created_at") public String created_at;
    @SerializedName("vehicle_type_name") public String vehicle_type_name;
    @SerializedName("vehicle_color_name") public String vehicle_color_name;
    @SerializedName("account_id") public Integer account_id;

    public boolean is_offline;
    public long local_id;

    // optional if you want
    public long customer_local_id;
}
