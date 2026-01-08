package co.highfive.petrolstation.vehicles;

import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.customers_settings.dto.LookupDto;
import co.highfive.petrolstation.data.local.entities.VehicleColorEntity;
import co.highfive.petrolstation.data.local.entities.VehicleTypeEntity;

public class CustomerVehicleSettingsMappers {

    public static List<VehicleTypeEntity> toTypes(List<LookupDto> dtos) {
        List<VehicleTypeEntity> out = new ArrayList<>();
        if (dtos == null) return out;
        for (LookupDto d : dtos) {
            if (d == null) continue;
            VehicleTypeEntity e = new VehicleTypeEntity();
            e.id = d.id;
            e.value2 = d.value2;
            e.name = d.name;
            e.enName = d.en_name;
            out.add(e);
        }
        return out;
    }

    public static List<VehicleColorEntity> toColors(List<LookupDto> dtos) {
        List<VehicleColorEntity> out = new ArrayList<>();
        if (dtos == null) return out;
        for (LookupDto d : dtos) {
            if (d == null) continue;
            VehicleColorEntity e = new VehicleColorEntity();
            e.id = d.id;
            e.value2 = d.value2;
            e.name = d.name;
            e.enName = d.en_name;
            out.add(e);
        }
        return out;
    }
}
