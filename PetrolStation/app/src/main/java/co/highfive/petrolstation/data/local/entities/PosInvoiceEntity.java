package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pos_invoices")
public class PosInvoiceEntity {

    @PrimaryKey
    public int id;

    public String date;
    public String statement;

    public Integer accountId;
    public Double payAmount;
    public Integer storeId;
    public Double discount;
    public Double total;

    public String invoiceNo;
    public String notes;

    // snapshots
    public String accountJson;  // account object
    public String pumpJson;     // غالباً null هنا
    public String campaignJson; // غالباً null هنا
    public String customerVehicleJson; // null هنا

    public long updatedAt;
}
