package co.highfive.petrolstation.fuelsale;

import com.google.gson.Gson;
import java.util.Collections;

import co.highfive.petrolstation.customers.dto.InvoiceDto;
import co.highfive.petrolstation.data.local.dao.FuelSalesDao;
import co.highfive.petrolstation.data.local.dao.InvoiceDetailDao;
import co.highfive.petrolstation.data.local.entities.FuelSaleEntity;
import co.highfive.petrolstation.fuelsale.dto.FuelPriceAddResponse;
import co.highfive.petrolstation.invoices.InvoicesListMappers;

public class FuelPriceAddRepository {

    private final FuelSalesDao fuelSalesDao;
    private final InvoiceDetailDao detailDao;
    private final Gson gson;

    public FuelPriceAddRepository(FuelSalesDao fuelSalesDao, InvoiceDetailDao detailDao, Gson gson) {
        this.fuelSalesDao = fuelSalesDao;
        this.detailDao = detailDao;
        this.gson = gson;
    }

    public void saveFromResponse(FuelPriceAddResponse response) {
        if (response == null || !response.status) return;

        InvoiceDto inv = response.getInvoiceOrNull(gson);
        if (inv == null) return;

        // 1) خزّن FuelSaleEntity (واحد)
        FuelSaleEntity e = FuelSalesSingleMapper.toEntity(inv, gson);
        if (e != null) {
            e.queryAccountId = inv.account_id; // فلتر الشاشة
            e.updatedAt = System.currentTimeMillis();
            fuelSalesDao.upsertAll(Collections.singletonList(e));
        }

        // 2) details
        detailDao.upsertAll(InvoicesListMappers.toDetailEntities(Collections.singletonList(inv), gson));
    }
}
