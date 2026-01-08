package co.highfive.petrolstation.vehicles;

import co.highfive.petrolstation.customers.dto.CustomerVehicleDto;
import co.highfive.petrolstation.data.local.dao.CustomerVehicleDao;
import co.highfive.petrolstation.data.local.entities.CustomerVehicleEntity;
import co.highfive.petrolstation.vehicles.dto.AddCustomerVehicleResponse;

public class AddCustomerVehicleRepository {

    private final CustomerVehicleDao vehicleDao;

    public AddCustomerVehicleRepository(CustomerVehicleDao vehicleDao) {
        this.vehicleDao = vehicleDao;
    }

    public void saveFromResponse(AddCustomerVehicleResponse response, com.google.gson.Gson gson) {
        if (response == null || !response.status) return;

        CustomerVehicleDto dto = response.getVehicleOrNull(gson);
        if (dto == null) return;

        CustomerVehicleEntity e = CustomerVehicleMapper.toEntity(dto);
        if (e != null) {
            vehicleDao.upsert(e);
        }
    }
}
