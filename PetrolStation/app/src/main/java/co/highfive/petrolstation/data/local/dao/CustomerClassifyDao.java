package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.CustomerClassifyEntity;

@Dao
public interface CustomerClassifyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<CustomerClassifyEntity> list);

    @Query("SELECT * FROM customer_classify ORDER BY id ASC")
    List<CustomerClassifyEntity> getAll();

    @Query("DELETE FROM customer_classify")
    void clear();
}
