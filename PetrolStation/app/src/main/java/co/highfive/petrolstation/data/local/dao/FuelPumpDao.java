package co.highfive.petrolstation.data.local.dao;

import androidx.room.*;
import java.util.List;
import co.highfive.petrolstation.data.local.entities.FuelPumpEntity;

@Dao
public interface FuelPumpDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<FuelPumpEntity> list);

    @Query("SELECT * FROM fuel_pumps ORDER BY id ASC")
    List<FuelPumpEntity> getAll();

    @Query("DELETE FROM fuel_pumps")
    void clear();
}
