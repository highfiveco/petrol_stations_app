package co.highfive.petrolstation.data.local.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "offline_customers",
        indices = {
                @Index(value = {"mobile_normalized"}),
                @Index(value = {"sync_status"})
        }
)
public class OfflineCustomerEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "local_id")
    public long localId;

    // بعد ما نعمل sync لاحقًا بنعبّيها
    @Nullable
    @ColumnInfo(name = "online_id")
    public Integer onlineId;

    @NonNull
    @ColumnInfo(name = "name")
    public String name = "";

    @NonNull
    @ColumnInfo(name = "mobile")
    public String mobile = "";

    // مهم للفحص والبحث
    @NonNull
    @ColumnInfo(name = "mobile_normalized")
    public String mobileNormalized = "";

    // اختياري
    @Nullable
    @ColumnInfo(name = "address")
    public String address;

    @Nullable
    @ColumnInfo(name = "notes")
    public String notes;

    // payload جاهز للمستقبل (sync)
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
