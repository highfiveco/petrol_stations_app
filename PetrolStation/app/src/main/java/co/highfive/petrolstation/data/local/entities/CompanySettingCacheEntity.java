package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "company_setting_cache")
public class CompanySettingCacheEntity {

    @PrimaryKey
    public int id = 1; // سجل واحد فقط

    public String json;      // نخزن BaseResponse<CompanySettingData> كـ JSON
    public long updatedAt;   // وقت آخر تحديث
}
