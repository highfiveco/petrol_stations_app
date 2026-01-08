package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.CustomerStatusEntity;

@Dao
public interface CustomerStatusDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<CustomerStatusEntity> list);

    @Query("SELECT * FROM customer_statuses ORDER BY id ASC")
    List<CustomerStatusEntity> getAll();

    @Query("DELETE FROM customer_statuses")
    void clear();
}
