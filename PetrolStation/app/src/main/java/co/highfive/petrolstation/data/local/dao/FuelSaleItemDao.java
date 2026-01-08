package co.highfive.petrolstation.data.local.dao;

import androidx.room.*;
import java.util.List;
import co.highfive.petrolstation.data.local.entities.FuelSaleItemEntity;

@Dao
public interface FuelSaleItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<FuelSaleItemEntity> list);

    @Query("SELECT * FROM fuel_items ORDER BY id ASC")
    List<FuelSaleItemEntity> getAll();

    @Query("DELETE FROM fuel_items")
    void clear();
}
