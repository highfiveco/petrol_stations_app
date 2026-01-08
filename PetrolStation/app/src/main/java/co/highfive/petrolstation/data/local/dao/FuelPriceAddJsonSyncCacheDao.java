package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import co.highfive.petrolstation.data.local.entities.FuelPriceAddJsonSyncCacheEntity;

@Dao
public interface FuelPriceAddJsonSyncCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(FuelPriceAddJsonSyncCacheEntity e);

    @Query("SELECT * FROM fuel_price_add_json_sync_cache WHERE id = 1 LIMIT 1")
    FuelPriceAddJsonSyncCacheEntity get();

    @Query("DELETE FROM fuel_price_add_json_sync_cache")
    void clear();
}
