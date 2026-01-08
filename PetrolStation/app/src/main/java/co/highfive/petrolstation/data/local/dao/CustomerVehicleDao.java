package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.CustomerVehicleEntity;

@Dao
public interface CustomerVehicleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<CustomerVehicleEntity> list);

    @Query("SELECT * FROM customer_vehicles WHERE customerId = :customerId ORDER BY id DESC")
    List<CustomerVehicleEntity> getByCustomer(int customerId);

    @Query("DELETE FROM customer_vehicles")
    void clear();

    @androidx.room.Query("DELETE FROM customer_vehicles WHERE customerId = :customerId")
    void deleteByCustomer(int customerId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(CustomerVehicleEntity e);

    @androidx.room.Query("SELECT * FROM customer_vehicles WHERE id = :id LIMIT 1")
    CustomerVehicleEntity getById(int id);


}
