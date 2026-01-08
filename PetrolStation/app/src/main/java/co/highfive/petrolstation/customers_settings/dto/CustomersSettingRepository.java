package co.highfive.petrolstation.customers_settings;

import com.google.gson.Gson;

import co.highfive.petrolstation.customers_settings.dto.GetCustomersSettingData;
import co.highfive.petrolstation.data.local.dao.CustomersSettingCacheDao;
import co.highfive.petrolstation.data.local.dao.CustomerStatusDao;
import co.highfive.petrolstation.data.local.dao.CustomerClassifyDao;
import co.highfive.petrolstation.data.local.dao.TypeCustomerDao;
import co.highfive.petrolstation.data.local.dao.UserLiteDao;

import co.highfive.petrolstation.data.local.entities.CustomersSettingCacheEntity;

public class CustomersSettingRepository {

    private final CustomersSettingCacheDao cacheDao;
    private final CustomerStatusDao statusDao;
    private final CustomerClassifyDao classifyDao;
    private final TypeCustomerDao typeCustomerDao;
    private final UserLiteDao userDao;
    private final Gson gson;

    public CustomersSettingRepository(
            CustomersSettingCacheDao cacheDao,
            CustomerStatusDao statusDao,
            CustomerClassifyDao classifyDao,
            TypeCustomerDao typeCustomerDao,
            UserLiteDao userDao,
            Gson gson
    ) {
        this.cacheDao = cacheDao;
        this.statusDao = statusDao;
        this.classifyDao = classifyDao;
        this.typeCustomerDao = typeCustomerDao;
        this.userDao = userDao;
        this.gson = gson;
    }

    public void saveFromResponse(GetCustomersSettingData data) {
        if (data == null) return;

        // refresh lists
        statusDao.clear();
        classifyDao.clear();
        typeCustomerDao.clear();
        userDao.clear();

        statusDao.upsertAll(CustomersSettingMappers.toStatuses(data.customer_status));
        classifyDao.upsertAll(CustomersSettingMappers.toClassify(data.customer_classify));
        typeCustomerDao.upsertAll(CustomersSettingMappers.toTypeCustomer(data.type_customer));
        userDao.upsertAll(CustomersSettingMappers.toUsers(data.users));

        // cache setting as JSON (كامل)
        CustomersSettingCacheEntity cache = new CustomersSettingCacheEntity();
        cache.id = 1;
        cache.settingJson = (data.setting != null) ? gson.toJson(data.setting) : null;
        cache.updatedAt = System.currentTimeMillis();
        cacheDao.upsert(cache);
    }

    public CustomersSettingCacheEntity getCache() {
        return cacheDao.get();
    }
}
