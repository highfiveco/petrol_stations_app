package co.highfive.petrolstation.customers.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import co.highfive.petrolstation.models.Setting;

public class CustomerVehiclesResponseDto {

    @SerializedName("view_customer_vehicles") public int view_customer_vehicles;
    @SerializedName("add_customer_vehicles") public int add_customer_vehicles;
    @SerializedName("edit_customer_vehicles") public int edit_customer_vehicles;
    @SerializedName("delete_customer_vehicles") public int delete_customer_vehicles;

    @SerializedName("customer") public CustomerMiniDto customer;
    @SerializedName("vehicles") public List<CustomerVehicleDto> vehicles;
    @SerializedName("setting") public Setting setting;

    public static class CustomerMiniDto {
        @SerializedName("id") public int id;
        @SerializedName("name") public String name;
        @SerializedName("mobile") public String mobile;
        @SerializedName("balance") public double balance;
        @SerializedName("account_id") public Integer account_id;
    }
}
