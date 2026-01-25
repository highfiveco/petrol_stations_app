package co.highfive.petrolstation.customers.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import co.highfive.petrolstation.fuelsale.dto.FuelCampaignDto;
import co.highfive.petrolstation.fuelsale.dto.PumpDto;
import co.highfive.petrolstation.models.Account;

public class InvoiceDto {
    @SerializedName("id") public int id;
    @SerializedName("date") public String date;
    @SerializedName("statement") public String statement;
    @SerializedName("account_id") public Integer account_id;
    @SerializedName("store_id") public Integer store_id;
    @SerializedName("discount") public Double discount;
    @SerializedName("total") public Double total;
    @SerializedName("invoice_no") public String invoice_no;
    @SerializedName("notes") public String notes;
    @SerializedName("campaign_id") public Integer campaign_id;
    @SerializedName("pump_id") public Integer pump_id;
    @SerializedName("customer_vehicle_id") public Integer customer_vehicle_id;
    @SerializedName("is_fuel_sale") public Integer is_fuel_sale;
    @SerializedName("created_at") public String created_at;

    @SerializedName("details") public List<InvoiceDetailDto> details;

    // IMPORTANT: changed from Object -> InvoiceAccountDto
    @SerializedName("account") public Account account;

    @SerializedName("pump")
    private PumpDto pump;

    @SerializedName("campaign")
    private FuelCampaignDto campaign;
    @SerializedName("customer_vehicle")
    private CustomerVehicleDto customerVehicle;



    @SerializedName("pay_amount") public Double pay_amount;
    @SerializedName("remain") public Double remain;

    public PumpDto getPump() { return pump; }
    public FuelCampaignDto getCampaign() { return campaign; }
    public CustomerVehicleDto getCustomerVehicle() { return customerVehicle; }

    public boolean is_offline = false; // NEW
    public long local_id = 0;          // NEW (offline_invoices.localId)
    public int sync_status = -1;       // NEW (0 pending, 1 sent, 2 failed)
    public String sync_error = null;   // NEW

}
