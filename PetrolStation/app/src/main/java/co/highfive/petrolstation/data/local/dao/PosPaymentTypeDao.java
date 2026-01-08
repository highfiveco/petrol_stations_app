package co.highfive.petrolstation.data.local.dao;

import androidx.room.*;
import java.util.List;
import co.highfive.petrolstation.data.local.entities.PosPaymentTypeEntity;

@Dao
public interface PosPaymentTypeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<PosPaymentTypeEntity> list);

    @Query("SELECT * FROM pos_payment_types ORDER BY id ASC")
    List<PosPaymentTypeEntity> getAll();

    @Query("DELETE FROM pos_payment_types")
    void clear();
}
