package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vehicle_sync_result_cache")
public class VehicleSyncResultCacheEntity {
    @PrimaryKey
    public int id = 1;

    public String responseJson; // VehicleSyncResponse كامل
    public long updatedAt;
}
