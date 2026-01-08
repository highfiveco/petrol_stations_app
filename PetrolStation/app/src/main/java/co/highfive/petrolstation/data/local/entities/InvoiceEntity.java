package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "invoices",
        indices = { @Index("customerId"), @Index("isFuelSale") }
)
public class InvoiceEntity {
    @PrimaryKey public int id;

    // مهم لأنه بالريسبونس الفواتير nested داخل customer
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
    public String createdAt;

    // snapshots JSON
    public String accountJson;
    public String pumpJson;
    public String customerVehicleJson;
}
