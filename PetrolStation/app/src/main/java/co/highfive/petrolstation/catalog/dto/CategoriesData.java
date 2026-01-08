package co.highfive.petrolstation.catalog.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import co.highfive.petrolstation.auth.dto.SettingDto;

public class CategoriesData {

    @SerializedName("categories")
    public List<CategoryDto> categories;

    @SerializedName("total_categories")
    public Integer total_categories;

    @SerializedName("total_items")
    public Integer total_items;

    @SerializedName("setting")
    public SettingDto setting;
}
