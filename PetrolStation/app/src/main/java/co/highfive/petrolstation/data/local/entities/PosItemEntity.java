package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.Index;

@Entity(
        tableName = "pos_items",
        primaryKeys = {"id", "queryCategoryId", "queryName"},
        indices = {
                @Index("id"),
                @Index("queryCategoryId")
        }
)
public class PosItemEntity {

    public int id;

    public String name;
    public int negativeCheck;
    public double price;
    public String barcode;
    public String icon;

    // من الريكوست (لتخزين النتائج حسب الفلتر)
    public Integer queryCategoryId; // nullable
    public String queryName;        // nullable أو "" للتوحيد

    public long updatedAt;
}
