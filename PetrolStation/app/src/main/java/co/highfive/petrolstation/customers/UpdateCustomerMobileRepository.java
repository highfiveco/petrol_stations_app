package co.highfive.petrolstation.customers;

import com.google.gson.Gson;

import co.highfive.petrolstation.auth.dto.SettingDto;
import co.highfive.petrolstation.customers.dto.CustomerDto;
import co.highfive.petrolstation.customers.dto.UpdateCustomerMobileResponse;
import co.highfive.petrolstation.data.local.dao.CustomerDao;
import co.highfive.petrolstation.data.local.dao.CustomersMetaDao;
import co.highfive.petrolstation.data.local.entities.CustomerEntity;
import co.highfive.petrolstation.data.local.entities.CustomersMetaCacheEntity;

public class UpdateCustomerMobileRepository {

    private final CustomerDao customerDao;
    private final CustomersMetaDao metaDao;
    private final Gson gson;

    public UpdateCustomerMobileRepository(CustomerDao customerDao, CustomersMetaDao metaDao, Gson gson) {
        this.customerDao = customerDao;
        this.metaDao = metaDao;
        this.gson = gson;
    }

    public void saveFromResponse(UpdateCustomerMobileResponse response) {
        if (response == null || !response.status) return;

        CustomerDto customer = response.getCustomerOrNull(gson);
        if (customer != null) {
            CustomerEntity existing = customerDao.getById(customer.id);
            CustomerEntity e = (existing != null) ? existing : new CustomerEntity();

            // تحديثات من السيرفر
            e.id = customer.id;
            e.mobile = customer.mobile;

            // بما أن السيرفر رجّع object كامل، نحدّث الباقي أيضاً
            e.name = customer.name != null ? customer.name : e.name;
            e.typeCustomer = customer.type_customer != null ? customer.type_customer : e.typeCustomer;
            e.customerClassify = customer.customer_classify != null ? customer.customer_classify : e.customerClassify;
            e.accountId = customer.account_id != null ? customer.account_id : e.accountId;
            e.status = customer.status != null ? customer.status : e.status;
            e.customerStatus = customer.customer_status != null ? customer.customer_status : e.customerStatus;
            e.customerClassifyName = customer.customer_classify_name != null ? customer.customer_classify_name : e.customerClassifyName;
            e.typeCustomerName = customer.type_customer_name != null ? customer.type_customer_name : e.typeCustomerName;
            e.balance = customer.balance != null ? customer.balance : e.balance;
            e.campaignName = customer.campaign_name != null ? customer.campaign_name : e.campaignName;
            e.remainingAmount = customer.remaining_amount != null ? customer.remaining_amount : e.remainingAmount;

            e.address = customer.address != null ? customer.address : e.address;
            e.assealNo = customer.asseal_no != null ? customer.asseal_no : e.assealNo;

            customerDao.upsert(e);
        }

        // (اختياري) تخزين setting
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
