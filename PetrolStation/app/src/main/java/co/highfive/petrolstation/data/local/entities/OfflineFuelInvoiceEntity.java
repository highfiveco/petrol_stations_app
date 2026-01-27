package co.highfive.petrolstation.data.local.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "offline_fuel_invoices",
        indices = {
                @Index(value = {"sync_status"}),
                @Index(value = {"customer_online_id"}),
                @Index(value = {"customer_local_id"})
        }
)
public class OfflineFuelInvoiceEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "local_id")
    public long localId;

    @Nullable
    @ColumnInfo(name = "customer_online_id")
    public Integer customerOnlineId;

    @Nullable
    @ColumnInfo(name = "customer_local_id")
    public Long customerLocalId;

    @Nullable
    @ColumnInfo(name = "vehicle_online_id")
    public Integer vehicleOnlineId;

    @Nullable
    @ColumnInfo(name = "vehicle_local_id")
    public Long vehicleLocalId;

    @Nullable
    @ColumnInfo(name = "pump_id")
    public Integer pumpId;

    @Nullable
    @ColumnInfo(name = "campaign_id")
    public Integer campaignId;

    @NonNull
    @ColumnInfo(name = "statement")
    public String statement = "";

    @ColumnInfo(name = "total")
    public double total;

    @NonNull
    @ColumnInfo(name = "invoice_no_placeholder")
    public String invoiceNoPlaceholder = "";

    @Nullable
    @ColumnInfo(name = "request_json")
    public String requestJson;

    // 0=PENDING, 1=SYNCED, 2=FAILED
    @ColumnInfo(name = "sync_status")
    public int syncStatus = 0;

    @Nullable
    @ColumnInfo(name = "sync_error")
    public String syncError;

    @ColumnInfo(name = "created_at_ts")
    public long createdAtTs;

    @ColumnInfo(name = "updated_at_ts")
    public long updatedAtTs;
}
