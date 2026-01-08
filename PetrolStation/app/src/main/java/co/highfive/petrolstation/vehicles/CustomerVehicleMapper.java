package co.highfive.petrolstation.vehicles;

import co.highfive.petrolstation.customers.dto.CustomerVehicleDto;
import co.highfive.petrolstation.data.local.entities.CustomerVehicleEntity;

public class CustomerVehicleMapper {

    public static CustomerVehicleEntity toEntity(CustomerVehicleDto v) {
        if (v == null) return null;

        CustomerVehicleEntity e = new CustomerVehicleEntity();
        e.id = v.id;
        e.customerId = v.customer_id;
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

        return e;
    }
}
