package co.highfive.petrolstation.data.local.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Upsert;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.InvoiceEntity;
import co.highfive.petrolstation.data.local.relations.InvoiceWithDetails;

@Dao
public interface InvoiceDao {

    @Upsert
    void upsertAll(List<InvoiceEntity> list);

    @Upsert
    void upsert(InvoiceEntity entity);

    @Query("DELETE FROM invoices")
    void deleteAll();

    @Query("DELETE FROM invoices WHERE customerId = :customerId")
    void deleteByCustomer(int customerId);

    // ===== Helpers for future pagination =====

    @Query("SELECT * FROM invoices WHERE customerId = :customerId ORDER BY id DESC LIMIT :limit OFFSET :offset")
    List<InvoiceEntity> getByCustomerPaged(int customerId, int limit, int offset);


    @Query("SELECT * FROM invoices WHERE customerId = :customerId AND isFuelSale = :isFuelSale ORDER BY id DESC LIMIT :limit OFFSET :offset")
    List<InvoiceEntity> getByCustomerAndTypePaged(int customerId, int isFuelSale, int limit, int offset);


    @Query("SELECT COUNT(*) FROM invoices WHERE customerId = :customerId")
    int countByCustomer(int customerId);


    @Query("SELECT * FROM invoices WHERE customerId = :customerId ORDER BY id DESC")
    List<InvoiceEntity> getByCustomer(int customerId);

    @Query("SELECT * FROM invoices WHERE isFuelSale = 1 ORDER BY id DESC")
    List<InvoiceEntity> getFuelInvoices();

    @Query("DELETE FROM invoices")
    void clear();

    @Query("DELETE FROM invoices WHERE customerId = :customerId")
    void clearForCustomer(int customerId);

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :invoiceId")
    InvoiceWithDetails getInvoiceWithDetails(int invoiceId);

    @Query("SELECT * FROM invoices WHERE customerId = :customerId ORDER BY id DESC LIMIT :limit OFFSET :offset")
    List<InvoiceEntity> getByCustomerIdPaged(int customerId, int limit, int offset);


    // InvoiceDao.java
    @androidx.room.Transaction
    @androidx.room.Query(
            "SELECT * FROM invoices " +
                    "WHERE customerId = :customerId AND isFuelSale = :isFuelSale " +
                    "ORDER BY id DESC " +
                    "LIMIT :limit OFFSET :offset"
    )
    java.util.List<co.highfive.petrolstation.data.local.relations.InvoiceWithDetails>
    getInvoicesWithDetailsPaged(int customerId, int isFuelSale, int limit, int offset);

    @androidx.room.Query(
            "SELECT COUNT(*) FROM invoices WHERE customerId = :customerId AND isFuelSale = :isFuelSale"
    )
    int countByCustomerAndFuelSale(int customerId, int isFuelSale);

    @androidx.room.Query(
            "SELECT COUNT(*) FROM invoices WHERE customerId = :customerId  AND isFuelSale = :isFuelSale"
    )
    int countByCustomerAndType(int customerId, int isFuelSale);

    @Query("SELECT * FROM invoices WHERE isFuelSale = :invoiceType ORDER BY id DESC LIMIT :limit OFFSET :offset")
    List<InvoiceEntity> getByTypePaged(int invoiceType, int limit, int offset);

    @Query("SELECT COUNT(*) FROM invoices WHERE isFuelSale = :invoiceType")
    int countByType(int invoiceType);


    // =========================
    // ✅ Unified feed (Online + Offline) sorted by createdAtTs
    // =========================
    @Query(
            "SELECT " +
                    "'ONLINE' AS source, " +
                    "i.id AS onlineId, " +
                    "NULL AS localId, " +
                    "i.customerId AS customerId, " +
                    "i.createdAtTs AS sortTs, " +
                    "i.statement AS statement, " +
                    "i.total AS total, " +
                    "i.invoiceNo AS invoiceNo, " +
                    "NULL AS syncStatus " +
                    "FROM invoices i " +
                    "WHERE i.customerId = :customerId AND i.isFuelSale = :isFuelSale " +

                    "UNION ALL " +

                    "SELECT " +
                    "'OFFLINE' AS source, " +
                    "oi.onlineId AS onlineId, " +
                    "oi.localId AS localId, " +
                    "oi.customerId AS customerId, " +
                    "oi.createdAtTs AS sortTs, " +
                    "oi.statement AS statement, " +
                    "oi.total AS total, " +
                    "oi.invoiceNo AS invoiceNo, " +
                    "oi.syncStatus AS syncStatus " +
                    "FROM offline_invoices oi " +
                    "WHERE oi.customerId = :customerId AND oi.isFuelSale = :isFuelSale " +

                    "ORDER BY sortTs DESC " +
                    "LIMIT :limit OFFSET :offset"
    )
    List<UnifiedInvoiceRow> getUnifiedInvoicesByCustomerPaged(int customerId, int isFuelSale, int limit, int offset);

    @Query(
            "SELECT COUNT(*) FROM (" +
                    "SELECT i.id AS anyId FROM invoices i WHERE i.customerId = :customerId AND i.isFuelSale = :isFuelSale " +
                    "UNION ALL " +
                    "SELECT oi.localId AS anyId FROM offline_invoices oi WHERE oi.customerId = :customerId AND oi.isFuelSale = :isFuelSale" +
                    ")"
    )
    int countUnifiedByCustomer(int customerId, int isFuelSale);

}
