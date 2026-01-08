package co.highfive.petrolstation.data.local.dao;

import androidx.room.*;
import java.util.List;

import co.highfive.petrolstation.data.local.entities.PosItemEntity;

@Dao
public interface PosItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<PosItemEntity> list);

    @Query("DELETE FROM pos_items WHERE queryCategoryId IS :categoryId AND queryName IS :name")
    void clearForFilter(Integer categoryId, String name);

    @Query("SELECT * FROM pos_items WHERE queryCategoryId IS :categoryId AND queryName IS :name ORDER BY id ASC")
    List<PosItemEntity> getByFilter(Integer categoryId, String name);

    @Query("DELETE FROM pos_items")
    void clearAll();
}
