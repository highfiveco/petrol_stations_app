package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import co.highfive.petrolstation.data.local.entities.GetSettingCacheEntity;

@Dao
public interface GetSettingCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(GetSettingCacheEntity entity);

    @Query("SELECT * FROM get_setting_cache WHERE id = 1 LIMIT 1")
    GetSettingCacheEntity get();

    @Query("DELETE FROM get_setting_cache")
    void clear();
}
