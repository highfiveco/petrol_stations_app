package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "invoices_list_meta_cache")
public class InvoiceListMetaCacheEntity {

    @PrimaryKey
    public String key; // مثال: "all" أو "account:16"

    public Integer currentPage;
    public Integer lastPage;
    public Integer perPage;
    public Integer total;

    public String nextPageUrl;
    public String prevPageUrl;

    public long updatedAt;
}
