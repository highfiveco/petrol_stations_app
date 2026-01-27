package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.OfflineFuelInvoiceEntity;

@Dao
public interface OfflineFuelInvoiceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(OfflineFuelInvoiceEntity e);

    @Update
    int update(OfflineFuelInvoiceEntity e);

    @Query("SELECT * FROM offline_fuel_invoices WHERE sync_status = 0 ORDER BY local_id ASC LIMIT :limit")
    List<OfflineFuelInvoiceEntity> getPending(int limit);

    @Query("SELECT * FROM offline_fuel_invoices WHERE local_id = :localId LIMIT 1")
    OfflineFuelInvoiceEntity getByLocalId(long localId);

    @Query("DELETE FROM offline_fuel_invoices WHERE local_id = :localId")
    int deleteByLocalId(long localId);
}
