package co.highfive.petrolstation.customers.dto;

import com.google.gson.annotations.SerializedName;

public class CustomerVehicleMiniDto {
    @SerializedName("id") public int id;
    @SerializedName("vehicle_number") public String vehicle_number;
}
