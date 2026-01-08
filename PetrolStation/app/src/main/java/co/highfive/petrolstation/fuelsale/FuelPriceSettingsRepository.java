package co.highfive.petrolstation.fuelsale;

import com.google.gson.Gson;

import co.highfive.petrolstation.data.local.dao.*;
import co.highfive.petrolstation.data.local.entities.FuelPriceSettingsCacheEntity;
import co.highfive.petrolstation.fuelsale.dto.FuelPriceSettingsData;

public class FuelPriceSettingsRepository {

    private final FuelPaymentTypeDao paymentTypeDao;
    private final FuelPumpDao pumpDao;
    private final FuelSaleItemDao itemDao;
    private final FuelCampaignDao campaignDao;
    private final FuelCampaignItemDao campaignItemDao;
    private final FuelPriceSettingsCacheDao cacheDao;
    private final Gson gson;

    public FuelPriceSettingsRepository(
            FuelPaymentTypeDao paymentTypeDao,
            FuelPumpDao pumpDao,
            FuelSaleItemDao itemDao,
            FuelCampaignDao campaignDao,
            FuelCampaignItemDao campaignItemDao,
            FuelPriceSettingsCacheDao cacheDao,
            Gson gson
    ) {
        this.paymentTypeDao = paymentTypeDao;
        this.pumpDao = pumpDao;
        this.itemDao = itemDao;
        this.campaignDao = campaignDao;
        this.campaignItemDao = campaignItemDao;
        this.cacheDao = cacheDao;
        this.gson = gson;
    }

    public void save(FuelPriceSettingsData data) {
        if (data == null) return;

        // replace snapshot
        paymentTypeDao.clear();
        pumpDao.clear();
        itemDao.clear();
        campaignItemDao.clear();
        campaignDao.clear();

        paymentTypeDao.upsertAll(FuelPriceSettingsMappers.toPaymentTypes(data.payment_type));
        pumpDao.upsertAll(FuelPriceSettingsMappers.toPumps(data.pumps));
        itemDao.upsertAll(FuelPriceSettingsMappers.toFuelItems(data.items));

        campaignDao.upsertAll(FuelPriceSettingsMappers.toCampaigns(data.campaigns));
        campaignItemDao.upsertAll(FuelPriceSettingsMappers.toCampaignItems(data.campaigns, gson));

        FuelPriceSettingsCacheEntity cache = new FuelPriceSettingsCacheEntity();
        cache.id = 1;
        cache.settingJson = (data.setting != null) ? gson.toJson(data.setting) : null;
        cache.updatedAt = System.currentTimeMillis();
        cacheDao.upsert(cache);
    }
}
