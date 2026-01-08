package co.highfive.petrolstation.vehicles.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import co.highfive.petrolstation.auth.dto.SettingDto;
import co.highfive.petrolstation.customers.dto.CustomerDto;
import co.highfive.petrolstation.customers.dto.CustomerVehicleDto;

public class CustomerVehiclesData {

    @SerializedName("view_customer_vehicles")
    public Integer view_customer_vehicles;

    @SerializedName("add_customer_vehicles")
    public Integer add_customer_vehicles;

    @SerializedName("edit_customer_vehicles")
    public Integer edit_customer_vehicles;

    @SerializedName("delete_customer_vehicles")
    public Integer delete_customer_vehicles;

    @SerializedName("customer")
    public CustomerDto customer;

    @SerializedName("vehicles")
    public List<CustomerVehicleDto> vehicles;

    @SerializedName("setting")
    public SettingDto setting;
}
