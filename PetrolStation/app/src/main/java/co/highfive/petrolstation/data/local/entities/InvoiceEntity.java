package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "invoices",
        indices = {
                @Index("customerId"),
                @Index("accountId"),
                @Index("pumpId"),
                @Index("campaignId"),
                @Index("customerVehicleId"),
                @Index("isFuelSale"),
                @Index("createdAtTs")
        }
)
public class InvoiceEntity {

    @PrimaryKey
    public int id; // online id from server

    public int customerId;

    public String date;
    public String statement;

    public Integer accountId;
    public Integer storeId;

    public Double discount;
    public Double total;

    public String invoiceNo;
    public String notes;

    public Integer campaignId;
    public Integer pumpId;
    public Integer customerVehicleId;

    public Integer isFuelSale; // 0/1
    public String createdAt;   // server string

    public long createdAtTs;   // ✅ NEW (for ordering)
    public long updatedAt;     // cache
}
