package co.highfive.petrolstation.customers_offline;

import com.google.gson.Gson;

import java.util.List;

import co.highfive.petrolstation.customers.dto.CustomersData;
import co.highfive.petrolstation.data.local.dao.CustomerDao;
import co.highfive.petrolstation.data.local.dao.CustomerVehicleDao;
import co.highfive.petrolstation.data.local.dao.InvoiceDao;
import co.highfive.petrolstation.data.local.dao.InvoiceDetailDao;
import co.highfive.petrolstation.data.local.entities.CustomerEntity;
import co.highfive.petrolstation.data.local.entities.CustomerVehicleEntity;
import co.highfive.petrolstation.data.local.entities.InvoiceDetailEntity;
import co.highfive.petrolstation.data.local.entities.InvoiceEntity;

public class CustomersOfflineRepository {

    private final CustomerDao customerDao;
    private final CustomerVehicleDao vehicleDao;
    private final InvoiceDao invoiceDao;
    private final InvoiceDetailDao invoiceDetailDao;
    private final Gson gson;

    public CustomersOfflineRepository(
            CustomerDao customerDao,
            CustomerVehicleDao vehicleDao,
            InvoiceDao invoiceDao,
            InvoiceDetailDao invoiceDetailDao,
            Gson gson
    ) {
        this.customerDao = customerDao;
        this.vehicleDao = vehicleDao;
        this.invoiceDao = invoiceDao;
        this.invoiceDetailDao = invoiceDetailDao;
        this.gson = gson;
    }

    public void clearAll() {
        customerDao.clear();
        vehicleDao.clear();
        invoiceDetailDao.clear();
//        invoiceDao.clear();
    }

    /** تُستدعى لكل صفحة */
    public void savePage(CustomersData data) {
        if (data == null || data.customers == null) return;

        List<CustomerEntity> customers = CustomersOfflineMappers.toCustomerEntities(data.customers);
        List<CustomerVehicleEntity> vehicles = CustomersOfflineMappers.toVehicleEntities(data.customers);
        List<InvoiceEntity> invoices = CustomersOfflineMappers.toInvoiceEntities(data.customers, gson);
        List<InvoiceDetailEntity> details = CustomersOfflineMappers.toInvoiceDetailEntities(data.customers, gson);

        customerDao.upsertAll(customers);
        vehicleDao.upsertAll(vehicles);
        invoiceDao.upsertAll(invoices);
        invoiceDetailDao.upsertAll(details);
    }
}
