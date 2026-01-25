package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.InvoiceDetailEntity;

@Dao
public interface InvoiceDetailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<InvoiceDetailEntity> list);

    @Query("SELECT * FROM invoice_details WHERE invoiceId = :invoiceId ORDER BY id ASC")
    List<InvoiceDetailEntity> getByInvoice(int invoiceId);

    @Query("DELETE FROM invoice_details")
    void clear();

    @Query("DELETE FROM invoice_details WHERE invoiceId = :invoiceId")
    void clearForInvoice(int invoiceId);
}
