package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import co.highfive.petrolstation.data.local.entities.CustomersSettingCacheEntity;

@Dao
public interface CustomersSettingCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(CustomersSettingCacheEntity entity);

    @Query("SELECT * FROM customers_setting_cache WHERE id = 1 LIMIT 1")
    CustomersSettingCacheEntity get();

    @Query("DELETE FROM customers_setting_cache")
    void clear();
}
