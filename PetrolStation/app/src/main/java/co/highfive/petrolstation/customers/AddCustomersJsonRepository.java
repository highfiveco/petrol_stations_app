package co.highfive.petrolstation.customers;

import java.util.List;

import co.highfive.petrolstation.customers.dto.AddCustomersJsonResult;
import co.highfive.petrolstation.customers.dto.AddedCustomerDto;
import co.highfive.petrolstation.data.local.dao.CustomerDao;
import co.highfive.petrolstation.data.local.entities.CustomerEntity;

public class AddCustomersJsonRepository {

    private final CustomerDao customerDao;

    public AddCustomersJsonRepository(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    /**
     * خزّن نتيجة /customers/add-json
     * - يعمل merge مع العميل الموجود (إن وجد) حتى لا نخسر بيانات أخرى
     */
    public void saveFromResult(AddCustomersJsonResult result) {
        if (result == null || result.added_customers == null) return;

        List<AddedCustomerDto> list = result.added_customers;

        for (AddedCustomerDto a : list) {
            if (a == null) continue;

            CustomerEntity existing = customerDao.getById(a.id);

            CustomerEntity e = (existing != null) ? existing : new CustomerEntity();
            e.id = a.id;

            // تحديثات مؤكدة من السيرفر
            e.name = a.name != null ? a.name : e.name;
            e.mobile = a.mobile != null ? a.mobile : e.mobile;
            e.accountId = a.account_id != null ? a.account_id : e.accountId;
            e.accountNo = a.account_no != null ? a.account_no : e.accountNo;

            customerDao.upsert(e);
        }
    }
}
