package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.VehicleColorEntity;

@Dao
public interface VehicleColorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<VehicleColorEntity> list);

    @Query("SELECT * FROM vehicle_colors ORDER BY id ASC")
    List<VehicleColorEntity> getAll();

    @Query("DELETE FROM vehicle_colors")
    void clear();
}
