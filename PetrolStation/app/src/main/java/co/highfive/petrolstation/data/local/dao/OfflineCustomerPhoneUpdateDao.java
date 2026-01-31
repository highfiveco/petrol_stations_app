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

    @Query("SELECT * FROM offline_customer_phone_updates WHERE sync_status = 0 ORDER BY created_at_ts ASC LIMIT :limit")
    List<OfflineCustomerPhoneUpdateEntity> getPending(int limit);

    @Query("SELECT COUNT(*) FROM offline_customer_phone_updates WHERE sync_status = 0")
    int countPending();
    @Query("UPDATE offline_customer_phone_updates SET sync_status = :newStatus, sync_error = :syncError, updated_at_ts = :ts WHERE local_id IN (:ids)")
    int markStatusByIds(List<Long> ids, int newStatus, String syncError, long ts);
}
