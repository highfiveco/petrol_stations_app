package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.CustomerEntity;

@Dao
public interface CustomerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<CustomerEntity> customers);

    @Query("SELECT * FROM customers ORDER BY id DESC")
    List<CustomerEntity> getAll();

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    CustomerEntity getById(int id);

    // بحث بسيط بالاسم أو الموبايل (للأوفلاين)
    @Query("SELECT * FROM customers WHERE name LIKE '%' || :q || '%' OR mobile LIKE '%' || :q || '%' ORDER BY id DESC")
    List<CustomerEntity> search(String q);

    @Query("DELETE FROM customers")
    void clear();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(CustomerEntity customer);

    @androidx.room.Query("UPDATE customers SET mobile = :mobile WHERE id = :customerId")
    int updateMobile(int customerId, String mobile);

    @androidx.room.Query(
            "SELECT id, name, accountId, campaignName, remainingAmount " +
                    "FROM customers " +
                    "WHERE name LIKE '%' || :q || '%' " +
                    "ORDER BY name ASC " +
                    "LIMIT :limit"
    )
    java.util.List<co.highfive.petrolstation.customers.local.CustomerSearchRow> searchByName(String q, int limit);

}
