package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "invoice_details",
        indices = { @Index("invoiceId"), @Index("itemId") }
)
public class InvoiceDetailEntity {
    @PrimaryKey public int id;

    public int invoiceId;
    public Integer itemId;

    public Integer updateCostPrice;
    public Double count;
    public Double price;

    // snapshot JSON
    public String itemJson;
}
