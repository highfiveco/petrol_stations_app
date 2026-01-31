package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.OfflineInvoiceEntity;

@Dao
public interface
OfflineInvoiceDao {

    @Insert
    long insert(OfflineInvoiceEntity e);

    @Update
    void update(OfflineInvoiceEntity e);

    @Query("SELECT * FROM offline_invoices WHERE syncStatus = 0 ORDER BY createdAtTs ASC LIMIT :limit")
    List<OfflineInvoiceEntity> getPending(int limit);

    @Query("SELECT * FROM offline_invoices WHERE localId = :localId LIMIT 1")
    OfflineInvoiceEntity getByLocalId(long localId);

    @Query("DELETE FROM offline_invoices WHERE localId = :localId")
    void deleteByLocalId(long localId);

    @Query("DELETE FROM offline_invoices")
    void clearAll();

    // pending + failed (بدك تعرضهم مع تمييز)
    @Query("SELECT * FROM offline_invoices WHERE isFuelSale = :isFuelSale AND syncStatus IN (0,2) ORDER BY createdAtTs DESC")
    List<OfflineInvoiceEntity> getPendingByType(int isFuelSale);

    @Query("SELECT * FROM offline_invoices WHERE accountId = :accountId AND isFuelSale = :isFuelSale AND syncStatus IN (0,2) ORDER BY createdAtTs DESC")
    List<OfflineInvoiceEntity> getPendingByAccountAndType(int accountId, int isFuelSale);

    @Query("SELECT * FROM offline_invoices WHERE customerId = :customerId AND isFuelSale = :isFuelSale AND syncStatus IN (0,2) ORDER BY createdAtTs DESC")
    List<OfflineInvoiceEntity> getPendingByCustomerAndType(int customerId, int isFuelSale);

    // ✅ Pending by type (POS=0 / FuelSale=1)
    @Query("SELECT * FROM offline_invoices WHERE syncStatus = 0 AND isFuelSale = :isFuelSale ORDER BY createdAtTs ASC LIMIT :limit")
    List<OfflineInvoiceEntity> getPendingByTypePendingOnly(int isFuelSale, int limit);

    // ✅ Count pending by type
    @Query("SELECT COUNT(*) FROM offline_invoices WHERE syncStatus = 0 AND isFuelSale = :isFuelSale")
    int countPendingByType(int isFuelSale);

    // ✅ Mark status (SENT / FAILED)
    @Query("UPDATE offline_invoices SET syncStatus = :newStatus, syncError = :syncError, updatedAtTs = :ts WHERE localId IN (:ids)")
    int markStatusByIds(List<Long> ids, int newStatus, String syncError, long ts);



}
