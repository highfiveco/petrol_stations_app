package co.highfive.petrolstation.fuelsale.dto;

public class FuelSaleDraft {
    public int local_id;          // auto increment
    public long saved_at;         // time

    // The exact payload you will submit later
    public FuelPriceAddRequest request;

    // UI fields for ActiveInvoices screen
    public String customer_name;
    public String customer_mobile;
    public String total_text;     // e.g. "40 شيكل"
    public String vehicle_text;   // optional
}
