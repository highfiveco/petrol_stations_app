package co.highfive.petrolstation.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "pos_items_cache",
        indices = {
                @Index(value = {"barcode"}),
                @Index(value = {"name"})
        }
)
public class PosItemCacheEntity {

    @PrimaryKey
    public int id;

    public String name;

    public int negativeCheck;

    public double price;

    public String barcode;

    public String icon;

    public long updatedAt;
}
