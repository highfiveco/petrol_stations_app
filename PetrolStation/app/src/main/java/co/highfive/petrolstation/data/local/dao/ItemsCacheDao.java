package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.ItemsCacheEntity;

@Dao
public interface ItemsCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<ItemsCacheEntity> items);

    @Query("DELETE FROM items_cache")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM items_cache")
    int count();

    @Query("SELECT * FROM items_cache WHERE id = :id LIMIT 1")
    ItemsCacheEntity getById(int id);

    @Query("SELECT * FROM items_cache WHERE barcode = :barcode LIMIT 1")
    ItemsCacheEntity getByBarcode(String barcode);

    // =========================
    // ✅ Offline filtering/paging
    // =========================

    @Query(
            "SELECT * FROM items_cache " +
                    "WHERE (:categoryId = 0 OR category = :categoryId) " +
                    "AND (:name = '' OR name LIKE '%' || :name || '%') " +
                    "ORDER BY name ASC " +
                    "LIMIT :limit OFFSET :offset"
    )
    List<ItemsCacheEntity> getPagedFiltered(int categoryId, String name, int limit, int offset);
}
