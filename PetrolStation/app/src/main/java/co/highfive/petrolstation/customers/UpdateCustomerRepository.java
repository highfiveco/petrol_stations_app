package co.highfive.petrolstation.customers;

import com.google.gson.Gson;

import co.highfive.petrolstation.auth.dto.SettingDto;
import co.highfive.petrolstation.customers.dto.CustomerDto;
import co.highfive.petrolstation.customers.dto.UpdateCustomerResponse;
import co.highfive.petrolstation.data.local.dao.CustomerDao;
import co.highfive.petrolstation.data.local.dao.CustomersMetaDao;
import co.highfive.petrolstation.data.local.entities.CustomerEntity;
import co.highfive.petrolstation.data.local.entities.CustomersMetaCacheEntity;

public class UpdateCustomerRepository {

    private final CustomerDao customerDao;
    private final CustomersMetaDao metaDao;
    private final Gson gson;

    public UpdateCustomerRepository(CustomerDao customerDao, CustomersMetaDao metaDao, Gson gson) {
        this.customerDao = customerDao;
        this.metaDao = metaDao;
        this.gson = gson;
    }

    public void saveFromResponse(UpdateCustomerResponse response) {
        if (response == null || !response.status) return;

        CustomerDto customer = response.getCustomerOrNull(gson);
        if (customer != null) {
            // merge بسيط: إذا كان موجود نحافظ على حقول قد لا يرجعها السيرفر
            CustomerEntity existing = customerDao.getById(customer.id);
            CustomerEntity e = (existing != null) ? existing : new CustomerEntity();

            // اكتب كل الحقول التي يرجعها السيرفر
            e.id = customer.id;
            e.typeCustomer = customer.type_customer;
            e.customerClassify = customer.customer_classify;
            e.name = customer.name;
            e.accountId = customer.account_id;
            e.mobile = customer.mobile;
            e.status = customer.status;
            e.customerStatus = customer.customer_status;
            e.customerClassifyName = customer.customer_classify_name;
            e.typeCustomerName = customer.type_customer_name;
            e.balance = customer.balance;
            e.campaignName = customer.campaign_name;
            e.remainingAmount = customer.remaining_amount;

            e.address = customer.address;
            e.assealNo = customer.asseal_no;

            customerDao.upsert(e);
        }

        // (اختياري) حفظ setting القادمة
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
