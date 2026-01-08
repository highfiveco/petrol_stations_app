package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.PosAddJsonSyncCacheEntity;

@Dao
public interface PosAddJsonSyncCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PosAddJsonSyncCacheEntity e);

    @Query("SELECT * FROM pos_add_json_sync_cache ORDER BY id DESC")
    List<PosAddJsonSyncCacheEntity> getAll();

    @Query("DELETE FROM pos_add_json_sync_cache")
    void clear();
}
