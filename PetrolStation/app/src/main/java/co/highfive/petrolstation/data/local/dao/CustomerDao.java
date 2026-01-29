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

    @Query("SELECT * FROM customers WHERE accountId = :accountId LIMIT 1")
    CustomerEntity getCustomerByAccountId(String accountId);

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

    @Query("SELECT * FROM customers WHERE name LIKE :nameLike ORDER BY id DESC LIMIT :limit OFFSET :offset")
    List<CustomerEntity> searchByNamePaged(String nameLike, int limit, int offset);

    @Query("SELECT * FROM customers WHERE balance >= :minBalance ORDER BY id DESC LIMIT :limit OFFSET :offset")
    List<CustomerEntity> searchByBalanceMinPaged(double minBalance, int limit, int offset);

    @Query("SELECT * FROM customers WHERE name LIKE :nameLike AND balance >= :minBalance ORDER BY id DESC LIMIT :limit OFFSET :offset")
    List<CustomerEntity> searchByNameAndBalanceMinPaged(String nameLike, double minBalance, int limit, int offset);

    @androidx.room.Query("SELECT * FROM customers WHERE accountId = :accountId LIMIT 1")
    co.highfive.petrolstation.data.local.entities.CustomerEntity getByAccountId(int accountId);

    @Query("SELECT * FROM customers WHERE mobile = :mobile LIMIT 1")
    CustomerEntity getByMobile(String mobile);

    @Query("SELECT * FROM customers WHERE mobile = :mobile LIMIT 1")
    CustomerEntity getByMobileExact(String mobile);

    @Query("SELECT * FROM customers WHERE REPLACE(REPLACE(REPLACE(mobile,' ',''),'-',''),'+','') = :mobileNormalized LIMIT 1")
    CustomerEntity getByMobileNormalizedLoose(String mobileNormalized);

    @Query("UPDATE customers SET mobile = :mobile WHERE id = :customerId")
    int updateMobileByCustomerId(String customerId, String mobile);

    @Query("UPDATE customers SET mobile = :mobile WHERE accountId = :accountId")
    int updateMobileByAccountId(String accountId, String mobile);
}
