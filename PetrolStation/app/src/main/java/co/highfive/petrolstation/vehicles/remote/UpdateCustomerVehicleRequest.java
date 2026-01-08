package co.highfive.petrolstation.vehicles.remote;

public class UpdateCustomerVehicleRequest {

    public int id;                     // required (vehicle id)
    public Integer customer_id;         // optional (في Postman مش محددة أحياناً)
    public String vehicle_number;       // required
    public Integer vehicle_type;        // required
    public Integer vehicle_color;       // required
    public String model;               // optional
    public String license_expiry_date;  // yyyy-MM-dd
    public String notes;               // optional
}
