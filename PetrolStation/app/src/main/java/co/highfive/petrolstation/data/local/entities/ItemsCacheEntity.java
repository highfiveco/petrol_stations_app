package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "items_cache")
public class ItemsCacheEntity {
    @PrimaryKey
    public int id;
    public int category;
    public String name;
    public int negativeCheck;
    public double price;
    public String barcode;
    public String icon;


//    public Integer lastCategoryId;
//    public String lastName;
//
//    public long updatedAt;
}
