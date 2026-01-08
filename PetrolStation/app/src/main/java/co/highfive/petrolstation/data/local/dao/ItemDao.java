package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.ItemEntity;

@Dao
public interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<ItemEntity> items);

    @Query("SELECT * FROM items WHERE categoryId = :categoryId ORDER BY id ASC")
    List<ItemEntity> getByCategory(int categoryId);

    @Query("SELECT * FROM items ORDER BY id ASC")
    List<ItemEntity> getAll();

    @Query("DELETE FROM items")
    void clear();
}
