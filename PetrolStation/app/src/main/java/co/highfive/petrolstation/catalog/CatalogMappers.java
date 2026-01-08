package co.highfive.petrolstation.catalog;

import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.catalog.dto.CategoryDto;
import co.highfive.petrolstation.catalog.dto.ItemDto;
import co.highfive.petrolstation.data.local.entities.CategoryEntity;
import co.highfive.petrolstation.data.local.entities.ItemEntity;

public class CatalogMappers {

    public static List<CategoryEntity> toCategoryEntities(List<CategoryDto> dtos) {
        List<CategoryEntity> out = new ArrayList<>();
        if (dtos == null) return out;

        for (CategoryDto c : dtos) {
            if (c == null) continue;
            CategoryEntity e = new CategoryEntity();
            e.id = c.id;
            e.name = c.name;
            e.value = c.value;
            e.itemsCount = c.items_count;
            out.add(e);
        }
        return out;
    }

    public static List<ItemEntity> toItemEntities(List<CategoryDto> categories) {
        List<ItemEntity> out = new ArrayList<>();
        if (categories == null) return out;

        for (CategoryDto c : categories) {
            if (c == null || c.items == null) continue;

            for (ItemDto i : c.items) {
                if (i == null) continue;
                ItemEntity e = new ItemEntity();
                e.id = i.id;
                e.categoryId = c.id;
                e.name = i.name;
                e.negativeCheck = i.negative_check;
                e.price = i.price;
                e.barcode = i.barcode;
                e.icon = i.icon;
                out.add(e);
            }
        }
        return out;
    }
}
