package co.highfive.petrolstation.data.local.dao;

import androidx.room.*;
import co.highfive.petrolstation.data.local.entities.FuelPriceSettingsCacheEntity;

@Dao
public interface FuelPriceSettingsCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(FuelPriceSettingsCacheEntity e);

    @Query("SELECT * FROM fuel_price_settings_cache WHERE id = 1 LIMIT 1")
    FuelPriceSettingsCacheEntity get();

    @Query("DELETE FROM fuel_price_settings_cache")
    void clear();
}
