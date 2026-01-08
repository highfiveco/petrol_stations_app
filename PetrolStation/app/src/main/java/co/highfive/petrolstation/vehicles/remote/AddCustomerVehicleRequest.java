package co.highfive.petrolstation.vehicles.remote;

public class AddCustomerVehicleRequest {
    public int customer_id;              // required
    public String vehicle_number;        // required
    public Integer vehicle_type;         // required
    public Integer vehicle_color;        // required
    public String model;                 // required/optional حسب السيرفر
    public String license_expiry_date;   // yyyy-MM-dd
    public String notes;                 // optional
}
