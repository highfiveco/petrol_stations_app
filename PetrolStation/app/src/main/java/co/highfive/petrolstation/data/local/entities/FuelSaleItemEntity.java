package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "fuel_items")
public class FuelSaleItemEntity {
    @PrimaryKey public int id;

    public String name;
    public Integer negativeCheck;
    public Double price;
    public String barcode;
    public String icon;
}
