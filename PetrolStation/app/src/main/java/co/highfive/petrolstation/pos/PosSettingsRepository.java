package co.highfive.petrolstation.pos;

import com.google.gson.Gson;

import co.highfive.petrolstation.data.local.dao.PosCategoryDao;
import co.highfive.petrolstation.data.local.dao.PosPaymentTypeDao;
import co.highfive.petrolstation.data.local.dao.PosSettingsCacheDao;
import co.highfive.petrolstation.data.local.entities.PosSettingsCacheEntity;
import co.highfive.petrolstation.pos.dto.PosSettingsData;

public class PosSettingsRepository {

    private final PosCategoryDao categoryDao;
    private final PosPaymentTypeDao paymentTypeDao;
    private final PosSettingsCacheDao cacheDao;
    private final Gson gson;

    public PosSettingsRepository(
            PosCategoryDao categoryDao,
            PosPaymentTypeDao paymentTypeDao,
            PosSettingsCacheDao cacheDao,
            Gson gson
    ) {
        this.categoryDao = categoryDao;
        this.paymentTypeDao = paymentTypeDao;
        this.cacheDao = cacheDao;
        this.gson = gson;
    }

    public void save(PosSettingsData data) {
        if (data == null) return;

        categoryDao.clear();
        paymentTypeDao.clear();

        categoryDao.upsertAll(PosSettingsMappers.toCategories(data.category));
        paymentTypeDao.upsertAll(PosSettingsMappers.toPaymentTypes(data.payment_type));

        PosSettingsCacheEntity c = new PosSettingsCacheEntity();
        c.id = 1;
        c.settingJson = data.setting != null ? gson.toJson(data.setting) : null;
        c.updatedAt = System.currentTimeMillis();
        cacheDao.upsert(c);
    }
}
