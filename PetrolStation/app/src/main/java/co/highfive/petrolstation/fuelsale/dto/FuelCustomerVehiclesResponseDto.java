package co.highfive.petrolstation.fuelsale.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import co.highfive.petrolstation.customers.dto.CustomerVehicleDto;

public class FuelCustomerVehiclesResponseDto {
    @SerializedName("vehicles")
    public List<CustomerVehicleDto> vehicles;
}
