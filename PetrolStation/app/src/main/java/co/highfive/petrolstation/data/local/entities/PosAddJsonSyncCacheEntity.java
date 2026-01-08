package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pos_add_json_sync_cache")
public class PosAddJsonSyncCacheEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String requestJson;
    public String responseJson;

    public Integer successCount;
    public Integer failCount;

    public String message;
    public String errorsJson;

    public long createdAt;
}
