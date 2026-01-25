package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import co.highfive.petrolstation.data.local.entities.CustomersMetaCacheEntity;

@Dao
public interface CustomersMetaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(CustomersMetaCacheEntity entity);

    @Query("SELECT * FROM customers_meta_cache WHERE id = 1 LIMIT 1")
    CustomersMetaCacheEntity get();

    @Query("DELETE FROM customers_meta_cache")
    void clear();

    @Query("SELECT * FROM customers_meta_cache WHERE id = 1 LIMIT 1")
    CustomersMetaCacheEntity getOne();
}
