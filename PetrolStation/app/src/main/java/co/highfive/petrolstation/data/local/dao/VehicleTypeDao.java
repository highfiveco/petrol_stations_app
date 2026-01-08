package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.VehicleTypeEntity;

@Dao
public interface VehicleTypeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<VehicleTypeEntity> list);

    @Query("SELECT * FROM vehicle_types ORDER BY id ASC")
    List<VehicleTypeEntity> getAll();

    @Query("DELETE FROM vehicle_types")
    void clear();
}
