package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories_meta_cache")
public class CategoriesMetaCacheEntity {

    @PrimaryKey
    public int id = 1;

    public Integer totalCategories;
    public Integer totalItems;

    // نخزن setting كـ JSON لتجنب تكرار مودل/جداول
    public String settingJson;

    public long updatedAt;
}
