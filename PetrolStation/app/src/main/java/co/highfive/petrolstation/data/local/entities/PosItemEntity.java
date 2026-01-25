package co.highfive.petrolstation.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;

@Entity(
        tableName = "pos_items",
        primaryKeys = {"id", "queryCategoryId", "queryName"},
        indices = {
                @Index("id"),
                @Index("queryCategoryId"),
                @Index("queryName")
        }
)
public class PosItemEntity {

    public int id;

    public String name;
    public int negativeCheck;
    public double price;
    public String barcode;
    public String icon;

    // IMPORTANT: must NOT be nullable because it's part of the primary key
    public int queryCategoryId; // 0 means "no filter"

    @NonNull
    public String queryName = ""; // empty means "no name filter"

    public long updatedAt;
}
