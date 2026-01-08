package co.highfive.petrolstation.customers.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import co.highfive.petrolstation.fuelsale.dto.FuelCampaignDto;
import co.highfive.petrolstation.fuelsale.dto.PumpDto;
import co.highfive.petrolstation.models.Account;

public class FuelSaleDto {
    @SerializedName("id") public int id;
    @SerializedName("date") public String date;
    @SerializedName("statement") public String statement;
    @SerializedName("pay_amount") public double pay_amount;

    @SerializedName("account_id") public int account_id;
    @SerializedName("store_id") public int store_id;

    @SerializedName("discount") public double discount;
    @SerializedName("total") public double total;

    @SerializedName("invoice_no") public String invoice_no;
    @SerializedName("notes") public String notes;

    @SerializedName("campaign_id") public Integer campaign_id;
    @SerializedName("pump_id") public Integer pump_id;
    @SerializedName("customer_vehicle_id") public Integer customer_vehicle_id;

    @SerializedName("details") public List<FuelSaleDetailDto> details;

    @SerializedName("account") public Account account;

    @SerializedName("pump") public PumpDto pump; // انت رافع PumpDto
    @SerializedName("campaign") public FuelCampaignDto campaign; // انت رافع FuelCampaignDto

    @SerializedName("customer_vehicle") public CustomerVehicleMiniDto customer_vehicle;
}
