package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "customer_vehicles",
        indices = { @Index("customerId"), @Index("vehicleNumber") }
)
public class CustomerVehicleEntity {
    @PrimaryKey public int id;

    public int customerId;
    public String vehicleNumber;

    public Integer vehicleType;
    public Integer vehicleColor;

    public String model;
    public String licenseExpiryDate;
    public String notes;
    public String createdAt;

    public String vehicleTypeName;
    public String vehicleColorName;

    public Integer accountId;
}
