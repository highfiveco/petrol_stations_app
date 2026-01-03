package co.highfive.petrolstation.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;


@Dao
public interface CollectionDao {
    // Basic CRUD operations
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    void insert(Collection collection);
//
//    @Update
//    void update(Collection collection);
//
//    @Query("DELETE FROM collections WHERE id = :id")
//    void deleteById(int id);
//
//    @Query("SELECT * FROM collections WHERE id = :id")
//    Collection getById(int id);
//
//    // Bulk operations
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    void insertAll(List<Collection> collections);
//
//    @Update
//    void updateAll(List<Collection> collections);
//
//    @Query("DELETE FROM collections")
//    void deleteAll();
//
//    // Query operations
//    @Query("SELECT * FROM collections")
//    List<Collection> getAll();
//
//    @Query("SELECT * FROM collections WHERE id = :id")
//    LiveData<Collection> getByIdLive(int id);
//
//    @Query("SELECT * FROM collections WHERE account_id = :accountId")
//    LiveData<List<Collection>> getByAccountId(int accountId);
//
//    @Query("SELECT * FROM collections WHERE name LIKE '%' || :searchTerm || '%' OR mobile LIKE '%' || :searchTerm || '%'")
//    LiveData<List<Collection>> searchCollections(String searchTerm);
//
//    @Query("SELECT * FROM collections WHERE month = :month")
//    LiveData<List<Collection>> getByMonth(String month);
//
//    // Advanced operations
//    @Transaction
//    default void upsert(Collection collection) {
//        if (getById(collection.getId()) != null) {
//            update(collection);
//        } else {
//            insert(collection);
//        }
//    }
//
//    @Transaction
//    default void upsertAll(List<Collection> collections) {
//        for (Collection collection : collections) {
//            upsert(collection);
//        }
//    }
//
//    @Query("SELECT COUNT(*) FROM collections")
//    LiveData<Integer> count();
//
//    @RawQuery
//    List<Collection> getFilteredCollections(SupportSQLiteQuery query);
//
//    @Query("SELECT * FROM collections WHERE account_id = :accountId LIMIT 1")
//    Collection getCustomerByAccountId(String accountId);
}