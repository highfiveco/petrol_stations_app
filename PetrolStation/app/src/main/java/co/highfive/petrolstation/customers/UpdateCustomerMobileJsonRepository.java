package co.highfive.petrolstation.customers;

import java.util.List;

import co.highfive.petrolstation.customers.dto.UpdateMobileJsonItemDto;
import co.highfive.petrolstation.data.local.dao.CustomerDao;
import co.highfive.petrolstation.network.SimpleStatusResponse;

public class UpdateCustomerMobileJsonRepository {

    private final CustomerDao customerDao;

    public UpdateCustomerMobileJsonRepository(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    /**
     * عند نجاح السيرفر، نطبّق نفس التعديلات محلياً.
     * (لأن الريسبونس ما يرجّع data)
     */
    public void applyIfSuccess(SimpleStatusResponse response, List<UpdateMobileJsonItemDto> sentList) {
        if (response == null || !response.status) return;
        if (sentList == null || sentList.isEmpty()) return;

        for (UpdateMobileJsonItemDto it : sentList) {
            if (it == null) continue;
            if (it.customer_id == null || it.customer_id.trim().isEmpty()) continue;

            int id;
            try {
                id = Integer.parseInt(it.customer_id.trim());
            } catch (Exception ignore) {
                continue;
            }

            String mobile = it.phone != null ? it.phone.trim() : null;
            if (mobile == null || mobile.isEmpty()) continue;

            customerDao.updateMobile(id, mobile);
        }
    }
}
