package co.highfive.petrolstation.data.local.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
@Entity(
        tableName = "offline_customer_phone_updates",
        indices = {
                @Index(value = {"sync_status"}),
                @Index(value = {"customer_id"}),
                @Index(value = {"offline_local_id"})
        }
)
public class OfflineCustomerPhoneUpdateEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "local_id")
    public long localId;

    @Nullable
    @ColumnInfo(name = "customer_id") // online customer id
    public String customerId;

    @ColumnInfo(name = "account_id")
    public String accountId;

    @ColumnInfo(name = "is_offline_customer")
    public int isOfflineCustomer; // 0/1

    @ColumnInfo(name = "offline_local_id")
    public long offlineLocalId;

    @NonNull
    @ColumnInfo(name = "old_mobile")
    public String oldMobile = "";

    @NonNull
    @ColumnInfo(name = "new_mobile")
    public String newMobile = "";

    @Nullable
    @ColumnInfo(name = "request_json")
    public String requestJson;

    @ColumnInfo(name = "sync_status") // 0=PENDING,1=SYNCED,2=FAILED
    public int syncStatus = 0;

    @Nullable
    @ColumnInfo(name = "sync_error")
    public String syncError;

    @ColumnInfo(name = "created_at_ts")
    public long createdAtTs;

    @ColumnInfo(name = "updated_at_ts")
    public long updatedAtTs;
}
