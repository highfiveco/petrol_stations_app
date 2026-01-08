package co.highfive.petrolstation.data.local.dao;

import androidx.room.*;
import java.util.List;
import co.highfive.petrolstation.data.local.entities.FuelPaymentTypeEntity;

@Dao
public interface FuelPaymentTypeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<FuelPaymentTypeEntity> list);

    @Query("SELECT * FROM fuel_payment_types ORDER BY id ASC")
    List<FuelPaymentTypeEntity> getAll();

    @Query("DELETE FROM fuel_payment_types")
    void clear();
}
