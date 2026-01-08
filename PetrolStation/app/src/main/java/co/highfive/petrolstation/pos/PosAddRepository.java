package co.highfive.petrolstation.pos;

import com.google.gson.Gson;

import java.util.List;

import co.highfive.petrolstation.customers.dto.InvoiceDto;
import co.highfive.petrolstation.data.local.dao.PosInvoiceDao;
import co.highfive.petrolstation.data.local.dao.PosInvoiceDetailDao;
import co.highfive.petrolstation.data.local.entities.PosInvoiceEntity;
import co.highfive.petrolstation.pos.dto.PosAddResponse;

public class PosAddRepository {

    private final PosInvoiceDao invoiceDao;
    private final PosInvoiceDetailDao detailDao;
    private final Gson gson;

    public PosAddRepository(PosInvoiceDao invoiceDao, PosInvoiceDetailDao detailDao, Gson gson) {
        this.invoiceDao = invoiceDao;
        this.detailDao = detailDao;
        this.gson = gson;
    }

    public void saveFromResponse(PosAddResponse response) {
        if (response == null || !response.status) return;

        InvoiceDto invoice = response.getInvoiceOrNull(gson);
        if (invoice == null) return;

        PosInvoiceEntity invEntity = PosInvoiceMappers.toInvoiceEntity(invoice, gson);
        invoiceDao.upsert(invEntity);

        // replace details for that invoice
        detailDao.clearForInvoice(invoice.id);
        detailDao.upsertAll(PosInvoiceMappers.toDetailEntities(invoice, gson));
    }
}
