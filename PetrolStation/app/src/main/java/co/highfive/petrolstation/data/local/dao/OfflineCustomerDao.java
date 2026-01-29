package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.OfflineCustomerEntity;

@Dao
public interface OfflineCustomerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(OfflineCustomerEntity e);

    @Update
    int update(OfflineCustomerEntity e);

    @Query("SELECT * FROM offline_customers WHERE local_id = :localId LIMIT 1")
    OfflineCustomerEntity getByLocalId(long localId);

    @Query("SELECT * FROM offline_customers WHERE mobile_normalized = :mobileNormalized LIMIT 1")
    OfflineCustomerEntity getByMobileNormalized(String mobileNormalized);

    @Query("SELECT COUNT(*) FROM offline_customers WHERE mobile_normalized = :mobileNormalized")
    int countByMobileNormalized(String mobileNormalized);

    @Query("SELECT * FROM offline_customers WHERE sync_status = 0 ORDER BY created_at_ts ASC")
    List<OfflineCustomerEntity> getPending();

    @Query("DELETE FROM offline_customers WHERE local_id = :localId")
    int deleteByLocalId(long localId);

    @Query("SELECT * FROM offline_customers WHERE name LIKE '%' || :q || '%' OR mobile LIKE '%' || :q || '%' ORDER BY name LIMIT 50")
    List<OfflineCustomerEntity> search(String q);

    @Query("UPDATE offline_customers SET mobile = :mobile, mobile_normalized = :mobileNormalized, updated_at_ts = :updatedAtTs WHERE local_id = :localId")
    int updateMobileByLocalId(long localId, String mobile, String mobileNormalized, long updatedAtTs);


}
