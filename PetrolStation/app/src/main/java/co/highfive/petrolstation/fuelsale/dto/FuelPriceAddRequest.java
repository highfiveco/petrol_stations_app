package co.highfive.petrolstation.fuelsale.dto;

import java.util.List;

public class FuelPriceAddRequest {
    public List<Integer> itemIds;     // item_id[]
    public List<Double> prices;       // price[]
    public List<Double> counts;       // count[]

    public Integer accountId;
    public Integer customerVehicleId;
    public Integer pumpId;
    public Integer campaignId;
    public Integer customerId;

    public List<PaymentMethodDto> paymentMethods; // JSON string
    public String notes;
}
