package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "fuel_sales_list",
        indices = { @Index("accountId"), @Index("date") }
)
public class FuelSaleEntity {

    @PrimaryKey
    public int id;

    public String date;
    public Double payAmount;
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

    public String accountJson;
    public String pumpJson;
    public String customerVehicleJson;
    public String campaignJson;

    public Integer queryAccountId;   // فلتر الشاشة
    public long updatedAt;
}
