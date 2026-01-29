package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import co.highfive.petrolstation.data.local.entities.OfflineFinancialTransactionEntity;

@Dao
public interface OfflineFinancialTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(OfflineFinancialTransactionEntity entity);
}
