package co.highfive.petrolstation.vehicles;

import com.google.gson.Gson;

import co.highfive.petrolstation.data.local.dao.CustomerVehiclesSettingsCacheDao;
import co.highfive.petrolstation.data.local.dao.VehicleColorDao;
import co.highfive.petrolstation.data.local.dao.VehicleTypeDao;
import co.highfive.petrolstation.data.local.entities.CustomerVehiclesSettingsCacheEntity;
import co.highfive.petrolstation.vehicles.dto.CustomerVehiclesSettingsData;

public class CustomerVehiclesSettingsRepository {

    private final VehicleTypeDao typeDao;
    private final VehicleColorDao colorDao;
    private final CustomerVehiclesSettingsCacheDao cacheDao;
    private final Gson gson;

    public CustomerVehiclesSettingsRepository(
            VehicleTypeDao typeDao,
            VehicleColorDao colorDao,
            CustomerVehiclesSettingsCacheDao cacheDao,
            Gson gson
    ) {
        this.typeDao = typeDao;
        this.colorDao = colorDao;
        this.cacheDao = cacheDao;
        this.gson = gson;
    }

    public void save(CustomerVehiclesSettingsData data) {
        if (data == null) return;

        typeDao.clear();
        colorDao.clear();

        typeDao.upsertAll(CustomerVehicleSettingsMappers.toTypes(data.vehicle_type));
        colorDao.upsertAll(CustomerVehicleSettingsMappers.toColors(data.vehicle_color));

        CustomerVehiclesSettingsCacheEntity cache = new CustomerVehiclesSettingsCacheEntity();
        cache.id = 1;
        cache.settingJson = (data.setting != null) ? gson.toJson(data.setting) : null;
        cache.updatedAt = System.currentTimeMillis();
        cacheDao.upsert(cache);
    }
}
