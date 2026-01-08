package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.CategoryEntity;

@Dao
public interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<CategoryEntity> categories);

    @Query("SELECT * FROM categories ORDER BY id ASC")
    List<CategoryEntity> getAll();

    @Query("DELETE FROM categories")
    void clear();
}
