package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.OfflineFinancialTransactionEntity;

@Dao
public interface OfflineFinancialTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(OfflineFinancialTransactionEntity entity);

    @Query("SELECT * FROM offline_financial_transactions WHERE syncStatus = 0 ORDER BY createdAtTs ASC LIMIT :limit")
    List<OfflineFinancialTransactionEntity> getPending(int limit);

    @Query("UPDATE offline_financial_transactions SET syncStatus = :newStatus, syncError = :syncError, updatedAtTs = :ts WHERE localId IN (:ids)")
    int markStatusByIds(List<Long> ids, int newStatus, String syncError, long ts);

    // ✅ optional: split by actionType (move/discount/refund)
    @Query("SELECT COUNT(*) FROM offline_financial_transactions WHERE syncStatus = 0 AND actionType = :actionType")
    int countPendingByActionType(int actionType);



    @Query("SELECT * FROM offline_financial_transactions WHERE syncStatus = 0 AND actionType = :actionType ORDER BY createdAtTs ASC LIMIT :limit")
    List<OfflineFinancialTransactionEntity> getPendingByActionType(int actionType, int limit);

}
