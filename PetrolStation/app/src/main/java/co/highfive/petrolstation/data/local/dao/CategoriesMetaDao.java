package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import co.highfive.petrolstation.data.local.entities.CategoriesMetaCacheEntity;

@Dao
public interface CategoriesMetaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(CategoriesMetaCacheEntity entity);

    @Query("SELECT * FROM categories_meta_cache WHERE id = 1 LIMIT 1")
    CategoriesMetaCacheEntity get();

    @Query("DELETE FROM categories_meta_cache")
    void clear();
}
