package co.highfive.petrolstation.data.local.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.OfflineCustomerVehicleEditEntity;

@Dao
public interface OfflineCustomerVehicleEditDao {

    @Query("SELECT * FROM offline_customer_vehicle_edits WHERE customerId = :customerId ORDER BY updatedAtTs DESC")
    List<OfflineCustomerVehicleEditEntity> getByCustomer(int customerId);

    @Query("SELECT * FROM offline_customer_vehicle_edits WHERE offlineCustomerLocalId = :offlineCustomerLocalId ORDER BY updatedAtTs DESC")
    List<OfflineCustomerVehicleEditEntity> getByOfflineCustomerLocalId(long offlineCustomerLocalId);

    @Query("SELECT localId FROM offline_customer_vehicle_edits WHERE customerId = :customerId AND targetOnlineVehicleId = :onlineVehicleId LIMIT 1")
    Long findLocalIdByOnlineTarget(int customerId, int onlineVehicleId);

    @Query("SELECT localId FROM offline_customer_vehicle_edits WHERE offlineCustomerLocalId = :offlineCustomerLocalId AND targetLocalVehicleId = :localVehicleId LIMIT 1")
    Long findLocalIdByLocalTarget(long offlineCustomerLocalId, long localVehicleId);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(OfflineCustomerVehicleEditEntity e);

    @Update
    int update(OfflineCustomerVehicleEditEntity e);

    @Query("DELETE FROM offline_customer_vehicle_edits WHERE customerId = :customerId")
    int deleteByCustomer(int customerId);

    @Query("DELETE FROM offline_customer_vehicle_edits WHERE offlineCustomerLocalId = :offlineCustomerLocalId")
    int deleteByOfflineCustomer(long offlineCustomerLocalId);

    /**
     * Upsert حسب Target:
     * - لو targetOnlineVehicleId موجود => upsert بواسطة (customerId + targetOnlineVehicleId)
     * - لو targetLocalVehicleId موجود => upsert بواسطة (offlineCustomerLocalId + targetLocalVehicleId)
     */
    @Transaction
    default void upsertByTarget(OfflineCustomerVehicleEditEntity e) {

        if (e == null) return;

        boolean hasOnlineTarget = e.targetOnlineVehicleId != null && e.targetOnlineVehicleId > 0;
        boolean hasLocalTarget = e.targetLocalVehicleId != null && e.targetLocalVehicleId > 0;

        if (!hasOnlineTarget && !hasLocalTarget) {
            // لا يوجد target واضح
            return;
        }

        Long existingLocalId = null;

        if (hasOnlineTarget) {
            int cid = e.customerId;
            int vid = e.targetOnlineVehicleId;
            if (cid > 0) {
                existingLocalId = findLocalIdByOnlineTarget(cid, vid);
            }
        } else {
            long offCid = e.offlineCustomerLocalId;
            long vLocal = e.targetLocalVehicleId;
            if (offCid > 0) {
                existingLocalId = findLocalIdByLocalTarget(offCid, vLocal);
            }
        }

        if (existingLocalId == null) {
            insert(e);
        } else {
            e.localId = existingLocalId;
            update(e);
        }
    }

    // ✅ pending list
    @Query("SELECT * FROM offline_customer_vehicle_edits WHERE syncStatus = 0 ORDER BY createdAtTs ASC LIMIT :limit")
    List<OfflineCustomerVehicleEditEntity> getPending(int limit);

    // ✅ count pending
    @Query("SELECT COUNT(*) FROM offline_customer_vehicle_edits WHERE syncStatus = 0")
    int countPending();

    // ✅ mark status
    @Query("UPDATE offline_customer_vehicle_edits SET syncStatus = :newStatus, syncError = :syncError, updatedAtTs = :ts WHERE localId IN (:ids)")
    int markStatusByIds(List<Long> ids, int newStatus, String syncError, long ts);
}
