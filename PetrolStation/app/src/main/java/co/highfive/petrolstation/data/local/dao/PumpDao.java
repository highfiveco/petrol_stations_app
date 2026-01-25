package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.PumpEntity;

@Dao
public interface PumpDao {

    @Upsert
    void upsert(PumpEntity entity);

    @Upsert
    void upsertAll(List<PumpEntity> list);

    @Query("SELECT * FROM pumps ORDER BY name ASC")
    List<PumpEntity> getAll();

    @Query("SELECT * FROM pumps WHERE id = :id LIMIT 1")
    PumpEntity getById(int id);

    @Query("DELETE FROM pumps")
    void clear();
}
