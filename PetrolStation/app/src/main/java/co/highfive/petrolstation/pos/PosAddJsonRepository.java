package co.highfive.petrolstation.pos;

import com.google.gson.Gson;

import java.util.List;

import co.highfive.petrolstation.customers.dto.InvoiceDto;
import co.highfive.petrolstation.data.local.dao.PosAddJsonSyncCacheDao;
import co.highfive.petrolstation.data.local.dao.PosInvoiceDao;
import co.highfive.petrolstation.data.local.dao.PosInvoiceDetailDao;
import co.highfive.petrolstation.data.local.entities.PosAddJsonSyncCacheEntity;
import co.highfive.petrolstation.pos.dto.PosAddJsonResponse;

public class PosAddJsonRepository {

    private final PosInvoiceDao invoiceDao;
    private final PosInvoiceDetailDao detailDao;
    private final PosAddJsonSyncCacheDao syncCacheDao;
    private final Gson gson;

    public PosAddJsonRepository(
            PosInvoiceDao invoiceDao,
            PosInvoiceDetailDao detailDao,
            PosAddJsonSyncCacheDao syncCacheDao,
            Gson gson
    ) {
        this.invoiceDao = invoiceDao;
        this.detailDao = detailDao;
        this.syncCacheDao = syncCacheDao;
        this.gson = gson;
    }

    public void saveSyncResult(String requestJson, String rawResponseJson, PosAddJsonResponse resp) {

        // 1) خزّن الكاش دائماً
        PosAddJsonSyncCacheEntity cache = new PosAddJsonSyncCacheEntity();
        cache.requestJson = requestJson;
        cache.responseJson = rawResponseJson;
        cache.createdAt = System.currentTimeMillis();

        if (resp != null) {
            cache.message = resp.message;
            if (resp.data != null) {
                cache.successCount = resp.data.success_count;
                cache.failCount = resp.data.fail_count;
                cache.errorsJson = resp.data.errors != null ? gson.toJson(resp.data.errors) : null;
            }
        }

        syncCacheDao.insert(cache);

        // 2) إذا status=true وفيه added_invoices -> خزّنهم
        if (resp == null || !resp.status || resp.data == null || resp.data.added_invoices == null) return;

        for (InvoiceDto inv : resp.data.added_invoices) {
            if (inv == null) continue;

            // upsert invoice
            invoiceDao.upsert(PosInvoiceMappers.toInvoiceEntity(inv, gson));

            // replace details
            detailDao.clearForInvoice(inv.id);
            detailDao.upsertAll(PosInvoiceMappers.toDetailEntities(inv, gson));
        }
    }
}
