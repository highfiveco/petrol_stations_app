package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Index;

@Entity(
        tableName = "pos_invoice_details",
        indices = {@Index("invoiceId")}
)
public class PosInvoiceDetailEntity {

    @PrimaryKey
    public int id;

    public int invoiceId;

    public int itemId;
    public double count;
    public double price;
    public int updateCostPrice;

    public String itemJson; // item object
}
