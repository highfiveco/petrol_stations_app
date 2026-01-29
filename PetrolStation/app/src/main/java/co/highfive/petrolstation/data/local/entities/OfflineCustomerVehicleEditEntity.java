package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "offline_customer_vehicle_edits",
        indices = {
                @Index("customerId"),
                @Index("offlineCustomerLocalId"),
                @Index("syncStatus"),
                @Index("createdAtTs"),

                // يمنع تكرار Edit لنفس المركبة (online) لنفس customerId
                @Index(value = {"customerId", "targetOnlineVehicleId"}, unique = true),

                // يمنع تكرار Edit لنفس المركبة (local) لنفس offlineCustomerLocalId
                @Index(value = {"offlineCustomerLocalId", "targetLocalVehicleId"}, unique = true)
        }
)
public class OfflineCustomerVehicleEditEntity {

    @PrimaryKey(autoGenerate = true)
    public long localId;

    // Scope: يا إما customer online أو offline customer
    public int customerId;                 // إذا الكستمر Online
    public long offlineCustomerLocalId;    // إذا الكستمر Offline

    // Target vehicle: يا إما online vehicle id أو local vehicle id
    public Integer targetOnlineVehicleId;  // vehicle.id من السيرفر (nullable)
    public Long targetLocalVehicleId;      // vehicle.localId (nullable)

    // Edited fields
    public String vehicleNumber;
    public Integer vehicleType;
    public Integer vehicleColor;
    public String model;
    public String licenseExpiryDate;
    public String notes;

    // Optional display names (للـ UI)
    public String vehicleTypeName;
    public String vehicleColorName;

    // Sync
    public int syncStatus; // 0=PENDING,1=SENT,2=FAILED
    public String syncError;

    public long createdAtTs;
    public long updatedAtTs;
}
