package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import co.highfive.petrolstation.data.local.entities.CompanySettingCacheEntity;

@Dao
public interface CompanySettingCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(CompanySettingCacheEntity entity);

    @Query("SELECT * FROM company_setting_cache WHERE id = 1 LIMIT 1")
    CompanySettingCacheEntity get();

    @Query("DELETE FROM company_setting_cache")
    void clear();
}
