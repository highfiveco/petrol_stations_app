package co.highfive.petrolstation.pos;

import com.google.gson.Gson;

import co.highfive.petrolstation.data.local.dao.PosItemDao;
import co.highfive.petrolstation.data.local.dao.PosItemsCacheDao;
import co.highfive.petrolstation.data.local.entities.PosItemsCacheEntity;
import co.highfive.petrolstation.pos.dto.PosItemsData;

public class PosItemsRepository {

    private final PosItemDao itemDao;
    private final PosItemsCacheDao cacheDao;
    private final Gson gson;

    public PosItemsRepository(PosItemDao itemDao, PosItemsCacheDao cacheDao, Gson gson) {
        this.itemDao = itemDao;
        this.cacheDao = cacheDao;
        this.gson = gson;
    }

    public void save(PosItemsData data, Integer queryCategoryId, String queryName) {
        if (data == null) return;

        String safeName = (queryName == null) ? "" : queryName;

        // امسح نتائج نفس الفلتر (عشان ما تتكرر)
        itemDao.clearForFilter(queryCategoryId, safeName);

        // خزّن items
        itemDao.upsertAll(PosItemsMappers.toEntities(data.items.data, queryCategoryId, safeName));

        // خزّن setting + آخر فلتر
        PosItemsCacheEntity c = new PosItemsCacheEntity();
        c.id = 1;
        c.lastCategoryId = queryCategoryId;
        c.lastName = safeName;
        c.updatedAt = System.currentTimeMillis();
        cacheDao.upsert(c);
    }
}
