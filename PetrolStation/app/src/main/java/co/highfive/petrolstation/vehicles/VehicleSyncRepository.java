package co.highfive.petrolstation.vehicles;

import com.google.gson.Gson;

import java.util.List;

import co.highfive.petrolstation.data.local.dao.CustomerVehicleDao;
import co.highfive.petrolstation.data.local.dao.VehicleSyncResultCacheDao;
import co.highfive.petrolstation.data.local.entities.CustomerVehicleEntity;
import co.highfive.petrolstation.data.local.entities.VehicleSyncResultCacheEntity;
import co.highfive.petrolstation.vehicles.dto.ProcessedVehicleDto;
import co.highfive.petrolstation.vehicles.dto.VehicleSyncResponse;

public class VehicleSyncRepository {

    private final CustomerVehicleDao vehicleDao;
    private final VehicleSyncResultCacheDao cacheDao; // اختياري
    private final Gson gson;

    public VehicleSyncRepository(CustomerVehicleDao vehicleDao, VehicleSyncResultCacheDao cacheDao, Gson gson) {
        this.vehicleDao = vehicleDao;
        this.cacheDao = cacheDao;
        this.gson = gson;
    }

    public void saveFromResponse(VehicleSyncResponse response) {
        if (response == null) return;

        // 1) خزّن نتيجة sync كـ JSON (اختياري)
        if (cacheDao != null) {
            VehicleSyncResultCacheEntity cache = new VehicleSyncResultCacheEntity();
            cache.id = 1;
            cache.responseJson = gson.toJson(response);
            cache.updatedAt = System.currentTimeMillis();
            cacheDao.upsert(cache);
        }

        // 2) إذا status true: طبّق processed_vehicles على Room
        if (!response.status || response.data == null || response.data.processed_vehicles == null) return;

        List<ProcessedVehicleDto> list = response.data.processed_vehicles;
        for (ProcessedVehicleDto p : list) {
            if (p == null) continue;

            CustomerVehicleEntity existing = vehicleDao.getById(p.id);
            CustomerVehicleEntity e = (existing != null) ? existing : new CustomerVehicleEntity();

            e.id = p.id;
            if (p.customer_id != null) e.customerId = p.customer_id;

            // تحديثات مؤكدة
            if (p.vehicle_number != null) e.vehicleNumber = p.vehicle_number;
            if (p.model != null) e.model = p.model;
            if (p.license_expiry_date != null) e.licenseExpiryDate = p.license_expiry_date;

            // أسماء من السيرفر
            if (p.vehicle_type_name != null) e.vehicleTypeName = p.vehicle_type_name;
            if (p.vehicle_color_name != null) e.vehicleColorName = p.vehicle_color_name;

            // ملاحظة: processed_vehicles لا يرجع vehicle_type/vehicle_color IDs ولا notes دائماً
            // لذلك لا نمسحها — نخلي الموجود.

            vehicleDao.upsert(e);
        }
    }
}
