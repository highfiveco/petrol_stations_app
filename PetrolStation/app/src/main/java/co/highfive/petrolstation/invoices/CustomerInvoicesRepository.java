package co.highfive.petrolstation.invoices;

import com.google.gson.Gson;

import java.util.List;

import co.highfive.petrolstation.customers.dto.InvoiceDto;
import co.highfive.petrolstation.data.local.dao.InvoiceDetailDao;
import co.highfive.petrolstation.data.local.dao.InvoiceListDao;
import co.highfive.petrolstation.data.local.dao.InvoiceListMetaDao;
import co.highfive.petrolstation.data.local.entities.InvoiceDetailEntity;
import co.highfive.petrolstation.data.local.entities.InvoiceListEntity;
import co.highfive.petrolstation.data.local.entities.InvoiceListMetaCacheEntity;
import co.highfive.petrolstation.network.PaginatedResponse;

public class CustomerInvoicesRepository {

    private final InvoiceListDao invoiceListDao;
    private final InvoiceDetailDao detailDao;
    private final InvoiceListMetaDao metaDao;
    private final Gson gson;

    public CustomerInvoicesRepository(
            InvoiceListDao invoiceListDao,
            InvoiceDetailDao detailDao,
            InvoiceListMetaDao metaDao,
            Gson gson
    ) {
        this.invoiceListDao = invoiceListDao;
        this.detailDao = detailDao;
        this.metaDao = metaDao;
        this.gson = gson;
    }

    private String metaKey(Integer accountId) {
        return (accountId == null) ? "all" : ("account:" + accountId);
    }

    /** نستخدمها لو بدك refresh كامل قبل paging */
    public void clearForFilter(Integer accountId) {
        invoiceListDao.clearByAccountFilter(accountId);
        metaDao.clear(metaKey(accountId));
        // ملاحظة: ما نمسح invoice_details هنا لأن details قد تكون مشتركة مع offline sync
        // وإذا بدك تنظيف كامل، نعمل clearAllDetails() في سيناريو خاص.
    }

    /** تخزين صفحة واحدة (Paging-safe) */
    public void savePage(Integer queryAccountId, PaginatedResponse<InvoiceDto> page) {
        if (page == null || page.data == null) return;

        List<InvoiceListEntity> invoices = InvoicesListMappers.toInvoiceListEntities(page.data, queryAccountId, gson);
        List<InvoiceDetailEntity> details = InvoicesListMappers.toDetailEntities(page.data, gson);

        invoiceListDao.upsertAll(invoices);
        detailDao.upsertAll(details);

        InvoiceListMetaCacheEntity meta = new InvoiceListMetaCacheEntity();
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

    // قراءة أوفلاين
    public List<InvoiceListEntity> getInvoices(Integer accountIdFilter) {
        return invoiceListDao.getByAccountFilter(accountIdFilter);
    }

    public InvoiceListMetaCacheEntity getMeta(Integer accountIdFilter) {
        return metaDao.get(metaKey(accountIdFilter));
    }
}
