package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.OfflineCustomerVehicleEntity;

@Dao
public interface OfflineCustomerVehicleDao {

    @Insert
    long insert(OfflineCustomerVehicleEntity e);

    @Update
    void update(OfflineCustomerVehicleEntity e);

    @Query("SELECT * FROM offline_customer_vehicles WHERE localId = :localId LIMIT 1")
    OfflineCustomerVehicleEntity getByLocalId(long localId);

    @Query("SELECT * FROM offline_customer_vehicles WHERE customerId = :customerId AND syncStatus IN (0,2) ORDER BY createdAtTs DESC")
    List<OfflineCustomerVehicleEntity> getPendingByCustomer(int customerId);

    @Query("SELECT * FROM offline_customer_vehicles WHERE syncStatus = 0 ORDER BY createdAtTs ASC LIMIT :limit")
    List<OfflineCustomerVehicleEntity> getPending(int limit);

    @Query("DELETE FROM offline_customer_vehicles WHERE localId = :localId")
    void deleteByLocalId(long localId);

    @Query("DELETE FROM offline_customer_vehicles")
    void clearAll();
}
