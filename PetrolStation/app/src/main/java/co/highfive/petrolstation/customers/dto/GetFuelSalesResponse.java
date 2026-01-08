package co.highfive.petrolstation.customers.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GetFuelSalesResponse {
    @SerializedName("data") public PagedResponse<FuelSaleDto> sales;
}
