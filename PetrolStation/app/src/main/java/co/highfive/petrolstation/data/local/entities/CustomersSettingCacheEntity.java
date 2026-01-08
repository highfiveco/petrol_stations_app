package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "customers_setting_cache")
public class CustomersSettingCacheEntity {

    @PrimaryKey
    public int id = 1;

    public String settingJson; // JSON لكائن setting فقط
    public long updatedAt;
}
