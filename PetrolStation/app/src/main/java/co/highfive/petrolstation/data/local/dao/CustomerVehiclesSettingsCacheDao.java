package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import co.highfive.petrolstation.data.local.entities.CustomerVehiclesSettingsCacheEntity;

@Dao
public interface CustomerVehiclesSettingsCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(CustomerVehiclesSettingsCacheEntity e);

    @Query("SELECT * FROM customer_vehicle_settings_cache WHERE id = 1 LIMIT 1")
    CustomerVehiclesSettingsCacheEntity get();

    @Query("DELETE FROM customer_vehicle_settings_cache")
    void clear();
}
