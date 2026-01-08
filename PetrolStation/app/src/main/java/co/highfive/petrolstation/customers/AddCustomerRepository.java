package co.highfive.petrolstation.customers;

import com.google.gson.Gson;

import co.highfive.petrolstation.auth.dto.SettingDto;
import co.highfive.petrolstation.customers.dto.AddCustomerResponse;
import co.highfive.petrolstation.data.local.dao.CustomerDao;
import co.highfive.petrolstation.data.local.dao.CustomersMetaDao;
import co.highfive.petrolstation.data.local.entities.CustomerEntity;
import co.highfive.petrolstation.data.local.entities.CustomersMetaCacheEntity;

public class AddCustomerRepository {

    private final CustomerDao customerDao;
    private final CustomersMetaDao metaDao;
    private final Gson gson;

    public AddCustomerRepository(CustomerDao customerDao, CustomersMetaDao metaDao, Gson gson) {
        this.customerDao = customerDao;
        this.metaDao = metaDao;
        this.gson = gson;
    }

    /** خزّن نتيجة add-customer في Room */
    public void saveFromResponse(AddCustomerResponse response) {
        if (response == null || !response.status || response.data == null) return;

        CustomerEntity entity = CustomerSingleMapper.toEntity(response.data);
        if (entity != null) {
            customerDao.upsert(entity);
        }

        // إذا بدك تخزن setting القادمة (اختياري)
        SettingDto setting = response.setting;
        if (setting != null) {
            CustomersMetaCacheEntity meta = metaDao.get();
            if (meta == null) meta = new CustomersMetaCacheEntity();
            meta.id = 1;
            meta.settingJson = gson.toJson(setting);
            meta.updatedAt = System.currentTimeMillis();
            metaDao.upsert(meta);
        }
    }
}
