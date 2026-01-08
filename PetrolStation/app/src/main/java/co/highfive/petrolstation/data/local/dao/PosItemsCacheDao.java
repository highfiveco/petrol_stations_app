package co.highfive.petrolstation.data.local.dao;

import androidx.room.*;
import co.highfive.petrolstation.data.local.entities.PosItemsCacheEntity;

@Dao
public interface PosItemsCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(PosItemsCacheEntity e);

    @Query("SELECT * FROM pos_items_cache WHERE id = 1 LIMIT 1")
    PosItemsCacheEntity get();

    @Query("DELETE FROM pos_items_cache")
    void clear();
}
