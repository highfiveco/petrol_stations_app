package co.highfive.petrolstation.pos.dto;

public class PosActiveInvoice {
    public long id;
    public int customerId;
    public String customerName;

    public int accountId;

    public String customerMobile;

    public String itemsJson;   // qtyMap as json
    public long createdAt;     // millis

    public String note;
    public String paymentsJson;
}
