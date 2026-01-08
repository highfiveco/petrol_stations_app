package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "get_setting_cache")
public class GetSettingCacheEntity {

    @PrimaryKey
    public int id = 1;

    public String json;     // نخزن BaseResponse<GetSettingData> كـ JSON
    public long updatedAt;
}
