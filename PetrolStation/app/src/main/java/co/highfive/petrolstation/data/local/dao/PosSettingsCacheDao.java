package co.highfive.petrolstation.data.local.dao;

import androidx.room.*;
import co.highfive.petrolstation.data.local.entities.PosSettingsCacheEntity;

@Dao
public interface PosSettingsCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(PosSettingsCacheEntity e);

    @Query("SELECT * FROM pos_settings_cache WHERE id = 1 LIMIT 1")
    PosSettingsCacheEntity get();

    @Query("DELETE FROM pos_settings_cache")
    void clear();
}
