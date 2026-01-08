package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import co.highfive.petrolstation.data.local.entities.InvoiceListMetaCacheEntity;

@Dao
public interface InvoiceListMetaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(InvoiceListMetaCacheEntity e);

    @Query("SELECT * FROM invoices_list_meta_cache WHERE `key` = :key LIMIT 1")
    InvoiceListMetaCacheEntity get(String key);

    @Query("DELETE FROM invoices_list_meta_cache WHERE `key` = :key")
    void clear(String key);
}
