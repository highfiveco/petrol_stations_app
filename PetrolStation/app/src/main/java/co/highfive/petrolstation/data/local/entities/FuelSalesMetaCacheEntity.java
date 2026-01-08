package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "fuel_sales_meta_cache")
public class FuelSalesMetaCacheEntity {

    @PrimaryKey
    public String key; // "all" أو "account:2"

    public Integer currentPage;
    public Integer lastPage;
    public Integer perPage;
    public Integer total;

    public String nextPageUrl;
    public String prevPageUrl;

    public long updatedAt;
}
