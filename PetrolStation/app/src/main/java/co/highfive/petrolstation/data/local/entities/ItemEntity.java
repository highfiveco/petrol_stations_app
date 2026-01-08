package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "items")
public class ItemEntity {

    @PrimaryKey
    public int id;

    public int categoryId;           // FK منطقي (بدون تعريف ForeignKey الآن لتبسيط)
    public String name;
    public Integer negativeCheck;
    public Double price;
    public String barcode;
    public String icon;
}
