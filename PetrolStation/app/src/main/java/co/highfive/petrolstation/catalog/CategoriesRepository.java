package co.highfive.petrolstation.catalog;

import com.google.gson.Gson;

import java.util.List;

import co.highfive.petrolstation.auth.dto.SettingDto;
import co.highfive.petrolstation.catalog.dto.CategoriesData;
import co.highfive.petrolstation.data.local.dao.CategoryDao;
import co.highfive.petrolstation.data.local.dao.ItemDao;
import co.highfive.petrolstation.data.local.dao.CategoriesMetaDao;
import co.highfive.petrolstation.data.local.entities.CategoriesMetaCacheEntity;
import co.highfive.petrolstation.data.local.entities.CategoryEntity;
import co.highfive.petrolstation.data.local.entities.ItemEntity;

public class CategoriesRepository {

    private final CategoryDao categoryDao;
    private final ItemDao itemDao;
    private final CategoriesMetaDao metaDao;
    private final Gson gson;

    public CategoriesRepository(CategoryDao categoryDao, ItemDao itemDao, CategoriesMetaDao metaDao, Gson gson) {
        this.categoryDao = categoryDao;
        this.itemDao = itemDao;
        this.metaDao = metaDao;
        this.gson = gson;
    }

    /** يحفظ البيانات القادمة من السيرفر داخل Room (بدون أي استدعاء للشبكة هنا) */
    public void saveFromResponse(CategoriesData data) {
        if (data == null) return;

        // سياسة: نعمل refresh كامل
        categoryDao.clear();
        itemDao.clear();
        metaDao.clear();

        List<CategoryEntity> categories = CatalogMappers.toCategoryEntities(data.categories);
        List<ItemEntity> items = CatalogMappers.toItemEntities(data.categories);

        categoryDao.upsertAll(categories);
        itemDao.upsertAll(items);

        CategoriesMetaCacheEntity meta = new CategoriesMetaCacheEntity();
        meta.id = 1;
        meta.totalCategories = data.total_categories;
        meta.totalItems = data.total_items;

        SettingDto setting = data.setting;
        meta.settingJson = setting != null ? gson.toJson(setting) : null;

        meta.updatedAt = System.currentTimeMillis();
        metaDao.upsert(meta);
    }

    // ===== قراءة من Room =====
    public List<CategoryEntity> getAllCategories() {
        return categoryDao.getAll();
    }

    public List<ItemEntity> getItemsByCategory(int categoryId) {
        return itemDao.getByCategory(categoryId);
    }

    public List<ItemEntity> getAllItems() {
        return itemDao.getAll();
    }

    public CategoriesMetaCacheEntity getMeta() {
        return metaDao.get();
    }
}
