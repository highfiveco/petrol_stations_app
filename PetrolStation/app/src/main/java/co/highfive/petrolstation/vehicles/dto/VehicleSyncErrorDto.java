package co.highfive.petrolstation.vehicles.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VehicleSyncErrorDto {

    @SerializedName("index")
    public Integer index;

    @SerializedName("vehicle_number")
    public String vehicle_number;

    @SerializedName("operation")
    public String operation;

    @SerializedName("errors")
    public List<String> errors;
}
