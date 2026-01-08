package co.highfive.petrolstation.pos;

import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.customers_settings.dto.LookupDto;
import co.highfive.petrolstation.data.local.entities.PosCategoryEntity;
import co.highfive.petrolstation.data.local.entities.PosPaymentTypeEntity;

public class PosSettingsMappers {

    public static List<PosCategoryEntity> toCategories(List<LookupDto> dtos) {
        List<PosCategoryEntity> out = new ArrayList<>();
        if (dtos == null) return out;
        for (LookupDto d : dtos) {
            if (d == null) continue;
            PosCategoryEntity e = new PosCategoryEntity();
            e.id = d.id;
            e.value2 = d.value2;
            e.name = d.name;
            e.enName = d.en_name;
            out.add(e);
        }
        return out;
    }

    public static List<PosPaymentTypeEntity> toPaymentTypes(List<LookupDto> dtos) {
        List<PosPaymentTypeEntity> out = new ArrayList<>();
        if (dtos == null) return out;
        for (LookupDto d : dtos) {
            if (d == null) continue;
            PosPaymentTypeEntity e = new PosPaymentTypeEntity();
            e.id = d.id;
            e.value2 = d.value2;
            e.name = d.name;
            e.enName = d.en_name;
            out.add(e);
        }
        return out;
    }
}
