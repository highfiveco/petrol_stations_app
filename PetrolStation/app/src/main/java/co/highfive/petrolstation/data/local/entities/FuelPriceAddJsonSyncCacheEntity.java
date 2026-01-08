package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "fuel_price_add_json_sync_cache")
public class FuelPriceAddJsonSyncCacheEntity {
    @PrimaryKey public int id = 1;

    public String responseJson; // FuelPriceAddJsonResponse كامل
    public long updatedAt;
}
