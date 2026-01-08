package co.highfive.petrolstation.fuelsales;

import com.google.gson.Gson;

import java.util.List;

import co.highfive.petrolstation.customers.dto.InvoiceDto;
import co.highfive.petrolstation.data.local.dao.FuelSalesDao;
import co.highfive.petrolstation.data.local.dao.FuelSalesMetaDao;
import co.highfive.petrolstation.data.local.dao.InvoiceDetailDao;
import co.highfive.petrolstation.data.local.entities.FuelSalesMetaCacheEntity;
import co.highfive.petrolstation.invoices.InvoicesListMappers; // لإعادة استخدام تفاصيل الفاتورة
import co.highfive.petrolstation.network.PaginatedResponse;

public class FuelSalesRepository {

    private final FuelSalesDao fuelSalesDao;
    private final InvoiceDetailDao detailDao;
    private final FuelSalesMetaDao metaDao;
    private final Gson gson;

    public FuelSalesRepository(FuelSalesDao fuelSalesDao, InvoiceDetailDao detailDao, FuelSalesMetaDao metaDao, Gson gson) {
        this.fuelSalesDao = fuelSalesDao;
        this.detailDao = detailDao;
        this.metaDao = metaDao;
        this.gson = gson;
    }

    private String metaKey(Integer accountId) {
        return (accountId == null) ? "all" : ("account:" + accountId);
    }

    public void clearForFilter(Integer accountId) {
        fuelSalesDao.clearByAccountFilter(accountId);
        metaDao.clear(metaKey(accountId));
    }

    public void savePage(Integer queryAccountId, PaginatedResponse<InvoiceDto> page) {
        if (page == null || page.data == null) return;

        fuelSalesDao.upsertAll(FuelSalesMappers.toEntities(page.data, queryAccountId, gson));
        detailDao.upsertAll(InvoicesListMappers.toDetailEntities(page.data, gson)); // نفس details table

        FuelSalesMetaCacheEntity meta = new FuelSalesMetaCacheEntity();
        meta.key = metaKey(queryAccountId);
        meta.currentPage = page.current_page;
        meta.lastPage = page.last_page;
        meta.perPage = page.per_page;
        meta.total = page.total;
        meta.nextPageUrl = page.next_page_url;
        meta.prevPageUrl = page.prev_page_url;
        meta.updatedAt = System.currentTimeMillis();
        metaDao.upsert(meta);
    }

    public List<co.highfive.petrolstation.data.local.entities.FuelSaleEntity> getFuelSales(Integer accountIdFilter) {
        return fuelSalesDao.getByAccountFilter(accountIdFilter);
    }

    public FuelSalesMetaCacheEntity getMeta(Integer accountIdFilter) {
        return metaDao.get(metaKey(accountIdFilter));
    }
}
