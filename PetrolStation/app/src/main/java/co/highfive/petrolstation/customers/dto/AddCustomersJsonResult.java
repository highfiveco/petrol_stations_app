package co.highfive.petrolstation.customers.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AddCustomersJsonResult {

    @SerializedName("success_count")
    public Integer success_count;

    @SerializedName("fail_count")
    public Integer fail_count;

    @SerializedName("added_customers")
    public List<AddedCustomerDto> added_customers;

    @SerializedName("errors")
    public List<Object> errors; // خليها Object لأنها قد تكون array من strings أو objects
}
