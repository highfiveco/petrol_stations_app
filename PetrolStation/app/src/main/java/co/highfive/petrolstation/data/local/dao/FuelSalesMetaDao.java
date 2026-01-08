package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import co.highfive.petrolstation.data.local.entities.FuelSalesMetaCacheEntity;

@Dao
public interface FuelSalesMetaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(FuelSalesMetaCacheEntity e);

    @Query("SELECT * FROM fuel_sales_meta_cache WHERE `key` = :key LIMIT 1")
    FuelSalesMetaCacheEntity get(String key);

    @Query("DELETE FROM fuel_sales_meta_cache WHERE `key` = :key")
    void clear(String key);
}
