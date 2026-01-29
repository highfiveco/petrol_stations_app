package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.OfflineCustomerPhoneUpdateEntity;

@Dao
public interface OfflineCustomerPhoneUpdateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(OfflineCustomerPhoneUpdateEntity e);

    @Query("SELECT * FROM offline_customer_phone_updates WHERE sync_status = 0 ORDER BY created_at_ts ASC")
    List<OfflineCustomerPhoneUpdateEntity> getPending();
}
