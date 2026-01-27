package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "offline_customer_vehicles",
        indices = {
                @Index("customerId"),
                @Index("syncStatus"),
                @Index("createdAtTs")
        }
)
public class OfflineCustomerVehicleEntity {

    @PrimaryKey(autoGenerate = true)
    public long localId;

    public Integer onlineId; // بعد السينك لاحقاً

    public int customerId;
    public Integer accountId;

    public String vehicleNumber;
    public Integer vehicleType;
    public Integer vehicleColor;
    public String model;
    public String licenseExpiryDate;
    public String notes;

    // أسماء للعرض (اختياري)
    public String vehicleTypeName;
    public String vehicleColorName;

    public String requestJson;

    public int syncStatus; // 0=PENDING,1=SENT,2=FAILED
    public String syncError;

    public long createdAtTs;
    public long updatedAtTs;
}
