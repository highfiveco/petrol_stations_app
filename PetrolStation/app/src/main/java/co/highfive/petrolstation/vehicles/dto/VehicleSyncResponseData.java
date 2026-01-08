package co.highfive.petrolstation.vehicles.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VehicleSyncResponseData {

    @SerializedName("added_count")
    public Integer added_count;

    @SerializedName("updated_count")
    public Integer updated_count;

    @SerializedName("fail_count")
    public Integer fail_count;

    @SerializedName("total_count")
    public Integer total_count;

    @SerializedName("processed_vehicles")
    public List<ProcessedVehicleDto> processed_vehicles;

    @SerializedName("errors")
    public List<VehicleSyncErrorDto> errors;
}
