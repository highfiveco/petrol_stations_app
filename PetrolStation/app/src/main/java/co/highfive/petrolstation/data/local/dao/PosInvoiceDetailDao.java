package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.PosInvoiceDetailEntity;

@Dao
public interface PosInvoiceDetailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<PosInvoiceDetailEntity> list);

    @Query("SELECT * FROM pos_invoice_details WHERE invoiceId = :invoiceId ORDER BY id ASC")
    List<PosInvoiceDetailEntity> getByInvoice(int invoiceId);

    @Query("DELETE FROM pos_invoice_details WHERE invoiceId = :invoiceId")
    void clearForInvoice(int invoiceId);

    @Query("DELETE FROM pos_invoice_details")
    void clear();
}
