package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.FuelSaleEntity;

@Dao
public interface FuelSalesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<FuelSaleEntity> list);

    @Query("SELECT * FROM fuel_sales_list WHERE (:accountId IS NULL OR queryAccountId = :accountId) ORDER BY id DESC")
    List<FuelSaleEntity> getByAccountFilter(Integer accountId);

    @Query("DELETE FROM fuel_sales_list WHERE (:accountId IS NULL OR queryAccountId = :accountId)")
    void clearByAccountFilter(Integer accountId);

    @Query("DELETE FROM fuel_sales_list")
    void clear();
}
