package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.UserLiteEntity;

@Dao
public interface UserLiteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<UserLiteEntity> list);

    @Query("SELECT * FROM users_lite ORDER BY id ASC")
    List<UserLiteEntity> getAll();

    @Query("DELETE FROM users_lite")
    void clear();
}
