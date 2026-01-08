package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import co.highfive.petrolstation.data.local.entities.VehicleSyncResultCacheEntity;

@Dao
public interface VehicleSyncResultCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(VehicleSyncResultCacheEntity e);

    @Query("SELECT * FROM vehicle_sync_result_cache WHERE id = 1 LIMIT 1")
    VehicleSyncResultCacheEntity get();

    @Query("DELETE FROM vehicle_sync_result_cache")
    void clear();
}
