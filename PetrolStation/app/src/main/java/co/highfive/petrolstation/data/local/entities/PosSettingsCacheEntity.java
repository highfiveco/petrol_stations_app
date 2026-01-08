package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pos_settings_cache")
public class PosSettingsCacheEntity {
    @PrimaryKey public int id = 1;

    public String settingJson;
    public long updatedAt;
}
