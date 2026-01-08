package co.highfive.petrolstation.data.local.dao;

import androidx.room.*;
import java.util.List;
import co.highfive.petrolstation.data.local.entities.PosCategoryEntity;

@Dao
public interface PosCategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<PosCategoryEntity> list);

    @Query("SELECT * FROM pos_categories ORDER BY id ASC")
    List<PosCategoryEntity> getAll();

    @Query("DELETE FROM pos_categories")
    void clear();
}
