package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Index;

@Entity(
        tableName = "invoices_list",
        indices = {
                @Index("accountId"),
                @Index("date")
        }
)
public class InvoiceListEntity {

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

    // snapshots JSON (قد تكون null)
    public String accountJson;
    public String pumpJson;
    public String customerVehicleJson;

    // لإدارة الفلاتر / الفصل بين نتائج account_id المختلفة
    public Integer queryAccountId;   // نفس account_id المرسل بالفلتر، أو null للكل

    public long updatedAt;
}
