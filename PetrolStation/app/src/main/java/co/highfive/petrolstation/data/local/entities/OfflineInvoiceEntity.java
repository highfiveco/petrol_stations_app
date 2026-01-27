package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "offline_invoices",
        indices = {
                @Index("customerId"),
                @Index("isFuelSale"),
                @Index("syncStatus"),
                @Index("createdAtTs")
        }
)
public class OfflineInvoiceEntity {

    @PrimaryKey(autoGenerate = true)
    public long localId; // local PK

    public Integer onlineId; // after sync, nullable

    public int customerId;
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

    public Integer isFuelSale; // 0/1

    // Snapshot fields for UI list
    public String statement;
    public String invoiceNo;   // optional placeholder
    public Double total;
    public String notes;

    // Full request payload as JSON (FuelPriceAddRequest)
    public String requestJson;

    // Sync
    public int syncStatus; // 0=PENDING,1=SENT,2=FAILED
    public String syncError;

    public long createdAtTs;   // unified ordering
    public long updatedAtTs;
}
