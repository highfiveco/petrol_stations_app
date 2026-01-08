package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.PosInvoiceEntity;

@Dao
public interface PosInvoiceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(PosInvoiceEntity e);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<PosInvoiceEntity> list);

    @Query("SELECT * FROM pos_invoices ORDER BY id DESC")
    List<PosInvoiceEntity> getAll();

    @Query("DELETE FROM pos_invoices")
    void clear();
}
