package co.highfive.petrolstation.pos;

import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.data.local.entities.PosItemEntity;
import co.highfive.petrolstation.pos.dto.PosItemDto;

public class PosItemsMappers {

    public static List<PosItemEntity> toEntities(
            List<PosItemDto> items,
            Integer queryCategoryId,
            String queryName
    ) {
        List<PosItemEntity> out = new ArrayList<>();
        if (items == null) return out;

        String safeName = (queryName == null) ? "" : queryName;

        for (PosItemDto d : items) {
            if (d == null) continue;

            PosItemEntity e = new PosItemEntity();
            e.id = d.id;
            e.name = d.name;
            e.negativeCheck = d.negative_check;
            e.price = d.price;
            e.barcode = d.barcode;
            e.icon = d.icon;

            e.queryCategoryId = queryCategoryId;
            e.queryName = safeName;

            e.updatedAt = System.currentTimeMillis();
            out.add(e);
        }
        return out;
    }
}
