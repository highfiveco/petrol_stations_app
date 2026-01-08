package co.highfive.petrolstation.customers;

import com.google.gson.Gson;

import java.util.List;

import co.highfive.petrolstation.auth.dto.SettingDto;
import co.highfive.petrolstation.customers.dto.CustomersData;
import co.highfive.petrolstation.data.local.dao.CustomerDao;
import co.highfive.petrolstation.data.local.dao.CustomersMetaDao;
import co.highfive.petrolstation.data.local.entities.CustomerEntity;
import co.highfive.petrolstation.data.local.entities.CustomersMetaCacheEntity;

public class CustomersRepository {

    private final CustomerDao customerDao;
    private final CustomersMetaDao metaDao;
    private final Gson gson;

    public CustomersRepository(CustomerDao customerDao, CustomersMetaDao metaDao, Gson gson) {
        this.customerDao = customerDao;
        this.metaDao = metaDao;
        this.gson = gson;
    }

    /** يحفظ البيانات القادمة من السيرفر داخل Room (بدون أي استدعاء للشبكة هنا) */
    public void saveFromResponse(CustomersData data) {
        if (data == null) return;

        // Refresh كامل
        customerDao.clear();
        metaDao.clear();

        List<CustomerEntity> entities = CustomersMappers.toEntities(data.customers);
        customerDao.upsertAll(entities);

        CustomersMetaCacheEntity meta = new CustomersMetaCacheEntity();
        meta.id = 1;

        meta.sms = data.sms;
        meta.viewLog = data.view_log;
        meta.viewFinancialMove = data.view_financial_move;
        meta.updateCustomers = data.update_customers;
        meta.addCustomers = data.add_customers;
        meta.viewReminders = data.view_reminders;
        meta.addReminders = data.add_reminders;
        meta.deleteReminders = data.delete_reminders;
        meta.updateMobile = data.update_mobile;
        meta.viewCustomerVehicles = data.view_customer_vehicles;

        SettingDto setting = data.setting;
        meta.settingJson = setting != null ? gson.toJson(setting) : null;
        meta.updatedAt = System.currentTimeMillis();

        metaDao.upsert(meta);
    }

    // ===== قراءة من Room =====
    public List<CustomerEntity> getAll() {
        return customerDao.getAll();
    }

    public CustomerEntity getById(int id) {
        return customerDao.getById(id);
    }

    public List<CustomerEntity> search(String q) {
        return customerDao.search(q);
    }

    public CustomersMetaCacheEntity getMeta() {
        return metaDao.get();
    }
}
