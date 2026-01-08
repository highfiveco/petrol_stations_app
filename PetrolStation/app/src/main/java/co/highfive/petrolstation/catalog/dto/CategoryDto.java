package co.highfive.petrolstation.catalog.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CategoryDto {
    @SerializedName("id") public int id;
    @SerializedName("name") public String name;
    @SerializedName("value") public Integer value;

    @SerializedName("items")
    public List<ItemDto> items;

    @SerializedName("items_count")
    public Integer items_count;
}
