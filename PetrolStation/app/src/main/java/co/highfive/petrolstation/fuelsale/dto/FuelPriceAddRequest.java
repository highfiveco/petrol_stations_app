package co.highfive.petrolstation.fuelsale.dto;

import java.util.List;

public class FuelPriceAddRequest {
    public List<Integer> itemIds;     // item_id[]
    public List<Double> prices;       // price[]
    public List<Double> counts;       // count[]

    public int customerId;

    // ✅ NEW: local reference to link multiple invoices
    public long offlineCustomerLocalId;

    // ✅ NEW: send customer data in the invoice payload
    public String customerName;
    public String customerMobile;

    public Integer accountId;
    public Integer customerVehicleId;
    public String vehicleNumber;
    public String vehicleModel;
    public String vehicleNotes;
    public Integer vehicleType;
    public String licenseExpiryDate;
    public Integer vehicleColor;

    public Integer pumpId;
    public Integer campaignId;

    public List<PaymentMethodDto> paymentMethods; // JSON string
    public String notes;
}
