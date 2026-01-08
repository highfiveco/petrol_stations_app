package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.InvoiceEntity;

@Dao
public interface InvoiceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<InvoiceEntity> list);

    @Query("SELECT * FROM invoices WHERE customerId = :customerId ORDER BY id DESC")
    List<InvoiceEntity> getByCustomer(int customerId);

    @Query("SELECT * FROM invoices WHERE customerId = :customerId AND isFuelSale = 1 ORDER BY id DESC")
    List<InvoiceEntity> getFuelByCustomer(int customerId);

    @Query("DELETE FROM invoices")
    void clear();
}
