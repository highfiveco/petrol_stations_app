package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pos_items_cache")
public class PosItemsCacheEntity {
    @PrimaryKey public int id = 1;

    public String settingJson;

    public Integer lastCategoryId;
    public String lastName;

    public long updatedAt;
}
