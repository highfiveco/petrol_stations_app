package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.InvoiceListEntity;

@Dao
public interface InvoiceListDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<InvoiceListEntity> list);

    @Query("SELECT * FROM invoices_list WHERE (:accountId IS NULL OR queryAccountId = :accountId) ORDER BY id DESC")
    List<InvoiceListEntity> getByAccountFilter(Integer accountId);

    @Query("DELETE FROM invoices_list WHERE (:accountId IS NULL OR queryAccountId = :accountId)")
    void clearByAccountFilter(Integer accountId);
}
