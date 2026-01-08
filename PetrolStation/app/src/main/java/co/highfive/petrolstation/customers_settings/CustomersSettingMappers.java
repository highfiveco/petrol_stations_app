package co.highfive.petrolstation.customers_settings;

import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.customers_settings.dto.CustomerStatusDto;
import co.highfive.petrolstation.customers_settings.dto.LookupDto;
import co.highfive.petrolstation.customers_settings.dto.UserLiteDto;
import co.highfive.petrolstation.data.local.entities.CustomerClassifyEntity;
import co.highfive.petrolstation.data.local.entities.CustomerStatusEntity;
import co.highfive.petrolstation.data.local.entities.TypeCustomerEntity;
import co.highfive.petrolstation.data.local.entities.UserLiteEntity;

public class CustomersSettingMappers {

    public static List<CustomerStatusEntity> toStatuses(List<CustomerStatusDto> dtos) {
        List<CustomerStatusEntity> out = new ArrayList<>();
        if (dtos == null) return out;
        for (CustomerStatusDto d : dtos) {
            if (d == null) continue;
            CustomerStatusEntity e = new CustomerStatusEntity();
            e.id = d.id;
            e.value2 = d.value2;
            e.name = d.name;
            e.enName = d.en_name;
            out.add(e);
        }
        return out;
    }

    public static List<CustomerClassifyEntity> toClassify(List<LookupDto> dtos) {
        List<CustomerClassifyEntity> out = new ArrayList<>();
        if (dtos == null) return out;
        for (LookupDto d : dtos) {
            if (d == null) continue;
            CustomerClassifyEntity e = new CustomerClassifyEntity();
            e.id = d.id;
            e.value2 = d.value2;
            e.name = d.name;
            e.enName = d.en_name;
            out.add(e);
        }
        return out;
    }

    public static List<TypeCustomerEntity> toTypeCustomer(List<LookupDto> dtos) {
        List<TypeCustomerEntity> out = new ArrayList<>();
        if (dtos == null) return out;
        for (LookupDto d : dtos) {
            if (d == null) continue;
            TypeCustomerEntity e = new TypeCustomerEntity();
            e.id = d.id;
            e.value2 = d.value2;
            e.name = d.name;
            e.enName = d.en_name;
            out.add(e);
        }
        return out;
    }

    public static List<UserLiteEntity> toUsers(List<UserLiteDto> dtos) {
        List<UserLiteEntity> out = new ArrayList<>();
        if (dtos == null) return out;
        for (UserLiteDto d : dtos) {
            if (d == null) continue;
            UserLiteEntity e = new UserLiteEntity();
            e.id = d.id;
            e.name = d.name;
            out.add(e);
        }
        return out;
    }
}
