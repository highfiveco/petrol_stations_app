package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.TypeCustomerEntity;

@Dao
public interface TypeCustomerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<TypeCustomerEntity> list);

    @Query("SELECT * FROM type_customer ORDER BY id ASC")
    List<TypeCustomerEntity> getAll();

    @Query("DELETE FROM type_customer")
    void clear();
}
