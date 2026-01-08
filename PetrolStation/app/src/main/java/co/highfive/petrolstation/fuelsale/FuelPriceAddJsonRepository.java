package co.highfive.petrolstation.fuelsale;

import com.google.gson.Gson;

import java.util.Collections;
import java.util.List;

import co.highfive.petrolstation.customers.dto.InvoiceDto;
import co.highfive.petrolstation.data.local.dao.FuelPriceAddJsonSyncCacheDao;
import co.highfive.petrolstation.data.local.dao.FuelSalesDao;
import co.highfive.petrolstation.data.local.dao.InvoiceDetailDao;
import co.highfive.petrolstation.data.local.entities.FuelPriceAddJsonSyncCacheEntity;
import co.highfive.petrolstation.fuelsale.dto.FuelPriceAddJsonResponse;
import co.highfive.petrolstation.fuelsale.dto.FuelPriceAddJsonResponseData;

public class FuelPriceAddJsonRepository {

    private final FuelSalesDao fuelSalesDao;
    private final InvoiceDetailDao detailDao;
    private final FuelPriceAddJsonSyncCacheDao cacheDao; // اختياري
    private final Gson gson;

    public FuelPriceAddJsonRepository(
            FuelSalesDao fuelSalesDao,
            InvoiceDetailDao detailDao,
            FuelPriceAddJsonSyncCacheDao cacheDao,
            Gson gson
    ) {
        this.fuelSalesDao = fuelSalesDao;
        this.detailDao = detailDao;
        this.cacheDao = cacheDao;
        this.gson = gson;
    }

    public void saveFromResponse(FuelPriceAddJsonResponse response) {
        if (response == null) return;

        // 1) cache آخر نتيجة (اختياري)
        if (cacheDao != null) {
            FuelPriceAddJsonSyncCacheEntity c = new FuelPriceAddJsonSyncCacheEntity();
            c.id = 1;
            c.responseJson = gson.toJson(response);
            c.updatedAt = System.currentTimeMillis();
            cacheDao.upsert(c);
        }

        // 2) إذا status=false ما نخزن invoices
        if (!response.status) return;

        FuelPriceAddJsonResponseData data = response.getDataOrNull(gson);
        if (data == null || data.added_invoices == null || data.added_invoices.isEmpty()) return;

        List<InvoiceDto> invoices = data.added_invoices;

        // 3) خزّن invoices كـ FuelSaleEntity
        fuelSalesDao.upsertAll(FuelSalesMappers.toFuelSaleEntities(invoices, gson));

        // 4) خزّن details
        detailDao.upsertAll(FuelSalesMappers.toDetailEntities(invoices, gson));
    }
}
