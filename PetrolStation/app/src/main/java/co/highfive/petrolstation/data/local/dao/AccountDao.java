package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.AccountEntity;

@Dao
public interface AccountDao {

    @Upsert
    void upsert(AccountEntity entity);

    @Upsert
    void upsertAll(List<AccountEntity> list);

    @Query("SELECT * FROM accounts ORDER BY name ASC")
    List<AccountEntity> getAll();

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    AccountEntity getById(int id);

    @Query("DELETE FROM accounts")
    void clear();
}
