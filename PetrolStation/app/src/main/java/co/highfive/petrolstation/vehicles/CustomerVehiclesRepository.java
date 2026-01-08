package co.highfive.petrolstation.vehicles;

import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.customers.CustomerSingleMapper;
import co.highfive.petrolstation.customers.dto.CustomerDto;
import co.highfive.petrolstation.customers.dto.CustomerVehicleDto;
import co.highfive.petrolstation.data.local.dao.CustomerDao;
import co.highfive.petrolstation.data.local.dao.CustomerVehicleDao;
import co.highfive.petrolstation.data.local.entities.CustomerEntity;
import co.highfive.petrolstation.data.local.entities.CustomerVehicleEntity;
import co.highfive.petrolstation.vehicles.dto.CustomerVehiclesData;
import co.highfive.petrolstation.vehicles.dto.CustomerVehiclesResponse;

public class CustomerVehiclesRepository {

    private final CustomerDao customerDao;
    private final CustomerVehicleDao vehicleDao;

    public CustomerVehiclesRepository(CustomerDao customerDao, CustomerVehicleDao vehicleDao) {
        this.customerDao = customerDao;
        this.vehicleDao = vehicleDao;
    }

    public void saveFromResponse(CustomerVehiclesResponse response, com.google.gson.Gson gson) {
        if (response == null || !response.status) return;

        CustomerVehiclesData data = response.getDataOrNull(gson);
        if (data == null) return;

        // 1) upsert customer
        CustomerDto c = data.customer;
        if (c != null) {
            CustomerEntity ce = CustomerSingleMapper.toEntity(c);
            if (ce != null) customerDao.upsert(ce);
        }

        // 2) replace vehicles for this customer (حتى لو القائمة فاضية)
        int customerId = (data.customer != null) ? data.customer.id : 0;
        if (customerId > 0) {
            vehicleDao.deleteByCustomer(customerId);
        }

        List<CustomerVehicleEntity> list = new ArrayList<>();
        if (data.vehicles != null) {
            for (CustomerVehicleDto v : data.vehicles) {
                if (v == null) continue;
                CustomerVehicleEntity e = new CustomerVehicleEntity();
                e.id = v.id;
                e.customerId = (v.customer_id != null) ? v.customer_id : customerId;
                e.vehicleNumber = v.vehicle_number;
                e.vehicleType = v.vehicle_type;
                e.vehicleColor = v.vehicle_color;
                e.model = v.model;
                e.licenseExpiryDate = v.license_expiry_date;
                e.notes = v.notes;
                e.createdAt = v.created_at;
                e.vehicleTypeName = v.vehicle_type_name;
                e.vehicleColorName = v.vehicle_color_name;
                e.accountId = v.account_id;
                list.add(e);
            }
        }
        if (!list.isEmpty()) {
            vehicleDao.upsertAll(list);
        }
    }
}
