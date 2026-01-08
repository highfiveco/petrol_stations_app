package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "customer_vehicle_settings_cache")
public class CustomerVehiclesSettingsCacheEntity {
    @PrimaryKey public int id = 1;

    public String settingJson;
    public long updatedAt;
}
