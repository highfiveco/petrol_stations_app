package co.highfive.petrolstation.pos.dto;

import java.util.ArrayList;

public class PosOfflineSaleRequest {
    public int accountId;

    // ممكن يكون 0 وقت الأوفلاين
    public int customerId;

    // ✅ NEW: local reference to link multiple invoices
    public long offlineCustomerLocalId;

    // ✅ NEW: send customer data in the invoice payload
    public String customerName;
    public String customerMobile;

    public String notes;

    public ArrayList<PosDraftItemDto> items = new ArrayList<>();
    public ArrayList<co.highfive.petrolstation.fuelsale.dto.PaymentMethodDto> paymentMethods = new ArrayList<>();
}
