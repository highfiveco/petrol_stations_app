package co.highfive.petrolstation.customers;

import co.highfive.petrolstation.customers.dto.CustomerDto;
import co.highfive.petrolstation.data.local.entities.CustomerEntity;

public class CustomerSingleMapper {

    public static CustomerEntity toEntity(CustomerDto c) {
        if (c == null) return null;

        CustomerEntity e = new CustomerEntity();
        e.id = c.id;
        e.typeCustomer = c.type_customer;
        e.customerClassify = c.customer_classify;
        e.name = c.name;
        e.accountId = c.account_id;
        e.mobile = c.mobile;
        e.status = c.status;
        e.customerStatus = c.customer_status;
        e.customerClassifyName = c.customer_classify_name;
        e.typeCustomerName = c.type_customer_name;
        e.balance = c.balance;
        e.campaignName = c.campaign_name;
        e.remainingAmount = c.remaining_amount;

        e.address = c.address;
        e.assealNo = c.asseal_no;

        return e;
    }
}
